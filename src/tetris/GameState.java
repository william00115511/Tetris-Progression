package tetris;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * GameState 負責管理遊戲資料與狀態（Model層）。
 */
public class GameState {
    public enum State {
        MENU, PLAYING, PAUSED, GAME_OVER
    }

    // Phase 8: 賦予遊戲雙軌模式
    public enum GameMode {
        ENDLESS, LEVEL
    }

    // Phase 13: 互動選單子狀態
    public enum SubMenuState {
        MAIN, SETTINGS, CONTROLS
    }

    private static final int COLUMNS = 10;
    private static final int ROWS = 20;

    private Board board;
    private Tetromino currentPiece;
    // Phase 11 variables
    private List<Tetromino> nextPieces;
    private long playStartTime;
    private long accumulatedPlayTime;
    private long pauseStartTime;
    private boolean softDropping;

    private Tetromino holdPiece;
    private boolean canHold;

    private List<Integer> bag;

    private int currentX;
    private int currentY;

    private boolean isLockWaiting;

    private State currentState;
    private int score;

    // Phase 8 variables
    private GameMode currentMode;
    private int level;
    private int totalLinesCleared;
    private int sessionHighScore;

    // Phase 9.5 variables
    private List<Particle> particles;
    private List<Lightning> lightnings;
    private long levelUpDisplayUntil;

    // Phase 10 variables
    private boolean lastMoveRotate;
    private int comboCount;
    private List<FloatingText> floatingTexts;

    // Phase 12, 13, 14 variables
    private List<Particle> menuParticles;
    private List<BgPiece> bgPieces;
    private SubMenuState currentSubMenu;
    private int menuSelectionIndex;
    private int pauseSelectionIndex;
    private int gameOverSelectionIndex;
    private int bgmVolume; // 0-100
    private int sfxVolume; // 0-100

