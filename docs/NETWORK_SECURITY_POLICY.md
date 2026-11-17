# Политика за мрежна защита, Rate Limiting, CORS и Security Headers

Този документ описва правилата и конфигурациите за защита на мрежово ниво, ограничаване на заявките (Rate Limiting), CORS политики и сигурни HTTP хедъри за UniCloud.

---

## 1. Rate Limiting (Ограничаване на заявките)
За да се предотврати злоупотреба с критични ресурси (DDoS, brute-force на login, изтощаване на търсачката или upload), се прилага rate limiting на ниво Nginx / Cloudflare:
* **Login Endpoint (`/api/auth/login`):** Ограничение до максимум 5 заявки в минута от един IP адрес (предпазва от брутално отгатване на пароли).
* **Upload Endpoint (`/api/materials/upload`):** Ограничение до 10 заявки на минута за качване на файлове.
* **Търсене и общи заявки:** Ограничение до 60-100 заявки в минута.
* **Критерий за приемане:** При прекомерни заявки, надвишаващи лимита, сървърът автоматично отговаря със статус **429 Too Many Requests**.

---

## 2. CORS Политика (Cross-Origin Resource Sharing)
* CORS е строго ограничен само до официалните и известни Frontend домейни на UniCloud (напр. нашия публичен уеб домейн).
* **Забрана:** Никога не се използва wildcard (`Access-Control-Allow-Origin: *`) едновременно с позволени credentials (`Access-Control-Allow-Credentials: true`), за да се предотвратят CSRF уязвимости и кражба на сесии.

---

## 3. Сигурни HTTP хедъри (Security Headers)
Nginx и Cloudflare конфигурацията задължително включва следните хедъри за защита на браузъра:
* `X-Content-Type-Options: nosniff` (забранява на браузъра да „погађа“ MIME типот).
* `X-Frame-Options: DENY` или `SAMEORIGIN` (предотвратява Clickjacking атаки).
* `X-XSS-Protection: 1; mode=block` (допълнителна XSS защита).
* `Strict-Transport-Security (HSTS): max-age=31536000; includeSubDomains` (налага използването на HTTPS).
* `Content-Security-Policy (CSP): default-src 'self'` (ограничава откъде могат да се зареждат скриптове и ресурси).