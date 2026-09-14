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

data class Region(
    var minX: Int = 0,
    var minY: Int = 0,
    var maxX: Int = 0,
    var maxY: Int = 0,
    var hotspotX: Int = 0,
    var hotspotY: Int = 0
)

const val SCREEN_WIDTH = 640
const val SCREEN_HEIGHT = 360

class Graphics(val biosTexture: Texture, val cartridge: Cartridge) : ControlDevice {
    val drawBuffer: IntArray = IntArray(SCREEN_WIDTH * SCREEN_HEIGHT) // in ABGR format

    var clearColor: Int = 0xFF000000.toInt()
    var multiplyColor: Int = 0xFFFFFFFF.toInt()
    var selectedTexture: Int = -1
        set(value) { field = if (cartridge.isInitialized) value else -1 }
    val texture: Texture
        get() = if (selectedTexture <= -1) biosTexture else cartridge.textures!![selectedTexture]
    var selectedRegion: Int = 0
        set(value) { if (value in 0 .. 4095) field = value }
    var drawingPointX: Int = 0
    var drawingPointY: Int = 0
    var drawingScaleX: Int = 0 // TODO use
    var drawingScaleY: Int = 0 // TODO use
    var drawingAngle: Float = 0F // TODO use

    val regions = Array(4096) { Region() }
    var regionMinX: Int
        get() = regions[selectedRegion].minX
        set(value) { regions[selectedRegion].minX = value }
    var regionMinY: Int
        get() = regions[selectedRegion].minY
        set(value) { regions[selectedRegion].minY = value }
    var regionMaxX: Int
        get() = regions[selectedRegion].maxX
        set(value) { regions[selectedRegion].maxX = value }
    var regionMaxY: Int
        get() = regions[selectedRegion].maxY
        set(value) { regions[selectedRegion].maxY = value }
    var regionHotspotX: Int
        get() = regions[selectedRegion].hotspotX
        set(value) { regions[selectedRegion].hotspotX = value }
    var regionHotspotY: Int
        get() = regions[selectedRegion].hotspotY
        set(value) { regions[selectedRegion].hotspotY = value }

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
                val pixel = texture.getPixel(x, y) ?: 0
                if (pixel == 0) continue
                putPixel(x + drawingPointX - regionHotspotX, y + drawingPointY - regionHotspotY, pixel)
            }
        }
    }

    override fun controlRead(address: Int): Int {
        return when (address) {
            0x02 -> clearColor
            0x03 -> multiplyColor
            0x05 -> selectedTexture
            0x06 -> selectedRegion
            0x07 -> drawingPointX
            0x08 -> drawingPointY
            0x0B -> drawingAngle.toRawBits()
            0x0C -> regionMinX
            0x0D -> regionMinY
            0x0E -> regionMaxX
            0x0F -> regionMaxY
            0x10 -> regionHotspotX
            0x11 -> regionHotspotY
            else -> TODO("Unsupported read address: $address")
        }
    }

    override fun controlWrite(address: Int, value: Int) {
        when (address) {
            0x00 -> {
                when (value) {
                    0x10 -> clear()
                    0x11 -> drawRegion()
                    0x12 -> {
                        println("draw region zoomed stub") // TODO
                        drawRegion()
                    }
                    0x13 -> {
                        println("draw region rotated stub") // TODO
                        drawRegion()
                    }
                    0x14 -> {
                        println("draw region rotozoomed stub") // TODO
                        drawRegion()
                    }
                    else -> TODO("Unsupported command: $value")
                }
            }
            0x02 -> clearColor = value
            0x03 -> multiplyColor = value
            0x05 -> selectedTexture = value
            0x06 -> selectedRegion = value
            0x07 -> drawingPointX = value
            0x08 -> drawingPointY = value
            0x09 -> drawingScaleX = value
            0x0A -> drawingScaleY = value
            0x0B -> drawingAngle = Float.fromBits(value)
            0x0C -> regionMinX = value
            0x0D -> regionMinY = value
            0x0E -> regionMaxX = value
            0x0F -> regionMaxY = value
            0x10 -> regionHotspotX = value
            0x11 -> regionHotspotY = value
            else -> TODO("Unsupported write address: $address")
        }
    }
}