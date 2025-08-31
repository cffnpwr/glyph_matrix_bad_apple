# Frame Generator Tool - Technical Design (Kotlin Implementation)

## Overview

Kotlin/JVMを使用してBad Apple!!動画から25x25 LEDマトリクス用のフレームデータを生成するコマンドラインツール。Java生態系の豊富な画像/動画処理ライブラリを活用し、プロジェクト全体の技術統一性を保つ。

## Architecture

### Module Structure
```
frame-generator/
├── build.gradle.kts
├── src/main/kotlin/
│   ├── dev/cffnpwr/framegen/
│   │   ├── Main.kt
│   │   ├── core/
│   │   │   ├── VideoProcessor.kt
│   │   │   ├── FrameExtractor.kt
│   │   │   ├── ImageProcessor.kt
│   │   │   └── FrameCompressor.kt
│   │   ├── model/
│   │   │   ├── ProcessingConfig.kt
│   │   │   ├── FrameData.kt
│   │   │   └── VideoInfo.kt
│   │   └── util/
│   │       ├── FileUtils.kt
│   │       └── MatrixUtils.kt
│   └── resources/
│       └── config.yaml
└── README.md
```

## Core Components

### 1. Data Models

```kotlin
data class ProcessingConfig(
    val targetResolution: Pair<Int, Int> = 25 to 25,
    val targetFps: Int = 30,
    val chunkSize: Int = 100,
    val compressionQuality: Float = 0.8f,
    val enhanceContrast: Boolean = true,
    val applyDithering: Boolean = true,
    val gammaCorrection: Double = 2.2
)

data class FrameData(
    val frameIndex: Int,
    val pixels: IntArray, // 625 values (25x25), 0-255
    val timestamp: Double
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FrameData
        return frameIndex == other.frameIndex && pixels.contentEquals(other.pixels)
    }
    
    override fun hashCode(): Int = frameIndex.hashCode()
}

data class VideoInfo(
    val duration: Double,
    val fps: Double,
    val frameCount: Int,
    val resolution: Pair<Int, Int>
)

data class CompressionResult(
    val totalFrames: Int,
    val totalSizeMB: Double,
    val compressionRatio: Double,
    val processingTimeMs: Long
)
```

### 2. Video Processing Engine

```kotlin
class VideoProcessor(private val config: ProcessingConfig) {
    
    fun processVideo(inputPath: String, outputPath: String): CompressionResult {
        val startTime = System.currentTimeMillis()
        
        // Analyze input video
        val videoInfo = analyzeVideo(inputPath)
        println("Video Info: ${videoInfo.duration}s, ${videoInfo.frameCount} frames")
        
        // Extract and process frames
        val frameExtractor = FrameExtractor(config)
        val imageProcessor = ImageProcessor(config)
        val frameCompressor = FrameCompressor(config)
        
        var totalSize = 0L
        var frameCount = 0
        
        frameExtractor.extractFrames(inputPath, videoInfo.fps).chunked(config.chunkSize)
            .forEachIndexed { chunkIndex, frameChunk ->
                val processedFrames = frameChunk.map { rawFrame ->
                    val processedFrame = imageProcessor.processFrame(rawFrame)
                    FrameData(frameCount++, processedFrame.toIntArray(), frameCount / config.targetFps.toDouble())
                }
                
                val compressedData = frameCompressor.compressFrameChunk(processedFrames)
                val outputFile = File(outputPath, "chunks/chunk_${chunkIndex.toString().padStart(4, '0')}.bin")
                outputFile.parentFile.mkdirs()
                outputFile.writeBytes(compressedData)
                
                totalSize += compressedData.size
                println("Processed chunk $chunkIndex: ${processedFrames.size} frames, ${compressedData.size} bytes")
            }
        
        // Generate metadata
        generateMetadata(outputPath, frameCount, config)
        
        val processingTime = System.currentTimeMillis() - startTime
        return CompressionResult(
            totalFrames = frameCount,
            totalSizeMB = totalSize / (1024.0 * 1024.0),
            compressionRatio = totalSize.toDouble() / (frameCount * 625), // Original size estimation
            processingTimeMs = processingTime
        )
    }
    
    private fun analyzeVideo(inputPath: String): VideoInfo {
        // Use VLCJ or JavaCV for video analysis
        val mediaPlayer = MediaPlayerFactory().mediaPlayers().newMediaPlayer()
        // Implementation details...
        return VideoInfo(216.0, 30.0, 6480, 480 to 360) // Example values
    }
}
```

