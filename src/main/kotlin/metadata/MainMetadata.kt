package metadata

import Block
import BlockHeader
import LightFieldFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Holds the main metadata for a light field file.
 *
 * @property derivations ???
 * @property views ???
 * @property schema ???
 * @property annotations ???
 * @property thumbnails metadata for each of the stored thumbnails.
 * @property frames metadata for each of the stored frames.
 * @property generators ???
 */
@Serializable
data class MainMetadata(
    val derivations: List<String>,
    val views: List<String>,
    val schema: String,
    val annotations: List<String>,
    val thumbnails: List<Thumbnail>,
    val frames: List<Frame>,
    val generators: List<String>,

) {
    companion object {
        fun readFrom(lightFieldFile: LightFieldFile): MainMetadata {
            val block = lightFieldFile.blocks
                .filterIsInstance<Block.WithData>()
                .find { block -> block.header.magic == BlockHeader.Magic.METADATA }
                ?: throw IllegalStateException("Could not find main metadata block!")
            val json = lightFieldFile.readData(block).readUtf8()
//            println("Raw JSON of main metadata: $json")
            return Json.decodeFromString<MainMetadata>(json)
        }
    }

    /**
     * Holds metadata for a single thumbnail.
     *
     * @property width width of the thumbnail, in pixels.
     * @property height height of the thumbnail, in pixels.
     * @property imageRef name of the file data block containing the thumbnail data.
     * @property colorSpace the color space used to store the thumbnail.
     * @property representation the image format used to store the thumbnail.
     */
    @Serializable
    data class Thumbnail(
        val width: Int,
        val height: Int,
        val imageRef: String,
        // TODO: Enum? Have only seen "sRGB"
        val colorSpace: String,
        // TODO: Enum? Have only seen "jpeg"
        val representation: String,
    )

    /**
     * Holds metadata for a single frame.
     *
     * @property assignedProperties ???
     * @property accelerations ???
     * @property frame a map which maps the reference type to the name of the file block
     *           containing that data. See [FrameReferenceNames] for the known names.
     */
    @Serializable
    data class Frame(
        val assignedProperties: Map<String, String>,
        val accelerations: List<String>,
        val frame: Map<String, String>,
    )

    // TODO: Can we make this an enum? Requires some custom serialisation.
    object FrameReferenceNames {
        const val METADATA_REF = "metadataRef"
        const val PRIVATE_METADATA_REF = "privateMetadataRef"
        const val IMAGE_REF = "imageRef"
        const val ABERRATION_CORRECTION_REF = "aberrationCorrectionRef"
        const val ABERRATION_CORRECTION_METADATA_REF = "aberrationCorrectionMetadataRef"
        const val HOT_PIXEL_REF = "hotPixelRef"
        const val EXPOSURE_HISTOGRAM_REF = "exposureHistogramRef"
        const val GEOMETRY_CORRECTION_REF = "geometryCorrectionRef"
        const val RECONSTRUCTION_FILTER_REF = "reconstructionFilterRef"
        const val RECONSTRUCTION_FILTER_METADATA_REF = "reconstructionFilterMetadataRef"
    }
}