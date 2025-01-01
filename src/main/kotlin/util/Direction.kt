package util

/**
 * Enumeration of orthogonal and diagonal directions.
 */
enum class Direction(val offset: IntOffset) {
    LEFT(IntOffset(-1, 0)),
    RIGHT(IntOffset(1, 0)),
    UP(IntOffset(0, -1)),
    DOWN(IntOffset(0, 1)),
    UP_LEFT(IntOffset(-1, -1)),
    UP_RIGHT(IntOffset(1, -1)),
    DOWN_LEFT(IntOffset(-1, 1)),
    DOWN_RIGHT(IntOffset(1, 1)),
    ;

    companion object {
        val Horizontal get() = listOf(LEFT, RIGHT)
        val Vertical get() = listOf(UP, DOWN)
        val Orthogonal get() = Horizontal + Vertical
        val Diagonal get() = listOf(UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT)
    }
}