### 3. Frame Extraction

```kotlin
class FrameExtractor(private val config: ProcessingConfig) {
    
    fun extractFrames(videoPath: String, originalFps: Double): Sequence<BufferedImage> {
        return sequence {
            val grabber = FFmpegFrameGrabber(videoPath).apply {
                start()
            }
            
            val frameInterval = (originalFps / config.targetFps).toInt()
            var frameIndex = 0
            
            try {
                while (true) {
                    val frame = grabber.grabFrame() ?: break
                    
                    if (frameIndex % frameInterval == 0 && frame.image != null) {
                        val bufferedImage = Java2DFrameConverter().convert(frame)
                        yield(bufferedImage)
                    }
                    frameIndex++
                }
            } finally {
                grabber.stop()
            }
        }
    }
}
```

### 4. Image Processing

```kotlin
class ImageProcessor(private val config: ProcessingConfig) {
    
    fun processFrame(frame: BufferedImage): Array<IntArray> {
        // Convert to grayscale
        val grayFrame = convertToGrayscale(frame)
        
        // Resize to target resolution
        val resized = resizeImage(grayFrame, config.targetResolution)
        
        // Apply enhancements
        val enhanced = if (config.enhanceContrast) {
            enhanceContrast(resized)
        } else resized
        
        val dithered = if (config.applyDithering) {
            applyFloydSteinbergDithering(enhanced)
        } else enhanced
        
        // Apply gamma correction
        val gammaCorrected = applyGammaCorrection(dithered, config.gammaCorrection)
        
        // Optimize for LED matrix
        return optimizeForLedMatrix(gammaCorrected)
    }
    
    private fun convertToGrayscale(image: BufferedImage): BufferedImage {
        val grayImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_BYTE_GRAY)
        val g2d = grayImage.createGraphics()
        g2d.drawImage(image, 0, 0, null)
        g2d.dispose()
        return grayImage
    }
    
    private fun resizeImage(image: BufferedImage, targetSize: Pair<Int, Int>): BufferedImage {
        val resized = BufferedImage(targetSize.first, targetSize.second, BufferedImage.TYPE_BYTE_GRAY)
        val g2d = resized.createGraphics().apply {
            setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        }
        g2d.drawImage(image, 0, 0, targetSize.first, targetSize.second, null)
        g2d.dispose()
        return resized
    }
    
    private fun enhanceContrast(image: BufferedImage): BufferedImage {
        val enhanced = BufferedImage(image.width, image.height, image.type)
        
        // Calculate histogram
        val histogram = IntArray(256)
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val pixel = image.getRGB(x, y) and 0xFF
                histogram[pixel]++
            }
        }
        
        // Apply histogram equalization
        val cdf = histogram.runningFold(0) { acc, value -> acc + value }.drop(1).toIntArray()
        val totalPixels = image.width * image.height
        
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val pixel = image.getRGB(x, y) and 0xFF
                val equalizedValue = ((cdf[pixel].toDouble() / totalPixels) * 255).toInt()
                val newPixel = (equalizedValue shl 16) or (equalizedValue shl 8) or equalizedValue or (0xFF shl 24)
                enhanced.setRGB(x, y, newPixel)
            }
        }
        
        return enhanced
    }
    
    private fun applyFloydSteinbergDithering(image: BufferedImage): BufferedImage {
        val dithered = BufferedImage(image.width, image.height, image.type)
        val pixels = Array(image.height) { IntArray(image.width) }
        
        // Copy pixel values
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                pixels[y][x] = image.getRGB(x, y) and 0xFF
            }
        }
        
        // Apply Floyd-Steinberg dithering
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val oldPixel = pixels[y][x]
                val newPixel = if (oldPixel < 128) 0 else 255
                pixels[y][x] = newPixel
                
                val quantError = oldPixel - newPixel
                
                // Distribute error to neighboring pixels
                if (x + 1 < image.width) pixels[y][x + 1] += (quantError * 7) / 16
                if (x > 0 && y + 1 < image.height) pixels[y + 1][x - 1] += (quantError * 3) / 16
                if (y + 1 < image.height) pixels[y + 1][x] += (quantError * 5) / 16
                if (x + 1 < image.width && y + 1 < image.height) pixels[y + 1][x + 1] += (quantError * 1) / 16
            }
        }
        
        // Copy back to image
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val pixel = pixels[y][x].coerceIn(0, 255)
                val rgbPixel = (pixel shl 16) or (pixel shl 8) or pixel or (0xFF shl 24)
                dithered.setRGB(x, y, rgbPixel)
            }
        }
        
        return dithered
    }
    
    private fun applyGammaCorrection(image: BufferedImage, gamma: Double): BufferedImage {
        val corrected = BufferedImage(image.width, image.height, image.type)
        val lookupTable = (0..255).map { value ->
            (255 * kotlin.math.pow(value / 255.0, 1.0 / gamma)).toInt().coerceIn(0, 255)
        }.toIntArray()
        
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val pixel = image.getRGB(x, y) and 0xFF
                val correctedValue = lookupTable[pixel]
                val newPixel = (correctedValue shl 16) or (correctedValue shl 8) or correctedValue or (0xFF shl 24)
                corrected.setRGB(x, y, newPixel)
            }
        }
        
        return corrected
    }
    
    private fun optimizeForLedMatrix(image: BufferedImage): Array<IntArray> {
        val matrix = Array(config.targetResolution.second) { IntArray(config.targetResolution.first) }
        
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val pixel = image.getRGB(x, y) and 0xFF
                // Apply LED-specific brightness curve
                matrix[y][x] = applyLedBrightnessCurve(pixel)
            }
        }
        
        return matrix
    }
    
    private fun applyLedBrightnessCurve(value: Int): Int {
        val normalized = value / 255.0
        // Apply S-curve for better LED visibility
        val mapped = 1.0 / (1.0 + kotlin.math.exp(-12.0 * (normalized - 0.5)))
        return (mapped * 255).toInt()
    }
}
```

