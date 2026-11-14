"""Run on a disposable vCraft demo database. Includes Phase 02 and M1 regressions."""
from concurrent.futures import ThreadPoolExecutor
from uuid import uuid4
import m1
from phase02 import a, admin, a_id, b, buyer, call, login, password

assert call('/categories')
filtered = call('/products/page?workshop=xuong-men-lam&category=coc-gom&q=men&sort=name-asc')
assert [p['slug'] for p in filtered['items']] == ['coc-men-lam']
assert call('/products/by-slug/coc-men-lam')['workshopSlug'] == 'xuong-men-lam'
call('/products/page?sort=unknown', 400)
assert call('/workshops?q=Men')['items'][0]['slug'] == 'xuong-men-lam'

profile = call('/artisan/workshop', token=a)
other = call('/artisan/workshop', token=b)
call('/artisan/workshop', 401)
call('/artisan/workshop', 403, token=buyer)
call('/artisan/workshop', 400, 'PUT', dict(profile, latitude=21, longitude=None), a)
try:
    saved = call('/artisan/workshop', 200, 'PUT', dict(profile, description='Gốm thử', latitude=21,
                 longitude=105, workshopId=999999), a)
    assert saved['latitude'] == 21 and call('/workshops/' + profile['slug'])['description'] == 'Gốm thử'
    assert any(pin['slug'] == profile['slug'] and pin['latitude'] == 21 for pin in call('/map/workshops'))
    assert call('/artisan/workshop', token=b) == other
finally:
    call('/artisan/workshop', 200, 'PUT', profile, a)

product = {'name': 'Bình thử quy trình', 'category': 'binh-gom', 'price': 15000,
           'priceVisible': False, 'hidden': False}
call('/artisan/products', 401)
call('/artisan/products', 403, token=buyer)
call('/artisan/products', 400, 'POST', dict(product, price=-1), a)
created = call('/artisan/products', 201, 'POST', product, a)
pid, slug = created['id'], created['slug']
own = f'/artisan/products/{pid}'
public = '/products/by-slug/' + slug
assert call(public)['price'] is None
call(own, 404, token=b)
call(own, 404, 'PUT', product, b)
call(own + '/steps', 404, 'PUT', {'steps': []}, b)
assert call(own, 200, 'PUT', dict(product, priceVisible=True), a)['priceVisible']
assert call(public)['price'] == 15000
steps = [{'name': 'Tạo dáng', 'description': 'Tạo dáng bằng bàn xoay'}, {'name': 'Nung'}]
call(own + '/steps', 200, 'PUT', {'steps': steps}, a)
assert [s['name'] for s in call(public + '/steps')] == ['Tạo dáng', 'Nung']
call(own + '/steps', 400, 'PUT', {'steps': [None]}, a)
assert len(call(public + '/steps')) == 2
call('/admin/products/' + str(pid) + '/hide', 403, 'POST', token=a)
call('/admin/products/' + str(pid) + '/hide', 204, 'POST', token=admin)
call(own, 200, 'PUT', dict(product, hidden=False), a)
call(public, 404)
call('/products/' + str(pid), 404)

username = 'spec-' + uuid4().hex[:12]
new = call('/admin/workshops', 201, 'POST', {'username': username, 'password': password, 'workshopName': 'Xưởng thử khóa'}, admin)
suspend = '/admin/workshops/' + str(new['id']) + '/suspend'
approve = '/admin/workshops/' + str(new['id']) + '/approve'
call(approve, 200, 'POST', token=admin)
token = login(username)['token']
assert call('/workshops/' + username)['approved']
call(suspend, 403, 'POST', token=buyer)
call(suspend, 200, 'POST', token=admin)
call('/workshops/' + username, 404)
call('/me', 401, token=token)
call(approve, 200, 'POST', token=admin)
call('/me', 401, token=token)
assert login(username)['token']

buyer_path, artisan_path = '/buyer/quote-requests', '/artisan/quote-requests'
draft = {'title': 'Mua bình thử nghiệm', 'category': 'binh-gom', 'quantity': 100, 'unit': 'chiếc', 'spec': 'Men lam'}
call(buyer_path, 401)
call(buyer_path, 403, token=a)
call(buyer_path, 400, 'POST', dict(draft, quantity=0), buyer)
created = call(buyer_path, 201, 'POST', draft, buyer)['request']
request_id = created['id']
bp = f'{buyer_path}/{request_id}'
ap = f'{artisan_path}/{request_id}'
call(ap, 404, token=a)
call(bp, 409, 'PUT', dict(draft, version=-1), buyer)
call(bp, 200, 'PUT', dict(draft, version=created['version']), buyer)
call(bp + '/send', 400, 'POST', {'workshop': 'unknown'}, buyer)
assert call(bp + '/send', 200, 'POST', {'workshop': 'xuong-men-lam'}, buyer)['request']['status'] == 'SENT'
call(ap, 404, token=b)
offer = {'unitPrice': 10000, 'totalPrice': 1000000, 'leadTimeDays': 5}
call(ap + '/quote', 404, 'PUT', offer, b)
first = call(ap + '/quote', 200, 'PUT', offer, a)['quotes'][0]
second = call(ap + '/quote', 200, 'PUT', dict(offer, leadTimeDays=4), a)['quotes'][0]
accept = bp + '/quotes/' + str(first['id']) + '/accept'
call(accept, 409, 'POST', {'quoteUpdatedAt': first['updatedAt']}, buyer)

