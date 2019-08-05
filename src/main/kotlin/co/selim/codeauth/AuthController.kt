package co.selim.codeauth

import co.selim.codeauth.api.Booking
import io.javalin.http.Context
import java.util.concurrent.ThreadLocalRandom
import javax.json.bind.JsonbBuilder

object AuthController {
    val code by lazy {
        ThreadLocalRandom.current().nextInt(100_000, 1_000_000)
    }

    private val jsonb by lazy {
        JsonbBuilder.create()
    }

    fun getBooking(context: Context) {
        val response = jsonb.toJson(
            Booking(
                "An der Welle 3, Frankfurt am Main",
                "Opernplatz, Frankfurt am Main",
                "04.05.2019 // 12:25",
                "04.05.2019 // 12:36",
                code
            )
        )

        context.result(response)
    }
}