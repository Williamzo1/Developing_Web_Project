"""Real HTTP integration tests; use a disposable local/demo PostgreSQL database.
With VCRAFT_TEST_DB_URL (postgresql://user@host:port/database) + pg8000,
also assert committed DB invariants and deterministically demonstrate row locks.
No test-only endpoints or code paths exist in the application.
"""
import json, os, time, threading
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime, timezone, timedelta
from urllib.request import Request, urlopen
from urllib.error import HTTPError
from urllib.parse import urlparse, unquote
from uuid import uuid4

base=os.getenv('VCRAFT_TEST_URL','http://localhost:8080')+'/api'
secret='VcraftDemo17!'
count=0

def call(path,method='GET',body=None,token=None,status=200):
    global count
    headers={'Accept':'application/json'}
    if token: headers['Authorization']='Bearer '+token
    data=None if body is None else json.dumps(body).encode()
    if data is not None: headers['Content-Type']='application/json'
    try: response=urlopen(Request(base+path,data,headers,method=method),timeout=25)
    except HTTPError as e: response=e
    with response:
        raw=response.read(); actual=response.status
    assert actual==status,(path,actual,status,raw)
    result=json.loads(raw) if raw else None
    if status>=400:
        assert set(result)=={'timestamp','status','code','message','path','fieldErrors'},result
        assert result['status']==status
    count+=1
    return result

def login(name):return call('/login','POST',{'username':name,'password':secret})['token']
buyer,other,admin,a,b=[login(x) for x in ('buyer','buyer-other','admin','xuong-men-lam','xuong-dat-nung')]
wa=call('/me',token=a)['workshop']; wb=call('/me',token=b)['workshop']
call(f'/artisan/workshops/{wa}/contact','PATCH',{'email':'atelier-a@example.test','phone':'0900000002'},a)
call(f'/artisan/workshops/{wb}/contact','PATCH',{'email':'attacker@example.test'},a,404)
call(f'/artisan/workshops/{wa}/contact','PATCH',{'email':'bad-email'},a,400)
tags={x['slug']:x['id'] for x in call('/quote-tags')}
category=call('/quote-categories')[0]['id']
req='/buyer/quote-requests'
art='/artisan/quote-requests'

def payload(**changes):
    d={'mode':'OPEN','categoryId':category,'title':'Mua sỉ cốc gốm '+uuid4().hex[:8],
       'quantity':100,'unit':'chiếc','spec':'Men thủ công, kiểm tra mẫu trước khi sản xuất',
       'desiredBy':'2099-12-31','tagIds':[tags['gom-thu-cong']],
       'buyerContactName':'Buyer demo','buyerContactEmail':'buyer@example.test','buyerContactPhone':'0900000001'}
    d.update(changes);return d

def new_sent(**changes):
    r=call(req,'POST',payload(**changes),buyer,201)
    return call(f'{req}/{r["id"]}/send','POST',{'version':r['version']},buyer)

def quote(r,token,price=10000):
    return call(f'{art}/{r["id"]}/quotes','POST',{'unitPrice':price,'leadTimeDays':10,'note':'Báo giá riêng'},token,201)

def accept(r,q,expected=200):
    return call(f'{req}/{r["id"]}/quotes/{q["id"]}/accept','POST',{'version':q['version']},buyer,expected)

def race(jobs):
    barrier=threading.Barrier(len(jobs))
    def run(f):barrier.wait();return f()
    with ThreadPoolExecutor(max_workers=len(jobs)) as ex:return list(ex.map(run,jobs))

def raw_accept(r,q):
    # Expected result is determined by the concurrent winner, never retry automatically.
    path=f'{req}/{r["id"]}/quotes/{q["id"]}/accept'
    headers={'Authorization':'Bearer '+buyer,'Content-Type':'application/json'}
    try:resp=urlopen(Request(base+path,json.dumps({'version':q['version']}).encode(),headers,method='POST'),timeout=25)
    except HTTPError as e:resp=e
    with resp:return resp.status,json.loads(resp.read())

