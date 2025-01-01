package metadata.types

import kotlinx.serialization.Serializable
import metadata.enums.MosaicCell

/**
 * Mapping of mosaic cells to double values.
 */
@Serializable
data class CellDoubleMapping(
    val r: Double,
    val gr: Double,
    val gb: Double,
    val b: Double,
) {
    // Pseudo-map accessor
    operator fun get(cell: MosaicCell) = when (cell) {
        MosaicCell.R -> r
        MosaicCell.GR -> gr
        MosaicCell.GB -> gr
        MosaicCell.B -> b
    }
}