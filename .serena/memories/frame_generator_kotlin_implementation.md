# Frame Generator Tool - Kotlin Implementation Details

## Technology Stack
- **Language**: Kotlin/JVM
- **Build**: Gradle with Kotlin DSL
- **Video Processing**: JavaCV (FFmpeg wrapper)
- **CLI**: Clikt
- **Compression**: Built-in DEFLATE + custom RLE

## Key Dependencies
```kotlin
dependencies {
    implementation("org.bytedeco:javacv-platform:1.5.9")
    implementation("com.github.ajalt.clikt:clikt:4.2.1")
    implementation("com.charleskorn.kaml:kaml:0.55.0")
}
```

## Core Classes

### VideoProcessor
- Main orchestrator for video processing pipeline
- Handles video analysis, frame extraction, processing, compression
- Generates metadata and manages output files

### FrameExtractor 
- Uses JavaCV FFmpegFrameGrabber
- Extracts frames at target FPS (30fps default)
- Converts video frames to BufferedImage

### ImageProcessor
- **Grayscale conversion**: RGB to grayscale
- **Resize**: Any resolution → 25x25 using bilinear interpolation
- **Contrast enhancement**: Histogram equalization
- **Dithering**: Floyd-Steinberg algorithm for better visual quality
- **Gamma correction**: LED-optimized brightness curve (gamma=2.2)
- **LED optimization**: S-curve brightness mapping

### FrameCompressor
- **Run-Length Encoding**: Compresses consecutive same pixels
- **DEFLATE**: Standard compression on RLE data
- **Chunk-based**: Process 100 frames per chunk for memory efficiency

## Image Processing Pipeline
1. Video → Extract frames at 30fps
2. Frame → Convert to grayscale
3. Grayscale → Resize to 25x25
4. Resize → Enhance contrast (histogram equalization)
5. Enhanced → Apply Floyd-Steinberg dithering
6. Dithered → Apply gamma correction (2.2)
7. Corrected → LED brightness curve optimization
8. Optimized → Convert to IntArray[625]

## Output Format
```
output/
├── metadata.json           # Frame count, compression info
├── chunks/
│   ├── chunk_0000.bin     # Frames 0-99 (compressed)
│   ├── chunk_0001.bin     # Frames 100-199
│   └── ...
└── preview/ (optional)
    ├── frame_0000.png     # Debug preview images
    └── ...
```

## Usage
```bash
# Build
./gradlew build

# Run
java -jar build/libs/frame-generator.jar input.mp4 \
    --output ./output \
    --fps 30 \
    --quality 0.8 \
    --preview \
    --verbose
```

## Performance Targets
- **Processing Speed**: 50-100 frames/second
- **Memory Usage**: 256-512MB peak
- **Output Size**: 5-10MB for full Bad Apple!! video  
- **Compression Ratio**: 85-90% reduction

## Integration with Android
- Generate frame data with Frame Generator
- Copy chunks/ to app/src/main/assets/frames/
- Copy metadata.json to app/src/main/assets/
- Android app loads and decompresses chunks at runtime