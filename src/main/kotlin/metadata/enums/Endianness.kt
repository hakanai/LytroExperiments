package metadata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enumeration of supported endianness for pixel packing.
 */
@Serializable
enum class Endianness {
    /**
     * Big endian
     */
    @SerialName("big")
    BIG,

    /**
     * Little endian
     */
    @SerialName("little")
    LITTLE,
}
