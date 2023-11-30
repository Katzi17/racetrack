/*
 * Copyright (c) 2008 University of Szeged
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package game.racetrack;

import java.awt.Color;
import java.awt.Frame;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import game.engine.Action;
import game.engine.Game;
import game.engine.ui.Drawable;
import game.engine.ui.GameFrame;
import game.engine.ui.GameObject;
import game.engine.utils.Pair;
import game.engine.utils.Utils;
import game.racetrack.players.DummyPlayer;
import game.racetrack.players.HumanPlayer;
import game.racetrack.ui.FilledOvalObject;
import game.racetrack.ui.RaceTrackCanvas;
import game.racetrack.ui.FilledRectangleObject;
import game.racetrack.utils.Cell;
import game.racetrack.utils.Coin;
import game.racetrack.utils.PathCell;
import game.racetrack.utils.PlayerState;

/**
 * https://en.wikipedia.org/wiki/Racetrack_(game)
 * https://3dpancakes.typepad.com/ernie/2009/06/how-hard-is-optimal-racing.html
 * https://harmmade.com/vectorracer/
 */
public class RaceTrackGame implements Game<RaceTrackPlayer, Direction>, Drawable {
  
  
  private static final int[] directions = new int[] {-2, -1, 1, 2};
  
  /** initial value of the track */
  public static final int INIT = 0;
  /** represents an empty cell on the track */
  public static final int EMPTY = 1 << 0;
  /** represents of a wall on the track */
  public static final int WALL = 1 << 1;
  /** represents the finish cells on the track */
  public static final int FINISH = 1 << 2;
  /** represents a trace of a path on the track */
  public static final int TRACE = 1 << 3;
  /** represents a coin on the track */
  public static final int COIN = 1 << 4;
  /** represents the players on the track */
  public static final int[] PLAYERS = new int[] {1 << 5, 1 << 6, 1 << 7, 1 << 8};
  /** represents all possible directions */
  public static final Direction[] DIRECTIONS = new Direction[] {
      new Direction(0, 0),
      new Direction(0, -1), 
      new Direction(-1, -1),
      new Direction(-1, 0),
      new Direction(-1, 1),
      new Direction(0, 1),
      new Direction(1, 1),
      new Direction(1, 0),
      new Direction(1, -1)
      };
  /** maps the direction names to direction objects */
  public static final Map<String, Direction> NAME2DIR = new TreeMap<String, Direction>();
  /** character representation of the game objects */
  public static final Map<Integer, String> VIEW = new TreeMap<Integer, String>();
  static {
    NAME2DIR.put("0", DIRECTIONS[0]);
    NAME2DIR.put("W", DIRECTIONS[1]);
    NAME2DIR.put("NW", DIRECTIONS[2]);
    NAME2DIR.put("N", DIRECTIONS[3]);
    NAME2DIR.put("NE", DIRECTIONS[4]);
    NAME2DIR.put("E", DIRECTIONS[5]);
    NAME2DIR.put("SE", DIRECTIONS[6]);
    NAME2DIR.put("S", DIRECTIONS[7]);
    NAME2DIR.put("SW", DIRECTIONS[8]);
    
    VIEW.put(INIT, ".");
    VIEW.put(EMPTY, " ");
    VIEW.put(WALL, "#");
    VIEW.put(FINISH, "-");
    VIEW.put(TRACE, "~");
    VIEW.put(COIN, "*");
    
    VIEW.put(PLAYERS[0], "X");
    VIEW.put(PLAYERS[1], "Y");
    VIEW.put(PLAYERS[2], "Z");
    VIEW.put(PLAYERS[3], "W");
  }
  /** number of rows of the track */
  public final int n;
  /** number of columns of the track */
  public final int m;
  /** scale factor of the track */
  public final int scale;
  /** number of coins on the track */
  public final int numCoins;
  /** random seed */
  public final long seed;
  /** maximal timeout of the player */
  public final long timeout;
  
  /** matrix of the track */
  private final int[][] track;
  
  private final List<PathCell> path;
  private final Coin[] coins;
  private final Cell playerStart;
  private final boolean isReplay;
  
