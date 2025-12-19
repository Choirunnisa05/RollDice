import java.util.*;
import java.util.stream.Collectors;

public class GameLogic {

    public static class Player {
        public String name;
        public int position = 1;
        public boolean greenMove;
        public int extraTurns = 0;
        public Stack<Integer> stepHistory = new Stack<>();

        public Player(String name) {
            this.name = name;
            this.stepHistory.push(1);
        }
    }

    private final int nodeCount;
    private final List<Player> players;
    private int currentPlayerIndex = 0;
    public final Random rand = new Random();
    private final Map<Integer, Integer> ladders = new HashMap<>();
    private final Set<Integer> starTiles = new HashSet<>();
    private int lastDiceRoll = 0;

    private final Map<String, Integer> winHistory = new HashMap<>();

    public GameLogic(int nodeCount, List<String> playerNames) {
        this.nodeCount = nodeCount;
        this.players = new ArrayList<>();

        for (String name : playerNames) {
            players.add(new Player(name));
            winHistory.put(name, 0);
        }

        Collections.shuffle(players, rand);

        generateStars();
        generateLadders();
    }

    // ===================== LADDER AWAL =====================
    private void generateLadders() {
        ladders.clear();
        ladders.put(3, 22);
        ladders.put(8, 26);
        ladders.put(28, 55);
        ladders.put(58, 77);
        ladders.put(75, 96);
    }

    private void generateStars() {
        starTiles.clear();
        for (int i = 5; i < nodeCount; i += 5) {
            starTiles.add(i);
        }
    }

    public boolean isStarTile(int pos) {
        return starTiles.contains(pos);
    }

    public Map<Integer, Integer> getLadders() {
        return ladders;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public int getLastDiceRoll() {
        return lastDiceRoll;
    }

    public void recordWin(String playerName) {
        winHistory.put(playerName, winHistory.getOrDefault(playerName, 0) + 1);
    }

    public List<Map.Entry<String, Integer>> getTopWinners() {
        return winHistory.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .collect(Collectors.toList());
    }

    public int rollDice() {
        int dice = rand.nextInt(6) + 1;
        Player p = getCurrentPlayer();
        p.greenMove = rand.nextDouble() < 0.8;
        lastDiceRoll = dice;
        return dice;
    }

    // ===================== MOVE PLAYER =====================
    public Stack<Integer> moveCurrentPlayer(int dice) {
        Player p = getCurrentPlayer();
        int currentPos = p.position;
        Stack<Integer> moves = new Stack<>();
        moves.push(currentPos);
        p.extraTurns = 0;

        if (p.greenMove) { // MAJU
            // lakukan langkah biasa
            for (int i = 0; i < dice; i++) {
                currentPos++;
                if (currentPos > nodeCount) {
                    currentPos = nodeCount;
                    moves.push(currentPos);
                    p.stepHistory.push(currentPos);
                    break;
                }
                moves.push(currentPos);
                p.stepHistory.push(currentPos);
            }

            // setelah langkah, cek tangga hanya jika berhenti tepat di dasar tangga
            if (ladders.containsKey(currentPos)) {
                currentPos = ladders.get(currentPos);
                moves.push(currentPos);
                p.stepHistory.push(currentPos);
            }

        } else { // MERAH → MUNDUR
            // mundur sesuai angka dice, minimum posisi = 1, tangga tidak berlaku
            for (int i = 0; i < dice; i++) {
                currentPos--;
                if (currentPos < 1) {
                    currentPos = 1;
                    moves.push(currentPos);
                    p.stepHistory.push(currentPos);
                    break;
                }
                moves.push(currentPos);
                p.stepHistory.push(currentPos);
            }
        }

        p.position = currentPos;

        if (isStarTile(p.position)) {
            p.extraTurns = 2;
        }

        return moves;
    }

    public void advanceTurn() {
        Player p = getCurrentPlayer();
        if (p.extraTurns > 0) {
            p.extraTurns--;
            return;
        }
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    private boolean isPrime(int n) {
        if (n < 2) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) return false;
        }
        return true;
    }

    // ===================== RESET GAME =====================
    public void resetGame() {
        currentPlayerIndex = 0;
        lastDiceRoll = 0;

        for (Player p : players) {
            p.position = 1;
            p.extraTurns = 0;
            p.greenMove = true;
            p.stepHistory.clear();
            p.stepHistory.push(1);
        }

        regenerateLadders(); // ladder baru setiap game
    }

    private void regenerateLadders() {
        ladders.clear();
        int ladderCount = 5;
        Set<Integer> used = new HashSet<>();

        while (ladders.size() < ladderCount) {
            int from = rand.nextInt(nodeCount - 20) + 2;
            int to = from + rand.nextInt(15) + 5;

            if (to >= nodeCount) continue;
            if (used.contains(from)) continue;
            if (starTiles.contains(from)) continue;

            ladders.put(from, to);
            used.add(from);
        }
    }
}
