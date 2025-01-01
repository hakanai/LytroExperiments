
import image.demosaicImage
import image.readGreyscaleImage
import metadata.MainMetadata
import metadata.Metadata
import okio.Path.Companion.toPath
import java.io.File
import javax.imageio.ImageIO

fun main() {
    val path = "large-files/img00078.lfr".toPath()
    LightFieldFile.open(path).use { lightFieldFile ->
        val metadata = Metadata.readFrom(lightFieldFile)

        val imageBlock = lightFieldFile.findDataBlock(MainMetadata.FrameReferenceNames.IMAGE_REF)
        val imageBuffer = lightFieldFile.readData(imageBlock)
        val mosaicImage = readGreyscaleImage(imageBuffer, metadata)
        ImageIO.write(mosaicImage, "PNG", File("greyscale.png"))

        val colourImage = demosaicImage(mosaicImage, metadata)
        ImageIO.write(colourImage, "PNG", File("result.png"))
    }
}
