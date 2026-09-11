package invalid.sergonezero.corvirc

interface SlaveDevice {
    fun read(address: Int): Int
    fun write(address: Int, value: Int)
}