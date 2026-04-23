# Tetris Engine - Architecture Notes & Bug Log

這份文件用於紀錄 Tetris 專案開發過程中遭遇的重大架構問題、Bug 分析以及除錯經驗，以確保未來的重構與擴充能避開相同的潛在陷阱。

## 核心易混淆機制 (Core Mechanisms)

### 1. 方塊固化 (`finalizePieceAndSpawnNext()`) vs 單純生成 (`spawnNewPiece()`)
在 `GameController.java` 中，這兩個方法的用途有嚴格的界線，絕不可混用：
- **`finalizePieceAndSpawnNext()`**：
  - 核心功能：包含 `board.placeTetromino(...)`。
  - 作用：會真正將目前的懸空方塊「**用水泥砌死**」寫入 2D 陣列矩陣中，並啟動滿行掃描與粒子特效。
  - 適用場景：`moveDown()` 觸底且超出 Lock Delay 時，或是啟動 `hardDrop()` 瞬間砸底後。
- **`spawnNewPiece()`**：
  - 核心功能：僅控制 `gameState.swapToNextPiece()`。
  - 作用：單純從 Multi-Queue 中抽出下一個方塊，並重新設定 X, Y 座標至螢幕頂部。
  - 適用場景：`hold()` 進行方塊對調時，或是初始化遊戲第一顆方塊時。

---

## 重大 Bug 修復紀錄

### [Resolved] Bug: 懸空方塊殘留 (Phase 11.6 - Hold Bug)
- **觸發條件**：玩家初次按下 `C` 鍵嘗試觸發 Hold (保留方塊) 時。
- **症狀表現**：當前的方塊會被瞬間「固化」並殘留在原地的半空中畫面，不自然地與正在下落的新方塊並存。
- **根本原因 (Root Cause)**：
  在使用程式碼輔助替換 (Fuzzy Match Replace) 時，遇到了上下文字串過於相似的問題。原本要針對 `spawnNewPiece()` 進行 `stopTimer` 邏輯互換，卻意外將 `hold()` 內部的呼叫，從 `spawnNewPiece()` 誤判替換成了 `finalizePieceAndSpawnNext()`。
  這導致系統在執行 Hold 的瞬間，強制執行了上段所述的 `board.placeTetromino`，直接把方塊定死在半空。
- **修復方式**：
  精確還原這兩處的邏輯。將 `hold()` 方法內的 `held == null` 判斷分支調回純粹的 `spawnNewPiece()`。
- **教訓 (Lesson Learned)**：
  執行程式碼區塊取代時，特別是處理 `if (!getBoard().canMove(...))` 這種在多個方法中都會出現的**通用碰撞邊界檢測**，必須嚴格宣告 TargetContext，避免修改範圍溢出至非預期的方法內。

---

## Phase 12 架構備忘

### 2. SoundManager 非同步設計
- **SFX**：每次 `playSFX()` 都啟動獨立 Thread 並新建 Clip，天然支援多重重疊播放 (Polyphony)。Clip 播放完畢後透過 `LineListener` 自動 close 回收資源。
- **BGM**：單一 Clip + `LOOP_CONTINUOUSLY`。呼叫 `playBGM()` 前會自動 `stopBGM()`。
- **容錯**：所有 I/O 操作都在 try-catch 中，檔案不存在僅 `System.out.println("Play Sound: ...")`。
- **呼叫時機**：所有 SFX 呼叫皆位於 `GameController`，不在 View 或 Model 層，維持 MVC 清潔。

### 3. Menu 粒子獨立生命週期
- `menuParticles` 是獨立於遊戲粒子 (`particles`) 的 List，只在 `MENU` 狀態下由 `animationTimer` 更新與補充。
- 進入 `PLAYING` 時 `startOrRestartGame()` 會呼叫 `getMenuParticles().clear()` 完全清除。
- 這確保了選單粒子不會干擾遊戲中的特效系統。
