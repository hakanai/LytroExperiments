package metadata

import LightFieldFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import metadata.enums.Endianness
import metadata.enums.MosaicCell
import metadata.types.CellDoubleMapping
import metadata.types.CellIntMapping

@Serializable
data class Metadata(
    val schema: String,
    val generator: String,
    val camera: Camera,
    val picture: Picture,
    val settings: Settings,
    val image: Image,
    val algorithms: Algorithms,
    val devices: Devices,
) {
    companion object {
        fun readFrom(lightFieldFile: LightFieldFile): Metadata {
            val metadataBlock = lightFieldFile.findDataBlock(MainMetadata.FrameReferenceNames.METADATA_REF)
            val json = lightFieldFile.readData(metadataBlock).readUtf8()
//            println("Raw JSON of metadata: $json")
            return Json.decodeFromString<Metadata>(json)
        }
    }

    /**
     * Metadata regarding the camera used to take the picture.
     *
     * @property make the make of the camera.
     * @property model the model of the camera.
     * @property firmware the firmware version running on the camera.
     */
    @Serializable
    data class Camera(
        val make: String,
        val model: String,
        val firmware: String,
    )

    @Serializable
    data class Picture(
        val totalFrames: Int,
        val frameIndex: Int,
        val dcfDirectory: String,
        val dcfFile: String,
    )

    @Serializable
    data class Settings(
        val depth: Depth,
        val zoom: Zoom,
        val focus: Focus,
        val exposure: Exposure,
        val shutter: Shutter,
        val flash: Flash,
        val whiteBalance: WhiteBalance,
    ) {
        @Serializable
        data class Depth(
            // XXX: All could be enum, values are off, on
            val assist: String,
            val histogram: String,
            val overlay: String,
        )

        @Serializable
        data class Zoom(
            val ringLock: Boolean
        )

        @Serializable
        data class RegionOfInterest(
            val top: Double,
            val left: Double,
            val bottom: Double,
            val right: Double,
        )

        @Serializable
        data class Focus(
            // XXX: Could be enum: "auto", "manual", "instant"
            val mode: String = "auto",
            val ringLock: Boolean,
            val captureLambda: Double,
            val bracketEnable: Boolean = false,
            val bracketCount: Int = 3,
            val bracketStep: Double = 0.0,
            val bracketOffset: Double = 0.0,
            // XXX: Could be enum: "single", "continuous"
            val afActuationMode: String = "single",
            // XXX: Could be enum: "manual", "instant", "single", "continuous"
            val afDriveMode: String = "manual",
            val roi: List<RegionOfInterest> = emptyList(),
        )

        @Serializable
        data class Exposure(
            // XXX: Could be enum: "program", "isoPriority", "shutterPriority", "manual"
            val mode: String = "program",
            val compensation: Double = 0.0,
            val bracketEnable: Boolean = false,
            val bracketCount: Int = 3,
            val bracketStep: Double = 0.0,
            val bracketOffset: Double = 0.0,
            val aeLock: Boolean,
            val meter: Meter,
        ) {
            @Serializable
            data class Meter(
                // XXX: Could be enum: "evaluative", "average", "roiAverage"
                val mode: String,
                // XXX: Could be enum: "af", "center", "manual"
                val roiMode: String = "af",
                val roi: List<RegionOfInterest> = emptyList(),
            )
        }

        @Serializable
        data class Shutter(
            // XXX: Could be enum: "single", "continuous", "timer", "auto", "timeLapse"
            val driveMode: String,
            val selfTimerEnable: Boolean,
            val selfTimerDuration: Double,
        )

        @Serializable
        data class Flash(
            // XXX: Could be enum: "unknown", "manual", "ttl", "multi"
            val mode: String,
            val exposureCompensation: Double,
            // XXX: Could be enum: "manual", "auto"
            val zoomMode: String,
            // XXX: Could be enum: "front", "rear"
            val curtainTriggerSync: String,
            // XXX: Could be enum: "off", "auto"
            val afAssistMode: String,
        )

        @Serializable
        data class WhiteBalance(
            // XXX: Could be enum: "auto", "preset", "manual"
            val mode: String,
            // XXX: Could be enum: "tungsten", "fluorescent", "daylight", "cloudy", "shade", "flash", "custom"
            val preset: String = "tungsten",
            val cct: Double,
            val tint: Double,
        )
    }

    @Serializable
    data class Mosaic(
        val tile: String,
        val upperLeftPixel: MosaicCell,
    ) {
        /**
         * Splits up the contents of `tile` and returns them as a list of lists, one list per row,
         * with the rows ordered top to bottom, and cells within the rows ordered left to right.
         *
         * Flips the lists as necessary to respect the `upperLeftPixel` value.
         */
        fun asCells(): List<List<MosaicCell>> {
            var cells = tile.split(':').map { it.split(',').map(MosaicCell::fromString) }

            if (upperLeftPixel !in cells[0]) {
                // flip top to bottom
                cells = cells.reversed()
            }

            if (cells[0][0] != upperLeftPixel) {
                // flip left to right
                cells = cells.map { it.reversed() }
            }

            return cells
        }
    }

    /**
     * Metadata regarding the image itself.
     *
     * @property width the width, in pixels.
     * @property height the height, in pixels.
     * @property orientation the orientation, ???
     * @property modulationExposureBias ???
     * @property limitExposureBias ???
     * @property iso the ISO number used to take the picture.
     * @property pixelPacking ???
     * @property pixelFormat ???
     * @property originOnSensor ???
     * @property mosaic ???
     * @property color ???
     */
    @Serializable
    data class Image(
        val width: Int,
        val height: Int,
        val originOnSensor: OriginOnSensor,
        // XXX: Could constrain this 1-8. Correlates with EXIF orientation values.
        val orientation: Int,
        val mosaic: Mosaic,
        val pixelPacking: PixelPacking,
        val pixelFormat: PixelFormat,
        val modulationExposureBias: Double,
        val limitExposureBias: Double,
        val iso: Int,
        val color: Color,
    ) {
        @Serializable
        data class OriginOnSensor(
            val x: Int,
            val y: Int,
        )

        /**
         * Metadata regarding how the pixel data is packed into bytes.
         *
         * @property endianness the endianness used to write the values.
         * @property bitsPerPixel the number of bits used per pixel.
         */
        @Serializable
        data class PixelPacking(
            val endianness: Endianness,
            val bitsPerPixel: Int,
        )

        /**
         * Metadata regarding the format of the pixel data.
         *
         * @property white a map of the cell name to the value representing white.
         * @property black a map of the cell name to the value representing black.
         * @property rightShift ???
         */
        @Serializable
        data class PixelFormat(
            val rightShift: Int,
            val black: CellIntMapping,
            val white: CellIntMapping,
        )

        @Serializable
        data class Color(
            val whiteBalanceGain: CellDoubleMapping,
            val ccm: List<Double>,
        )
    }

    @Serializable
    data class Algorithms(
        val awb: Awb,
        val ae: Ae,
        val af: Af,
    ) {
        @Serializable
        data class Awb(
            // XXX: Could be enum: "fullFrame"
            val roi: String,
            val computed: Computed,
        ) {
            @Serializable
            data class Computed(
                val gain: CellDoubleMapping,
                val cct: Double,
            )
        }

        @Serializable
        data class Ae(
            // XXX: Could be enum: "live", "preflash"
            val mode: String,
            // XXX: Could be enum: "fullFrame", "meterRoi", "followAf"
            val roi: String,
            val computed: Computed,
        ) {
            @Serializable
            data class Computed(
                val ev: Double
            )
        }

        @Serializable
        data class Af(
            // XXX: Could be enum: "focusRoi"
            val roi: String,
            val computed: Computed,
        ) {
            @Serializable
            data class Computed(
                val focusStep: Int,
            )
        }
    }

    @Serializable
    data class Devices(
        val accelerometer: Accelerometer,
        val battery: Battery,
        val clock: Clock,
        // XXX: val flashUnits: List<FlashUnit>,
        val lens: Lens,
        val mla: Mla,
        val sdCard: SdCard? = null,
        val sensor: Sensor,
        val shutter: Shutter,
    ) {
        @Serializable
        data class Accelerometer(
            val samples: List<AccelerometerSample>,
        ) {
            @Serializable
            data class AccelerometerSample(
                val time: Double,
                val x: Double,
                val y: Double,
                val z: Double,
            )
        }

        @Serializable
        data class Battery(
            val make: String = "",
            val model: String = "",
            val chargeLevel: Int,
            val cycleCount: Int = 0,
            // XXX: Could be enum: "none", "usb", "charger"
            val chargeSource: String = "none",
        )

        @Serializable
        data class Clock(
            // TODO: Should be something like Instant, pending finding a good time API for Kotlin
            val zuluTime: String,
            val isTimeValid: Boolean = true,
        )

        @Serializable
        data class Lens(
            val focalLength: Double,
            val infinityLambda: Double,
            val zoomStep: Int,
            val focusStep: Int,
            val fNumber: Double,
            val exitPupilOffset: ExitPupilOffset,
            val opticalCenterOffset: OpticalCenterOffset,
        ) {
            @Serializable
            data class ExitPupilOffset(
                val z: Double,
            )

            @Serializable
            data class OpticalCenterOffset(
                val x: Double,
                val y: Double,
            )
        }

        @Serializable
        data class Mla(
            val config: String,
            // XXX: Could be enum: "hexUniformRowMajor"
            val tiling: String,
            val rotation: Double,
            val lensPitch: Double,
            val scaleFactor: ScaleFactor,
            val sensorOffset: SensorOffset,
        ) {
            @Serializable
            data class ScaleFactor(
                val x: Double,
                val y: Double,
            )

            @Serializable
            data class SensorOffset(
                val x: Double,
                val y: Double,
                val z: Double,
            )
        }

        @Serializable
        data class SdCard(
            val make: String = "",
            val model: String = "",
            val capacity: Int,
            val `class`: String,
        )

        @Serializable
        data class Sensor(
            val pixelWidth: Int,
            val pixelHeight: Int,
            val pixelPitch: Double,
            val mosaic: Mosaic,
            val bitsPerPixel: Int,
            val temperature: Double? = null,
            val baseIso: Double,
            val analogGain: CellDoubleMapping,
            val normalizedResponses: List<NormalizedResponse>,
            val perCcm: List<PerCcmEntry>,
        ) {
            @Serializable
            data class NormalizedResponse(
                val cct: Double,
                // XXX: Could be enum: "A", "B", "C", "D50", "D55", "D65", "D75", "E",
                //      "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12"
                val standardIlluminant: String = "D65",
                val r: Double,
                val gr: Double,
                val gb: Double,
                val b: Double,
            )

            @Serializable
            data class PerCcmEntry(
                val cct: Double,
                // XXX: Could be enum, see other occurrence above
                val standardIlluminant: String = "D65",
                val ccm: List<Double>,
            )
        }

        @Serializable
        data class Shutter(
            // XXX: Could be enum: "focalPlaneCurtain"
            val mechanism: String,
            val pixelExposureDuration: Double,
            val frameExposureDuration: Double,
            val maxSyncSpeed: Double
        )
    }
}

