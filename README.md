# Tetris Evolution — Java 俄羅斯方塊演進式教學專案

## 1. 專案名稱
**Tetris Evolution** — 一款以 Java Swing 純手工打造的俄羅斯方塊，從最陽春的 10×20 網格骨架，歷經三個階段的疊代開發，最終進化為具備 3D 立體渲染、物理粒子特效與非同步音效引擎的完整遊戲。

## 2. 遊戲簡介
Tetris Evolution 是一個以教學為導向的 Java 桌面遊戲專案。它的核心特色在於：專案本身就是一部「演進史」。透過 Git 分支與程式碼中的註解封印技術，學生可以在 Stage 1（骨架）、Stage 2（機制）、Stage 3（完全體）之間自由切換，觀察每個階段的架構差異，並深入理解一個遊戲專案是如何從零成長為完整產品的。

## 3. 遊戲玩法

### 按鍵說明
| 按鍵 | 功能 |
|------|------|
| `←` `→` | 左右移動方塊 |
| `↓` | 軟下落 (加速下落) |
| `↑` | 順時針旋轉方塊 |
| `空白鍵` | Hard Drop (瞬間落下並固化) |
| `C` | Hold (保留/交換方塊) |
| `ESC` | 暫停 / 返回主選單 |
| `Enter` | 確認選單選項 |

### 遊戲規則
- 方塊從頂部生成，玩家需操控方塊填滿水平列以消除得分。
- 支援 **T-Spin** 與 **Combo** 連擊等競技級進階計分規則。
- 提供 **Endless（無盡模式）** 與 **Level（關卡模式）** 兩種遊戲模式。

## 4. 核心功能列表

### Stage 1：基礎骨架與網格
- 10×20 二維陣列網格 (`int[][] grid`)
- 七種經典俄羅斯方塊的隨機生成 (7-bag 洗牌)
- 基礎碰撞偵測與旋轉
- 滿列消除與陽春計分 (`行數 × 100`)

### Stage 2：操作解封與進階計分
- Hold Piece（保留方塊，按 C 鍵交換）
- Hard Drop（空白鍵瞬間下落）
- Ghost Piece（底部白色線框落點預測）
- 4 格 Next Queue 預覽佇列
- T-Spin 碰撞判定與 Combo 連擊加成計分

### Stage 3：視覺與聽覺完全體
- 3D 立體 `GradientPaint` 漸層渲染與斜角高光/陰影
- 消除時的碎石物理粒子與閃電折線特效
- Hard Drop 破風金星粒子軌跡
- 戰鬥浮動文字 (T-SPIN DOUBLE!, COMBO!)
- 非同步背景音樂 (BGM) 與音效 (SFX) 引擎
- 華麗的動態主選單 UI（含呼吸燈、巨型背景方塊動畫）
- 暫停選單與 Game Over 結算互動介面

## 5. 使用技術
- **程式語言**：Java 17+
- **圖形框架**：Java AWT / Swing (`Graphics2D`, `GradientPaint`, `AlphaComposite`)
- **音效引擎**：`javax.sound.sampled` (支援 WAV 格式、自動 24-bit → 16-bit 降轉)
- **設計模式**：嚴格的 MVC (Model-View-Controller) 三層架構
- **版本控制**：Git 分支管理（main / feature/stage2-mechanics / feature/stage3-polish）

## 6. 專案架構與主要類別說明

```
tetris-demo/
├── src/tetris/
│   ├── Main.java              # 程式進入點，建立 JFrame 視窗
│   ├── GameState.java         # [Model] 遊戲狀態、分數、方塊佇列管理
│   ├── Board.java             # [Model] 10×20 網格陣列與碰撞/消除邏輯
│   ├── Tetromino.java         # [Model] 七種方塊的形狀矩陣與旋轉定義
│   ├── GamePanel.java         # [View]  畫面渲染引擎（粒子、3D渲染、選單UI）
│   ├── GameController.java    # [Controller] 遊戲主迴圈、物理更新、特效觸發
│   ├── InputController.java   # [Controller] 鍵盤事件監聽與分派
│   ├── SoundManager.java      # 非同步音效引擎（BGM迴圈/SFX重疊播放）
│   ├── Particle.java          # 粒子資料結構
│   ├── Lightning.java         # 閃電折線資料結構
│   ├── FloatingText.java      # 浮動戰鬥文字資料結構
│   └── BgPiece.java           # 主選單背景方塊動畫資料結構
├── sounds/                    # WAV 音效檔案目錄
├── docs/
│   ├── stage1.md              # Stage 1 實作紀錄
│   └── stage2.md              # Stage 2 實作紀錄
├── assets/                    # 截圖與展示素材
└── README.md                  # 本文件
```

