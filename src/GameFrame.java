import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.io.File;
import java.net.URL;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.util.Map;

public class GameFrame extends JFrame {

    private final GameLogic game;
    private final JPanel playerPanel;
    private final JButton rollButton;
    private final JLabel infoLabel;
    private final BoardPanel boardPanel;
    private final JLabel diceLabel;
    private final ImageIcon[] diceIcons = new ImageIcon[7];
    private final JPanel rankingPanel;
    private final JLabel statusLabel;
    private Clip bgClip;

    public GameFrame() {
        loadDiceIcons();

        int numPlayers = askPlayerCount();
        List<String> names = askPlayerNames(numPlayers);
        game = new GameLogic(100, names);

        setTitle("Roll Dice Game – Multi Player Enhanced");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        infoLabel = new JLabel("Silakan tekan Roll Dice", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 24));
        infoLabel.setOpaque(true);
        infoLabel.setBackground(Color.WHITE);
        add(infoLabel, BorderLayout.NORTH);

        playerPanel = new JPanel(new GridLayout(0, 1));
        updatePlayerPanel();
        playerPanel.setPreferredSize(new Dimension(220, 700));
        add(playerPanel, BorderLayout.WEST);

        boardPanel = new BoardPanel(game, this);
        add(boardPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(200, 700));

        rollButton = new JButton("ROLL DICE");
        rollButton.setFont(new Font("Arial", Font.BOLD, 30));
        rollButton.addActionListener(e -> rollAction());

        diceLabel = new JLabel(diceIcons[1] != null ? diceIcons[1] : new ImageIcon(createPlaceholderImage(80,80,"1")), SwingConstants.CENTER);
        diceLabel.setFont(new Font("Arial", Font.BOLD, 48));
        diceLabel.setPreferredSize(new Dimension(150, 150));

        rankingPanel = new JPanel();
        rankingPanel.setLayout(new BoxLayout(rankingPanel, BoxLayout.Y_AXIS));
        rankingPanel.setBorder(BorderFactory.createTitledBorder("🏆 Top Winners"));
        updateRankingPanel();

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        JPanel diceAndRank = new JPanel(new BorderLayout());
        diceAndRank.add(diceLabel, BorderLayout.NORTH);
        diceAndRank.add(statusLabel, BorderLayout.CENTER);
        diceAndRank.add(rankingPanel, BorderLayout.SOUTH);

        rightPanel.add(rollButton, BorderLayout.NORTH);
        rightPanel.add(diceAndRank, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        playBackgroundMusic();

        setVisible(true);
    }

    private void displayTempMessage(String message, Color color) {
        statusLabel.setText("<html><div style='text-align: center; color: " + (color == Color.RED ? "red" : "green") + ";'>" + message + "</div></html>");

        Timer clearTimer = new Timer(5000, e -> statusLabel.setText(""));
        clearTimer.setRepeats(false);
        clearTimer.start();
    }

    private void displayStarMessage(GameLogic.Player cp) {
        String msg = "<html><div style='text-align: center; color: purple;'>★ Extra Turn! Sisa: " + cp.extraTurns + "</div></html>";
        statusLabel.setText(msg);

        Timer clearTimer = new Timer(5000, e -> statusLabel.setText(""));
        clearTimer.setRepeats(false);
        clearTimer.start();
    }

    private void updateRankingPanel() {
        rankingPanel.removeAll();
        List<Map.Entry<String, Integer>> topWinners = game.getTopWinners();

        if (topWinners.isEmpty()) {
            rankingPanel.add(new JLabel("Belum ada kemenangan."));
            return;
        }

        int rank = 1;
        for (Map.Entry<String, Integer> entry : topWinners) {
            JLabel rankLabel = new JLabel(rank + ". " + entry.getKey() + " (" + entry.getValue() + " win)");
            rankLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            rankingPanel.add(rankLabel);
            rank++;
        }
        rankingPanel.revalidate();
        rankingPanel.repaint();
    }

