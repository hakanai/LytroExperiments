import metadata.MainMetadata
import okio.Buffer
import okio.Closeable
import okio.FileHandle
import okio.FileSystem
import okio.Path
import okio.buffer
import util.readNullTerminatedUtf8
import util.roundUpToMultipleOf

/**
 * Represents a single Lytro light field file.
 */
class LightFieldFile private constructor(private val fileHandle: FileHandle): Closeable {

    /**
     * All blocks found in the file.
     */
    val blocks: List<Block> by lazy {
        val source = fileHandle.source().buffer()
        val blockHeaderBuffer = Buffer()
        val nameBuffer = Buffer()
        buildList {
            while (!source.exhausted()) {
                blockHeaderBuffer.clear()
                source.readFully(blockHeaderBuffer, 16)
                val blockHeader = BlockHeader.readFrom(blockHeaderBuffer)

                if (blockHeader.magic == BlockHeader.Magic.PACKAGE) {
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
     * The main metadata for the file.
     */
    val mainMetadata by lazy { MainMetadata.readFrom(this) }

    /**
     * Finds a data block by its reference name.
     *
     * @param referenceName the reference name to look up. See [MainMetadata.FrameReferenceNames] for known values.
     * @return the data block.
     * @throws IllegalStateException if the block is missing. XXX: Not sure whether this is an IAE or an ISE, really.
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
        /**
         * Opens the light field file from the given path.
         *
         * @param path the path to an LFP file.
         * @return the [LightFieldFile]. Generally you should call [use] on it to ensure it is
         *         safely closed.
         */
        fun open(path: Path) = LightFieldFile(FileSystem.SYSTEM.openReadOnly(path))
    }
}