# Validation, draft ownership, optimistic revision and deletion.
r=call(req,'POST',{'mode':'OPEN'},buyer,201); rid=r['id']
call(f'{req}/{rid}/send','POST',{'version':0},buyer,400)
call(f'{req}/{rid}',token=other,status=404)
call(f'{art}/{rid}',token=a,status=404)
call(req,token=admin,status=403);call(req,status=401)
call(req,'POST',payload(quantity=0),buyer,400)
call(req,'POST',payload(tagIds=[99999999]),buyer,400)
u=call(f'{req}/{rid}','PUT',payload(version=r['version']),buyer)
assert u['version']>r['version']
assert call(f'{req}/{rid}','PUT',payload(version=r['version']),buyer,409)['code']=='STALE_DATA'
call(f'{req}/{rid}','DELETE',{'version':u['version']},buyer,204)
call(f'{req}/{rid}',token=buyer,status=404)

# Targeted and tag eligibility: unrelated artisan cannot see/bid.
r=new_sent(mode='TARGETED',targetWorkshopId=wa,tagIds=[])
call(f'{art}/{r["id"]}',token=b,status=404)
call(f'{art}/{r["id"]}/quotes','POST',{'unitPrice':10000,'leadTimeDays':5},b,404)
call(f'{req}/{r["id"]}/cancel','POST',{'version':r['version']},other,404)
call(f'{req}/{r["id"]}/cancel','POST',{'version':r['version']},buyer)
r=new_sent(tagIds=[tags['men-lam']])
call(f'{art}/{r["id"]}',token=a)
call(f'{art}/{r["id"]}',token=b,status=404)
assert not any(x['id']==r['id'] for x in call(art+'?size=50',token=b)['items'])

# Main OPEN flow, prices private, revision check and full six states.
r=new_sent();rid=r['id']
assert call(f'{art}/{rid}',token=a)['buyerContact'] is None
call(f'{req}/{rid}/send','POST',{'version':r['version']},buyer,409)
qa=quote(r,a);qb=quote(r,b,12000)
assert qa['totalPrice']==1000000
call(f'{art}/{rid}/quotes','POST',{'unitPrice':1,'leadTimeDays':1},a,409)
call('/artisan/quotes/'+str(qa['id']),token=b,status=404)
assert [x['id'] for x in call(f'{art}/{rid}/quotes',token=b)]==[qb['id']]
view=call(f'{art}/{rid}',token=b)
assert len(view['quotes'])==1 and view['quotes'][0]['id']==qb['id'] and view['buyerContact']['email']=='buyer@example.test'
assert len(call(f'{req}/{rid}/quotes',token=buyer))==2
updated=call('/artisan/quotes/'+str(qa['id']),'PUT',{'version':qa['version'],'unitPrice':11000,'leadTimeDays':12},a)
assert accept(r,qa,409)['code']=='STALE_DATA'
state=accept(r,updated);assert state['status']=='ACCEPTED'
assert sorted(x['status'] for x in state['quotes'])==['ACCEPTED','NOT_SELECTED']
loser=call(f'{art}/{rid}',token=b)
assert loser['acceptedQuoteId'] is None and len(loser['quotes'])==1 and loser['quotes'][0]['status']=='NOT_SELECTED'
accept(r,updated,409)
call('/artisan/quotes/'+str(qb['id']),'PUT',{'version':qb['version'],'unitPrice':1,'leadTimeDays':1},b,409)
call(f'{req}/{rid}/cancel','POST',{'version':state['version']},buyer,409)
call(f'{art}/{rid}/start-production','POST',{'version':state['version']},b,404)
call(f'{art}/{rid}/deliver','POST',{'version':state['version']},a,409)
call(f'{art}/{rid}/start-production','POST',{'version':state['version']},buyer,403)
prod=call(f'{art}/{rid}/start-production','POST',{'version':state['version']},a)
assert prod['status']=='IN_PRODUCTION'
done=call(f'{art}/{rid}/deliver','POST',{'version':prod['version']},a)
assert done['status']=='DELIVERED'
call(f'{art}/{rid}/deliver','POST',{'version':done['version']},a,409)
history=call(f'{req}/{rid}/events',token=buyer)
for s in ('DRAFT','SENT','BIDDING','ACCEPTED','IN_PRODUCTION','DELIVERED'):assert s in [x['toStatus'] for x in history]
assert all(x['actorAccountId'] is not None and x['createdAt'] for x in history)
b_id=call('/me',token=b)['id']
assert all(e['actorAccountId'] in (None,b_id) for e in call(f'{art}/{rid}/events',token=b))
call(f'{req}/{rid}/events',token=other,status=404)

