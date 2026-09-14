// SPDX-FileCopyrightText: 2026 SergOneZero
// SPDX-License-Identifier: MIT-0 OR Apache-2.0
import invalid.sergonezero.corvirc.CPU
import invalid.sergonezero.corvirc.Cartridge
import invalid.sergonezero.corvirc.Graphics
import invalid.sergonezero.corvirc.SCREEN_HEIGHT
import invalid.sergonezero.corvirc.SCREEN_WIDTH
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.InputStream
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.WindowConstants

// exists only to use getResourceAsStream
object Dummy {
    fun getResourceAsStream(name: String): InputStream? {
        return javaClass.getResourceAsStream(name)
    }
}

fun main() {
    val bios = Cartridge(Dummy.getResourceAsStream("/StandardBios.v32")!!.readBytes())
    val cart = Cartridge()

    val g = Graphics(bios.textures!![0], cart)
    val cpu = CPU(bios, cart, g)

    fun runCPUFrame() {
        cpu.runUntilHaltOrWait()
        cpu.frame()
    }

    for (i in 1..250) {
        runCPUFrame()
    }
    println(cpu)

//    g.clearColor = 0xFFFF00FF.toInt()
//    g.clear()
//    g.setPixel(0, 0, 0xFFFFFFFF.toInt())
//    g.setPixel(1, 0, 0xFFFFFFFF.toInt())
//    g.setPixel(2, 0, 0xFFFFFFFF.toInt())
//    g.setPixel(2, 1, 0xFFFFFFFF.toInt())
//    g.setPixel(2, 2, 0xFFFFFFFF.toInt())
//    g.drawTexture()

    val image = BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB)

    fun updateScreenBuffer() {
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
    }
    updateScreenBuffer()

    val imageComponent = ImageComponent(image)
    imageComponent.setBounds(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT)
    val jframe = JFrame()
    val stepButton = JButton("step")
    stepButton.setBounds(641, 10, 60, 20)
    stepButton.addActionListener { _ ->
        runCPUFrame()
        updateScreenBuffer()
        println(cpu.frameCounter)
        jframe.repaint()
    }

    jframe.apply {
        contentPane.layout = null
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        minimumSize = Dimension(SCREEN_WIDTH + 70, SCREEN_HEIGHT)
        add(imageComponent)
        add(stepButton)
        isVisible = true
    }
}

private class ImageComponent(val image: BufferedImage) : JComponent() {
    override fun paint(g: java.awt.Graphics) {
        g as Graphics2D
        g.drawImage(image as Image, 0, 0, null)
    }
}
