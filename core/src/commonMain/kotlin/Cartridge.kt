package invalid.sergonezero.corvirc

import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.Source
import kotlinx.io.bytestring.decodeToString
import kotlinx.io.readByteString
import kotlinx.io.readIntLe

class Cartridge(
    val virconVersion: Int = 1,
    val virconRevision: Int = 0,
    var title: String = "",
    val romVersion: Int = 1,
    val romRevision: Int = 0,
    var program: IntArray? = null,
    var textures: Array<Texture>? = null
) : MemoryDevice, ControlDevice {
    val initialized: Boolean
        get() = program != null

    constructor(program: IntArray, textures: Array<Texture>) : this() {
        this.program = program
        this.textures = textures
    }

    constructor(data: ByteArray) : this() {
        var buffer = Buffer()
        buffer.write(data)
        val magic = buffer.readByteString(8).decodeToString()
        require((magic == "V32-CART") || (magic == "V32-BIOS"))
        val virconVersion = buffer.readIntLe()
        val virconRevision = buffer.readIntLe()
        title = buffer.readByteString(64).decodeToString()
        val romVersion = buffer.readIntLe()
        val romRevision = buffer.readIntLe()
        val textures = Array(buffer.readIntLe()) { Texture() }
        val numberSounds = buffer.readIntLe()
        val programOffset = buffer.readIntLe()
        val programSize = buffer.readIntLe()
        val videoOffset = buffer.readIntLe()
        val videoSize = buffer.readIntLe()
        val audioOffset = buffer.readIntLe()
        val audioSize = buffer.readIntLe()
        buffer.skip(8) // reserved
        buffer.close()

        buffer = Buffer()
        buffer.write(data)
        buffer.skip(programOffset.toLong())
        require(buffer.readByteString(8).decodeToString() == "V32-VBIN")
        val program = IntArray(buffer.readIntLe())
        for (i in program.indices) {
            program[i] = buffer.readIntLe()
        }
        buffer.close()

        buffer = Buffer()
        buffer.write(data)
        buffer.skip(videoOffset.toLong())
        for (i in textures.indices) {
            require(buffer.readByteString(8).decodeToString() == "V32-VTEX")
            val width = buffer.readIntLe()
            val height = buffer.readIntLe()
            val data = IntArray(width * height)
            for (j in data.indices) {
                data[j] = buffer.readIntLe()
            }
            textures[i] = Texture(width, height, data)
        }
        buffer.close()

        // TODO sounds

        this.program = program
        this.textures = textures
    }
    override fun memoryRead(address: Int): Int {
        val v = program!![address]
        println("memoryRead: $address = $v on ${toString()}")
        return v
    }

    override fun memoryWrite(address: Int, value: Int) {
        TODO("Not yet implemented")
    }

    override fun controlRead(address: Int): Int {
        TODO("Not yet implemented")
    }

    override fun controlWrite(address: Int, value: Int) {
        TODO("Not yet implemented")
    }

    override fun toString() = "Cartridge: $title"
}