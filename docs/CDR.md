# Asterisk doğrudan PostgreSQL CDR

Akış: **Asterisk → cdr_adaptive_odbc → PostgreSQL platform.cdr → backend → panel**.
Backend kapalı olsa bile Asterisk veritabanına yazabilir. CSV okuyucu kaldırıldı.
CDR için `docker-compose.asterisk.yml` gerekmez; bu dosya yalnızca IVR ses paylaşımı içindir.

## 1. Veritabanı geçişi

Mevcut veritabanında pgAdmin Query Tool ile **`docs/asterisk/install-cdr.sql`** dosyasını çalıştırın.
Bu tek dosya tabloyu ve indeksleri oluşturur, kimlik için UUID varsayılanı ekler ve önceki
trigger varsa kaldırır. PostgreSQL 15+ gerekir. Mevcut `platform.tenants` tablosu korunur.
`tenant_id` sütunu eski kurulumlarla uyumluluk için kalır; backend bu sütuna güvenmez ve
kurumu context/kanaldan sorgu sırasında hesaplar. Trigger veya özel SQL fonksiyonu kullanılmaz.
Yeni volume için Compose schema.sql ve 003 scriptini otomatik uygular; mevcut volume
üzerinde SQL dosyasını elle çalıştırın. Önceki kayıtlar silinmez.

```bash
docker compose up -d --build
```

## 2. Asterisk host üzerinde ODBC bağlantısı

Asterisk sunucusunda unixODBC, PostgreSQL Unicode ODBC sürücüsü ve Asterisk'in
`res_odbc.so`, `cdr_adaptive_odbc.so` modülleri kurulu olmalıdır.
Paket adları işletim sistemine göre değişir. Sürücü adını `odbcinst -q -d` ile doğrulayın.

Aşağıdaki örneklerin bölümlerini mevcut dosyalara ekleyin; mevcut Realtime bağlantılarını
ve diğer ayarları koruyun. Örneklerdeki host, port, veritabanı, kullanıcı ve parolayı
kendi kurulumunuza göre doldurun. `${...}` ifadeleri Asterisk tarafından `.env`'den okunmaz.

| Projedeki örnek | Asterisk host üzerindeki hedef |
| --- | --- |
| `docs/asterisk/odbc.ini.example` | `/etc/odbc.ini` |
| `docs/asterisk/res_odbc.conf.example` | `/etc/asterisk/res_odbc.conf` |
| `docs/asterisk/cdr_adaptive_odbc.conf.example` | `/etc/asterisk/cdr_adaptive_odbc.conf` |
| `docs/asterisk/cdr.conf.example` | `/etc/asterisk/cdr.conf` |

Asterisk aynı Docker host üzerindeyse PostgreSQL adresi `127.0.0.1`, portu `.env` içindeki
`DB_PORT` (varsayılan 5433) olur. Backend'in kullandığı `postgres:5432` Docker içi adrestir.
Asterisk başka bir sunucudaysa loopback bağlantısı çalışmaz; erişilebilir PostgreSQL
adresi ve buna uygun ağ/veritabanı erişim ayarları gerekir.

Örnek, mevcut veritabanı kullanıcısını kullanır. Ayrı ODBC hesabı kullanırsanız schema
USAGE, cdr tablosunda INSERT ve tenant çözümlemesi için tenants tablosunda SELECT gerekir.
`usegmtime=yes` ile DSN'deki `ConnSettings = SET TIME ZONE 'UTC'` birlikte kullanılmalıdır.
Böylece ofsetsiz ODBC zamanları PostgreSQL'e doğru UTC zamanı olarak girer.

`modules.conf` içinde bu modülleri engelleyen `noload` olmamalıdır. Autoload kapalıysa
mevcut `[modules]` bölümüne `preload => res_odbc.so` ve `load => cdr_adaptive_odbc.so` ekleyin.
İlk yüklemede:

```bash
asterisk -rx "module load res_odbc.so"
asterisk -rx "module load cdr_adaptive_odbc.so"
```

Modüller zaten yüklüyse ayar değişikliklerinden sonra:

```bash
asterisk -rx "module reload res_odbc.so"
asterisk -rx "module reload cdr_adaptive_odbc.so"
asterisk -rx "cdr reload"
asterisk -rx "odbc show"
asterisk -rx "cdr show status"
```

