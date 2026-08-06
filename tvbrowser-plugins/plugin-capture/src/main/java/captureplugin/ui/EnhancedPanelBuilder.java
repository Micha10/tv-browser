package captureplugin.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * Lightweight Swing-only replacement for form-based panel building.
 */
public class EnhancedPanelBuilder {
  private final JPanel mPanel;
  private final int mColumnCount;
  private final boolean[] mGrowingColumns;
  private final Map<Integer, Integer> mRowTopInsets;
  private final Set<Integer> mGrowingRows;
  private int mCurrentRow;

  public EnhancedPanelBuilder(final String encodedColumnSpecs) {
    this(encodedColumnSpecs, new JPanel());
  }

  public EnhancedPanelBuilder(final String encodedColumnSpecs, final JPanel parentPanel) {
    mPanel = parentPanel;
    mPanel.setLayout(new GridBagLayout());
    mCurrentRow = -1;
    mRowTopInsets = new HashMap<>();
    mGrowingRows = new HashSet<>();

    String[] columns = encodedColumnSpecs == null ? new String[0] : encodedColumnSpecs.split(",");
    if (columns.length == 0) {
      mColumnCount = 1;
      mGrowingColumns = new boolean[] { true };
    } else {
      mColumnCount = columns.length;
      mGrowingColumns = new boolean[mColumnCount];
      for (int i = 0; i < columns.length; i++) {
        mGrowingColumns[i] = columns[i].toLowerCase().contains("grow");
      }
    }
  }

  public int getColumnCount() {
    return mColumnCount;
  }

  public JPanel getPanel() {
    return mPanel;
  }

  public JComponent addParagraph(final String textWithMnemonic) {
    int topInset = mCurrentRow >= 0 ? 10 : 0;
    mCurrentRow++;
    mRowTopInsets.put(mCurrentRow, topInset);

    JPanel separatorPanel = new JPanel(new BorderLayout(5, 0));
    separatorPanel.setOpaque(false);

    JLabel label = new JLabel(textWithMnemonic);
    Font f = label.getFont();
    if (f != null) {
      label.setFont(f.deriveFont(Font.BOLD));
    }
    separatorPanel.add(label, BorderLayout.WEST);
    separatorPanel.add(new JSeparator(), BorderLayout.CENTER);

    GridBagConstraints gc = createConstraints(1, mColumnCount);
    gc.insets = new Insets(topInset, 0, 2, 0);
    gc.weightx = 1.0;
    gc.fill = GridBagConstraints.HORIZONTAL;
    gc.anchor = GridBagConstraints.WEST;
    mPanel.add(separatorPanel, gc);

    return separatorPanel;
  }

  public EnhancedPanelBuilder addRow() {
    return addRow(true);
  }

  public EnhancedPanelBuilder addRow(final boolean withGap) {
    return addRow("default", withGap);
  }

  public EnhancedPanelBuilder addGrowingRow() {
    return addGrowingRow(true);
  }

  public EnhancedPanelBuilder addGrowingRow(final boolean withGap) {
    return addRow("fill:default:grow", withGap);
  }

  public EnhancedPanelBuilder addRow(final String rowHeightCode) {
    return addRow(rowHeightCode, true);
  }

  public EnhancedPanelBuilder addRow(final String rowHeightCode, final boolean withGap) {
    mCurrentRow++;
    mRowTopInsets.put(mCurrentRow, withGap ? 5 : 0);

    if (rowHeightCode != null && rowHeightCode.toLowerCase().contains("grow")) {
      mGrowingRows.add(mCurrentRow);
    }

    return this;
  }

  public Component add(final Component component, final int columnIndex) {
    return add(component, columnIndex, 1);
  }

  public Component add(final Component component, final int columnIndex, final int colSpan) {
    if (mCurrentRow < 0) {
      addRow(false);
    }

    GridBagConstraints gc = createConstraints(columnIndex, colSpan);

    int topInset = mRowTopInsets.containsKey(mCurrentRow) ? mRowTopInsets.get(mCurrentRow) : 0;
    gc.insets = new Insets(topInset, 0, 0, 0);

    boolean growingColumn = isGrowingColumn(columnIndex, colSpan);
    boolean growingRow = mGrowingRows.contains(mCurrentRow);

    gc.weightx = growingColumn ? 1.0 : 0.0;
    gc.weighty = growingRow ? 1.0 : 0.0;
    gc.fill = growingRow ? GridBagConstraints.BOTH : (growingColumn ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE);
    gc.anchor = GridBagConstraints.WEST;

    mPanel.add(component, gc);
    return component;
  }

  private GridBagConstraints createConstraints(final int columnIndex, final int colSpan) {
    int startColumn = Math.max(1, Math.min(columnIndex, mColumnCount));
    int span = Math.max(1, colSpan);
    int maxSpan = mColumnCount - startColumn + 1;
    span = Math.min(span, maxSpan);

    GridBagConstraints gc = new GridBagConstraints();
    gc.gridx = startColumn - 1;
    gc.gridy = mCurrentRow;
    gc.gridwidth = span;
    return gc;
  }

  private boolean isGrowingColumn(final int columnIndex, final int colSpan) {
    int startColumn = Math.max(1, Math.min(columnIndex, mColumnCount));
    int endColumn = Math.min(mColumnCount, startColumn + Math.max(1, colSpan) - 1);

    for (int i = startColumn - 1; i < endColumn; i++) {
      if (mGrowingColumns[i]) {
        return true;
      }
    }

    return false;
  }
}
