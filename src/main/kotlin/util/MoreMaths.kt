package util
const val BITS_PER_BYTE = 8

/**
 * Rounds an unsigned integer up to some multiple.
 *
 * @receiver the unsigned integer to apply this to.
 * @param m the multiple to round up to.
 * @return the result of rounding up.
 */
fun UInt.roundUpToMultipleOf(m: UInt): UInt {
    return ((this + m - 1U) / m) * m
}
