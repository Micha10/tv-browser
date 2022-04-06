/*
 * Copyright Michael Keppler
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package util.ui;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * Panel builder with additional methods for typical TV-Browser UI (e.g. settings tab).<br>
 * When using this class, you should normally use {@link #addParagraph(String)} to add a section to your settings tab
 * and afterwards use {@link #addRow()} to add standard controls into the section. For lists and other large controls
 * you may also use {@link #addGrowingRow()} instead.
 * <br>Don't create rows with height settings yourself!
 *
 * @author bananeweizen
 * @since 3.0
 *
 */
public class EnhancedPanelBuilder extends PanelBuilder {
  public static final String GAP_SPEC_DEFAULT = "5dlu";
  public static final String PARAGRAPH_GAP_SPEC_DEFAULT = "10dlu";
  
  private RowSpec mDefaultRowGapSpec;
  private RowSpec mDefaultParagraphGapSpec;
  
  public EnhancedPanelBuilder(final FormLayout layout, final JPanel parentPanel) {
    this(layout,GAP_SPEC_DEFAULT,parentPanel);
  }

  /**
   * Create a new panel builder with the given columns.
   * You can add rows afterwards by using {@link #addParagraph(String)}, {@link #addRow()} and {@link #addGrowingRow()}.
   * @param layout The layout to use for this builder.
   * @param defaultRowGapSpec The encoded row spec for the default gap size.
   * @param parentPanel the finally built panel will be a child of this parent panel
   * @since 4.2.5
   */
  public EnhancedPanelBuilder(final FormLayout layout, final String defaultRowGapSpec, JPanel parentPanel) {
    this(layout, defaultRowGapSpec, PARAGRAPH_GAP_SPEC_DEFAULT, parentPanel);
  }
  
  /**
   * Create a new panel builder with the given columns.
   * You can add rows afterwards by using {@link #addParagraph(String)}, {@link #addRow()} and {@link #addGrowingRow()}.
   * @param layout The layout to use for this builder.
   * @param defaultRowGapSpec The encoded row spec for the default gap size.
   * @param defaultParagraphGapSpec The encoded row spec for the default paragraph gap size.
   * @param parentPanel the finally built panel will be a child of this parent panel
   * @since 4.2.5
   */
  public EnhancedPanelBuilder(final FormLayout layout, final String defaultRowGapSpec, final String defaultParagraphGapSpec, JPanel parentPanel) {
    super(layout,parentPanel);
    mDefaultRowGapSpec = RowSpec.decode(defaultRowGapSpec);
    mDefaultParagraphGapSpec = RowSpec.decode(defaultParagraphGapSpec);
  }
  
  public EnhancedPanelBuilder(final FormLayout layout) {
    this(layout,GAP_SPEC_DEFAULT);
  }

  /**
   * Create a new panel builder with the given columns.
   * You can add rows afterwards by using {@link #addParagraph(String)}, {@link #addRow()} and {@link #addGrowingRow()}.
   * @param layout The layout to use for this builder.
   * @param defaultRowGapSpec The encoded row spec for the default gap size.
   * @since 4.2.5
   */
  public EnhancedPanelBuilder(final FormLayout layout, final String defaultRowGapSpec) {
    this(layout,defaultRowGapSpec,PARAGRAPH_GAP_SPEC_DEFAULT);
  }

  /**
   * Create a new panel builder with the given columns.
   * You can add rows afterwards by using {@link #addParagraph(String)}, {@link #addRow()} and {@link #addGrowingRow()}.
   * @param layout The layout to use for this builder.
   * @param defaultRowGapSpec The encoded row spec for the default gap size.
   * @param defaultParagraphGapSpec The encoded row spec for the default paragraph gap size.
   * @since 4.2.5
   */
  public EnhancedPanelBuilder(final FormLayout layout, final String defaultRowGapSpec, final String defaultParagraphGapSpec) {
    super(layout);
    mDefaultRowGapSpec = RowSpec.decode(defaultRowGapSpec);
    mDefaultParagraphGapSpec = RowSpec.decode(defaultParagraphGapSpec);
  }

  
  /**
   * Create a new panel builder with the given columns.
   * You can add rows afterwards by using {@link #addParagraph(String)}, {@link #addRow()} and {@link #addGrowingRow()}.
   * @param encodedColumnSpecs The encoded column spec.
   */
  public EnhancedPanelBuilder(final String encodedColumnSpecs) {
    this(encodedColumnSpecs,GAP_SPEC_DEFAULT);
  }
  
  /**
   * Create a new panel builder with the given columns.
   * You can add rows afterwards by using {@link #addParagraph(String)}, {@link #addRow()} and {@link #addGrowingRow()}.
   * @param encodedColumnSpecs The encoded column spec.
   * @param defaultRowGapSpec The encoded row spec for the default gap size.
   * @since 4.2.5
   */
  public EnhancedPanelBuilder(final String encodedColumnSpecs, final String defaultRowGapSpec) {
    this(new FormLayout(encodedColumnSpecs,""), defaultRowGapSpec, PARAGRAPH_GAP_SPEC_DEFAULT);
  }

  /**
   * Create a new panel builder with the given columns, which sits on the given panel.
   * You can add rows afterwards by using {@link #addParagraph(String)}, {@link #addRow()} and {@link #addGrowingRow()}.
   * @param encodedColumnSpecs The encoded column spec.
   * @param parentPanel the finally built panel will be a child of this parent panel
   */
  public EnhancedPanelBuilder(final String encodedColumnSpecs, final JPanel parentPanel) {
    this(encodedColumnSpecs, GAP_SPEC_DEFAULT, parentPanel);
  }

