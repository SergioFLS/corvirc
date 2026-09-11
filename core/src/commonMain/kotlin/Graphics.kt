// SPDX-FileCopyrightText: 2026 SergOneZero
// SPDX-License-Identifier: MIT-0 OR Apache-2.0

package invalid.sergonezero.corvirc

data class DecomposedPixel(
    val red: Int,
    val green: Int,
    val blue: Int,
    val alpha: Int
) {
    constructor(pixel: Int) : this(
        pixel and 0x000000FF,
        (pixel and 0x0000FF00) shr 8,
        (pixel and 0x00FF0000) shr 16,
        (pixel shr 24) and 0xFF
    )

    fun toInt(): Int = (
            red or (green shl 8)
                or (blue shl 16)
                or (alpha shl 24)
            )

}
const val SCREEN_WIDTH = 640
const val SCREEN_HEIGHT = 360
private const val TEXTURE_SIZE = 1024

class Graphics : SlaveDevice {
    val drawBuffer: IntArray = IntArray(SCREEN_WIDTH * SCREEN_HEIGHT) // in ABGR format
    val texture = IntArray(TEXTURE_SIZE * TEXTURE_SIZE)

    var clearColor: Int = 0xFF000000.toInt()
    var selectedTexture: Int = 0
    var selectedRegion: Int = 0
    var drawingPointX: Int = 0
    var drawingPointY: Int = 0
    var regionMinX: Int = 0
    var regionMinY: Int = 0
    var regionMaxX: Int = 0
    var regionMaxY: Int = 0
    var regionHotspotX: Int = 0
    var regionHotspotY: Int = 0

    fun getTexturePixel(x: Int, y: Int): Int? {
        if (x !in 0..<TEXTURE_SIZE) return null
        if (y !in 0..<TEXTURE_SIZE) return null
        return texture[y * TEXTURE_SIZE + x]
    }

    fun setTexturePixel(x: Int, y: Int, color: Int) {
        if (x !in 0..<TEXTURE_SIZE) return
        if (y !in 0..<TEXTURE_SIZE) return
        texture[y * TEXTURE_SIZE + x] = color
    }

    fun getPixel(x: Int, y: Int): Int? {
        if (x !in 0..<SCREEN_WIDTH) return null
        if (y !in 0..<SCREEN_HEIGHT) return null
        return drawBuffer[y * SCREEN_WIDTH + x]
    }

    fun setPixel(x: Int, y: Int, color: Int) {
        if (x !in 0..<SCREEN_WIDTH) return
        if (y !in 0..<SCREEN_HEIGHT) return
        drawBuffer[y * SCREEN_WIDTH + x] = color
    }

    fun putPixel(x: Int, y: Int, color: Int) {
        val currentPixel = DecomposedPixel(getPixel(x, y) ?: 0)
        val inPixel = DecomposedPixel(color)

        setPixel(x, y, DecomposedPixel(
            (inPixel.red * inPixel.alpha + currentPixel.red * (255 - inPixel.alpha)) / 255,
            (inPixel.green * inPixel.alpha + currentPixel.green * (255 - inPixel.alpha)) / 255,
            (inPixel.blue * inPixel.alpha + currentPixel.blue * (255 - inPixel.alpha)) / 255,
            255
        ).toInt())
    }

    fun clear() {
        for (i in drawBuffer.indices) {
            drawBuffer[i] = clearColor
        }
    }

    fun drawRegion() {
        for (y in regionMinY .. regionMaxY) {
            for (x in regionMinX .. regionMaxX) {
                val pixel = getTexturePixel(x, y) ?: 0
                if (pixel == 0) continue
                putPixel(x + drawingPointX - regionHotspotX, y + drawingPointY - regionHotspotY, pixel)
            }
        }
    }

    override fun read(address: Int): Int {
        TODO("Not yet implemented")
    }

    override fun write(address: Int, value: Int) {
        when (address) {
            0x00 -> {
                when (value) {
                    0x10 -> clear()
                    0x11 -> drawRegion()
                    else -> TODO("Unsupported command: $value")
                }
            }
            0x02 -> clearColor = value
            0x05 -> selectedTexture = value
            0x06 -> selectedRegion = value
            0x07 -> drawingPointX = value
            0x08 -> drawingPointY = value
            0x0C -> regionMinX = value
            0x0D -> regionMinY = value
            0x0E -> regionMaxX = value
            0x0F -> regionMaxY = value
            0x10 -> regionHotspotX = value
            0x11 -> regionHotspotY = value
            else -> TODO("Unsupported address: $address")
        }
    }
}