# Withdraw/resubmit one row; cancellation; no bid can enter closed request.
r=new_sent();q=quote(r,a)
w=call('/artisan/quotes/'+str(q['id'])+'/withdraw','POST',{'version':q['version']},a)
assert call(f'{req}/{r["id"]}',token=buyer)['status']=='SENT'
q2=call(f'{art}/{r["id"]}/quotes','POST',{'version':w['version'],'unitPrice':500,'leadTimeDays':2},a,201)
assert q2['id']==q['id']
current=call(f'{req}/{r["id"]}',token=buyer)
cancelled=call(f'{req}/{r["id"]}/cancel','POST',{'version':current['version']},buyer)
assert cancelled['status']=='CANCELLED' and cancelled['quotes'][0]['status']=='NOT_SELECTED'
call(f'{art}/{r["id"]}/quotes','POST',{'unitPrice':1,'leadTimeDays':1},b,409)

# Deadline is optional. After it passes: reject NEW bid, permit editing existing and acceptance.
deadline=(datetime.now(timezone.utc)+timedelta(seconds=2)).isoformat()
r=new_sent(biddingDeadline=deadline);q=quote(r,a)
time.sleep(2.1)
assert call(f'{art}/{r["id"]}/quotes','POST',{'unitPrice':1,'leadTimeDays':1},b,409)['code']=='BIDDING_CLOSED'
q=call('/artisan/quotes/'+str(q['id']),'PUT',{'version':q['version'],'unitPrice':9000,'leadTimeDays':3},a)
assert accept(r,q)['status']=='ACCEPTED'

# Accept different quotes concurrently, with an actual server/connection per request.
for iteration in range(10):
    r=new_sent();qs=[quote(r,a),quote(r,b,13000)]
    results=race([lambda q=q:raw_accept(r,q) for q in qs])
    assert sorted(s for s,_ in results)==[200,409],results
    assert next(v for s,v in results if s==409)['code']=='INVALID_STATE_TRANSITION'
    final=call(f'{req}/{r["id"]}',token=buyer)
    assert sum(q['status']=='ACCEPTED' for q in final['quotes'])==1
    assert sum(q['status']=='NOT_SELECTED' for q in final['quotes'])==1
print('PASS concurrent acceptance: 10 rounds, exactly one 200 and one 409 each')

# Other conflicting writes must obey the same parent-row lock and versions.
def raw_action(path,method,body,token):
    try:
        resp=urlopen(Request(base+path,json.dumps(body).encode(),{'Authorization':'Bearer '+token,'Content-Type':'application/json'},method=method),timeout=25)
    except HTTPError as e:resp=e
    with resp:return resp.status,json.loads(resp.read())

