package com.homelab.app.data

/** Сервис в дашборде. */
data class Service(
    val name: String,
    val url: String,
    val emoji: String,
)

data class ServiceGroup(
    val title: String,
    val items: List<Service>,
)

/**
 * Каталог сервисов. Где есть внешний HTTPS (*.bad-cat.duckdns.org) — он работает и дома,
 * и снаружи. Остальные по IP:порту — только в домашней сети.
 */
val SERVICE_CATALOG = listOf(
    ServiceGroup(
        "Книги / Манга / Комиксы", listOf(
            Service("Kavita", "https://kavita.bad-cat.duckdns.org", "📖"),
            Service("Suwayomi", "https://suwayomi.bad-cat.duckdns.org", "🗾"),
            Service("Kapowarr", "https://kapowarr.bad-cat.duckdns.org", "🦸"),
            Service("LazyLibrarian", "http://192.168.0.107:5299", "📚"),
        )
    ),
    ServiceGroup(
        "Медиастек", listOf(
            Service("Jellyfin", "http://192.168.0.107:8096", "🎬"),
            Service("Jellyseerr", "http://192.168.0.107:5055", "🎟️"),
            Service("Radarr", "http://192.168.0.107:7878", "🎥"),
            Service("Sonarr", "http://192.168.0.107:8989", "📺"),
            Service("Lidarr", "http://192.168.0.107:8686", "🎵"),
            Service("Bazarr", "http://192.168.0.107:6767", "💬"),
            Service("Prowlarr", "http://192.168.0.107:9696", "🔎"),
            Service("qBittorrent", "http://192.168.0.107:8070", "🧲"),
            Service("Lampac", "http://192.168.0.107:9118", "🍿"),
            Service("TorrServer", "http://192.168.0.107:5665", "🌐"),
        )
    ),
    ServiceGroup(
        "Умный дом", listOf(
            Service("Home Assistant", "http://192.168.0.107:8123", "🏠"),
            Service("Zigbee2MQTT", "http://192.168.0.107:8080", "📡"),
            Service("Immich", "http://192.168.0.107:2283", "📷"),
        )
    ),
    ServiceGroup(
        "Инфраструктура", listOf(
            Service("Дашборд", "https://home.bad-cat.duckdns.org", "📊"),
            Service("Vaultwarden", "https://vault.bad-cat.duckdns.org", "🔐"),
            Service("BookStack", "https://wiki.bad-cat.duckdns.org", "📔"),
            Service("AdGuard", "http://192.168.0.107:8083", "🛡️"),
            Service("Portainer", "https://192.168.0.107:9443", "🐳"),
        )
    ),
)
