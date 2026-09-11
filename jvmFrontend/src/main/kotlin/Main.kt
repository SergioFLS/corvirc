// SPDX-FileCopyrightText: 2026 SergOneZero
// SPDX-License-Identifier: MIT-0 OR Apache-2.0
import invalid.sergonezero.corvirc.CPU
import invalid.sergonezero.corvirc.Graphics
import invalid.sergonezero.corvirc.SCREEN_HEIGHT
import invalid.sergonezero.corvirc.SCREEN_WIDTH
import kotlinx.io.buffered
import kotlinx.io.bytestring.decodeToString
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteString
import kotlinx.io.readIntLe
import kotlinx.io.readUIntLe
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.WindowConstants

fun main() {
    // TODO is there a way to read directly from JAR instead of this repo?
    val testTxt = Path("jvmFrontend/src/main/resources/Test - Minimal test.v32")
    val g = Graphics()
    val cpu: CPU
    SystemFileSystem.source(testTxt).buffered().use {
        it.skip(0x80)
        require(it.readByteString(8).decodeToString() == "V32-VBIN")
        val wordSize = it.readIntLe()
        val program = IntArray(wordSize)
        for (i in program.indices) {
            program[i] = it.readIntLe()
        }
        cpu = CPU(program)

        //it.skip(0x314 - 0x80)
        require(it.readByteString(8).decodeToString() == "V32-VTEX")
        val textureWidth = it.readUIntLe()
        val textureHeight = it.readUIntLe()
        println("Texture width: $textureWidth height: $textureHeight")
        for (y in 0 ..<textureHeight.toInt()) {
            for (x in 0 ..<textureWidth.toInt()) {
                g.setTexturePixel(x, y, it.readIntLe())
            }
        }
    }

    cpu.runUntilHalt()
    println(cpu)

    g.clearColor = 0xFFFF00FF.toInt()
    g.clear()
    g.setPixel(0, 0, 0xFFFFFFFF.toInt())
    g.setPixel(1, 0, 0xFFFFFFFF.toInt())
    g.setPixel(2, 0, 0xFFFFFFFF.toInt())
    g.setPixel(2, 1, 0xFFFFFFFF.toInt())
    g.setPixel(2, 2, 0xFFFFFFFF.toInt())
    g.drawTexture()

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