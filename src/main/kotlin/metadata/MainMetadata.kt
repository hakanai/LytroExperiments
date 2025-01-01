import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class MainMetadata(
    val derivations: List<String>,
    val views: List<String>,
    val schema: String,
    val annotations: List<String>,

) {

    /*
{
    "derivations": [
        "sha1-7323df2b9028ce1732c22e0758db71d4af1fc4f1"
    ],
    "views": [],
    "schema": "http://schema.lytro.com/lfp/picture/2.1.4/picture_schema.json",
    "annotations": [],
    "thumbnails": [
        {
            "width": 704,
            "imageRef": "sha1-4020b3c30a9bd523f2432be4035fb16c47773a11",
            "colorSpace": "sRGB",
            "representation": "jpeg",
            "height": 480
        }
    ],
    "frames": [
        {
            "assignedProperties": {},
            "accelerations": [],
            "frame": {
                "aberrationCorrectionRef": "sha1-76b6ceafb7e0b95e851f7f657af068707abaed1c",
                "hotPixelRef": "sha1-cfde3e9a4534028a15da8cbb19d3d2f8d83ab81a",
                "exposureHistogramRef": "sha1-8b6b4edc3066f89be3b929cc143ce764b817acd4",
                "aberrationCorrectionMetadataRef": "sha1-a7c1177c0326ef241f1386d4268e687093632c01",
                "geometryCorrectionRef": "sha1-7b86e118c855aa8a7bea2b00719fdf70ccea29e2",
                "reconstructionFilterRef": "sha1-b14b28ba303e4a1bf2e15f8f24d31096e5ffcc0d",
                "reconstructionFilterMetadataRef": "sha1-00ff6f82ece5e55e19cba35ce4d2cf5dcb22e43d",
                "metadataRef": "sha1-8c4bd8815d409eac482373ac74018927b8b2b474",
                "privateMetadataRef": "sha1-26e98568d267734640472d1ee0264b6fd9b3555a",
                "imageRef": "sha1-2bf6a27b1049261b98845cee317ed369703981d3"
            }
        }
    ],
    "generators": [
        "lightning"
    ]
}
     */

    companion object {
        fun readFrom(lightFieldFile: LightFieldFile): MainMetadata {
            val block = lightFieldFile.blocks
                .filterIsInstance<Block.WithData>()
                .find { block -> block.header.magic == METADATA_BLOCK_MAGIC }
                ?: throw IllegalStateException("Could not find main metadata block!")
            val json = lightFieldFile.readData(block).readUtf8()
            println("Raw JSON of main metadata:")
            println(json)
            return Json.decodeFromString<MainMetadata>(json)
        }
    }
}