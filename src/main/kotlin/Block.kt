/**
 * Blocks inside a [LightFieldFile].
 */
sealed class Block {

    /**
     * A block which includes some data.
     *
     * @property header the block header. (Most usefully, contains the block's length.)
     * @property name the name of the block.
     * @property dataOffset the offset of the data from the start of the file.
     */
    data class WithData(
        val header: BlockHeader,
        val name: String,
        val dataOffset: Long,
    ) : Block()

    /**
     * A block which includes no data. These blocks also have no name.
     *
     * @property header the block header.
     */
    data class WithHeaderOnly(
        val header: BlockHeader
    ) : Block()
}
