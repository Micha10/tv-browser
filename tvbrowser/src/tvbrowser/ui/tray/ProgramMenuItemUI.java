/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.tray;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.plaf.basic.BasicMenuItemUI;

import tvbrowser.core.Settings;
import util.io.IOUtilities;
import util.program.ProgramUtilities;
import util.i18n.Localizer;
import util.ui.ProgramPanel;
import util.ui.TextAreaIcon;
import util.ui.UiUtilities;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;

/**
 * A class that paint the ProgramMenuItem.
 * 
 * @author René Mach
 * 
 */
public class ProgramMenuItemUI extends BasicMenuItemUI {
  private Program mProgram;
  private TextAreaIcon mChannelName;
  private Icon mIcon;
  private boolean mShowStartTime, mShowDate, mShowIcon, mShowName;
  private int mTime;

  /**
   * Constructs the UI.
   * 
   * @param program
   *          The program that is to show in the ProgramMenuItem.
   * @param channelName
   *          The TextAreaIcon that contains the channel name
   * @param icon
   *          The channel icon.
   * @param showStartTime
   *          The ProgramMenuItem should show the start time.
   * @param showDate
   *          The ProgramMenuItem should show the date.
   * @param showIcon
   *          The ProgramMenuItem should show the channel icon.
   * @param showName
   *          The ProgramMenuItem should show the channel name.
   * @param time
   *          The time of the time button.
   */
  public ProgramMenuItemUI(Program program, TextAreaIcon channelName, Icon icon, boolean showStartTime,
      boolean showDate, boolean showIcon, boolean showName, int time) {
    mProgram = program;
    mChannelName = channelName;
    mShowStartTime = showStartTime;
    mShowDate = showDate;
    mShowIcon = showIcon;
    mShowName = showName;
    mIcon = icon;
    mTime = time;
  }

  protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
    boolean isOnAir = ProgramUtilities.isOnAir(mProgram);
    g.clearRect(0, 0, menuItem.getWidth(), menuItem.getHeight());
    
    boolean isMarked = mProgram.getMarkPriorityMax() > Program.PRIORITY_MARK_NONE;

    if (menuItem.isArmed()) {
      g.setColor(bgColor);
    } else if (!isOnAir
        && !isMarked
        && ((mTime != -1 && Settings.Tray.OnTime.COLOR_PROGRESS_BACKGROUND_LIGHT.getColor().getAlpha() == 0 && Settings.Tray.OnTime.COLOR_PROGRESS_BACKGROUND_DARK
            .getColor().getAlpha() == 0) || mTime == -1)) {
      g.setColor(menuItem.getBackground());
    } else {
      g.setColor(((ProgramMenuItem) menuItem).getDefaultBackground());
    }

    g.fillRect(0, 0, menuItem.getWidth(), menuItem.getHeight());

    Insets i = menuItem.getMargin();
    int x = mIcon == null ? 0 : mIcon.getIconWidth() + i.left;

    int width = menuItem.getWidth() - x;
    int height = menuItem.getHeight();
    int top = i.top;
    int bottom = height - i.bottom;

    if (!menuItem.isArmed() && (isMarked || isOnAir || mTime != -1)) {
      if(!UiUtilities.colorsInEqualRange(menuItem.getForeground(),Color.white,20)) {
        g.setColor(Color.white);
        g.fillRect(x, top, menuItem.getWidth(), bottom);
      }
    }

    Color markedColor = Plugin.getPluginManager().getTvBrowserSettings().getColorForMarkingPriority(
        mProgram.getMarkPriorityMax());

    if (isMarked) {
      ProgramPanel.paintHighlighting((Graphics2D)g, x, top, menuItem.getWidth(),  bottom, mProgram.getMarkerArr(), mProgram, (byte)10);
    }

