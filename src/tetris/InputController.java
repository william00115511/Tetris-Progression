package tetris;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InputController extends KeyAdapter {
    private GameController gameController;

    public InputController(GameController gameController) {
        this.gameController = gameController;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                gameController.leftPressed();
                break;
            case KeyEvent.VK_RIGHT:
                gameController.rightPressed();
                break;
            case KeyEvent.VK_DOWN:
                gameController.downPressed();
                break;
            case KeyEvent.VK_UP:
                gameController.upPressed();
                break;
            case KeyEvent.VK_SPACE:
                gameController.hardDrop();
                break;
            /* Stage 1: Disable Pause
            case KeyEvent.VK_ESCAPE:
                gameController.togglePause();
                break;
            */
            /* Stage 1: Disable Menu Navigation
            case KeyEvent.VK_ENTER:
                gameController.enterPressed();
                break;
            */
            case KeyEvent.VK_C:
                gameController.hold(); // Phase 13: 移除 SHIFT，僅保留 C 鍵，避免觸發輸入法
                break;
        }
    }

    // Phase 11: Soft Drop 解除
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            gameController.disableSoftDrop();
        }
    }
}
