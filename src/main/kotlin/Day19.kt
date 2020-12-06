import java.io.File

enum class Direction {
    UP, DOWN, RIGHT, LEFT
}

fun main() {

    val map = File("resources/day19.txt").readLines()

    fun isValid(x: Int, y: Int): Boolean = x in map[0].indices && y in map.indices

    var x = map[0].indexOfFirst { it == '|' }
    var y = 0
    var direction = Direction.DOWN
    var steps = 0

    val letters = StringBuilder()

    var currentChar = '|'
    while (!currentChar.isWhitespace()){
        steps++
        when(direction){
            Direction.DOWN -> y++
            Direction.UP -> y--
            Direction.LEFT -> x--
            Direction.RIGHT -> x++
        }

        currentChar = map[y][x]
        println("$y $x $direction $currentChar")

        if(currentChar == '+'){
            direction = when(direction){
                Direction.DOWN, Direction.UP ->
                    if(!map[y][x-1].isWhitespace() && isValid(x - 1, y)) Direction.LEFT else Direction.RIGHT
                Direction.LEFT, Direction.RIGHT ->
                    if(!map[y-1][x].isWhitespace() && isValid(x, y - 1)) Direction.UP else Direction.DOWN
            }
        }else if(currentChar !in setOf('|', '-')){
            letters.append(currentChar)
        }
    }

    println(letters.toString())
    println(steps)

}