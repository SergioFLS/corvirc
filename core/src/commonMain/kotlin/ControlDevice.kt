package invalid.sergonezero.corvirc

interface ControlDevice {
    fun controlRead(address: Int): Int
    fun controlWrite(address: Int, value: Int)
}