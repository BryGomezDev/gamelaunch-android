package com.gamelaunch.domain.model

enum class Region(val igdbId: Int, val displayName: String) {
    EUROPE(1, "Europa"),
    NORTH_AMERICA(2, "Norteamérica"),
    AUSTRALIA(3, "Australia"),
    NEW_ZEALAND(4, "Nueva Zelanda"),
    JAPAN(5, "Japón"),
    CHINA(6, "China"),
    ASIA(7, "Asia"),
    WORLDWIDE(8, "Mundial"),
    KOREA(9, "Corea"),
    BRAZIL(10, "Brasil");

    companion object {
        fun fromId(id: Int): Region = entries.firstOrNull { it.igdbId == id } ?: WORLDWIDE
    }
}
