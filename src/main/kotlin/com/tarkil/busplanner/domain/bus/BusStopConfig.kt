package com.tarkil.busplanner.domain.bus

import java.util.*

data class BusStopConfig(
    val busNumber: String,
    val info_hkbus: String
) {

    override fun toString(): String = "Bus #$busNumber - Info $info_hkbus"

    override fun hashCode(): Int {
        return (Objects.hash(busNumber, info_hkbus))
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || javaClass != other.javaClass) return false
        val that = other as BusStopConfig
        return this.busNumber == that.busNumber &&
                this.info_hkbus == that.info_hkbus
    }
}