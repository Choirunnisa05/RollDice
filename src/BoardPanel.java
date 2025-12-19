import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class BoardPanel extends JPanel {
    private final GameLogic gameLogic;
    private final GameFrame gameFrame;

    private javax.swing.Timer animationTimer;
    private Stack<Integer> animationPath;
    private final int ANIMATION_DELAY = 220;

    private Image boardBg, tileImg, tileFinishImg, tileStarImg;

    public BoardPanel(GameLogic gameLogic, GameFrame gameFrame) {
        this.gameLogic = gameLogic;
        this.gameFrame = gameFrame;
        setPreferredSize(new Dimension(700, 700));
        setOpaque(false);
        loadImages();
    }

    private void loadImages() {
        boardBg = loadScaled("/board/board_bg.png", 700, 700);
        tileImg = loadRaw("/board/tile.png");
        tileFinishImg = loadRaw("/board/tile_finish.png");
        tileStarImg = loadRaw("/board/tile_star.png");
    }

    private Image loadRaw(String path) {
        java.net.URL url = getClass().getResource(path);
        return (url != null) ? new ImageIcon(url).getImage() : null;
    }

    private Image loadScaled(String path, int w, int h) {
        java.net.URL url = getClass().getResource(path);
        return (url != null) ? new ImageIcon(new ImageIcon(url).getImage()
                .getScaledInstance(w, h, Image.SCALE_SMOOTH)).getImage() : null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int size = 10;
        int cellSize = Math.min(getWidth(), getHeight()) / size;

        // background board
        if (boardBg != null) {
            g.drawImage(boardBg, 0, 0, getWidth(), getHeight(), null);
        } else {
            g.setColor(new Color(210, 230, 255));
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // tiles grid
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int num = getNumberAt(row, col);
                int x = col * cellSize;
                int y = (size - 1 - row) * cellSize;

                Image useTile = tileImg;
                if (num == 100 && tileFinishImg != null) useTile = tileFinishImg;
                else if (gameLogic.isStarTile(num) && tileStarImg != null) useTile = tileStarImg;

                if (useTile != null) {
                    g2.drawImage(useTile, x, y, cellSize, cellSize, null);
                } else {
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.fillRect(x, y, cellSize, cellSize);
                    g2.setColor(Color.GRAY);
                    g2.drawRect(x, y, cellSize, cellSize);
                }

                // tile number overlay
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Arial", Font.PLAIN, 12));
                g2.drawString(String.valueOf(num), x + 5, y + 15);
            }
        }

        // players
        drawAllPlayers(g2, cellSize);
        g2.dispose();
    }

    private void drawAllPlayers(Graphics2D g, int cellSize) {
        List<GameLogic.Player> players = gameLogic.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            GameLogic.Player p = players.get(i);
            Point pos = getCellCenter(p.position, cellSize);

            Image avatar = loadRaw("/player/p" + (i + 1) + ".png");
            int tokenSize = Math.max(28, cellSize / 3);
            Shape circle = new java.awt.geom.Ellipse2D.Float(pos.x - tokenSize / 2f, pos.y - tokenSize / 2f, tokenSize, tokenSize);
            g.setClip(circle);
            if (avatar != null) {
                g.drawImage(avatar, pos.x - tokenSize / 2, pos.y - tokenSize / 2, tokenSize, tokenSize, null);
            } else {
                g.setColor(new Color(180, 180, 180));
                g.fill(circle);
            }
            g.setClip(null);
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2f));
            g.draw(circle);
        }
    }

    private Point getCellCenter(int number, int cellSize) {
        int size = 10;
        int row = (number - 1) / size;
        int colInRow = (number - 1) % size;
        int col = (row % 2 == 0) ? colInRow : (size - 1) - colInRow;
        int x = col * cellSize + cellSize / 2;
        int y = (size - 1 - row) * cellSize + cellSize / 2;
        return new Point(x, y);
    }

    private int getNumberAt(int row, int col) {
        int size = 10;
        return (row % 2 == 0) ? row * size + (col + 1) : row * size + (size - col);
    }

    public void animateMove(Stack<Integer> path, Runnable onComplete) {
        if (animationTimer != null && animationTimer.isRunning()) return;
        animationPath = new Stack<>();
        List<Integer> temp = new ArrayList<>(path);
        for (int i = temp.size() - 1; i > 0; i--) animationPath.push(temp.get(i));

        GameLogic.Player currentPlayer = gameLogic.getCurrentPlayer();
        animationTimer = new javax.swing.Timer(ANIMATION_DELAY, e -> {
            if (!animationPath.isEmpty()) {
                currentPlayer.position = animationPath.pop();
                repaint();
            } else {
                ((javax.swing.Timer) e.getSource()).stop();
                if (onComplete != null) onComplete.run();
            }
        });
        animationTimer.start();
    }
}