def choose(_):
    try:
        call(accept, 200, 'POST', {'quoteUpdatedAt': second['updatedAt']}, buyer)
        return 200
    except AssertionError as error:
        assert error.args[0][1] == 409, error
        return 409

with ThreadPoolExecutor(max_workers=2) as pool:
    assert sorted(pool.map(choose, range(2))) == [200, 409]
call(ap + '/quote', 409, 'PUT', offer, a)
call(ap + '/start-production', 404, 'POST', token=b)
assert call(ap + '/start-production', 200, 'POST', token=a)['request']['status'] == 'IN_PRODUCTION'
assert call(ap + '/deliver', 200, 'POST', token=a)['request']['status'] == 'DELIVERED'
call(ap + '/deliver', 409, 'POST', token=a)
assert [event['toStatus'] for event in call(bp, token=buyer)['events']] == [
    'DRAFT', 'SENT', 'BIDDING', 'ACCEPTED', 'IN_PRODUCTION', 'DELIVERED'
]
cancelled = call(buyer_path, 201, 'POST', draft, buyer)['request']['id']
cp = f'{buyer_path}/{cancelled}'
call(cp + '/send', 200, 'POST', {'workshop': 'xuong-men-lam'}, buyer)
call(f'{artisan_path}/{cancelled}/quote', 200, 'PUT', offer, a)
assert call(cp + '/cancel', 200, 'POST', token=buyer)['quotes'][0]['status'] == 'NOT_SELECTED'

batch_product = call('/artisan/products', 201, 'POST', product, a)['id']
batch_input = {'productId': batch_product, 'code': 'LO-01', 'name': 'Lo thu nghiem', 'kind': 'IN_STOCK',
               'quantity': 10, 'unit': 'chiec', 'price': 14000, 'priceVisible': False, 'visibility': 'PRIVATE'}
call('/artisan/batches', 401)
call('/artisan/batches', 403, token=buyer)
call('/artisan/batches', 404, 'POST', batch_input, b)
call('/artisan/batches', 400, 'POST', dict(batch_input, quantity=-2), a)
batch = call('/artisan/batches', 201, 'POST', batch_input, a)['batch']
private_path, batch_path = '/batches/' + batch['publicCode'], '/artisan/batches/' + str(batch['id'])
call(batch_path, 404, token=b)
call(batch_path + '/share-token', 404, 'POST', token=b)
call(private_path, 404)
call(private_path + '?token=invalid', 404)
token_one = call(batch_path + '/share-token', 200, 'POST', token=a)['token']
assert call(private_path + '?token=' + token_one)['batch']['price'] is None
token_two = call(batch_path + '/share-token', 200, 'POST', token=a)['token']
call(private_path + '?token=' + token_one, 404)
call(batch_path + '/share-token', 204, 'DELETE', token=a)
call(private_path + '?token=' + token_two, 404)
call(batch_path + '/milestones', 200, 'PUT', {'items': [
    {'title': 'Nung', 'happenedOn': '2026-11-13'},
    {'title': 'Tao dang', 'happenedOn': '2026-11-12'},
]}, a)
call(batch_path, 200, 'PUT', dict(batch_input, visibility='PUBLIC', priceVisible=True), a)
assert [m['title'] for m in call(private_path)['milestones']] == ['Tao dang', 'Nung']
assert call(private_path)['batch']['price'] == 14000
listing = call('/batches?workshop=xuong-men-lam&kind=IN_STOCK')
assert any(item['id'] == batch['id'] for item in listing['items'])

call('/admin/stats', 401)
call('/admin/stats', 403, token=buyer)
assert call('/admin/stats', token=admin)['quoteRequests'] >= 2
featured = {'workshopId': a_id, 'productId': batch_product, 'batchId': batch['id']}
call('/admin/featured', 403, 'PUT', featured, a)
call('/admin/featured', 400, 'PUT', dict(featured, batchId=999999), admin)
call('/admin/featured', 200, 'PUT', featured, admin)
assert call('/home')['batch']['id'] == batch['id']
call('/admin/batches/' + str(batch['id']) + '/hide', 204, 'POST', token=admin)
call(private_path, 404)
assert call('/home')['batch'] is None

article = {'title': 'Lich hoi cho', 'body': 'Thong tin test', 'kind': 'EVENT',
           'eventStart': '2026-11-20T04:00:00Z', 'eventPlace': 'Bat Trang', 'hidden': False}
call('/admin/news', 403, 'POST', article, a)
call('/admin/news', 400, 'POST', dict(article, eventPlace=''), admin)
news = call('/admin/news', 201, 'POST', article, admin)
assert call('/news/' + news['slug'])['eventPlace'] == 'Bat Trang'
assert any(item['id'] == news['id'] for item in call('/news?kind=EVENT')['items'])
call('/admin/news/' + str(news['id']), 200, 'PUT', dict(article, hidden=True), admin)
call('/news/' + news['slug'], 404)

token = login(username)['token']
second_token = login(username)['token']
change = {'currentPassword': password, 'newPassword': 'VcraftUpdated17!'}
call('/password', 401, 'PUT', change)
call('/password', 400, 'PUT', dict(change, currentPassword='wrong'), token)
call('/password', 204, 'PUT', change, token)
call('/me', 401, token=token)
call('/me', 401, token=second_token)
login(username, 401)
assert login(username, secret=change['newPassword'])['token']
print('PASS workshop profiles, map pins, catalog, ownership, moderation, targeted quotes, concurrency, batch sharing, news, featured content and password changes')
