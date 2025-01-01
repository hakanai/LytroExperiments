package metadata

import LightFieldFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PrivateMetadata(
    val schema: String,
    val generator: String,
    val camera: Camera,
    val devices: Devices,
) {
    companion object {
        fun readFrom(lightFieldFile: LightFieldFile): PrivateMetadata {
            val metadataBlock = lightFieldFile.findDataBlock(MainMetadata.FrameReferenceNames.PRIVATE_METADATA_REF)
            val json = lightFieldFile.readData(metadataBlock).readUtf8()
//            println("Raw JSON of private metadata: $json")
            return Json.decodeFromString(json)
        }
    }

    @Serializable
    data class Camera(
        val serialNumber: String,
    )

    @Serializable
    data class Devices(
        val sensor: Sensor,
    ) {
        @Serializable
        data class Sensor(
            val serialNumber: String,
        )
    }
}