  /**
   * Create a new panel builder with the given columns, which sits on the given panel.
   * You can add rows afterwards by using {@link #addParagraph(String)}, {@link #addRow()} and {@link #addGrowingRow()}.
   * @param encodedColumnSpecs The encoded column spec.
   * @param defaultRowGapSpec The encoded row spec for the default gap size.
   * @param parentPanel the finally built panel will be a child of this parent panel
   * @since 4.2.5
   */
  public EnhancedPanelBuilder(final String encodedColumnSpecs, final String defaultRowGapSpec, final JPanel parentPanel) {
    this(encodedColumnSpecs, defaultRowGapSpec, PARAGRAPH_GAP_SPEC_DEFAULT, parentPanel);
  }
  
  /**
   * Create a new panel builder with the given columns, which sits on the given panel.
   * You can add rows afterwards by using {@link #addParagraph(String)}, {@link #addRow()} and {@link #addGrowingRow()}.
   * @param encodedColumnSpecs The encoded column spec.
   * @param defaultRowGapSpec The encoded row spec for the default gap size.
   * @param defaultParagraphGapSpec The encoded row spec for the default paragraph gap size.
   * @param parentPanel the finally built panel will be a child of this parent panel
   * @since 4.2.5
   */
  public EnhancedPanelBuilder(final String encodedColumnSpecs, final String defaultRowGapSpec, final String defaultParagraphGapSpec, final JPanel parentPanel) {
    super(new FormLayout(encodedColumnSpecs,""), parentPanel);
    mDefaultRowGapSpec = RowSpec.decode(defaultRowGapSpec);
    mDefaultParagraphGapSpec = RowSpec.decode(defaultParagraphGapSpec);
  }
  
  /**
   * create a new section in the layout, which is separated from the previous line by a PARAGRAPH_GAP
   * @param textWithMnemonic label string
   * @return the new separator component
   */
  public JComponent addParagraph(final String textWithMnemonic) {
    return addParagraph(textWithMnemonic, -1);
  }

  /**
   * create a new section in the layout, which is separated from the previous line by a PARAGRAPH_GAP
   * NOTE: If a column index is given the separator spreads over all columns from columnIndex to the last column.
   * 
   * @param textWithMnemonic label string
   * @param columnStartIndex The column index the component should be added to. If -1 is added to the whole row.
   * @return the new separator component
   * @since 4.2.5
   */
  public JComponent addParagraph(final String textWithMnemonic, int columnStartIndex) {
    return addParagraph(textWithMnemonic, columnStartIndex, -1);
  }
  
  /**
   * create a new section in the layout, which is separated from the previous line by a PARAGRAPH_GAP
   * NOTE: If a column index is given the separator spreads over all columns from columnIndex to the last column.
   * 
   * @param textWithMnemonic label string
   * @param columnStartIndex The column index the component should be added to. If -1 is added to the whole row.
   * @param colSpan The number of columns the component should be spread over. If -1 is added until the last column.
   * @return the new separator component
   * @since 4.2.5
   */
  public JComponent addParagraph(final String textWithMnemonic, int columnStartIndex, int colSpan) {
    if (getRowCount() > 0) {
      appendRow(mDefaultParagraphGapSpec);
    }
    else {
      appendRow(FormSpecs.NARROW_LINE_GAP_ROWSPEC);
    }
    appendRow(FormSpecs.DEFAULT_ROWSPEC);
    incrementRowNumber(true);
    if (textWithMnemonic != null && !textWithMnemonic.isEmpty()) {
      if(columnStartIndex == -1 && colSpan == -1) {
        return addSeparator(textWithMnemonic, CC.xyw(1, getRowCount(), getColumnCount()));
      }
      else if(columnStartIndex != -1 && colSpan == -1) {
        return addSeparator(textWithMnemonic, CC.xyw(columnStartIndex, getRowCount(), getColumnCount()-columnStartIndex));
      }
      else if(columnStartIndex == -1 && colSpan != -1) {
        return addSeparator(textWithMnemonic, CC.xyw(1, getRowCount(), colSpan));
      }
      else {
        return addSeparator(textWithMnemonic, CC.xyw(columnStartIndex, getRowCount(), colSpan));
      }
    }
    return null;
  }
  
  /**
   * Add a new standard layout row to the builders layout.
   * It is separated from the preceding row with a LINE_GAP. Use {@link #getRow()} to address this line
   * afterwards.
   *
   * @return the builder
   */
  public PanelBuilder addRow() {
    return addRow(true);
  }
  
  /**
   * Add a new standard layout row to the builders layout.
   * It is separated from the preceding row with a LINE_GAP if parameter withGap is <code>true</code>. Use {@link #getRow()} to address this line
   * afterwards.
   * 
   * @param withGap If the LINE_GAP should be added
   * @return the builder
   */
  public PanelBuilder addRow(boolean withGap) {
    return addRow(FormSpecs.DEFAULT_ROWSPEC.encode(),withGap);
  }

  private void incrementRowNumber(boolean lineGap) {
    // there is no line number zero, therefore only add one row, if we are still in the first line
    if (getRow() == 1 || !lineGap) {
      nextRow();
    }
    else {
      nextRow(2);
    }
  }

  /**
   * Add a new growing layout row to the builders layout.
   * It is separated from the preceding row by LINE_GAP and will grow to take the available space.
   * @return the builder
   */
  public PanelBuilder addGrowingRow() {
    return addGrowingRow(true);
  }


  /**
   * Add a new growing layout row to the builders layout.
   * It is separated from the preceding row by LINE_GAP if parameter withGap is <code>true</code> and will grow to take the available space.
   * @param withGap If the LINE_GAP should be added
   * @return the builder
   */
  public PanelBuilder addGrowingRow(boolean withGap) {
    return addRow("fill:default:grow", withGap);
  }
  
