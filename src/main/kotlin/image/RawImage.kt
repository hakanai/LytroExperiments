package image

import metadata.Metadata
import okio.Buffer
import util.BITS_PER_BYTE
import util.BitUnpacker
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.awt.image.ComponentColorModel
import java.awt.image.DataBuffer
import java.awt.image.DataBufferUShort
import java.awt.image.PixelInterleavedSampleModel
import java.awt.image.Raster
import java.awt.image.SampleModel
import java.nio.ShortBuffer
import java.util.Properties

/**
 * Reads the raw greyscale image from the provided buffer.
 *
 * @param imageBuffer a buffer containing the image data.
 * @param metadata the image metadata.
 * @return the image.
 */
fun readGreyscaleImage(imageBuffer: Buffer, metadata: Metadata): BufferedImage {
    val imageWidth = metadata.image.width
    val imageHeight = metadata.image.height
    val bitsPerPixel = metadata.image.pixelPacking.bitsPerPixel
    // println("Reading greyscale image of width $imageWidth, height $imageHeight, bits per pixel $bitsPerPixel")

    require(imageBuffer.size == imageWidth.toLong() * imageHeight * bitsPerPixel / BITS_PER_BYTE)

    val sampleModel: SampleModel = PixelInterleavedSampleModel(
        DataBuffer.TYPE_USHORT, imageWidth, imageHeight, 1, imageWidth, intArrayOf(0)
    )
    val colorModel = ComponentColorModel(
        ColorSpace.getInstance(ColorSpace.CS_GRAY),
        intArrayOf(16), false, true, Transparency.OPAQUE, DataBuffer.TYPE_USHORT
    )
    val dataBuffer = DataBufferUShort(imageWidth * imageHeight)
    val dataBufferAsBuffer = ShortBuffer.wrap(dataBuffer.data)
    val raster = Raster.createWritableRaster(sampleModel, dataBuffer, null)

    // Unfortunately setting the ColorModel itself to 10 bits per pixel doesn't change behaviour,
    // so we have set it to 16 and are going to shift the values ourselves. :(
    val bitShift = 16 - bitsPerPixel

    val unpackHelper = BitUnpacker.create(
        bitsPerValue = bitsPerPixel,
        endianness = metadata.image.pixelPacking.endianness,
    )
    val packedBlock = ByteArray(unpackHelper.packedBlockSize)
    var unpackedBlock = IntArray(unpackHelper.unpackedBlockSize)

    while (!imageBuffer.exhausted()) {
        imageBuffer.readFully(packedBlock)
        unpackHelper.unpack(packedBlock, unpackedBlock)
        unpackedBlock.forEach { value ->
            val sample = value.shl(bitShift).toShort()
            dataBufferAsBuffer.put(sample)
        }
    }
    assert(!dataBufferAsBuffer.hasRemaining())

    // Wrap Raster as a BufferedImage
    return BufferedImage(colorModel, raster, false, Properties())
}
