import { test } from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import vm from 'node:vm'
import { createRequire } from 'node:module'
import ts from 'typescript'
import * as vue from 'vue'
const require = createRequire(import.meta.url)
const root = path.resolve(import.meta.dirname, '..')
function load(file, mocks = {}, globals = {}) {
  const filename = path.resolve(root, file)
  const output = ts.transpileModule(fs.readFileSync(filename, 'utf8'), { compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 } }).outputText
  const module = { exports: {} }
  vm.runInNewContext(output, { module, exports: module.exports, require: id => {
    if (id in mocks) return mocks[id]
    if (id.startsWith('@/') || id.startsWith('.')) {
      const target = id.startsWith('@/') ? path.join(root, 'src', id.slice(2)) : path.resolve(path.dirname(filename), id)
      return load(path.relative(root, target + '.ts'), mocks, globals)
    }
    return require(id)
  }, TextDecoder, TextEncoder, Uint8Array, atob, Date, console, ...globals }, { filename })
  return module.exports
}
const { readClaims } = load('src/utils/session.ts')
const { resources, pbxKeys } = load('src/config/resources.ts')
const { buildPayload, normalizeTenantCode, validatePayload } = load('src/utils/resourceForm.ts')
const token = data => `header.${Buffer.from(JSON.stringify(data)).toString('base64url')}.signature`
const valid = { sub: '12', userId: 12, role: 'SUPER_ADMIN', exp: Math.floor(Date.now() / 1000) + 3600 }
test('JWT: both backend roles map to the correct identity', () => {
  assert.equal(readClaims(token(valid), 'admin@example.com').role, 'SUPER_ADMIN')
  const tenant = readClaims(token({ ...valid, role: 'TENANT_ADMIN', tenantId: 42 }))
  assert.equal(tenant.tenantId, '42')
  assert.equal(tenant.id, '12')
})
test('JWT: malformed, expired, unsupported and unscoped sessions are rejected', () => {
  for (const value of ['', 'broken', 'a.%%%.c', token({ ...valid, exp: 1 }), token({ ...valid, exp: '99999999999' }), token({ ...valid, role: 'CUSTOMER' }), token({ ...valid, role: 'TENANT_ADMIN' }), token({ ...valid, sub: null })]) assert.equal(readClaims(value), null)
})
test('Payload: backend fields only; no ID, context or response metadata', () => {
  const form = { displayName: '  Desk  ', extension: '1001', transport: 'transport-udp', codecs: 'alaw', enabled: true, password: '  long-password  ', id: 77, context: 'evil', tenantId: 99 }
  const result = buildPayload(resources.endpoints.fields, form, false, 'endpoints', true, 42)
  assert.equal(result.displayName, 'Desk'); assert.equal(result.password, '  long-password  ')
  assert.equal(result.tenantId, 42); assert.equal(result.id, undefined); assert.equal(result.context, undefined)
  const tenant = buildPayload(resources.endpoints.fields, form, false, 'endpoints', false, 99)
  assert.equal('tenantId' in tenant, false)
})
test('Tenant code accepts friendly input and normalizes before save', () => {
  assert.equal(normalizeTenantCode('Net GSM A.Ş.'), 'net_gsm_a_s')
  const data = buildPayload(resources.tenants.fields, { name: 'Net GSM A.Ş.', code: ' Net GSM A.Ş. ', status: 'ACTIVE' }, false, 'tenants', true)
  assert.equal(data.code, 'net_gsm_a_s')
})
test('Update: empty password is omitted, not sent as an empty string', () => {
  assert.equal('password' in buildPayload(resources.users.fields, { password: '' }, true, 'users', true, 3), false)
  assert.equal(buildPayload(resources.users.fields, { password: 'new-password-12' }, true, 'users', true, 3).password, 'new-password-12')
})
test('IVR HANGUP has null target; numeric references and timing are numbers', () => {
  assert.equal(buildPayload(resources.options.fields, { actionType: 'HANGUP', targetId: 12 }, false, 'options', true, 1).targetId, null)
  const data = buildPayload(resources.members.fields, { endpointId: '21', penalty: '0', paused: false }, false, 'members', false, 1)
  assert.equal(data.endpointId, 21); assert.equal(data.penalty, 0); assert.equal(data.paused, false)
})
test('Dialplan validates the backend allowlist and argument rules', () => {
  for (const [application, applicationData] of [['Answer',''], ['Hangup',''], ['Playback','welcome_tr'], ['Wait','0'], ['Wait','300']]) assert.equal(Object.keys(validatePayload({ application, applicationData }, 'dialplans')).length, 0)
  for (const [application, applicationData] of [['Answer','1'], ['Hangup','x'], ['Playback','../secret'], ['Wait','301'], ['Wait','-1'], ['Wait','1.2'], ['System','ls']]) assert.ok(validatePayload({ application, applicationData }, 'dialplans').applicationData)
})
test('Password byte limit catches multi-byte Turkish characters', () => {
  assert.equal(validatePayload({ password: 'a'.repeat(72) }, 'users').password, undefined)
  assert.ok(validatePayload({ password: 'ş'.repeat(37) }, 'users').password)
})
test('All PBX resources have required fields; unsupported CUSTOM is excluded', () => {
  assert.equal(pbxKeys.length, 6)
  for (const key of pbxKeys) assert.ok(resources[key].fields.some(f => f.key === 'enabled'))
  assert.equal(resources.extensions.fields.find(f => f.key === 'targetType').options.includes('CUSTOM'), false)
  assert.equal(resources.tenants.api, '/admin/tenants')
  assert.equal(resources.tenants.softDelete, true)
})
test('Router: role boundaries, guest redirect, expiry and exactly two dashboards', () => {
  let guard, routes
  const auth = { isAuthenticated: true, user: { exp: valid.exp }, isSuperAdmin: true, role: 'SUPER_ADMIN', logout() { this.isAuthenticated = false } }
  load('src/router/index.ts', {
    'vue-router': { createWebHistory: () => ({}), createRouter: options => { routes = options.routes; return { beforeEach: cb => { guard = cb }, afterEach() {} } } },
    '@/stores/auth.store': { useAuthStore: () => auth }, '@/config/resources': { resources, pbxKeys },
  })
  assert.equal(guard({ meta: { guest: true } }), '/super-admin/dashboard')
  assert.equal(guard({ meta: { requiresAuth: true, role: 'TENANT_ADMIN' } }), '/super-admin/dashboard')
  auth.role = 'TENANT_ADMIN'; auth.isSuperAdmin = false
  assert.equal(guard({ meta: { requiresAuth: true, role: 'SUPER_ADMIN' } }), '/tenant/dashboard')
  auth.user.exp = 1
  assert.equal(guard({ meta: { requiresAuth: true } }), '/login')
  assert.equal(routes.filter(r => r.children).length, 2)
  const tenantRoutes = routes.find(r => r.path === '/tenant').children
  assert.equal(tenantRoutes.some(r => r.path === 'users' || r.path === 'tenants'), false)
  assert.ok(tenantRoutes.some(r => r.path === 'ivrs/:parentId/options/:id/edit'))
  assert.ok(tenantRoutes.some(r => r.path === 'queues/:parentId/members/:id/edit'))
})
test('API errors distinguish network, auth, validation and service failures', () => {
  const { errorMessage, fieldErrors } = load('src/api/platform.ts', { axios: { isAxiosError: e => Boolean(e?.isAxiosError) }, './axios': {} })
  assert.match(errorMessage({ isAxiosError: true }), /Sunucuya ulaşılamıyor/)
  const unavailable = errorMessage({ isAxiosError: true, response: { status: 503 } })
  assert.match(unavailable, /geçici olarak kullanılamıyor/)
  assert.doesNotMatch(unavailable, /Realtime|kaydedilmedi/)
  assert.match(errorMessage({ isAxiosError: true, response: { status: 400, data: { error: 'VALIDATION_ERROR' } } }), /hatalı alanları/)
  assert.match(errorMessage({ isAxiosError: true, response: { status: 400, data: { message: 'Tenant is inactive' } } }), /tenant pasif/)
  assert.match(errorMessage({ isAxiosError: true, response: { status: 409, data: { message: 'Record changed; reload and retry' } } }), /başka bir işlem/)
  assert.match(errorMessage({ isAxiosError: true, response: { status: 403 } }), /yetkiniz/)
  assert.equal(fieldErrors({ isAxiosError: true, response: { data: { errors: { code: 'invalid' } } } }).code, 'invalid')
})
test('Login service sends email/password and consumes accessToken response', async () => {
  let request
  const { authService } = load('src/services/auth.service.ts', { '@/api/axios': { post: async (url, body) => { request = { url, body }; return { data: { accessToken: 'test', tokenType: 'Bearer' } } } } })
  const response = await authService.login({ email: 'admin@example.com', password: 'test-password' })
  assert.equal(request.url, '/auth/login'); assert.equal(request.body.email, 'admin@example.com')
  assert.equal('usernameOrEmail' in request.body, false); assert.equal(response.accessToken, 'test')
})

