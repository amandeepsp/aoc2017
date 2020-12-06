import java.io.File
import kotlin.math.abs
import kotlin.math.sqrt

data class Vec3(val x: Int, val y: Int, val z: Int) : Comparable<Vec3> {
    operator fun plus(other: Vec3) = Vec3(x + other.x, y + other.y, z + other.z)
    operator fun times(scalar: Int) = Vec3(scalar * x, scalar * y, scalar * z)
    operator fun div(scalar: Int) = Vec3(x / scalar, y / scalar, z / scalar)
    fun norm() = x * x + y * y + z * z
    fun dot(other: Vec3) = x * other.x + y * other.y + z * other.z
    override fun compareTo(other: Vec3): Int = norm().compareTo(other.norm())
}

data class Particle(val id: Int, val position: Vec3, val velocity: Vec3, val acceleration: Vec3) {

    fun isOscillating() = velocity.dot(acceleration) == 0
            && velocity != Vec3(0, 0, 0)
            && acceleration != Vec3(0, 0, 0)

    fun tick(): Particle{
        val newVelocity = velocity + acceleration
        val newPosition = position + velocity + acceleration
        return copy(position = newPosition, velocity = newVelocity)
    }
}

private fun List<Particle>.withOutCollisions(): List<Particle> {
    val out = mutableListOf<Particle>()
    this.forEach { comp ->
        if (this.filter {it != comp}.none {it.position == comp.position}) {
            out.add(comp)
        }
    }
    return out
}

fun main() {
    val lineRegex =
        "^p=<(-?\\d+),(-?\\d+),(-?\\d+)>, v=<(-?\\d+),(-?\\d+),(-?\\d+)>, a=<(-?\\d+),(-?\\d+),(-?\\d+)>$".toRegex()
    val particles = File("resources/day20.txt").readLines().mapIndexed { index, line ->
        val matchValues = lineRegex.matchEntire(line)?.groupValues?.drop(1) ?: listOf()
        val position = Vec3(matchValues[0].toInt(), matchValues[1].toInt(), matchValues[2].toInt())
        val velocity = Vec3(matchValues[3].toInt(), matchValues[4].toInt(), matchValues[5].toInt())
        val acceleration = Vec3(matchValues[6].toInt(), matchValues[7].toInt(), matchValues[8].toInt())
        Particle(index, position, velocity, acceleration)
    }

    println("Min a: ${particles.minByOrNull { it.acceleration }}}")

    var currentParticles = particles
    repeat(100){
        currentParticles = currentParticles.withOutCollisions().map { it.tick() }
        println(currentParticles.size)
    }
}