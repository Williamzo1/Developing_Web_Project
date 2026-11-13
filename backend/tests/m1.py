"""HTTP integration checks. Run against disposable local/demo data (creates a workshop).
Usage from project root: python backend/tests/m1.py. No third-party Python packages.
Runs the existing Phase02 suite first, then checks M1 contracts.
"""
import json
from urllib.error import HTTPError
from urllib.request import Request, urlopen
import phase02 as p

checks = 0

def error(path, status, method='GET', data=None, token=None, fields=(), code=None):
    global checks
    result = p.call(path, status, method, data, token)
    assert set(result) == {'timestamp','status','code','message','path','fieldErrors'}, result
    assert result['status'] == status and result['path'] == '/api' + path.split('?')[0]
    assert result['timestamp'] and result['message'] and isinstance(result['fieldErrors'], dict)
    if code: assert result['code'] == code, result
    for field in fields: assert field in result['fieldErrors'], result
    assert 'VcraftDemo17!' not in json.dumps(result)
    checks += 1
    return result

error('/login',400,'POST',{},fields=('username','password'),code='VALIDATION_FAILED')
error('/login',400,'POST',{'username':'BAD NAME','password':'x'*73},fields=('username','password'))
error('/login',400,'POST',{'username':'buyer','password':'🙂'*19},fields=('password',))
error('/login',401,'POST',{'username':'buyer','password':'wrong-password'},code='UNAUTHENTICATED')
error('/admin/workshops',400,'POST',{'workshopName':' ','username':'BAD','password':'short'},p.admin,fields=('workshopName','username','password'))
error('/admin/workshops',400,'POST',{'workshopName':'Xưởng','username':'valid-name','password':'🙂'*19},p.admin,fields=('password',))
error('/artisan/workshops/'+str(p.a_id),400,'PATCH',{'name':'x'*161},p.a,fields=('name',))
error('/buyer/me',401,code='UNAUTHENTICATED')
error('/buyer/me',401,token='invalid-token',code='UNAUTHENTICATED')
assert p.call('/buyer/me',token=p.buyer)['role'] == 'BUYER'
for token in (p.a,p.b,p.admin): error('/buyer/me',403,token=token,code='FORBIDDEN')
for token in (p.buyer,p.a,p.b,p.admin): assert p.call('/me',token=token)['id']
error('/admin/workshops',403,token=p.buyer,code='FORBIDDEN')
error('/artisan/workshops/'+str(p.b_id),403,'PATCH',{'name':'Denied'},p.a,code='FORBIDDEN')
legacy=p.call('/products')
assert isinstance(legacy,list) and len(legacy)>=3
first=p.call('/products/page?page=0&size=1')
second=p.call('/products/page?page=1&size=1')
assert set(first)=={'items','page','size','totalElements','totalPages','first','last'}
assert first['page']==0 and first['size']==1 and first['totalElements']==len(legacy)
assert first['totalPages']==len(legacy) and first['first'] and not first['last']
assert first['items'][0]['id']<second['items'][0]['id']
assert p.call('/products/page?page=100&size=1')['items']==[]
assert p.call('/products/page')['size']==12
for query,field in (('page=-1','page'),('page=100001','page'),('size=0','size'),('size=51','size'),('size=abc','size')):
    error('/products/page?'+query,400,fields=(field,),code='VALIDATION_FAILED')
error('/products/999999999',404,code='PRODUCT_NOT_FOUND')
error('/products/abc',400,code='INVALID_INPUT')
error('/does-not-exist',404,token=p.buyer,code='NOT_FOUND')
error('/login',405,token=p.buyer,code='METHOD_NOT_ALLOWED')
# Bad JSON and content type: not intercepted as a generic 500.
for raw,content_type,status,code in ((b'{','application/json',400,'INVALID_INPUT'),(b'hello','text/plain',415,'UNSUPPORTED_MEDIA_TYPE')):
    try:
        response=urlopen(Request(p.base+'/api/login',data=raw,headers={'Content-Type':content_type},method='POST'),timeout=15)
    except HTTPError as e: response=e
    with response:
        data=json.loads(response.read()); assert response.status==status and data['code']==code,data
        assert data['status']==status and isinstance(data['fieldErrors'],dict)
checks+=2
print(f'PASS M1: {checks} error-contract checks plus pagination, UTF-8 validation, buyer isolation, legacy API and Phase02 regression')
