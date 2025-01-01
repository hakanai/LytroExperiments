package image

import LightFieldFile
import kotlinx.serialization.json.Json
import metadata.MainMetadata
import metadata.Metadata

class ImageBlock {
    companion object {
        fun readFrom(lightFieldFile: LightFieldFile): ImageBlock {
            val imageBlock = lightFieldFile.findDataBlock(MainMetadata.FrameReferenceNames.IMAGE_REF)
            val imageBuffer = lightFieldFile.readData(imageBlock)

        }
    }

}
