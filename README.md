# Asterisk

PostgreSQL tabanlı çağrı geçmişi için [CDR kurulumu, mimari, güvenlik ve curl örnekleri](docs/CDR.md).

Gelen arama kuralları, canlı SIP kayıt durumu, IVR tuşları ve kuyruk üyeleri için [PBX kurulum ve geçiş notları](docs/PBX-CALL-SETUP.md).

## Proje dizini

```text
asterisk/
├── backend/             # Spring Boot, Maven, mapper'lar ve backend Dockerfile
├── frontend/            # Frontend kodlarının ekleneceği klasör
├── docs/                # CDR.md ve curl örnekleri (docs/examples/)
├── docker-compose.yml   # Frontend, backend, PostgreSQL ve pgAdmin
├── docker-compose.asterisk.yml  # IVR ses paylaşımı için isteğe bağlı overlay
├── .env                 # Yerel ayarlar (Git'e eklenmez)
└── .env.example
```

Frontend uygulaması `frontend/asterisk-frontend/` içine eklendi. `frontend/` içindeki Dockerfile ve Nginx
ayarları, npm kullanan ve `dist/` çıktısı üreten Vite tabanlı bir uygulama için
hazırdır. Kodları ekleme adımları [frontend/README.md](frontend/README.md) dosyasındadır.

## Çalıştırma

Yalnızca backend ve veritabanı araçlarını başlatmak için proje kökünde:

```powershell
docker compose up -d --build backend pgadmin
```

Bu komut backend, PostgreSQL ve pgAdmin servislerini başlatır. CDR kayıtları PostgreSQL platform.cdr tablosunda tutulur. Mevcut veritabanı için önce docs/CDR.md içindeki migration adımını uygulayın.

Frontend dahil tüm servisler için:

```powershell
docker compose config --quiet
docker compose up -d --build
```

Bu komut frontend, backend, PostgreSQL ve pgAdmin'in tümünü başlatır.
Ek profil veya environment dosyası seçilmez. Frontend image'ının build edilmesi
için `frontend/asterisk-frontend/package.json`, `frontend/asterisk-frontend/package-lock.json` ve uygulama kaynakları
bulunmalıdır. Frontend API temel adresi `/api` olmalıdır.

- Frontend: http://localhost:3000
- Swagger: http://localhost:8080/swagger-ui/index.html
- API: http://localhost:8080/api
- PostgreSQL: localhost:5433
- Veritabanı: asterisk
- DB kullanıcısı: asterisk_user
- pgAdmin: http://localhost:5050

## Asterisk aynı sunucuda, bu proje Docker'da

CDR akışı **Asterisk → ODBC → PostgreSQL platform.cdr → backend → panel** şeklindedir.
CDR için yalnızca `docker-compose.yml` yeterlidir; CSV okuyucu veya dosya bağlaması yoktur.
Asterisk ODBC kurulumu ve mevcut veritabanı migration adımları [docs/CDR.md](docs/CDR.md) içindedir.

**Yalnızca IVR ses yüklemesi kullanılıyorsa** host ile ses klasörünü paylaşan ek dosyayı kullanın:

```bash
docker compose -f docker-compose.yml -f docker-compose.asterisk.yml up -d --build
```

Host tarafında bir kez (root olarak):

```bash
getent group asterisk | cut -d: -f3               # .env içindeki ASTERISK_GID
install -d -g asterisk -m 2775 /var/lib/asterisk/sounds/custom
```

`2775` (setgid) sayesinde backend'in oluşturduğu `custom/tenantN` dizinleri ve WAV
dosyaları `asterisk` grubuna geçer: backend yazabilir, Asterisk okuyabilir. `sounds`
dizininin kendisi 0755 kalabilir, çünkü backend yalnızca `custom` altına yazar.
Backend container'ının kök dosya sistemi salt okunurdur; ses dosyaları bu bağlama
olmadan hiç saklanamaz (hata backend loglarına `WARN` olarak yazılır).

**Asterisk'in veritabanına erişimi.** PostgreSQL container içinde çalışır ve portu
`127.0.0.1:${DB_PORT:-5433}` üzerinde yayınlanır; aynı sunucudaki Asterisk
`res_config_pgsql`/ODBC ile buraya bağlanır (host `127.0.0.1`, port `5433`,
veritabanı `asterisk`, kullanıcı `asterisk_user`). Ek ağ ayarı gerekmez. Realtime
tabloların (`ps_*`, `queues`, `queue_members`) bu veritabanında bulunması gerekir;
`schema.sql` bunları oluşturmaz (bkz. "Dosyalar").

Backend'den Asterisk'e giden bir AMI/ARI bağlantısı yoktur; provisioning tamamen
realtime tabloları üzerinden çalışır. Bu yüzden container'ın host ağına erişmesine
gerek duyulmaz.

Windows geliştirme makinesinde overlay kullanılmaz; mutlak Linux yolları orada
bağlanamayacağı için yalnızca `docker-compose.yml` ile çalışılır.

## Ayarlar ve giriş

