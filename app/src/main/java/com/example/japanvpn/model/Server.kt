package com.example.japanvpn.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Server(
    val id: String,
    val name: String,
    val city: String,
    val country: String,
    val protocol: Protocol,
    val hostname: String,
    val port: Int,
    val ping: Int = -1,
    val load: Int = 0,
    val isActive: Boolean = false
) : Parcelable {

    enum class Protocol {
        UDP,
        TCP;

        override fun toString(): String = name
    }

    companion object {
        fun createTestServers(): List<Server> = listOf(
            Server(
                id = "tokyo-1",
                name = "Tokyo Server 1",
                city = "Tokyo",
                country = "Japan",
                protocol = Protocol.UDP,
                hostname = "tokyo1.vpn.example.com",
                port = 1194,
                ping = 25,
                load = 45
            ),
            Server(
                id = "tokyo-2",
                name = "Tokyo Server 2",
                city = "Tokyo",
                country = "Japan",
                protocol = Protocol.TCP,
                hostname = "tokyo2.vpn.example.com",
                port = 443,
                ping = 35,
                load = 65
            ),
            Server(
                id = "osaka-1",
                name = "Osaka Server 1",
                city = "Osaka",
                country = "Japan",
                protocol = Protocol.UDP,
                hostname = "osaka1.vpn.example.com",
                port = 1194,
                ping = 30,
                load = 55
            ),
            Server(
                id = "osaka-2",
                name = "Osaka Server 2",
                city = "Osaka",
                country = "Japan",
                protocol = Protocol.TCP,
                hostname = "osaka2.vpn.example.com",
                port = 443,
                ping = 40,
                load = 75
            ),
            Server(
                id = "fukuoka-1",
                name = "Fukuoka Server 1",
                city = "Fukuoka",
                country = "Japan",
                protocol = Protocol.UDP,
                hostname = "fukuoka1.vpn.example.com",
                port = 1194,
                ping = 45,
                load = 35
            )
        )
    }
}
