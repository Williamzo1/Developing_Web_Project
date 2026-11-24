"""M1 steps 1–10 integration checks. Use disposable local/demo PostgreSQL data.
Run: VCRAFT_TEST_URL=http://localhost:8080 python backend/tests/m1_extension.py
Only Python standard library. Creates its own product, batch, workshop and news.
Optional OTP delivery checks: configure backend SMTP to 127.0.0.1:<port>,
disable SMTP auth/TLS, and set VCRAFT_TEST_SMTP_PORT=<port> for this test.
"""
import base64
import json
import os
import queue
import re
import socketserver
import threading
from urllib.error import HTTPError
from urllib.request import Request, urlopen
from uuid import uuid4
from datetime import date

base = os.getenv('VCRAFT_TEST_URL', 'http://localhost:8080')
count = 0
suffix = uuid4().hex[:10]
smtp_messages = queue.Queue()

class SMTP(socketserver.StreamRequestHandler):
    def handle(self):
        self.wfile.write(b'220 local test SMTP\r\n')
        while line := self.rfile.readline():
            command = line.upper()
            if command.startswith((b'EHLO', b'HELO')):
                reply = b'250-localhost\r\n250 OK\r\n'
            elif command.startswith(b'DATA'):
                self.wfile.write(b'354 End with dot\r\n')
                body = b''
                while (part := self.rfile.readline()) not in (b'.\r\n', b''):
                    body += part
                smtp_messages.put(body)
                reply = b'250 OK\r\n'
            elif command.startswith(b'QUIT'):
                self.wfile.write(b'221 Bye\r\n'); break
            else:
                reply = b'250 OK\r\n'
            self.wfile.write(reply)

smtp_server = None
if os.getenv('VCRAFT_TEST_SMTP_PORT'):
    smtp_server = socketserver.ThreadingTCPServer(('127.0.0.1', int(os.environ['VCRAFT_TEST_SMTP_PORT'])), SMTP)
    threading.Thread(target=smtp_server.serve_forever, daemon=True).start()

def call(path, status=200, method='GET', body=None, token=None, raw=None, headers=None):
    global count
    h = dict(headers or {})
    if token: h['Authorization'] = 'Bearer ' + token
    if body is not None:
        raw = json.dumps(body).encode(); h['Content-Type'] = 'application/json'
    try: response = urlopen(Request(base + path, raw, h, method=method), timeout=20)
    except HTTPError as error: response = error
    with response:
        data = response.read()
        assert response.status == status, (path, status, response.status, data)
        content_type = response.headers.get('Content-Type', '')
    count += 1
    if 'json' in content_type:
        result = json.loads(data)
        if status >= 400: assert result['status'] == status and result['message'], result
        return result
    return data

def login(name):
    return call('/api/login', method='POST', body={'username':name, 'password':'VcraftDemo17!'})['token']

a, b, admin, buyer = [login(x) for x in ('xuong-men-lam','xuong-dat-nung','admin','buyer')]
wa = call('/api/me', token=a)['workshop']
wb = call('/api/me', token=b)['workshop']
for path in ('/api/home','/api/workshops','/api/workshops/map','/api/news','/api/batches'):
    call(path)
call('/api/artisan/products',401)
call('/api/artisan/products',403,token=buyer)
call('/api/admin/stats',403,token=a)
call('/api/workshops?page=-1',400)
call('/api/batches?size=0',400)
call('/api/news?size=51',400)
call('/api/products/page?size=abc',400)
# CORS preflight must work before authentication.
call('/api/artisan/products',200,'OPTIONS',headers={'Origin':'http://localhost:5173','Access-Control-Request-Method':'POST','Access-Control-Request-Headers':'Authorization,Content-Type'})