Tüm ayarlar proje kökündeki **.env** dosyasındadır. Git'e eklenmez.
Yeni checkout için .env.example dosyasını .env olarak kopyalayıp boş parola ve secret
alanlarını doldur. JWT_SECRET en az 32 byte rastgele bir değer olmalıdır; ayrıca
`change_this` ile başlayan bir değer de reddedilir.

`FRONTEND_PORT` varsayılan olarak 3000'dir. `SERVER_PORT`, Docker kullanırken
backend'in bilgisayardaki portunu belirler; container içinde backend daima 8080
portunda çalışır. Compose, frontend'in localhost adresini mevcut CORS listesine
ekler. Mevcut `.env` dosyası ve veritabanı volume adları korunmuştur.

Swagger'da POST /api/auth/login için APP_BOOTSTRAP_EMAIL ve
APP_BOOTSTRAP_PASSWORD değerlerini kullan. DB_PASSWORD veritabanı bağlantısı içindir.
APP_BOOTSTRAP_PASSWORD en az 12 karakter olmalı ve UTF-8 baytı olarak 72'yi geçmemelidir;
bu koşul sağlanmazsa backend açılışta hata verip durur.

Swagger'da **Try it out** seçip aşağıdaki gövdede password alanına .env dosyasındaki
APP_BOOTSTRAP_PASSWORD değerini kopyala (değişken adını yazma):

```json
{"email":"admin@local.invalid","password":"APP_BOOTSTRAP_PASSWORD değerini buraya kopyala"}
```

Email alanında .env içindeki APP_BOOTSTRAP_EMAIL değerini kullan. E-postalar tüm tenantlar genelinde benzersizdir; büyük/küçük harf farkı gözetilmeden küçük harfle kaydedilir. Login yanıtı yalnızca accessToken ve tokenType içerir; kullanıcı kimliği, rol ve varsa tenant kimliği JWT içindedir.

Yanıttaki accessToken değerini Swagger **Authorize** kutusuna yapıştır.
Önceden kaydedilmiş token varsa önce **Logout** ile temizle.

GET /api/admin/tenants için page (0'dan başlayan sayfa), size (sayfadaki kayıt sayısı)
ve sort (örneğin name,asc) isteğe bağlıdır. Boş bırakarak Execute seçebilirsin.
sort alanına JSON dizisi veya Swagger'ın string örneğini yazma; doğrudan name,asc yaz.
Swagger'daki Schema sekmesi yanıtın alanlarını açıklayan dokümantasyondur;
veritabanı şeması oluşturmanı veya bir şema adı göndermeni istemez.

APP_BOOTSTRAP_ENABLED=true olduğunda ilk SUPER_ADMIN yalnızca henüz yoksa oluşturulur.
Yeniden başlatma mevcut kullanıcının parolasını değiştirmez.

## pgAdmin ile tabloları görüntüleme

http://localhost:5050 adresinde .env içindeki PGADMIN_DEFAULT_EMAIL ve
PGADMIN_DEFAULT_PASSWORD ile giriş yap. Bu parola backend login parolasından ayrıdır.
Sol menüde **Servers → Asterisk PostgreSQL** bağlantısı hazır gelir; bağlantı parolası
istendiğinde .env içindeki **DB_PASSWORD** değerini gir.
**Databases → asterisk → Schemas → platform → Tables** altında tabloları görebilirsin.
Bir tabloya sağ tıklayıp **View/Edit Data → All Rows** seçerek kayıtları açabilirsin.
pgAdmin ayarları ayrı bir Docker volume'unda saklanır.

## Dosyalar

- **docker-compose.yml:** frontend, backend, PostgreSQL ve pgAdmin servisleri, portlar ve kalıcı veriler.
  Asterisk'e ait host yolları burada bağlanmaz; bu dosya Windows geliştirme makinesinde de çalışır.
- **docker-compose.asterisk.yml:** Asterisk aynı sunucuda host üzerinde çalışırken eklenen overlay; CDR dizinini
  salt okunur, `sounds` dizinini yazılabilir bağlar ve container'ı host `asterisk` grubuna ekler.
- **backend/Dockerfile:** Java 21 ile build/test ve uygulama image'ı.
- **frontend/Dockerfile:** frontend build'i ve Nginx image'ı.
- **frontend/nginx.conf:** SPA sayfaları ve `/api` isteklerinin backend'e yönlendirilmesi.
- **.env:** DB, JWT, port, CORS, ilk admin, Asterisk host yolu/grup ayarları.
- **backend/src/main/resources/application.yml:** Spring ayarları.
- **backend/src/main/resources/schema.sql:** ilk boş veritabanında `platform` şemasını ve
  Asterisk realtime dialplanı için `extensions` tablosunu oluşturan SQL. PJSIP `ps_*` tabloları
  ile `queues`/`queue_members` **bu script tarafından oluşturulmaz**; bunlar kullanılan Asterisk
  sürümünün kendi realtime şemasından sağlanmalıdır (bkz. "Kod düzeni").

IntelliJ'de `backend/pom.xml` dosyasını Maven projesi olarak yükle. Backend'i
IDE'den çalıştırırken kökteki `.env` dosyasının bulunabilmesi için Run Configuration
içindeki Working directory değerini `asterisk/` kök dizini yap.
Maven testlerini proje kökünden `backend\mvnw.cmd -f backend/pom.xml test` ile
çalıştırabilirsin (Java 21 veya uyumlu bir JDK gerekir).

schema.sql sadece PostgreSQL'in ilk kurulumu sırasında Compose tarafından yüklenir.
Backend platform tablolarını otomatik değiştirmez; Hibernate validate kullanır.
Veriler Docker volume'unda kalır. Önceki kurulumun verilerini korumak için Compose
mevcut asterisk-local_postgres_data volume adını kullanır; bu bir ortam ayrımı değildir.

## Yararlı komutlar

```powershell
docker compose ps
docker compose logs --tail=100 -f backend
docker compose down
```

down veritabanı verilerini korur. .env değişikliğinden sonra:

```powershell
docker compose up -d --force-recreate backend pgadmin
```

.env içindeki DB veya admin parolasını değiştirmek mevcut veritabanı/kullanıcının
parolasını kendiliğinden değiştirmez.

Kod değişiklikleri için `docker compose up -d --build`
çalıştır. Bu kurulum derlenmiş frontend'i sunar; canlı yenileme içermez.
`docker compose down -v` veritabanı volume'larını da siler; verileri korumak için
`docker compose down` kullan.

## Kod düzeni

Backend sınıfları backend/src/main/java/com/netgsm/asterisk altında controller,
service, mapper, repository, entity, dto, enums, config, security, exception ve
response paketlerinde katmanlarına göre bulunur.
Akış: Controller → Service → Repository.
Request/entity/response dönüşümleri mapper paketindedir.

Santral (endpoint, trunk, queue/üye, IVR/seçenek, extension, dialplan) servisleri ayrıca
`service/provisioning` altındaki `Asterisk*ProvisioningService` sınıflarını kullanır: bu
sınıflar aynı kaydı Asterisk'in kendi realtime tablolarına (ps_endpoints, ps_auths, ps_aors,
ps_endpoint_id_ips, `extensions`, queues, queue_members) işleyerek çalışan Asterisk sunucusunun
PJSIP/dialplan/kuyruk yapılandırmasını gerçek zamanlı günceller; `AsteriskProvisioningReconciler`
platform ve realtime tabloları arasındaki sapmaları uzlaştırır. Bu tablolara karşılık gelen
JPA entity/repository sınıfları, platform şemasından ayrı olarak `asterisk/realtime` paketinde
(`entity` ve `repository` alt paketleri) bulunur.

