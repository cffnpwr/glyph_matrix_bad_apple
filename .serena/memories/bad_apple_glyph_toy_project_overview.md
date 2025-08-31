# Bad Apple!! Glyph Toy Project Overview

## Project Goal
Nothing Phone 3のGlyph Matrix (25x25 LED)でBad Apple!!の影絵PVを表示するGlyph Toysアプリケーション

## Key Components

### 1. Android App (Main Application)
- **Language**: Kotlin
- **SDK**: Glyph Matrix SDK 1.0
- **Target**: Nothing Phone 3 (Glyph.DEVICE_23112)
- **Main Classes**:
  - BadAppleGlyphToyService: Glyph Toyサービス
  - VideoPlayerEngine: 動画再生制御
  - FrameDataManager: フレームデータ管理
  - MatrixRenderer: 25x25マトリクス表示

### 2. Frame Generator Tool
- **Language**: Kotlin/JVM
- **Purpose**: 動画から25x25マトリクスデータ生成
- **Key Features**:
  - 動画フレーム抽出
  - 25x25リサイズ・グレースケール変換
  - Floyd-Steinberg dithering
  - Run-length encoding + DEFLATE圧縮
- **Libraries**: JavaCV, Clikt

## Technical Requirements
- **Target Resolution**: 25x25 pixels
- **Frame Rate**: 30fps
- **Data Size**: <10MB for full video
- **Performance**: <50ms frame update latency
- **Memory**: <100MB usage

## User Interactions
- **Short Press**: Play/Pause
- **Long Press**: Reset to beginning
- **AOD Support**: Continue playback when screen off

## Data Format
```kotlin
data class FrameData(
    val frameIndex: Int,
    val pixels: IntArray, // 625 values (25x25), 0-255
    val timestamp: Double
)
```

## Architecture Pattern
- Service-based architecture using GlyphMatrixService
- Component separation: Data → Processing → Rendering → Display
- Memory-efficient frame buffering with LRU cache
- Error handling with retry policies

## Development Phases
1. Frame Generator Tool (Week 1-2)
2. Core Video Engine (Week 3-4)  
3. User Interaction (Week 5)
4. Advanced Features & AOD (Week 6)
5. Testing & Polish (Week 7-8)
6. Release Preparation (Week 9)

## Project Structure
```
glyph_matrix_bad_apple/
├── docs/               # Design documents
├── frame-generator/    # Kotlin frame generation tool
├── app/               # Android application
│   ├── src/main/assets/   # Generated frame data
│   └── libs/             # Glyph Matrix SDK
```