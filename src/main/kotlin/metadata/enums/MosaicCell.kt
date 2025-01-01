package metadata.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Enumeration of cells in a Bayer mosaic.
 */
@Serializable
enum class MosaicCell {

    /**
     * The red cell
     */
    @SerialName("r")
    R,

    /**
     * The green cell horizontal from red.
     */
    @SerialName("gr")
    GR,

    /**
     * The green cell horizontal from blue.
     */
    @SerialName("gb")
    GB,

    /**
     * The blue cell.
     */
    @SerialName("b")
    B,
    ;

    companion object {

        /**
         * Converts from the string form found in metadata.
         */
        fun fromString(string: String) = Json.decodeFromString<MosaicCell>("\"$string\"")
    }
}
