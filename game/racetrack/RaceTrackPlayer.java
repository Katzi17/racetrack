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

import java.util.List;
import java.util.Random;

import game.engine.Player;
import game.engine.utils.Pair;
import game.racetrack.utils.Coin;
import game.racetrack.utils.PlayerState;

/**
 * Represents a player of the {@link RaceTrackGame} game.
 */
public abstract class RaceTrackPlayer implements Player<Direction> {
  /** position and velocity of the player */
  public final PlayerState state;
  /** random seed generator */
  public final Random random;
  /** represents the track of the game */
  public final int[][] track;
  /** list of coins on the track */
  public final Coin[] coins;
  /** the color of the player */
  public final int color;
  /**
   * Creates a player and sets the specified values.
   * @param state position and velocity
   * @param random random generator
   * @param track game track
   * @param coins list of coins on the map
   * @param color player color
   */
  public RaceTrackPlayer(PlayerState state, Random random, int[][] track, Coin[] coins, int color) {
    this.state = state;
    this.random = random;
    this.track = track;
    this.coins = coins;
    this.color = color;
  }
  /**
   * Updates the state of the player and applies the specified modification on 
   * the velocity.
   * @param direction velocity will be modified with
   */
  public final void step(Direction direction) {
    state.vi += direction.i;
    state.vj += direction.j;
    state.i += state.vi;
    state.j += state.vj;
  }
  
  /**
   * Returns the action of the player, as a direction that changes the velocity.
   * @param remainingTime the remaining time of the player.
   * @return direction to be applied
   */
  public abstract Direction getDirection(long remainingTime);
  
  @Override
  public final boolean equals(Object obj) {
    if (obj instanceof RaceTrackPlayer) {
      RaceTrackPlayer o = (RaceTrackPlayer)obj;
      return same(o);
    }
    return false;
  }
  /**
   * Returns true iff the state and the color are the same as the specified 
   * object has.
   * @param player to be checked
   * @return true if they are same
   */
  public final boolean same(RaceTrackPlayer player) {
    return player != null && player.state != null && state.same(player.state) && color == player.color;
  }
  @Override
  public final String toString() {
    return RaceTrackGame.VIEW.get(RaceTrackGame.PLAYERS[color]) + " " + getClass().getCanonicalName() + " " + state;
  }
  @Override
  public final int getColor() {
    return color;
  }
  @Override
  public final Direction getAction(List<Pair<Integer, Direction>> prevActions, long[] remainingTimes) {
    return getDirection(remainingTimes[color]);
  }
}
