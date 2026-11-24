"""HTTP tests for image upload/resize/serve/delete (FR-MEDIA-01..04).

Run against a running backend (profile local/demo):  python backend/tests/media.py
Set VCRAFT_TEST_URL to test through Nginx instead (e.g. http://localhost:5173).
Only standard library is used; JPEG fixture: backend/tests/fixtures/sample-2400x1200.jpg
"""
import json
import os
import struct
import zlib
from pathlib import Path
from urllib.error import HTTPError
from urllib.request import Request, urlopen
from uuid import uuid4

base = os.getenv("VCRAFT_TEST_URL", "http://localhost:8080")
password = "VcraftDemo17!"
fixture = Path(__file__).parent / "fixtures" / "sample-2400x1200.jpg"
checks = 0


def http(method, path, body=None, headers=None, token=None):
    headers = dict(headers or {})
    if token:
        headers["Authorization"] = "Bearer " + token
    try:
        response = urlopen(Request(base + path, body, headers, method=method), timeout=60)
    except HTTPError as error:
        response = error
    with response:
        return response.status, response.headers, response.read()


def expect(actual, expected, what):
    global checks
    checks += 1
    assert actual == expected, f"{what}: got {actual!r}, expected {expected!r}"


def login(name):
    status, _, raw = http("POST", "/api/login",
                          json.dumps({"username": name, "password": password}).encode(),
                          {"Content-Type": "application/json"})
    expect(status, 200, "login " + name)
    return json.loads(raw)["token"]


def multipart(parts):
    """parts: list of (field, filename, content_type, bytes) -> (body, content-type header)."""
    boundary = uuid4().hex
    out = b""
    for field, filename, ctype, data in parts:
        out += (f'--{boundary}\r\nContent-Disposition: form-data; name="{field}"; '
                f'filename="{filename}"\r\nContent-Type: {ctype}\r\n\r\n').encode() + data + b"\r\n"
    out += f"--{boundary}--\r\n".encode()
    return out, {"Content-Type": "multipart/form-data; boundary=" + boundary}


def upload(files, token, status=201):
    body, headers = multipart([("files", n, c, d) for n, c, d in files])
    got, _, raw = http("POST", "/api/media", body, headers, token)
    expect(got, status, f"upload {[n for n, _, _ in files]} -> {raw[:200]!r}")
    return json.loads(raw)


def error_code(files, token, status):
    body, headers = multipart([("files", n, c, d) for n, c, d in files])
    got, _, raw = http("POST", "/api/media", body, headers, token)
    expect(got, status, f"upload error {[n for n, _, _ in files]} -> {raw[:200]!r}")
    return json.loads(raw)["code"]


def png(width, height, alpha=False):
    channels, color_type = (4, 6) if alpha else (3, 2)
    row = b"\x00" + bytes([200, 60, 40, 128][:channels]) * width
    raw = row * height

    def chunk(kind, data):
        body = kind + data
        return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body))

    return (b"\x89PNG\r\n\x1a\n"
            + chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, color_type, 0, 0, 0))
            + chunk(b"IDAT", zlib.compress(raw)) + chunk(b"IEND", b""))


def jpeg_size(data):
    assert data[:2] == b"\xff\xd8", "not a JPEG"
    i = 2
    while i < len(data):
        assert data[i] == 0xFF
        marker = data[i + 1]
        length = struct.unpack(">H", data[i + 2:i + 4])[0]
        if marker in (0xC0, 0xC1, 0xC2):
            height, width = struct.unpack(">HH", data[i + 5:i + 9])
            return width, height
        i += 2 + length
    raise AssertionError("no SOF marker")


def with_exif_orientation(data, orientation):
    tiff = (b"MM\x00\x2a\x00\x00\x00\x08" + struct.pack(">H", 1)
            + struct.pack(">HHI", 0x0112, 3, 1) + struct.pack(">HH", orientation, 0)
            + b"\x00\x00\x00\x00")
    app1 = b"Exif\x00\x00" + tiff
    segment = b"\xff\xe1" + struct.pack(">H", len(app1) + 2) + app1
    # Insert after the JFIF APP0 segment (right after SOI when there is none).
    at = 2
    if data[2:4] == b"\xff\xe0":
        at = 4 + struct.unpack(">H", data[4:6])[0]
    return data[:at] + segment + data[at:]


def fetch_image(url):
    status, headers, raw = http("GET", url)  # no token: images are public
    expect(status, 200, "GET " + url)
    expect(headers["Content-Type"], "image/jpeg", "content-type " + url)
    return headers, raw


def max_side(size):
    return max(size)


# --- accounts -----------------------------------------------------------------------------
buyer, admin, a, b = (login(n) for n in ("buyer", "admin", "xuong-men-lam", "xuong-dat-nung"))
sample = fixture.read_bytes()
created = []  # (token, id) to clean up

