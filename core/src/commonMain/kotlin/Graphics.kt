package invalid.sergonezero.corvirc

const val SCREEN_WIDTH = 640
const val SCREEN_HEIGHT = 360

class Graphics {
    val drawBuffer: IntArray = IntArray(SCREEN_WIDTH * SCREEN_HEIGHT) // in ABGR format
    var clearColor: Int = 0xFF000000.toInt()

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

    fun clear() {
        for (i in drawBuffer.indices) {
            drawBuffer[i] = clearColor
        }
    }
}