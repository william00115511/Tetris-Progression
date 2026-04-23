package tetris;

import javax.swing.JPanel;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class GamePanel extends JPanel {
    
    private static final int CELL_SIZE = 30;
    private static final int LEFT_PANEL_COLS = 6; 
    private static final int RIGHT_PANEL_COLS = 6; 
    private static final int PADDING = 20; // 核心遊戲區的呼吸感留白間距
    
    private GameState gameState;

    public GamePanel(GameState gameState) {
        this.gameState = gameState;
        
        int width = (LEFT_PANEL_COLS * CELL_SIZE) + PADDING + (gameState.getColumns() * CELL_SIZE) + PADDING + (RIGHT_PANEL_COLS * CELL_SIZE);
        int height = gameState.getRows() * CELL_SIZE;
        
        this.setPreferredSize(new Dimension(width, height));
        this.setBackground(Color.BLACK);
        
        this.setFocusable(true);
        this.requestFocusInWindow();
    }

    private Color getColorMapping(int colorCode) {
        if (!gameState.isCanHold()) return Color.DARK_GRAY;
        return getRawColorMapping(colorCode);
    }
    
    private Color getRawColorMapping(int colorCode) {
        switch (colorCode) {
            case 1: return Color.CYAN;
            case 2: return Color.BLUE;
            case 3: return Color.ORANGE;
            case 4: return Color.YELLOW;
            case 5: return Color.GREEN;
            case 6: return new Color(128, 0, 128); // 紫色
            case 7: return Color.RED;
            default: return Color.DARK_GRAY;
        }
    }

    // Phase 13: 陰影文字繪製工具
    private void drawStringWithShadow(Graphics2D g, String text, int x, int y, Color color) {
        g.setColor(new Color(0, 0, 0, 200));
        g.drawString(text, x + 2, y + 2);
        g.setColor(color);
        g.drawString(text, x, y);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Phase 9: 全面升級至 Java2D 圖形渲染器，為浮凸立體與半透明鋪路
        Graphics2D g2d = (Graphics2D) g;
        // 啟動反鋸齒以保證特效極其平滑
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (gameState.getCurrentState() == GameState.State.MENU) {
            drawMenu(g2d);
            return;
        }

        // --- 核心遊戲區（中欄）偏移量 ---
        int matrixOffsetX = (LEFT_PANEL_COLS * CELL_SIZE) + PADDING;
        g2d.translate(matrixOffsetX, 0);

        drawGridLines(g2d);
        drawBoard(g2d);
        /* Stage 1: Disable advanced visuals
        drawDropTrail(g2d); // Phase 11
        drawGhostPiece(g2d);
        */
        drawCurrentPiece(g2d);
        
        /* Stage 1: Disable Effects
        drawParticles(g2d);
        drawFloatingTexts(g2d);
        */
        
        // 恢復原點供 UI 繪製
        g2d.translate(-matrixOffsetX, 0);
        
        drawLeftPanel(g2d);
        drawRightPanel(g2d);
        
        // Level Up 中央特效
        /* Stage 1: Disable Level Up Text
        g2d.translate(matrixOffsetX, 0);
        drawLevelUpTrigger(g2d);
        g2d.translate(-matrixOffsetX, 0);
        */
        
        drawOverlay(g2d);
    }

    /**
     * Phase 12/13: 殿堂級圖形化主選單
     */
    private void drawMenu(Graphics2D g) {
        // --- 背景環境粒子 ---
        for (Particle mp : gameState.getMenuParticles()) {
            float alpha = (float) mp.life / mp.maxLife;
            if (alpha < 0) alpha = 0;
            if (alpha > 1) alpha = 1;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.5f));
            g.setColor(mp.color);
            int sz = 15 + (int)((1f - alpha) * 20);
            g.fillRoundRect((int)mp.x, (int)mp.y, sz, sz, 5, 5);
        }
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        // --- Phase 14 巨型動態背景 ---
        for (BgPiece p : gameState.getBgPieces()) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, p.alpha));
            Color c = getRawColorMapping(p.colorCode);
            g.setColor(c);
            
            java.awt.geom.AffineTransform old = g.getTransform();
            g.translate(p.x, p.y);
            g.rotate(p.rotation);
            
            int[][] shape = new Tetromino(p.colorCode).getShape();
            int s = CELL_SIZE * p.scale;
            for(int r = 0; r < shape.length; r++) {
                for(int cld = 0; cld < shape[0].length; cld++) {
                    if(shape[r][cld] != 0) {
                        int px = (cld - shape[0].length / 2) * s;
                        int py = (r - shape.length / 2) * s;
                        g.fillRect(px, py, s, s); // 繪製實心方塊，移除縫隙
                        g.drawRect(px, py, s, s);
                    }
                }
            }
            g.setTransform(old);
        }
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        int cx = getWidth() / 2;
        int titleY = getHeight() / 2 - 120; // 稍微拉高給予選單空間

        // --- 標題外發光 (多層半透明擴散) ---
        String title = "TETRIS";
        Font titleFont = new Font("Arial", Font.BOLD, 72);
        g.setFont(titleFont);
        int tWidth = g.getFontMetrics().stringWidth(title);

        int[][] glowLayers = {{6, 15}, {4, 25}, {2, 40}};
        for (int[] layer : glowLayers) {
            int offset = layer[0];
            int glowAlpha = layer[1];
            g.setColor(new Color(0, 180, 255, glowAlpha));
            g.drawString(title, cx - tWidth / 2 + offset, titleY);
            g.drawString(title, cx - tWidth / 2 - offset, titleY);
            g.drawString(title, cx - tWidth / 2, titleY + offset);
            g.drawString(title, cx - tWidth / 2, titleY - offset);
        }

        // 立體投影
        g.setColor(new Color(0, 40, 80));
        g.drawString(title, cx - tWidth / 2 + 3, titleY + 3);

        // 主標題漸層
        java.awt.GradientPaint gradient = new java.awt.GradientPaint(
            0, titleY - 60, new Color(0, 220, 255),
            0, titleY + 10, Color.WHITE
        );
        g.setPaint(gradient);
        g.drawString(title, cx - tWidth / 2, titleY);
        g.setPaint(null);

        // --- 子選單渲染 ---
        int startY = titleY + 80;
        GameState.SubMenuState subMenu = gameState.getCurrentSubMenu();
        
        if (subMenu == GameState.SubMenuState.MAIN) {
            String[] options = {"Endless Mode", "Level Mode", "Controls", "Settings", "Quit"};
            // Phase 14.5: 精準對應方塊 ICON (Endless=I, Level=J, Controls=T, Settings=O, Quit=Z)
            int[] iconTypes = {1, 2, 6, 4, 7}; 
            Color[] colors = {Color.CYAN, Color.GREEN, new Color(200, 100, 255), Color.ORANGE, Color.RED};
            
            for (int i = 0; i < options.length; i++) {
                boolean isSelected = (gameState.getMenuSelectionIndex() == i);
                int yPos = startY + (i * 45);
                
                Color tintColor = isSelected ? Color.CYAN : colors[i];
                
                // 動態 Icon
                drawMicroPiece(g, iconTypes[i], cx - 110, yPos - 18, 12, isSelected ? tintColor : null, isSelected);

                // Phase 14.5: 將游標三角形畫在圖示更左方，不重疊
                if (isSelected) {
                    g.setColor(tintColor);
                    drawArrow(g, cx - 135, yPos - 6, 1); // Right-pointing arrow
                }
                
                renderMenuItem(g, options[i], cx - 50, yPos, isSelected, colors[i], true);
            }
        } else if (subMenu == GameState.SubMenuState.SETTINGS) {
            String[] options = {"BGM Volume: " + gameState.getBgmVolume(), "SFX Volume: " + gameState.getSfxVolume(), "Back"};
            Color[] colors = {Color.CYAN, Color.CYAN, Color.GRAY};

            for (int i = 0; i < options.length; i++) {
                boolean isSelected = (gameState.getMenuSelectionIndex() == i);
                int yPos = startY + (i * 50);
                
                if (isSelected) {
                    g.setColor(Color.CYAN);
                    drawArrow(g, cx - 100, yPos - 6, 1);
                }

                renderMenuItem(g, options[i], cx - 80, yPos, isSelected, colors[i], true);
                
                // 畫音量條
                if (i < 2) {
                    int vol = (i == 0) ? gameState.getBgmVolume() : gameState.getSfxVolume();
                    g.setColor(Color.DARK_GRAY);
                    g.fillRect(cx - 80, yPos + 8, 160, 6);
                    g.setColor(isSelected ? Color.CYAN : Color.LIGHT_GRAY);
                    g.fillRect(cx - 80, yPos + 8, (int)(160 * (vol / 100f)), 6);
                }
            }
        } else if (subMenu == GameState.SubMenuState.CONTROLS) {
            String[] ctrls = {
                "       : Move",
                "   : Rotate",
                "   : Soft Drop",
                "SPACE : Hard Drop",
                "C : Hold Piece",
                "ESC : Pause Game"
            };
            g.setFont(new Font("Arial", Font.BOLD, 18));
            for (int i = 0; i < ctrls.length; i++) {
                drawStringWithShadow(g, ctrls[i], cx - 80, startY + (i * 35), Color.LIGHT_GRAY);
                
                // 手動渲染完美無缺的三角形方向圖示
                if (i == 0) {
                    drawArrow(g, cx - 72, startY + (i * 35) - 6, 3); // Left
                    drawArrow(g, cx - 52, startY + (i * 35) - 6, 1); // Right
                } else if (i == 1) {
                    drawArrow(g, cx - 72, startY + (i * 35) - 6, 0); // Up
                } else if (i == 2) {
                    drawArrow(g, cx - 72, startY + (i * 35) - 6, 2); // Down
                }
            }
            
            renderMenuItem(g, "Back", cx - 30, startY + 230, true, Color.WHITE, true);
        }
    }

    private void renderMenuItem(Graphics2D g, String text, int x, int y, boolean isSelected, Color baseColor, boolean isMainMenu) {
        if (isSelected) {
            double breathFactor = (Math.sin(System.currentTimeMillis() / 200.0) + 1.0) / 2.0;
            g.setFont(new Font("Arial", Font.BOLD, 26)); 
            Color highlightColor = isMainMenu ? Color.CYAN : new Color(255, 215, 0);
            
            g.setColor(new Color(highlightColor.getRed(), highlightColor.getGreen(), highlightColor.getBlue(), 60 + (int)(breathFactor * 100)));
            g.drawString(text, x, y); 
            drawStringWithShadow(g, text, x, y, highlightColor);
        } else {
            g.setFont(new Font("Arial", Font.BOLD, 24));
            drawStringWithShadow(g, text, x, y, Color.DARK_GRAY);
        }
    }

    // Phase 13.5 圖形實作三角形方向鍵
    private void drawArrow(Graphics2D g, int x, int y, int direction) {
        g.setColor(Color.LIGHT_GRAY);
        int[] xs = new int[3];
        int[] ys = new int[3];
        int size = 12;
        int half = size / 2;
        
        if (direction == 0) { // Up
            xs = new int[]{x - half, x + half, x};
            ys = new int[]{y + half, y + half, y - half};
        } else if (direction == 1) { // Right
            xs = new int[]{x - half, x - half, x + half};
            ys = new int[]{y - half, y + half, y};
        } else if (direction == 2) { // Down
            xs = new int[]{x - half, x + half, x};
            ys = new int[]{y - half, y - half, y + half};
        } else if (direction == 3) { // Left
            xs = new int[]{x + half, x + half, x - half};
            ys = new int[]{y - half, y + half, y};
        }
        g.fillPolygon(xs, ys, 3);
    }

    // Phase 13.5 微型方塊 ICON，支援 Override Color 與發光特效
    private void drawMicroPiece(Graphics2D g, int colorCode, int startX, int startY, int size, Color overrideColor, boolean glow) {
        int[][] shape = new Tetromino(colorCode).getShape();
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[0].length; c++) {
                if (shape[r][c] != 0) {
                    int px = startX + c * size;
                    int py = startY + r * size;
                    
                    Color baseColor = (overrideColor != null) ? overrideColor : getRawColorMapping(colorCode);
                    
                    if (glow) {
                        double breathFactor = (Math.sin(System.currentTimeMillis() / 200.0) + 1.0) / 2.0;
                        g.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 60 + (int)(breathFactor * 100)));
                        g.fillRect(px - 2, py - 2, size + 4, size + 4);
                    }
                    
                    java.awt.GradientPaint gp = new java.awt.GradientPaint(px, py, baseColor.brighter(), px + size, py + size, baseColor.darker());
                    g.setPaint(gp);
                    g.fillRect(px, py, size, size);
                    g.setPaint(null);
                    
                    g.setColor(new Color(255, 255, 255, 120));
                    g.fillRect(px, py, size, 1);
                    g.fillRect(px, py, 1, size);
                    g.setColor(new Color(0, 0, 0, 120));
                    g.fillRect(px, py + size - 1, size, 1);
                    g.fillRect(px + size - 1, py, 1, size);
                    g.setColor(Color.WHITE);
                    g.drawRect(px, py, size, size);
                }
            }
        }
    }

    private void drawGridLines(Graphics2D g) {
        // --- Phase 14: 核心盤面底網格 (極暗灰色) ---
        g.setColor(new Color(30, 30, 30));
        int w = gameState.getColumns() * CELL_SIZE;
        int h = gameState.getRows() * CELL_SIZE;
        
        for (int row = 0; row <= gameState.getRows(); row++) {
            g.drawLine(0, row * CELL_SIZE, w, row * CELL_SIZE);
        }
        for (int col = 0; col <= gameState.getColumns(); col++) {
            g.drawLine(col * CELL_SIZE, 0, col * CELL_SIZE, h);
        }

        // --- Phase 14: 實體清晰邊框線 ---
        g.setColor(new Color(0, 150, 200)); // 深青藍色邊線
        g.setStroke(new BasicStroke(2f));
        g.drawLine(0, 0, 0, h);     // Left
        g.drawLine(w, 0, w, h);     // Right
        g.drawLine(0, h, w, h);     // Bottom
        g.setStroke(new BasicStroke(1f));
    }

    private void drawBoard(Graphics2D g) {
        Board board = gameState.getBoard();
        int[][] grid = board.getGrid();
        
        for (int y = 0; y < board.getRows(); y++) {
            for (int x = 0; x < board.getColumns(); x++) {
                int colorCode = grid[y][x];
                if (colorCode != 0) {
                    drawBlock(g, x, y, colorCode, true, true);
                }
            }
        }
    }

    private void drawCurrentPiece(Graphics2D g) {
        Tetromino piece = gameState.getCurrentPiece();
        if (piece == null) return;

        int[][] shape = piece.getShape();
        int colorCode = piece.getColorCode();
        int pieceX = gameState.getCurrentX();
        int pieceY = gameState.getCurrentY();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[0].length; c++) {
                if (shape[r][c] != 0) {
                    drawBlock(g, pieceX + c, pieceY + r, colorCode, true, true);
                }
            }
        }
    }

    /**
     * Phase 11: 軟下落殘影特效
     */
    private void drawDropTrail(Graphics2D g) {
        if (!gameState.isSoftDropping() || gameState.getCurrentPiece() == null) return;
        
        Tetromino piece = gameState.getCurrentPiece();
        int pieceX = gameState.getCurrentX();
        int pieceY = gameState.getCurrentY();
        int[][] shape = piece.getShape();
        
        // 向上延伸三格的透明殘影
        for (int offset = 1; offset <= 3; offset++) {
            if (pieceY - offset < 0) continue; // 超出畫面頂端不畫
            
            float alpha = 0.3f - (offset * 0.08f);
            if (alpha < 0) alpha = 0;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            
            for (int r = 0; r < shape.length; r++) {
                for (int c = 0; c < shape[0].length; c++) {
                    if (shape[r][c] != 0) {
                        int px = (pieceX + c) * CELL_SIZE;
                        int py = (pieceY + r - offset) * CELL_SIZE;
                        
                        g.setColor(new Color(255, 255, 255, 200)); 
                        g.fillRect(px, py, CELL_SIZE, CELL_SIZE);
                    }
                }
            }
        }
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    private void drawGhostPiece(Graphics2D g) {
        Tetromino piece = gameState.getCurrentPiece();
        if (piece == null) return;

        int ghostX = gameState.getCurrentX();
        int ghostY = gameState.getCurrentY();
        Board board = gameState.getBoard();

        while (board.canMove(ghostX, ghostY + 1, piece)) {
            ghostY++;
        }

        int[][] shape = piece.getShape();
        int colorCode = piece.getColorCode();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[0].length; c++) {
                if (shape[r][c] != 0) {
                    // 利用 drawBlock 中專屬 false 走外框描繪邏輯
                    drawBlock(g, ghostX + c, ghostY + r, colorCode, false, true);
                }
            }
        }
    }

    private void drawLeftPanel(Graphics2D g) {
        /* Stage 1: Disable Hold System
        g.setFont(new Font("Arial", Font.BOLD, 18));
        drawStringWithShadow(g, "HOLD", 20, 60, gameState.isCanHold() ? Color.WHITE : Color.DARK_GRAY);
        
        // Phase 14 HOLD 半透明底框
        g.setColor(new Color(255, 255, 255, 10));
        g.fillRect(15, 75, 120, 105);
        g.setColor(new Color(255, 255, 255, 20));
        g.drawRect(15, 75, 120, 105);

        Tetromino holdPiece = gameState.getHoldPiece();
        if (holdPiece != null) {
            drawPreviewPiece(g, holdPiece, 30, 95, !gameState.isCanHold());
        }
        */

        g.setFont(new Font("Arial", Font.BOLD, 18));
        drawStringWithShadow(g, "TIME", 20, 300, Color.WHITE);
        long ms = gameState.getPlayTimeMs();
        long s = (ms / 1000) % 60;
        long m = (ms / 1000) / 60;
        String timeStr = String.format("%02d:%02d", m, s);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        drawStringWithShadow(g, timeStr, 20, 325, Color.WHITE);
    }

    private void drawRightPanel(Graphics2D g) {
        int sidebarStartX = (LEFT_PANEL_COLS * CELL_SIZE) + PADDING + (gameState.getColumns() * CELL_SIZE) + PADDING;

        g.setFont(new Font("Arial", Font.BOLD, 14));
        String modeName = (gameState.getCurrentMode() == GameState.GameMode.ENDLESS) ? "MODE: ENDLESS" : "MODE: LEVEL";
        drawStringWithShadow(g, modeName, sidebarStartX + 20, 30, Color.WHITE);

        if (gameState.getCurrentMode() == GameState.GameMode.LEVEL) {
            drawStringWithShadow(g, "LEVEL: " + gameState.getLevel(), sidebarStartX + 20, 55, Color.CYAN);
        }

        // Phase 9.6: MODE 對應正確歷史紀錄標題
        g.setFont(new Font("Arial", Font.BOLD, 18));
        boolean isLevel = (gameState.getCurrentMode() == GameState.GameMode.LEVEL);
        drawStringWithShadow(g, isLevel ? "HI-LEVEL" : "HI-SCORE", sidebarStartX + 20, 100, new Color(255, 200, 0));
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        drawStringWithShadow(g, String.valueOf(gameState.getSessionHighScore()), sidebarStartX + 20, 125, new Color(255, 200, 0));

        g.setFont(new Font("Arial", Font.BOLD, 18));
        drawStringWithShadow(g, "SCORE", sidebarStartX + 20, 180, Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        drawStringWithShadow(g, String.valueOf(gameState.getScore()), sidebarStartX + 20, 205, Color.WHITE);

        g.setFont(new Font("Arial", Font.BOLD, 18));
        drawStringWithShadow(g, "NEXT", sidebarStartX + 20, 260, Color.WHITE);

        // Phase 14 NEXT 半透明底框
        g.setColor(new Color(255, 255, 255, 10));
        g.fillRect(sidebarStartX + 15, 275, 120, 300);
        g.setColor(new Color(255, 255, 255, 20));
        g.drawRect(sidebarStartX + 15, 275, 120, 300);

        // Phase 11: 多重預覽佇列佔據垂直剩餘空間
        java.util.List<Tetromino> nexts = gameState.getNextPieces();
        for (int i = 0; i < nexts.size(); i++) {
            drawPreviewPiece(g, nexts.get(i), sidebarStartX + 30, 280 + (i * 80), false);
        }
    }

    private void drawPreviewPiece(Graphics2D g, Tetromino piece, int startX, int startY, boolean isGrayedOut) {
        int[][] shape = piece.getShape();
        int colorCode = piece.getColorCode();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[0].length; c++) {
                if (shape[r][c] != 0) {
                    int px = startX + (c * CELL_SIZE);
                    int py = startY + (r * CELL_SIZE);
                    
                    Color baseColor = isGrayedOut ? Color.DARK_GRAY : getRawColorMapping(colorCode);
                    
                    // Stage 1: Basic Flat Color
                    g.setColor(baseColor);
                    g.fillRect(px, py, CELL_SIZE, CELL_SIZE);
                    
                    /* Stage 1: Disable 3D Rendering
                    java.awt.GradientPaint gp = new java.awt.GradientPaint(
                        px, py, baseColor.brighter(),
                        px + CELL_SIZE, py + CELL_SIZE, baseColor.darker()
                    );
                    g.setPaint(gp);
                    g.fillRect(px, py, CELL_SIZE, CELL_SIZE);
                    g.setPaint(null);
                    
                    // 同步套用 Phase 13 粗高光/陰影以匹配立體感
                    if (!isGrayedOut) {
                        g.setColor(new Color(255, 255, 255, 120));
                        g.fillRect(px, py, CELL_SIZE, 5);
                        g.fillRect(px, py, 5, CELL_SIZE);
                        
                        g.setColor(new Color(0, 0, 0, 120));
                        g.fillRect(px, py + CELL_SIZE - 5, CELL_SIZE, 5);
                        g.fillRect(px + CELL_SIZE - 5, py, 5, CELL_SIZE);
                    }
                    */

                    g.setColor(Color.WHITE);
                    g.drawRect(px, py, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }

    /**
     * Phase 13: 強化立體感 GradientPaint 萬能磚塊渲染器
     */
    private void drawBlock(Graphics2D g, int x, int y, int colorCode, boolean fill, boolean useMainDict) {
        int px = x * CELL_SIZE;
        int py = y * CELL_SIZE;

        if (fill) {
            Color baseColor = useMainDict ? getRawColorMapping(colorCode) : getColorMapping(colorCode);
            
            // Stage 1: Basic Flat Color
            g.setColor(baseColor);
            g.fillRect(px, py, CELL_SIZE, CELL_SIZE);
            
            /* Stage 1: Disable 3D rendering
            java.awt.GradientPaint gp = new java.awt.GradientPaint(
                px, py, baseColor.brighter(),
                px + CELL_SIZE, py + CELL_SIZE, baseColor.darker()
            );
            g.setPaint(gp);
            g.fillRect(px, py, CELL_SIZE, CELL_SIZE);
            g.setPaint(null);
            
            // 上半邊與左邊疊加強光澤
            g.setColor(new Color(255, 255, 255, 120));
            g.fillRect(px, py, CELL_SIZE, 5);
            g.fillRect(px, py, 5, CELL_SIZE);
            
            // 下邊與右邊疊加深陰影
            g.setColor(new Color(0, 0, 0, 120));
            g.fillRect(px, py + CELL_SIZE - 5, CELL_SIZE, 5);
            g.fillRect(px + CELL_SIZE - 5, py, 5, CELL_SIZE);
            */
        } else {
            // GhostPiece 依然保持原樣
            g.setColor(Color.WHITE);
            g.drawRect(px, py, CELL_SIZE, CELL_SIZE);
            g.drawRect(px + 1, py + 1, CELL_SIZE - 2, CELL_SIZE - 2); 
            return; 
        }

        g.setColor(Color.WHITE);
        g.drawRect(px, py, CELL_SIZE, CELL_SIZE);
    }

    /**
     * Phase 9.5 & 11.6: 負責疊加描繪多型態粒子以及雷電折線的運算
     */
    private void drawParticles(Graphics2D g2d) {
        for (Particle p : gameState.getParticles()) {
            float alpha = (float) p.life / p.maxLife;
            if (alpha < 0) alpha = 0;
            if (alpha > 1) alpha = 1;

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setColor(p.color);
            
            if (p.type == 0) {
                // 粉末煙霧：用圓潤的形狀膨脹，越消失越大
                int size = 5 + (int)((1f - alpha) * 6);
                g2d.fillOval((int)p.x, (int)p.y, size, size); 
            } else if (p.type == 1) {
                // 電流火花：十字銳利爆破光芒
                g2d.fillRect((int)p.x, (int)p.y, 10, 2);
                g2d.fillRect((int)p.x + 4, (int)p.y - 4, 2, 10);
            } else if (p.type == 2) {
                // Phase 11.6: 破風金芒 (細密矩形)
                int size = 2 + (int)(alpha * 3);
                g2d.fillRect((int)p.x, (int)p.y, size, size);
            }
        }
        
        // 疊加畫出閃電折線
        for (Lightning l : gameState.getLightnings()) {
            float alpha = (float) l.life / l.maxLife;
            // Phase 9.6: Quadratic fade-out 讓閃電的消散更具爆發感
            alpha = alpha * alpha; 
            if (alpha < 0) alpha = 0;
            if (alpha > 1) alpha = 1;
            
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            // 外圍柔和的青藍光晕 (加寬 BasicStroke，但大幅調低 Alpha 呈現光暈而非實體實線)
            g2d.setColor(new Color(100, 200, 255, 90)); 
            g2d.setStroke(new java.awt.BasicStroke(12f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            g2d.drawPolyline(l.mainX, l.mainY, l.mainX.length);
            
            // 中心極熱高能熾白電核 (極細絲 1f)
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new java.awt.BasicStroke(1f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            g2d.drawPolyline(l.mainX, l.mainY, l.mainX.length);

            // 繪製細密的隨機分支
            if (l.hasBranch) {
                // 分支的光暈較淡
                g2d.setColor(new Color(100, 200, 255, 60));
                g2d.setStroke(new java.awt.BasicStroke(6f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                g2d.drawPolyline(l.branchX, l.branchY, l.branchX.length);
                
                // 分支的電核稍微半透明，模擬真實閃電中較弱的分支
                g2d.setColor(new Color(255, 255, 255, 180));
                g2d.setStroke(new java.awt.BasicStroke(1f));
                g2d.drawPolyline(l.branchX, l.branchY, l.branchX.length);
            }
        }
        
        g2d.setStroke(new java.awt.BasicStroke(1f)); // 復原筆刷以免傷及其他底層UI
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    /**
     * Phase 10: 戰鬥文字特效專屬渲染
     */
    private void drawFloatingTexts(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 22));
        for (FloatingText ft : gameState.getFloatingTexts()) {
            float alpha = (float) ft.life / ft.maxLife;
            if (alpha < 0) alpha = 0;
            if (alpha > 1) alpha = 1;
            
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            
            // 下墜陰影
            g2d.setColor(Color.BLACK);
            g2d.drawString(ft.text, ft.x + 2, ft.y + 2);
            
            // 字體主色
            g2d.setColor(ft.color);
            g2d.drawString(ft.text, ft.x, ft.y);
        }
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    /**
     * Phase 9: 中央等級提升字樣 (不中斷背景的無干擾文字宣告)
     */
    private void drawLevelUpTrigger(Graphics2D g2d) {
        if (gameState.isDisplayingLevelUp()) {
            g2d.setColor(new Color(255, 215, 0)); // 色彩鮮艷的金黃色
            g2d.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 35));
            String lvlUp = "LEVEL UP!";
            int w = g2d.getFontMetrics().stringWidth(lvlUp);
            
            // 投影效果
            g2d.setColor(Color.BLACK);
            g2d.drawString(lvlUp, (gameState.getColumns() * CELL_SIZE - w) / 2 + 2, getHeight() / 2 - 48);
            
            g2d.setColor(new Color(255, 215, 0));
            g2d.drawString(lvlUp, (gameState.getColumns() * CELL_SIZE - w) / 2, getHeight() / 2 - 50);
        }
    }

    private void drawOverlay(Graphics2D g) {
        GameState.State state = gameState.getCurrentState();

        if (state == GameState.State.GAME_OVER || state == GameState.State.PAUSED) {
            
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());

            String titleStr = (state == GameState.State.GAME_OVER) ? "GAME OVER" : "PAUSED";
            Font titleFont = new Font("Arial", Font.BOLD, 48);
            g.setFont(titleFont);
            int tWidth = g.getFontMetrics().stringWidth(titleStr);
            int cx = getWidth() / 2;
            int startY = getHeight() / 2 - 60;
            
            // 標題發光
            g.setColor(new Color(255, 50, 50, 100));
            if (state == GameState.State.PAUSED) g.setColor(new Color(50, 150, 255, 100));
            g.drawString(titleStr, cx - tWidth / 2 + 2, startY + 2);
            g.setColor(Color.WHITE);
            g.drawString(titleStr, cx - tWidth / 2, startY);

            // 分數與時間資訊 (Game Over 限定)
            if (state == GameState.State.GAME_OVER) {
                g.setFont(new Font("Arial", Font.BOLD, 20));
                
                long ms = gameState.getPlayTimeMs();
                long s = (ms / 1000) % 60;
                long m = (ms / 1000) / 60;
                String timeStr = String.format("Time Survived: %02d:%02d", m, s);
                String scoreStr = "Final Score: " + gameState.getScore();
                
                drawStringWithShadow(g, scoreStr, cx - g.getFontMetrics().stringWidth(scoreStr) / 2, startY + 40, Color.YELLOW);
                drawStringWithShadow(g, timeStr, cx - g.getFontMetrics().stringWidth(timeStr) / 2, startY + 70, Color.CYAN);
                
                startY += 80;
            }

            // 互動式選項陣列
            /* Stage 1: Disable Interactive Menus
            String[] options;
            int selectionIdx;
            if (state == GameState.State.PAUSED) {
                options = new String[]{"Resume", "Restart", "Main Menu"};
                selectionIdx = gameState.getPauseSelectionIndex();
                startY += 50;
            } else {
                options = new String[]{"Retry", "Main Menu"};
                selectionIdx = gameState.getGameOverSelectionIndex();
                startY += 50;
            }

            for (int i = 0; i < options.length; i++) {
                boolean isSelected = (selectionIdx == i);
                int yPos = startY + (i * 45);
                
                if (isSelected) {
                    g.setColor(new Color(255, 215, 0));
                    drawArrow(g, cx - 60, yPos - 6, 1);
                }
                renderMenuItem(g, options[i], cx - 40, yPos, isSelected, Color.WHITE, false);
            }
            */
        }
    }
}
