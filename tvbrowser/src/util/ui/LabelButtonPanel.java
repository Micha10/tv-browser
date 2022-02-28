package util.ui;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

public class LabelButtonPanel extends JPanel {
  private final JButton mButton; 
  
  public LabelButtonPanel(final String labelText, final Color labelForeground, final String buttonText, final Runnable buttonAction, final boolean buttonVisible) {
    final JLabel label = new JLabel(labelText);
   
    if(labelForeground != null) {
      label.setForeground(labelForeground);
    }
   
    mButton = new JButton(buttonText);
    mButton.addActionListener(e -> {
      buttonAction.run();
    });
   
    setLayout(new FormLayout("default:grow,3dlu,default","default"));
    setBorder(Borders.createEmptyBorder("5dlu,5dlu,0dlu,5dlu"));
    setVisible(buttonVisible);
    add(label, CC.xy(1, 1));
    add(mButton, CC.xy(3, 1));
  }
  
  public void setButtonText(final String text) {
    mButton.setText(text);
  }
}
