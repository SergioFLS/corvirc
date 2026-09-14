// SPDX-FileCopyrightText: 2026 SergOneZero
// SPDX-License-Identifier: MIT-0 OR Apache-2.0

import invalid.sergonezero.corvirc.Graphics
import invalid.sergonezero.corvirc.CPU
import invalid.sergonezero.corvirc.Cartridge
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import web.http.bytes
import web.http.fetch

suspend fun urlToByteArray(url: String): ByteArray {
    val request = fetch(url)
    check(request.ok)

    val bytes = request.bytes()
    val output = ByteArray(bytes.length)
    for (i in output.indices) {
        output[i] = bytes[i].toByte()
    }

    return output
}

suspend fun main() {
    val bios = Cartridge(urlToByteArray("/StandardBios.v32"))
    val cart = Cartridge(urlToByteArray("/Test - Minimal test.v32"))
    //val cart = Cartridge()

    val g = Graphics(bios.textures!![0], cart)
    val cpu = CPU(bios, cart, g)

    val myCanvas = document.getElementById("my-canvas")!! as HTMLCanvasElement
    val ctx = myCanvas.getContext("2d") as CanvasRenderingContext2D
    val myImageData = ctx.createImageData(640.0, 360.0)

    fun runCPUFrame() {
        cpu.runUntilHaltOrWait()
        cpu.frame()

        for (i in 0 until myImageData.data.length / 4) {
            // https://youtrack.jetbrains.com/issue/KT-24583
            val data = myImageData.data.asDynamic()
            data[i * 4 + 0] = (g.drawBuffer[i] and 0x000000FF)
            data[i * 4 + 1] = (g.drawBuffer[i] and 0x0000FF00) shr 8
            data[i * 4 + 2] = (g.drawBuffer[i] and 0x00FF0000) shr 16
            data[i * 4 + 3] = (g.drawBuffer[i].toLong() and 0xFF000000) shr 24
        }

        ctx.putImageData(myImageData, 0.0, 0.0)
    }

    window.setInterval({ runCPUFrame() }, 1000/60)
}