    if (isOnAir || mTime != -1) {
      int minutesAfterMidnight = mTime != -1 ? mTime : IOUtilities.getMinutesAfterMidnight();
      int progLength = mProgram.getLength();
      int startTime = mProgram.getHours() * 60 + mProgram.getMinutes();
      int elapsedMinutes;
      if (minutesAfterMidnight < startTime) {
        // The next day has begun -> we have to add 24 * 60 minutes
        // Example: Start time was 23:50 = 1430 minutes after midnight
        // now it is 0:03 = 3 minutes after midnight
        // elapsedMinutes = (24 * 60) + 3 - 1430 = 13 minutes
        elapsedMinutes = (24 * 60) + minutesAfterMidnight - startTime;
      } else {
        elapsedMinutes = minutesAfterMidnight - startTime;
      }

      int progressX = 0;
      if (progLength > 0) {
        progressX = elapsedMinutes * (width - i.left - i.right) / progLength;
      }

      if (!isMarked) {
        g.setColor(mTime == -1 ? Settings.ProgramPanel.COLOR_ON_AIR_LIGHT.getColor()
            : Settings.Tray.OnTime.COLOR_PROGRESS_BACKGROUND_LIGHT.getColor());
        g.fillRect(x + progressX - i.right - i.left, top, width - progressX + i.right + i.left, bottom);
      }
      g.setColor(mTime == -1 ? Settings.ProgramPanel.COLOR_ON_AIR_DARK.getColor() : isMarked ? new Color(markedColor
          .darker().getRed(), markedColor.darker().getGreen(), markedColor.darker().getBlue(), (markedColor
          .darker().getAlpha() / 3)) : Settings.Tray.OnTime.COLOR_PROGRESS_BACKGROUND_DARK.getColor());

      g.fillRect(x, top, progressX - i.right - i.left, bottom);
    } else if (mProgram.isExpired()) {
      ((ProgramMenuItem) menuItem).stopTimer();
    }

    if (mIcon != null) {
      mIcon.paintIcon(menuItem, g, menuItem.getMargin().left, menuItem.getMargin().top);
    }
  }

  protected void paintText(Graphics g, JMenuItem menuItem, Rectangle textRect, String text) {
    if (g instanceof Graphics2D && Settings.Tray.ANTIALIASING.getBoolean()) {
      Graphics2D g2d = (Graphics2D) g;
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    if (menuItem.isArmed()) {
      g.setColor(selectionForeground);
    } else {
      g.setColor(menuItem.getForeground());
    }

    int x = mShowIcon ? mIcon.getIconWidth() + menuItem.getIconTextGap() : textRect.x;
    int y = (menuItem.getHeight() - mChannelName.getIconHeight()) / 2 - 1;

    if (mShowName) {
      mChannelName.paintIcon(null, g, x, y);
      x += Settings.Tray.Channels.WIDTH.getInt() + menuItem.getIconTextGap();
    }

    int temp = y + (menuItem.getFont().getSize() * (mChannelName.getLineCount() / 2 + 1));

    y = mShowName && ((mChannelName.getLineCount() & 1) == 1) ? temp : (menuItem.getHeight() - menuItem.getFont()
        .getSize())
        / 2 - 1 + menuItem.getFont().getSize();
    if (mShowDate) {
      g.setFont(menuItem.getFont().deriveFont(Font.BOLD));
      Date currentDate = Date.getCurrentDate();

      if (currentDate.equals(mProgram.getDate().addDays(1))) {
        g.drawString(Localizer.getLocalization(Localizer.I18N_YESTERDAY), x, y);
      } else if (currentDate.equals(mProgram.getDate())) {
        g.drawString(Localizer.getLocalization(Localizer.I18N_TODAY), x, y);
      } else if (currentDate.addDays(1).equals(mProgram.getDate())) {
        g.drawString(Localizer.getLocalization(Localizer.I18N_TOMORROW), x, y);
      } else {
        g.drawString(mProgram.getDateString(), x, y);
      }
      x += ProgramMenuItem.DATE_WIDTH;
    }
    if (mShowStartTime) {
      g.setFont(menuItem.getFont().deriveFont(Font.BOLD));
      g.drawString(mProgram.getTimeString(), x, y);
      x += ProgramMenuItem.TIME_WIDTH;
    }
    g.setFont(menuItem.getFont());
    g.drawString(
        mProgram.getTitle().length() > 70 ? mProgram.getTitle().substring(0, 67) + "..." : mProgram.getTitle(), x, y);
  }
}
