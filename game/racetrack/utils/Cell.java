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
 * Represents a cell object a position on the track.
 */
public class Cell implements Comparable<Cell> {
  /** row index of the position */
  public final int i;
  /** column index of the position */
  public final int j;
  /**
   * Creates a cell object by the specified values.
   * @param i row index of the cell
   * @param j column index of the cell
   */
  public Cell(int i, int j) {
    this.i = i;
    this.j = j;
  }
  /**
   * Returns true, iff the specified object is the same as the current.
   * @param cell to be checked
   * @return true, if they are same
   */
  public boolean same(Cell cell) {
    return cell != null && i == cell.i && j == cell.j;
  }
  @Override
  public int compareTo(Cell o) {
    if (i < o.i || (i == o.i && j < o.j)) {
      return -1;
    }
    if (o.i < i || (i == o.i && o.j < j)) {
      return 1;
    }
    return 0;
  }
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Cell) {
      Cell c = (Cell) obj;
      return same(c);
    }
    return false;
  }
  @Override
  public String toString() {
    return "(" + i + ", " + j + ")";
  }

}
