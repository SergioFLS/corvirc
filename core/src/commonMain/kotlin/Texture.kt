package invalid.sergonezero.corvirc

private const val TEXTURE_SIZE = 1024

class Texture(
    val canonicalWidth: Int = TEXTURE_SIZE,
    val canonicalHeight: Int = TEXTURE_SIZE,
) {
    val data = IntArray(TEXTURE_SIZE * TEXTURE_SIZE)

    fun getPixel(x: Int, y: Int): Int? {
        if (x !in 0..<TEXTURE_SIZE) return null
        if (y !in 0..<TEXTURE_SIZE) return null
        return data[y * TEXTURE_SIZE + x]
    }

    fun setPixel(x: Int, y: Int, color: Int) {
        if (x !in 0..<TEXTURE_SIZE) return
        if (y !in 0..<TEXTURE_SIZE) return
        data[y * TEXTURE_SIZE + x] = color
    }

    constructor(
        canonicalWidth: Int,
        canonicalHeight: Int,
        canonicalData: IntArray) : this(canonicalWidth, canonicalHeight) {
        for (y in 0 until canonicalHeight) {
            for (x in 0 until canonicalWidth) {
                setPixel(x, y, canonicalData[y * canonicalWidth + x])
            }
        }
    }
}