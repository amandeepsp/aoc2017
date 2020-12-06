import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.File
import java.util.*

data class Op(val opCode: String, val reg: String, val value: String = "")

fun isReg(s: String): Boolean = s[0] in 'a'..'z'

fun getValue(value: String, regmap: LongArray): Long = if (isReg(value)) {
    regmap[value[0] - 'a']
} else {
    value.toLong()
}

fun runMachine(opSequence: List<Op>): Long {
    val regmap = LongArray(26) { 0L }
    var programCounter = 0

    val sounds = mutableListOf<Long>()

    while (programCounter < opSequence.size) {
        val (opCode, reg, value) = opSequence[programCounter]
        when (opCode) {
            "snd" -> sounds.add(getValue(reg, regmap))
            "set" -> regmap[index(reg)] = getValue(value, regmap)
            "add" -> regmap[index(reg)] += getValue(value, regmap)
            "mul" -> regmap[index(reg)] *= getValue(value, regmap)
            "mod" -> regmap[index(reg)] %= getValue(value, regmap)
            "jgz" -> if (getValue(reg, regmap) > 0) programCounter += getValue(value, regmap).toInt() - 1
            "rcv" -> if (getValue(reg, regmap) != 0L) {
                return sounds.last()
            }
        }
        programCounter++
    }

    return -1
}

fun index(regString: String): Int = regString[0] - 'a'

@ExperimentalCoroutinesApi
suspend fun runMachineAsync(opSequence: List<Op>,
                            inChannel: Channel<Long>,
                            outChannel: Channel<Long>,
                            programId: Long): Int {

    val regmap = LongArray(26) { 0L }
    regmap['p' - 'a'] = programId
    var programCounter = 0

    var count = 0
    
    program@ while (programCounter < opSequence.size) {
        val (opCode, reg, value) = opSequence[programCounter]
        when (opCode) {
            "snd" -> {
                outChannel.send(getValue(reg, regmap))
                count++
            }
            "set" -> regmap[index(reg)] = getValue(value, regmap)
            "add" -> regmap[index(reg)] += getValue(value, regmap)
            "mul" -> regmap[index(reg)] *= getValue(value, regmap)
            "mod" -> regmap[index(reg)] %= getValue(value, regmap)
            "jgz" -> if (getValue(reg, regmap) > 0) programCounter += getValue(value, regmap).toInt() - 1
            "rcv" -> {
                try {
                    regmap[index(reg)] = withTimeout(1000) { inChannel.receive() }
                }catch (e: TimeoutCancellationException){ //Deadlock
                    break@program
                }
            }
        }
        programCounter++
    }
    return count
}

@ExperimentalCoroutinesApi
fun main() {
    val opSequence = File("resources/day18.txt")
        .readLines()
        .map {
            val splits = it.split(" ")
            Op(splits[0], splits[1], splits.getOrNull(2) ?: "")
        }

    println("Part1 : ${runMachine(opSequence)}")

    runBlocking {
        val channel1 = Channel<Long>(capacity = Channel.UNLIMITED)
        val channel2 = Channel<Long>(capacity = Channel.UNLIMITED)

        val firstRun = async { runMachineAsync(opSequence, channel1, channel2, 0) }
        val secondRun = async { runMachineAsync(opSequence, channel2, channel1, 1) }

        println("Part2 : ${secondRun.await()}")
    }

}