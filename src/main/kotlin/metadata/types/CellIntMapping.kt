package metadata.types

import kotlinx.serialization.Serializable
import metadata.enums.MosaicCell

/**
 * Mapping of mosaic cells to int values.
 */
@Serializable
data class CellIntMapping(
    val r: Int,
    val gr: Int,
    val gb: Int,
    val b: Int,
) {
    // Pseudo-map accessor
    operator fun get(cell: MosaicCell) = when (cell) {
        MosaicCell.R -> r
        MosaicCell.GR -> gr
        MosaicCell.GB -> gr
        MosaicCell.B -> b
    }
}
