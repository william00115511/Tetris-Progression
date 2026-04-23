package tetris;

import java.util.Random;

/**
 * Tetromino 負責定義方塊形狀與旋轉邏輯（Model層）。
 */
public class Tetromino {
    // 方塊形狀的 2D 陣列 (0: 無方塊, 非0: 該方塊相對應顏色的代碼)
    private int[][] shape;
    // 辨識是哪一種方塊顏色的代碼
    private int colorCode;

    // 定義 7 種標準方塊形狀的三維陣列
    // 在 2D 陣列中，1 代表 I, 2 代表 J, 依此類推
    private static final int[][][] SHAPES = {
        // 0: Empty (為保留索引值，不應該被選到)
        { { 0 } },
        // 1: I-shape (Cyan)
        { { 0, 0, 0, 0 },
          { 1, 1, 1, 1 },
          { 0, 0, 0, 0 },
          { 0, 0, 0, 0 } },
        // 2: J-shape (Blue)
        { { 2, 0, 0 },
          { 2, 2, 2 },
          { 0, 0, 0 } },
        // 3: L-shape (Orange)
        { { 0, 0, 3 },
          { 3, 3, 3 },
          { 0, 0, 0 } },
        // 4: O-shape (Yellow)
        { { 4, 4 },
          { 4, 4 } },
        // 5: S-shape (Green)
        { { 0, 5, 5 },
          { 5, 5, 0 },
          { 0, 0, 0 } },
        // 6: T-shape (Purple)
        { { 0, 6, 0 },
          { 6, 6, 6 },
          { 0, 0, 0 } },
        // 7: Z-shape (Red)
        { { 7, 7, 0 },
          { 0, 7, 7 },
          { 0, 0, 0 } }
    };

    private static final Random random = new Random();

    public Tetromino(int shapeIndex) {
        this.colorCode = shapeIndex;
        // 深拷貝一份形狀陣列，以免旋轉時修改到本體的常數範本
        int[][] template = SHAPES[shapeIndex];
        this.shape = new int[template.length][template[0].length];
        for (int r = 0; r < template.length; r++) {
            for (int c = 0; c < template[0].length; c++) {
                this.shape[r][c] = template[r][c];
            }
        }
    }

    /**
     * 完全隨機產生一個方塊（代碼 1 ~ 7）
     */
    public static Tetromino generateRandomTetromino() {
        int index = random.nextInt(7) + 1; // 產生 1 到 7
        return new Tetromino(index);
    }

    /**
     * 讀取形狀資料
     */
    public int[][] getShape() {
        return shape;
    }

    public int getColorCode() {
        return colorCode;
    }

    /**
     * 取回方塊矩陣的列數 (陣列高)
     */
    public int getHeight() {
        return shape.length;
    }

    /**
     * 取回方塊矩陣的行數 (陣列寬)
     */
    public int getWidth() {
        return shape[0].length;
    }

    /**
     * 將方塊順時針旋轉 90 度
     * 演算法：透過矩陣的「轉置 (Transpose)」再加上「水平翻轉」來實現。
     */
    public void rotateClockwise() {
        int size = shape.length;
        int[][] rotated = new int[size][size];

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                // 原本座標 [r][c]，順時針旋轉後會移動到 [c][size - 1 - r]
                rotated[c][size - 1 - r] = shape[r][c];
            }
        }
        this.shape = rotated;
    }
}
