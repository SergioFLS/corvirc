package invalid.sergonezero.corvirc

class RNG : ControlDevice {
    var currentValue = 1

    fun next(): Int {
        val v = currentValue.toLong() * 48271.toLong()
        return v.mod(0x7FFFFFFF.toLong()).toInt()
    }
    override fun controlRead(address: Int): Int {
        if (address != 0)
            TODO("RNG address read nonzero: $address")
        val output = currentValue
        currentValue = next()
        return output
    }

    override fun controlWrite(address: Int, value: Int) {
        if (address != 0)
            TODO("RNG address write nonzero: $address")
        currentValue = value
    }
}