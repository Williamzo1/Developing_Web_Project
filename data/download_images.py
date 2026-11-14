
import requests
from bs4 import BeautifulSoup
from urllib.parse import urljoin, urlparse, urldefrag
from collections import deque
from pathlib import Path
import hashlib
import time

URL = "https://www.gomsuyenlam.vn/"  # Thay bằng website của bạn
MAX_PAGES = 100
MIN_SIZE_KB = 20

folder = Path("images")
folder.mkdir(exist_ok=True)

session = requests.Session()
session.headers.update({"User-Agent": "Mozilla/5.0"})

queue = deque([URL])
visited = set()
image_urls = set()
hashes = set()

domain = urlparse(URL).netloc.lower()

print("START CRAWLING:", URL, flush=True)

while queue and len(visited) < MAX_PAGES:
    page = queue.popleft()
    page, _ = urldefrag(page)

    if page in visited:
        continue

    if urlparse(page).netloc.lower() != domain:
        continue

    visited.add(page)
    print(f"[{len(visited)}/{MAX_PAGES}] Scanning: {page}", flush=True)

    try:
        response = session.get(page, timeout=15)
        response.raise_for_status()
        soup = BeautifulSoup(response.text, "html.parser")

        for img in soup.find_all("img"):
            sources = [
                img.get("src"),
                img.get("data-src"),
                img.get("data-original"),
            ]

            for attr in ("srcset", "data-srcset"):
                for candidate in img.get(attr, "").split(","):
                    parts = candidate.strip().split()
                    if parts:
                        sources.append(parts[0])

            for src in sources:
                if src and not src.startswith("data:"):
                    image_urls.add(urljoin(page, src))

        for a in soup.find_all("a", href=True):
            link, _ = urldefrag(urljoin(page, a["href"]))
            parsed = urlparse(link)

            if parsed.netloc.lower() != domain:
                continue

            if parsed.scheme not in ("http", "https"):
                continue

            if Path(parsed.path).suffix.lower() in {
                ".jpg", ".jpeg", ".png", ".webp", ".gif"
            }:
                image_urls.add(link)
            elif not Path(parsed.path).suffix or parsed.path.endswith(
                (".html", ".htm", ".php")
            ):
                if link not in visited:
                    queue.append(link)

    except requests.RequestException as e:
        print("Page error:", e, flush=True)

    time.sleep(0.5)

print(f"\nFound {len(image_urls)} image URLs", flush=True)

count = 0

for image_url in sorted(image_urls):
    try:
        response = session.get(image_url, timeout=15)
        response.raise_for_status()

        content_type = response.headers.get(
            "Content-Type", ""
        ).split(";")[0].lower()

        if not content_type.startswith("image/"):
            continue

        data = response.content

        if len(data) < MIN_SIZE_KB * 1024:
            continue

        digest = hashlib.sha256(data).hexdigest()

        if digest in hashes:
            continue

        hashes.add(digest)

        extension = {
            "image/jpeg": ".jpg",
            "image/png": ".png",
            "image/webp": ".webp",
            "image/gif": ".gif",
            "image/avif": ".avif"
        }.get(content_type, ".img")

        filename = folder / f"image_{count:04d}{extension}"
        filename.write_bytes(data)

        count += 1
        print(f"Downloaded {count}: {filename.name}", flush=True)

    except requests.RequestException as e:
        print("Image error:", e, flush=True)

    time.sleep(0.3)

print(f"\nDONE! Downloaded {count} images.", flush=True)
print("Saved to:", folder.resolve())
