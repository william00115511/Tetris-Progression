package tetris;

import java.util.Random;

public class BgPiece {
    public double x;
    public double y;
    public double dy;
    public double rotation;
    public double rotSpeed;
    public int colorCode;
    public float alpha;
    public int scale;

    public BgPiece(int maxWidth, int maxHeight) {
        Random rand = new Random();
        this.x = rand.nextInt(maxWidth);
        this.y = rand.nextInt(maxHeight) - 400; // 允許從較高的位置開始降落
        this.dy = 0.2 + rand.nextDouble() * 0.8; // 緩慢掉落速度
        this.rotation = rand.nextDouble() * Math.PI * 2;
        this.rotSpeed = (rand.nextDouble() - 0.5) * 0.02; // 非常緩慢的自轉
        this.colorCode = rand.nextInt(7) + 1;
        this.alpha = 0.02f + rand.nextFloat() * 0.06f; // 極低的透明度 (0.02 ~ 0.08)
        this.scale = 3 + rand.nextInt(4); // 巨大的放大倍率 (3x ~ 6x)
    }
}
