package co.selim.codeauth

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*

fun main() {
    Javalin.create { config ->
        config.defaultContentType = "application/json"
        config.autogenerateEtags = true
        config.dynamicGzip = true
        config.addStaticFiles("/static")
    }.routes {
        path("/booking") {
            get(AuthController::getBooking)
        }
        ws("/liveView") { webSocket ->
            webSocket.onConnect(LiveViewController::registerSession)
            webSocket.onClose(LiveViewController::removeSession)
        }
    }.start(8080)
}