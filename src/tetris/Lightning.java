package tetris;

/**
 * Phase 9.6: 美學優化版雷電（更密集的 Jitter 震幅並融合亂數分支 Branch 系統）
 */
public class Lightning {
    public int[] mainX;
    public int[] mainY;
    
    public int[] branchX;
    public int[] branchY;
    public boolean hasBranch;
    
    public int life;
    public int maxLife;

    public Lightning(int gridY, int maxLife) {
        this.life = maxLife;
        this.maxLife = maxLife;
        
        java.util.Random rand = new java.util.Random();
        int segments = 25; // 增加段數，使其更具侵略性的曲折感
        mainX = new int[segments];
        mainY = new int[segments];
        
        for (int i = 0; i < segments; i++) {
            mainX[i] = (int) (i * (300.0 / (segments - 1)));
            int baseY = (gridY * 30) + 15;
            // 短幅密集震盪 [-8, +8]
            mainY[i] = baseY + (rand.nextInt(16) - 8);
        }
        
        hasBranch = rand.nextBoolean(); // 50% 機率夾帶一束狂野的分支
        if (hasBranch) {
            int branchSegments = 6 + rand.nextInt(6);
            branchX = new int[branchSegments];
            branchY = new int[branchSegments];
            
            // 隨機在閃電中後段剝出閃電分支
            int startIdx = 8 + rand.nextInt(8);
            branchX[0] = mainX[startIdx];
            branchY[0] = mainY[startIdx];
            
            int yDirection = rand.nextBoolean() ? 1 : -1;
            for (int i = 1; i < branchSegments; i++) {
                branchX[i] = branchX[i - 1] + 8 + rand.nextInt(8);
                branchY[i] = branchY[i - 1] + yDirection * (5 + rand.nextInt(8));
            }
        }
    }
}
