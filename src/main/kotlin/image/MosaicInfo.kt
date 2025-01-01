package image

import metadata.enums.MosaicCell

data class MosaicInfo(
    val cell: MosaicCell,
    val xOffset: Int,
    val yOffset: Int,
    val pixelFormatBlack: Int,
    val pixelFormatWhite: Int,
)