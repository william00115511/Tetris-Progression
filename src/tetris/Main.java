package tetris;

import javax.swing.SwingUtilities;

/**
 * Main 為程式的進入點。
 * 
 * 【Phase 2.5 更新說明】
 * 除了產生 Model 與 Panel，這次我們會正式註冊 Controller 來承接一切，
 * 並呼叫 controller.start() 來推動遊戲 Timer 運轉。
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            
            // 1. 初始化資料模型 (Model)
            GameState gameState = new GameState();
            
            // 2. 初始化遊戲面板並傳入資料模型 (View)
            GamePanel gamePanel = new GamePanel(gameState);
            
            // 3. 初始化遊戲控制器 (GameController)，讓它具有改 Model 和推播 View 的權力
            GameController gameController = new GameController(gameState, gamePanel);
            
            // 4. 初始化按鍵輸入控制器，負責捕捉按鍵轉發給 GameController 去跑邏輯
            InputController inputController = new InputController(gameController);
            
            // 將按鍵輸入事件掛在面板上
            gamePanel.addKeyListener(inputController);
            
            // 5. 初始化主視窗並加入面板，最後展示
            GameFrame gameFrame = new GameFrame(gamePanel);
            gameFrame.setVisible(true);
            
            // 6. 萬事俱備，正式啟動心臟 Timer 開演！
            gameController.start();
            
        });
    }
}