    public GameState() {
        this.board = new Board(COLUMNS, ROWS);
        this.currentState = State.PLAYING; // Stage 1: Skip Menu
        this.score = 0;

        this.currentMode = GameMode.ENDLESS;
        this.level = 1;
        this.totalLinesCleared = 0;
        this.sessionHighScore = 0;

        this.holdPiece = null;
        this.canHold = true;
        this.isLockWaiting = false;

        this.bag = new ArrayList<>();
        this.nextPieces = new ArrayList<>();
        // Stage 2: Restore 4 next pieces
        for (int i = 0; i < 4; i++) {
            this.nextPieces.add(new Tetromino(getNextFromBag()));
        }

        this.particles = new ArrayList<>();
        this.lightnings = new ArrayList<>();
        this.levelUpDisplayUntil = 0;

        this.lastMoveRotate = false;
        this.comboCount = 0;
        this.floatingTexts = new ArrayList<>();

        this.softDropping = false;

        // Phase 12, 13, 14
        this.menuParticles = new ArrayList<>();
        this.bgPieces = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            this.bgPieces.add(new BgPiece(1000, 1000)); // 假設解析度隨機範圍 1000x1000
        }
        this.currentSubMenu = SubMenuState.MAIN;
        this.menuSelectionIndex = 0;
        this.pauseSelectionIndex = 0;
        this.gameOverSelectionIndex = 0;
        this.bgmVolume = 80;
        this.sfxVolume = 80;
    }

    /**
     * Phase 9: 觸發中央橫幅 "LEVEL UP!" 提示 1.5 秒
     */
    public void triggerLevelUpDisplay() {
        this.levelUpDisplayUntil = System.currentTimeMillis() + 1500;
    }

    public boolean isDisplayingLevelUp() {
        return System.currentTimeMillis() < levelUpDisplayUntil;
    }

    public int getNextFromBag() {
        if (bag.isEmpty()) {
            bag.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7));
            Collections.shuffle(bag);
        }
        return bag.remove(0);
    }
    /**
     * Phase 10: 競技級分數結算，根據 T-Spin 與 Combo 加倍獎勵！
     */
    public void addAdvancedScore(int lines, boolean isTSpin, int combo) {
        if (lines <= 0) return;
        int base = 0;
        if (isTSpin) {
            switch (lines) {
                case 1: base = 800; break;
                case 2: base = 1200; break;
                case 3: base = 1600; break;
                default: base = 400; break;
            }
        } else {
            switch (lines) {
                case 1: base = 100; break;
                case 2: base = 300; break;
                case 3: base = 500; break;
                case 4: base = 800; break;
                default: base = lines * 200; break;
            }
        }
        
        // Combo multiplier (連擊次數越多加成越大)
        if (combo > 0) {
            base += 50 * combo;
        }
        this.score += base;
    }

    public void swapToNextPiece() {
        this.currentPiece = this.nextPieces.remove(0);
        this.nextPieces.add(new Tetromino(getNextFromBag()));
    }

    // Phase 11: 存活計時機制
    public void startTimer() {
        playStartTime = System.currentTimeMillis();
        accumulatedPlayTime = 0;
    }
    
    public void stopTimer() {
        if (currentState == State.PLAYING) {
            accumulatedPlayTime += (System.currentTimeMillis() - playStartTime);
        }
    }
    
    public void pauseTimer() {
        pauseStartTime = System.currentTimeMillis();
    }
    
    public void resumeTimer() {
        playStartTime += (System.currentTimeMillis() - pauseStartTime);
    }
    
    public long getPlayTimeMs() {
        if (currentState == State.PLAYING) {
            return accumulatedPlayTime + (System.currentTimeMillis() - playStartTime);
        } else if (currentState == State.PAUSED) {
            return accumulatedPlayTime + (pauseStartTime - playStartTime);
        }
        return accumulatedPlayTime; // For GAME_OVER or MENU
    }

    // ==========================================
    // Getters & Setters
    // ==========================================
    public int getColumns() {
        return COLUMNS;
    }

    public int getRows() {
        return ROWS;
    }

    public Board getBoard() {
        return board;
    }

    public Tetromino getCurrentPiece() {
        return currentPiece;
    }

    public void setCurrentPiece(Tetromino currentPiece) {
        this.currentPiece = currentPiece;
    }

    public List<Tetromino> getNextPieces() {
        return nextPieces;
    }
    
    public boolean isSoftDropping() { return softDropping; }
    public void setSoftDropping(boolean softDropping) { this.softDropping = softDropping; }

    public Tetromino getHoldPiece() {
        return holdPiece;
    }

    public void setHoldPiece(Tetromino holdPiece) {
        this.holdPiece = holdPiece;
    }

    public boolean isCanHold() {
        return canHold;
    }

    public void setCanHold(boolean canHold) {
        this.canHold = canHold;
    }

    public int getCurrentX() {
        return currentX;
    }

    public void setCurrentX(int currentX) {
        this.currentX = currentX;
    }

    public int getCurrentY() {
        return currentY;
    }

    public void setCurrentY(int currentY) {
        this.currentY = currentY;
    }

    public boolean isLockWaiting() {
        return isLockWaiting;
    }

    public void setLockWaiting(boolean lockWaiting) {
        this.isLockWaiting = lockWaiting;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State currentState) {
        this.currentState = currentState;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public GameMode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(GameMode currentMode) {
        this.currentMode = currentMode;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getTotalLinesCleared() {
        return totalLinesCleared;
    }

    public void setTotalLinesCleared(int totalLinesCleared) {
        this.totalLinesCleared = totalLinesCleared;
    }

    public int getSessionHighScore() {
        return sessionHighScore;
    }

    public void setSessionHighScore(int sessionHighScore) { this.sessionHighScore = sessionHighScore; }

    public List<Particle> getParticles() { return particles; }
    public List<Lightning> getLightnings() { return lightnings; }
    public List<FloatingText> getFloatingTexts() { return floatingTexts; }
    public List<Particle> getMenuParticles() { return menuParticles; }
    public List<BgPiece> getBgPieces() { return bgPieces; }
    
    public boolean isLastMoveRotate() { return lastMoveRotate; }
    public void setLastMoveRotate(boolean lastMoveRotate) { this.lastMoveRotate = lastMoveRotate; }

    public int getComboCount() { return comboCount; }
    public void setComboCount(int comboCount) { this.comboCount = comboCount; }

    public SubMenuState getCurrentSubMenu() { return currentSubMenu; }
    public void setCurrentSubMenu(SubMenuState currentSubMenu) { this.currentSubMenu = currentSubMenu; }

    public int getMenuSelectionIndex() { return menuSelectionIndex; }
    public void setMenuSelectionIndex(int menuSelectionIndex) { this.menuSelectionIndex = menuSelectionIndex; }

    public int getPauseSelectionIndex() { return pauseSelectionIndex; }
    public void setPauseSelectionIndex(int pauseSelectionIndex) { this.pauseSelectionIndex = pauseSelectionIndex; }

    public int getGameOverSelectionIndex() { return gameOverSelectionIndex; }
    public void setGameOverSelectionIndex(int gameOverSelectionIndex) { this.gameOverSelectionIndex = gameOverSelectionIndex; }

    public int getBgmVolume() { return bgmVolume; }
    public void setBgmVolume(int bgmVolume) { this.bgmVolume = bgmVolume; }

    public int getSfxVolume() { return sfxVolume; }
    public void setSfxVolume(int sfxVolume) { this.sfxVolume = sfxVolume; }
}