## 7. 執行方式

### 編譯
```bash
javac -d bin src/tetris/*.java
```

### 執行
```bash
java -cp bin tetris.Main
```

> **環境需求**：Java 17 或以上版本。請確保 `sounds/` 資料夾與 `bin/` 在同一層級目錄下，以便音效引擎正確載入 WAV 檔案。

## 8. 畫面截圖

### 遊戲大廳主選單
![Menu Interface](assets/stage3-demo%281%29.png)

----

### 實際遊玩介面
![Gameplay Screen](assets/stage3-demo%282%29.png)

----

### 遊戲結算畫面
![Game Over Screen](assets/stage3-demo%283%29.png)

## 9. 組員分工
本專案由**個人獨立完成**，涵蓋需求分析、架構設計、程式實作、視覺特效開發、音效整合與文件撰寫等所有環節。

## 10. 開發過程中的主要困難與解法

### 困難一：音效的非同步播放與資源競態
**問題**：在 `javax.sound.sampled` 中，`Clip.open()` 與 `Clip.start()` 是阻塞式呼叫。若在遊戲主迴圈中直接執行，會導致方塊掉落卡頓。
**解法**：將所有音效播放邏輯封裝進獨立的 `Thread`，並使用 `LineListener` 監聽 `STOP` 事件來自動回收 `Clip` 資源，避免記憶體洩漏。

### 困難二：MVC 架構的嚴格分離
**問題**：開發初期，渲染邏輯（如粒子座標計算）容易與 Controller 的物理更新混在一起。
**解法**：將所有粒子、閃電、浮動文字的資料結構存放在 `GameState`（Model），由 `GameController` 負責更新座標與生命週期，`GamePanel` 僅負責根據資料繪製，徹底實現單向資料流。

### 困難三：24-bit WAV 格式相容性
**問題**：部分音效檔案為 24-bit 編碼，Java 的 `AudioSystem` 無法直接開啟，會拋出 `UnsupportedAudioFileException`。
**解法**：在 `SoundManager` 中實作了 `getSafeStream()` 方法，自動偵測音訊位元深度，並在必要時將 24-bit/32-bit 格式即時轉換為 16-bit PCM_SIGNED。

## 11. 與 AI 協作的心得

本專案的一大特色是：它並非從零開始撰寫，而是透過 **AI 協助進行「逆向拆解」**。

我們先完成了 v1.0 的完全體，再要求 AI 用 `/* ... */` 區塊註解的方式，將專案安全地「退版」回 Stage 1 的基礎骨架。這個過程中有幾個重要的學習：

1. **AI 的重構品味**：在處理音效模組的高耦合時，AI 提出了一個非常優雅的策略——不去修改幾十處 `playSFX()` 的呼叫端，而是直接將 `SoundManager` 內部的方法實作註解掉，使其成為無副作用的空殼。這展示了介面抽象化的思維。
2. **AI 也會犯錯**：一開始 AI 沒看清楚路徑，把主專案 `tetris` 的檔案改了（而非 `tetris-demo`）。事後它自己用 `git restore` 復原，雖然有自首，但這提醒我們：AI 的輸出必須經過人類驗證。
3. **精準解封的挑戰**：在 Stage 2 解封時，我們要求 AI 只恢復「操作機制」而保持「視覺特效」封印。這需要 AI 在同一個函式中精確地區分哪些行屬於機制、哪些行屬於特效，展現了對程式碼語意的深層理解。

## 12. 未來可擴充方向
- **網路對戰模式**：透過 Socket 實現雙人即時對戰，互相發送垃圾行。
- **自訂主題皮膚**：允許玩家切換方塊的色彩主題與背景風格。
- **排行榜系統**：將分數寫入本地或遠端資料庫，建立全域排行榜。
- **觸控/手把支援**：擴充 `InputController` 以支援遊戲手把或觸控螢幕操作。
- **回放系統**：記錄每一步操作，支援遊戲回放與教學分析。