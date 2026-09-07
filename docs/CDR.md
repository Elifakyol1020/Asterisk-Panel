# Elasticsearch CDR modülü

## 1. Eklenen dosyalar

Backend kökü: `backend/src/main/java/com/netgsm/asterisk/`.

- `config/CdrIndex.java`: yapılandırılabilir indeks koordinatları.
- `entity/CdrDocument.java`: Elasticsearch mapping; JPA entity değildir.
- `dto/request/CdrInput.java`, `dto/request/CdrSearchRequest.java`: veri kabulü ve arama.
- `dto/response/CdrResponse.java`: panel yanıtı.
- `mapper/CdrMapper.java`: normalizasyon, belge kimliği ve yanıt dönüşümü.
- `repository/CdrElasticsearchRepository.java`: mapping oluşturma, yazma, güvenlik filtreli sorgular.
- `service/TenantResolver.java`, `service/ContextTenantResolver.java`: tenant çözümleme.
- `service/CdrIngestionService.java`, `service/CdrRejectedEvent.java`: kaynaklardan bağımsız kabul hattı ve reddetme olayı.
- `service/CdrService.java`: mevcut kullanıcıyla yetkilendirilmiş okuma.
- `controller/CdrController.java`, `controller/CdrIngestionController.java`: okuma ve admin veri kabul API'leri.
- `backend/src/test/java/com/netgsm/asterisk/CdrTests.java`: filtreler, tenant sınırları, idempotency, reddetme.
- `backend/src/test/java/com/netgsm/asterisk/CdrElasticsearchIntegrationTests.java`: isteğe bağlı gerçek Elasticsearch testi.
- `frontend/asterisk-frontend/src/types/cdr.ts`, `src/services/cdr.service.ts`, `src/views/pbx/cdr/CdrListView.vue`.
- `docs/examples/cdr.json`: curl için örnek veri.

## 2. Değiştirilen dosyalar

- `backend/pom.xml`: Spring Boot tarafından yönetilen Spring Data Elasticsearch starter.
- `backend/src/main/resources/application.yml`: Elasticsearch URI, kimlik bilgileri ve indeks adı.
- `docker-compose.yml`: geliştirme Elasticsearch servisi, kalıcı volume, backend bağlantısı.
- `.env.example`: Elasticsearch ayarları.
- `frontend/asterisk-frontend/src/layouts/AdminLayout.vue`, `src/router/routes/pbx.ts`: iki rolde CDR menüsü ve sayfası.
- `backend/src/test/java/com/netgsm/asterisk/AsteriskApplicationTests.java`, `PlatformApiTests.java`: mevcut testler Elasticsearch gerektirmeden çalışır; gerçek JWT ile CDR giriş/yetki doğrulama testleri eklendi.
- `README.md`: bu kılavuzun bağlantısı.

DTO request/response ayrımı korunur. PostgreSQL şemasına CDR tablosu eklenmez.

## 3. Elasticsearch mimarisi

Asterisk CDR kaynağı → kaynak adaptörü → POST /api/admin/cdr → CdrIngestionService
→ TenantResolver → CdrMapper → CdrElasticsearchRepository → Elasticsearch.

Panel → GET /api/cdr veya /api/cdr/{id} → mevcut JWT → CdrService
→ tenant filtreli Elasticsearch sorgusu → CdrResponse.

Santral endpoint/auth/aor/extension/queue/trunk/IVR konfigürasyonları PostgreSQL'de kalır.
Elasticsearch sadece CDR içindir.

