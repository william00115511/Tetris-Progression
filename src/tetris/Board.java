package tetris;

/**
 * Board 負責管理已經固定在網底的方塊狀態、碰撞與消除邏輯（Model層）。
 */
public class Board {
    private final int columns;
    private final int rows;
    private int[][] grid;

    public Board(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
        this.grid = new int[rows][columns];
    }

    public int getColumns() { return columns; }
    public int getRows() { return rows; }
    public int[][] getGrid() { return grid; }

    /**
     * Phase 3：清空盤面（讓 Restart 功能使用）
     */
    public void clear() {
        this.grid = new int[rows][columns];
    }

    public boolean canMove(int targetX, int targetY, Tetromino piece) {
        int[][] shape = piece.getShape();
        int pieceSize = shape.length;

        for (int r = 0; r < pieceSize; r++) {
            for (int c = 0; c < pieceSize; c++) {
                if (shape[r][c] != 0) {
                    int absoluteX = targetX + c;
                    int absoluteY = targetY + r;

                    if (absoluteX < 0 || absoluteX >= columns) return false; 
                    if (absoluteY >= rows) return false; 
                    if (absoluteY >= 0) {
                        if (grid[absoluteY][absoluteX] != 0) return false;
                    }
                }
            }
        }
        return true; 
    }

    public void placeTetromino(int x, int y, Tetromino piece) {
        int[][] shape = piece.getShape();
        int pieceSize = shape.length;

        for (int r = 0; r < pieceSize; r++) {
            for (int c = 0; c < pieceSize; c++) {
                if (shape[r][c] != 0) {
                    int absoluteX = x + c;
                    int absoluteY = y + r;
                    if (absoluteY >= 0 && absoluteY < rows && absoluteX >= 0 && absoluteX < columns) {
                        grid[absoluteY][absoluteX] = shape[r][c];
                    }
                }
            }
        }
    }

    /**
     * 【重點 Phase 3：消行邏輯】
     * 當方塊固化後觸發。從盤面最底部（rows - 1）往上掃描。
     * 若發現某一列全滿（沒有 0），則將上方所有列的資料「往下平移一格覆蓋目前的列」，
     * 以達成滿列消除與下放的效果。
     *
     * @return 本次同時觸發消除的總行數
     */
    public int checkAndClearLines() {
        int linesCleared = 0;
        
        // 由下（y=19）往上掃描
        for (int y = rows - 1; y >= 0; y--) {
            boolean isLineFull = true;
            
            // 檢查橫向這行是否全部都有方塊佔據
            for (int x = 0; x < columns; x++) {
                if (grid[y][x] == 0) {
                    isLineFull = false;
                    break;
                }
            }
            
            if (isLineFull) {
                linesCleared++;
                
                // 【二維陣列平移邏輯】：我們從滿列處(y)開始往上掃描
                // 每一個 y 都去「拿上方的資料 (rowY-1)」來覆寫自已
                for (int rowY = y; rowY > 0; rowY--) {
                    for (int x = 0; x < columns; x++) {
                        grid[rowY][x] = grid[rowY - 1][x];
                    }
                }
                
                // 最頂端 (y=0) 的行數已經沒有更上方的東西可以拿了，直接清空為 0
                for (int x = 0; x < columns; x++) {
                    grid[0][x] = 0;
                }
                
                // 【關鍵點】：由於上方陣列墜下來覆蓋了這一排（原本滿行的資料），
                // 掉下來的新資料『可能也是全滿的』，所以要使得 y 停在原地再檢查一次，
                // 以免連續滿行被忽略（抵銷 y--）。
                y++; 
            }
        }
        
        return linesCleared;
    }
}
