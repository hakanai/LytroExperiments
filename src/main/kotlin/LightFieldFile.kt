import metadata.MainMetadata
import okio.Buffer
import okio.Closeable
import okio.FileHandle
import okio.FileSystem
import okio.Path
import okio.buffer
import util.readNullTerminatedUtf8
import util.roundUpToMultipleOf

class LightFieldFile(private val fileHandle: FileHandle): Closeable {
    val blocks: List<Block> = readBlocks()

    val mainMetadata = MainMetadata.readFrom(this)

    private fun readBlocks(): List<Block> {
        val source = fileHandle.source().buffer()
        val blockHeaderBuffer = Buffer()
        val nameBuffer = Buffer()
        return buildList {
            while (!source.exhausted()) {
                blockHeaderBuffer.clear()
                source.readFully(blockHeaderBuffer, 16)
                val blockHeader = BlockHeader.readFrom(blockHeaderBuffer)

                if (blockHeader.magic == BlockMagic.PACKAGE) {
                    add(Block.WithHeaderOnly(blockHeader))
                } else {
                    nameBuffer.clear()
                    source.readFully(nameBuffer, 80)
                    val name = nameBuffer.readNullTerminatedUtf8()

                    val dataOffset = fileHandle.position(source)

                    // Block headers are always aligned to a multiple of 16 bytes.
                    val paddedDataLength = blockHeader.dataLength.roundUpToMultipleOf(16U).toLong()
                    source.skip(paddedDataLength)

                    add(Block.WithData(blockHeader, name, dataOffset))
                }
            }
        }
    }

    /**
     * Finds a data block by its reference name.
     *
     * @param referenceName the reference name to look up. See [MainMetadata.FrameReferenceNames] for known values.
     * @return the data block.
     * @throws IllegalStateException if the block is missing.
     */
    fun findDataBlock(referenceName: String): Block.WithData {
        val blockName = mainMetadata.frames[0].frame[referenceName]
            ?: throw IllegalStateException("Missing reference?")

        return blocks
            .filterIsInstance<Block.WithData>()
            .find { block -> block.name == blockName }
            ?: throw IllegalStateException("Missing block?")
    }

    /**
     * Reads data for a single block.
     *
     * @param block the block.
     * @return the data for that block, in a buffer.
     */
    fun readData(block: Block.WithData): Buffer {
        val source = fileHandle.source(fileOffset = block.dataOffset).buffer()
        val buffer = Buffer()
        source.readFully(buffer, block.header.dataLength.toLong())
        return buffer
    }

    override fun close() {
        fileHandle.close()
    }

    companion object {
        fun open(path: Path) = LightFieldFile(FileSystem.SYSTEM.openReadOnly(path))
    }
}