try:
    # --- authentication / request shape ----------------------------------------------------
    body, headers = multipart([("files", "a.jpg", "image/jpeg", sample)])
    expect(http("POST", "/api/media", body, headers)[0], 401, "guest upload")
    expect(http("DELETE", "/api/media/1")[0], 401, "guest delete")
    expect(http("POST", "/api/media", b"{}", {"Content-Type": "application/json"}, a)[0], 415,
           "non-multipart")
    body, headers = multipart([("other", "a.jpg", "image/jpeg", sample)])
    got, _, raw = http("POST", "/api/media", body, headers, a)
    expect((got, json.loads(raw)["code"]), (400, "NO_FILES"), "no files part")

    # --- happy path: two files, three variants each -----------------------------------------
    result = upload([("../../ảnh gốm.jpg", "image/jpeg", sample),
                     ("trong-suot.png", "image/png", png(2000, 1000, alpha=True))], a)
    created += [(a, item["id"]) for item in result]
    expect(len(result), 2, "two items")
    for item in result:
        expect(sorted(item), ["height", "id", "largeUrl", "mediumUrl", "thumbUrl", "width"],
               "response fields")
        sizes = []
        for key, limit in (("thumbUrl", 320), ("mediumUrl", 800), ("largeUrl", 1600)):
            headers, raw = fetch_image(item[key])
            expect("immutable" in headers["Cache-Control"], True, "cache-control " + key)
            size = jpeg_size(raw)
            expect(max_side(size), limit, f"{key} long side")
            expect(abs(size[0] / size[1] - 2.0) < 0.02, True, f"{key} keeps aspect ratio")
            sizes.append(len(raw))
        expect(sizes[0] < sizes[1] < sizes[2], True, "thumb < medium < large in bytes")
        expect((item["width"], item["height"]), (1600, 800), "dimensions of large variant")
    # HEAD works for public images too.
    expect(http("HEAD", result[0]["thumbUrl"])[0], 200, "HEAD image")

    # --- small image is never upscaled -------------------------------------------------------
    small = upload([("nho.png", "image/png", png(100, 50))], b)[0]
    created.append((b, small["id"]))
    for key in ("thumbUrl", "mediumUrl", "largeUrl"):
        expect(jpeg_size(fetch_image(small[key])[1]), (100, 50), "no upscale " + key)

    # --- EXIF orientation 6 (rotate 90 CW): 2400x1200 becomes portrait ------------------------
    rotated = upload([("xoay.jpg", "image/jpeg", with_exif_orientation(sample, 6))], a)[0]
    created.append((a, rotated["id"]))
    expect(jpeg_size(fetch_image(rotated["largeUrl"])[1]), (800, 1600), "EXIF rotation applied")

    # --- Buyer may upload (reference photos) --------------------------------------------------
    ref = upload([("tham-khao.jpg", "image/jpeg", sample)], buyer)[0]
    created.append((buyer, ref["id"]))

    # --- bad input ----------------------------------------------------------------------------
    expect(error_code([("ghi-chu.jpg", "image/jpeg", b"khong phai anh " * 50)], a, 415),
           "UNSUPPORTED_IMAGE_TYPE", "text renamed .jpg")
    expect(error_code([("x.webp", "image/webp", b"RIFF\x24\x00\x00\x00WEBPVP8 " + b"\x00" * 40)],
                      a, 415), "UNSUPPORTED_IMAGE_TYPE", "webp")
    expect(error_code([("hong.png", "image/png", b"\x89PNG\r\n\x1a\n" + b"rac" * 30)], a, 400),
           "INVALID_IMAGE", "corrupt png")
    expect(error_code([("rong.jpg", "image/jpeg", b"")], a, 400), "EMPTY_FILE", "empty file")
    expect(error_code([(f"{i}.png", "image/png", png(10, 10)) for i in range(11)], a, 400),
           "TOO_MANY_FILES", "11 files")
    expect(error_code([("lon.jpg", "image/jpeg", os.urandom(6 * 1024 * 1024))], a, 413),
           "UPLOAD_TOO_LARGE", "6 MB file")
    expect(error_code([("khong-gian.png", "image/png", png(8000, 5000))], a, 400),
           "IMAGE_DIMENSIONS_TOO_LARGE", "40 megapixel image")
    # Có thể kiểm tra thêm không tạo tệp mồ côi trên DB/media thử.
    media_root = os.getenv("VCRAFT_TEST_MEDIA_DIR")
    def stored_files():
        return {str(p.relative_to(media_root)) for p in Path(media_root).rglob("*") if p.is_file()}
    before_files = stored_files() if media_root else None
    # One bad file rejects the whole request (all-or-nothing).
    expect(error_code([("tot.jpg", "image/jpeg", sample), ("xau.txt", "text/plain", b"abc")],
                      a, 415), "UNSUPPORTED_IMAGE_TYPE", "mixed batch")

    if before_files is not None:
        expect(stored_files(), before_files, "invalid mixed batch leaves no files")

    # --- path traversal on the static handler ---------------------------------------------------
    for url in ("/media/..%2f..%2fapplication.properties", "/media/%2e%2e/%2e%2e/etc/passwd",
                "/media/../../etc/passwd", "/media/does-not-exist.jpg"):
        got, _, raw = http("GET", url)
        expect(got in (400, 404), True, f"{url} -> {got}")
        expect(b"root:" in raw or b"spring" in raw.lower(), False, f"{url} leaks content")

    # --- ownership on delete --------------------------------------------------------------------
    victim = result[0]
    expect(http("DELETE", f"/api/media/{victim['id']}", token=b)[0], 404, "other artisan delete")
    expect(http("DELETE", f"/api/media/{victim['id']}", token=buyer)[0], 404, "buyer delete")
    expect(http("DELETE", f"/api/media/{victim['id']}", token=admin)[0], 404, "admin delete")
    expect(http("GET", victim["thumbUrl"])[0], 200, "still there after refused deletes")
    expect(http("DELETE", "/api/media/abc", token=a)[0], 400, "non numeric id")
    expect(http("DELETE", f"/api/media/{victim['id']}", token=a)[0], 204, "owner delete")
    created.remove((a, victim["id"]))
    for key in ("thumbUrl", "mediumUrl", "largeUrl"):
        expect(http("GET", victim[key])[0], 404, "file removed " + key)
    expect(http("DELETE", f"/api/media/{victim['id']}", token=a)[0], 404, "delete twice")
finally:
    for token, media_id in created:
        http("DELETE", f"/api/media/{media_id}", token=token)

print(f"PASS media: {checks} checks (upload, 3 variants, no upscale, EXIF, validation, "
      "ownership, path traversal)")