`odbc show` bağlantıyı, CDR durumu Adaptive ODBC kaydını göstermelidir. Aynı tabloya
ikinci bir cdr_odbc/cdr_pgsql/adaptive writer tanımlamayın; çift yazma oluşabilir.
Bu modül veritabanı kesintilerine karşı kalıcı teslim kuyruğu sağlamaz; yazma hatalarını
Asterisk loglarından takip edin. PostgreSQL'e geçiş tek başına kayıpsız teslim garantisi değildir.

## 3. Kayıt eşlemesi ve kurum güvenliği

Alias dosyası Asterisk alanlarını backend sütunlarına eşler. `sequence` doğrudan
Asteriskten, `id` PostgreSQL varsayılanından gelir. Backend sorgusu context ve iki kanalı
inceler: `tenant_1_internal`, `PJSIP/tenant1_1003-0001` gibi. Tek bir mevcut kurumla
uyum varsa kurum hesaplanır; belirsiz veya çelişkili kayıtları yalnızca SUPER_ADMIN görür.
Bu filtre sayfalama ve toplam kayıt sayısı hesaplanmadan önce veritabanı sorgusunda
uygulanır. Saklanmış tenant_id değeri yetki kazandırmaz. Epoch yanıt zamanı backendde
boş yanıt zamanına dönüştürülür.

Standart CDR çağrının ses dosyasını içermez. Ses yolu istenirse dialplan tarafından
özel `CDR(recording_path)` alanı doldurulmalıdır; isim alanları da gönderilmezse boş kalır.
Panelin ses oynatma/indirme özelliği yoktur.

## 4. Uçtan uca kontrol

Bir test çağrısını bitirin, ardından pgAdmin'de:

```sql
SELECT id, tenant_id, unique_id, cdr_sequence, src, dst, disposition,
       start_time, answer_time, duration, billsec
FROM platform.cdr ORDER BY start_time DESC LIMIT 20;
```

Panelde `/super-admin/cdr` veya `/tenant/cdr` sayfasını yenileyin.
Kayıt yoksa Asterisk ODBC/CDR loglarını kontrol edin. Panelde kurum belirlenemiyorsa
context/kanal isimlerini ve platform.tenants tablosunu kontrol edin. Tablodaki tenant_id
alanı kurum hesaplamasında kullanılmaz. Backend artık CSV dosyası aramaz.

API aynı kaldı: `GET /api/cdr`, `GET /api/cdr/{id}`. SUPER_ADMIN için isteğe bağlı
`POST /api/admin/cdr` manuel aktarımı korunur; doğrudan ODBC akışında bu API çağrılmaz.
Filtreler ve sayfalama korunur (size 1–100, ilk 10.000 kayıt, başlangıç dahil/bitiş hariç).
Eski Elasticsearch kayıtları otomatik taşınmaz; eski volume'u silmeyin.

## Testler ve kaynaklar

`backend/mvnw.cmd -f backend/pom.xml clean test`

Varsayılan persistence testleri H2 ile backend sorgularını ve atanmamış kayıtların
kurum kullanıcılarından gizlenmesini doğrular. Gerçek PostgreSQL tablo/default testi için
`CDR_PG_TEST_URL` (jdbc:postgresql://...), `CDR_PG_TEST_USER`, `CDR_PG_TEST_PASSWORD`
ortam değişkenlerini tanımlayıp `-Dtest=CdrDirectPostgresTests test` çalıştırın.
Test rastgele bir şema oluşturur, migration ve doğrudan INSERT senaryolarını çalıştırır,
sonunda transaction'ı rollback eder; test hesabının CREATE SCHEMA yetkisi gerekir.

- [Asterisk CDR sürücüleri](https://docs.asterisk.org/Fundamentals/Asterisk-Architecture/Types-of-Asterisk-Modules/Call-Detail-Record-CDR-Drivers/)
- [Res ODBC yapılandırması](https://docs.asterisk.org/Configuration/Interfaces/Back-end-Database-and-Realtime-Connectivity/ODBC/Configuring-res_odbc/)
- [Adaptive ODBC resmi alias örneği](https://github.com/asterisk/asterisk/blob/master/configs/samples/cdr_adaptive_odbc.conf.sample)
- [PostgreSQL ODBC bağlantı seçenekleri](https://odbc.postgresql.org/docs/config-opt.html)
