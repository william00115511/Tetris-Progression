package tetris;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Phase 8: 負責最高分紀錄本地持久化的工具類
 */
public class ScoreManager {
    private static final String FILE_ENDLESS = "highscore_endless.txt";
    private static final String FILE_LEVEL = "hilevel_levelmode.txt"; // Phase 9.6: Level模式專用最高等級紀錄

    public static int loadScore(GameState.GameMode mode) {
        String filename = (mode == GameState.GameMode.ENDLESS) ? FILE_ENDLESS : FILE_LEVEL;
        File file = new File(filename);
        if (!file.exists()) {
            return (mode == GameState.GameMode.LEVEL) ? 1 : 0;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if (line != null && !line.trim().isEmpty()) {
                return Integer.parseInt(line.trim());
            }
        } catch (Exception e) {
            System.err.println("讀取最高分失敗: " + filename);
        }
        return 0;
    }

    public static void saveScore(GameState.GameMode mode, int score) {
        String filename = (mode == GameState.GameMode.ENDLESS) ? FILE_ENDLESS : FILE_LEVEL;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            bw.write(String.valueOf(score));
        } catch (Exception e) {
            System.err.println("儲存最高分失敗: " + filename);
        }
    }
}
