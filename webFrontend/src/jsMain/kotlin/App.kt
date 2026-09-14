// SPDX-FileCopyrightText: 2026 SergOneZero
// SPDX-License-Identifier: MIT-0 OR Apache-2.0

import invalid.sergonezero.corvirc.Graphics
import invalid.sergonezero.corvirc.CPU
import js.buffer.ArrayBuffer
import js.buffer.DataView
import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import web.http.arrayBuffer
import web.http.fetch

fun getProgram(cart: DataView<ArrayBuffer>): IntArray {
    val offset = cart.getInt32(0x60, true)

    require(cart.getInt32(offset + 0, false) == 0x5633322D) // V32-
    require(cart.getInt32(offset + 4, false) == 0x5642494E) // VBIN
    val wordSize = cart.getInt32(offset + 8, true)
    val program = IntArray(wordSize)
    for (i in 0 until wordSize) {
        program[i] = cart.getInt32(offset + 12 + (i * 4), true)
    }
    return program
}

fun loadTexture(gpu: Graphics, cart: DataView<ArrayBuffer>) {
    val offset = cart.getInt32(0x68, true)

    require(cart.getInt32(offset + 0, false) == 0x5633322D) // V32-
    require(cart.getInt32(offset + 4, false) == 0x56544558) // VTEX

    val textureWidth = cart.getInt32(offset + 8, true)
    val textureHeight = cart.getInt32(offset + 12, true)
    var dataOffset = offset + 16
    for (y in 0 until textureHeight) {
        for (x in 0 until textureWidth) {
            gpu.setTexturePixel(x, y, cart.getInt32(dataOffset, true))
            dataOffset += 4
        }
    }
}

suspend fun main() {
    val cartRequest = fetch("/Test - Minimal test.v32")
    check(cartRequest.ok)

    val cartV32 = DataView(cartRequest.arrayBuffer())
    val g = Graphics(bios.textures!![0], cart.textures!!)
    val cpu = CPU(getProgram(cartV32), g)
    console.log(cpu.program)
    loadTexture(g, cartV32)
    cpu.runUntilHaltOrWait()

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