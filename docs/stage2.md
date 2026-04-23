# Stage 2 實作紀錄：操作解封與進階計分

## (1) 本階段目標
本階段的目標是替原本純粹、陽春的 10x20 網格加入現代化 Tetris 的核心操作機制與競技級進階計分系統，讓遊戲體驗從「能玩」進化到「流暢且具備深度」，但同時依然保持平面的視覺風格，暫不引入過於華麗的特效。

## (2) 本階段完成內容
- **實作 Hold Piece (保留方塊)**：玩家按下 `C` 鍵可將當前方塊存入左側暫存區，或與已暫存的方塊互換。
- **實作 Hard Drop (瞬間重擊)**：按下 `空白鍵`，方塊將無延遲地瞬間落下並固化，大幅提升遊戲節奏。
- **實作 Ghost Piece (落點預測)**：在網格底部以白色空心線框繪製當前方塊的預計落點，幫助玩家精準定位。
- **擴展 Next Queue**：將右側的下一個方塊預覽數量從 1 個擴充為 4 個。
- **進階計分機制**：解封了 T-Spin (T型旋轉) 的碰撞判定與 Combo 連擊加成，使得高階技巧能獲得對應的豐厚分數獎勵。

## (3) 修改的主要 class / module
- `InputController.java`：重新解封 `VK_SPACE` (Hard Drop) 與 `VK_C` (Hold) 的按鍵事件綁定。
- `GameState.java`：將 `nextPieces` 的預覽數量從 1 改回 4，並恢復了 `addAdvancedScore()` 內的 T-Spin 與 Combo 分數倍率運算。
- `GameController.java`：解開 `hold()` 與 `hardDrop()` 的主要邏輯區塊，並在消行結算時恢復呼叫 T-Spin 判定。但特別保留了內部關於 Particle 與 Sound 的註解封印。
- `GamePanel.java`：在 `paintComponent()` 中解封 `drawGhostPiece()` 呼叫，並恢復 `drawLeftPanel()` 中的 Hold 視窗與文字繪製。

## (4) 實作說明

### Hold 系統切換機制
`GameController` 內的 `hold()` 函式加入了 Debounce (防抖) 設計避免連續觸發。切換邏輯如下：
1. 若 `HoldPiece` 為空：將當前 `CurrentPiece` 存入，並從 `NextQueue` 生成新方塊。
2. 若 `HoldPiece` 已有方塊：將 `CurrentPiece` 存入，並將原先的 `HoldPiece` 座標重置到頂部中央成為新的操作方塊。
每次成功 Hold 後，會將 `canHold` 標記設為 `false`，直到該方塊固化落地才能再次使用。

### Ghost Piece 落點計算
`GamePanel` 的 `drawGhostPiece()` 會複製一份當前方塊的座標作為 `ghostY`。接著透過一個 `while` 迴圈持續呼叫 `board.canMove(ghostX, ghostY + 1, piece)` 進行虛擬下落測試，直到偵測到碰撞為止。最後在計算出的極限 `ghostY` 位置繪製外框。

## (5) 問題與除錯

**遇到的小狀況：落點預測殘影與實體方塊難以區分？**
在解開 `drawGhostPiece` 時，我們遇到了一個視覺問題：因為我們目前處於 Stage 2，3D 的 `GradientPaint` 漸層與高光都被我們封印了，實體方塊只是純色的 `fillRect`。如果殘影也用實心填色，加上透明度設定不佳，在快速下落時很容易跟原本的方塊混在一起。

**思考與解決方案**：
這時候才發現原本 `GamePanel` 的程式設計非常有遠見！我們不需要重新寫繪圖邏輯，只要觀察 `drawBlock` 方法，它有一個 `boolean fill` 參數。在繪製 Ghost Piece 時，我們故意傳入 `false`，這樣它就會跳過純色填充，只執行 `g.drawRect` 畫出一個白色的細線空心外框。在沒有 3D 特效的純平面 Stage 2 中，空心白框反而是最完美、最不干擾視覺的殘影解法！

## (6) AI 協作紀錄

這次解封 Stage 2 的任務非常有挑戰性，因為這不是「全部解開」，而是「精準的外科手術式解封」。

- **精準解封操作，維持特效封印**：我們要求 AI 在解開 `hardDrop()` 時，必須小心翼翼地把裡面的「破風金星特效 (Particles)」迴圈繼續保留在註解狀態。同樣地，在解開 T-Spin 分數時，也必須把會跳出 "T-SPIN DOUBLE!" 的 Floating Text 與音效保持封印。
- **AI 的細心發揮**：AI 這次展現了極高的穩定度。它精準地挑出了 `InputController`、`GameState`、`GameController` 與 `GamePanel` 中與「機制」相關的程式碼進行還原，而完全沒有動到 Stage 3 的選單或 3D 渲染區塊。
- 整個過程一次到位，編譯同樣保持 0 錯誤，完美實現了「操作進化，視覺維持陽春」的過渡期版本。

## (7) 下階段預計工作
目前遊戲的核心機制已經 100% 完整，手感也達到了現代 Tetris 的標準。
在最終的 **Stage 3**，我們將全面解除視覺與聽覺的封印限制：
1. 啟動 `SoundManager`，補上 BGM 與打擊音效。
2. 恢復華麗的 3D 立體 `GradientPaint` 渲染。
3. 實裝所有的消除碎石、閃電與金星粒子特效。
4. 恢復浮動戰鬥文字 (Floating Text) 與動態背景主選單 (Menu)。
