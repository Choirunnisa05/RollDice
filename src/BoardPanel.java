import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class BoardPanel extends JPanel {

    private final GameLogic gameLogic;
    private final GameFrame gameFrame;
    private Timer animationTimer;
    private Stack<Integer> animationPath;
    private final int ANIMATION_DELAY = 250; // Ditingkatkan dari 180ms ke 250ms

    public BoardPanel(GameLogic gameLogic, GameFrame gameFrame) {
        this.gameLogic = gameLogic;
        this.gameFrame = gameFrame;
        setPreferredSize(new Dimension(700, 700));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int size = 10;
        int cellSize = getWidth() / size;
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int num = getNumberAt(row, col);
                int x = col * cellSize;
                int y = (size - 1 - row) * cellSize;

                g.setColor((row + col) % 2 == 0 ? new Color(240, 240, 240) : Color.WHITE);
                g.fillRect(x, y, cellSize, cellSize);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, cellSize, cellSize);

                g.setFont(new Font("Arial", Font.PLAIN, 12));
                g.drawString(String.valueOf(num), x + 5, y + 15);

                if (gameLogic.isStarTile(num)) {
                    g.setColor(new Color(255, 255, 100, 180));
                    g.fillRect(x, y, cellSize, cellSize);

                    g.setColor(Color.BLUE);
                    g.setFont(new Font("Arial", Font.BOLD, 14));
                    FontMetrics fm = g.getFontMetrics();
                    String text = "STAR";
                    int textWidth = fm.stringWidth(text);
                    int textHeight = fm.getAscent();
                    int tx = x + (cellSize - textWidth) / 2;
                    int ty = y + (cellSize + textHeight) / 2 - 6;
                    g.drawString(text, tx, ty);
                }

                if (num == 100) {
                    g.setColor(Color.RED);
                    g.setFont(new Font("Arial", Font.BOLD, 14));
                    FontMetrics fm = g.getFontMetrics();
                    String text = "FINISH";
                    int textWidth = fm.stringWidth(text);
                    int textHeight = fm.getAscent();
                    int tx = x + (cellSize - textWidth) / 2;
                    int ty = y + (cellSize + textHeight / 2) / 2;
                    g.drawString(text, tx, ty);
                }
            }
        }

        g2.setColor(new Color(0, 150, 255));
        Map<Integer, Integer> ladders = gameLogic.getLadders();
        for (Map.Entry<Integer, Integer> e : ladders.entrySet()) {
            Point p1 = getCellCenter(e.getKey(), cellSize);
            Point p2 = getCellCenter(e.getValue(), cellSize);
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        drawAllPlayers(g, cellSize);
    }

    private void drawAllPlayers(Graphics g, int cellSize) {
        List<GameLogic.Player> players = gameLogic.getPlayers();
        Color[] colors = { Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.ORANGE };

        for (int i = 0; i < players.size(); i++) {
            GameLogic.Player p = players.get(i);
            Point pos = getCellCenter(p.position, cellSize);
            Color playerColor = colors[i % colors.length];

            g.setColor(playerColor);
            g.fillOval(pos.x - 12, pos.y - 12, 24, 24);
            g.setColor(Color.BLACK);
            g.drawOval(pos.x - 12, pos.y - 12, 24, 24);

            String initial = String.valueOf(i + 1);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            int tx = pos.x - fm.stringWidth(initial) / 2;
            int ty = pos.y + fm.getAscent() / 2 - 1;
            g.drawString(initial, tx, ty);
        }
    }

    private Point getCellCenter(int number, int cellSize) {
        int size = 10;
        int row = (number - 1) / size;
        int colInRow = (number - 1) % size;

        int col;
        if (row % 2 == 0)
            col = colInRow;
        else
            col = (size - 1) - colInRow;

        int x = col * cellSize + cellSize / 2;
        int y = (size - 1 - row) * cellSize + cellSize / 2;
        return new Point(x, y);
    }

    private int getNumberAt(int row, int col) {
        int size = 10;
        if (row % 2 == 0)
            return row * size + (col + 1);
        else
            return row * size + (size - col);
    }

    public void animateMove(Stack<Integer> path, Runnable onComplete) {
        if (animationTimer != null && animationTimer.isRunning()) return;

        animationPath = new Stack<>();
        List<Integer> temp = new ArrayList<>(path);

        for (int i = temp.size() - 1; i > 0; i--) {
            animationPath.push(temp.get(i));
        }

        GameLogic.Player currentPlayer = gameLogic.getCurrentPlayer();

        animationTimer = new Timer(ANIMATION_DELAY, e -> {
            if (!animationPath.isEmpty()) {
                int nextPos = animationPath.pop();
                currentPlayer.position = nextPos;

                gameFrame.playSound("steps.wav");

                repaint();
            } else {
                ((Timer) e.getSource()).stop();

                if (onComplete != null) {
                    try { onComplete.run(); } catch (Exception ex) { ex.printStackTrace(); }
                }
            }
        });

        animationTimer.start();
    }
}