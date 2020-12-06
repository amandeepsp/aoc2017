import java.io.File

fun main() {
    File("resources/day21.txt").readLines().map {
        println(it.split(" => ").map { iter ->
            iter.split("/")
        })
    }
}