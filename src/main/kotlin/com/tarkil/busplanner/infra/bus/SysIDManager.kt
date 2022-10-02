package com.tarkil.busplanner.infra.bus

import org.slf4j.LoggerFactory

class SysIDManager {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private var sysid = 0

    fun getNextValidSysID(): String {
        this.sysid = this.sysid + 1
        return this.sysid.toString()
    }

    fun getNextNextValidSysID(): String {
        this.sysid = this.sysid + 1
        this.sysid = this.sysid + 1
        return this.sysid.toString()
    }

    fun resetSysId() {
        this.sysid = 0
    }
}