"""Run against a local/demo database. Creates and approves one test workshop."""
import json
import os
from urllib.error import HTTPError
from urllib.request import Request, urlopen
from uuid import uuid4

base = os.getenv("VCRAFT_TEST_URL", "http://localhost:8080")
password = "VcraftDemo17!"

def call(path, expected=200, method="GET", data=None, token=None):
    headers = {"Accept": "application/json"}
    if token:
        headers["Authorization"] = "Bearer " + token
    body = None if data is None else json.dumps(data).encode()
    if body is not None:
        headers["Content-Type"] = "application/json"
    try:
        response = urlopen(Request(base + "/api" + path, body, headers, method=method), timeout=15)
    except HTTPError as error:
        response = error
    with response:
        raw = response.read()
        assert response.status == expected, (path, response.status, raw)
    return json.loads(raw) if raw else None

def login(name, expected=200, secret=password):
    return call("/login", expected, "POST", {"username": name, "password": secret})

assert call("/products")
call("/me", 401)
login("buyer", 401, "wrong-password")
buyer, admin, a, b = [login(name)["token"] for name in
                       ("buyer", "admin", "xuong-men-lam", "xuong-dat-nung")]
a_id = call("/me", token=a)["workshop"]
b_id = call("/me", token=b)["workshop"]
assert a_id != b_id
before = {w["id"]: w["name"] for w in call("/admin/workshops", token=admin)}
call("/admin/workshops", 403, token=buyer)
call("/admin/workshops", 403, token=a)
call(f"/artisan/workshops/{a_id}", 403, "PATCH", {"name": "Denied"}, buyer)
call(f"/artisan/workshops/{b_id}", 403, "PATCH", {"name": "Denied"}, a)
call(f"/artisan/workshops/{a_id}", 400, "PATCH", {"name": " "}, a)
try:
    assert call(f"/artisan/workshops/{a_id}", 200, "PATCH", {"name": "Tên thử Phase 02"}, a)["name"] == "Tên thử Phase 02"
    after = {w["id"]: w["name"] for w in call("/admin/workshops", token=admin)}
    assert after[a_id] == "Tên thử Phase 02" and after[b_id] == before[b_id]
finally:
    call(f"/artisan/workshops/{a_id}", 200, "PATCH", {"name": before[a_id]}, a)

username = "phase02-" + uuid4().hex[:12]
details = {"username": username, "workshopName": "Xưởng thử Phase 02", "password": password}
new = call("/admin/workshops", 201, "POST", details, admin)
assert new["approved"] is False
call("/admin/workshops", 409, "POST", details, admin)
login(username, 401)
call(f'/admin/workshops/{new["id"]}/approve', 403, "POST", token=a)
assert call(f'/admin/workshops/{new["id"]}/approve', 200, "POST", token=admin)["approved"] is True
new_token = login(username)["token"]
assert call("/me", token=new_token)["workshop"] == new["id"]
call("/logout", 204, "POST", token=new_token)
call("/me", 401, token=new_token)
print("PASS Phase 02: public catalog, roles, ownership, provisioning, approval and token revocation")
