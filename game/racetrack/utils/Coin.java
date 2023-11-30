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
 * A special cell that represents a coin on the track.
 */
public class Coin extends Cell {
  /** represents the value of the coin */
  public final int value;
  /**
   * Creates a coin object by the specified values.
   * @param i row index of the position
   * @param j column index of the position
   * @param value the values of the coin
   */
  public Coin(int i, int j, int value) {
    super(i, j);
    this.value = value;
  }
  /**
   * Copy constructor.
   * @param coin to be cloned
   */
  public Coin(Coin coin) {
    this(coin.i, coin.j, coin.value);
  }

}
