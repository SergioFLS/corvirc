// SPDX-FileCopyrightText: 2026 SergOneZero
// SPDX-License-Identifier: MIT-0 OR Apache-2.0

import invalid.sergonezero.corvirc.Graphics
import invalid.sergonezero.corvirc.CPU
import invalid.sergonezero.corvirc.Cartridge
import js.date.Date
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
        val start = Date.now()
        cpu.runUntilHaltOrWait()
        console.log("Run finished in ${Date.now() - start} ms")
        if (g.screenUpdated) {
            for (i in 0 until myImageData.data.length / 4) {
                // https://youtrack.jetbrains.com/issue/KT-24583
                myImageData.data.asDynamic()[i * 4    ] = (g.drawBuffer[i] and 0xFF)
                myImageData.data.asDynamic()[i * 4 + 1] = (g.drawBuffer[i] shr 8) and 0xFF
                myImageData.data.asDynamic()[i * 4 + 2] = (g.drawBuffer[i] shr 16) and 0xFF
                myImageData.data.asDynamic()[i * 4 + 3] = (g.drawBuffer[i] shr 24) and 0xFF
            }

            ctx.putImageData(myImageData, 0.0, 0.0)
        }

        cpu.frame()
        console.log("Frame finished in ${Date.now() - start} ms")
    }

    window.setInterval({ runCPUFrame() }, 1000/60)
}