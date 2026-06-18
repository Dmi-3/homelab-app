# Homelab App

Нативное Android-приложение (Kotlin + Jetpack Compose) для домашнего сервера:
управление qBittorrent и пуш-уведомления через ntfy.

## Возможности (v1)
- **Торренты** — список с прогрессом и скоростями, пауза/старт, удаление
  (с файлами или только из списка), добавление magnet-ссылки. Автообновление каждые 2 с.
- **Пуши** — фоновый сервис подписывается на тему [ntfy](https://ntfy.sh) и показывает
  уведомления (живёт после закрытия приложения). Сюда можно слать события от *arr,
  qBittorrent и т.д.
- **Настройки** — адреса/логины qBittorrent и ntfy, токен, переключатель пушей.

## Технологии
- Kotlin 2.0, Jetpack Compose (Material 3), Navigation Compose
- OkHttp (qBittorrent WebUI API v2, поток ntfy `/json`)
- kotlinx.serialization, Coroutines
- minSdk 26, targetSdk 35

## Сборка
APK собирается автоматически в **GitHub Actions** при каждом push в `main`
(workflow `.github/workflows/build.yml`).

Скачать готовый APK:
1. Вкладка **Actions** в репозитории → последний успешный запуск **Build APK**.
2. Раздел **Artifacts** → `homelab-debug-apk` → скачать и распаковать.

Локальная сборка (если нужен Android SDK + Gradle 8.10+):
```bash
gradle assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Установка
Через ADB (есть в platform-tools):
```bash
adb install -r app-debug.apk
# или на ТВ-приставку:
adb connect 192.168.0.237:5555 && adb -s 192.168.0.237:5555 install -r app-debug.apk
```
Либо скинуть APK на телефон и установить вручную (разрешив «из неизвестных источников»).

## Первая настройка в приложении
1. **Настройки** → qBittorrent: адрес (`http://192.168.0.107:8070` дома),
   логин/пароль.
2. **ntfy**: адрес (`https://ntfy.bad-cat.duckdns.org`), тема (`homelab`),
   токен доступа (Bearer).
3. Включи переключатель **Пуш-уведомления** и разреши уведомления.

> Адреса `*.bad-cat.duckdns.org` работают и дома, и снаружи. Локальные `.lan` —
> только в домашней сети.

## Дорожная карта
См. `BACKLOG.md` в репозитории инфраструктуры. Ближайшее: *arr → ntfy вебхуки,
управление Radarr/Sonarr, виджеты на рабочий стол.
