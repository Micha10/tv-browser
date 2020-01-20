package printplugin.dlgs.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ItemEvent;
import java.awt.font.TextAttribute;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import javax.accessibility.Accessible;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import printplugin.util.Utils;

/**
 * An enhanced {@link util.ui.FontChooserPanel} that displays the available
 * fonts using their glyphs.
 * Non-printable fonts (i.e. symbol fonts) are visualized strike-through with
 * the default font.
 */
public final class FontChooserPanel extends util.ui.FontChooserPanel {

  private static final long serialVersionUID = 4011039724182411597L;

  // https://stackoverflow.com/questions/23470604/
  static {
    for (final Font font : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()) {
      font.getPSName();
    }
  }

  private boolean mDisplayFonts = true;

  public FontChooserPanel(final String title, final Font selectedFont) {
    this(title, selectedFont, false);
  }

  public FontChooserPanel(final String title, final Font selectedFont, final boolean showStyle) {
    super(title, null, showStyle);
    modifyFontComboBox();
    selectFont(selectedFont);
  }

  // https://stackoverflow.com/questions/5896282/
  @SuppressWarnings({"unchecked", "rawtypes"})
  private void modifyFontComboBox() {
    final JComboBox comboBox = Utils.findFirst(JComboBox.class, this);
    comboBox.setMaximumRowCount(16);
    comboBox.setPrototypeDisplayValue(getPrototypeValue(comboBox));
    final Accessible accessible = comboBox.getUI().getAccessibleChild(comboBox, 0);
    if (accessible instanceof javax.swing.plaf.basic.ComboPopup) {
      ((javax.swing.plaf.basic.ComboPopup) accessible).getList()
          .setPrototypeCellValue(comboBox.getPrototypeDisplayValue());
    }
    comboBox.setRenderer(new DefaultListCellRenderer() {

      private static final long serialVersionUID = 1186525746730292882L;

      @Override
      public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
          final boolean isSelected,
          final boolean cellHasFocus) {
        final JLabel listCellRendererComponent = (JLabel) super.getListCellRendererComponent(list, value, index,
            isSelected, cellHasFocus);
        if (mDisplayFonts) {
          listCellRendererComponent.setFont(decodeFont(listCellRendererComponent, String.valueOf(value)));
        }
        return listCellRendererComponent;
      }
    });
    comboBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        if (mDisplayFonts) {
          comboBox.setFont(decodeFont(comboBox, String.valueOf(comboBox.getSelectedItem())));
        }
      }
    });
    final Dimension dimension = comboBox.getPreferredSize();
    dimension.width += 120;
    comboBox.setPreferredSize(dimension);
    comboBox.setMinimumSize(dimension);
  }

  private static String getPrototypeValue(final JComboBox<?> comboBox) {
    final char[] buffer = new char[35];
    Arrays.fill(buffer, 'M');
    String result = new String(buffer);
    int length = 0;
    for (int i = 0; i < comboBox.getModel().getSize(); i++) {
      final String value = String.valueOf(comboBox.getModel().getElementAt(i));
      if (value.length() > length) {
        result = value;
      }
    }
    return result;
  }

  @SuppressWarnings({"nls", "boxing", "unchecked", "rawtypes"})
  private static Font decodeFont(final Component component, final String fontName) {
    Font font = Font.decode(
        String.format(Locale.US, "%s-PLAIN-%d", fontName, component.getFont().getSize()));
    if (font.canDisplayUpTo(fontName) != -1) {
      final Font componentFont = component.getFont();
      final Map attributes = componentFont.getAttributes();
      attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
      font = componentFont.deriveFont(attributes);
    }
    return font;
  }

  public boolean isDisplayFonts() {
    return mDisplayFonts;
  }

  public void setDisplayFonts(final boolean displayFonts) {
    mDisplayFonts = displayFonts;
  }
}