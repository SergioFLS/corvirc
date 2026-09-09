import invalid.sergonezero.corvirc.Graphics
import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

fun main() {
    val g = Graphics()

    g.clearColor = 0xFF0000FF.toInt()
    g.clear()
    g.setPixel(0, 0, 0xFFFFFFFF.toInt())
    g.setPixel(1, 0, 0xFFFFFFFF.toInt())
    g.setPixel(2, 0, 0xFFFFFFFF.toInt())
    g.setPixel(2, 1, 0xFFFFFFFF.toInt())
    g.setPixel(2, 2, 0xFFFFFFFF.toInt())

    val myCanvas = document.getElementById("my-canvas")!! as HTMLCanvasElement
    val ctx = myCanvas.getContext("2d") as CanvasRenderingContext2D
    val myImageData = ctx.createImageData(640.0, 360.0)

    for (i in 0 until myImageData.data.length / 4) {
        // https://youtrack.jetbrains.com/issue/KT-24583
        val data = myImageData.data.asDynamic()
        data[i * 4 + 0] = (g.drawBuffer[i] and 0x000000FF)
        data[i * 4 + 1] = (g.drawBuffer[i] and 0x0000FF00) shr 8
        data[i * 4 + 2] = (g.drawBuffer[i] and 0x00FF0000) shr 16
        data[i * 4 + 3] = (g.drawBuffer[i].toLong() and 0xFF000000) shr 24
    }

    ctx.putImageData(myImageData, 0.0, 0.0)

    console.log(g.drawBuffer)
}