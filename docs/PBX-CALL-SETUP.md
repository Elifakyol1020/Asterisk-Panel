# Gelen arama, dahili kaydı, IVR tuşları ve kuyruk üyeleri

## Uygulamadaki akış

- Menüde Arama kuralları, yeni /inbound-routes kaynağını açar. Eski /extensions ve /dialplans iç yönlendirmeleri korunur; eski kayıtlar otomatik olarak gelen aramaya dönüştürülmez.
- Gelen kural: kaynak trunk + operatörün gönderdiği tam DID numarası + dahili/kuyruk/IVR hedefi. Aynı kurum/trunk/numara tekrar eklenemez; farklı trunklarda aynı DID kullanılabilir. Numara deseni veya genel eşleşme bu sürümde yoktur.
- POST /api/ivrs, isteğe bağlı options dizisini kabul eder. Her eleman digit, actionType, targetId içerir. ENDPOINT, QUEUE, IVR, HANGUP desteklenir; eski EXTENSION hedefleri de API'de korunur. HANGUP için targetId null olmalıdır.
- POST /api/queues, isteğe bağlı members dizisini kabul eder. Her eleman endpointId, penalty (0–1000), paused içerir.
- Ana kayıt ve alt kayıtlar aynı veritabanı transaction'ında oluşturulur. Hatalı veya yinelenen tuş/üye durumunda tüm oluşturma işlemi geri alınır. Alt kayıtlar daha sonra mevcut tuşlama/üyeler ekranlarından düzenlenebilir.

## Veritabanı

Yeni platform.inbound_routes tablosu ve IVR ENDPOINT hedefini kabul eden check constraint, backend schema.sql içine eklendi. Backend SQL başlangıç ayarı açık olduğunda uygulanır. Önceden uygulamak için:

backend/src/main/resources/manual-migrations/004_inbound_routes_and_ivr_targets.sql

Bu dosya uygulama şemasının geçişidir; Asterisk'in Alembic migration'larından ayrıdır. Kurulum öncesi veritabanı yedeği alın.

## Asterisk gelen çağrı context'leri

Trunk endpoint'leri artık tenant_<kurumId>_inbound_<trunkId> context'ine yönlenir. Gelen kurallar yalnızca bu context'e yazılır; dahili numaralarının internal context'indeki satırlar değiştirilmez. IVR hedefleri mevcut kurumun internal context'indeki IVR girişine Goto ile gider.

**Dağıtım sırasında yeni context'ler ve DID kuralları hazırlanmalıdır.** Eski trunk çağrılarını internal context'te karşılayan kurallar yeni gelen kural olarak ayrıca tanımlanmalıdır. Context veya DID kuralı eksikse gelen çağrı eşleşmez.

Örnek /etc/asterisk/extensions.conf parçası (kurum 1, trunk 7):

```ini
[tenant_1_inbound_7]
switch => Realtime/@
```

Her trunk için aynı kalıpta context tanımlanır. Mevcut internal/IVR context tanımları korunur. Tüm trunk context parçalarını üretmek için PostgreSQL'de:

```sql
SELECT format('[tenant_%s_inbound_%s]%sswitch => Realtime/@', tenant_id, id, chr(10))
FROM platform.trunks ORDER BY tenant_id, id;
```

Realtime lookup için /etc/asterisk/extconfig.conf içindeki mevcut [settings] bölümünde gerekli eşlemeler bulunmalıdır:

```ini
extensions => odbc,asterisk,extensions
queues => odbc,asterisk,queues
queue_members => odbc,asterisk,queue_members
```

ODBC hesabının varsayılan şeması public olmalıdır. res_config_odbc ve pbx_realtime modülleri yüklenmiş olmalı; modules.conf içindeki çelişen noload satırları kaldırılmalıdır. Güncelleme sonrası dialplan reload ve pjsip show endpoint ile trunk context'i doğrulanır. Realtime endpoint önbelleği kullanılan kurulumlarda önbellek de yenilenmelidir.

## Canlı SIP kayıt durumu (AMI)

Kayıt durumu enabled bayrağından veya platform tablosundan türetilmez. Backend AMI PJSIPShowContacts çıktısını okur; süresi dolmamış dinamik contact varsa REGISTERED döndürür. Statik trunk contact'ları ve süresi dolmuş contact'lar dahil edilmez. Kayıtlı olmak, cihazın o anda OPTIONS'a yanıt verdiği anlamına gelmez.

.env ayarları:

```dotenv
ASTERISK_AMI_HOST=ASTERISKIN_BACKENDDEN_ERISILEBILEN_ADRESI
ASTERISK_AMI_PORT=5038
ASTERISK_AMI_USERNAME=panel-status
ASTERISK_AMI_SECRET=GUCLU_BIR_PAROLA
```

Backend Docker içindeyse 127.0.0.1 container'ın kendisidir; Asterisk host'unun erişilebilir adresi kullanılmalıdır. Compose mevcut env_file üzerinden bu değerleri aktarır.

/etc/asterisk/manager.conf mevcut yapılandırmasıyla birleştirilecek kullanıcı örneği:

```ini
[panel-status]
secret = GUCLU_BIR_PAROLA
read = system,command
write = system
deny = 0.0.0.0/0.0.0.0
permit = BACKEND_KAYNAK_IP/255.255.255.255
```

AMI genel bölümünde enabled=yes olmalı; bindaddr/backend erişimi kurulum ağına göre ayarlanmalıdır. AMI erişimini yalnızca backend kaynak adresine açın. system action yetkisi PJSIPShowContacts için gereklidir; arayüzden keyfi AMI action çalıştırma API'si açılmamıştır.

GET /api/endpoints/registration-status?ids=1,2 yalnızca kullanıcının yetkili olduğu dahililer için durum döndürür; başka kurumun ID'si reddedilir. En fazla 100 ID kabul edilir. Durumlar REGISTERED, UNREGISTERED ve UNKNOWN'dır. Eksik AMI ayarı, bağlantı veya yetki hatası UNKNOWN olur. Liste 5 saniyede bir kontrol edilir; arka plan sekmesinde durur ve sayfadan çıkışta iptal edilir. Backend 4 saniyelik kısa contact önbelleği kullanır.

## Doğrulama

- Frontend: npm run build ve npm test (frontend/asterisk-frontend dizininde).
- Yeni backend testleri: ./mvnw -Ppbx-tests clean test (backend dizininde, JDK 21+). Profil yalnızca yeni service/*Test.java testlerini derler/çalıştırır. Mevcut genel test takımının kaldırılmış Spring test bağımlılıklarıyla uyumsuzluğu bu profilden bağımsızdır.
- Sunucuda gerçek telefon REGISTER/UNREGISTER işlemleriyle Kayıtlı/Kayıtlı değil geçişi kontrol edilir.
- Test DID'ye dış arama yapılarak doğru trunk context'i ve hedef doğrulanır.
- IVR'da 1, *, # tuşları; hatalı tuş ve timeout denenir.
- Kuyruk üyesinin PJSIP arayüzü queue show ile doğrulanır, üye duraklatıldığında yeni çağrı dağıtılmadığı kontrol edilir.

Bu geliştirmede sunucu ayarları otomatik değiştirilmez ve üretim çağrısı testi yapılmaz.