  /**
   * Add a new layout row with the given height to the builders layout.
   * It is separated from the preceding row with a LINE_GAP.
   * Use {@link #getRow()} to address this line afterwards.<br>
   * This method should normally not be used! Use {@link #addRow()} or {@link #addGrowingRow()} instead.
   * The necessary sizes for rows will be calculated by the PanelBuilder.
   * @param rowHeightCode row height
   * @return the builder
   */
  public PanelBuilder addRow(final String rowHeightCode) {
    return addRow(rowHeightCode, true);
  }
  
  /**
   * Add a new layout row with the given height to the builders layout.
   * It is separated from the preceding row with a LINE_GAP if parameter withGap is <code>true</code>.
   * Use {@link #getRow()} to address this line afterwards.<br>
   * This method should normally not be used! Use {@link #addRow()} or {@link #addGrowingRow()} instead.
   * The necessary sizes for rows will be calculated by the PanelBuilder.
   * @param rowHeightCode row height
   * @param withGap If the LINE_GAP should be added
   * @return the builder
   */
  public PanelBuilder addRow(String rowHeightCode, boolean withGap) {
    if(withGap) {
      if(rowHeightCode.contains(",")) {
        String[] parts = rowHeightCode.split(",");
        
        if(parts.length == 2) {
          appendRow(parts[0]);
          rowHeightCode = parts[1];
        }
        else {
          appendRow(mDefaultRowGapSpec);
          rowHeightCode = parts[parts.length-1];
        }
      }
      else {
        appendRow(mDefaultRowGapSpec);
      }
    }
    
    appendRow(rowHeightCode);
    incrementRowNumber(withGap);
    return this;
  }
  
  /**
   * Adds a new standard row with gap to the layout and then adds the component to the new row from first to last column.
   * 
   * @param component The component to add.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addRowFull(final Component component) {
    addRow();
    return add(component, CC.xyw(1, getRowCount(), getColumnCount()));
  }

  /**
   * Adds a new standard row with gap to the layout and then adds the component to the new row.
   * 
   * @param component The component to add.
   * @param columnIndex The column index the component should be added to.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addRow(final Component component, final int columnIndex) {
    addRow();
    return add(component, CC.xy(columnIndex, getRowCount()));
  }
  
  /**
   * Adds a new standard row with gap to the layout and then adds the component to the new row
   * spanning from columnStartIndex to the last column.
   * 
   * @param component The component to add.
   * @param columnStartIndex The column index the component should be added to.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addRowFull(final Component component, final int columnStartIndex) {
    addRow();
    return add(component, CC.xyw(columnStartIndex, getRowCount(), getColumnCount() - columnStartIndex + 1));
  }
    
  /**
   * Adds a new standard row with gap to the layout and then adds the component to the new row.
   * 
   * @param component The component to add.
   * @param columnIndex The column index the component should be added to.
   * @param colSpan The number of columns the component should be spread over.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addRow(final Component component, final int columnIndex, final int colSpan) {
    addRow();
    return add(component, CC.xyw(columnIndex, getRowCount(), colSpan));
  }
  
  /**
   * Adds a new standard row to the layout and then adds the component to the new row from first to last column.
   * 
   * @param withGap If the LINE_GAP should be added
   * @param component The component to add.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addRow(final boolean withGap, final Component component) {
    addRow(withGap);
    return add(component, CC.xyw(1, getRowCount(), getColumnCount()));
  }

  /**
   * Adds a new standard row to the layout and then adds the component to the new row.
   * 
   * @param withGap If the LINE_GAP should be added
   * @param component The component to add.
   * @param columnIndex The column index the component should be added to.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addRow(final boolean withGap, final Component component, final int columnIndex) {
    addRow(withGap);
    return add(component, CC.xy(columnIndex, getRowCount()));
  }
  
  /**
   * Adds a new standard row to the layout and then adds the component to the new row
   * spanning from columnStartIndex to the last column.
   * 
   * @param withGap If the LINE_GAP should be added
   * @param component The component to add.
   * @param columnStartIndex The column index the component should be added to.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addRowFull(final boolean withGap, final Component component, final int columnStartIndex) {
    addRow(withGap);
    return add(component, CC.xyw(columnStartIndex, getRowCount(), getColumnCount() - columnStartIndex + 1));
  }
  
  /**
   * Adds a new standard row to the layout and then adds the component to the new row.
   * 
   * @param withGap If the LINE_GAP should be added
   * @param component The component to add.
   * @param columnIndex The column index the component should be added to.
   * @param colSpan The number of columns the component should be spread over.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addRow(final boolean withGap, final Component component, final int columnIndex, final int colSpan) {
    addRow(withGap);
    return add(component, CC.xyw(columnIndex, getRowCount(), colSpan));
  }
 
  /**
   * Adds a new row with gap to the layout and then adds the component to the new row from first to last column.
   * 
   * @param rowHeightCode row height
   * @param component The component to add.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addRow(final String rowHeightCode, final Component component) {
    addRow(rowHeightCode, true);
    return add(component, CC.xyw(1, getRowCount(), getColumnCount()));
  }
  
  /**
   * Adds a new row with gap to the layout and then adds the component to the new row.
   * 
   * @param rowHeightCode row height
   * @param component The component to add.
   * @param columnIndex The column index the component should be added to.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addRow(final String rowHeightCode, final Component component, final int columnIndex) {
    addRow(rowHeightCode, true);
    return add(component, CC.xy(columnIndex, getRowCount()));
  }
  
  /**
   * Adds a new row with gap to the layout and then adds the component to the new row
   * spanning from columnStartIndex to the last column.
   * 
   * @param rowHeightCode row height
   * @param component The component to add.
   * @param columnStartIndex The column index the component should be added to.
   * @return The component given with parameter component.
   * @since 4.2.5
   */  
  public Component addRowFull(final String rowHeightCode, final Component component, final int columnStartIndex) {
    addRow(rowHeightCode, true);
    return add(component, CC.xyw(columnStartIndex, getRowCount(), getColumnCount() - columnStartIndex + 1));
  }
  
