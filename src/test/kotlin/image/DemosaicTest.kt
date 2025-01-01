package image

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import metadata.enums.MosaicCell
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class DemosaicTest : FreeSpec({
    fun loadImage(resourcePath: String): BufferedImage {
        return FileSystem.RESOURCES.source(resourcePath.toPath()).buffer().inputStream().use {
            ImageIO.read(it)
        }
    }

    "demosaicing an example image" {
        val input = loadImage("demosaic_test_input.png")
        val expected = loadImage("demosaic_test_expected.png")

        // Test image was supposedly for testing OpenCV's `COLOR_BayerRG2RGB`,
        // which is equivalent to the BGGR Bayer pattern, which we hard-code here:
        val actual = demosaicImage(input, listOf(
            MosaicInfo(MosaicCell.B, 0, 0, 64, 4096),
            MosaicInfo(MosaicCell.GB, 1, 0, 64, 4096),
            MosaicInfo(MosaicCell.GR, 0, 1, 64, 4096),
            MosaicInfo(MosaicCell.R, 1, 1, 64, 4096),
        ))

        try {
            actual.width shouldBe expected.width
            actual.height shouldBe expected.height
            actual.type shouldBe BufferedImage.TYPE_INT_RGB

            for (y in 0..<actual.height) {
                for (x in 0..<actual.width) {
                    actual.getRGB(x, y) shouldBe expected.getRGB(x, y)
                }
            }
        } catch (e: Exception) {
            ImageIO.write(actual, "PNG", File("actual-DemosaicTest.png"))
            throw e
        }
    }
})
