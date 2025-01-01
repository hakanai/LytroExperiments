package util

import okio.Buffer

/**
 * Finds the next null byte in the buffer.
 *
 * @receiver the buffer.
 * @return the offset of the next null byte, or `null` if not found.
 */
private fun Buffer.findNullTerminator(): Long? {
    return this.indexOf(0).takeIf { it >= 0 }
}

/**
 * Reads null-terminated UTF-8 from the buffer.
 *
 * @receiver the buffer.
 * @param maxLength the maximum length of data to use.
 * @return the string up to the first null byte. If no null byte is found, returns
 *         the entire string up to the specified max length.
 */
fun Buffer.readNullTerminatedUtf8(maxLength: Long = size): String {
    val nameLength = findNullTerminator() ?: maxLength
    return readUtf8(nameLength)
}
