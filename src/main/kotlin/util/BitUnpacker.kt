package util

import metadata.enums.Endianness

/**
 * Helper to assist with reading arbitrary bit length values from a stream of bytes.
 */
sealed class BitUnpackHelper2(val packedBlockSize: Int, val unpackedBlockSize: Int) {

    abstract fun unpackImpl(packed: ByteArray, unpacked: IntArray)

    /**
     * Unpacks the values packed into the provided packed byte array.
     *
     * @param packedBlock the packed array. Must be of size [packedBlockSize].
     * @param unpackedBlock an unpacked block array to reuse. If not provided, or if it's the wrong size,
     *        a new one will be created.
     * @return the unpacked block to use.
     */
    fun unpack(packedBlock: ByteArray, unpackedBlock: IntArray?): IntArray {
        require(packedBlock.size == packedBlockSize) { "Packed block size must be $packedBlockSize" }
        val safeUnpackedBlock = if (unpackedBlock == null || unpackedBlock.size != unpackedBlockSize) {
            IntArray(unpackedBlockSize)
        } else {
            unpackedBlock
        }
        unpackImpl(packedBlock, safeUnpackedBlock)
        return safeUnpackedBlock
    }


    companion object {

        /**
         * Factory method to create an appropriate unpack helper.
         *
         * @param bitsPerValue the number of bits used for each value.
         * @param endianness the endianness used when packing the bits.
         * @return the unpack helper to use for those settings.
         */
        fun create(bitsPerValue: Int, endianness: Endianness): BitUnpackHelper2 = when (bitsPerValue) {
            8 -> Packed8
            10 -> when (endianness) {
                Endianness.BIG -> TODO("Have never seen Packed10Big")
                Endianness.LITTLE -> Packed10Little
            }

            12 -> when (endianness) {
                Endianness.BIG -> Packed12Big
                Endianness.LITTLE -> TODO("Have never seen Packed12Little")
            }

            16 -> when (endianness) {
                Endianness.BIG -> Packed16Big
                Endianness.LITTLE -> Packed16Little
            }

            else -> TODO("Have never seen bits per value of $bitsPerValue")
        }

        private fun Byte.safeToInt() = toInt().and(0b11111111)
    }

    private data object Packed8 : BitUnpackHelper2(packedBlockSize = 1, unpackedBlockSize = 1) {
        override fun unpackImpl(packed: ByteArray, unpacked: IntArray) {
            unpacked[0] = packed[0].safeToInt()
        }
    }

    private data object Packed10Little : BitUnpackHelper2(packedBlockSize = 5, unpackedBlockSize = 4) {
        override fun unpackImpl(packed: ByteArray, unpacked: IntArray) {
            val p0 = packed[0].safeToInt()
            val p1 = packed[1].safeToInt()
            val p2 = packed[2].safeToInt()
            val p3 = packed[3].safeToInt()
            val p4 = packed[4].safeToInt()

            // Packed structure according to:
            // https://github.com/hazirbas/light-field-toolbox/blob/master/LFToolbox0.4/SupportFunctions/LFUnpackRawBuffer.m
            //   +----------+----------+----------+----------+----------+
            //   | 00000000 | 11111111 | 22222222 | 33333333 | 33221100 |
            //   +----------+----------+----------+----------+----------+
            // Because it's this unusual structure, we can't guess what big-endian might look like.
            unpacked[0] = p0.shl(2) or p4.and(0b11)
            unpacked[1] = p1.shl(2) or p4.shr(2).and(0b11)
            unpacked[2] = p2.shl(2) or p4.shr(4).and(0b11)
            unpacked[3] = p3.shl(2) or p4.shr(6).and(0b11)
        }
    }

    private data object Packed12Big : BitUnpackHelper2(packedBlockSize = 3, unpackedBlockSize = 2) {
        override fun unpackImpl(packed: ByteArray, unpacked: IntArray) {
            val p0 = packed[0].safeToInt()
            val p1 = packed[1].safeToInt()
            val p2 = packed[2].safeToInt()

            unpacked[0] = p0.shl(4) or p1.and(0b11110000).shr(4)
            unpacked[1] = p1.and(0b00001111).shl(8) or p2
        }
    }

    private data object Packed16Big : BitUnpackHelper2(packedBlockSize = 2, unpackedBlockSize = 1) {
        override fun unpackImpl(packed: ByteArray, unpacked: IntArray) {
            unpacked[0] = packed[0].safeToInt().shl(8) or packed[1].safeToInt()
        }
    }

    private data object Packed16Little : BitUnpackHelper2(packedBlockSize = 2, unpackedBlockSize = 1) {
        override fun unpackImpl(packed: ByteArray, unpacked: IntArray) {
            unpacked[0] = packed[0].safeToInt() or packed[1].safeToInt().shl(8)
        }
    }
}
