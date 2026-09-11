package invalid.sergonezero.corvirc

enum class Opcode(val op: Int) {
    // CPU control
    HALT(0),
    WAIT(1),
    // Jump instructions
    JUMP(2),
    CALL(3),
    RETURN(4),
    JUMP_TRUE(5),
    JUMP_FALSE(6),
    // Integer comparisons
    INT_EQUAL(7),
    INT_NOT_EQUAL(8),
    INT_GREATER(9),
    INT_GREATER_OR_EQUAL(10),
    INT_LESS(11),
    INT_LESS_OR_EQUAL(12),
    // Float comparisons
    FLOAT_EQUAL(13),
    FLOAT_NOT_EQUAL(14),
    FLOAT_GREATER(15),
    FLOAT_GREATER_OR_EQUAL(16),
    FLOAT_LESS(17),
    FLOAT_LESS_OR_EQUAL(18),
    // Data movement
    MOVE(19),
    LOAD_EFFECTIVE_ADDRESS(20),
    PUSH(21),
    POP(22),
    INPUT(23),
    OUTPUT(24),
    // String operations
    MOVE_STRING(25),
    SET_STRING(26),
    COMPARE_STRING(27),
    // Data conversion
    CONVERT_INT_FLOAT(28),
    CONVERT_FLOAT_INT(29),
    CONVERT_INT_BOOLEAN(30),
    CONVERT_FLOAT_BOOLEAN(31),
    // Binary
    NOT(32),
    AND(33),
    OR(34),
    XOR(35),
    BOOLEAN_NOT(36),
    SHIFT_LEFT(37),
    // Integer arithmetic
    INT_ADD(38),
    INT_SUBTRACT(39),
    INT_MULIPLY(40),
    INT_DIVIDE(41),
    INT_MODULUS(42),
    INT_SIGN(43),
    INT_MIN(44),
    INT_MAX(45),
    INT_ABSOLUTE(46),
    // Float arithmetic
    FLOAT_ADD(47),
    FLOAT_SUBTRACT(48),
    FLOAT_MULTIPLY(49),
    FLOAT_DIVIDE(50),
    FLOAT_MODULUS(51),
    FLOAT_SIGN(52),
    FLOAT_MIN(53),
    FLOAT_MAX(54),
    FLOAT_ABSOLUTE(55),
    // Extended float operations
    FLOOR(56),
    CEILING(57),
    ROUND(58),
    SINE(59),
    COSINE(60),
    ARC_COSINE(61),
    ARC_TANGENT2(62),
    LOGARITHM(63),
    POWER(64);

    companion object {
        fun fromInt(i: Int): Opcode? = entries.find { it.op == i }
    }
}

data class Instruction(
    val opcode: Opcode,
    val isImmediate: Boolean,
    val r1: Int,
    val r2: Int,
    val addressingMode: Int,
    val portAddress: Int
) {
    constructor(instruction: Int) : this(
        // 0bOOOOOO_I_RRRR_rrrr_AAA_PPPPPPPPPPPPPP
        // O: opcode
        // I: immediate
        // R: register 1
        // r: register 2
        // A: addressing mode for MOVE
        // P: I/O port
        Opcode.fromInt((instruction shr 26) and 63)!!,
        (instruction and 0x2000000) != 0,
        (instruction and 0x1E00000) shr 21,
        (instruction and 0x1E0000) shr 17,
        (instruction and 0x1C000) shr 14,
        instruction and 0x3FFF
    )
}
const val STACK_POINTER = 15

class CPU(
    val program: IntArray,
    val gpu: Graphics
) {
    var instructionPointer: Int = 0x20000000
    var instructionRegister: Int = 0
    val immediateValue: Int = 0
    val registers = IntArray(16)
    val memory = IntArray(0x10000000)
    var isHalted = false

    fun reset() {
        registers.fill(0)
        registers[STACK_POINTER] = 0xfffffff
        isHalted = false
    }

    init {
        reset()
    }

    /** Reads a value from memory. */
    fun read(address: Int): Int {
        val addr = address and 0x3FFFFFFF

        return when (addr and 0x30000000) {
            0 -> memory[addr]
            0x20000000 -> program[addr - 0x20000000]
            else -> TODO("unfinished memory map")
        }
    }

    /** Writes a value to memory. */
    fun write(address: Int, value: Int) {
        val addr = address and 0x3FFFFFFF

        when (addr and 0x30000000) {
            0 -> memory[addr] = value
            //0x20000000 -> program[addr - 0x20000000] = value
            else -> TODO("unfinished memory map")
        }
    }

    fun push(input: Int) {
        registers[STACK_POINTER]--
        write(registers[STACK_POINTER], input)
    }

    fun pop(): Int {
        val output = read(registers[STACK_POINTER])
        registers[STACK_POINTER]++
        return output
    }

    fun step() {
        if (isHalted) return
        instructionRegister = read(instructionPointer)
        instructionPointer++
        val instruction = Instruction(instructionRegister)
        println("$instruction @ ${instructionPointer.toHexString()}")

        val immediateValue: Int?
        if (instruction.isImmediate) {
            immediateValue = read(instructionPointer)
            instructionPointer++
        } else immediateValue = null
        when (instruction.opcode) {
            Opcode.HALT -> isHalted = true
            Opcode.CALL -> {
                push(instructionPointer)
                instructionPointer = immediateValue ?: registers[instruction.r1]
            }
            Opcode.RETURN -> {
                instructionPointer = pop()
            }
            Opcode.MOVE -> {
                when (instruction.addressingMode) {
                    0 -> registers[instruction.r1] = immediateValue!!
                    1 -> registers[instruction.r1] = registers[instruction.r2]
                    4 -> registers[instruction.r1] = read(registers[instruction.r2] + immediateValue!!)
                    5 -> write(immediateValue!!, registers[instruction.r2])
                    6 -> write(registers[instruction.r1], registers[instruction.r2])
                    7 -> write(registers[instruction.r1] + immediateValue!!, registers[instruction.r2])
                    else -> check(false) { "Unreachable code" }
                }
            }
            Opcode.PUSH -> {
                push(registers[instruction.r1])
            }
            Opcode.POP -> {
                registers[instruction.r1] = pop()
            }
            Opcode.OUTPUT -> {
                val value = immediateValue ?: registers[instruction.r1]
                println("OUT stub: portNumber = ${instruction.portAddress} value = $value")
                output(instruction.portAddress, value)
            }
            Opcode.INT_ADD -> registers[instruction.r1] += immediateValue ?: registers[instruction.r2]
            Opcode.INT_SUBTRACT -> registers[instruction.r1] -= immediateValue ?: registers[instruction.r2]
            Opcode.INT_DIVIDE -> registers[instruction.r1] /= immediateValue ?: registers[instruction.r2]
            else -> {
                throw RuntimeException("Unimplemented opcode ${instruction.opcode.name}")
            }
        }
    }

    /** Write to a slave device. */
    private fun output(address: Int, value: Int) {
        /*
        0b###_III_AAAAAAAA
        #: unused
        I: device ID
        A: device local address
         */

        val localAddress = address and 0xFF
        when (val deviceID = (address shr 8) and 0b111) {
            2 -> gpu.write(localAddress, value)
            3 -> println("SPU write stub")
            else -> TODO("Not yet implemented: $deviceID")
        }
    }

    fun runUntilHalt() {
        while (!isHalted) step()
    }

    override fun toString(): String {
        return "CPU // IP=0x${instructionPointer.toHexString(HexFormat.UpperCase)}"
    }
}