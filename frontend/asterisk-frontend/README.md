# Netgsm · Asterisk Frontend

Vue 3, TypeScript, Pinia ve Vue Router ile Türkçe santral yönetim arayüzü.
Backend sözleşmesi `C:\Users\elif.akyol\IdeaProjects\asterisk` kaynak kodundan incelenmiştir.
Backend değiştirilmemiştir.

## Çalıştırma

```sh
npm install
npm run dev
```

Arayüzü **http://localhost:5173** üzerinden açın. Vite `/api` isteklerini
`http://localhost:8080` backend adresine yönlendirir. Backend farklı bir portta
çalışıyorsa vite.config.ts içindeki proxy hedefini güncelleyin.

`.env` dosyası (`.env.example` örneği):

```dotenv
VITE_API_BASE_URL=/api
```

Backend için kendi README'sindeki Docker Compose adımlarını uygulayın.
Girişte backend'de tanımlı e-posta ve şifreyi kullanın. Demo hesap, sabit şifre
veya gerçekmiş gibi gösterilen örnek veri bulunmaz. İlk süperadmin backend'in
bootstrap ayarlarıyla oluşturulur.

## Paneller

- **Süperadmin — /super-admin/dashboard:** Platform kaynak sayıları, son tenant'lar,
  tenant oluşturma/düzenleme/pasifleştirme, tenant yöneticilerini yönetme ve tüm
  tenant'ların santral kaynakları.
- **Tenant admin — /tenant/dashboard:** Kendi tenant'ının kaynak sayıları ve santral
  yönetimi. Başka tenant seçimi veya kullanıcı yönetimi gösterilmez.
