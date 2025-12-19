import java.util.*;

public class ShortestPathSolver {

    public static List<Integer> getShortestPath(int start, int goal, int nodeCount, Map<Integer, Integer> ladders) {
        Map<Integer, Integer> parent = new HashMap<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(start);
        parent.put(start, null);

        while (!queue.isEmpty()) {
            int cur = queue.poll();
            if (cur == goal) break;

            List<Integer> neighbors = new ArrayList<>();

            if (cur + 1 <= nodeCount) {
                neighbors.add(cur + 1);
            }

            if (ladders.containsKey(cur)) {
                neighbors.add(ladders.get(cur));
            }

            for (int nb : neighbors) {
                if (!parent.containsKey(nb)) {
                    parent.put(nb, cur);
                    queue.add(nb);
                }
            }
        }

        List<Integer> path = new ArrayList<>();
        Integer cur = goal;
        while (cur != null) {
            path.add(0, cur);
            cur = parent.get(cur);
        }

        if (path.isEmpty() || path.get(0) != start) {
            path.clear();
            path.add(start);
        }

        return path;
    }
}
