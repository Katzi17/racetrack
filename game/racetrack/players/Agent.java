package game.racetrack.players;

import game.racetrack.Direction;
import game.racetrack.RaceTrackPlayer;
import game.racetrack.utils.Coin;
import game.racetrack.utils.PlayerState;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class Agent extends RaceTrackPlayer {
    public Agent(PlayerState state, Random random, int[][] track, Coin[] coins, int color) {
        super(state, random, track, coins, color);
    }

    public Direction getDirection(long remainingTime) {
        PlayerState currentState = getCurrentState();
        int[] currentPosition = { currentState.i, currentState.j };
        int[] goalPosition = { track[0].length - 1, track.length - 1 };

        return bfs(currentState, new PlayerState(goalPosition[0], goalPosition[1], 0, 0));
    }

    private PlayerState getCurrentState() {
        // Az aktuális pozíció megszerzése
        int i = state.i + state.vi;
        int j = state.j + state.vj;

        // A sebesség megszerzése
        int vi = state.vi;
        int vj = state.vj;

        // PlayerState objektum létrehozása és visszaadása
        return new PlayerState(i, j, vi, vj);
    }

    private Direction bfs(PlayerState start, PlayerState goal) {
        int[][] distance = new int[track.length][track[0].length];
        for (int i = 0; i < track.length; i++) {
            for (int j = 0; j < track[0].length; j++) {
                distance[i][j] = Integer.MAX_VALUE;
            }
        }

        Queue<PlayerState> queue = new LinkedList<>();
        queue.add(start);
        distance[start.i][start.j] = 0;

        while (!queue.isEmpty()) {
            PlayerState current = queue.poll();

            // Cél elérve
            if (current.equals(goal)) {
                // Találtunk optimális utat, most visszafele követhetjük az irányokat
                return reconstructPath(start, goal, distance);
            }

            // Lehetséges mozgások: fel, le, balra, jobbra
            int[][] moves = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] move : moves) {
                PlayerState neighbor = new PlayerState(current.i + move[0], current.j + move[1], 0, 0);

                if (isValidMove(neighbor) && distance[neighbor.i][neighbor.j] == Integer.MAX_VALUE) {
                    queue.add(neighbor);
                    distance[neighbor.i][neighbor.j] = distance[current.i][current.j] + 1;
                }
            }
        }

        // Ha nem sikerült elérni a célt, visszatérünk egy alapértelmezett iránnyal
        return new Direction(1, 0); // Példa: jobbra megyünk
    }

    private Direction reconstructPath(PlayerState start, PlayerState goal, int[][] distance) {
        PlayerState current = goal;

        while (!current.equals(start)) {
            int[][] moves = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] move : moves) {
                PlayerState neighbor = new PlayerState(current.i + move[0], current.j + move[1], 0, 0);

                if (isValidMove(neighbor) && distance[neighbor.i][neighbor.j] == distance[current.i][current.j] - 1) {
                    return new Direction(move[1], move[0]);
                }
            }
        }

        // Alapértelmezett irány, ha valami hiba történik
        return new Direction(1, 0);
    }

    private boolean isValidMove(PlayerState position) {
        int row = position.i;
        int col = position.j;
        return row >= 0 && row < track.length && col >= 0 && col < track[0].length && track[row][col] == 0;
    }


}