  /**
   * Adds a new row with gap to the layout and then adds the component to the new row.
   * 
   * @param rowHeightCode row height
   * @param component The component to add.
   * @param columnIndex The column index the component should be added to.
   * @param colSpan The number of columns the component should be spread over.
   * @return The component given with parameter component.
   * @since 4.2.5
   */  
  public Component addRow(final String rowHeightCode, final Component component, final int columnIndex, final int colSpan) {
    addRow(rowHeightCode, true);
    return add(component, CC.xyw(columnIndex, getRowCount(), colSpan));
  }

  /**
   * Adds a new row to the layout and then adds the component to the new row from first to last column.
   * 
   * @param rowHeightCode row height
   * @param withGap If the LINE_GAP should be added
   * @param component The component to add.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addRow(final String rowHeightCode, final boolean withGap, final Component component) {
    addRow(rowHeightCode, withGap);
    return add(component, CC.xyw(1, getRowCount(), getColumnCount()));
  }

  /**
   * Adds a new row to the layout and then adds the component to the new row.
   * 
   * @param rowHeightCode row height
   * @param withGap If the LINE_GAP should be added
   * @param component The component to add.
   * @param columnIndex The column index the component should be added to.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addRow(final String rowHeightCode, final boolean withGap, final Component component, final int columnIndex) {
    addRow(rowHeightCode, withGap);
    return add(component, CC.xy(columnIndex, getRowCount()));
  }
  
  /**
   * Adds a new row to the layout and then adds the component to the new row
   * spanning from columnStartIndex to the last column.
   * 
   * @param rowHeightCode row height
   * @param withGap If the LINE_GAP should be added
   * @param component The component to add.
   * @param columnStartIndex The column index the component should be added to.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addRowFull(final String rowHeightCode, final boolean withGap, final Component component, final int columnStartIndex) {
    addRow(rowHeightCode, withGap);
    return add(component, CC.xyw(columnStartIndex, getRowCount(), getColumnCount() - columnStartIndex + 1));
  }
  
  /**
   * Adds a new row to the layout and then adds the component to the new row.
   * 
   * @param rowHeightCode row height
   * @param withGap If the LINE_GAP should be added
   * @param component The component to add.
   * @param columnIndex The column index the component should be added to.
   * @param colSpan The number of columns the component should be spread over.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addRow(final String rowHeightCode, final boolean withGap, final Component component, final int columnIndex, final int colSpan) {
    addRow(rowHeightCode, withGap);
    return add(component, CC.xyw(columnIndex, getRowCount(), colSpan));
  }
  
  /**
   * Adds a new standard growing row with gap to the layout and then adds the component to the new row from first to last column.
   * 
   * @param withGap If the LINE_GAP should be added
   * @param component The component to add.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addGrowingRowFull(final Component component) {
    addGrowingRow();
    return add(component, CC.xyw(1, getRowCount(), getColumnCount()));
  }

  /**
   * Adds a new standard growing row with gap to the layout and then adds the component to the new row.
   * 
   * @param component The component to add.
   * @param columnIndex The column index the component should be added to.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addGrowingRow(final Component component, final int columnIndex) {
    addGrowingRow();
    return add(component, CC.xy(columnIndex, getRowCount()));
  }
  
  /**
   * Adds a new standard growing row with gap to the layout and then adds the component to the new row
   * spanning from columnStartIndex to the last column.
   * 
   * @param component The component to add.
   * @param columnStartIndex The column index the component should be added to.
   * @param colSpan The number of columns the component should be spread over.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addGrowingRowFull(final Component component, final int columnStartIndex) {
    addGrowingRow();
    return add(component, CC.xyw(columnStartIndex, getRowCount(), getColumnCount() - columnStartIndex + 1));
  }
  
  /**
   * Adds a new standard growing row with gap to the layout and then adds the component to the new row.
   * 
   * @param component The component to add.
   * @param columnIndex The column index the component should be added to.
   * @param colSpan The number of columns the component should be spread over.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addGrowingRow(final Component component, final int columnIndex, final int colSpan) {
    addGrowingRow();
    return add(component, CC.xyw(columnIndex, getRowCount(), colSpan));
  }
  
  /**
   * Adds a new standard growing row to the layout and then adds the component to the new row from first to last column.
   * 
   * @param withGap If the LINE_GAP should be added
   * @param component The component to add.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addGrowingRow(final boolean withGap, final Component component) {
    addGrowingRow(withGap);
    return add(component, CC.xyw(1, getRowCount(), getColumnCount()));
  }

  /**
   * Adds a new standard growing row to the layout and then adds the component to the new row.
   * 
   * @param withGap If the LINE_GAP should be added
   * @param component The component to add.
   * @param columnIndex The column index the component should be added to.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addGrowingRow(final boolean withGap, final Component component, final int columnIndex) {
    addGrowingRow(withGap);
    return add(component, CC.xy(columnIndex, getRowCount()));
  }
  
  /**
   * Adds a new standard growing row to the layout and then adds the component to the new row
   * spanning from columnStartIndex to the last column.
   * 
   * @param withGap If the LINE_GAP should be added
   * @param component The component to add.
   * @param columnStartIndex The column index the component should be added to.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addGrowingRowFull(final boolean withGap, final Component component, final int columnStartIndex) {
    addGrowingRow(withGap);
    return add(component, CC.xyw(columnStartIndex, getRowCount(), getColumnCount() - columnStartIndex + 1));
  }
  
  /**
   * Adds a new standard growing row to the layout and then adds the component to the new row.
   * 
   * @param withGap If the LINE_GAP should be added
   * @param component The component to add.
   * @param columnIndex The column index the component should be added to.
   * @param colSpan The number of columns the component should be spread over.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addGrowingRow(final boolean withGap, final Component component, final int columnIndex, final int colSpan) {
    addGrowingRow(withGap);
    return add(component, CC.xyw(columnIndex, getRowCount(), colSpan));
  }
  
  /**
   * Adds a new standard row with gap to the layout and then adds a separator with the given text to the new row from first to last column.
   * 
   * @param textWithMnemonic The text for the separator.
   * @return A separator with the parameter text..
   * @since 4.2.5
   */
  public JComponent addSeparatorRowFull(final String textWithMnemonic) {
    addRow();
    return addSeparator(textWithMnemonic, CC.xyw(1, getRowCount(), getColumnCount()));
  }

