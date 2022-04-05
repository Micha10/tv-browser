package util.ui;

import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JSeparator;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;

import util.i18n.Localizer;

public class OkayCancelDialog extends JDialog implements WindowClosingIf {
  private JButton mOk;
  private boolean mOkWasPressed;
  
  public OkayCancelDialog(final Window parent, final String title, final JComponent message, final String help, final boolean okayEnabled) {
    super(parent, ModalityType.DOCUMENT_MODAL);
    setTitle(title);
    
    mOkWasPressed = false;
    
    mOk = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    mOk.setEnabled(okayEnabled);
    mOk.addActionListener(e -> {
      mOkWasPressed = true;
      close();
    });
    
    JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancel.addActionListener(e -> {
      close();
    });
    
    EnhancedPanelBuilder pb = new EnhancedPanelBuilder("100dlu:grow,default,5dlu,default");
    pb.border(Borders.DIALOG);
    
    if(help != null && !help.isBlank()) {
      pb.addRow(false);
      pb.addLabel("<html>"+help+"</html>", CC.xyw(1, pb.getRowCount(), 4));
    }
    
    pb.addRow("fill:10dlu:grow");
    pb.add(message, CC.xyw(1, pb.getRowCount(), 4));
    
    pb.addRow("default");
    pb.add(new JSeparator(JSeparator.HORIZONTAL), CC.xyw(1, pb.getRowCount(), 4));
    pb.addRow();
    pb.add(cancel, CC.xy(2, pb.getRowCount()));
    pb.add(mOk, CC.xy(4, pb.getRowCount()));
    
    setContentPane(pb.getPanel());
    getRootPane().setDefaultButton(mOk);
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
  }
  
  public void setOkayEnabled(final boolean enabled) {
    mOk.setEnabled(enabled);
  }
  
  public boolean getOkWasPressed() {
    return mOkWasPressed;
  }

  @Override
  public void close() {
    dispose();
  }
}
