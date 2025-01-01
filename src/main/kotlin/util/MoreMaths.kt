package util
const val BITS_PER_BYTE = 8

fun UInt.roundUpToMultipleOf(m: UInt): UInt {
    return ((this + m - 1U) / m) * m
}

fun findLCM(a: Int, b: Int): Int {
    val larger = if (a > b) a else b
    val maxLcm = a * b
    var lcm = larger
    while (lcm <= maxLcm) {
        if (lcm % a == 0 && lcm % b == 0) {
            return lcm
        }
        lcm += larger
    }
    return maxLcm
}