  /**
   * Adds a new standard row with gap to the layout and then adds a separator with the given text to the new row.
   * 
   * @param textWithMnemonic The text for the separator.
   * @param columnIndex The column index the separator should be added to.
   * @return A separator with the parameter text..
   * @since 4.2.5
   */
  public JComponent addSeparatorRow(final String textWithMnemonic, final int columnIndex) {
    addRow();
    return addSeparator(textWithMnemonic, CC.xy(columnIndex, getRowCount()));
  }
  
  /**
   * Adds a new standard row with gap to the layout and then adds a separator with the given text to the new row
   * spanning from columnStartIndex to the last column.
   * 
   * @param textWithMnemonic The text for the separator.
   * @param columnStartIndex The column index the separator should be added to.
   * @return A separator with the parameter text..
   * @since 4.2.5
   */
  public JComponent addSeparatorRowFull(final String textWithMnemonic, final int columnStartIndex) {
    addRow();
    return addSeparator(textWithMnemonic, CC.xyw(columnStartIndex, getRowCount(), getColumnCount() - columnStartIndex + 1));
  }
  
  /**
   * Adds a new standard row with gap to the layout and then adds a separator with the given text to the new row.
   * 
   * @param textWithMnemonic The text for the separator.
   * @param columnIndex The column index the separator should be added to.
   * @param colSpan The number of columns the separator should be spread over.
   * @return A separator with the parameter text..
   * @since 4.2.5
   */
  public JComponent addSeparatorRow(final String textWithMnemonic, final int columnIndex, final int colSpan) {
    addRow();
    return addSeparator(textWithMnemonic, CC.xyw(columnIndex, getRowCount(), colSpan));
  }
  
  /**
   * Adds a new standard row to the layout and then adds a separator with the given text to the new row from first to last column.
   * 
   * @param withGap If the LINE_GAP should be added
   * @param textWithMnemonic The text for the separator.
   * @return A separator with the parameter text..
   * @since 4.2.5
   */
  public JComponent addSeparatorRowFull(final boolean withGap, final String textWithMnemonic) {
    addRow(withGap);
    return addSeparator(textWithMnemonic, CC.xyw(1, getRowCount(), getColumnCount()));
  }

  /**
   * Adds a new standard row to the layout and then adds a separator with the given text to the new row.
   * 
   * @param withGap If the LINE_GAP should be added
   * @param textWithMnemonic The text for the separator.
   * @param columnIndex The column index the separator should be added to.
   * @return A separator with the parameter text..
   * @since 4.2.5
   */
  public JComponent addSeparatorRow(final boolean withGap, final String textWithMnemonic, final int columnIndex) {
    addRow(withGap);
    return addSeparator(textWithMnemonic, CC.xy(columnIndex, getRowCount()));
  }
  
  /**
   * Adds a new standard row to the layout and then adds a separator with the given text to the new row
   * spanning from columnStartIndex to the last column.
   * 
   * @param withGap If the LINE_GAP should be added
   * @param textWithMnemonic The text for the separator.
   * @param columnStartIndex The column index the separator should be added to.
   * @return A separator with the parameter text..
   * @since 4.2.5
   */
  public JComponent addSeparatorRowFull(final boolean withGap, final String textWithMnemonic, final int columnStartIndex) {
    addRow(withGap);
    return addSeparator(textWithMnemonic, CC.xyw(columnStartIndex, getRowCount(), getColumnCount() - columnStartIndex + 1));
  }
  
  /**
   * Adds a new standard row to the layout and then adds a separator with the given text to the new row.
   * 
   * @param withGap If the LINE_GAP should be added
   * @param textWithMnemonic The text for the separator.
   * @param columnIndex The column index the separator should be added to.
   * @param colSpan The number of columns the separator should be spread over.
   * @return A separator with the parameter text..
   * @since 4.2.5
   */
  public JComponent addSeparatorRow(final boolean withGap, final String textWithMnemonic, final int columnIndex, final int colSpan) {
    addRow(withGap);
    return addSeparator(textWithMnemonic, CC.xyw(columnIndex, getRowCount(), colSpan));
  }
 
  /**
   * Adds a new row with gap to the layout and then adds a separator with the given text to the new row from first to last column.
   * 
   * @param rowHeightCode row height
   * @param textWithMnemonic The text for the separator.
   * @return A separator with the parameter text..
   * @since 4.2.5
   */
  public JComponent addSeparatorRow(final String rowHeightCode, final String textWithMnemonic) {
    addRow(rowHeightCode, true);
    return addSeparator(textWithMnemonic, CC.xyw(1, getRowCount(), getColumnCount()));
  }
  
