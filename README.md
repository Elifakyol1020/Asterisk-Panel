# Asterisk Backend

## Çalıştırma

Proje kökünde:

```powershell
docker compose up -d --build
```

Bu komut backend ve PostgreSQL'i başlatır. Ek profil veya environment dosyası seçilmez.

- Swagger: http://localhost:8080/swagger-ui/index.html
- API: http://localhost:8080/api
- PostgreSQL: localhost:5433
- Veritabanı: asterisk
- DB kullanıcısı: asterisk_user

## Ayarlar ve giriş

Tüm ayarlar proje kökündeki **.env** dosyasındadır. Git'e eklenmez.
Yeni checkout için .env.example dosyasını .env olarak kopyalayıp boş parola ve secret
alanlarını doldur. JWT_SECRET en az 32 byte rastgele bir değer olmalıdır.

Swagger'da POST /api/auth/login için APP_BOOTSTRAP_USERNAME ve
APP_BOOTSTRAP_PASSWORD değerlerini kullan. DB_PASSWORD veritabanı bağlantısı içindir.

APP_BOOTSTRAP_ENABLED=true olduğunda ilk SUPER_ADMIN yalnızca henüz yoksa oluşturulur.
Yeniden başlatma mevcut kullanıcının parolasını değiştirmez.

## Dosyalar

- **docker-compose.yml:** backend ve PostgreSQL servisleri, portlar ve kalıcı veriler.
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
dto, config, security, exception ve response paketlerinde katmanlarına göre bulunur.
Akış: Controller → Service → Repository.

Asterisk Realtime eşlemeleri henüz tamamlanmadığından bunları gerektiren yazmalar
503 döndürür ve rollback olur. Docker kurulumu yalnızca backend ve PostgreSQL içerir.
