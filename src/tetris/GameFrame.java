package tetris;

import javax.swing.JFrame;

/**
 * GameFrame 負責主視窗設定（View 的最外層容器）。
 * 
 * 【MVC 架構說明 - View】
 * GameFrame 單純作為一個電腦視窗的載體，將 GamePanel (負責實際畫面) 包裝起來並展示給使用者。
 * 它的職責只有初始化視窗的標題、關閉行為、禁止使用者調整大小等視窗系統相關的設定。
 * 如果要切換選單或遊戲畫面，也是在 JFrame 的層級來置換內部的 Panel。
 */
public class GameFrame extends JFrame {

    public GameFrame(GamePanel gamePanel) {
        super("Tetris Game");
        
        // 將負責繪圖與互動的 Panel 加入視窗
        this.add(gamePanel);
        
        // 設定點擊右上角 X 時結束整個應用程式
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 依照 Panel 的 PreferredSize (期望大小) 來自動運算調整視窗的實際大小
        this.pack();
        
        // 禁止使用者縮放視窗，保持遊戲畫面長寬比例不被破壞
        this.setResizable(false);
        
        // 將視窗置中顯示在電腦螢幕上
        this.setLocationRelativeTo(null);
    }
}
