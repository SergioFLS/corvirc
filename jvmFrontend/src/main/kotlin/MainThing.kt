import invalid.sergonezero.corvirc.Graphics
import invalid.sergonezero.corvirc.SCREEN_HEIGHT
import invalid.sergonezero.corvirc.SCREEN_WIDTH
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.WindowConstants

fun main() {
    val g = Graphics()

    g.clearColor = 0xFF0000FF.toInt()
    g.clear()
    g.setPixel(0, 0, 0xFFFFFFFF.toInt())
    g.setPixel(1, 0, 0xFFFFFFFF.toInt())
    g.setPixel(2, 0, 0xFFFFFFFF.toInt())
    g.setPixel(2, 1, 0xFFFFFFFF.toInt())
    g.setPixel(2, 2, 0xFFFFFFFF.toInt())

    val image = BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB)

    for (y in 0 until SCREEN_HEIGHT) {
        for (x in 0 until SCREEN_WIDTH) {
            val pixelABGR = g.getPixel(x, y) ?: 0
            val pixel = (
                    (pixelABGR and 0xFF00FF00.toInt())
                            or ((pixelABGR and 0x000000FF) shl 16)
                            or ((pixelABGR and 0x00FF0000) shr 16))
            image.setRGB(x, y, pixel)
        }
    }

    val imageComponent = ImageComponent(image)
    JFrame().apply {
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        minimumSize = Dimension(SCREEN_WIDTH, SCREEN_HEIGHT)
        add(imageComponent)
        isVisible = true
    }
}

private class ImageComponent(val image: BufferedImage) : JComponent() {
    override fun paint(g: java.awt.Graphics) {
        g as Graphics2D
        g.drawImage(image as Image, 0, 0, null)
    }
}