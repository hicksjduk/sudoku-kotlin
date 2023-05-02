import kotlin.math.*

typealias Grid = Array<IntArray>
typealias Square = Pair<Int, Int>
typealias Box = Pair<IntRange, IntRange>

val permittedValues = (1..9).toList()
val emptySquare = 0
val gridSize = permittedValues.size
val boxes = calcBoxes()

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

fun calcBoxes(): List<Box> {
    validateParameters()
    val (rowsPerBox, colsPerBox) = boxSize()
    return (0 until gridSize step rowsPerBox).flatMap {topRow -> 
        val bottomRow = topRow + rowsPerBox - 1
        (0 until gridSize step colsPerBox).map {leftCol ->
            val rightCol = leftCol + colsPerBox - 1
            Box(topRow..bottomRow, leftCol..rightCol)
        }
    }
}

fun main() = println(sudoku(puzzle).firstOrNull()?.asString() ?: "No solution found")

fun sudoku(grid: Grid): Sequence<Grid> {
    validate(grid)
    return solve(grid)
}

fun validate(grid: Grid) {
    if (grid.size != gridSize)
        throw Exception("Wrong number of rows")
    if (grid.any {it.size != gridSize})
        throw Exception("Wrong number of columns")
    val rows = (0 until gridSize).map {rowValues(grid, it)}
    if (rows.any {r -> r.any {it !in permittedValues}})
        throw Exception("Unsupported value")
    if (rows.any(::hasDuplicates))
        throw Exception("Row has duplicate values")
    if ((0 until gridSize).map {colValues(grid, it)}.any(::hasDuplicates))
        throw Exception("Column has duplicate values")
    if (boxes.map {boxValues(grid, it)}.any(::hasDuplicates))
        throw Exception ("Box has duplicate values")
}

fun hasDuplicates(values: List<Int>) = values.size != values.distinct().size

fun solve(grid: Grid): Sequence<Grid> {
    val square = emptySquares(grid).firstOrNull()
    return if (square == null) 
        sequenceOf(grid) 
    else 
        solveAt(grid, square)
}

fun emptySquares(grid: Grid) =
    grid.asSequence().flatMapIndexed {row, values ->
        values.asSequence().mapIndexedNotNull {col, value ->
            if (value == emptySquare) Square(row, col) else null
        }
    }

fun solveAt(grid: Grid, square: Square) = allowedValues(grid, square).asSequence()
    .map {setValueAt(grid, square, it)}
    .flatMap {solve(it)}

fun allowedValues(grid: Grid, square: Square) = square.let {(row, col) ->
    permittedValues - 
        rowValues(grid, row) - 
        colValues(grid, col) -
        boxValues(grid, boxContaining(square))
}

fun setValueAt(grid: Grid, square: Square, value: Int): Grid = square.let {(row, col) ->
    val newRow = intArrayOf(*grid[row])
    newRow[col] = value
    val answer = arrayOf(*grid)
    answer[row] = newRow
    return answer
}

fun notEmpty(value: Int) = value != emptySquare

fun rowValues(grid: Grid, row: Int) = grid[row].filter(::notEmpty)

fun colValues(grid: Grid, col: Int) = grid.map {it[col]}.filter(::notEmpty)

fun boxValues(grid: Grid, box: Box) = box.let {(rows, cols) ->
    grid.slice(rows).flatMap {it.slice(cols)}.filter(::notEmpty)
}

fun boxContaining(square: Square) = square.let {(row, col) ->
    boxes.first {(rows, cols) -> row in rows && col in cols}
}

fun Grid.asString() = this.map {it.map {it.toString()}.joinToString(" ")}.joinToString("\n")

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