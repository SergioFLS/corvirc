package invalid.sergonezero.corvirc

interface MemoryDevice {
    fun memoryRead(address: Int): Int
    fun memoryWrite(address: Int, value: Int)
}