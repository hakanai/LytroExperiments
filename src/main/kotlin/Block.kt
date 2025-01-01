sealed class Block {
    data class WithData(
        val header: BlockHeader,
        val name: String,
        val dataOffset: Long,
    ) : Block()

    data class WithHeaderOnly(
        val header: BlockHeader
    ) : Block()
}
