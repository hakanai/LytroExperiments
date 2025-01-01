package metadata

import LightFieldFile
import kotlinx.serialization.json.Json

data class ReconstructionFilterMetadata(
    val targetWidth: Int,
    val targetHeight: Int,
    val numKernelsX: Int,
    val numKernelsY: Int,
    val cameraModel: String,
    val zoomStep: Int,
    val focusStep: Int,
    val imageFile: String,
    val lambdas: List<Double>,
) {
    companion object {
        fun readFrom(lightFieldFile: LightFieldFile): ReconstructionFilterMetadata {
            val metadataBlock =
                lightFieldFile.findDataBlock(MainMetadata.FrameReferenceNames.RECONSTRUCTION_FILTER_METADATA_REF)
            val json = lightFieldFile.readData(metadataBlock).readUtf8()
//            println("Raw JSON of metadata: $json")
            return Json.decodeFromString(json)
        }
    }
}
