package com.gamelaunch.domain.model

enum class Region(val igdbId: Int) {
    EUROPE(1),
    NORTH_AMERICA(2),
    AUSTRALIA(3),
    NEW_ZEALAND(4),
    JAPAN(5),
    CHINA(6),
    ASIA(7),
    WORLDWIDE(8),
    KOREA(9),
    BRAZIL(10);

    companion object {
        fun fromId(id: Int): Region = entries.firstOrNull { it.igdbId == id } ?: WORLDWIDE
    }
}
