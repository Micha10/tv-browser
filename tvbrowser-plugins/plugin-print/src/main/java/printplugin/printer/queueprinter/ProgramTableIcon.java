/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package printplugin.printer.queueprinter;

import devplugin.Date;
import devplugin.Program;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import printplugin.printer.PositionedIcon;
import printplugin.printer.ProgramItem;
import printplugin.settings.ProgramIconSettings;

import util.ui.TextAreaIcon;

public class ProgramTableIcon implements Icon {

  private int mWidth;
  private int mHeight;
  private int mCurColumnInx;
  private int mCurY;
  private int mNumOfCols;
  private final List<PositionedIcon> mPrograms;
  private final ProgramIconSettings mProgramIconSettings;
  private Date mCurDate;
  private Font mDateFont;

  public ProgramTableIcon(final ProgramIconSettings settings, final Font dateFont, final int width, final int height,
      final int numOfCols) {
    mProgramIconSettings = settings;
    mDateFont = dateFont;
    mWidth = width;
    mHeight = height;
    mNumOfCols = numOfCols;
    mCurColumnInx = 0;
    mCurY = 0;
    mPrograms = new ArrayList<>();
  }

  public boolean add(final Program prog, final boolean forceAdding) {
    ProgramItem item = new ProgramItem(prog, mProgramIconSettings, mWidth / mNumOfCols - 10, true, true);
    item.setMaximumHeight(200);
    int spaceForDatestring = 0;
    if (!prog.getDate().equals(mCurDate)) {
      mCurDate = prog.getDate();
      spaceForDatestring = mDateFont.getSize();
    }
    boolean canAdd = false;
    if (forceAdding) {
      canAdd = true;
    } else if (mCurY + item.getHeight() + spaceForDatestring < mHeight) {
      canAdd = true;
    } else if (mCurColumnInx + 1 < mNumOfCols) {
      mCurColumnInx++;
      mCurY = 0;
      canAdd = true;
    }

    if (canAdd) {
      int x = mWidth / mNumOfCols * mCurColumnInx;
      if (spaceForDatestring > 0) {
        mCurY += spaceForDatestring;
        mPrograms.add(
            new DateItem(new TextAreaIcon(mCurDate.getLongDateString(), mDateFont, mWidth / mNumOfCols), x, mCurY));
        mCurY += mDateFont.getSize() * 1.3;
      }
      mPrograms.add(item);
      item.setPos(x, mCurY);
      mCurY = mCurY + item.getHeight() + mProgramIconSettings.getTitleFont().getSize() / 3;

      return true;
    }

    return false;
  }

  @Override
  public int getIconHeight() {
    return mHeight;
  }

  @Override
  public int getIconWidth() {
    return mWidth;
  }

  @Override
  public void paintIcon(final Component c, final Graphics graphics, final int x, final int y) {
    for (PositionedIcon item : mPrograms) {
      item.paint(graphics, (int) (x + item.getX()), (int) (y + item.getY()));
    }

    graphics.setColor(Color.lightGray);
    for (int i = 0; i < mNumOfCols - 1; i++) {
      int x0 = mWidth / mNumOfCols * (i + 1) + x;
      graphics.drawLine(x0, 0 + y, x0, mHeight + y);
    }
  }

  private static class DateItem implements PositionedIcon {

    private final int mX, mY;
    private final TextAreaIcon mIcon;

    public DateItem(TextAreaIcon icon, int x, int y) {
      mX = x;
      mY = y;
      mIcon = icon;
    }

    @Override
    public double getX() {
      return mX;
    }

    @Override
    public double getY() {
      return mY;
    }

    @Override
    public void paint(Graphics g, int x, int y) {
      mIcon.paintIcon(null, g, x, y);
    }
  }
}