  /**
   * Adds a new row with gap to the layout and then adds a separator with the given text to the new row.
   * 
   * @param rowHeightCode row height
   * @param textWithMnemonic The text for the separator.
   * @param columnIndex The column index the separator should be added to.
   * @return A separator with the parameter text..
   * @since 4.2.5
   */
  public JComponent addSeparatorRow(final String rowHeightCode, final String textWithMnemonic, final int columnIndex) {
    addRow(rowHeightCode, true);
    return addSeparator(textWithMnemonic, CC.xy(columnIndex, getRowCount()));
  }
  
  /**
   * Adds a new row with gap to the layout and then adds a separator with the given text to the new row
   * spanning from columnStartIndex to the last column.
   * 
   * @param rowHeightCode row height
   * @param textWithMnemonic The text for the separator.
   * @param columnStartIndex The column index the separator should be added to.
   * @return A separator with the parameter text..
   * @since 4.2.5
   */  
  public JComponent addSeparatorRowFull(final String rowHeightCode, final String textWithMnemonic, final int columnStartIndex) {
    addRow(rowHeightCode, true);
    return addSeparator(textWithMnemonic, CC.xyw(columnStartIndex, getRowCount(), getColumnCount() - columnStartIndex + 1));
  }
  
  /**
   * Adds a new row with gap to the layout and then adds a separator with the given text to the new row.
   * 
   * @param rowHeightCode row height
   * @param textWithMnemonic The text for the separator.
   * @param columnIndex The column index the separator should be added to.
   * @param colSpan The number of columns the separator should be spread over.
   * @return A separator with the parameter text..
   * @since 4.2.5
   */  
  public JComponent addSeparatorRow(final String rowHeightCode, final String textWithMnemonic, final int columnIndex, final int colSpan) {
    addRow(rowHeightCode, true);
    return addSeparator(textWithMnemonic, CC.xyw(columnIndex, getRowCount(), colSpan));
  }

  /**
   * Adds a new row to the layout and then adds a separator with the given text to the new row from first to last column.
   * 
   * @param rowHeightCode row height
   * @param withGap If the LINE_GAP should be added
   * @param textWithMnemonic The text for the separator.
   * @return A separator with the parameter text..
   * @since 4.2.5
   */
  public JComponent addSeparatorRow(final String rowHeightCode, final boolean withGap, final String textWithMnemonic) {
    addRow(rowHeightCode, withGap);
    return addSeparator(textWithMnemonic, CC.xyw(1, getRowCount(), getColumnCount()));
  }

  /**
   * Adds a new row to the layout and then adds a separator with the given text to the new row.
   * 
   * @param rowHeightCode row height
   * @param withGap If the LINE_GAP should be added
   * @param textWithMnemonic The text for the separator.
   * @param columnIndex The column index the separator should be added to.
   * @return A separator with the parameter text..
   * @since 4.2.5
   */
  public JComponent addSeparatorRow(final String rowHeightCode, final boolean withGap, final String textWithMnemonic, final int columnIndex) {
    addRow(rowHeightCode, withGap);
    return addSeparator(textWithMnemonic, CC.xy(columnIndex, getRowCount()));
  }
  
  /**
   * Adds a new row to the layout and then adds a separator with the given text to the new row
   * spanning from columnStartIndex to the last column.
   * 
   * @param rowHeightCode row height
   * @param withGap If the LINE_GAP should be added
   * @param textWithMnemonic The text for the separator.
   * @param columnStartIndex The column index the separator should be added to.
   * @return A separator with the parameter text..
   * @since 4.2.5
   */
  public JComponent addSeparatorRowFull(final String rowHeightCode, final boolean withGap, final String textWithMnemonic, final int columnStartIndex) {
    addRow(rowHeightCode, withGap);
    return addSeparator(textWithMnemonic, CC.xyw(columnStartIndex, getRowCount(), getColumnCount() - columnStartIndex));
  }
  
  /**
   * Adds a new row to the layout and then adds a separator with the given text to the new row.
   * 
   * @param rowHeightCode row height
   * @param withGap If the LINE_GAP should be added
   * @param textWithMnemonic The text for the separator.
   * @param columnIndex The column index the separator should be added to.
   * @param colSpan The number of columns the separator should be spread over.
   * @return A separator with the parameter text..
   * @since 4.2.5
   */
  public JComponent addSeparatorRow(final String rowHeightCode, final boolean withGap, final String textWithMnemonic, final int columnIndex, final int colSpan) {
    addRow(rowHeightCode, withGap);
    return addSeparator(textWithMnemonic, CC.xyw(columnIndex, getRowCount(), colSpan));
  }
  
  /**
   * Adds a new standard row with gap to the layout and then adds a label with the given text to the new row from first to last column.
   * 
   * @param textWithMnemonic The text for the separator.
   * @return A label with the given parameter text.
   * @since 4.2.5
   */
  public JLabel addLabelRowFull(final String textWithMnemonic) {
    addRow();
    return addLabel(textWithMnemonic, CC.xyw(1, getRowCount(), getColumnCount()));
  }

  /**
   * Adds a new standard row with gap to the layout and then adds a label with the given text to the new row.
   * 
   * @param textWithMnemonic The text for the separator.
   * @param columnIndex The column index the label with the given text should be added to.
   * @return A label with the given parameter text.
   * @since 4.2.5
   */
  public JLabel addLabelRow(final String textWithMnemonic, final int columnIndex) {
    addRow();
    return addLabel(textWithMnemonic, CC.xy(columnIndex, getRowCount()));
  }
  
