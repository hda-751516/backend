package co.selim.codeauth

import co.selim.codeauth.api.LiveViewResponse
import co.selim.codeauth.ext.logger
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import io.javalin.websocket.WsCloseContext
import io.javalin.websocket.WsConnectContext
import org.eclipse.jetty.websocket.api.Session
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import javax.imageio.ImageIO
import javax.json.bind.JsonbBuilder

private const val CAPTURE_SIZE = 1024

object LiveViewController {
    private val sessions = ConcurrentHashMap.newKeySet<Session>()

    private val isRunning = AtomicBoolean()

    private val base64Encoder by lazy {
        Base64.getEncoder()
    }

    private val jsonb by lazy {
        JsonbBuilder.create()
    }

    fun registerSession(context: WsConnectContext) {
        logger.info("New client connected ${context.host()}")
        sessions.add(context.session)
        isRunning.set(true)
        transmit()
    }

    fun removeSession(context: WsCloseContext) {
        logger.info("Client disconnected ${context.host()}")
        sessions.remove(context.session)
        if (sessions.isEmpty()) {
            logger.info("Stopping transmission")
            isRunning.set(false)
        }
    }

    private fun transmit() {
        logger.info("Starting transmission")
        while (isRunning.get()) {
            val photoBytes = takePhoto()
            val message = decodeImage(photoBytes)
            val response = LiveViewResponse(base64Encoder.encodeToString(photoBytes), message)
            val responseAsJson = jsonb.toJson(response)
            sessions.forEach { session ->
                if (session.isOpen)
                    session.remote.sendString(responseAsJson)
            }
        }
    }

    private fun takePhoto(): ByteArray {
        val command = listOf(
            "raspistill",
            "--timeout", "1",
            "-w", (CAPTURE_SIZE / 2).toString(),
            "-h", CAPTURE_SIZE.toString(),
            "-n",
            "--shutter", "2500",
            "-o", "pic.jpg"
        )

        val captureProcess = ProcessBuilder(command)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()

        captureProcess.waitFor()
        return File("pic.jpg").readBytes()
    }

    private fun decodeImage(photoBytes: ByteArray): String {
        val bufferedImage = ImageIO.read(photoBytes.inputStream())
        val luminanceSource = BufferedImageLuminanceSource(bufferedImage)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(luminanceSource))

        return try {
            val result = MultiFormatReader().decode(binaryBitmap)
            if (AuthController.code == result.text.toInt()) {
                "Welcome on board!"
            } else {
                "Invalid QR code."
            }
        } catch (e: NotFoundException) {
            "Point your QR code to the camera to check in"
        } catch (other: Exception) {
            "Invalid QR code"
        }
    }
}