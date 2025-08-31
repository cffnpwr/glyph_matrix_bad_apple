# Glyph Matrix Android App Architecture

## Service Architecture
Based on Nothing's Glyph Matrix SDK pattern using bound service architecture.

## Core Components

### 1. BadAppleGlyphToyService extends GlyphMatrixService
- **Purpose**: Main Glyph Toy service entry point
- **Responsibilities**:
  - Handle Glyph Button events (short/long press, touch up/down)
  - Manage AOD (Always-On Display) updates
  - Interface with Glyph Matrix SDK

```kotlin
class BadAppleGlyphToyService : GlyphMatrixService("BadApple") {
    override fun performOnServiceConnected(context: Context, manager: GlyphMatrixManager)
    override fun onTouchPointPressed()      // Play/Pause toggle
    override fun onTouchPointLongPress()    // Reset video
    override fun onTouchPointReleased()     // End operation
    override fun onAODUpdate()              // AOD 1-minute updates
}
```

### 2. VideoPlayerEngine
- **Purpose**: Video playback logic controller
- **State Management**: PLAYING, PAUSED, STOPPED
- **Frame Control**: Current frame index, seeking, frame counting
- **Timing**: FPS control, frame intervals

### 3. FrameDataManager  
- **Purpose**: Efficient frame data loading and caching
- **Features**:
  - LRU cache for frequently accessed frames
  - Chunk-based loading (100 frames per chunk)
  - Decompression of RLE + DEFLATE data
  - Memory optimization

### 4. MatrixRenderer
- **Purpose**: Convert frame data to GlyphMatrixFrame objects
- **Optimizations**:
  - Bitmap reuse to minimize GC
  - LED-specific brightness adjustment
  - Layer management (Top/Mid/Low)

## Data Flow

### Initialization
```
App Start → Service Bind → SDK Init → Load Metadata → 
Load First Chunk → Ready State
```

### Playback Loop
```
Button Press → State Change → Get Next Frame → 
Process Frame → Render Matrix → Display → 
Timer Schedule → Repeat
```

### AOD Flow
```
Screen Off → AOD Event → Get Current Frame → 
Update Matrix → Schedule Next Update (1min)
```

## Thread Management
- **Main Thread**: UI, SDK calls, service lifecycle
- **Background Thread**: Data loading, decompression
- **Render Thread**: Frame rendering, timing control

## Memory Management
```kotlin
class FrameDataManager {
    private val frameCache = LRUCache<Int, IntArray>(50) // Cache 50 frames
    private val chunkSize = 100
    
    // Load chunks on-demand, cache frequently used frames
    // Decompress: RLE → DEFLATE → IntArray[625]
}
```

## Error Handling
- **Service Connection**: Retry policy with exponential backoff
- **Data Loading**: Fallback data, memory pressure handling
- **Rendering**: Pause on error, safe state recovery

## Performance Optimizations
- **Frame Buffering**: Preload next frames while playing current
- **Adaptive Brightness**: Content-based brightness adjustment
- **Frame Skipping**: Skip frames if rendering falls behind
- **Memory Pressure**: Reduce cache size under memory pressure

## Glyph Matrix SDK Integration
```kotlin
// Registration for Phone 3
glyphMatrixManager.register(Glyph.DEVICE_23112)

// Frame update
val frame = GlyphMatrixFrame.Builder()
    .addTop(matrixObject)
    .build(applicationContext)
glyphMatrixManager.setMatrixFrame(frame.render())
```

## Manifest Configuration
```xml
<service android:name=".BadAppleGlyphToyService"
    android:exported="true">
    <intent-filter>
        <action android:name="com.nothing.glyph.TOY"/>
    </intent-filter>
    <meta-data android:name="com.nothing.glyph.toy.name" 
               android:resource="@string/toy_name"/>
    <meta-data android:name="com.nothing.glyph.toy.image" 
               android:resource="@drawable/toy_preview"/>
    <meta-data android:name="com.nothing.glyph.toy.longpress" 
               android:value="1"/>
    <meta-data android:name="com.nothing.glyph.toy.aod_support" 
               android:value="1"/>
</service>
```