function resourceHarness(key, { role = 'SUPER_ADMIN', form = false, id, parentId, tenantId, get, post, put, remove } = {}) {
  const route = { meta: { resource: key, form }, params: { id, parentId }, query: tenantId ? { tenantId } : {} }
  const calls = [], navigation = []
  const defaultPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 }
  const api = {
    get: async (url, options) => { calls.push(['GET', url, options]); return { data: get ? await get(url, options) : defaultPage } },
    post: async (url, body) => { calls.push(['POST', url, body]); if (post) await post(url, body); return { data: body } },
    put: async (url, body) => { calls.push(['PUT', url, body]); if (put) await put(url, body); return { data: body } },
    delete: async url => { calls.push(['DELETE', url]); if (remove) await remove(url) },
  }
  const mocks = {
    vue: { ...vue, onMounted() {} },
    'vue-router': { useRoute: () => route, useRouter: () => ({ push: async value => navigation.push(value), replace: async () => {} }), onBeforeRouteLeave() {} },
    '@/api/axios': api,
    '@/api/platform': { listPage: async (url, params) => (await api.get(url, { params })).data, errorMessage: error => error.message, fieldErrors: () => ({}) },
    '@/config/resources': { resources, recordLabel: row => row.name || row.id, displayValue: String },
    '@/stores/auth.store': { useAuthStore: () => ({ isSuperAdmin: role === 'SUPER_ADMIN', isTenantAdmin: role === 'TENANT_ADMIN', user: { tenantId: '7' } }) },
    '@/components/common/AppIcon.vue': {}, '@/utils/resourceForm': { buildPayload, validatePayload },
  }
  const globals = { window: { confirm: () => true } }
  const state = form
    ? load('src/composables/useResourceForm.ts', mocks, globals).useResourceForm(resources[key], id ? 'edit' : 'create')
    : load('src/composables/useResourceList.ts', mocks, globals).useResourceList(resources[key])
  return { state, calls, navigation }
}
test('Resource flow: user list uses selected tenant and Spring page metadata', async () => {
  const h = resourceHarness('users', { tenantId: '8', get: url => url === '/admin/tenants' ? { content: [{ id: 8, name: 'Test' }], totalPages: 1 } : { content: [{ id: 3, username: 'test' }], totalElements: 21, totalPages: 3 } })
  await h.state.initialize()
  assert.equal(h.state.total.value, 21); assert.equal(h.state.rows.value.length, 1)
  assert.ok(h.calls.some(call => call[1] === '/admin/tenants/8/users'))
  await h.state.changePage(1)
  assert.equal(h.calls.at(-1)[2].params.page, 1)
})
test('Resource flow: tenant cannot change scope via query and create excludes tenantId', async () => {
  const h = resourceHarness('endpoints', { role: 'TENANT_ADMIN', form: true, tenantId: '999' })
  await h.state.initialize()
  assert.equal(h.state.tenantId.value, '7')
  Object.assign(h.state.form, { displayName: 'Desk', extension: '100', password: 'long-password' })
  await h.state.save()
  const call = h.calls.find(call => call[0] === 'POST')
  assert.equal(call[1], '/endpoints'); assert.equal('tenantId' in call[2], false)
  assert.equal(h.navigation.length, 1)
})
test('Resource flow: failed write keeps the form and does not report success', async () => {
  const h = resourceHarness('trunks', { role: 'TENANT_ADMIN', form: true, post: () => { throw new Error('Database operation failed') } })
  await h.state.initialize(); h.state.form.name = 'Keep me'
  await h.state.save()
  assert.equal(h.state.error.value, 'Database operation failed'); assert.equal(h.state.form.name, 'Keep me')
  assert.equal(h.navigation.length, 0); assert.equal(h.state.saving.value, false)
})
test('Resource flow: edit loads values and uses PUT without blank password', async () => {
  const h = resourceHarness('endpoints', { role: 'TENANT_ADMIN', form: true, id: '4', get: () => ({ id: 4, tenantId: 7, extension: '100', displayName: 'Desk', transport: 'transport-udp', codecs: 'alaw', enabled: true }) })
  await h.state.initialize(); h.state.form.displayName = 'Updated'
  await h.state.save()
  const call = h.calls.find(call => call[0] === 'PUT')
  assert.equal(call[1], '/endpoints/4'); assert.equal(call[2].displayName, 'Updated'); assert.equal('password' in call[2], false)
})
test('Resource flow: nested IVR option update uses parent path and null HANGUP target', async () => {
  const h = resourceHarness('options', { role: 'TENANT_ADMIN', form: true, parentId: '9', id: '2', get: url => url === '/ivrs/9' ? { id: 9, tenantId: 7, name: 'Main' } : { content: [{ id: 2, digit: '0', actionType: 'HANGUP', targetId: null }], totalPages: 1 } })
  await h.state.initialize(); await h.state.save()
  const call = h.calls.find(call => call[0] === 'PUT')
  assert.equal(call[1], '/ivrs/9/options/2'); assert.equal(call[2].targetId, null)
})
test('Resource flow: nested queue member update uses parent path', async () => {
  const h = resourceHarness('members', { role: 'TENANT_ADMIN', form: true, parentId: '6', id: '3', get: url => url === '/queues/6' ? { id: 6, tenantId: 7, name: 'Support' } : { content: [{ id: 3, endpointId: 11, penalty: 0, paused: false }], totalPages: 1 } })
  await h.state.initialize(); h.state.form.paused = true
  await h.state.save()
  const call = h.calls.find(call => call[0] === 'PUT')
  assert.equal(call[1], '/queues/6/members/3'); assert.equal(call[2].endpointId, 11)
  assert.equal(call[2].paused, true)
})
test('Resource flow: tenant deactivate uses DELETE then reloads', async () => {
  const h = resourceHarness('tenants')
  await h.state.initialize(); h.state.deleteTarget.value = { id: 5, name: 'Example' }
  await h.state.remove()
  assert.ok(h.calls.some(call => call[0] === 'DELETE' && call[1] === '/admin/tenants/5'))
  assert.equal(h.state.success.value, 'Kayıt pasifleştirildi.')
})

