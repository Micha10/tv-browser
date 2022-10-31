/*
 * TV-Browser
 * Copyright (C) 2022 TV-Browser team (dev@tvbrowser.org)
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.core.filters.filtercomponents;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.Program;

/**
 * filter component to filter for a month range
 *
 * @author René Mach
 * @since 4.2.7
 */
public class MonthFilterComponent extends AbstractFilterComponent {

  private static final util.i18n.Localizer LOCALIZER = util.i18n.Localizer
      .getLocalizerFor(MonthFilterComponent.class);
  private JComboBox<Month> mFromBoxMonth;
  private JComboBox<Month> mToBoxMonth;
  private JComboBox<Integer> mFromBoxDay;
  private JComboBox<Integer> mToBoxDay;
  private int mStartDay = 1;
  private int mEndDay = 31;
  private int mStartMonth = Calendar.JANUARY;
  private int mEndMonth = Calendar.DECEMBER;

  public MonthFilterComponent(String name, String description) {
    super(name, description);
  }

  public MonthFilterComponent() {
    this("", "");
  }

  @Override
  public boolean accept(final Program program) {
    boolean result = program.getDate().getMonth() >= (mStartMonth+1) && program.getDate().getMonth() <= (mEndMonth+1);
    
    if(result && program.getDate().getMonth() == (mStartMonth+1)) {
      result = program.getDate().getDayOfMonth() >= mStartDay;
    }
    
    if(result && program.getDate().getMonth() == (mEndMonth+1)) {
      result = program.getDate().getDayOfMonth() <= mEndDay;
    }
    
    return result;
  }

  public String getTypeDescription() {
    return LOCALIZER.msg("description", "Accepts all programs with month between (inclusive) start and end.");
  }
  
  private void handleItemEventFor(ItemEvent e, JComboBox<Integer> box) {
    if(e.getStateChange() == ItemEvent.SELECTED) {
      int value = (int)box.getSelectedItem();
      box.removeAllItems();
      
      int days = ((Month)e.getItem()).mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
      
      for(int i = 1; i <= days; i++) {
        box.addItem(i);
        
        if(value == i) {
          box.setSelectedIndex(box.getItemCount()-1);
        }
      }
    }
  }
  
  @Override
  public JPanel getSettingsPanel() {
    FormLayout layout = new FormLayout(
        "default, 3dlu, default, 3dlu, default", "");
    JPanel content = new JPanel(layout);
    content.setBorder(Borders.DIALOG);

    CellConstraints cc = new CellConstraints();
    int currentRow = 1;

    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("3dlu"));

    mFromBoxDay = new JComboBox<Integer>();
    mFromBoxDay.addItem(mStartDay);
    mFromBoxDay.setSelectedIndex(0);
    mToBoxDay = new JComboBox<Integer>();
    mToBoxDay.addItem(mEndDay);
    mToBoxDay.setSelectedIndex(0);
    
    mFromBoxMonth = new JComboBox<MonthFilterComponent.Month>();
    mFromBoxMonth.addItemListener(e -> {
      handleItemEventFor(e, mFromBoxDay);
    });
    mToBoxMonth = new JComboBox<MonthFilterComponent.Month>();
    mToBoxMonth.addItemListener(e -> {
      handleItemEventFor(e, mToBoxDay);
    });
    
    for(int month = Calendar.JANUARY; month <= Calendar.DECEMBER; month++) {
      mFromBoxMonth.addItem(new Month(month));
      mToBoxMonth.addItem(new Month(month));
      
      if(mStartMonth == month) {
        mFromBoxMonth.setSelectedIndex(mFromBoxMonth.getItemCount()-1);
      }
      if(mEndMonth == month) {
        mToBoxMonth.setSelectedIndex(mToBoxMonth.getItemCount()-1);
      }
    }
    
    content.add(new JLabel(LOCALIZER.msg("from", "From")), cc.xy(
        1, currentRow));
    content.add(mFromBoxDay, cc.xy(3, currentRow));
    content.add(mFromBoxMonth, cc.xy(5, currentRow));
    
    layout.appendRow(RowSpec.decode("pref"));

    content.add(new JLabel(LOCALIZER.msg("to", "Until")), cc.xy(
        1, currentRow += 2));
    content.add(mToBoxDay, cc.xy(3, currentRow));
    content.add(mToBoxMonth, cc.xy(5, currentRow));

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(content, BorderLayout.NORTH);
    return centerPanel;
  }

  @Override
  public int getVersion() {
    return 1;
  }

  @Override
  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {
    mStartMonth = in.readInt();
    mEndMonth = in.readInt();
    mStartDay = in.readInt();
    mEndDay = in.readInt();
  }

  @Override
  public void saveSettings() {
    mStartMonth = ((Month) mFromBoxMonth.getSelectedItem()).mCalendar.get(Calendar.MONTH);
    mEndMonth = ((Month) mToBoxMonth.getSelectedItem()).mCalendar.get(Calendar.MONTH);
    mStartDay = (int)mFromBoxDay.getSelectedItem();
    mEndDay = (int)mToBoxDay.getSelectedItem();
  }

  @Override
  public void write(ObjectOutputStream out) throws IOException {
    out.writeInt(mStartMonth);
    out.writeInt(mEndMonth);
    out.writeInt(mStartDay);
    out.writeInt(mEndDay);
  }

  @Override
  public String toString() {
    return LOCALIZER.msg("month", "Month");
  }
  
  private static final class Month {
    private Calendar mCalendar;
    
    public Month(int month) {
      mCalendar = Calendar.getInstance();
      mCalendar.set(Calendar.YEAR, 2022);
      mCalendar.set(Calendar.DAY_OF_MONTH, 1);
      mCalendar.set(Calendar.MONTH, month);
    }
    
    @Override
    public String toString() {
      return mCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.getDefault());
    }
  }

}
