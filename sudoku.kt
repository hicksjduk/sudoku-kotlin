import kotlin.math.*

typealias Grid = Array<IntArray>
data class Square (val row: Int, val col: Int)
data class Box (val rows: IntRange, val cols: IntRange)

val permittedValues = (1..9).toList()
val emptySquare = 0
val gridSize = permittedValues.size
val boxes = {validateParameters(); calcBoxes()}()

fun validateParameters() {
    if (gridSize == 0)
        throw Exception("No permitted values specified")
    if (hasDuplicates(permittedValues))
        throw Exception("Permitted values include duplicates")
    if (emptySquare in permittedValues)
        throw Exception("Permitted values include the empty square value")
}

fun boxSize(): Pair<Int, Int> {
    val squareRoot = sqrt(gridSize.toFloat())
    val rows = (squareRoot.roundToInt() downTo 1).first {gridSize % it == 0}
    return Pair(rows, gridSize / rows)
}

fun calcBoxes(): List<Box> = boxSize().let {(rowsPerBox, colsPerBox) ->
    fun ranges(perBox: Int): List<IntRange> = 
        (0 until gridSize step perBox).map {it..(it + perBox - 1)}
    val rowRanges = ranges(rowsPerBox)
    val colRanges = ranges(colsPerBox)
    return rowRanges.flatMap {rows -> colRanges.map {cols -> Box(rows, cols)}}
}

fun main() = println(puzzle.sudoku().firstOrNull()?.asString() ?: "No solution found")

fun Grid.sudoku(): Sequence<Grid> {
    validate()
    return solve()
}

fun Grid.validate() {
    if (size != gridSize)
        throw Exception("Wrong number of rows")
    if (any {it.size != gridSize})
        throw Exception("Wrong number of columns")
    val rows = (0 until gridSize).map(this::rowValues)
    if (rows.any {r -> r.any {it !in permittedValues}})
        throw Exception("Unsupported value")
    if (rows.any(::hasDuplicates))
        throw Exception("Row has duplicate values")
    if ((0 until gridSize).map(this::colValues).any(::hasDuplicates))
        throw Exception("Column has duplicate values")
    if (boxes.map(this::boxValues).any(::hasDuplicates))
        throw Exception ("Box has duplicate values")
}

fun hasDuplicates(values: List<Int>): Boolean = 
    values.size != values.distinct().size

fun Grid.solve(): Sequence<Grid> =
    emptySquares().firstOrNull()?.let(this::solveAt) ?: sequenceOf(this) 

fun Grid.emptySquares(): Sequence<Square> =
    asSequence().flatMapIndexed {row, values ->
        values.asSequence().mapIndexedNotNull {col, value ->
            if (value == emptySquare) Square(row, col) else null
        }
    }

fun Grid.solveAt(square: Square): Sequence<Grid> = 
    allowedValues(square).asSequence()
        .map {setValueAt(square, it)}
        .flatMap(Grid::solve)

fun Grid.allowedValues(square: Square): List<Int> = 
    permittedValues - 
        rowValues(square.row) - 
        colValues(square.col) -
        boxValues(boxContaining(square))

fun Grid.setValueAt(square: Square, value: Int): Grid {
    val newRow = intArrayOf(*this[square.row])
    newRow[square.col] = value
    val answer = arrayOf(*this)
    answer[square.row] = newRow
    return answer
}
    
fun notEmpty(value: Int): Boolean = value != emptySquare

fun Grid.rowValues(row: Int): List<Int> = this[row].filter(::notEmpty)

fun Grid.colValues(col: Int): List<Int> = this.map {it[col]}.filter(::notEmpty)

fun Grid.boxValues(box: Box): List<Int> = 
    slice(box.rows).flatMap {it.slice(box.cols)}.filter(::notEmpty)

fun boxContaining(square: Square): Box = 
    boxes.first {square.row in it.rows && square.col in it.cols}

fun Grid.asString(): String = 
    map {it.map {it.toString()}.joinToString(" ")}.joinToString("\n")

val puzzle: Grid = arrayOf(
    intArrayOf(8,0,0,0,0,0,0,0,0),
    intArrayOf(0,0,3,6,0,0,0,0,0),
    intArrayOf(0,7,0,0,9,0,2,0,0),
    intArrayOf(0,5,0,0,0,7,0,0,0),
    intArrayOf(0,0,0,0,4,5,7,0,0),
    intArrayOf(0,0,0,1,0,0,0,3,0),
    intArrayOf(0,0,1,0,0,0,0,6,8),
    intArrayOf(0,0,8,5,0,0,0,1,0),
    intArrayOf(0,9,0,0,0,0,4,0,0)
)