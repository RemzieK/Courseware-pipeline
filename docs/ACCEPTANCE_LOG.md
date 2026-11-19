# ACCEPTANCE_LOG – приемане на работата (UC-08)

PM проверява всяка слята задача спрямо критерия ѝ от `PRODUCT_BACKLOG.md` / `ACCEPTANCE_CRITERIA.md`. Задача е **Done** само ако критерият е изпълнен; иначе се отваря отново с коментар какво липсва.

**Проверка №1 – 2026-11-19**, спрямо `main` (последен merge: PR #74).

| Задача | Роля | Критерий | Какво има в `main` | Решение |
| --- | --- | --- | --- | --- |
| UC-01 визия | PM | прегледана от екипа | `PRODUCT_VISION.md` (#48) | ✅ Done |
| UC-03 истории | PM | всяко изискване има история | `USER_STORIES.md` (#49), 26 истории | ✅ Done |
| UC-04 беклог | PM | задачите са в борда | 47 задачи + `PRODUCT_BACKLOG.md` | ✅ Done |
| UC-05 API контракти | PM | форматът е потвърден или въпросът е записан | `API_CONTRACTS.md` (#50, #53, #67, #70) | ✅ Done |
| UC-06 критерии | PM | всяка история има критерий | `ACCEPTANCE_CRITERIA.md` (#71) | ✅ Done |
| UC-07 SLA | PM | одобрен от DevOps и Security | `SLA.md` (#72) | ✅ Done, ако #72 има одобрение и от двамата |
| UC-09 писмо | PM | готово за демото | `LETTER_TO_DEANS.md` (#73) | ✅ Done (ред 33 се обновява след k6) |
| UC-10 демо | PM | всички P0 минават на репетицията | `DEMO.md` (#74) | ⏳ отворена до репетицията |
| UC-11 STRIDE | Security | `SECURITY.md` съдържа модела на заплахите | активи, входни точки, STRIDE таблица (#54) | ✅ Done |
| UC-13 сигурно качване | Security | `.exe` / прекалено голям файл се отказва | само политика `FILE_SECURITY_POLICY.md` (#57); **няма код** | ❌ не е изпълнен |
| UC-15 проверки в CI | Security | pipeline-ът пада при критична уязвимост | `security-ci.yml`: Gitleaks, Trivy (`exit-code: 1`, CRITICAL), Dependency-Check (`--failOnCVSS 9`) (#59) | ✅ Done с бележки |
| UC-16 rate limiting | Security | прекомерни заявки получават 429 | само политика `NETWORK_SECURITY_POLICY.md` (#60); **в `nginx.conf` няма `limit_req` и security headers** | ❌ не е изпълнен |
| UC-19 хранилище и Docker | DevOps | `docker compose up` стартира системата | защитен `main` ✅, Dockerfile-и ✅, `docker-compose.yml` ✅; **backend няма `pom.xml` и `src` в `main`** (чака PR #51) → `compose up` не може да build-не | ⏳ не още – след #51 |
| UC-21 Nginx + 3 инстанции | DevOps | при спиране на една инстанция заявките продължават | `nginx.conf` с 3 backend-а и `proxy_next_upstream`; `docker-compose.lb-demo.yml` с `whoami` (#52, #55) | 🔍 да се провери с `lb-demo` (вж. долу) |
| UC-35 React структура | SE 2 | макетите са одобрени от PM | маршрути и празни страници (#63); **няма макети** | ❌ PM още не е одобрил макети |

## Коментари за задачите, които не са приети

**UC-13 (Хадел):**
> Политиката е добра, но критерият е „опит за качване на `.exe` или прекалено голям файл се отказва“ – това изисква код. Предлагам да го направиш заедно със Златин в качването (UC-29): проверка на реалния тип на файла и лимит 50 MB, с тест. Също: форматите в политиката (`.doc`, `.ppt`, `.png`, `.jpg`, `.jpeg`) не съвпадат с `API_CONTRACTS.md` (PDF, PPTX, DOCX, PNG, JPG) – моля да ги уеднаквим. Задачата остава отворена дотогава.

**UC-16 (Хадел):**
> Политиката е описана, но в `nginx/nginx.conf` и `frontend/nginx.conf` още няма `limit_req` и security headers (`X-Content-Type-Options`, `X-Frame-Options`, CSP, HSTS). Критерият е „прекомерни заявки получават 429“. Моля да ги добавиш в конфигурацията със съгласието на Ремзие и да покажем 429 с `curl` в цикъл. Също: адресите `/api/auth/login` и `/api/materials/upload` да се сменят по договора (`/api/v1/auth/login`, `/api/v1/lessons/{id}/materials`). Задачата остава отворена.

**UC-19 (Ремзие):**
> Хранилището, защитата на `main` и Dockerfile-ите са готови. Критерият „`docker compose up` стартира системата“ още не може да мине, защото backend кодът е в PR #51 на Златин. Три неща за проверка, когато #51 се слее:
> 1. `backend2` и `backend3` имат само `image: unicloud-backend:latest` без `build` – Compose може да опита да го изтегли от Docker Hub преди `backend1` да го е build-нал. Предлагам `build: ./backend/unicloud` да е в общия блок `x-backend`, или `pull_policy: build`.
> 2. Health проверката е `/actuator/health` – нужна е зависимостта Actuator (Стилиян, UC-42).
> 3. Frontend-ът вика `/api`, но `frontend/nginx.conf` не препраща `/api` към backend-а (той е на друг порт – 8080). Нужен е `location /api/ { proxy_pass http://nginx:80; }` или общ вход.

**UC-35 (Даниел):**
> Структурата с маршрутите и общите компоненти е добра. Критерият е „макетите са одобрени от PM“ – моля прати ми скици на основните страници (каталог, курс, материал, търсене, качване), за да ги одобря. Също: `baseURL` в `api.js` е `/api`, а по договора е `/api/v1`; и `frontend/README.md` е още шаблонът на Vite.

## Бележки към приетите

**UC-15 (Хадел):** приема се. Препоръка: `trivy-action@master` и `Dependency-Check_Action@main` да се закрепят към конкретна версия (напр. `@0.28.0`) – иначе всяка промяна в тях влиза в нашия CI без преглед. Dependency-Check тръгва чак когато има `pom.xml` (след #51).

## Как се проверява UC-21 сега

```bash
docker compose -f docker-compose.lb-demo.yml up -d
for i in $(seq 1 6); do curl -s localhost:8080 | grep Hostname; done   # трябва да се редуват 3 имена
docker compose -f docker-compose.lb-demo.yml stop backend2
for i in $(seq 1 6); do curl -s -o /dev/null -w "%{http_code}\n" localhost:8080; done   # трябва само 200
docker compose -f docker-compose.lb-demo.yml down
```