test('Architecture: every route loads a dedicated page, with distinct dashboard views', () => {
  const { superadminRoutes } = load('src/router/routes/superadmin.ts')
  const { tenantRoutes } = load('src/router/routes/tenant.ts')
  const viewPath = route => route.component?.toString().match(/@\/views\/[^'"]+\.vue/)?.[0]
  assert.equal(viewPath(superadminRoutes.find(route => route.path === 'dashboard')), '@/views/superadmin/DashboardView.vue')
  assert.equal(viewPath(tenantRoutes.find(route => route.path === 'dashboard')), '@/views/tenant/DashboardView.vue')
  for (const routes of [superadminRoutes, tenantRoutes]) {
    const pages = routes.filter(route => route.component).map(viewPath)
    assert.equal(pages.length, new Set(pages).size, 'Different routes must not reuse the same page component')
    for (const page of pages) {
      assert.ok(page, 'Route must use a concrete view import')
      const file = path.join(root, 'src', page.slice(2))
      assert.ok(fs.existsSync(file), file)
      const source = fs.readFileSync(file, 'utf8')
      assert.ok(source.includes('<template>'), page)
      assert.ok(source.split('\n').length > 35, 'Pages must own content, not just wrap a generic page')
      assert.equal(source.includes('ResourceView'), false)
    }
  }
  assert.equal(fs.existsSync(path.join(root, 'src/views/ResourceView.vue')), false)
  assert.equal(fs.existsSync(path.join(root, 'src/views/DashboardView.vue')), false)
})

test('Architecture: resource-specific field components explicitly declare every field', () => {
  const names = { tenants: 'Tenant', users: 'User', endpoints: 'Endpoint', trunks: 'Trunk', queues: 'Queue', ivrs: 'Ivr', extensions: 'Extension', dialplans: 'Dialplan', members: 'QueueMember', options: 'IvrOption' }
  for (const [key, name] of Object.entries(names)) {
    const source = fs.readFileSync(path.join(root, 'src/components', key, name + 'Fields.vue'), 'utf8')
    for (const field of resources[key].fields) assert.ok(source.includes('v-model="form.' + field.key + '"'), key + '.' + field.key)
    assert.equal(source.includes('v-for="field'), false)
  }
})

test('PBX create, update and delete remain wired after removal of the Realtime dependency', async () => {
  const fixtures = {
    endpoints: { displayName: 'Test endpoint', extension: '1001', password: 'test-password-12' },
    trunks: { name: 'Test trunk', host: '192.0.2.10', username: 'testuser', password: 'test-password-12' },
    queues: { name: 'support' },
    ivrs: { name: 'Main menu', audioFile: 'welcome' },
    extensions: { name: 'Reception', extensionNumber: '1002', targetType: 'ENDPOINT', targetId: 11 },
    dialplans: { extension: '1003', application: 'Answer', applicationData: '' },
  }
  for (const [key, fixture] of Object.entries(fixtures)) {
    const get = url => url.endsWith('/42')
      ? { id: 42, tenantId: 7, ...fixture }
      : { content: [{ id: 11, tenantId: 7, displayName: 'Target' }], totalPages: 1, totalElements: 1 }
    const create = resourceHarness(key, { role: 'TENANT_ADMIN', form: true, get })
    await create.state.initialize()
    Object.assign(create.state.form, fixture)
    await create.state.save()
    const post = create.calls.find(call => call[0] === 'POST')
    assert.equal(post?.[1], '/' + key)
    assert.equal(create.navigation[0]?.query.saved, '1')
    assert.equal(create.state.error.value, '')

    const edit = resourceHarness(key, { role: 'TENANT_ADMIN', form: true, id: '42', get })
    await edit.state.initialize()
    edit.state.form.enabled = false
    await edit.state.save()
    const put = edit.calls.find(call => call[0] === 'PUT')
    assert.equal(put?.[1], '/' + key + '/42')
    assert.equal(put[2].enabled, false)
    assert.equal('password' in put[2], false)

    const list = resourceHarness(key, { role: 'TENANT_ADMIN' })
    await list.state.initialize()
    list.state.deleteTarget.value = { id: 42 }
    await list.state.remove()
    assert.ok(list.calls.some(call => call[0] === 'DELETE' && call[1] === '/' + key + '/42'))
    assert.equal(list.state.success.value, 'Kayıt silindi.')
  }
})

test('UI does not claim that configuration writes require Realtime', () => {
  function inspect(directory) {
    for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
      const file = path.join(directory, entry.name)
      if (entry.isDirectory()) inspect(file)
      else if (/\.(vue|ts)$/.test(entry.name)) {
        assert.doesNotMatch(fs.readFileSync(file, 'utf8'), /RealtimeNotice|503 hatası döndürür ve kaydedilmez|yazma işlemleri henüz kullanılamıyor|Realtime entegrasyonu henüz hazır değil/, file)
      }
    }
  }
  inspect(path.join(root, 'src'))
  const notice = fs.readFileSync(path.join(root, 'src/components/common/ConfigurationNotice.vue'), 'utf8')
  assert.match(notice, /veritabanında/)
  assert.doesNotMatch(notice, /canlı santral/)
})

// Opt-in read-only contract verification. No login or mutation requests are made.
test('Live backend OpenAPI matches all frontend request fields and operation paths', {
  skip: !process.env.ASTERISK_OPENAPI_URL,
}, async () => {
  const response = await fetch(process.env.ASTERISK_OPENAPI_URL, { signal: AbortSignal.timeout(10000) })
  assert.equal(response.status, 200)
  const spec = await response.json()
  const schemas = spec.components.schemas
  const names = { tenants: 'Tenant', users: 'User', endpoints: 'Endpoint', trunks: 'Trunk', queues: 'Queue', ivrs: 'Ivr', extensions: 'Extension', dialplans: 'Dialplan', members: 'QueueMember', options: 'IvrOption' }
  const simpleRequests = new Set(['tenants', 'members', 'options'])
  for (const [key, name] of Object.entries(names)) {
    const requestNames = simpleRequests.has(key) ? [name + 'Request'] : ['Create' + name + 'Request', 'Update' + name + 'Request']
    const fields = resources[key].fields.map(field => field.key)
    if (pbxKeys.includes(key)) fields.push('tenantId')
    for (const requestName of requestNames) {
      assert.ok(schemas[requestName], requestName)
      assert.deepEqual([...fields].sort(), Object.keys(schemas[requestName].properties).sort(), requestName)
    }
    const collection = key === 'users' ? '/api/admin/tenants/{tenantId}/users'
      : key === 'members' ? '/api/queues/{queueId}/members'
      : key === 'options' ? '/api/ivrs/{ivrId}/options' : '/api' + resources[key].api
    assert.ok(spec.paths[collection]?.get, collection + ' GET')
    assert.ok(spec.paths[collection]?.post, collection + ' POST')
    const item = key === 'users' ? '/api/admin/users/{id}'
      : key === 'members' ? collection + '/{memberId}' : collection + '/{id}'
    assert.ok(spec.paths[item]?.delete, item + ' DELETE')
    assert.ok(spec.paths[item]?.put, item + ' PUT')
    if (!['members', 'options'].includes(key)) assert.ok(spec.paths[item]?.get, item + ' GET')
  }
  assert.ok(spec.paths['/api/auth/login']?.post)
  assert.deepEqual(Object.keys(schemas.LoginRequest.properties).sort(), ['email', 'password'])
  assert.deepEqual(Object.keys(schemas.LoginResponse.properties).sort(), ['accessToken', 'tokenType'])
})