username='buyer-'+suffix
reg={'username':username,'password':'GoodPass123!','displayName':'Người mua thử','email':suffix+'@example.test','phone':'0901234567'}
call('/api/register',400,'POST',body={**reg,'password':'🙂'*19})
call('/api/register',201,'POST',body=reg)
call('/api/register',409,'POST',body=reg)
call('/api/register/verify-email',400,'POST',body={'username':username,'otp':'abcdef'})
if smtp_server:
    import email
    def otp():
        msg=email.message_from_bytes(smtp_messages.get(timeout=8))
        content=msg.get_payload(decode=True).decode(msg.get_content_charset() or 'utf-8')
        return re.search(r'\b[0-9]{6}\b',content)[0]
    first=otp()
    call('/api/register/resend-otp',200,'POST',body={'username':username})
    second=otp()
    if first!=second:
        call('/api/register/verify-email',400,'POST',body={'username':username,'otp':first})
    call('/api/register/verify-email',200,'POST',body={'username':username,'otp':second})
    call('/api/register/verify-email',400,'POST',body={'username':username,'otp':second})
else:
    call('/api/register/resend-otp',200,'POST',body={'username':username})

category=call('/api/quote-categories')[0]['id']
tag_row=call('/api/quote-tags')[0]
tag=tag_row['id']
product={'name':'Bình thử '+suffix,'categoryId':category,'referencePrice':150000,'priceVisible':False,'tagIds':[tag]}
call('/api/artisan/products',400,'POST',body={**product,'referencePrice':0},token=a)
p=call('/api/artisan/products',201,'POST',body=product,token=a)
pid=p['id']; slug=p['slug']
assert p['referencePrice'] is None and p['tags']
assert call('/api/products/'+str(pid))['slug']==slug
call('/api/artisan/products/'+str(pid),404,token=b)
call('/api/artisan/products/'+str(pid),404,'PUT',body=product,token=b)
call('/api/artisan/products/'+str(pid)+'/steps',400,'PUT',body=[{'name':' '}],token=a)
steps=call('/api/artisan/products/'+str(pid)+'/steps',204,'PUT',body=[{'name':'Tạo hình','description':'Tạo hình gốm'}],token=a)
step=call('/api/products/'+slug+'/steps')[0]['id']
# Replacing steps must also work under the unique(product_id,position) constraint.
call('/api/artisan/products/'+str(pid)+'/steps',204,'PUT',body=[{'name':'Nung'}],token=a)
step=call('/api/products/'+slug+'/steps')[0]['id']
filtered=call('/api/products/page?workshopId='+str(wa))
assert any(item['id']==pid for item in filtered['items'])
assert any(x['id']==pid for x in call('/api/products/page?workshopId='+str(wa)+'&tagSlug='+tag_row['slug'])['items'])
call('/api/artisan/workshop/hours',400,'PUT',body=[{'dayOfWeek':1,'openTime':'18:00','closeTime':'08:00'}],token=a)
call('/api/artisan/workshop/hours',204,'PUT',body=[{'dayOfWeek':1,'openTime':'08:00','closeTime':'18:00'}],token=a)
call('/api/artisan/workshop/certificates',201,'POST',body={'name':'Chứng nhận thử'},token=a)
call('/api/artisan/workshops/'+str(wb),404,'PATCH',body={'name':'Sai chủ'},token=a)

batch={'productId':pid,'code':'TEST-'+suffix,'name':'Lô thử','kind':'IN_STOCK','quantity':5,'visibility':'PRIVATE','referencePrice':200000,'priceVisible':False}
call('/api/artisan/batches',400,'POST',body={**batch,'kind':'BAD'},token=a)
batch_result=call('/api/artisan/batches',201,'POST',body=batch,token=a)
bid=batch_result['id']; public_code=batch_result['publicCode']
call('/api/artisan/batches/'+str(bid),404,token=b)
call('/api/batches/'+public_code,404)
call('/api/batches/'+public_code+'/qr.png',404)
assert all(x['id']!=bid for x in call('/api/batches')['items'])
call('/api/artisan/batches/'+str(bid)+'/qr.png',409,token=a)
share=call('/api/artisan/batches/'+str(bid)+'/share-token',method='POST',token=a)['shareUrl']
query=share[share.index('?'):]
landing=call('/api/batches/'+public_code+query)
assert landing['referencePrice'] is None and landing['product']['id']==pid
call('/api/artisan/batches/'+str(bid)+'/milestones',204,'PUT',body=[{'title':'Nung xong','happenedOn':str(date.today()),'processStepId':step}],token=a)
assert len(call('/api/batches/'+public_code+query)['milestones'])==1
png=call('/api/artisan/batches/'+str(bid)+'/qr.png',token=a)
assert png.startswith(b'\x89PNG\r\n\x1a\n')
call('/api/artisan/batches/'+str(bid)+'/share-token',204,'DELETE',token=a)
call('/api/batches/'+public_code+query,404)
call('/api/artisan/batches/'+str(bid),method='PUT',body={**batch,'visibility':'PUBLIC'},token=a)
assert call('/api/batches/'+public_code+'/qr.png').startswith(b'\x89PNG')

