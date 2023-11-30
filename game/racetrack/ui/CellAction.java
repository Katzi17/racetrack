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

package game.racetrack.ui;

import game.engine.Action;
import game.racetrack.utils.Cell;

/**
 * Helper class for human player.
 */
public class CellAction extends Cell implements Action {
  private static final long serialVersionUID = 7973643781971610723L;

  /**
   * Creates a cell action at the specified position.
   * @param i row position
   * @param j column position
   */
  public CellAction(int i, int j) {
    super(i, j);
  }

}
