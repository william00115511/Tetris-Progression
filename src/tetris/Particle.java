package tetris;

import java.awt.Color;

/**
 * Phase 9.5: 負責存放粒子特效資料的資料結構類別 (支援雙型態)
 */
public class Particle {
    public int type; // 0 = 煙霧粉末, 1 = 高壓電火花
    public float x, y, vx, vy;
    public Color color;
    public int life;
    public int maxLife;

    public Particle(int type, float x, float y, float vx, float vy, Color color, int maxLife) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.color = color;
        this.life = maxLife;
        this.maxLife = maxLife;
    }
}