  /**
   * Adds a new standard row with gap to the layout and then adds a label with the given text to the new row
   * spanning from columnStartIndex to the last column.
   * 
   * @param textWithMnemonic The text for the separator.
   * @param columnStartIndex The column index the label with the given text should be added to.
   * @return A label with the given parameter text.
   * @since 4.2.5
   */
  public JLabel addLabelRowFull(final String textWithMnemonic, final int columnStartIndex) {
    addRow();
    return addLabel(textWithMnemonic, CC.xyw(columnStartIndex, getRowCount(), getColumnCount() - columnStartIndex + 1));
  }
    
  /**
   * Adds a new standard row with gap to the layout and then adds a label with the given text to the new row.
   * 
   * @param textWithMnemonic The text for the separator.
   * @param columnIndex The column index the label with the given text should be added to.
   * @param colSpan The number of columns the label with the given text should be spread over.
   * @return A label with the given parameter text.
   * @since 4.2.5
   */
  public JLabel addLabelRow(final String textWithMnemonic, final int columnIndex, final int colSpan) {
    addRow();
    return addLabel(textWithMnemonic, CC.xyw(columnIndex, getRowCount(), colSpan));
  }
  
  /**
   * Adds a new standard row to the layout and then adds a label with the given text to the new row from first to last column.
   * 
   * @param withGap If the LINE_GAP should be added
   * @param textWithMnemonic The text for the separator.
   * @return A label with the given parameter text.
   * @since 4.2.5
   */
  public JLabel addLabelRow(final boolean withGap, final String textWithMnemonic) {
    addRow(withGap);
    return addLabel(textWithMnemonic, CC.xyw(1, getRowCount(), getColumnCount()));
  }

  /**
   * Adds a new standard row to the layout and then adds a label with the given text to the new row.
   * 
   * @param withGap If the LINE_GAP should be added
   * @param textWithMnemonic The text for the separator.
   * @param columnIndex The column index the label with the given text should be added to.
   * @return A label with the given parameter text.
   * @since 4.2.5
   */
  public JLabel addLabelRow(final boolean withGap, final String textWithMnemonic, final int columnIndex) {
    addRow(withGap);
    return addLabel(textWithMnemonic, CC.xy(columnIndex, getRowCount()));
  }
  
  /**
   * Adds a new standard row to the layout and then adds a label with the given text to the new row
   * spanning from columnStartIndex to the last column.
   * 
   * @param withGap If the LINE_GAP should be added
   * @param textWithMnemonic The text for the separator.
   * @param columnStartIndex The column index the label with the given text should be added to.
   * @return A label with the given parameter text.
   * @since 4.2.5
   */
  public JLabel addLabelRowFull(final boolean withGap, final String textWithMnemonic, final int columnStartIndex) {
    addRow(withGap);
    return addLabel(textWithMnemonic, CC.xyw(columnStartIndex, getRowCount(), getColumnCount() - columnStartIndex + 1));
  }
  
  /**
   * Adds a new standard row to the layout and then adds a label with the given text to the new row.
   * 
   * @param withGap If the LINE_GAP should be added
   * @param textWithMnemonic The text for the separator.
   * @param columnIndex The column index the label with the given text should be added to.
   * @param colSpan The number of columns the label with the given text should be spread over.
   * @return A label with the given parameter text.
   * @since 4.2.5
   */
  public JLabel addLabelRow(final boolean withGap, final String textWithMnemonic, final int columnIndex, final int colSpan) {
    addRow(withGap);
    return addLabel(textWithMnemonic, CC.xyw(columnIndex, getRowCount(), colSpan));
  }
 
  /**
   * Adds a new row with gap to the layout and then adds a label with the given text to the new row from first to last column.
   * 
   * @param rowHeightCode row height
   * @param textWithMnemonic The text for the separator.
   * @return A label with the given parameter text.
   * @since 4.2.5
   */
  public JLabel addLabelRow(final String rowHeightCode, final String textWithMnemonic) {
    addRow(rowHeightCode, true);
    return addLabel(textWithMnemonic, CC.xyw(1, getRowCount(), getColumnCount()));
  }
  
  /**
   * Adds a new row with gap to the layout and then adds a label with the given text to the new row.
   * 
   * @param rowHeightCode row height
   * @param textWithMnemonic The text for the separator.
   * @param columnIndex The column index the label with the given text should be added to.
   * @return A label with the given parameter text.
   * @since 4.2.5
   */
  public JLabel addLabelRow(final String rowHeightCode, final String textWithMnemonic, final int columnIndex) {
    addRow(rowHeightCode, true);
    return addLabel(textWithMnemonic, CC.xy(columnIndex, getRowCount()));
  }
  
  /**
   * Adds a new row with gap to the layout and then adds a label with the given text to the new row.
   * 
   * @param rowHeightCode row height
   * @param textWithMnemonic The text for the separator.
   * @param columnStartIndex The column index the label with the given text should be added to.
   * @return A label with the given parameter text.
   * @since 4.2.5
   */  
  public JLabel addLabelRowFull(final String rowHeightCode, final String textWithMnemonic, final int columnStartIndex) {
    addRow(rowHeightCode, true);
    return addLabel(textWithMnemonic, CC.xyw(columnStartIndex, getRowCount(), getColumnCount() - columnStartIndex + 1));
  }
  
  /**
   * Adds a new row with gap to the layout and then adds a label with the given text to the new row.
   * 
   * @param rowHeightCode row height
   * @param textWithMnemonic The text for the separator.
   * @param columnIndex The column index the label with the given text should be added to.
   * @param colSpan The number of columns the label with the given text should be spread over.
   * @return A label with the given parameter text.
   * @since 4.2.5
   */  
  public JLabel addLabelRow(final String rowHeightCode, final String textWithMnemonic, final int columnIndex, final int colSpan) {
    addRow(rowHeightCode, true);
    return addLabel(textWithMnemonic, CC.xyw(columnIndex, getRowCount(), colSpan));
  }