    private void loadDiceIcons() {
        for (int i = 1; i <= 6; i++) {
            URL url = getClass().getResource("/dice" + i + ".png");
            int diceSize = 80;
            if (url != null) {
                diceIcons[i] = new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(diceSize, diceSize, Image.SCALE_SMOOTH));
            } else {
                System.err.println("❌ ERROR: Dice icon not found in classpath: dice" + i + ".png. Using placeholder.");
                diceIcons[i] = new ImageIcon(createPlaceholderImage(diceSize, diceSize, String.valueOf(i)));
            }
        }
    }

    private Image createPlaceholderImage(int width, int height, String text) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, width - 1, height - 1);
        g.setFont(new Font("Arial", Font.BOLD, width / 2));
        FontMetrics fm = g.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        int y = (height + fm.getAscent()) / 2 - 2;
        g.drawString(text, x, y);
        g.dispose();
        return image;
    }

    public void playSound(String filename) {
        try {
            URL url = this.getClass().getResource("/" + filename);
            if (url == null) {
                System.err.println("❌ ERROR: Audio file not found in classpath: " + filename);
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            System.err.println("⚠️ Warning: Error playing sound for " + filename + ": " + e.getMessage());
        }
    }

    private void playBackgroundMusic() {
        try {
            URL url = this.getClass().getResource("/bg_music.wav");
            if (url == null) {
                System.out.println("Background music (bg_music.wav) not found. Skipping background music.");
                return;
            }
            System.out.println("✅ Success: Background music found: bg_music.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            bgClip = AudioSystem.getClip();
            bgClip.open(audioIn);
            bgClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgClip.start();
        } catch (Exception e) {
            System.err.println("⚠️ Warning: Error playing background music: " + e.getMessage());
        }
    }

    private void animateDiceRoll(int finalResult, Runnable onComplete) {
        playSound("roll_dice.wav");

        Timer rollTimer = new Timer(0, null); // DIKOREKSI: Delay 75ms
        final int[] rolls = { 0 };
        final int totalRolls = 15; // DIKOREKSI: 15 frames total

        rollTimer.addActionListener(e -> {
            if (rolls[0] < totalRolls) {
                int randomRoll = game.rand.nextInt(6) + 1;
                diceLabel.setIcon(diceIcons[randomRoll]);
                rolls[0]++;
            } else {
                ((Timer) e.getSource()).stop();
                diceLabel.setIcon(diceIcons[finalResult]);
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
        rollTimer.start();
    }

    private void rollAction() {
        rollButton.setEnabled(false);
        GameLogic.Player cp = game.getCurrentPlayer();

        int dice = game.rollDice();
        boolean green = cp.greenMove;

        animateDiceRoll(dice, () -> {

            Stack<Integer> movePath = game.moveCurrentPlayer(dice);

            String moveDirection = green ? "Maju" : "Mundur";

            infoLabel.setText("🎲 " + cp.name + " melempar dadu: " + dice +
                    " (" + moveDirection + ")");
            infoLabel.setBackground(green ? new Color(180, 255, 180) : new Color(255, 180, 180));

            String msg = cp.name + " " + moveDirection + " " + dice + " langkah. Posisi: " + cp.position;
            displayTempMessage(msg, green ? Color.GREEN : Color.RED);

            Timer resetTimer = new Timer(2000, e -> infoLabel.setBackground(Color.WHITE));
            resetTimer.setRepeats(false);
            resetTimer.start();

            boardPanel.animateMove(movePath, this::endTurnCheck);
        });
    }

    private void endTurnCheck() {
        GameLogic.Player cp = game.getCurrentPlayer();

        if (cp.position >= game.getNodeCount()) {
            game.recordWin(cp.name);
            updateRankingPanel();

            JOptionPane.showMessageDialog(this,
                    "🎉 Selamat " + cp.name + " telah mencapai FINISH!");
            rollButton.setEnabled(false);
            return;
        }

        if (cp.extraTurns > 0) {
            displayStarMessage(cp);
        }

        game.advanceTurn();
        updatePlayerPanel();

        GameLogic.Player next = game.getCurrentPlayer();

        String turnInfo = "Giliran ke-" + (game.getCurrentPlayerIndex() + 1)
                + " dari " + game.getPlayers().size() + ": " + next.name;

        if (next.extraTurns > 0) {
            turnInfo = next.name + " mendapat giliran ekstra! Sisa: " + next.extraTurns;
        }

        infoLabel.setText(turnInfo);
        infoLabel.setBackground(Color.WHITE);

        rollButton.setEnabled(true);
    }

    private int askPlayerCount() {
        while (true) {
            String input = JOptionPane.showInputDialog(
                    null, "Berapa pemain? (1–5)", "Player Count", JOptionPane.QUESTION_MESSAGE);
            if (input == null) System.exit(0);
            try {
                int n = Integer.parseInt(input);
                if (n >= 1 && n <= 5) return n;
                else JOptionPane.showMessageDialog(null, "Jumlah pemain harus antara 1 dan 5.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ignored) {
                JOptionPane.showMessageDialog(null, "Masukkan angka yang valid untuk jumlah pemain.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private List<String> askPlayerNames(int n) {
        List<String> names = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            String name = JOptionPane.showInputDialog(
                    null, "Nama Player " + i + ":", "Input Nama", JOptionPane.QUESTION_MESSAGE);
            if (name == null || name.trim().isEmpty()) name = "Player " + i;
            names.add(name);
        }
        return names;
    }

    private void updatePlayerPanel() {
        playerPanel.removeAll();
        List<GameLogic.Player> list = game.getPlayers();
        int currentIndex = game.getCurrentPlayerIndex();

        Color[] colors = { Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.ORANGE };

        for (int i = 0; i < list.size(); i++) {
            GameLogic.Player p = list.get(i);
            JPanel box = new JPanel(new GridLayout(3, 1));
            box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            if (i == currentIndex) {
                Color tokenColor = colors[i % colors.length];
                int r = Math.min(255, tokenColor.getRed() + 120);
                int g = Math.min(255, tokenColor.getGreen() + 120);
                int b = Math.min(255, tokenColor.getBlue() + 120);
                Color soft = new Color(r, g, b);
                box.setBackground(soft);
            } else {
                box.setBackground(Color.WHITE);
            }

            JLabel nameLabel = new JLabel((i == currentIndex ? "→ " : "   ") + p.name);
            nameLabel.setFont(new Font("Arial", Font.BOLD, 18));

            JLabel posLabel = new JLabel("Posisi: " + p.position);
            posLabel.setFont(new Font("Arial", Font.PLAIN, 15));

            String statText = p.extraTurns > 0 ? "★ Extra Turn (sisa " + p.extraTurns + ")" : "";
            JLabel stat = new JLabel(statText);
            stat.setFont(new Font("Arial", Font.ITALIC, 13));

            box.add(nameLabel);
            box.add(posLabel);
            box.add(stat);
            playerPanel.add(box);
        }

        playerPanel.revalidate();
        playerPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameFrame());
    }
}