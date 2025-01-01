package util

import okio.ByteString

/**
 * Quick hex dump utility for okio. Maybe it has one somewhere already, but I couldn't find it.
 *
 * @receiver a byte string.
 * @return the hex dump as a string.
 */
@OptIn(ExperimentalStdlibApi::class)
fun ByteString.hexDump(): String {
    val bytes = this
    return buildString {
        for (rowOffset in 0..<bytes.size step 16) {
            val rowBytes = (0..<16)
                .map { rowOffset + it }
                .filter { it < bytes.size }
                .map { bytes[it] }

            val hexColumn = rowBytes
                .map { b -> b.toHexString(HexFormat.UpperCase) }
                .joinToString(separator = " ")

            val asciiColumn = rowBytes
                .map { b ->
                    when (b) {
                        in 32..126 -> b.toInt().toChar()
                        else -> '.'
                    }
                }
                .joinToString(separator = "")

            append("0x")
            append(rowOffset.toHexString(HexFormat.UpperCase))
            append("  ")
            append(hexColumn.padEnd(48))
            append(" ")
            append(asciiColumn)
            append("\n")
        }
    }
}
