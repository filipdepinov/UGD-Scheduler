# TapIn — NFC Систем за Евиденција на Присуство

## Опис на проектот

TapIn е дистрибуиран систем за евиденција на присуство во училница базиран на NFC (Near Field Communication) технологија. Системот овозможува студентите да се регистрираат на час со едноставно допирање на нивниот телефон до телефонот на наставникот, без потреба од рачно пополнување на листи.

Системот се состои од 4 компоненти:

- **Student Android App** — Студентската апликација емитира шифриран NFC токен преку HCE (Host Card Emulation)
- **Teacher Android App** — Наставничката апликација го чита NFC токенот и го валидира преку backend-от
- **Node.js Backend** — REST API со JWT автентикација, распореден на Railway
- **React Dashboard** — Веб панел за администратори, наставници и студенти со графикони и статистики

## Автори

| Име и Презиме | Индекс |
|---------------|--------|
| Филип Депинов | 102701 |
| Алек Даниловски | 102729 |

## Технологии

### Android апликации
- Kotlin + MVVM архитектура
- Hilt (Dependency Injection)
- Retrofit (HTTP клиент)
- Room (локална база на податоци)
- Navigation Component
- StateFlow + Coroutines
- HCE (Host Card Emulation) за NFC емитување
- WorkManager за синхронизација во позадина

### Backend
- Node.js 20 + Express.js
- Prisma ORM
- PostgreSQL (Supabase)
- JWT автентикација
- AES-256 шифрирање на NFC токени
- Swagger UI документација

### Dashboard
- React 18 + Vite
- TailwindCSS
- Chart.js (графикони)
- React Router v6
- Axios

### Инфраструктура
- **База на податоци:** Supabase (PostgreSQL) — EU West (Paris)
- **Backend:** Railway — https://tapin-production-4955.up.railway.app
- **Верзионирање:** GitHub — https://github.com/filipdepinov/TapIN

## Безбедност

- Секој NFC токен е шифриран со AES-256 и важи само 5 минути
- Токените се еднократни — повторна употреба е невозможна (replay attack заштита)
- JWT токени со RBAC (Admin / Teacher / Student) улоги
- bcrypt хаширање на лозинки (12 рунди)


### Android апликации
- Отвори ги TapIn-Student и TapIn-Teacher во Android Studio
- Поврзи уред или стартувај емулатор
- Кликни Run ▶

## Тест акредитиви

| Улога | Е-пошта | Лозинка |
|-------|---------|---------|
| Admin | admin@tapin.app | Admin@1234 |
| Наставник | teacher@tapin.app | Teacher@1234 |
| Студент | alice@tapin.app | Student@1234 |

## Користени библиотеки и референци

- [Prisma ORM](https://www.prisma.io/)
- [Supabase](https://supabase.com/)
- [Railway](https://railway.app/)
- [Hilt — Dependency Injection](https://dagger.dev/hilt/)
- [Android NFC / HCE документација](https://developer.android.com/guide/topics/connectivity/nfc/hce)
- [Chart.js](https://www.chartjs.org/)
- [TailwindCSS](https://tailwindcss.com/)
- [React Router](https://reactrouter.com/)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Retrofit](https://square.github.io/retrofit/)
