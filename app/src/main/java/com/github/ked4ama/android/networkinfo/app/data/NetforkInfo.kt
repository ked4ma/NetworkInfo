package com.github.ked4ama.android.networkinfo.app.data

sealed class NetworkInfo {
    abstract val name: String
    abstract val ip: String

    data class Wifi(override val name: String, private val ipInt: Int) : NetworkInfo() {
        override val ip = parseIp(ipInt)
    }

    data class Mobile(override val name: String, private val ipInt: Int) : NetworkInfo() {
        override val ip = parseIp(ipInt)
    }

    object NONE : NetworkInfo() {
        override val name = "Not Found"
        override val ip = "----"
    }

    protected fun parseIp(ip: Int) = String.format(
        "%02d.%02d.%02d.%02d",
        ip shr 0 and 0xff,
        ip shr 8 and 0xff,
        ip shr 16 and 0xff,
        ip shr 24 and 0xff
    )
}
