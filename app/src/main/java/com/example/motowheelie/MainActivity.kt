package com.example.motowheelie

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD
import java.io.IOException
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.concurrent.CopyOnWriteArrayList

class MainActivity : Activity() {
    private var server: RelayServer? = null

    inner class Bridge {
        @JavascriptInterface
        fun startServer(): String {
            if (server == null) {
                server = RelayServer(8080).also { it.start(0, false) }
            }
            return localIp()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.addJavascriptInterface(Bridge(), "Android")
        setContentView(web)
        web.loadUrl("file:///android_asset/index.html")
    }

    override fun onDestroy() {
        server?.stop()
        super.onDestroy()
    }

    // First private IPv4 address of this phone (the hotspot address when this phone is the hotspot).
    private fun localIp(): String {
        try {
            for (ni in NetworkInterface.getNetworkInterfaces()) {
                for (a in ni.inetAddresses) {
                    if (a is Inet4Address && !a.isLoopbackAddress && a.isSiteLocalAddress) {
                        return a.hostAddress ?: continue
                    }
                }
            }
        } catch (e: Exception) { }
        return "unknown"
    }
}

// Relays every message to all other connected phones.
class RelayServer(port: Int) : NanoWSD(port) {
    private val clients = CopyOnWriteArrayList<Sock>()

    override fun openWebSocket(handshake: NanoHTTPD.IHTTPSession): NanoWSD.WebSocket = Sock(handshake)

    inner class Sock(h: NanoHTTPD.IHTTPSession) : NanoWSD.WebSocket(h) {
        override fun onOpen() { clients.add(this) }
        override fun onClose(code: NanoWSD.WebSocketFrame.CloseCode?, reason: String?, initiatedByRemote: Boolean) {
            clients.remove(this)
        }
        override fun onMessage(message: NanoWSD.WebSocketFrame) {
            val text = message.textPayload
            for (c in clients) {
                if (c !== this) {
                    try { c.send(text) } catch (e: IOException) { clients.remove(c) }
                }
            }
        }
        override fun onPong(pong: NanoWSD.WebSocketFrame?) { }
        override fun onException(exception: IOException?) { clients.remove(this) }
    }
}