## Yetkiler

Admin rolünün kod karşılığı SUPER_ADMIN'dir. Rol ve tenant durumu enum'ları enums paketindedir.

| İşlem | SUPER_ADMIN | TENANT_ADMIN |
| --- | --- | --- |
| Tenant listeleme, oluşturma, güncelleme ve pasifleştirme | Tümü | Yok |
| Tenant kullanıcılarını listeleme, oluşturma, güncelleme ve pasifleştirme | Tümü | Yok |
| Endpoint, trunk, queue/üye, IVR/seçenek, extension ve dialplan CRUD | Tüm tenantlar | Yalnızca kendi tenant'ı |
| CDR görüntüleme (GET /api/cdr) | Tüm tenantlar veya seçilen tenant | Yalnızca kendi tenant'ı |
| CDR veri kabulü (POST /api/admin/cdr) | Evet | Yok |

TenantService ve UserService, @PreAuthorize("hasRole('SUPER_ADMIN')") ile korunur.
Santral CRUD servisleri @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')") ile korunur.
CDR okuma servisi (CdrService) aynı şekilde her iki role açıktır; CDR veri kabul uç noktası
(CdrIngestionController) yalnızca SUPER_ADMIN'e açıktır (ayrıntı için bkz. [docs/CDR.md](docs/CDR.md)).
Asterisk CDR kayıtlarını ODBC ile doğrudan PostgreSQL platform.cdr tablosuna yazar; kurulum için bkz. [docs/CDR.md](docs/CDR.md).
Bu sınıf seviyesindeki kurallar servislerin tüm public metotlarına uygulanır.
Tenant izolasyonu ayrıca kayıt sorgularında korunur; TENANT_ADMIN başka tenant'ın
kimliğini göndererek yetki kazanamaz. SUPER_ADMIN santral kaydı oluştururken tenantId belirtir.
Login herkese açıktır; Swagger'ı açmak API işlemlerine yetki vermez.

Santral CRUD işlemleri uygulama veritabanındaki platform tablolarına yazılır **ve** yukarıdaki
"Kod düzeni" bölümünde açıklanan provisioning katmanı üzerinden Asterisk'in realtime tablolarına
da işlenir; bu nedenle kayıt, güncelleme ve silme işlemleri Asterisk Realtime entegrasyonundan
bağımsız değildir.
