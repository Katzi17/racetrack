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
 * A special cell, that has a parent member. Can be used for search paths.
 */
public class PathCell extends Cell {
  /** parent of the current object */
  public final PathCell parent;
  /**
   * Creates a path cell object by the specified values.
   * @param i row index of the position
   * @param j column index of the position
   * @param parent parent object
   */
  public PathCell(int i, int j, PathCell parent) {
    super(i, j);
    this.parent = parent;
  }

}
