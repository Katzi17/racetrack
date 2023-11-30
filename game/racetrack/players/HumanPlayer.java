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

package game.racetrack.players;

import java.util.Random;

import game.engine.ui.GameCanvas;
import game.racetrack.Direction;
import game.racetrack.RaceTrackGame;
import game.racetrack.RaceTrackPlayer;
import game.racetrack.ui.CellAction;
import game.racetrack.utils.Coin;
import game.racetrack.utils.PlayerState;

/**
 * For handling human interactions on GUI.
 */
public class HumanPlayer extends RaceTrackPlayer {

  public HumanPlayer(PlayerState state, Random random, int[][] track, Coin[] coins, int color) {
    super(state, random, track, coins, color);
  }

  @Override
  public Direction getDirection(long remainingTime) {
    Direction direction = null;
    try {
      while (direction == null) {
        CellAction action = (CellAction) GameCanvas.getAction();
        direction = RaceTrackGame.direction(RaceTrackGame.toCell(this), action);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return direction;
  }

}
