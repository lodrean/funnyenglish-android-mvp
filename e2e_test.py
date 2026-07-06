import urllib.request
import json
import sys

BASE = 'http://localhost:8082'

def post(path, data):
    req = urllib.request.Request(f'{BASE}{path}', data=json.dumps(data).encode('utf-8'), headers={'Content-Type': 'application/json'}, method='POST')
    resp = urllib.request.urlopen(req)
    return resp.status, json.loads(resp.read().decode())

def get(path):
    resp = urllib.request.urlopen(f'{BASE}{path}')
    return resp.status, json.loads(resp.read().decode())

def check(status, cond, name):
    if status == 200 and cond:
        print(f'  PASS: {name}')
        return True
    else:
        print(f'  FAIL: {name} (status={status})')
        return False

passed = 0
failed = 0

# E2E flow
s, u = post('/api/v1/users', {'telegramId': 777001, 'username': 'e2e_user', 'firstName': 'E2E', 'lastName': 'Test'})
if check(s, u['telegramId'] == 777001, 'Create user'): passed += 1
else: failed += 1

s, users = get('/api/v1/users')
if check(s, any(x['telegramId'] == 777001 for x in users), 'Get users'): passed += 1
else: failed += 1

s, g = post('/api/v1/games', {'gameType': 'TIC_TAC_TOE_PVE', 'playerXId': 777001, 'playerOId': None, 'difficulty': 3})
if check(s, g['status'] == 'IN_PROGRESS', 'Create game'): passed += 1
else: failed += 1

s, games = get('/api/v1/games/active')
if check(s, any(x['playerXId'] == 777001 for x in games), 'Get active games'): passed += 1
else: failed += 1

s, m = post('/api/v1/chat/messages', {'userId': 777001, 'userName': 'e2e_user', 'content': 'E2E hello', 'isFromUser': True})
if check(s, m['content'] == 'E2E hello', 'Send chat message'): passed += 1
else: failed += 1

s, msgs = get('/api/v1/chat/messages/recent')
if check(s, any(x['content'] == 'E2E hello' for x in msgs), 'Get recent messages'): passed += 1
else: failed += 1

# Admin login page
resp = urllib.request.urlopen(f'{BASE}/admin/login')
html = resp.read().decode()
if resp.status == 200 and 'Войти' in html:
    print('  PASS: Admin login page')
    passed += 1
else:
    print('  FAIL: Admin login page')
    failed += 1

# Dashboard requires auth
req = urllib.request.Request(f'{BASE}/admin/dashboard')
try:
    resp = urllib.request.urlopen(req)
    html = resp.read().decode()
    if 'Войти' in html or 'Логин' in html or 'login' in html.lower():
        print('  PASS: Dashboard auth redirect')
        passed += 1
    else:
        print('  FAIL: Dashboard auth (unexpected page)')
        failed += 1
except urllib.error.HTTPError as e:
    if e.code in (302, 401, 403):
        print('  PASS: Dashboard auth redirect')
        passed += 1
    else:
        print(f'  FAIL: Dashboard auth (code={e.code})')
        failed += 1

print(f'\nE2E Results: {passed} passed, {failed} failed')
sys.exit(1 if failed > 0 else 0)
