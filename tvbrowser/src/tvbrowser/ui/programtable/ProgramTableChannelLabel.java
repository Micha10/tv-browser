package tvbrowser.ui.programtable;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import devplugin.Channel;
import tvbrowser.core.Settings;
import tvbrowser.ui.mainframe.MainFrame;
import util.browserlauncher.Launch;
import util.ui.ChannelContextMenu;
import util.ui.ChannelLabel;
import util.ui.ImageUtilities;
import util.ui.ToolTipWithIcon;
import util.ui.UiUtilities;
import util.ui.persona.Persona;

public class ProgramTableChannelLabel extends ChannelLabel {

  private static Cursor linkCursor=new Cursor(Cursor.HAND_CURSOR);
  private static Font channelNameFont;

  private Channel mChannel;
  private boolean mIsRollover;
  
  public ProgramTableChannelLabel(Channel ch,KeyListener keyListener) {
    super(Settings.IconAndNames.SHOW_ICONS_IN_PROGRAM_TABLE.getBoolean(),Settings.IconAndNames.SHOW_NAMES_IN_PROGRAM_TABLE.getBoolean(),false,false,true,false,Settings.IconAndNames.SHOW_SORT_NUMBER_IN_PROGRAM_TABLE.getBoolean());
    mChannel = ch;
    
    addKeyListener(keyListener);
    updatePersona();
    
    setChannel(mChannel);
    setToolTipText("");
    
    // Check whether the font was set
    if (channelNameFont == null) {
      fontChanged();
    }

    // Avoid that a null-font is set
    // (Happens when the font from the config is null)
    if (channelNameFont != null) {
      setFont(channelNameFont);
    }
    
    mIsRollover = false;
    setOpaque(false);
    setHorizontalAlignment(SwingConstants.CENTER);
          
    setCursor(linkCursor);
    addMouseListener(new MouseAdapter(){
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showPopUp(e);
        } else if (e.getButton() == MouseEvent.BUTTON1){
          Launch.openURL(mChannel.getWebpage());
        }
        super.mouseReleased(e);
      }
      
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showPopUp(e);
        }
        super.mouseReleased(e);
      }
      
      public void mouseEntered(MouseEvent e) {
        mIsRollover = true;
        
        int r = (getForeground().getRed()   + getBackground().getRed())   >> 1;
        int g = (getForeground().getGreen() + getBackground().getGreen()) >> 1;
        int b = (getForeground().getBlue()  + getBackground().getBlue())  >> 1;
          
        e.getComponent().setForeground(new Color(r,g,b));
        
        if(Settings.ProgramTable.HIGHLIGHT_CHANNEL_COLUMN_BY_MOUSE.getBoolean()) {
          MainFrame.getInstance().getProgramTableScrollPane().highlightChannel(mChannel);
        }
      }
      
      public void mouseExited(MouseEvent e) {
        mIsRollover = false;
        
        e.getComponent().setForeground(UIManager.getColor("List.selectionForeground"));
        
        if(Settings.ProgramTable.HIGHLIGHT_CHANNEL_COLUMN_BY_MOUSE.getBoolean()) {
          MainFrame.getInstance().getProgramTableScrollPane().unHighlightChannel(mChannel);
        }
      }
    });
  }
 /* public void setChannel(Channel ch) {
    super.clearIconCache();
    super.setChannel(ch);
  }*/
    
  private void showPopUp(MouseEvent e) {
    new ChannelContextMenu(e,mChannel,this);
  }

  public static void fontChanged() {
    boolean useDefaults = Settings.Fonts.USE_DEFAULT.getBoolean();
    if (useDefaults) {
      channelNameFont = Settings.Fonts.CHANNEL_NAME.getDefault();
    } else {
      channelNameFont = Settings.Fonts.CHANNEL_NAME.getFont();
    }
  }
  
  @Override
  public JToolTip createToolTip() {
    // don't show tooltip, if disabled in settings
    if (!Settings.IconAndNames.SHOW_CHANNEL_TOOLTIP_IN_PROGRAM_TABLE.getBoolean()) {
      return new ToolTipWithIcon(null, null);
    }
    boolean showIcon = false;
    boolean showText = false;
    JToolTip tip;
        
    Icon channelIcon = mChannel.getJointChannel() != null ? ImageUtilities.createImageIcon(mChannel.getJointChannelIcon(),Color.WHITE,4): mChannel.getIcon();
    
    
    
    if (channelIcon != null && channelIcon instanceof ImageIcon) {
      Icon shownIcon = this.getIcon();
      if (shownIcon != null && (channelIcon.getIconHeight() > shownIcon.getIconHeight() || channelIcon.getIconWidth() > shownIcon.getIconWidth())) {
        showIcon = true;
      }
    }
    if (showIcon) {
      tip = new ToolTipWithIcon((ImageIcon) channelIcon);
//      tip.setMinimumSize(new Dimension(getIcon().getIconWidth() + 2,getIcon().getIconHeight() + 2));
    }
    else {
      tip = new ToolTipWithIcon((ImageIcon)null);
    }
    tip.setBackground(Color.WHITE);
    tip.setComponent(this);
    String text = null;
    if (showText) {
      text = mChannel.getName();
    }
    tip.setTipText(text);
    return tip;
  }

  @Override
  public Point getToolTipLocation(MouseEvent event) {
    FontMetrics metrics = this.getFontMetrics(this.getFont());
    int stringWidth = SwingUtilities.computeStringWidth(metrics, this.getText());
    int x = 0;
    Icon icon = mChannel.getJointChannel() != null ? mChannel.getJointChannelIcon() : this.getIcon();
    Icon channelIcon = mChannel.getJointChannel() != null ? mChannel.getJointChannelIcon() : mChannel.getIcon();

    int iconWidth = icon.getIconWidth();
    if (channelIcon != null) {
      iconWidth = channelIcon.getIconWidth();
    }

    int iconHeight = icon.getIconHeight();
    if (channelIcon != null) {
      iconHeight = channelIcon.getIconHeight();
    }

    if (icon != null) {
      x = (this.getWidth() - stringWidth - this.getIconTextGap() - icon.getIconWidth()) / 2;
      if (x < 0) {
        x = 0;
      }
      x += (icon.getIconWidth() - iconWidth) / 2;
    }
    int y = (this.getHeight() - iconHeight - 2) / 2;
    return new Point(x, y);
  }
  
  /**
   * Updates Persona after change.
   */
  public void updatePersona() {
    if(Persona.getInstance().getHeaderImage() != null && Persona.getInstance().getTextColor() != null) {
      setForeground(Persona.getInstance().getTextColor());
    }
    else {
      setForeground(UIManager.getColor("List.selectionForeground"));
    }
  }
  
  protected void paintComponent(Graphics g) {
    if(mChannel.isUsingUserBackgroundColor() || ( Persona.getInstance().getHeaderImage() != null && Persona.getInstance().getTextColor() != null && Persona.getInstance().getShadowColor() != null)) {
      try {
      Color c = mChannel.isUsingUserBackgroundColor() ? mChannel.getUserBackgroundColor() : Persona.getInstance().getAccentColor();
      
      if(!mChannel.isUsingUserBackgroundColor()) {
        c = Persona.testPersonaForegroundAgainst(c);
      }
      
      Color textColor = mChannel.isUsingUserBackgroundColor() ? getForeground() : Persona.getInstance().getTextColor();
      
      if(mIsRollover) {
        Color test = UIManager.getColor("List.selectionBackground");
        
        if(UiUtilities.isBrightnessSimilarForColors(test, textColor, 84)) {
          textColor = UIManager.getColor("List.selectionForeground");
        }
      }
            
      g.setColor(c);
      g.fillRect(0,0,getWidth(),getHeight());
      
      int iconX = (getWidth()/2);
      int iconY = (getHeight()/2);
      int iconWidth = 0;
      int iconTextGap = 0;
      
      FontMetrics metrics = g.getFontMetrics(getFont());
      String text = getText();
      int textWidth = metrics.stringWidth(text);
      int baseLine =  getHeight()/2+ metrics.getMaxDescent()+1;
      
      Icon icon = getIcon();
      
      int iconTextLength = iconWidth + getIconTextGap() + textWidth;
      
      if(icon != null) {
        iconWidth = icon.getIconWidth();
        iconTextGap = getIconTextGap();
        
        iconTextLength += iconWidth;
      }
      
      while(iconTextLength > getSize().getWidth()) {
        text = text.substring(0, text.length()-4) + "...";
        
        iconTextLength = metrics.stringWidth(text) + iconWidth + getIconTextGap();
      }
      
      if(icon != null) {
        iconX -= (iconTextLength/2);
        iconY -= (icon.getIconHeight()/2);
        
        icon.paintIcon(this,g,iconX,iconY);
      }
      else {
        iconX -= textWidth/2;
      }
      
      if(!mChannel.isUsingUserBackgroundColor() && !Persona.getInstance().getShadowColor().equals(textColor) && Persona.getInstance().getTextColor().equals(textColor)) {
        g.setColor(Persona.getInstance().getShadowColor());
        
        g.drawString(text,iconX+iconWidth+iconTextGap+1,baseLine+1);
      }
      
      g.setColor(textColor);
      g.drawString(text,iconX+iconWidth+iconTextGap,baseLine);
      }catch(Throwable t) {
        t.printStackTrace();
        super.paintComponent(g);
      }
    }
    else {
      super.paintComponent(g);
    }
  }
}