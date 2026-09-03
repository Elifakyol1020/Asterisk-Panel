# Frontend

Vue/Vite uygulaması `asterisk-frontend/` klasöründedir. Dockerfile, nginx.conf
ve .dockerignore üst klasörde kalır; Compose build context'i `./frontend`dir.

Proje kökünde tüm servisleri başlatmak için:

```powershell
docker compose config --quiet
docker compose up -d --build
```

Frontend http://localhost:3000 adresinde açılır. Port, kökteki `.env` dosyasında
`FRONTEND_PORT` ile değiştirilebilir. Docker build'i npm ve Node 22 kullanır;
`npm run build` ile üretilen `dist/` çıktısını Nginx sunar.

Docker build'inde API temel adresi `/api` olarak ayarlanır. Nginx bu istekleri
`backend:8080` servisine iletir. Yerel `.env` ve iç içe `.git` klasörleri Docker
build'ine dahil edilmez.

Yerel geliştirme için `asterisk-frontend/` klasöründe:

```powershell
npm ci
npm test
npm run dev
```

Vite http://localhost:5173 adresinde çalışır ve `/api` isteklerini localhost:8080'e
yönlendirir. Yerel frontend `.env` dosyası varsa `VITE_API_BASE_URL=/api` kullan.
Backend host portunu değiştirdiysen Vite proxy hedefini de güncelle.

Docker kurulumu canlı yenileme yapmaz; kod değişince proje kökünde
`docker compose up -d --build frontend` çalıştır. İlk başlangıçta backend henüz
hazır değilse kısa süreli 502 yanıtı alınabilir.

`asterisk-frontend/` kendi `.git` klasörüyle eklenmiştir. Ana depoda sıradan bir
klasör olarak takip edilmesi isteniyorsa bu Git düzeni ayrıca birleştirilmelidir;
aksi halde ana depoya eklerken iç içe depo/gitlink oluşabilir. Docker build'i
bu durumdan etkilenmez.