  private final String[] playerClasses;
  private final RaceTrackPlayer[] players;
  private final PlayerState[] states;
  private final long[] remainingTimes;
  private final int[] scores;
  private int currentPlayer;
  private int iteration;
  private final int maxIterations;
  
  private final PrintStream errStream;
  private final RaceTrackCanvas canvas;
  /**
   * Constructs the game object by the specified parameters.
   * @param errStream used for logs
   * @param isReplay the game is a replay
   * @param params command line parameters
   */
  public RaceTrackGame(PrintStream errStream, boolean isReplay, String[] params) {
    this.errStream = errStream;
    int numParams = 7;
    if (params.length <= numParams || numParams + PLAYERS.length <= params.length) {
      errStream.println("required parameters for the game are:");
      errStream.println("\t- board size (n)         : number of race-track rows");
      errStream.println("\t- board size (m)         : number of race-track columns");
      errStream.println("\t- scale                  : factor of race-track upscale");
      errStream.println("\t- degree of wall cleaning: probability of making holes in the walls");
      errStream.println("\t- number of coins        : number of coins on the race-track");
      errStream.println("\t- random seed            : controls the sequence of the random numbers");
      errStream.println("\t- timeout                : play-time for a player in milliseconds");
      errStream.println("\t- player class           : player class (max " + PLAYERS.length + ")");
      System.exit(1);
    }
    
    this.n = Integer.parseInt(params[0]);
    this.m = Integer.parseInt(params[1]);
    this.scale = Integer.parseInt(params[2]);
    double cleanProb = Double.parseDouble(params[3]);
    this.numCoins = Integer.parseInt(params[4]);
    this.seed = Long.parseLong(params[5]);
    this.timeout = Long.parseLong(params[6]) * 1000000;
    this.isReplay = isReplay;
    
    Random random = new Random(seed);
    iteration = 0;
    Cell start = new Cell(0, 1);
    Cell finish = new Cell(0, m - 2);
    playerStart = new Cell(start.i * scale + scale / 2, start.j * scale + scale / 2);
    int[][] smallTrack = init(n, m);
    smallTrack[start.i][start.j] ^= WALL;
    smallTrack[start.i][start.j] |= EMPTY;
    smallTrack[finish.i][finish.j] ^= WALL;
    smallTrack[finish.i][finish.j] |= FINISH | EMPTY;
    
    
    generate(1, 1, smallTrack, random);
    makeHoles(smallTrack, 1, cleanProb, random);
    replace(smallTrack, INIT, EMPTY);
    track = scale(smallTrack, scale);
    cutCorners(track);
    replace(track, INIT, EMPTY);
    path = BFS(playerStart.i, playerStart.j, track);
    coins = addCoins(track, numCoins, scale, random, path);
    int maxIter = path.size();
    for (Coin c : coins) {
      maxIter += c.value;
    }
    this.maxIterations = maxIter;
    
    canvas = new RaceTrackCanvas(track.length, track[0].length, this);
    
    playerClasses = new String[params.length - numParams];
    remainingTimes = new long[params.length - numParams];
    players = new RaceTrackPlayer[params.length - numParams];
    scores = new int[params.length - numParams];
    states = new PlayerState[params.length - numParams];
    for (int playerIdx = 0; playerIdx < params.length - numParams; playerIdx++) {
      playerClasses[playerIdx] = params[numParams + playerIdx];
      remainingTimes[playerIdx] = this.timeout;
      track[playerStart.i][playerStart.j] |= PLAYERS[playerIdx];
      scores[playerIdx] = -path.size() + 1;
    }
    currentPlayer = 0;
  }
  /**
   * Returns an initial matrix of a labyrinth where every cell on odd 
   * coordinates are empty and the rest are walls.
   * @param n number of rows
   * @param m number of columns
   * @return initial labyrinth
   */
  public static int[][] init(int n, int m) {
    int[][] track = new int[n][m];
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < m; j++) {
        if (i % 2 != 1 || j % 2 != 1) {
          track[i][j] |= WALL;
        } else {
          track[i][j] |= EMPTY;
        }
      }
    }
    return track;
  }
  /**
   * Generates a perfect maze by random depth-first-search, from the specified 
   * coordinates on the specified and initialized {@link RaceTrackGame#init(int, int)}
   * @param i row index of start position
   * @param j column index of start position
   * @param maze to be prepared
   * @param random random number generator
   */
  public static void generate(int i, int j, int[][] maze, Random random) {
    maze[i][j] = INIT;
    int[] dirs = Utils.copyShuffle(directions, 0, directions.length, random);
    for (int d = 0; d < dirs.length; d++) {
      int di = 2 * (dirs[d] / 2);
      int dj = 2 * (dirs[d] % 2);
      if (0 < i + di && 0 < j + dj && i + di < maze.length && j + dj < maze[i + di].length && mask(maze[i + di][j + dj], EMPTY)) {
        maze[i + (di / 2)][j + (dj / 2)] ^= WALL;
        maze[i + (di / 2)][j + (dj / 2)] |= EMPTY;
        generate(i + di, j + dj, maze, random);
      }
    }
  }
  /**
   * center could be removed, on NOT SCALED track
   *  ___ | _#_ | ___ | _#_ | ___ | ___ | ___
   *  _#_ | _#_ | ### | _#_ | _## | ##_ | _#_
   *  ___ | _#_ | ___ | ___ | ___ | ___ | _#_
   * Makes holes on the walls of a labyrinth according to the specified 
   * probability using the above patterns.
   * @param track to be holed
   * @param iterations number of repeats of the process
   * @param probability probability of making a hole
   * @param random generates random numbers
   */
  public static void makeHoles(int[][] track, int iterations, double probability, Random random) {
    for (int iter = 0; iter < iterations; iter++) {
      for (int i = 2; i < track.length - 1; i++) {
        for (int j = 2; j < track[i].length - 1; j++) {
          if (mask(track[i][j], WALL) &&
              ((!mask(track[i - 1][j], WALL) && !mask(track[i + 1][j], WALL) && !mask(track[i][j - 1], WALL) && !mask(track[i][j + 1], WALL)) ||
               (mask(track[i - 1][j], WALL) && mask(track[i + 1][j], WALL) && !mask(track[i][j - 1], WALL) && !mask(track[i][j + 1], WALL)) || 
               (!mask(track[i - 1][j], WALL) && !mask(track[i + 1][j], WALL) && mask(track[i][j - 1], WALL) && mask(track[i][j + 1], WALL)) || 
               (mask(track[i - 1][j], WALL) && !mask(track[i + 1][j], WALL) && !mask(track[i][j - 1], WALL) && !mask(track[i][j + 1], WALL)) ||
               (!mask(track[i - 1][j], WALL) && !mask(track[i + 1][j], WALL) && !mask(track[i][j - 1], WALL) && mask(track[i][j + 1], WALL)) ||
               (!mask(track[i - 1][j], WALL) && !mask(track[i + 1][j], WALL) && mask(track[i][j - 1], WALL) && !mask(track[i][j + 1], WALL)) ||
               (!mask(track[i - 1][j], WALL) && mask(track[i + 1][j], WALL) && !mask(track[i][j - 1], WALL) && !mask(track[i][j + 1], WALL))) &&
               random.nextDouble() < probability) {
            track[i][j] = INIT;
          }
        }
      }
    }
  }
  /**
   * Up-scales the the specified track by the specified scale factor. 
   * @param track to be scaled up
   * @param scale scale factor
   * @return up-scaled track
   */
  public static int[][] scale(int[][] track, int scale) {
    int[][] scaled = new int[scale * track.length][scale * track[0].length];
    for (int i = 0; i < track.length; i++) {
      for (int j = 0; j < track[i].length; j++) {
        for (int si = 0; si < scale; si++) {
          for (int sj = 0; sj < scale; sj++) {
            scaled[i * scale + si][j * scale + sj] = track[i][j];
          }
        }
      }
    }
    return scaled;
  }
  /**
   center could be removed, on SCALED track
    ##_ | _## | ___ | ___
    ##_ | _## | _## | ##_
    ___ | ___ | _## | ##_
   * Reduces the sharpness of the corners on a scaled track according to the 
   * above specified patterns.
   * @param track to be maintained
   */
  public static void cutCorners(int[][] track) {
    for (int i = 1; i < track.length - 1; i++) {
      for (int j = 1; j < track[i].length - 1; j++) {
        if (mask(track[i][j], WALL) &&
            ((mask(track[i - 1][j], WALL) && mask(track[i + 1][j], EMPTY) && mask(track[i][j - 1], WALL) && mask(track[i][j + 1], EMPTY)) ||
             (mask(track[i - 1][j], EMPTY) && mask(track[i + 1][j], WALL) && mask(track[i][j - 1], WALL) && mask(track[i][j + 1], EMPTY)) ||
             (mask(track[i - 1][j], EMPTY) && mask(track[i + 1][j], WALL) && mask(track[i][j - 1], EMPTY) && mask(track[i][j + 1], WALL)) ||
             (mask(track[i - 1][j], WALL) && mask(track[i + 1][j], EMPTY) && mask(track[i][j - 1], EMPTY) && mask(track[i][j + 1], WALL)))){
          track[i][j] = INIT;
        }
      }
    }
  }
  /**
   * Adds the specified number of coins to the specified track at random cells 
   * and returns the list of coins have been added. The value of a coin depends 
   * on its distance to the specified path on the track.
   * @param track coins to be placed on
   * @param numCoins number of coins to be added
   * @param scale to avoid the start and finish parts of the track
   * @param random random number generator
   * @param path used to get the values of the coins
   * @return list of the added coins
   */
  public static Coin[] addCoins(int[][] track, int numCoins, int scale, Random random, List<PathCell> path) {
    Coin[] coins = new Coin[numCoins];
    int addedCoins = 0;
    while (numCoins != addedCoins) {
      int i = scale + random.nextInt(track.length - scale);
      int j = scale + random.nextInt(track[i].length - scale);
      if (mask(track[i][j], EMPTY)) {
        track[i][j] |= COIN;
        coins[addedCoins] = new Coin(i, j, 3 * distance(i, j, path));
        addedCoins ++;
      }
    }
    return coins;
  }
  /**
   * Returns the minimal distance (manhattan) between the specified coordinates 
   * and the cells of the specified path.
   * @param i row coordinate
   * @param j column coordinate
   * @param path distance measured to
   * @return minimal distance to the path
   */
  private static int distance(int i, int j, List<PathCell> path) {
    return distance(new Cell(i, j), path);
  }
  /**
   * Returns the minimal distance (manhattan) between the specified cell and 
   * the cells of the specified path.
   * @param cell distance measured from
   * @param path distance measured to
   * @return minimal distance to the path
   */
  private static int distance(Cell cell, List<PathCell> path) {
    int minDist = Integer.MAX_VALUE;
    for (Cell c : path) {
      int dist = manhattanDistance(cell, c);
      if (dist < minDist) {
        minDist = dist;
      }
    }
    return minDist;
  }
  /**
   * Adds the specified path to the specified track by marking the cells of the 
   * track by {@link RaceTrackGame#TRACE}
   * @param track to be marked
   * @param path to be added
   */
  public static void addPath(int[][] track, List<Cell> path) {
    for (Cell c : path) {
      track[c.i][c.j] |= TRACE;
    }
  }
  /**
   * Replaces cells of the track contain the specified value with the specified
   * other value.
   * @param track to be checked
   * @param what to be replaced
   * @param with replaced with
   */
  public static void replace(int[][] track, int what, int with) {
    for (int i = 0; i < track.length - 1; i++) {
      for (int j = 0; j < track[i].length - 1; j++) {
        if (track[i][j] == what) {
          track[i][j] = with;
        }
      }
    }
  }
  /**
   * Returns true iff the specified value contains the specified mask.
   * @param value to be checked
   * @param mask to be found
   * @return true iff the specified value contains the specified mask.
   */
  public static boolean mask(int value, int mask) {
    return (value & mask) == mask;
  }
  /**
   * Performs a breadth-first-search from the specified position on the 
   * specified track while one of a {@link RaceTrackGame#FINISH} cell has been
   * reached.
   * @param i row index of start
   * @param j column index of start
   * @param track to be searched on
   * @return path from the start to a finish
   */
  public static List<PathCell> BFS(int i, int j, int[][] track) {
    LinkedList<PathCell> path = new LinkedList<PathCell>();
    LinkedList<PathCell> open = new LinkedList<PathCell>();
    LinkedList<PathCell> close = new LinkedList<PathCell>();
    PathCell current = new PathCell(i, j, null);
    open.add(current);
    while (!open.isEmpty()) {
      current = open.pollFirst();
      if (mask(track[current.i][current.j], FINISH)) {
        break;
      }
      close.add(current);
      for (int idx = 0; idx < DIRECTIONS.length; idx++) {
        i = current.i + DIRECTIONS[idx].i;
        j = current.j + DIRECTIONS[idx].j;
        PathCell neighbor = new PathCell(i, j, current);
        if (isNotWall(i, j, track) && !close.contains(neighbor) && !open.contains(neighbor)) {
          open.add(neighbor);
        }
      }
    }
    while (current != null) {
      path.addFirst(current);
      current = current.parent;
    }
    return path;
  }
  /**
   * Returns true, iff the cell of the specified track is not a wall at the 
   * specified position.
   * @param i row index of the position to be checked
   * @param j column index of the position to be checked
   * @param track to be checked on
   * @return true, if not a wall
   */
  public static boolean isNotWall(int i, int j, int[][] track) {
    return 0 <= i && i < track.length && 0 <= j && j < track[i].length && !mask(track[i][j], WALL);
  }
  /**
   * Returns true, iff the specified cell of the specified track is not a wall.
   * @param cell to be checked
   * @param track to be checked on
   * @return true, if not a wall
   */
  public static boolean isNotWall(Cell cell, int[][] track) {
    return isNotWall(cell.i, cell.j, track);
  }
  /**
   * Returns true, iff none of the cells of specified list are walls on the 
   * specified track.
   * @param list cells to be checked
   * @param track to be checked on
   * @return true, if none of cells are walls
   */
  public static boolean isNeitherWall(List<Cell> list, int[][] track) {
    for (Cell cell : list) {
      if (!isNotWall(cell, track)) {
        return false;
      }
    }
    return true;
  }
  /**
   * Returns the coordinates of the line between the specified points.
   * The line drawing algorithm is based on the 
   * <a target="_blank" href="https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm">Bresenham's line algorithm</a>
   * @param from draw line from
   * @param to draw line to
   * @return coordinates of the line
   */
  public static List<Cell> line8connect(Cell from, Cell to) {
    List<Cell> result = new LinkedList<Cell>();
    int di = Math.abs(to.i - from.i);
    int dj = Math.abs(to.j - from.j);
    
    int ii = from.i < to.i ? 1 : -1;
    int ij = from.j < to.j ? 1 : -1;
    int e = di - dj;
    int i = from.i;
    int j = from.j;
    
    for (int k = 0; k < Math.max(di, dj) + 1; k++) {
      result.add(new Cell(i, j));
      int e2 = 2 * e;
      if (e2 < di) {
        j = j + ij;
        e = e + di;
      }
      if (-dj < e2) {
        i = i + ii;
        e = e - dj;
      }
    }
    return result;
  }
  /**
   * Returns the coordinates of the line between the specified points, but the 
   * line is 4-connected instead of 8.
   * The line drawing algorithm is based on the 
   * <a target="_blank" href="https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm">Bresenham's line algorithm</a>
   * @param from draw line from
   * @param to draw line to
   * @return coordinates of the line
   */
  public static List<Cell> line4connect(Cell from, Cell to) {
    List<Cell> result = new LinkedList<Cell>();
    int di = Math.abs(to.i - from.i);
    int dj = Math.abs(to.j - from.j);
    
    int ii = from.i < to.i ? 1 : -1;
    int ij = from.j < to.j ? 1 : -1;
    int e = 0;
    int i = from.i;
    int j = from.j;
    
    for (int k = 0; k < di + dj + 1; k++) {
      result.add(new Cell(i, j));
      int ei = e + di;
      int ej = e - dj;
      if (Math.abs(ei) < Math.abs(ej)) {
        j += ij;
        e = ei;
      } else {
        i += ii;
        e = ej;
      }
    }
    return result;
  }
  /**
   * Returns a negative integer, zero, or a positive integer as specified 
   * cell (i,j) is on the right site, on, or on the left side of the line 
   * specified by A-B points (ai,aj)-(bi,bj)
   * @param ai i coordinate of the point A
   * @param aj j coordinate of the point A
   * @param bi i coordinate of the point B
   * @param bj j coordinate of the point B
   * @param i i coordinate of the point to be checked
   * @param j j coordinate of the point to be checked
   * @return -1,0,1 if point is right,on,left
   */
  public static int side(int ai, int aj, int bi, int bj, int i, int j) {
    int result = ((bi - ai) * (j - aj)) - ((bj - aj) * (i - ai));
    return result < 0 ? -1 : (0 < result ? 1 : 0);
  }
  /**
   * Returns the list of cells which are crossed or touched (its corner is on 
   * the line) by the line defined by the specified coordinates.
   * @param from line starts from
   * @param to line goes to
   * @return crossed cells
   */
  public static List<Cell> lineCrossing(Cell from, Cell to) {
    List<Cell> result = new LinkedList<Cell>();
    int di = to.i - from.i < 0 ? -1 : 1;
    int dj = to.j - from.j < 0 ? -1 : 1;
    for (int i = from.i; i != to.i + di; i += di) {
      for (int j = from.j; j != to.j + dj; j += dj) {
        int sides = side(from.i * 2 + 1, from.j * 2 + 1, to.i * 2 + 1, to.j * 2 + 1, i * 2, j * 2) + 
                    side(from.i * 2 + 1, from.j * 2 + 1, to.i * 2 + 1, to.j * 2 + 1, i * 2 + 2, j * 2) + 
                    side(from.i * 2 + 1, from.j * 2 + 1, to.i * 2 + 1, to.j * 2 + 1, i * 2, j * 2 + 2) + 
                    side(from.i * 2 + 1, from.j * 2 + 1, to.i * 2 + 1, to.j * 2 + 1, i * 2 + 2, j * 2 + 2);
        if (sides != 4 && sides != -4) {
          result.add(new Cell(i, j));
        }
      }
    }
    return result;
  }
  /**
   * Moves the specified player on the specified track using its state and the 
   * specified direction. The method checks the wall collisions and updates the 
   * state of the player.
   * @param player to be moved
   * @param direction direction to be applied
   * @param track to be moved on
   */
  public static void move(RaceTrackPlayer player, Direction direction, int[][] track) {
    int i = player.state.i + player.state.vi + direction.i;
    int j = player.state.j + player.state.vj + direction.j;
    // check wall collision
    Cell cell = null;
    for (Cell c : line8connect(toCell(player), new Cell(i, j))) {
      if (isNotWall(c, track)) {
        cell = c;
      } else {
        direction = null;
        break;
      }
    }
    // wall collision has been occurred
    if (direction == null) {
      player.state.vi = cell.i - player.state.i;
      player.state.vj = cell.j - player.state.j;
      direction = new Direction(0, 0);
    }
    
    /*while (!isNeitherWall(line8connect(toCell(player), new Cell(i, j)), track)) {
      if (player.state.vi < 0) {
        player.state.vi += 1;
      } else if (0 < player.state.vi) {
        player.state.vi -= 1;
      } else if (player.state.vj < 0) {
        player.state.vj += 1;
      } else if (0 < player.state.vj) {
        player.state.vj -= 1;
      } else {
        direction = new Direction(0, 0);
      }
      i = player.state.i + player.state.vi + direction.i;
      j = player.state.j + player.state.vj + direction.j;
    }*/
    player.step(direction);
  }
  /**
   * Returns the manhattan distance between the specified positions.
   * @param a position a
   * @param b position b
   * @return manhattan distance
   */
  public static int manhattanDistance(Cell a, Cell b) {
    return Math.abs(a.i - b.i) + Math.abs(a.j - b.j);
  }
  /**
   * Returns the euclidean distance between the specified positions.
   * @param a position a
   * @param b position b
   * @return euclidean distance
   */
  public static double euclideanDistance(Cell a, Cell b) {
    return Math.sqrt((a.i - b.i) * (a.i - b.i) + (a.j - b.j) * (a.j - b.j));
  }
  /**
   * Returns a direction object from the specified cell to the other specified one.
   * @param from compute direction from
   * @param to compute direction to
   * @return direction to the specified cell
   */
  public static Direction direction(Cell from, Cell to) {
    return new Direction(to.i - from.i, to.j - from.j);
  }
  /**
   * Converts the position of the specified state to a {@link Cell} object.
   * @param state to be converted
   * @return the position as a cell
   */
  public static Cell toCell(PlayerState state) {
    return new Cell(state.i, state.j);
  }
  /**
   * Converts the position of the specified players to a {@link Cell} object.
   * @param player to be converted
   * @return the position of the player
   */
  public static Cell toCell(RaceTrackPlayer player) {
    return toCell(player.state);
  }
  /**
   * Prints the current state of the game.
   * @return string representation of the game.
   */
  public String print() {
    StringBuffer sb = new StringBuffer();
    sb.append(track + "\tITERATION: " + iteration + " BASELINE: " + path.size());
    for (int i = 0; i < players.length; i++) {
      sb.append("\n\tPLAYER: " + players[i] + " SCORE: " + scores[i] + " REMAINING: " + remainingTimes[i]);
    }
    return sb.toString();
  }
  
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < track.length; i++) {
      for (int j = 0; j < track[i].length; j++) {
        int mask = 1 << VIEW.size();
        while (0 < mask && 0 == (mask & track[i][j])) {
          mask >>>= 1;
        }
        sb.append(VIEW.get(mask));
      }
      sb.append("\n");
    }
    return sb.toString();
  }
  @Override
  public long getTimeout() {
    return timeout;
  }
  @Override
  public List<Pair<Constructor<? extends RaceTrackPlayer>, Object[]>> getPlayerConstructors() throws Exception {
    List<Pair<Constructor<? extends RaceTrackPlayer>, Object[]>> result = new LinkedList<Pair<Constructor<? extends RaceTrackPlayer>, Object[]>>();
    for (int i = 0; i < players.length; i++) {
      Class<? extends RaceTrackPlayer> clazz = Class.forName(DummyPlayer.class.getName()).asSubclass(RaceTrackPlayer.class);
      if (isReplay) {
        errStream.println("Game is in replay mode, Player: " + i + " is the DummyPlayer, but was: " + playerClasses[i]);
      } else {
        clazz = Class.forName(playerClasses[i]).asSubclass(RaceTrackPlayer.class);
      }
      Constructor<? extends RaceTrackPlayer> constructor =  clazz.getConstructor(PlayerState.class, Random.class, int[][].class, Coin[].class, int.class);
      PlayerState state = new PlayerState(playerStart.i, playerStart.j, 0, 0);
      states[i] = state;
      Coin[] coinsCopy = new Coin[numCoins];
      for (int ci = 0; ci < numCoins; ci++) {
        coinsCopy[ci] = new Coin(coins[ci]);
      }
      result.add(new Pair<Constructor<? extends RaceTrackPlayer>, Object[]>(constructor, new Object[] {state.clone(), new Random(seed), Utils.copy(track), coinsCopy, i}));
    }
    return result;
  }
  @Override
  public void setPlayers(List<Pair<? extends RaceTrackPlayer, Long>> playersAndTimes) throws Exception {
    int idx = 0;
    for (Pair<? extends RaceTrackPlayer, Long> pair : playersAndTimes) {
      players[idx] = pair.first;
      remainingTimes[idx] -= pair.second;
      if (players[idx] instanceof HumanPlayer) {
        remainingTimes[idx] = Long.MAX_VALUE - pair.second - 10;
      }
      // check color hacking
      if (players[idx] != null && players[idx].color != idx) {
        int color = players[idx].color;
        remainingTimes[idx] = 0;
        Field field = RaceTrackPlayer.class.getDeclaredField("color");
        field.setAccessible(true);
        field.set(players[idx], idx);
        field.setAccessible(false);
        errStream.println("Illegal color (" + color + ") was set for player: " + players[idx]);
      }
      idx++;
    }
  }
  @Override
  public RaceTrackPlayer[] getPlayers() {
    return players;
  }
  @Override
  public RaceTrackPlayer getNextPlayer() {
    return players[currentPlayer];
  }
  @Override
  public boolean isValid(Direction action) {
    return action != null;
  }
  @Override
  public void setAction(RaceTrackPlayer player, Direction action, long time) {
    if (!isValid(action)) {
      errStream.println("INVALID ACTION: " + action);
      remainingTimes[currentPlayer] = -1;
      return;
    }
    // check player state hacking
    if (player.color != currentPlayer || !player.state.same(states[currentPlayer])) {
      errStream.println("PLAYER OBJECT HAS BEEN MANIPULATED: " + player + " <-> color: " + currentPlayer + " state: " + states[currentPlayer]);
      remainingTimes[currentPlayer] = -1;
      return;
    }
    Cell currentPosition = toCell(player);
    track[currentPosition.i][currentPosition.j] ^= PLAYERS[player.color];
    track[currentPosition.i][currentPosition.j] |= TRACE;
    
    // move and check wall collision and update player velocity and direction
    move(player, action, track);
    for (Cell cell : line8connect(currentPosition, toCell(player))) {
      track[cell.i][cell.j] |= TRACE;
    }
    
    // check other collisions
    for (Cell cell : lineCrossing(currentPosition, toCell(player))) {
      //track[cell.i][cell.j] |= TRACE;
      if (mask(track[cell.i][cell.j], COIN)) {
        track[cell.i][cell.j] ^= COIN;
        for (Coin coin : coins) {
          if (cell.same(coin)) {
            scores[player.color] -= coin.value;
          }
        }
      }
    }
    
    // update game state
    track[player.state.i][player.state.j] |= PLAYERS[player.color];
    scores[player.color]++;
    remainingTimes[player.color] -= time;
    states[currentPlayer].set(player.state);
    
    currentPlayer++;
    if (currentPlayer == players.length) {
      currentPlayer = 0;
      iteration++;
    }
  }
  @Override
  public long getRemainingTime(RaceTrackPlayer player) {
    return player == null ? -1 : remainingTimes[player.color];
  }
  @Override
  public boolean isFinished() {
    return maxIterations < iteration || remainingTimes[currentPlayer] <= 0 || mask(track[players[currentPlayer].state.i][players[currentPlayer].state.j], FINISH);
  }
  @Override
  public double getScore(RaceTrackPlayer player) {
    return player == null || remainingTimes[player.color] <= 0 ? scale * track.length * track[0].length : scores[player.color];
  }
  @Override
  public Class<? extends Action> getActionClass() {
    return Direction.class;
  }
  @Override
  public Frame getFrame() {
    String iconPath = "/game/engine/ui/resources/icon-game.png";
    return new GameFrame("RaceTrack", iconPath, canvas);
  }
  @Override
  public List<GameObject> getGameObjects() {
    LinkedList<GameObject> gos = new LinkedList<GameObject>();
    for (int i = 0; i < track.length; i++) {
      for (int j = 0; j < track[i].length; j++) {
        int x = (int) Math.round(canvas.multiplier * (j + 0.1));
        int y = (int) Math.round(canvas.multiplier * (i + 0.1));
        int w = (int) Math.round(canvas.multiplier * 0.8);
        int h = (int) Math.round(canvas.multiplier * 0.8);
        if (mask(track[i][j], WALL)) {
          gos.add(new FilledRectangleObject(x, y, w, h, Color.darkGray));
        }
        if (mask(track[i][j], TRACE)) {
          gos.add(new FilledOvalObject(x, y, w, h, Color.lightGray));
        }
        if (mask(track[i][j], FINISH)) {
          gos.add(new FilledRectangleObject(x, y, w, h, Color.pink));
        }
        if (mask(track[i][j], COIN)) {
          gos.add(new FilledOvalObject(x, y, w, h, new Color(244, 180, 0)));
        }
        if (mask(track[i][j], PLAYERS[0])) {
          gos.add(new FilledOvalObject(x, y, w, h, new Color(219, 68, 55)));
        }
        if (mask(track[i][j], PLAYERS[1])) {
          gos.add(new FilledOvalObject(x, y, w, h, new Color(15, 157, 88)));
        }
        if (mask(track[i][j], PLAYERS[2])) {
          gos.add(new FilledOvalObject(x, y, w, h, new Color(66, 133, 244)));
        }
        if (mask(track[i][j], PLAYERS[3])) {
          gos.add(new FilledOvalObject(x, y, w, h, Color.gray));
        }
      }
    }
    return gos;
  }

}
