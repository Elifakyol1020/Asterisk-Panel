# Asterisk Backend

## Çalıştırma

Proje kökünde:

```powershell
docker compose up -d --build
```

Bu komut backend, PostgreSQL ve pgAdmin'i başlatır. Ek profil veya environment dosyası seçilmez.

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

- **docker-compose.yml:** backend, PostgreSQL ve pgAdmin servisleri, portlar ve kalıcı veriler.
- **Dockerfile:** Java 21 ile build/test ve uygulama image'ı.
- **.env:** DB, JWT, port, CORS ve ilk admin ayarları.
- **src/main/resources/application.yml:** Spring ayarları.
- **src/main/resources/schema.sql:** ilk boş veritabanında tabloları oluşturan SQL.

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
docker compose up -d --force-recreate
```

.env içindeki DB veya admin parolasını değiştirmek mevcut veritabanı/kullanıcının
parolasını kendiliğinden değiştirmez.

## Kod düzeni

Tüm sınıflar com.netgsm.asterisk altında controller, service, repository, entity,
dto, enums, config, security, exception ve response paketlerinde katmanlarına göre bulunur.
Akış: Controller → Service → Repository.

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

Asterisk Realtime eşlemeleri henüz tamamlanmadığından bunları gerektiren yazmalar
503 döndürür ve rollback olur.