### 5. Frame Compression

```kotlin
class FrameCompressor(private val config: ProcessingConfig) {
    
    fun compressFrameChunk(frames: List<FrameData>): ByteArray {
        // Convert frames to byte array
        val frameData = frames.flatMap { frame ->
            frame.pixels.map { it.toByte() }
        }.toByteArray()
        
        // Apply Run-Length Encoding
        val rleData = runLengthEncode(frameData)
        
        // Apply DEFLATE compression
        return compress(rleData)
    }
    
    private fun runLengthEncode(data: ByteArray): ByteArray {
        val encoded = mutableListOf<Byte>()
        var i = 0
        
        while (i < data.size) {
            val currentValue = data[i]
            var count = 1
            
            // Count consecutive same values (max 255)
            while (i + count < data.size && 
                   data[i + count] == currentValue && 
                   count < 255) {
                count++
            }
            
            encoded.add(currentValue)
            encoded.add(count.toByte())
            i += count
        }
        
        return encoded.toByteArray()
    }
    
    private fun compress(data: ByteArray): ByteArray {
        val outputStream = ByteArrayOutputStream()
        DeflaterOutputStream(outputStream, Deflater(Deflater.BEST_COMPRESSION)).use { deflater ->
            deflater.write(data)
        }
        return outputStream.toByteArray()
    }
}
```

## Build Configuration

