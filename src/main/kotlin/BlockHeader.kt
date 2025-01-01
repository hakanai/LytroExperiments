import okio.Buffer

data class BlockHeader(
    val magic: ULong,
    val version: UInt,
    val dataLength: UInt,
) {
    companion object {
        fun readFrom(buffer: Buffer): BlockHeader {
            val magic = buffer.readLong().toULong()
            val version = buffer.readInt().toUInt()
            val dataLength = buffer.readInt().toUInt()
            return BlockHeader(magic, version, dataLength)
        }
    }
}
