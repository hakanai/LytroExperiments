package image

import metadata.MosaicCell
import java.io.File
import javax.imageio.ImageIO

fun main() {
    // Test image was supposedly for testing OpenCV's `COLOR_BayerRG2RGB`, which is equivalent to the BGGR Bayer pattern
//    val input = ImageIO.read(File("data/demosaic_test_input.png"))
    val input = ImageIO.read(File("data/demosaic_sample_input.png"))

//    val result = demosaicImage(input, listOf(
//        MosaicInfo(MosaicCell.B, 0, 0, 64, 4096),
//        MosaicInfo(MosaicCell.GB, 1, 0, 64, 4096),
//        MosaicInfo(MosaicCell.GR, 0, 1, 64, 4096),
//        MosaicInfo(MosaicCell.R, 1, 1, 64, 4096),
//    ))
    val result = demosaicImage(input, listOf(
        MosaicInfo(MosaicCell.GR, 0, 0, 64, 4096),
        MosaicInfo(MosaicCell.R, 1, 0, 64, 4096),
        MosaicInfo(MosaicCell.B, 0, 1, 64, 4096),
        MosaicInfo(MosaicCell.GB, 1, 1, 64, 4096),
    ))

    ImageIO.write(result, "PNG", File("data/deleteme-result.png"))
}