  /**
   * Adds a new row to the layout and then adds a label with the given text to the new row from first to last column.
   * 
   * @param rowHeightCode row height
   * @param withGap If the LINE_GAP should be added
   * @param textWithMnemonic The text for the separator.
   * @return A label with the given parameter text.
   * @since 4.2.5
   */
  public JLabel addLabelRow(final String rowHeightCode, final boolean withGap, final String textWithMnemonic) {
    addRow(rowHeightCode, withGap);
    return addLabel(textWithMnemonic, CC.xyw(1, getRowCount(), getColumnCount()));
  }

  /**
   * Adds a new row to the layout and then adds a label with the given text to the new row.
   * 
   * @param rowHeightCode row height
   * @param withGap If the LINE_GAP should be added
   * @param textWithMnemonic The text for the separator.
   * @param columnIndex The column index the label with the given text should be added to.
   * @return A label with the given parameter text.
   * @since 4.2.5
   */
  public JLabel addLabelRow(final String rowHeightCode, final boolean withGap, final String textWithMnemonic, final int columnIndex) {
    addRow(rowHeightCode, withGap);
    return addLabel(textWithMnemonic, CC.xy(columnIndex, getRowCount()));
  }
  
  /**
   * Adds a new row to the layout and then adds a label with the given text to the new row
   * spanning from columnStartIndex to the last column.
   * 
   * @param rowHeightCode row height
   * @param withGap If the LINE_GAP should be added
   * @param textWithMnemonic The text for the separator.
   * @param columnStartIndex The column index the label with the given text should be added to.
   * @return A label with the given parameter text.
   * @since 4.2.5
   */
  public JLabel addLabelRowFull(final String rowHeightCode, final boolean withGap, final String textWithMnemonic, final int columnStartIndex) {
    addRow(rowHeightCode, withGap);
    return addLabel(textWithMnemonic, CC.xyw(columnStartIndex, getRowCount(), getColumnCount() - columnStartIndex + 1));
  }

  /**
   * Adds a new row to the layout and then adds a label with the given text to the new row.
   * 
   * @param rowHeightCode row height
   * @param withGap If the LINE_GAP should be added
   * @param textWithMnemonic The text for the separator.
   * @param columnIndex The column index the label with the given text should be added to.
   * @param colSpan The number of columns the label with the given text should be spread over.
   * @return A label with the given parameter text.
   * @since 4.2.5
   */
  public JLabel addLabelRow(final String rowHeightCode, final boolean withGap, final String textWithMnemonic, final int columnIndex, final int colSpan) {
    addRow(rowHeightCode, withGap);
    return addLabel(textWithMnemonic, CC.xyw(columnIndex, getRowCount(), colSpan));
  }
  
  /**
   * Adds the component to the last row from first to last column.
   * 
   * @param component The component to add.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addFull(final JComponent component) {
    return add(component, CC.xyw(1, getRowCount(), getColumnCount()));
  }

  /**
   * Adds the component to the last row.
   * 
   * @param component The component to add.
   * @param columnIndex The column index the component should be added to.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component add(final Component component, final int columnIndex) {
    return add(component, CC.xy(columnIndex, getRowCount()));
  }
  
  /**
   * Adds the component to the last row
   * spanning from columnStartIndex to the last column.
   * 
   * @param component The component to add.
   * @param columnStartIndex The column index the component should be added to.
   * @param colSpan The number of columns the component should be spread over.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component addFull(final Component component, final int columnStartIndex) {
    return add(component, CC.xyw(columnStartIndex, getRowCount(), getColumnCount() - columnStartIndex + 1));
  }
  
  /**
   * Adds the component to the last row.
   * 
   * @param component The component to add.
   * @param columnIndex The column index the component should be added to.
   * @param colSpan The number of columns the component should be spread over.
   * @return The component given with parameter component.
   * @since 4.2.5
   */
  public Component add(final Component component, final int columnIndex, final int colSpan) {
    return add(component, CC.xyw(columnIndex, getRowCount(), colSpan));
  }
  
  /**
   * Adds a new label to the last row from first to last column.
   * 
   * @param textWithMnemonic The component to add.
   * @return A label with the given parameter text.
   * @since 4.2.5
   */
  public JLabel labelAddFull(final String textWithMnemonic) {
    return addLabel(textWithMnemonic, CC.xyw(1, getRowCount(), getColumnCount()));
  }

  /**
   * Adds a new label to the last row.
   * 
   * @param textWithMnemonic The component to add.
   * @param columnIndex The column index the component should be added to.
   * @return A label with the given parameter text.
   * @since 4.2.5
   */
  public JLabel labelAdd(final String textWithMnemonic, final int columnIndex) {
    return addLabel(textWithMnemonic, CC.xy(columnIndex, getRowCount()));
  }
  
  /**
   * Adds a new label to the last row
   * spanning from columnStartIndex to the last column.
   * 
   * @param textWithMnemonic The component to add.
   * @param columnStartIndex The column index the label should be added to.
   * @return A label with the given parameter text.
   * @since 4.2.5
   */
  public Component labelAddFull(final String textWithMnemonic, final int columnStartIndex) {
    return addLabel(textWithMnemonic, CC.xyw(columnStartIndex, getRowCount(), getColumnCount() - columnStartIndex + 1));
  }
  
  /**
   * Adds a new label to the last row.
   * 
   * @param textWithMnemonic The component to add.
   * @param columnIndex The column index the label should be added to.
   * @param colSpan The number of columns the label should be spread over.
   * @return A label with the given parameter text.
   * @since 4.2.5
   */
  public Component labelAdd(final String textWithMnemonic, final int columnIndex, final int colSpan) {
    return addLabel(textWithMnemonic, CC.xyw(columnIndex, getRowCount(), colSpan));
  }
}
