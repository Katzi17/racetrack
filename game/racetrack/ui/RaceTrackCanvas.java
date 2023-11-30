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

import java.awt.Graphics;

import game.engine.ui.BoardGameCanvas;
import game.engine.ui.Drawable;
import game.racetrack.RaceTrackGame;

/**
 * Represents the canvas for the {@link RaceTrackGame} game.
 */
public class RaceTrackCanvas extends BoardGameCanvas {
  private static final long serialVersionUID = 7870778539016899188L;
  /** maximal screen height */
  public static final int HEIGHT = 800;

  /**
   * Creates the canvas for the game.
   * @param n height
   * @param m width
   * @param game canvas belongs to
   */
  public RaceTrackCanvas(int n, int m, Drawable game) {
    super(n, m, HEIGHT / n, game);
  }
  
  @Override
  public void paintBackground(Graphics graphics) {
  }
  
  @Override
  public void setCoordinates() {
    CellAction action = new CellAction(lasty, lastx);
    QUEUE.add(action);
  }

}
