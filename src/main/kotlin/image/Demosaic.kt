package image

import metadata.Metadata
import metadata.enums.MosaicCell
import util.Direction
import util.IntOffset
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

/**
 * Removes Bayer filtering from an image, retrieving the full colour image from the greyscale.
 *
 * This method allows poking the mosaic values in directly, which is more convenient in
 * unit test code.
 *
 * @param input the input greyscale image.
 * @param mosaicValues a list of programmatically provided mosaic values.
 * @return the demosaiced image.
 */
fun demosaicImage(input: BufferedImage, mosaicValues: List<MosaicInfo>): BufferedImage {
    val mosaicCellsByModOffset = mosaicValues
        .associateBy { IntOffset(it.xOffset, it.yOffset) }
        .mapValues { (_, v) -> v.cell }

    val width = input.width
    val xRange = 0..<width
    val height = input.height
    val yRange = 0..<height

    val result = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    for (y in yRange) {
        for (x in xRange) {
            // Which mosaic cell are we pointing at?
            val cell = mosaicCellsByModOffset[IntOffset(x % 2, y % 2)]
                ?: throw IllegalStateException("Missing map entry!")

            fun sample(x: Int, y: Int) = input.getRGB(x, y).and(0xFF)

            fun currentPixel() = sample(x, y)

            fun averageOfNeighbours(directions: List<Direction>): Int {
                val v = directions.mapNotNull { direction ->
                    val dx = direction.offset.x
                    val dy = direction.offset.y
                    val x1 = x + dx
                    val y1 = y + dy
                    if (x1 in xRange && y1 in yRange) {
                        sample(x1, y1)
                    } else {
                        null
                    }
                }
                check(v.isNotEmpty()) { "No neighbours were in range somehow. x = $x, y = $y, directions = $directions" }
                return v.average().roundToInt()
            }

            val r: Int
            val g: Int
            val b: Int

            when (cell) {
                MosaicCell.R -> {
                    r = currentPixel()
                    g = averageOfNeighbours(Direction.Orthogonal)
                    b = averageOfNeighbours(Direction.Diagonal)
                }

                MosaicCell.GR -> {
                    r = averageOfNeighbours(Direction.Horizontal)
                    g = currentPixel()
                    b = averageOfNeighbours(Direction.Vertical)
                }

                MosaicCell.GB -> {
                    r = averageOfNeighbours(Direction.Vertical)
                    g = currentPixel()
                    b = averageOfNeighbours(Direction.Horizontal)
                }

                MosaicCell.B -> {
                    r = averageOfNeighbours(Direction.Diagonal)
                    g = averageOfNeighbours(Direction.Orthogonal)
                    b = currentPixel()
                }
            }

            assert(r in 0..255 && g in 0..255 && b in 0..255)

            result.setRGB(x, y, 0xFF000000.toInt().or(r.shl(16)).or(g.shl(8)).or(b))
        }
    }

    return result
}

/**
 * Removes Bayer filtering from an image, retrieving the full colour image from the greyscale.
 *
 * @param input the input greyscale image.
 * @param metadata the Lytro image metadata
 * @return the demosaiced image.
 */
fun demosaicImage(input: BufferedImage, metadata: Metadata): BufferedImage {
    val pixelFormatBlack = metadata.image.pixelFormat.black
    val pixelFormatWhite = metadata.image.pixelFormat.white
    val mosaicCells = metadata.image.mosaic.asCells()
    val mosaicValues = mosaicCells.flatMapIndexed { yOffset, row ->
        row.mapIndexed { xOffset, cell ->
            MosaicInfo(
                cell = cell,
                xOffset = xOffset,
                yOffset = yOffset,
                pixelFormatBlack = pixelFormatBlack[cell],
                pixelFormatWhite = pixelFormatWhite[cell],
            )
        }
    }

    return demosaicImage(input, mosaicValues)
}
