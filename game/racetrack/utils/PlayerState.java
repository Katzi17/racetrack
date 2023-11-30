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

package game.racetrack.utils;

/**
 * Represents the sate of a player by a position and a velocity.
 */
public class PlayerState {
  /** row index of the position */
  public int i;
  /** column index of the position */
  public int j;
  /** horizontal velocity */
  public int vi;
  /** vertical velocity */
  public int vj;
  /**
   * Creates a state object by the specified values.
   * @param i row index of the position
   * @param j column index of the position
   * @param vi vertical velocity
   * @param vj horizontal velocity
   */
  public PlayerState(int i, int j, int vi, int vj) {
    this.i = i;
    this.j = j;
    this.vi = vi;
    this.vj = vj;
  }
  /**
   * Copy constructor, initializes by the specified object.
   * @param state to be set.
   */
  public PlayerState(PlayerState state) {
    this(state.i, state.j, state.vi, state.vj);
  }
  /**
   * Returns a deep copy of the object.
   */
  public PlayerState clone() {
    return new PlayerState(this);
  }
  /**
   * Sets the values of the object by the members of the specified object.
   * @param state to be set
   */
  public void set(PlayerState state) {
    this.i = state.i;
    this.j = state.j;
    this.vi = state.vi;
    this.vj = state.vj;
  }
  /**
   * Returns true, iff the specified object has the same position and velocity 
   * values as the current has.
   * @param state to be checked
   * @return true, if they are same
   */
  public boolean same(PlayerState state) {
    return i == state.i && j == state.j && 
           vi == state.vi && vj == state.vj;
  }
  public String toString() {
    return "p:(" + i + ", " + j + ") v:(" + vi + ", " + vj + ")";
  }

}
