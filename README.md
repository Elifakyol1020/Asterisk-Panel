# Asterisk

## Proje dizini

```text
asterisk/
├── backend/             # Spring Boot, Maven, mapper'lar ve backend Dockerfile
├── frontend/            # Frontend kodlarının ekleneceği klasör
├── docker-compose.yml   # Frontend, backend, PostgreSQL ve pgAdmin
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

Bu komut backend, PostgreSQL ve pgAdmin'i başlatır.
Frontend dahil tüm servisler için:

```powershell
docker compose config --quiet
docker compose up -d --build
```

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

## Ayarlar ve giriş

Tüm ayarlar proje kökündeki **.env** dosyasındadır. Git'e eklenmez.
Yeni checkout için .env.example dosyasını .env olarak kopyalayıp boş parola ve secret
alanlarını doldur. JWT_SECRET en az 32 byte rastgele bir değer olmalıdır.

`FRONTEND_PORT` varsayılan olarak 3000'dir. `SERVER_PORT`, Docker kullanırken
backend'in bilgisayardaki portunu belirler; container içinde backend daima 8080
portunda çalışır. Compose, frontend'in localhost adresini mevcut CORS listesine
ekler. Mevcut `.env` dosyası ve veritabanı volume adları korunmuştur.

Swagger'da POST /api/auth/login için APP_BOOTSTRAP_EMAIL ve
APP_BOOTSTRAP_PASSWORD değerlerini kullan. DB_PASSWORD veritabanı bağlantısı içindir.

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
- **backend/Dockerfile:** Java 21 ile build/test ve uygulama image'ı.
- **frontend/Dockerfile:** frontend build'i ve Nginx image'ı.
- **frontend/nginx.conf:** SPA sayfaları ve `/api` isteklerinin backend'e yönlendirilmesi.
- **.env:** DB, JWT, port, CORS ve ilk admin ayarları.
- **backend/src/main/resources/application.yml:** Spring ayarları.
- **backend/src/main/resources/schema.sql:** ilk boş veritabanında tabloları oluşturan SQL.

IntelliJ'de `backend/pom.xml` dosyasını Maven projesi olarak yükle. Backend'i
IDE'den çalıştırırken kökteki `.env` dosyasının bulunabilmesi için Run Configuration
içindeki Working directory değerini `asterisk/` kök dizini yap.
Maven testlerini proje kökünden `backend\mvnw.cmd -f backend/pom.xml test` ile
çalıştırabilirsin (Java 21 veya uyumlu bir JDK gerekir).

schema.sql sadece PostgreSQL'in ilk kurulumu sırasında Compose tarafından yüklenir.
Backend tabloları otomatik değiştirmez; Hibernate validate kullanır.
Veriler Docker volume'unda kalır. Önceki kurulumun verilerini korumak için Compose
mevcut asterisk-local_postgres_data volume adını kullanır; bu bir ortam ayrımı değildir.

## Yararlı komutlar

```powershell
docker compose ps
docker compose logs --tail=100 -f backend
docker compose down
```

down veritabanındaki kayıtları korur. .env değişikliğinden sonra:

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

## Yetkiler

Admin rolünün kod karşılığı SUPER_ADMIN'dir. Rol ve tenant durumu enum'ları enums paketindedir.

| İşlem | SUPER_ADMIN | TENANT_ADMIN |
| --- | --- | --- |
| Tenant listeleme, oluşturma, güncelleme ve pasifleştirme | Tümü | Yok |
| Tenant kullanıcılarını listeleme, oluşturma, güncelleme ve pasifleştirme | Tümü | Yok |
| Endpoint, trunk, queue/üye, IVR/seçenek, extension ve dialplan CRUD | Tüm tenantlar | Yalnızca kendi tenant'ı |

TenantService ve UserService, @PreAuthorize("hasRole('SUPER_ADMIN')") ile korunur.
Santral CRUD servisleri @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')") ile korunur.
Bu sınıf seviyesindeki kurallar servislerin tüm public metotlarına uygulanır.
Tenant izolasyonu ayrıca kayıt sorgularında korunur; TENANT_ADMIN başka tenant'ın
kimliğini göndererek yetki kazanamaz. SUPER_ADMIN santral kaydı oluştururken tenantId belirtir.
Login herkese açıktır; Swagger'ı açmak API işlemlerine yetki vermez.

Santral CRUD işlemleri doğrudan uygulama veritabanındaki platform tablolarına yazılır.
Kayıt, güncelleme ve silme işlemleri Asterisk Realtime entegrasyonuna bağlı değildir.

