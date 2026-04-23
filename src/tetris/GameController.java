package tetris;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameController implements ActionListener {
    private GameState gameState;
    private GamePanel gamePanel;
    private Timer timer;
    private Timer animationTimer;
    private SoundManager soundManager;
    
    // Phase 14.5: Debounce for keyboard IME fallback
    private long lastHardDropTime = 0;
    private long lastHoldTime = 0;

    public GameController(GameState gameState, GamePanel gamePanel) {
        this.gameState = gameState;
        this.gamePanel = gamePanel;
        this.soundManager = new SoundManager();

        this.timer = new Timer(500, this);

        // Phase 9.5: 獨立特效渲染循環 (33ms)，現在負責雙粒子路徑與閃電折線
        /* Stage 1: Disable Animation Timer
        this.animationTimer = new Timer(33, e -> {
            boolean needsRepaint = false;
            java.util.Random rand = new java.util.Random();
            
            // 粒子更新
            java.util.Iterator<Particle> it = this.gameState.getParticles().iterator();
            while (it.hasNext()) {
                Particle p = it.next();
                p.x += p.vx;
                p.y += p.vy;
                
                // Type 1 電火花的專屬隨機暴衝軌跡
                if (p.type == 1) {
                    p.x += rand.nextFloat() * 8 - 4;
                    p.y += rand.nextFloat() * 8 - 4;
                }
                
                p.life--;
                if (p.life <= 0) it.remove();
                needsRepaint = true; 
            }
            
            // 閃電更新
            java.util.Iterator<Lightning> lit = this.gameState.getLightnings().iterator();
            while (lit.hasNext()) {
                Lightning l = lit.next();
                l.life--;
                if (l.life <= 0) lit.remove();
                needsRepaint = true;
            }
            
            // 戰鬥文字特效更新
            java.util.Iterator<FloatingText> fit = this.gameState.getFloatingTexts().iterator();
            while (fit.hasNext()) {
                FloatingText ft = fit.next();
                ft.y -= 1.0f; // 向上飄移
                ft.life--;
                if (ft.life <= 0) fit.remove();
                needsRepaint = true;
            }
            if (this.gameState.isDisplayingLevelUp()) {
                needsRepaint = true;
            }
            // Phase 12: 主選單動畫需要持續重繪以驅動呼吸燈與背景粒子
            if (gameState.getCurrentState() == GameState.State.MENU) {
                if (Math.random() < 0.05) {
                    gameState.getMenuParticles().add(new Particle(
                        3, (float)(Math.random() * 800), (float)(Math.random() * 800),
                        (float)((Math.random() - 0.5) * 1), (float)((Math.random() - 0.5) * 1),
                        new java.awt.Color(50, 150, 255, 100),
                        60 + (int)(Math.random() * 40)
                    ));
                }

                // Phase 14 bgPieces 動畫
                for (BgPiece p : gameState.getBgPieces()) {
                    p.y += p.dy;
                    p.rotation += p.rotSpeed;
                    if (p.y > 900) {
                        p.y = -200;
                        p.x = Math.random() * 1000;
                    }
                }

                // 更新選單背景粒子
                java.util.Iterator<Particle> menuIt = this.gameState.getMenuParticles().iterator();
                while (menuIt.hasNext()) {
                    Particle mp = menuIt.next();
                    mp.x += mp.vx;
                    mp.y += mp.vy;
                    mp.life--;
                    if (mp.life <= 0) menuIt.remove();
                }
                // 持續補充粒子（維持約 15~25 顆在畫面上緩飄）
                if (this.gameState.getMenuParticles().size() < 20 && rand.nextInt(3) == 0) {
                    int panelW = (6 + this.gameState.getColumns() + 6) * 30;
                    int panelH = this.gameState.getRows() * 30;
                    float px = rand.nextInt(panelW);
                    float py = rand.nextInt(panelH);
                    float vx = rand.nextFloat() * 0.6f - 0.3f;
                    float vy = -0.2f - rand.nextFloat() * 0.3f;
                    int life = 80 + rand.nextInt(60);
                    this.gameState.getMenuParticles().add(
                        new Particle(3, px, py, vx, vy, new java.awt.Color(80, 80, 120, 100), life)
                    );
                }
                needsRepaint = true;
            }
            if (needsRepaint) {
                this.gamePanel.repaint();
            }
        });
        this.animationTimer.start();
        */
    }

    public void start() {
        // Stage 1: Jump right into Endless level
        startGameSegment(GameState.GameMode.ENDLESS);
        timer.start();
        // soundManager.playBGM("bgm_menu.wav"); // Phase 12.5: 啟動時播放選單 BGM
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState.getCurrentState() != GameState.State.PLAYING) {
            return;
        }

        Board board = gameState.getBoard();
        Tetromino currentPiece = gameState.getCurrentPiece();
        int currentX = gameState.getCurrentX();
        int currentY = gameState.getCurrentY();

        if (board.canMove(currentX, currentY + 1, currentPiece)) {
            gameState.setCurrentY(currentY + 1);
            gameState.setLastMoveRotate(false); // Phase 10: 掉落視為一般移動，解除旋轉狀態
            gameState.setLockWaiting(false);
        } else {
            if (gameState.isLockWaiting()) {
                gameState.setLockWaiting(false);
                finalizePieceAndSpawnNext();
            } else {
                gameState.setLockWaiting(true);
            }
        }

        gamePanel.repaint();
    }

    // Phase 10: 檢查 T-Spin 的四角覆蓋率
    private boolean checkTSpin() {
        if (!gameState.isLastMoveRotate()) return false;
        Tetromino piece = gameState.getCurrentPiece();
        if (piece.getColorCode() != 6) return false; // 紫色 T 方形
        
        int x = gameState.getCurrentX();
        int y = gameState.getCurrentY();
        Board board = gameState.getBoard();
        int cols = board.getColumns();
        int rows = board.getRows();
        
        int cornerCount = 0;
        int[][] corners = {{0, 0}, {0, 2}, {2, 0}, {2, 2}};
        for (int[] c : corners) {
            int cx = x + c[1];
            int cy = y + c[0];
            if (cx < 0 || cx >= cols || cy >= rows) {
                cornerCount++;
            } else if (cy >= 0 && board.getGrid()[cy][cx] != 0) {
                cornerCount++;
            }
        }
        return cornerCount >= 3;
    }

    private void finalizePieceAndSpawnNext() {
        Board board = gameState.getBoard();

        board.placeTetromino(gameState.getCurrentX(), gameState.getCurrentY(), gameState.getCurrentPiece());

        // Phase 9: 預掃描粒子
        /* Stage 1: Disable Particles
        int cols = gameState.getColumns();
        int rows = gameState.getRows();
        int[][] grid = board.getGrid();
        for (int y = 0; y < rows; y++) {
            boolean full = true;
            for (int x = 0; x < cols; x++) {
                if (grid[y][x] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                spawnParticles(y);
            }
        }
        */

        // Phase 10: T-Spin 與 Combo 結算
        // boolean isTSpin = checkTSpin();
        int clearedLines = board.checkAndClearLines();
        
        if (clearedLines > 0) {
            /* Stage 1: Disable Advanced Score Multipliers
            gameState.setComboCount(gameState.getComboCount() + 1);
            gameState.addAdvancedScore(clearedLines, isTSpin, gameState.getComboCount() - 1);

            // Phase 12: 消行音效
            soundManager.playSFX(clearedLines >= 4 ? "tetris" : "line_clear");

            if (isTSpin) {
                String tText = "T-SPIN";
                if(clearedLines == 1) tText = "T-SPIN SINGLE";
                if(clearedLines == 2) tText = "T-SPIN DOUBLE!";
                if(clearedLines == 3) tText = "T-SPIN TRIPLE!";
                gameState.getFloatingTexts().add(new FloatingText(tText,
                    gameState.getCurrentX() * 30 - 30, 
                    gameState.getCurrentY() * 30, 
                    new java.awt.Color(230, 0, 255), 45));
                soundManager.playSFX("tspin"); // Phase 12
            }

            if (gameState.getComboCount() > 1) {
                gameState.getFloatingTexts().add(new FloatingText(gameState.getComboCount() + " COMBO!",
                    gameState.getCurrentX() * 30 - 20,
                    (gameState.getCurrentY() * 30) - 30, 
                    java.awt.Color.ORANGE, 45));
                soundManager.playSFX("combo"); // Phase 12
            }
            
            gameState.setTotalLinesCleared(gameState.getTotalLinesCleared() + clearedLines);

            if (gameState.getCurrentMode() == GameState.GameMode.LEVEL) {
                int newLevel = (gameState.getTotalLinesCleared() / 10) + 1;
                if (newLevel > gameState.getLevel()) {
                    gameState.setLevel(newLevel);

                    // Phase 9: 更大膽的難度提升曲線 Math.max(50, 500 - (L-1)*60)
                    int newDelay = Math.max(50, 500 - ((newLevel - 1) * 60));
                    timer.setDelay(newDelay);

                    // 引發視覺回饋！
                    gameState.triggerLevelUpDisplay();
                    soundManager.playSFX("level_up"); // Phase 12
                }
            }
            */
            gameState.addAdvancedScore(clearedLines, false, 0);
        } else {
            /* Stage 1: Disable TSpin visual
            // Phase 10: 無消行，Combo 歸零。但若是純 T-Spin 依然有短暫提示加分
            gameState.setComboCount(0);
            if (isTSpin) {
                gameState.getFloatingTexts().add(new FloatingText("T-SPIN",
                    gameState.getCurrentX() * 30 - 10, 
                    gameState.getCurrentY() * 30, 
                    new java.awt.Color(230, 0, 255), 30));
            }
            */
        }

        gameState.setCanHold(true);
        spawnNewPiece();
    }

    private void spawnParticles(int gridY) {
        java.util.Random rand = new java.util.Random();
        
        // Phase 9.5: 產生橫向貫穿的暴力閃電 (生命週期極短 5~8 個 frame，約 0.2 秒)
        gameState.getLightnings().add(new Lightning(gridY, 5 + rand.nextInt(4)));
        
        for (int x = 0; x < gameState.getColumns(); x++) {
            
            // Type 0: 粉末煙霧 - 模擬石材碎裂，平緩發散，顏色偏白灰
            for (int i = 0; i < 4; i++) {
                float px = (float)(x * 30 + rand.nextInt(30));
                float py = (float)(gridY * 30 + rand.nextInt(30));
                float vx = rand.nextFloat() * 8 - 4; // 溫和水平飄散
                float vy = rand.nextFloat() * 4 - 2;
                
                gameState.getParticles().add(
                    new Particle(0, px, py, vx, vy, new java.awt.Color(220, 220, 230, 180), 25 + rand.nextInt(15))
                );
            }
            
            // Type 1: 電火花 - 極致狂暴的速度，亮青色或極具穿透感的高能亮白
            if (rand.nextFloat() < 0.6f) { // 不是每一格都有，但發生率很高
                float px = (float)(x * 30 + 15);
                float py = (float)(gridY * 30 + 15);
                float vx = rand.nextFloat() * 30 - 15; // 誇張的爆炸初速
                float vy = rand.nextFloat() * 30 - 15;
                
                java.awt.Color sparkColor = rand.nextBoolean() ? java.awt.Color.CYAN : java.awt.Color.WHITE;
                gameState.getParticles().add(
                    new Particle(1, px, py, vx, vy, sparkColor, 10 + rand.nextInt(10))
                );
            }
        }
    }

    private void spawnNewPiece() {
        gameState.swapToNextPiece();
        Tetromino newPiece = gameState.getCurrentPiece();

        int startX = (gameState.getBoard().getColumns() / 2) - (newPiece.getWidth() / 2);
        int startY = 0;

        gameState.setCurrentX(startX);
        gameState.setCurrentY(startY);

        if (!gameState.getBoard().canMove(startX, startY, newPiece)) {
            gameState.stopTimer(); // Phase 11.6: 提早紀錄死亡時間避免 STATE 干擾
            gameState.setCurrentState(GameState.State.GAME_OVER);
            soundManager.stopBGM(); // Phase 12.5: 死亡時靜默背景音樂
            soundManager.playSFX("game_over"); // Phase 12
            System.out.println(">>> 遊戲結束！您的最終分數為: " + gameState.getScore());

            // Phase 8 & 9.6: 刷新並強制存檔實體本地檔案，確保判定符合當下模式屬性！
            if (gameState.getCurrentMode() == GameState.GameMode.LEVEL) {
                if (gameState.getLevel() > gameState.getSessionHighScore()) {
                    gameState.setSessionHighScore(gameState.getLevel());
                    ScoreManager.saveScore(gameState.getCurrentMode(), gameState.getLevel());
                }
            } else {
                if (gameState.getScore() > gameState.getSessionHighScore()) {
                    gameState.setSessionHighScore(gameState.getScore());
                    ScoreManager.saveScore(gameState.getCurrentMode(), gameState.getScore());
                }
            }
        }
    }

    // ==========================================
    // Lifecycle 週期系統與選單控制器
    // ==========================================

    /**
     * Phase 8: 接收從 Input 指定模式進入新一局
     */
    public void startGameSegment(GameState.GameMode mode) {
        // ... (依舊由外部管理)
        gameState.setCurrentMode(mode);
        gameState.setTotalLinesCleared(0);
        gameState.setLevel(1);
        gameState.setSessionHighScore(ScoreManager.loadScore(mode));

        // Timer 強制重置回穩定頻度
        timer.setDelay(500);

        startOrRestartGame();
    }

    public void enterPressed() {
        GameState.State state = gameState.getCurrentState();
        if (state == GameState.State.MENU) {
            soundManager.playSFX("hard_drop");
            if (gameState.getCurrentSubMenu() == GameState.SubMenuState.MAIN) {
                int idx = gameState.getMenuSelectionIndex();
                if (idx == 0) startGameSegment(GameState.GameMode.ENDLESS);
                else if (idx == 1) startGameSegment(GameState.GameMode.LEVEL);
                else if (idx == 2) {
                    gameState.setCurrentSubMenu(GameState.SubMenuState.CONTROLS);
                } else if (idx == 3) {
                    gameState.setCurrentSubMenu(GameState.SubMenuState.SETTINGS);
                    gameState.setMenuSelectionIndex(0); // Reset for settings
                } else if (idx == 4) {
                    System.exit(0);
                }
            } else if (gameState.getCurrentSubMenu() == GameState.SubMenuState.SETTINGS) {
                if (gameState.getMenuSelectionIndex() == 2) { // Back
                    gameState.setCurrentSubMenu(GameState.SubMenuState.MAIN);
                    gameState.setMenuSelectionIndex(3); // return cursor to settings
                }
            } else if (gameState.getCurrentSubMenu() == GameState.SubMenuState.CONTROLS) {
                gameState.setCurrentSubMenu(GameState.SubMenuState.MAIN);
                gameState.setMenuSelectionIndex(2); // return cursor to controls
            }
        } else if (state == GameState.State.PAUSED) {
            soundManager.playSFX("hard_drop");
            int idx = gameState.getPauseSelectionIndex();
            if (idx == 0) {
                togglePause(); // Resume
            } else if (idx == 1) {
                startGameSegment(gameState.getCurrentMode()); // Restart
            } else if (idx == 2) {
                gameState.setCurrentState(GameState.State.MENU); // Main Menu
                gameState.setCurrentSubMenu(GameState.SubMenuState.MAIN);
                timer.stop();
                soundManager.playBGM("bgm_menu.wav");
            }
        } else if (state == GameState.State.GAME_OVER) {
            soundManager.playSFX("hard_drop");
            int idx = gameState.getGameOverSelectionIndex();
            if (idx == 0) {
                startGameSegment(gameState.getCurrentMode()); // Retry
            } else if (idx == 1) {
                gameState.setCurrentState(GameState.State.MENU); // Main Menu
                gameState.setCurrentSubMenu(GameState.SubMenuState.MAIN);
                timer.stop();
                soundManager.playBGM("bgm_menu.wav");
            }
        }
        gamePanel.repaint();
    }

    private void startOrRestartGame() {
        gameState.getBoard().clear();
        gameState.setScore(0);
        
        // Phase 9.6, 10 & 11.6: 重開局時掃光殘留特效
        gameState.getParticles().clear();
        gameState.getLightnings().clear();
        gameState.getFloatingTexts().clear();
        gameState.setComboCount(0);
        gameState.setLastMoveRotate(false);

        gameState.setHoldPiece(null);
        gameState.setCanHold(true);
        gameState.setLockWaiting(false);

        // Phase 11: Queue & Timer Initialization
        gameState.getNextPieces().clear();
        for (int i = 0; i < 4; i++) {
            gameState.getNextPieces().add(new Tetromino(gameState.getNextFromBag()));
        }
        
        gameState.startTimer();
        gameState.getMenuParticles().clear(); // Phase 12: 清空選單殘留粒子

        gameState.setCurrentState(GameState.State.PLAYING);
        spawnNewPiece();
        soundManager.playSFX("game_start"); // Phase 12

        // Phase 12.5: 根據模式切換對應 BGM
        if (gameState.getCurrentMode() == GameState.GameMode.ENDLESS) {
            soundManager.playBGM("bgm_endless.wav");
        } else {
            soundManager.playBGM("bgm_level.wav");
        }

        if (!timer.isRunning()) {
            timer.start();
        }
        gamePanel.repaint();
    }

    // ==========================================
    // Phase 6 / 7 : 旋轉防壁與瞬降系統保留
    // ==========================================

    public void hold() {
        /* Stage 1: Disable Hold
        if (System.currentTimeMillis() - lastHoldTime < 100) return; // Debounce
        lastHoldTime = System.currentTimeMillis();

        soundManager.playSFX("hold"); // Phase 12
        if (gameState.getCurrentState() != GameState.State.PLAYING)
            return;
        if (!gameState.isCanHold())
            return;

        Tetromino current = gameState.getCurrentPiece();
        Tetromino held = gameState.getHoldPiece();

        gameState.setHoldPiece(current);

        if (held == null) {
            spawnNewPiece();
        } else {
            gameState.setCurrentPiece(held);

            int startX = (gameState.getBoard().getColumns() / 2) - (held.getWidth() / 2);
            int startY = 0;
            gameState.setCurrentX(startX);
            gameState.setCurrentY(startY);

            if (!gameState.getBoard().canMove(startX, startY, held)) {
                gameState.stopTimer();
                gameState.setCurrentState(GameState.State.GAME_OVER);
            }
        }

        gameState.setLockWaiting(false);
        gameState.setCanHold(false);
        gamePanel.repaint();
        */
    }

    // ==========================================
    // Phase 5 : Hard Drop 瞬間下落固化
    // ==========================================

    public void hardDrop() {
        /* Stage 1: Disable HardDrop
        if (System.currentTimeMillis() - lastHardDropTime < 100) return; // Debounce
        lastHardDropTime = System.currentTimeMillis();

        soundManager.playSFX("hard_drop"); // Phase 12
        if (gameState.getCurrentState() != GameState.State.PLAYING)
            return;

        Board board = gameState.getBoard();
        Tetromino currentPiece = gameState.getCurrentPiece();
        int currentX = gameState.getCurrentX();
        int dropY = gameState.getCurrentY();

        while (board.canMove(currentX, dropY + 1, currentPiece)) {
            dropY++;
        }

        if (dropY != gameState.getCurrentY()) {
            gameState.setLastMoveRotate(false); 
        }

        // Phase 11.6: 破風金星特效取代舊光柱
        int[][] shape = currentPiece.getShape();
        int minC = shape[0].length;
        int maxC = -1;
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[0].length; c++) {
                if (shape[r][c] != 0) {
                    if (c < minC) minC = c;
                    if (c > maxC) maxC = c;
                }
            }
        }
        if (maxC >= minC) {
            int effX = currentX + minC;
            int effW = (maxC - minC + 1);
            int startY = gameState.getCurrentY();
            java.awt.Color goldColor = new java.awt.Color(255, 230, 150);
            java.util.Random rand = new java.util.Random();
            
            for (int y = startY; y <= dropY; y++) {
                // 左側邊緣飛散
                for (int p = 0; p < 2; p++) {
                    float vx = -1.5f - rand.nextFloat() * 2.0f;
                    float vy = -0.5f + rand.nextFloat();
                    int maxLife = 10 + rand.nextInt(6);
                    gameState.getParticles().add(new Particle(2, effX * 30, y * 30 + rand.nextInt(30), vx, vy, goldColor, maxLife));
                }
                // 右側邊緣飛散
                for (int p = 0; p < 2; p++) {
                    float vx = 1.5f + rand.nextFloat() * 2.0f;
                    float vy = -0.5f + rand.nextFloat();
                    int maxLife = 10 + rand.nextInt(6);
                    gameState.getParticles().add(new Particle(2, (effX + effW) * 30, y * 30 + rand.nextInt(30), vx, vy, goldColor, maxLife));
                }
            }
        }

        gameState.setCurrentY(dropY);

        gameState.setLockWaiting(false);
        finalizePieceAndSpawnNext();
        gamePanel.repaint();
        */
    }

    public void leftPressed() {
        if (gameState.getCurrentState() == GameState.State.PLAYING) {
            moveLeft();
        } else if (gameState.getCurrentState() == GameState.State.MENU && gameState.getCurrentSubMenu() == GameState.SubMenuState.SETTINGS) {
            if (gameState.getMenuSelectionIndex() == 0) { // BGM
                gameState.setBgmVolume(Math.max(0, gameState.getBgmVolume() - 10));
                soundManager.setBgmVolume(gameState.getBgmVolume());
                soundManager.playSFX("hold");
            } else if (gameState.getMenuSelectionIndex() == 1) { // SFX
                gameState.setSfxVolume(Math.max(0, gameState.getSfxVolume() - 10));
                soundManager.setSfxVolume(gameState.getSfxVolume());
                soundManager.playSFX("hold");
            }
            gamePanel.repaint();
        }
    }

    public void rightPressed() {
        if (gameState.getCurrentState() == GameState.State.PLAYING) {
            moveRight();
        } else if (gameState.getCurrentState() == GameState.State.MENU && gameState.getCurrentSubMenu() == GameState.SubMenuState.SETTINGS) {
            if (gameState.getMenuSelectionIndex() == 0) {
                gameState.setBgmVolume(Math.min(100, gameState.getBgmVolume() + 10));
                soundManager.setBgmVolume(gameState.getBgmVolume());
                soundManager.playSFX("hold");
            } else if (gameState.getMenuSelectionIndex() == 1) {
                gameState.setSfxVolume(Math.min(100, gameState.getSfxVolume() + 10));
                soundManager.setSfxVolume(gameState.getSfxVolume());
                soundManager.playSFX("hold");
            }
            gamePanel.repaint();
        }
    }

    // Phase 11: Soft Drop Toggles
    public void enableSoftDrop() {
        if (gameState.getCurrentState() == GameState.State.PLAYING) {
            gameState.setSoftDropping(true);
        }
    }
    
    public void disableSoftDrop() {
        gameState.setSoftDropping(false);
    }

    public void downPressed() {
        if (gameState.getCurrentState() == GameState.State.PLAYING) {
            enableSoftDrop();
            moveDown();
            return;
        }
        
        soundManager.playSFX("hold"); 
        if (gameState.getCurrentState() == GameState.State.MENU) {
            int max = (gameState.getCurrentSubMenu() == GameState.SubMenuState.MAIN) ? 4 : 
                      (gameState.getCurrentSubMenu() == GameState.SubMenuState.SETTINGS) ? 2 : 0;
            if (max > 0) {
                gameState.setMenuSelectionIndex((gameState.getMenuSelectionIndex() + 1) % (max + 1));
            }
        } else if (gameState.getCurrentState() == GameState.State.PAUSED) {
            gameState.setPauseSelectionIndex((gameState.getPauseSelectionIndex() + 1) % 3);
        } else if (gameState.getCurrentState() == GameState.State.GAME_OVER) {
            gameState.setGameOverSelectionIndex((gameState.getGameOverSelectionIndex() + 1) % 2);
        }
        gamePanel.repaint();
    }

    public void upPressed() {
        if (gameState.getCurrentState() == GameState.State.PLAYING) {
            rotate();
            return;
        }
        
        soundManager.playSFX("hold");
        if (gameState.getCurrentState() == GameState.State.MENU) {
            int max = (gameState.getCurrentSubMenu() == GameState.SubMenuState.MAIN) ? 4 : 
                      (gameState.getCurrentSubMenu() == GameState.SubMenuState.SETTINGS) ? 2 : 0;
            if (max > 0) {
                gameState.setMenuSelectionIndex((gameState.getMenuSelectionIndex() - 1 + max + 1) % (max + 1));
            }
        } else if (gameState.getCurrentState() == GameState.State.PAUSED) {
            gameState.setPauseSelectionIndex((gameState.getPauseSelectionIndex() - 1 + 3) % 3);
        } else if (gameState.getCurrentState() == GameState.State.GAME_OVER) {
            gameState.setGameOverSelectionIndex((gameState.getGameOverSelectionIndex() - 1 + 2) % 2);
        }
        gamePanel.repaint();
    }
    
    // Core movements extracted for playing state
    private void moveLeft() {
        Board board = gameState.getBoard();
        Tetromino piece = gameState.getCurrentPiece();
        int x = gameState.getCurrentX();
        int y = gameState.getCurrentY();
        if (board.canMove(x - 1, y, piece)) {
            gameState.setCurrentX(x - 1);
            gameState.setLastMoveRotate(false);
            if (board.canMove(x - 1, y + 1, piece)) {
                gameState.setLockWaiting(false);
            }
            gamePanel.repaint();
        }
    }

    private void moveRight() {
        Board board = gameState.getBoard();
        Tetromino piece = gameState.getCurrentPiece();
        int x = gameState.getCurrentX();
        int y = gameState.getCurrentY();
        if (board.canMove(x + 1, y, piece)) {
            gameState.setCurrentX(x + 1);
            gameState.setLastMoveRotate(false);
            if (board.canMove(x + 1, y + 1, piece)) {
                gameState.setLockWaiting(false);
            }
            gamePanel.repaint();
        }
    }

    private void moveDown() {
        actionPerformed(null);
    }

    private void rotate() {
        Tetromino piece = gameState.getCurrentPiece();
        Board board = gameState.getBoard();
        int x = gameState.getCurrentX();
        int y = gameState.getCurrentY();

        piece.rotateClockwise();

        if (board.canMove(x, y, piece)) {
            applyRotation(x, y);
            return;
        }
        if (board.canMove(x - 1, y, piece)) {
            gameState.setCurrentX(x - 1);
            applyRotation(x - 1, y);
            return;
        }
        if (board.canMove(x + 1, y, piece)) {
            gameState.setCurrentX(x + 1);
            applyRotation(x + 1, y);
            return;
        }
        if (board.canMove(x, y - 1, piece)) {
            gameState.setCurrentY(y - 1);
            applyRotation(x, y - 1);
            return;
        }

        piece.rotateClockwise();
        piece.rotateClockwise();
        piece.rotateClockwise();
    }

    private void applyRotation(int finalX, int finalY) {
        gameState.setLastMoveRotate(true); // Phase 10: 記錄最後一次動作為旋轉！
        if (gameState.getBoard().canMove(finalX, finalY + 1, gameState.getCurrentPiece())) {
            gameState.setLockWaiting(false);
        }
        gamePanel.repaint();
    }

    public void togglePause() {
        if (gameState.getCurrentState() == GameState.State.MENU ||
                gameState.getCurrentState() == GameState.State.GAME_OVER)
            return;

        if (gameState.getCurrentState() == GameState.State.PLAYING) {
            gameState.setCurrentState(GameState.State.PAUSED);
            gameState.pauseTimer(); // Phase 11
            soundManager.pauseBGM(); // Phase 13
            gameState.setPauseSelectionIndex(0);
            timer.stop();
        } else if (gameState.getCurrentState() == GameState.State.PAUSED) {
            gameState.setCurrentState(GameState.State.PLAYING);
            gameState.resumeTimer(); // Phase 11
            soundManager.resumeBGM(); // Phase 13
            timer.start();
        }
        gamePanel.repaint();
    }
}
