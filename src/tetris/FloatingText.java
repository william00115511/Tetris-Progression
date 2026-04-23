package tetris;

import java.awt.Color;

/**
 * Phase 10: 戰鬥文字特效屬性存放區 (T-Spin, Combo)
 */
public class FloatingText {
    public String text;
    public float x, y;
    public Color color;
    public int life;
    public int maxLife;

    public FloatingText(String text, float x, float y, Color color, int maxLife) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
        this.life = maxLife;
        this.maxLife = maxLife;
    }
}