# Multi-part upload, ownership on links/deletion, and static image serving.
png=base64.b64decode('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aZ1cAAAAASUVORK5CYII=')
def upload(token,content=png,name='tiny.png',status=201):
    boundary='vcraft'+uuid4().hex
    payload=(f'--{boundary}\r\nContent-Disposition: form-data; name="files"; filename="{name}"\r\nContent-Type: image/png\r\n\r\n').encode()+content+f'\r\n--{boundary}--\r\n'.encode()
    return call('/api/media',status,'POST',raw=payload,token=token,headers={'Content-Type':'multipart/form-data; boundary='+boundary})
media=upload(a)[0]
from urllib.parse import urlsplit
assert call(urlsplit(media['thumbUrl']).path).startswith(b'\x89PNG')
call('/api/media/'+str(media['id']),404,'DELETE',token=b)
call('/api/artisan/batches/'+str(bid),404,'PUT',body={**batch,'mediaIds':[media['id']]},token=b)
call('/api/artisan/products',404,'POST',body={**product,'mediaIds':[media['id']]},token=b)
call('/api/artisan/products/'+str(pid),method='PUT',body={**product,'mediaIds':[media['id']]},token=a)
call('/api/media/'+str(media['id']),409,'DELETE',token=a)
upload(a,b'not an image',status=415)

news={'title':'Tin thử '+suffix,'body':'Thông báo thử nghiệm','kind':'OTHER','publish':True}
n=call('/api/admin/news',201,'POST',body=news,token=admin)
assert call('/api/news/'+n['slug'])['title']==news['title']
call('/api/admin/news/'+str(n['id']),method='PUT',body={**news,'title':'Đã cập nhật'},token=admin)
call('/api/admin/news/'+str(n['id']),204,'DELETE',token=admin)
call('/api/news/'+n['slug'],404)
newtag=call('/api/admin/tags',201,'POST',body={'slug':'test-'+suffix,'name':'Tag thử'},token=admin)
call('/api/admin/tags/'+str(newtag['id']),method='PUT',body={'slug':'test-'+suffix,'name':'Tag đã sửa'},token=admin)
call('/api/admin/tags/'+str(newtag['id']),204,'DELETE',token=admin)
call('/api/admin/featured',204,'PUT',body={'workshopIds':[wa],'productIds':[pid],'batchIds':[bid]},token=admin)
assert any(x['id']==pid for x in call('/api/home')['featuredProducts'])
# A workshop lock revokes existing sessions, blocks login, and hides its public catalogue.
call('/api/admin/workshops/'+str(wa)+'/lock',204,'POST',body={'reason':'Kiểm thử khóa'},token=admin)
call('/api/me',401,token=a)
call('/api/login',401,'POST',body={'username':'xuong-men-lam','password':'VcraftDemo17!'})
call('/api/products/'+slug,404)
call('/api/batches/'+public_code,404)
assert not call('/api/products/page?workshopId='+str(wa))['items']
assert not call('/api/batches?workshopId='+str(wa))['items']
call('/api/admin/workshops/'+str(wa)+'/unlock',204,'POST',token=admin)
login('xuong-men-lam')
print(f'PASS: {count} M1 extension HTTP checks')
if smtp_server: smtp_server.shutdown(); smtp_server.server_close()
