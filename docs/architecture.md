# Bad Apple!! Glyph Toy Architecture

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Glyph Matrix System                     │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐    ┌──────────────────────────────┐    │
│  │  Glyph Toy      │    │     Video Player Engine      │    │
│  │  Service        │◄───┤                              │    │
│  │                 │    │ - Frame Management           │    │
│  │ - Button Events │    │ - Playback Control           │    │
│  │ - AOD Support   │    │ - Data Loading               │    │
│  │ - Matrix Update │    │                              │    │
│  └─────────────────┘    └──────────────────────────────┘    │
│           │                           │                      │
│           ▼                           ▼                      │
│  ┌─────────────────┐    ┌──────────────────────────────┐    │
│  │ Glyph Matrix    │    │     Frame Data Storage       │    │
│  │ Manager         │    │                              │    │
│  │                 │    │ - Compressed Matrix Data     │    │
│  │ - SDK Interface │    │ - Asset Management           │    │
│  │ - Frame Render  │    │ - Memory Optimization        │    │
│  └─────────────────┘    └──────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

## Component Design

### 1. BadAppleGlyphToyService

**Responsibility**: Glyph Toyサービスのエントリーポイント
- Glyph Matrix SDKとの統合
- ボタンイベントの処理
- AODモードのサポート

**Key Methods**:
```kotlin
class BadAppleGlyphToyService : GlyphMatrixService("BadApple") {
    override fun performOnServiceConnected(context: Context, manager: GlyphMatrixManager)
    override fun onTouchPointPressed() // 再生/一時停止
    override fun onTouchPointLongPress() // リセット
    override fun onTouchPointReleased() // 操作終了
    override fun onAODUpdate() // AODでの定期更新
}
```

### 2. VideoPlayerEngine

**Responsibility**: 動画再生ロジックの制御
- フレーム単位での再生制御
- 再生状態の管理
- フレームレート調整

**Key Components**:
```kotlin
class VideoPlayerEngine {
    // Playback Control
    fun play()
    fun pause()
    fun reset()
    fun seekTo(frameIndex: Int)
    
    // Frame Management
    fun getCurrentFrame(): IntArray
    fun hasNextFrame(): Boolean
    fun getFrameCount(): Int
    
    // State Management
    enum class PlayState { PLAYING, PAUSED, STOPPED }
    val currentState: PlayState
    val currentFrameIndex: Int
}
```

### 3. FrameDataManager

**Responsibility**: フレームデータの管理と最適化
- 圧縮データの読み込み
- メモリ効率的なバッファリング
- フレームデータの変換

**Key Features**:
```kotlin
class FrameDataManager {
    // Data Loading
    fun loadFrameData(): Boolean
    fun getFrame(index: Int): IntArray
    
    // Memory Management
    private val frameBuffer: LRUCache<Int, IntArray>
    private val chunkSize: Int = 100 // フレームチャンクサイズ
    
    // Data Format
    private fun decompressFrame(compressedData: ByteArray): IntArray
}
```

### 4. MatrixRenderer

**Responsibility**: 25x25マトリクス表示の最適化
- フレームデータのマトリクス変換
- 明度調整
- レイヤー管理

**Implementation**:
```kotlin
class MatrixRenderer {
    fun renderFrame(frameData: IntArray): GlyphMatrixFrame {
        val matrixObject = GlyphMatrixObject.Builder()
            .setImageSource(frameToBitmap(frameData))
            .setBrightness(optimizedBrightness)
            .setPosition(0, 0)
            .build()
            
        return GlyphMatrixFrame.Builder()
            .addTop(matrixObject)
            .build(context)
    }
    
    private fun frameToBitmap(data: IntArray): Bitmap
    private fun optimizeBrightness(frame: IntArray): Int
}
```

## Data Flow

### 1. Initialization Flow
```
App Start → Service Bind → SDK Init → Load Frame Data → Ready State
```

### 2. Playback Flow
```
Button Press → State Change → Get Next Frame → Render Matrix → Display → Timer Trigger → Repeat
```

### 3. AOD Flow
```
Screen Off → AOD Trigger → Get Current Frame → Update Matrix → 1min Timer → Repeat
```

## Data Structures

### Frame Data Format
```kotlin
// Raw frame data (25x25 = 625 values, 0-255 grayscale)
data class FrameData(
    val frameIndex: Int,
    val pixels: IntArray // Size: 625, Values: 0-255
)

// Compressed frame storage
data class CompressedFrameChunk(
    val startIndex: Int,
    val frameCount: Int,
    val compressedData: ByteArray
)
```

### Playback State
```kotlin
data class PlaybackState(
    val isPlaying: Boolean,
    val currentFrameIndex: Int,
    val totalFrames: Int,
    val playbackSpeed: Float = 1.0f
)
```

## Performance Optimizations

### 1. Memory Management
- **Frame Buffering**: LRU cache for frequently accessed frames
- **Chunk Loading**: Load frames in chunks to reduce memory footprint
- **Garbage Collection**: Minimize object creation in render loop

### 2. Rendering Optimization
- **Frame Skipping**: Skip frames if rendering falls behind
- **Adaptive Brightness**: Adjust brightness based on frame content
- **Efficient Bitmap Creation**: Reuse bitmap objects

### 3. Data Compression
- **Run-Length Encoding**: Compress similar consecutive pixels
- **Frame Differencing**: Store only differences between frames
- **Lossy Compression**: Reduce color depth for better compression

## Error Handling

### 1. Service Connection Issues
```kotlin
private val connectionRetryPolicy = RetryPolicy(
    maxRetries = 3,
    backoffDelay = 1000L
)
```

### 2. Data Loading Failures
```kotlin
private fun handleDataLoadError(error: Throwable) {
    when (error) {
        is FileNotFoundException -> loadFallbackData()
        is OutOfMemoryError -> reduceBufferSize()
        else -> notifyUser(error)
    }
}
```

### 3. Rendering Issues
```kotlin
private fun handleRenderError(error: Throwable) {
    pausePlayback()
    resetToSafeState()
    logError(error)
}
```

## Threading Model

### 1. Main Thread
- UI interactions
- Glyph Matrix SDK calls
- Service lifecycle

### 2. Background Thread
- Frame data loading
- Data decompression
- Bitmap processing

### 3. Render Thread
- Frame rendering
- Matrix updates
- Timing control

```kotlin
class ThreadManager {
    private val backgroundExecutor = Executors.newSingleThreadExecutor()
    private val renderHandler = Handler(Looper.getMainLooper())
    
    fun executeBackground(task: Runnable) = backgroundExecutor.execute(task)
    fun scheduleRender(task: Runnable, delay: Long) = renderHandler.postDelayed(task, delay)
}
```

## Testing Strategy

### 1. Unit Tests
- VideoPlayerEngine logic
- FrameDataManager operations
- MatrixRenderer output

### 2. Integration Tests
- Service lifecycle
- SDK integration
- End-to-end playback

### 3. Performance Tests
- Memory usage monitoring
- Frame rate measurement
- Battery consumption analysis