- Endpoint, trunk, kuyruk, IVR, dahili ve arama planı listeleme/oluşturma/düzenleme/silme.
- Kuyruk üyeleri: listeleme, ekleme ve silme (backend güncelleme API'si sunmuyor).
- IVR tuşlama seçenekleri: listeleme, ekleme, düzenleme ve silme.
- Sunucu taraflı sayfalama ve sıralama; açık sayfada arama.
- Mobil menü, boş/yükleniyor/hata durumları, silme onayı ve kaydedilmemiş değişiklik uyarısı.

## API ve oturum

Login: `POST /api/auth/login`, gövde `{ email, password }`.
Yanıt `{ accessToken, tokenType }`; rol, kullanıcı ID ve tenant ID JWT içinden okunur.
JWT çözümlemesi yalnızca arayüz içindir; imza ve yetkilendirme backend tarafından doğrulanır.

Token yalnızca sekme ömrü boyunca `sessionStorage` içinde tutulur. Şifre saklanmaz.
JWT süresi dolduğunda ve API 401 döndürdüğünde tekrar giriş gerekir. Refresh-token
ve şifre sıfırlama API'si olmadığı için bu işlevler eklenmemiştir.

Liste API'leri Spring Page döndürür (`content, totalElements, totalPages`).
Tenant/kullanıcı DELETE işlemleri pasifleştirmedir; santral DELETE işlemleri silmedir.
Tenant admin için payload'a tenantId eklenmez. Süperadmin santral yazmalarında
seçilen tenantId gönderilir. Güncellemede boş şifre gönderilmez.

## Backend'in mevcut sınırları

Güncel backend'de santral CRUD işlemleri doğrudan `platform` tablolarına yazılır;
Realtime bağımlılığı ve buna bağlı zorunlu 503 engeli kaldırılmıştır. Endpoint,
trunk, kuyruk, kuyruk üyesi, IVR, dahili ve arama planı işlemleri normal API
akışını kullanır. Yetki, aktif tenant, benzersizlik ve hedef referansı kontrolleri
devam eder. Endpoint/trunk şifreleri hash olarak saklanır ve yanıtta bulunmaz.

Veritabanına kayıt yapılması, yapılandırmanın canlı Asterisk'e uygulanmış olduğunu
göstermez. Frontend cihaz bağlantısı veya canlı santral etkinleştirmesi iddiasında
bulunmaz. Olası 503 yanıtları genel servis kullanılamama hatası olarak gösterilir;
eski Realtime açıklaması kullanılmaz. Başarısız istekte form korunur.

Canlı çağrı, SIP kayıt durumu, trafik ve sağlık API'leri bulunmuyor. Dashboard
sayıları yalnızca kayıtlı yapılandırmalardır; canlı istatistik veya sağlık sonucu
olarak sunulmaz. CUSTOM hedefi backend tarafından reddedildiği için formda yoktur.
Ses yükleme API'si yoktur; IVR'de sunucudaki ses dosyasının adı girilir.

## Doğrulama

```sh
npm test
npm run build
```

21 yerel otomatik test: JWT/rol kontrolleri, login sözleşmesi, tenant izolasyonu,
istek gövdeleri, parola UTF-8 byte sınırı, arama planı kuralları, sayfalama,
oluşturma/düzenleme/pasifleştirme, IVR seçenekleri ve hata akışları.
Akış testleri mock API kullanır; gerçek veritabanına yazmaz. Ayrıca her rotanın ayrı
bir sayfa bileşenine gittiği, forma özel alanların açıkça tanımlandığı ve eski
Realtime engeli uyarılarının kalmadığı test edilir.

Çalışan backend ile salt okunur sözleşme kontrolünü de açmak için PowerShell'de:

```powershell
$env:ASTERISK_OPENAPI_URL = 'http://localhost:8080/v3/api-docs'
npm test
```

Bu modda toplam 22 test çalışır. Ek test, Swagger'daki tüm istek alanları ve işlem
yollarını frontend ile karşılaştırır; giriş yapmaz ve kayıt değiştirmez. Son
kontrolde 22 testin tamamı geçti. Değişken verilmezse canlı sözleşme testi atlanır.

Backend'e kimliksiz erişimin 401 döndürdüğü ve localhost:5173 için CORS preflight
isteğinin başarılı olduğu doğrulandı. Gerçek hesapla uçtan uca giriş ve tarayıcı
etkileşim/görsel testleri yapılmadı.

## Kod düzeni

Her ekran ayrı bir Vue sayfasıdır. Listeleme, oluşturma ve düzenleme sayfalarının
kendi şablonları, başlıkları, tablo sütunları ve işlemleri vardır. Tek bir genel
sayfaya yönlendiren sarmalayıcı dosyalar kullanılmaz.

```text
src/
  views/
    auth/LoginView.vue
    superadmin/
      DashboardView.vue
      tenants/
        TenantListView.vue
        TenantCreateView.vue
        TenantEditView.vue
      users/
        UserListView.vue
        UserCreateView.vue
        UserEditView.vue
    tenant/DashboardView.vue
    pbx/
      endpoints/
        EndpointListView.vue
        EndpointCreateView.vue
        EndpointEditView.vue
      trunks/          # TrunkListView, TrunkCreateView, TrunkEditView
      queues/          # QueueListView, QueueCreateView, QueueEditView
        members/       # QueueMemberListView, QueueMemberCreateView
      ivrs/            # IvrListView, IvrCreateView, IvrEditView
        options/       # IvrOptionListView, IvrOptionCreateView, IvrOptionEditView
      extensions/      # ExtensionListView, ExtensionCreateView, ExtensionEditView
      dialplans/       # DialplanListView, DialplanCreateView, DialplanEditView
    NotFoundView.vue
  components/
    endpoints/EndpointFields.vue
    tenants/TenantFields.vue
    ...                # Her kaynak için açıkça tanımlanan form alanları
    common/            # Başlık, durum rozeti, onay diyaloğu, boş durum
    forms/             # FormField, TenantSelect, FormActions
    tables/            # Sayfalama ve liste araç çubuğu
    dashboard/         # İstatistik kartları, kaynak dağılımı, hızlı işlemler
  composables/
    useResourceContext.ts
    useResourceList.ts
    useResourceForm.ts
    useResourceTargets.ts
    useDashboardData.ts
  config/resources/    # Her kaynağın kendi DTO alanları ve doğrulama tanımı
  router/routes/
    superadmin.ts
    tenant.ts
    pbx.ts
  services/            # HTTP işlemleri
  types/               # API modelleri
```

Toplam **33 sayfa dosyası**: 29 kaynak sayfası, 2 dashboard, login ve 404.
Kuyruk üyesi düzenleme sayfası yoktur; backend güncelleme API'si sunmaz.
Süperadmin ve tenant admin aynı santral işlevlerini kullandığı için PBX sayfaları
ortaktır; erişim ve tenant kapsamı değişmez. Dashboard'lar ise iki ayrı dosyadır.

- Sayfa içeriğini değiştirmek için ilgili `views/.../*View.vue` dosyasını düzenleyin.
- Bir form alanı eklemek için ilgili `config/resources/*.ts` tanımını ve
  `components/<kaynak>/*Fields.vue` bileşenini güncelleyin. Create/Edit aynı
  kaynağın form alanlarını paylaşır.
- Listeleme ve form yaşam döngüsü ayrı composable'lardadır; görsel içerik içermezler.
- `services/resource.service.ts` ortak HTTP operasyonlarını kapsüller.
- `router/routes` dosyaları her sayfayı ayrı lazy import ile yükler.
- Eski tek parça `ResourceView.vue` ve ortak `DashboardView.vue` kaldırıldı;
  içerdikleri işlevler yukarıdaki dosyalara taşındı.

## Yayına hazırlık

Bu çalışma yerel backend ile yapılandırıldı; dış servise kod/veri yayınlanmadı.
Docker Compose build'inde VITE_API_BASE_URL=/api kullanılır; Nginx API isteklerini
backend'e, SPA rotalarını index.html'e yönlendirir. Ayrı bir API domain'i
kullanılacaksa VITE_API_BASE_URL ve backend CORS ayarları buna göre güncellenmelidir.
Vite değişkenleri derleme zamanında yerleştirilir; değer değiştiğinde yeniden
build alın. Yerel `.env` dosyanızda eski localhost adresi varsa geliştirmede
proxy kullanmak için `/api` olarak değiştirin; Docker bu dosyayı image'a almaz.
