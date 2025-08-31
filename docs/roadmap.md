# Bad Apple!! Glyph Toy Project Roadmap

## プロジェクト概要

Nothing Phone 3のGlyph Matrix (25x25 LED)でBad Apple!!の影絵PVを表示するGlyph Toysアプリケーションの開発計画。

## 開発フェーズ

### Phase 1: Foundation Setup (Week 1-2)
**目標**: 基盤となるプロジェクト構造とFrame Generator Toolの構築

#### 1.1 Frame Generator Tool開発
- [ ] Kotlin Frame Generator moduleの作成
- [ ] 基本的な動画読み込み・フレーム抽出機能
- [ ] 25x25マトリクス変換アルゴリズム実装
- [ ] 画像処理機能（グレースケール、リサイズ、コントラスト調整）
- [ ] フレームデータ圧縮機能
- [ ] CLI インターフェース実装

**成果物**:
```
frame-generator/
├── build.gradle.kts
├── src/main/kotlin/dev/cffnpwr/framegen/
└── Bad Apple!! 動画データ生成済み
```

#### 1.2 Android Project基盤整備
- [ ] Glyph Matrix SDK統合確認
- [ ] 基本的なGlyph Toyサービス骨格作成
- [ ] プロジェクト設定最適化

### Phase 2: Core Implementation (Week 3-4)
**目標**: 動画再生エンジンとGlyph Matrix表示の基本機能実装

#### 2.1 データ管理システム
- [ ] FrameDataManagerクラス実装
- [ ] 圧縮フレームデータの読み込み機能
- [ ] メモリ効率的なフレームバッファリング
- [ ] データ完整性チェック機能

#### 2.2 動画再生エンジン
- [ ] VideoPlayerEngineクラス実装
- [ ] フレーム単位での再生制御
- [ ] 再生状態管理（再生/一時停止/停止）
- [ ] フレームレート制御機能

#### 2.3 Matrix表示機能
- [ ] MatrixRendererクラス実装
- [ ] GlyphMatrixFrameの生成・更新
- [ ] LED最適化された明度調整
- [ ] フレーム表示性能最適化

### Phase 3: User Interaction (Week 5)
**目標**: ユーザーインタラクション機能とGlyph Toy統合

#### 3.1 Glyph Button操作
- [ ] BadAppleGlyphToyServiceクラス実装
- [ ] Short Press: 再生/一時停止機能
- [ ] Long Press: 動画リセット機能
- [ ] Touch-down/Touch-up詳細制御

#### 3.2 状態管理
- [ ] 再生状態の永続化
- [ ] エラー処理・復旧機能
- [ ] ユーザーフィードバック機能

### Phase 4: Advanced Features (Week 6)
**目標**: 高度な機能とパフォーマンス最適化

#### 4.1 AOD (Always-On Display) 対応
- [ ] AODモードでの動画表示継続
- [ ] 1分間隔での自動更新実装
- [ ] バッテリー効率最適化

#### 4.2 パフォーマンス最適化
- [ ] フレーム更新遅延最小化
- [ ] メモリ使用量最適化
- [ ] CPU使用率削減対策
- [ ] バッテリー消費削減

#### 4.3 品質向上
- [ ] フレームスキップ機能
- [ ] 適応的明度調整
- [ ] 表示品質チューニング

### Phase 5: Polish & Testing (Week 7-8)
**目標**: 品質保証とリリース準備

#### 5.1 テスト実装
- [ ] Unit Testの作成
- [ ] 統合テストの実装
- [ ] パフォーマンステスト
- [ ] 長時間動作テスト

#### 5.2 UI/UX改善
- [ ] Toy設定画面実装
- [ ] プレビュー画像最適化
- [ ] エラーメッセージ改善

#### 5.3 ドキュメント整備
- [ ] READMEファイル作成
- [ ] API Documentation
- [ ] トラブルシューティングガイド

### Phase 6: Release Preparation (Week 9)
**目標**: リリース準備とデプロイメント

#### 6.1 パッケージング
- [ ] APKビルド最適化
- [ ] 署名設定
- [ ] ProGuard設定

#### 6.2 配布準備
- [ ] Google Play Console準備
- [ ] アプリストア向けメタデータ作成
- [ ] スクリーンショット・動画作成

## 技術スタック確認

### Frame Generator Tool
- **言語**: Kotlin/JVM
- **Build Tool**: Gradle
- **主要ライブラリ**:
  - JavaCV (動画処理)
  - Clikt (CLI)
  - Logback (ログ)

### Android App
- **言語**: Kotlin
- **Min SDK**: API 29 (Android 10)
- **Target SDK**: API 34 (Android 14)
- **主要ライブラリ**:
  - Glyph Matrix SDK 1.0
  - Jetpack Compose (UI)
  - Coroutines (並行処理)

## リスク管理

### 高リスク項目
1. **動画データサイズ**: 10MB以下に圧縮できるか
2. **フレームレート**: 滑らかな再生が実現できるか
3. **バッテリー消費**: 長時間動作に耐えられるか

### 対策
1. **圧縮最適化**: 複数の圧縮アルゴリズム検討
2. **適応的品質**: 動的フレームレート調整
3. **省電力モード**: AOD時の表示間隔延長

## 成功指標 (KPI)

### 技術指標
- **起動時間**: 3秒以内
- **フレーム更新遅延**: 50ms以内
- **メモリ使用量**: 100MB以内
- **APKサイズ**: 15MB以下

### ユーザー体験指標
- **認識可能性**: Bad Apple!!として認識できる
- **操作性**: 直感的なボタン操作
- **安定性**: 3分36秒完走率95%以上

## マイルストーン

### Milestone 1 (Week 2)
Frame Generator Tool完成とテストデータ生成

### Milestone 2 (Week 4)
基本的な動画再生機能完成

### Milestone 3 (Week 6)
全機能完成（AOD含む）

### Milestone 4 (Week 8)
テスト完了・品質確認完了

### Milestone 5 (Week 9)
リリース準備完了

## リソース計画

### 開発リソース
- **主担当**: 1名 (フルタイム)
- **レビュワー**: 1名 (パートタイム)

### ツール・環境
- **開発環境**: Android Studio, IntelliJ IDEA
- **テスト機**: Nothing Phone 3
- **動画素材**: Bad Apple!! オリジナル動画

### 外部依存
- **Glyph Matrix SDK**: Nothing社提供
- **JavaCV**: オープンソース動画処理ライブラリ
- **Bad Apple!! 動画**: 著作権確認済み使用

## 次のステップ

1. **Phase 1開始**: Frame Generator Tool開発着手
2. **開発環境準備**: 必要なSDK・ツールのセットアップ
3. **Bad Apple!! 動画準備**: 高品質な入力動画の入手
4. **初回ミーティング**: 開発計画詳細化

---

**最終更新**: 2025-08-30
**ドキュメントバージョン**: 1.0