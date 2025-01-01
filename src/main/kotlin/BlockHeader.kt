import BlockHeader.Magic
import okio.Buffer

/**
 * The header found on all blocks in a [LightFieldFile].
 *
 * @property magic the magic number for the block. See [Magic] for the known values.
 * @property version the version of the block format.
 * @property dataLength the length of data for the block, if any.
 */
data class BlockHeader(
    val magic: ULong,
    val version: UInt,
    val dataLength: UInt,
) {
    companion object {
        /**
         * Reads the block header from a buffer.
         *
         * @param buffer the buffer.
         * @return the block header.
         */
        fun readFrom(buffer: Buffer): BlockHeader {
            val magic = buffer.readLong().toULong()
            val version = buffer.readInt().toUInt()
            val dataLength = buffer.readInt().toUInt()
            return BlockHeader(magic, version, dataLength)
        }
    }

    /**
     * Known magic numbers seen on blocks.
     */
    object Magic {
        /**
         * Magic number for a package block.
         * Seems to occur once, at the start of the file.
         * 0x89 'LFP' 0xD 0xA 0x1A 0xA
         */
        val PACKAGE = 0x894C46500D0A1A0AUL

        /**
         * Magic number for a metadata block.
         * Seems to occur once in the file?
         * Although the location of it appears to vary.
         * 0x89 'LFM' 0xD 0xA 0x1A 0xA
         */
        val METADATA = 0x894C464D0D0A1A0AUL

        /**
         * Magic number for a "component" block.
         * Seems to be the fallback for when it isn't any of the other block types.
         * 0x89 'LFC' 0xD 0xA 0x1A 0xA
         */
        val COMPONENT = 0x894C46430D0A1A0AUL
    }
}