Spring Boot 4.1.1 BOM bağımlılık sürümlerini yönetir. Compose Elasticsearch 9.4.5 kullanır.
Sürüm referansı: [Spring Data Elasticsearch uyumluluk tablosu](https://docs.spring.io/spring-data/elasticsearch/reference/elasticsearch/versions.html).

İndeks varsayılan olarak `asterisk-cdr`; ilk başarılı veri yazma girişiminde mapping ile oluşturulur.
Okuma sırasında indeks henüz yoksa boş liste döner; servis ulaşılamıyorsa 503 döner.
Elasticsearch yokken diğer PBX özellikleri açılışta uzak indeks oluşturmayı beklemez.

Mapping: tenantId long; telefon numaraları, kimlikler, durum, context ve kanal alanları keyword;
zamanlar date; duration/billsec/sequence integer. İndeks tek shard ve geliştirme için sıfır replica ile oluşturulur.
`sortId`, aynı başlangıç zamanındaki kayıtlar için sabit ikinci sıralama alanıdır.
Default sıralama `startTime DESC, sortId ASC`.

`CDR_INDEX` tek indeks veya yazılabilir alias adı olabilir. İleride bu adı bir rollover/write alias
olarak yönetip aylık fiziksel indekslere geçirmek mümkündür. Otomatik aylık indeks/ILM/retention
bu sürümde yoktur; mevcut verinin taşınması veya alias yönetimi kendiliğinden yapılmaz.

## 4. Tenant izolasyonu

- TENANT_ADMIN için tenantId her liste ve detay sorgusunda mevcut authentication context'ten alınır.
  URL'deki başka tenantId yetki kazandırmaz.
- SUPER_ADMIN tüm tenantları listeler veya tenantId ile daraltır.
- Projede NORMAL USER rolü yok; yeni bir rol veya authentication sistemi eklenmedi.
- Detay API'si de tenant filtresiyle sorgular; başka kurumun id'si 404 verir.
- Authentication tenant bilgisi yoksa tenant kullanıcısının isteği reddedilir.
- Yazma API'si yalnızca mevcut SUPER_ADMIN JWT'sine açıktır. Tenant yöneticileri CDR üretemez.
- CdrInput içinde tenantId yoktur. Context `tenant_1_internal` ve kanal
  `PJSIP/tenant1_1003-00000034` gibi proje isimlendirmeleri çözülür.
  Bulunan tenant PostgreSQL'de gerçekten mevcut olmalıdır.
- Context ve kanallar farklı tenant gösterirse kayıt reddedilir. Kaynaklar tenant ataması için tahmin edilmez.
- Paylaşılan dahili numaraları üzerinden belirsiz kurum seçimi yapılmaz. PostgreSQL endpoint/DID fallback'i
  eklemek için TenantResolver değiştirilebilir; mevcut projede DID/CDR kaynağı bulunmadı.
- Tenant çözülemezse 422, güvenli warning log ve CdrRejectedEvent üretilir; Elasticsearch yazılmaz.
  Olay kalıcı dead-letter kuyruğu değildir. Gönderen adaptör başarısız girdiyi saklamalıdır.
- Ses kaydı yolu yalnızca metin olarak gösterilir; sunucuda dosya okuma/indirme endpoint'i yoktur.

## 5. Elasticsearch'e yazılma anı ve kaynak sözleşmesi

Bu sürümün çalışan adaptörü JSON HTTP girişidir: `POST /api/admin/cdr`.
Projede mevcut Master.csv veya CDR okuyucusu olmadığı için varsayımsal dosya yolu/polling eklenmedi.
Asterisk'in kendiliğinden bu URL'ye gönderim yaptığı varsayılmamalıdır.
Asterisk tarafında kullandığınız CDR kaynağını okuyacak süreç, her tamamlanmış kaydı bu API'ye
göndermelidir. Java içindeki CSV/AMI/CEL/JDBC adaptörleri de aynı ingestion servisini çağırabilir.

İşlem sırası: alan/zaman doğrulama → tenant çözümleme → numaralarda trim ve durum normalizasyonu
→ belge kimliği üretme → mapping kontrolü → Elasticsearch save.
HTTP 200, Elasticsearch yazmasının başarılı olduğunu gösterir. Aramada görünmesi indeks refresh'ine
kadar kısa süre gecikebilir.

Kimlik SHA-256(tenantId + uniqueId + sequence) ile sabittir. Aynı girdinin tekrar gönderimi aynı belgeyi
günceller; farklı sequence farklı CDR bacaklarını korur. Kaynak uniqueId'leri tek Asterisk kurulumunda
benzersiz olmalıdır. Aynı tenant için birden çok Asterisk sunucusu varsa adaptör uniqueId'ye kaynak
kimliğini dahil etmelidir. sequence zorunludur; tek kayıtlı kaynaklar 0 gönderebilir.

422/400 girdileri düzeltmeden tekrar denemeyin. 503/ağ kesintisinde gönderen taraf aynı kaydı yeniden
gönderebilir. Bu uygulama kaynağın kalıcı teslim kuyruğunun yerini almaz.

Zamanlar ISO-8601 ve UTC ofsetli olmalıdır. Örneğin `2026-09-07T10:22:14+03:00`.
startDate dahil, endDate hariçtir. Süreler saniyedir.

## 6. Çalıştırma

Proje kökünde, mevcut .env ayarlarıyla:

```powershell
docker compose config --quiet
docker compose up -d --build
docker compose ps
docker compose logs --tail=100 elasticsearch backend
```

Yalnızca Elasticsearch başlatmak için:

```powershell
docker compose up -d elasticsearch
curl.exe "http://localhost:9200/_cluster/health"
```

IDE'den backend çalıştırırken varsayılan `http://localhost:9200`; Compose backend'i
`http://elasticsearch:9200` kullanır. Mevcut .env'ye yeni değerler eklemek zorunlu değildir;
application.yml varsayılanları sağlar.

Panel: http://localhost:3000/super-admin/cdr veya http://localhost:3000/tenant/cdr.

Compose'un güvenlik kapalı Elasticsearch servisi yalnızca geliştirme içindir, host portu 127.0.0.1'e
bağlıdır. Üretimde TLS, Elasticsearch kullanıcı/parolası ve uygun ağ erişimi yapılandırılmalıdır.

`docker compose down` kayıtları korur. Volume'ları silmeyin.

## 7. Curl örnekleri

Aşağıdaki komutlar PowerShell içindir. Mevcut login endpoint'inden alınan JWT'leri kullanın:

```powershell
$adminToken = "SUPER_ADMIN_ACCESS_TOKEN"
$tenantToken = "TENANT_1_ACCESS_TOKEN"

# Önce örnek JSON'daki context ve kanalları gerçekten mevcut tenant kimliğine göre düzenleyin.
curl.exe -X POST "http://localhost:8080/api/admin/cdr" -H "Authorization: Bearer $adminToken" -H "Content-Type: application/json" --data-binary "@docs/examples/cdr.json"

curl.exe "http://localhost:8080/api/cdr?page=0&size=20&src=1003&disposition=ANSWERED" -H "Authorization: Bearer $tenantToken"
curl.exe "http://localhost:8080/api/cdr?src=100&prefix=true&minDuration=10" -H "Authorization: Bearer $tenantToken"
curl.exe "http://localhost:8080/api/cdr?startDate=2026-09-07T00:00:00Z&endDate=2026-09-08T00:00:00Z" -H "Authorization: Bearer $tenantToken"

# Tenant kullanıcısında tenantId=2 dikkate alınmaz; kendi kurumunun kayıtları gelir.
curl.exe "http://localhost:8080/api/cdr?tenantId=2" -H "Authorization: Bearer $tenantToken"

# Süperadmin tüm kayıtları veya seçilen tenant'ı görür.
curl.exe "http://localhost:8080/api/cdr" -H "Authorization: Bearer $adminToken"
curl.exe "http://localhost:8080/api/cdr?tenantId=1" -H "Authorization: Bearer $adminToken"

# POST veya liste yanıtındaki id alanını kullanın (uniqueId ile aynı değildir).
$cdrId = "RESPONSE_ID"
curl.exe "http://localhost:8080/api/cdr/$cdrId" -H "Authorization: Bearer $tenantToken"
```

Desteklenen filtreler: page, size, src, dst, prefix, disposition, startDate, endDate,
minDuration, maxDuration, uniqueId, linkedId, tenantId (süperadmin).
size 1–100; ilk 10.000 sonuç arasında sayfalama. Daha eski kayıtlar için tarih aralığını daraltın.
`NO ANSWER` URL'de `NO%20ANSWER` olarak gönderilir.
uniqueId/linkedId ve numaralar varsayılan olarak tam eşleşir; prefix yalnızca src/dst'yi etkiler.

## Testler

```powershell
backend\mvnw.cmd -f backend/pom.xml test
cd frontend/asterisk-frontend
npm run build
npm test
```

Varsayılan backend testleri canlı Elasticsearch gerektirmez; sorgu üretimi, güvenlik sınırları,
JWT erişimi, doğrulama ve ingestion reddi test edilir.
Gerçek mapping/arama/yazma testi geliştirme Elasticsearch'i açıldıktan sonra ayrıca çalıştırılır:

```powershell
$env:CDR_ES_TEST_URL = "http://localhost:9200"
backend\mvnw.cmd -f backend/pom.xml "-Dtest=CdrElasticsearchIntegrationTests" test
Remove-Item Env:CDR_ES_TEST_URL
```

Test rastgele `cdr-test-...` indeksi oluşturur ve yalnızca o indeksi temizler.
Normal çalışmada bu test ortam değişkeni verilmediği için atlanır.
Bu çalışma ortamında Docker CLI bulunmadığından canlı Elasticsearch testi çalıştırılmamıştır.
Frontend'in mevcut testinde PBX menü sayısı 6 beklenirken mevcut listede 5 öğe vardır;
CDR ayrı menü girdisidir, bu eski beklenti değiştirilmemiştir.

