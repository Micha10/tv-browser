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

package printplugin.settings;

import devplugin.ProgramFieldType;

import java.awt.Font;

import javax.swing.JPanel;

import printplugin.PrintPlugin;

import util.ui.TimeFormatter;

@SuppressWarnings("nls")
public class PrinterProgramIconSettings implements ProgramIconSettings {

  private static final Font PROGRAMTITLEFONT = PrintPlugin.getInstance().getPluginSettings()
      .deriveDefaultFont(Font.BOLD, 12);
  private static final Font PROGRAMTEXTFONT = PrintPlugin.getInstance().getPluginSettings()
      .deriveDefaultFont(Font.PLAIN, 10);
  private static final Font PROGRAMTIMEFONT = PrintPlugin.getInstance().getPluginSettings().deriveDefaultFont(Font.BOLD,
      12);

  private ProgramFieldType[] mProgramInfoFields;
  private boolean mShowPluginMark;
  private int mTimeFileWidth = -1;

  protected PrinterProgramIconSettings() {
    mProgramInfoFields = new ProgramFieldType[] {
        ProgramFieldType.SHORT_DESCRIPTION_TYPE,
        ProgramFieldType.ACTOR_LIST_TYPE,
        ProgramFieldType.DESCRIPTION_TYPE
    };

    mShowPluginMark = false;
  }

  public static ProgramIconSettings create(ProgramFieldType[] programInfoFields, boolean showPluginMark) {
    PrinterProgramIconSettings settings = new PrinterProgramIconSettings();
    settings.mProgramInfoFields = programInfoFields;
    settings.mShowPluginMark = showPluginMark;
    return settings;
  }

  public static ProgramIconSettings create() {
    return new PrinterProgramIconSettings();
  }

  @Override
  public Font getTitleFont() {
    return PROGRAMTITLEFONT;
  }

  @Override
  public Font getTextFont() {
    return PROGRAMTEXTFONT;
  }

  @Override
  public Font getTimeFont() {
    return PROGRAMTIMEFONT;
  }

  @Override
  public int getTimeFieldWidth() {
    if (mTimeFileWidth == -1) {
      TimeFormatter time = new TimeFormatter();

      JPanel temp = new JPanel();
      mTimeFileWidth = temp.getFontMetrics(temp.getFont()).stringWidth(time.formatTime(23, 59)) + 4;
    }

    return mTimeFileWidth;
  }

  @Override
  public ProgramFieldType[] getProgramInfoFields() {
    return mProgramInfoFields;
  }

  @Override
  public String[] getProgramTableIconPlugins() {
    return new String[] {"java.programinfo.ProgramInfo"};
  }

  @Override
  public boolean getPaintExpiredProgramsPale() {
    return false;
  }

  @Override
  public boolean getPaintProgramOnAir() {
    return false;
  }

  @Override
  public boolean getPaintPluginMarks() {
    return mShowPluginMark;
  }
}