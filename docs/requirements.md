# Bad Apple!! Glyph Toy Requirements

## Project Overview

Nothing Phone 3のGlyph Matrix (25x25 LED)でBad Apple!!の影絵PVを表示するGlyph Toysアプリケーション。

## Core Requirements

### Functional Requirements

1. **Video Playback**
   - Bad Apple!!の動画を25x25のマトリクス形式で再生
   - フレームレートの調整（通常30fps、必要に応じて調整可能）
   - 再生の開始・停止・一時停止機能

2. **Glyph Matrix Display**
   - 25x25 LEDマトリクスでの白黒（グレースケール）表示
   - 適切な明度調整（バッテリー消費と視認性のバランス）
   - リアルタイムでのフレーム更新

3. **User Interaction**
   - **Short Press**: 再生/一時停止の切り替え
   - **Long Press**: 動画のリセット（最初から再生）
   - **Touch Down/Up**: 詳細制御（早送り/巻き戻し等）

4. **AOD (Always-On Display) Support**
   - スクリーン消灯時も動画再生を継続
   - 1分間隔での自動更新対応

### Technical Requirements

1. **Performance**
   - 滑らかな動画再生（フレーム落ちの最小化）
   - 低レイテンシでのユーザー操作応答
   - バッテリー効率の最適化

2. **Data Management**
   - 動画データの効率的な格納・読み込み
   - メモリ使用量の最適化
   - 圧縮されたマトリクスデータの管理

3. **Compatibility**
   - Nothing Phone 3専用（Glyph.DEVICE_23112）
   - Android APIレベル要件の遵守
   - Glyph Matrix SDK 1.0の完全活用

## Non-Functional Requirements

### Usability
- 直感的なボタン操作
- 明確な動作フィードバック
- プレビュー画像での識別しやすさ

### Performance
- 起動時間: 3秒以内
- フレーム更新遅延: 50ms以内
- メモリ使用量: 100MB以内

### Reliability
- 長時間再生での安定動作
- エラー時の適切な復旧処理
- AODモードでの安定性

## Data Requirements

### Video Data
- **Source**: Bad Apple!! オリジナル動画
- **Format**: 25x25 グレースケールマトリクス
- **Duration**: 約3分36秒
- **Total Frames**: 約6,480フレーム（30fps想定）

### Storage
- **Format**: 圧縮されたバイナリ形式
- **Size**: 最大10MB以内を目標
- **Location**: APK内のassetsフォルダ

## Constraints

### Hardware Limitations
- 25x25 LEDマトリクスの解像度制限
- バッテリー消費への配慮
- 処理能力の制約

### SDK Limitations
- Glyph Matrix SDK 1.0の機能範囲内
- レイヤーシステム（Top/Mid/Low）の活用
- AODでの1分間隔更新制限

## Success Criteria

1. Bad Apple!!が認識可能な品質で25x25マトリクスに表示される
2. ユーザー操作が直感的で応答性が良い
3. 長時間再生でも安定して動作する
4. Nothing Phoneユーザーに愛される体験を提供する

## Future Enhancements

- 他の動画コンテンツの対応
- 再生速度の調整機能
- 複数の表示モード（反転、ズーム等）
- 音楽との同期再生