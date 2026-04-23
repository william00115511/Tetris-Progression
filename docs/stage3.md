# Stage 3 實作紀錄：視覺與聽覺完全體

## (1) 本階段目標
本階段的目標是將 Tetris 從一個機制完整但視覺素雅的版本，全面解封為具備 3D 立體渲染、物理粒子特效、戰鬥浮動文字與非同步音效引擎的最終完全體。讓遊戲的視聽體驗達到「令人驚艷」的水準。

## (2) 本階段完成內容
- **恢復 SoundManager 實體播放邏輯**：所有 `playBGM`, `stopBGM`, `pauseBGM`, `resumeBGM`, `playSFX` 方法的內部邏輯已全面解封，遊戲重新擁有完整的 BGM 背景音樂迴圈與 SFX 音效重疊播放能力。
- **恢復 3D 立體渲染**：`drawBlock` 與 `drawPreviewPiece` 中的 `GradientPaint` 漸層、高光與陰影邊框已全面恢復，方塊重新具備立體質感。
- **恢復粒子與閃電特效**：消除時的碎石粒子 (`Particle`)、閃電折線 (`Lightning`)、Hard Drop 破風金星，以及戰鬥浮動文字 (`FloatingText`) 皆已全數復活。
- **恢復動態主選單 UI**：含呼吸燈文字、巨型背景方塊緩落動畫、暫停/結束的互動式選項陣列。
- **恢復所有按鍵**：`VK_ESCAPE` (暫停) 與 `VK_ENTER` (確認) 已解封。

## (3) 修改的主要 class / module
- `SoundManager.java`：移除了 `playBGM`, `stopBGM`, `pauseBGM`, `resumeBGM`, `playSFX` 五大方法的 `/* ... */` 註解封印。
- `GameState.java`：將初始狀態從 `State.PLAYING` 改回 `State.MENU`。
- `GameController.java`：恢復了 `animationTimer` 的完整 33ms 特效更新迴圈，以及 `start()` 中的 BGM 播放呼叫。恢復了消行結算中所有的 Floating Text 與 SFX 觸發。恢復了 Hard Drop 的金星粒子特效。
- `GamePanel.java`：恢復了 `drawDropTrail`, `drawParticles`, `drawFloatingTexts`, `drawLevelUpTrigger` 的呼叫。恢復了 `drawBlock` 與 `drawPreviewPiece` 中的 3D 渲染。恢復了 `drawOverlay` 中的互動式暫停/結束選單。
- `InputController.java`：解封了 `VK_ESCAPE` 與 `VK_ENTER` 的按鍵監聽。

## (4) 實作說明

### 音效引擎架構
`SoundManager` 採用非同步 `Thread` 設計。BGM 使用單一 `Clip` 搭配 `LOOP_CONTINUOUSLY` 實現無限迴圈；SFX 則每次呼叫都開啟新的 `Clip`，天然支援多重重疊播放 (Polyphony)。播放完畢後透過 `LineListener` 監聽 `STOP` 事件自動釋放資源。

### 3D 渲染原理
`drawBlock` 使用 `GradientPaint` 從左上到右下繪製由亮到暗的漸層，模擬光源照射。接著在上方與左方疊加半透明白色矩形 (高光)，下方與右方疊加半透明黑色矩形 (陰影)，產生斜角立體效果。

## (5) 問題與除錯

**遇到的問題：解封後的大括號配對錯位**
在全面解封 `drawPreviewPiece` 的 3D 渲染程式碼時，有一個 `if (!isGrayedOut) {` 區塊的閉合大括號在之前的封印過程中被不小心吞掉了。因為在 Stage 1 封印時，我們把整個高光/陰影的 `if` 區塊連同它的閉合括號一起包進了 `/* ... */`，解開時需要格外留意括號的配對是否完整。

**解決方案**：
在編譯前仔細檢查了每一個被解封的區塊，確認所有 `{` 與 `}` 都正確配對。最終手動補回了遺失的閉合括號，編譯順利通過。

## (6) AI 協作紀錄

Stage 3 的解封相對直覺——因為這次是「全部打開」，不需要像 Stage 2 那樣精挑細選。但仍然有幾個值得記錄的細節：

- **SoundManager 的乾淨解封**：AI 精準地移除了五個方法中的 `/* Stage 1: Disable Audio` 與 `*/` 標記，將它們替換為 `// Stage 3: Restore Audio` 的語意化註釋，方便未來回溯。
- **AnimationTimer 的完整復原**：這是整個專案中最大的單一註解區塊（約 95 行），AI 成功地將它從 `/* ... */` 改回活躍程式碼，並在復原過程中發現並修正了一個重複的 `animationTimer.start()` 呼叫。
- **自動排查大括號**：AI 在解封 `drawPreviewPiece` 的 3D 渲染時，主動檢查了括號的完整性，並補回了遺失的閉合括號，避免了編譯錯誤。

## (7) 下階段預計工作
本專案的三階段教學演進已全部完成。未來可考慮的擴充方向包括：
1. 網路對戰模式（Socket 雙人即時對戰）。
2. 自訂主題皮膚系統。
3. 排行榜與回放系統。
4. 遊戲手把支援。
