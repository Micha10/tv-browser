package printplugin.dlgs.printfromqueuedialog;

import devplugin.Program;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import util.ui.UiUtilities;

@SuppressWarnings("nls")
final class ProgramListCellRenderer extends DefaultListCellRenderer {

  private static final long serialVersionUID = -7118962368945181696L;

  private static final String FORMAT_STRING = "<html><body><div style=\"color:%s;font-family:%s;\"><span style=\"font-size:%d;\">%s - %s</span><br><span style=\"font-size:%d;\"><b>%s %s</b></span></div></body></html>";
  private static final String FORMAT_STRING_TOOLTIP = "<html><<body><p style=\"width:360px;\">%s - %s<br><b>%s %s</b><br>%s</p></body></html>";

  private final JLabel mIcon;
  private final JEditorPane mText;
  private final JCheckBox mCheckBox;
  private final JPanel mPanel;

  public ProgramListCellRenderer() {
    mIcon = new JLabel();
    mIcon.setOpaque(false);

    mText = new JEditorPane("text/html", "");
    mText.setBackground(null);
    mText.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
    mText.setEditable(false);
    mText.setFocusable(false);
    mText.setOpaque(false);

    mCheckBox = new JCheckBox();
    mCheckBox.setOpaque(false);

    mPanel = new JPanel(new BorderLayout(8, 0));
    mPanel.add(mIcon, BorderLayout.LINE_START);
    mPanel.add(mText, BorderLayout.CENTER);
    mPanel.add(mCheckBox, BorderLayout.LINE_END);
  }

  @Override
  public Component getListCellRendererComponent(final JList<? extends Object> list, final Object value, final int index,
      final boolean isSelected, final boolean cellHasFocus) {
    final JLabel component = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
        cellHasFocus);
    if (value instanceof Program) {

      final Program program = (Program) value;

      mIcon.setIcon(UiUtilities.createChannelIcon(program.getChannel().getIcon()));

      mText.setFont(component.getFont());
      mText.setForeground(component.getForeground());
      mText.setText(getFormatString(component, program));

      mCheckBox.setSelected(isSelected);

      mPanel.setBackground(component.getBackground());
      mPanel.setBorder(new CompoundBorder(component.getBorder(), new EmptyBorder(0, 8, 0, 8)));
      mPanel.setToolTipText(getToolTipText(program));

      return mPanel;
    }
    return component;
  }

  @SuppressWarnings("boxing")
  private static String getFormatString(final JComponent component, final Program program) {
    final Font font = component.getFont();
    final String color = UiUtilities.getHTMLColorCode(component.getForeground());
    final String fontName = font.getName();
    final int fontSize = font.getSize();
    return String.format(Locale.getDefault(),
        FORMAT_STRING,
        color,
        fontName,
        fontSize,
        program.getDateString(),
        program.getChannel().getName(),
        fontSize,
        program.getTimeString(),
        program.getTitle());
  }

  private static String getToolTipText(final Program program) {
    return String.format(Locale.getDefault(),
        FORMAT_STRING_TOOLTIP,
        program.getDateString(),
        program.getChannel().getName(),
        program.getTimeString(),
        program.getTitle(),
        getDescription(program));
  }

  private static String getDescription(final Program program) {
    if (program == null) {
      return "";
    }
    String description = program.getDescription();
    if (description == null || description.trim().length() == 0) {
      description = program.getShortInfo();
    }
    if (description == null || description.trim().length() == 0) {
      return "";
    }
    description = description.replace("(Text unter CC BY 2.0 von omdb.org)", "").trim();
    if (description.length() > 400) {
      description = description.substring(0, 396).trim().concat("...");
    }
    return description;
  }
}