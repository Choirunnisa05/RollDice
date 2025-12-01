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

        generateLadders();
        generateStars();
    }

    private void generateLadders() {
        ladders.clear();
        ladders.put(3, 22);
        ladders.put(8, 26);
        ladders.put(28, 55);
        ladders.put(58, 77);
        ladders.put(75, 96);
        for (Map.Entry<Integer,Integer> e : ladders.entrySet()) {
            int from = e.getKey();
            int to = e.getValue();
            if (from < 1 || from > nodeCount || to < 1 || to > nodeCount) {
                System.err.println("Invalid ladder mapping: " + from + " -> " + to);
            }
        }
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

    public Stack<Integer> moveCurrentPlayer(int dice) {
        Player p = getCurrentPlayer();
        int startPos = p.position;
        boolean startOnPrime = isPrime(startPos);
        int currentPos = startPos;

        Stack<Integer> moves = new Stack<>();
        moves.push(startPos);

        List<Integer> reversePath = new ArrayList<>();

        p.extraTurns = 0;

        // JALUR PRIMA: Shortest Path + Tangga aktif
        if (startOnPrime) {
            List<Integer> path = ShortestPathSolver.getShortestPath(startPos, nodeCount, nodeCount, ladders);

            int steps = Math.min(dice, Math.max(0, path.size() - 1));

            p.stepHistory.clear();
            p.stepHistory.push(startPos);

            for (int k = 1; k <= steps; k++) {
                currentPos = path.get(k);

                if (currentPos > nodeCount) {
                    currentPos = nodeCount;
                    moves.push(currentPos);
                    p.stepHistory.push(currentPos);
                    break;
                }

                moves.push(currentPos);
                p.stepHistory.push(currentPos);

                if (ladders.containsKey(currentPos)) {
                    currentPos = ladders.get(currentPos);
                    moves.push(currentPos);
                    p.stepHistory.push(currentPos);
                    break;
                }
            }

            // JALUR NON-PRIMA: Gerak Normal (Tangga TIDAK AKTIF)
        } else {
            int dir = p.greenMove ? 1 : -1;

            if (dir == 1) { // MAJU NORMAL

                if (!p.stepHistory.isEmpty()) {
                    if (p.stepHistory.peek() == startPos) {
                        p.stepHistory.pop();
                    }
                } else {
                    p.stepHistory.push(startPos);
                }


                for (int k = 1; k <= dice; k++) {
                    currentPos += dir;

                    if (currentPos < 1) currentPos = 1;

                    if (currentPos > nodeCount) {
                        currentPos = nodeCount;
                        moves.push(currentPos);
                        p.stepHistory.push(currentPos);
                        break;
                    }

                    moves.push(currentPos);
                    p.stepHistory.push(currentPos);

                    // Tangga TIDAK AKTIF di sini
                }
            } else { // MUNDUR NORMAL (Berbasis Stack History)

                if (!p.stepHistory.isEmpty() && p.stepHistory.peek() == startPos) {
                    p.stepHistory.pop();
                }

                int stepsToPop = Math.min(dice, p.stepHistory.size());

                for (int k = 1; k <= stepsToPop; k++) {
                    if (!p.stepHistory.isEmpty()) {
                        int poppedPos = p.stepHistory.pop();
                        reversePath.add(poppedPos);
                        currentPos = poppedPos;
                    } else {
                        currentPos = 1;
                        break;
                    }
                }

                p.stepHistory.push(currentPos);

                for (int i = reversePath.size() - 1; i >= 0; i--) {
                    moves.push(reversePath.get(i));
                }
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
}