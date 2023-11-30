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

import game.engine.Action;

/**
 * Defines a direction vector on a grid by horizontal (j) and vertical (i) values.
 * The values of a vector can be {-1,0,1}. The top left corner is the zero point 
 * and positive values indicate bottom and right directions.
 */
public class Direction implements Action, Comparable<Direction> {
  private static final long serialVersionUID = 5693557374625208430L;
  /** vertical direction */
  public final int i;
  /** horizontal direction */
  public final int j;
  /**
   * Constructs a direction object using the specified values.
   * @param i vertical direction
   * @param j horizontal direction 
   */
  public Direction(int i, int j) {
    this.i = (int) Math.signum(i);
    this.j = (int) Math.signum(j);
  }
  /**
   * Returns true iff the specified direction is equal to the current object.
   * @param direction to be checked
   * @return true if they are same
   */
  public boolean same(Direction direction) {
    return direction != null && direction.i == i && direction.j == j;
  }
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Direction) {
      Direction d = (Direction)obj;
      return same(d);
    }
    return false;
  }
  @Override
  public int compareTo(Direction o) {
    if (i < o.i || (i == o.i && j < o.j)) {
      return -1;
    }
    if (o.i < i || (i == o.i && o.j < j)) {
      return 1;
    }
    return 0;
  }
  @Override
  public String toString() {
    return "DIRECTION: (" + i + "," + j + ")";
  }
}