r=new_sent();q=quote(r,a)
results=race([lambda:raw_accept(r,q),lambda:raw_action('/artisan/quotes/'+str(q['id']),'PUT',{'version':q['version'],'unitPrice':777,'leadTimeDays':2},a)])
assert sorted(x[0] for x in results)==[200,409],results
final=call(f'{req}/{r["id"]}',token=buyer)
if final['status']=='ACCEPTED':assert final['quotes'][0]['unitPrice']==q['unitPrice']
else:assert final['status']=='BIDDING' and final['quotes'][0]['unitPrice']==777
r=new_sent();q=quote(r,a)
results=race([lambda:raw_accept(r,q),lambda:raw_action('/artisan/quotes/'+str(q['id'])+'/withdraw','POST',{'version':q['version']},a)])
assert sorted(x[0] for x in results)==[200,409],results
final=call(f'{req}/{r["id"]}',token=buyer)
assert (final['status'],final['quotes'][0]['status']) in [('SENT','WITHDRAWN'),('ACCEPTED','ACCEPTED')]
r=new_sent()
results=race([lambda:raw_action(f'{req}/{r["id"]}/cancel','POST',{'version':r['version']},buyer),lambda:raw_action(f'{art}/{r["id"]}/quotes','POST',{'unitPrice':1,'leadTimeDays':1},a)])
assert sorted(x[0] for x in results) in ([200,409],[201,409]),results
final=call(f'{req}/{r["id"]}',token=buyer)
assert (final['status']=='CANCELLED' and not final['quotes']) or (final['status']=='BIDDING' and len(final['quotes'])==1)
r=new_sent()
results=race([lambda:raw_action(f'{art}/{r["id"]}/quotes','POST',{'unitPrice':1,'leadTimeDays':1},a)]*2)
assert sorted(x[0] for x in results)==[201,409],results
assert len(call(f'{req}/{r["id"]}/quotes',token=buyer))==1
r=call(req,'POST',payload(),buyer,201)
results=race([lambda:raw_action(f'{req}/{r["id"]}','PUT',payload(version=r['version']),buyer)]*2)
assert sorted(x[0] for x in results)==[200,409],results
print('PASS additional races: accept/edit, accept/withdraw, cancel/submit, duplicate submit, draft edit')

# Optional DB-backed assertions: distinguish real row locking from mere HTTP timing.
url=os.getenv('VCRAFT_TEST_DB_URL')
if url:
    import pg8000.dbapi
    u=urlparse(url)
    def connect():return pg8000.dbapi.connect(user=unquote(u.username or 'postgres'),password=os.getenv('VCRAFT_TEST_DB_PASSWORD',''),host=u.hostname or 'localhost',port=u.port or 5432,database=u.path.lstrip('/'),timeout=20)
    r=new_sent();qs=[quote(r,a),quote(r,b)]
    lock=connect();cur=lock.cursor();cur.execute('SELECT id FROM quote_requests WHERE id=%s FOR UPDATE',(r['id'],));cur.fetchone()
    barrier=threading.Barrier(3)
    def held_accept(q):barrier.wait();return raw_accept(r,q)
    with ThreadPoolExecutor(max_workers=2) as ex:
        futures=[ex.submit(held_accept,q) for q in qs]
        barrier.wait();time.sleep(.5)
        assert not any(f.done() for f in futures),'Accept request did not wait for the row lock'
        lock.commit();lock.close()
        assert sorted(f.result()[0] for f in futures)==[200,409]
    db=connect();c=db.cursor()
    c.execute("SELECT request_id FROM quotes WHERE status='ACCEPTED' GROUP BY request_id HAVING count(*)>1");assert not c.fetchall()
    c.execute("SELECT r.id FROM quote_requests r LEFT JOIN quotes q ON q.id=r.accepted_quote_id AND q.request_id=r.id WHERE r.accepted_quote_id IS NOT NULL AND (q.id IS NULL OR q.status<>'ACCEPTED')");assert not c.fetchall()
    c.execute("SELECT count(*) FROM quote_request_events WHERE request_id=%s AND to_status='ACCEPTED'",(r['id'],));assert c.fetchone()[0]==1
    c.execute("SELECT count(*) FROM pg_indexes WHERE indexname='quotes_one_accepted_per_request'");assert c.fetchone()[0]==1
    # Both application locks and the partial unique index are required, verified independently.
    try:
        c.execute("UPDATE quotes SET status='ACCEPTED' WHERE request_id=%s AND status='NOT_SELECTED'",(r['id'],))
        raise AssertionError('Partial unique index failed to reject a second ACCEPTED quote')
    except pg8000.dbapi.DatabaseError as e:
        assert e.args[0]['C']=='23505' and e.args[0].get('n')=='quotes_one_accepted_per_request',e
        db.rollback()
    db.close()
    print('PASS PostgreSQL: two HTTP requests blocked by held row lock; commit invariants and partial unique index verified')
else:
    print('SKIP direct DB/held-lock checks: set VCRAFT_TEST_DB_URL and install quote_requirements.txt')
print(f'PASS quote workflow: {count} HTTP checks + privacy, state history, deadlines and concurrency')
