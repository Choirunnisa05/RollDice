import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class GameFrame extends JFrame {
    private final GameLogic game;
    private final BoardPanel boardPanel;

    private final JLabel turnLabel;
    private final JLabel profileCircle;
    private final JLabel diceLabel;
    private final JButton rollButton;
    private final JButton startNewGameButton;
    private final JPanel leaderboardPanel;

    private final ImageIcon[] diceGreen = new ImageIcon[7];
    private final ImageIcon[] diceRed = new ImageIcon[7];
    private Image panelRightBg, panelLeftBg, leaderboardBg;

    private final int ANIMATION_DELAY = 220; // untuk suara step

    public GameFrame() {
        int numPlayers = askPlayerCount();
        List<String> names = askPlayerNames(numPlayers);
        game = new GameLogic(100, names);

        setTitle("Roll Dice Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        loadUiImages();

        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (panelLeftBg != null) g.drawImage(panelLeftBg, 0, 0, getWidth(), getHeight(), null);
            }
        };
        leftPanel.setPreferredSize(new Dimension(220, 750));
        add(leftPanel, BorderLayout.WEST);

        boardPanel = new BoardPanel(game, this);
        add(boardPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (panelRightBg != null) g.drawImage(panelRightBg, 0, 0, getWidth(), getHeight(), null);
            }
        };
        rightPanel.setPreferredSize(new Dimension(280, 750));
        add(rightPanel, BorderLayout.EAST);

        turnLabel = new JLabel("its your turn", SwingConstants.CENTER);
        turnLabel.setFont(new Font("Arial", Font.BOLD, 24));
        rightPanel.add(turnLabel, BorderLayout.NORTH);

        JPanel centerStack = new JPanel();
        centerStack.setOpaque(false);
        centerStack.setLayout(new BoxLayout(centerStack, BoxLayout.Y_AXIS));

        profileCircle = new JLabel();
        profileCircle.setAlignmentX(Component.CENTER_ALIGNMENT);
        profileCircle.setPreferredSize(new Dimension(120, 120));
        centerStack.add(Box.createVerticalStrut(10));
        centerStack.add(profileCircle);

        diceLabel = new JLabel();
        diceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        diceLabel.setPreferredSize(new Dimension(120, 120));
        centerStack.add(Box.createVerticalStrut(10));
        centerStack.add(diceLabel);

        leaderboardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (leaderboardBg != null) g.drawImage(leaderboardBg, 0, 0, getWidth(), getHeight(), null);
            }
        };
        leaderboardPanel.setOpaque(false);
        leaderboardPanel.setLayout(new BoxLayout(leaderboardPanel, BoxLayout.Y_AXIS));
        leaderboardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centerStack.add(Box.createVerticalStrut(10));
        centerStack.add(leaderboardPanel);

        rightPanel.add(centerStack, BorderLayout.CENTER);

        JPanel bottomButtons = new JPanel(new GridLayout(2, 1, 10, 10));
        bottomButtons.setOpaque(false);

        rollButton = createImageButton("/ui/roll_button.png", "ROLL DICE");
        rollButton.addActionListener(e -> rollAction());
        bottomButtons.add(rollButton);

        startNewGameButton = new JButton("START NEW GAME");
        startNewGameButton.setFont(new Font("Arial", Font.BOLD, 16));
        startNewGameButton.addActionListener(e -> {
            game.resetGame();
            rollButton.setEnabled(true);
            updateAllUI();
        });
        bottomButtons.add(startNewGameButton);

        rightPanel.add(bottomButtons, BorderLayout.SOUTH);

        loadDiceIcons();
        diceLabel.setIcon(diceGreen[1] != null ? diceGreen[1] : new ImageIcon(createPlaceholderImage(100, 100, "1")));

        updateAllUI();

        setVisible(true);
    }

    private JButton createImageButton(String path, String fallbackText) {
        URL url = getClass().getResource(path);
        JButton btn = new JButton(fallbackText);
        btn.setFocusPainted(false);
        if (url != null) {
            Image img = new ImageIcon(url).getImage().getScaledInstance(220, 60, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(img));
            btn.setText("");
            btn.setBorder(BorderFactory.createEmptyBorder());
            btn.setContentAreaFilled(false);
        }
        return btn;
    }

    private void loadUiImages() {
        panelRightBg = loadImage("/ui/panel_right.png");
        panelLeftBg = loadImage("/ui/panel_left.png");
        leaderboardBg = loadImage("/ui/leaderboard_bg.png");
    }

    private void loadDiceIcons() {
        int diceSize = 100;
        for (int i = 1; i <= 6; i++) {
            diceGreen[i] = loadIconScaled("/dice_green_" + i + ".png", diceSize, diceSize);
            diceRed[i] = loadIconScaled("/dice_red_" + i + ".png", diceSize, diceSize);
        }
    }

    private Image loadImage(String path) {
        URL url = getClass().getResource(path);
        return (url != null) ? new ImageIcon(url).getImage() : null;
    }

    private ImageIcon loadIconScaled(String path, int w, int h) {
        URL url = getClass().getResource(path);
        if (url == null) return null;
        Image scaled = new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private void updateAllUI() {
        updateCurrentTurnHeader();
        updateLeaderboardPanel();
        boardPanel.repaint();
    }

    private void updateCurrentTurnHeader() {
        GameLogic.Player cp = game.getCurrentPlayer();
        turnLabel.setText("its your turn: " + cp.name);

        int avatarIndex = game.getCurrentPlayerIndex() + 1;
        Image avatar = loadImage("/player/p" + avatarIndex + ".png");
        int size = 120;
        if (avatar != null) profileCircle.setIcon(new ImageIcon(toCircular(avatar, size, size)));
        else profileCircle.setIcon(new ImageIcon(createPlaceholderImage(size, size, "P" + avatarIndex)));

        ImageIcon[] set = cp.greenMove ? diceGreen : diceRed;
        diceLabel.setIcon(set[1] != null ? set[1] : new ImageIcon(createPlaceholderImage(100, 100, "1")));
    }

    private BufferedImage toCircular(Image img, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape circle = new java.awt.geom.Ellipse2D.Float(0, 0, w, h);
        g2.setClip(circle);
        g2.drawImage(img, 0, 0, w, h, null);
        g2.setClip(null);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3f));
        g2.draw(circle);
        g2.dispose();
        return out;
    }

    private void updateLeaderboardPanel() {
        leaderboardPanel.removeAll();
        JLabel title = new JLabel("leader board", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        leaderboardPanel.add(title);
        leaderboardPanel.add(Box.createVerticalStrut(8));

        List<Map.Entry<String, Integer>> top = game.getTopWinners();
        if (top.isEmpty()) {
            JLabel empty = new JLabel("Belum ada kemenangan.", SwingConstants.CENTER);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            leaderboardPanel.add(empty);
        } else {
            for (Map.Entry<String, Integer> e : top) {
                JLabel row = new JLabel(e.getKey() + " — " + e.getValue() + " win");
                row.setFont(new Font("Arial", Font.PLAIN, 14));
                row.setAlignmentX(Component.CENTER_ALIGNMENT);
                leaderboardPanel.add(row);
            }
        }
        leaderboardPanel.revalidate();
        leaderboardPanel.repaint();
    }

    private void animateDiceRoll(boolean green, int finalResult, Runnable onComplete) {
        final ImageIcon[] set = green ? diceGreen : diceRed;
        javax.swing.Timer rollTimer = new javax.swing.Timer(70, null);
        final int[] rolls = {0};
        final int totalRolls = 14;

        rollTimer.addActionListener(e -> {
            if (rolls[0] < totalRolls) {
                int val = (int) (Math.random() * 6) + 1;
                diceLabel.setIcon(set[val]);
                rolls[0]++;
            } else {
                ((javax.swing.Timer) e.getSource()).stop();
                diceLabel.setIcon(set[finalResult]);
                if (onComplete != null) onComplete.run();
            }
        });
        rollTimer.start();
    }

    private void rollAction() {
        rollButton.setEnabled(false);
        GameLogic.Player cp = game.getCurrentPlayer();
        int dice = game.rollDice();

        playSound("roll_dice.wav");

        animateDiceRoll(cp.greenMove, dice, () -> {
            Stack<Integer> movePath = game.moveCurrentPlayer(dice);
            new Thread(() -> playStepSounds(movePath.size())).start();
            boardPanel.animateMove(movePath, this::endTurnCheck);
        });
    }

    private void playStepSounds(int stepCount) {
        for (int i = 0; i < stepCount; i++) {
            playSound("steps.wav");
            try { Thread.sleep(ANIMATION_DELAY); } catch (InterruptedException ignored) {}
        }
    }

    private void playSound(String filename) {
        try {
            URL url = getClass().getResource("/sounds/" + filename);
            if (url == null) return;
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void endTurnCheck() {
        GameLogic.Player cp = game.getCurrentPlayer();
        if (cp.position >= game.getNodeCount()) {
            game.recordWin(cp.name);
            updateLeaderboardPanel();
            JOptionPane.showMessageDialog(this, "Selamat " + cp.name + " telah mencapai FINISH!");
            rollButton.setEnabled(false);
            return;
        }

        game.advanceTurn();
        updateAllUI();
        rollButton.setEnabled(true);
    }

    private int askPlayerCount() {
        while (true) {
            String input = JOptionPane.showInputDialog(null, "Berapa pemain? (1-5)", "Player Count", JOptionPane.QUESTION_MESSAGE);
            if (input == null) System.exit(0);
            try {
                int n = Integer.parseInt(input.trim());
                if (n >= 1 && n <= 5) return n;
                else JOptionPane.showMessageDialog(null, "Jumlah pemain harus antara 1 dan 5.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ignored) {
                JOptionPane.showMessageDialog(null, "Masukkan angka yang valid.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private List<String> askPlayerNames(int n) {
        List<String> names = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            String name = JOptionPane.showInputDialog(null, "Nama Player " + i + ":", "Input Nama", JOptionPane.QUESTION_MESSAGE);
            if (name == null || name.trim().isEmpty()) name = "Player " + i;
            names.add(name.trim());
        }
        return names;
    }

    private BufferedImage createPlaceholderImage(int width, int height, String text) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, width - 1, height - 1);
        g.setFont(new Font("Arial", Font.BOLD, Math.max(12, width / 3)));
        FontMetrics fm = g.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        int y = (height + fm.getAscent()) / 2 - 2;
        g.drawString(text, x, y);
        g.dispose();
        return image;
    }
}