### build.gradle.kts
```kotlin
plugins {
    kotlin("jvm") version "1.9.20"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Video/Image processing
    implementation("org.bytedeco:javacv-platform:1.5.9")
    implementation("uk.co.caprica:vlcj:4.8.2")
    
    // CLI
    implementation("com.github.ajalt.clikt:clikt:4.2.1")
    
    // Configuration
    implementation("com.charleskorn.kaml:kaml:0.55.0")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    
    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.mockk:mockk:1.13.8")
}

application {
    mainClass.set("dev.cffnpwr.framegen.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

// Create fat jar for distribution
tasks.jar {
    manifest {
        attributes["Main-Class"] = "dev.cffnpwr.framegen.MainKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
```

## Command Line Interface

### Main.kt
```kotlin
class FrameGeneratorCommand : CliktCommand() {
    private val inputVideo by argument(help="Input video file path")
    private val outputDir by option("-o", "--output", help="Output directory").default("./output")
    private val configFile by option("-c", "--config", help="Configuration file").default("config.yaml")
    private val fps by option("--fps", help="Target FPS").int().default(30)
    private val quality by option("--quality", help="Compression quality 0.0-1.0").float().default(0.8f)
    private val preview by option("--preview", help="Generate preview images").flag(default = false)
    private val verbose by option("-v", "--verbose", help="Verbose output").flag(default = false)

    override fun run() {
        val config = loadConfig(configFile).copy(
            targetFps = fps,
            compressionQuality = quality
        )
        
        if (verbose) {
            println("Configuration: $config")
        }
        
        val processor = VideoProcessor(config)
        val result = processor.processVideo(inputVideo, outputDir)
        
        println("Processing completed!")
        println("Total frames: ${result.totalFrames}")
        println("Output size: ${"%.2f".format(result.totalSizeMB)} MB")
        println("Compression ratio: ${"%.2f".format(result.compressionRatio * 100)}%")
        println("Processing time: ${result.processingTimeMs / 1000.0}s")
        
        if (preview) {
            generatePreviewImages(outputDir, config)
        }
    }
}

fun main(args: Array<String>) = FrameGeneratorCommand().main(args)
```

## Usage Examples

### Basic Usage
```bash
# Build the tool
./gradlew build

# Run with default settings
java -jar build/libs/frame-generator.jar input/bad_apple.mp4

# Advanced usage
java -jar build/libs/frame-generator.jar input/bad_apple.mp4 \
    --output ./output \
    --fps 30 \
    --quality 0.8 \
    --preview \
    --verbose
```

### Configuration File (config.yaml)
```yaml
processing:
  targetFps: 30
  chunkSize: 100
  compressionQuality: 0.8
  enhanceContrast: true
  applyDithering: true
  gammaCorrection: 2.2

output:
  generatePreview: true
  previewInterval: 100  # Generate preview every N frames
```

## Integration with Android Project

### 1. Asset Generation
```bash
# Generate frame data
java -jar frame-generator.jar input/bad_apple.mp4 -o temp_output

# Copy to Android assets
cp -r temp_output/chunks ../app/src/main/assets/frames/
cp temp_output/metadata.json ../app/src/main/assets/
```

### 2. Gradle Integration
```kotlin
// In app/build.gradle.kts
task("generateFrames") {
    doLast {
        exec {
            commandLine("java", "-jar", "../frame-generator/build/libs/frame-generator.jar", 
                       "input/bad_apple.mp4", "-o", "src/main/assets")
        }
    }
}

tasks.named("preBuild") {
    dependsOn("generateFrames")
}
```

## Performance Characteristics

### Expected Performance
- **Processing Speed**: 50-100 frames/second (depending on hardware)
- **Memory Usage**: 256MB-512MB peak
- **Output Size**: 5-10MB for full Bad Apple!! video
- **Compression Ratio**: 85-90% size reduction

### Optimization Features
- **Parallel Processing**: Utilize multiple CPU cores for frame processing
- **Memory Management**: Stream processing to minimize memory usage
- **Efficient I/O**: Batch file operations for better performance

KotlinでのFrame Generator Toolの技術設計が完成しました！この設計ではJava生態系の豊富なライブラリを活用し、プロジェクト全体の技術統一性を保ちながら高性能な動画処理を実現します。