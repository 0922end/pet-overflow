package com.petoverflow.app

class HeatEngine {
    var heat = 0
        private set
    var level: HeatLevel = HeatLevel.COOL
        private set

    fun addHeat(amount: Int) {
        heat = (heat + amount).coerceAtMost(100)
        updateLevel()
    }

    fun reduceHeat(amount: Int) {
        heat = (heat - amount).coerceAtLeast(0)
        updateLevel()
    }

    private fun updateLevel() {
        level = when {
            heat >= 80 -> HeatLevel.BURNING
            heat >= 60 -> HeatLevel.HOT
            heat >= 40 -> HeatLevel.WARM
            heat >= 20 -> HeatLevel.TEPID
            else -> HeatLevel.COOL
        }
    }

    enum HeatLevel {
        COOL( 끽训‌),
        TEPID( 微晨‌),
        WARM( 隆 犽‌),
        HOT( 发热”),
        BURNING( 发燌○)
    }
}
