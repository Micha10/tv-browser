package util.ui.textcomponentpopup;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;

/**
 * This {@link EventQueue} is based on the implementation of Santhosh.
 * <p>
 * For details look here: {@link <a href="https://web.archive.org/web/20060504030843/http://jroller.com/page/santhosh?entry=context_menu_for_textcomponents">http://jroller.com/page/santhosh?entry=context_menu_for_textcomponents</a>}
 * 
 * @author Santhosh Kumar T - santhosh@in.fiorano.com
 */
public class TextComponentPopupEventQueue extends EventQueue {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(TextComponentPopupEventQueue.class);

  protected void dispatchEvent(AWTEvent event) {
    // stop auto scrolling on any mouse event
    try {
      if (event instanceof MouseEvent) {
        MouseEvent me = (MouseEvent) event;
        if (me.getButton() != MouseEvent.NOBUTTON && me.getID() == MouseEvent.MOUSE_CLICKED) {
          if (MainFrame.getInstance().getProgramTableScrollPane().getProgramTable().stopAutoScroll()) {
            return;
          }
        }
      }
    } catch (Exception e1) {
    }
    
    try {
      super.dispatchEvent(event);
    }catch(Throwable e) {
      return;}

    // interested only in mouseevents
    if (!(event instanceof MouseEvent)) {
      return;
    }

    MouseEvent me = (MouseEvent) event;
    
    // interested only in popuptriggers
    if (!me.isPopupTrigger() || me.getComponent() == null) {
      return;
    }

    // me.getComponent(...) returns the heavy weight component on which event
    // occured
    Component comp = SwingUtilities.getDeepestComponentAt(me.getComponent(), me.getX(), me.getY());

    // interested only in textcomponents
    if (!(comp instanceof JTextComponent)) {
      return;
    }

    // no popup shown by user code
    if (MenuSelectionManager.defaultManager().getSelectedPath().length > 0) {
      return;
    }

    // create popup menu and show
    JTextComponent tc = (JTextComponent) comp;
    JPopupMenu menu = new JPopupMenu();
    
    Point pt = SwingUtilities.convertPoint(me.getComponent(), me.getPoint(), tc);
    addStandardContextMenu(tc, menu);
    menu.show(tc, pt.x, pt.y);
  }

  public static void addStandardContextMenu(JTextComponent tc, JPopupMenu menu) {
    addStandardContextMenu(tc, menu, null);
  }
  
  public static void addStandardContextMenu(JTextComponent tc, JPopupMenu menu, String link) {
    if (menu.getSubElements().length > 0) {
      menu.addSeparator();
    }
    
    if(link != null) {
      menu.add(new CopyLinkAction(link));
      menu.addSeparator();
    }
    
    menu.add(new CutAction(tc));
    menu.add(new CopyAction(tc));
    menu.add(new PasteAction(tc));
    menu.add(new DeleteAction(tc));
    menu.addSeparator();
    menu.add(new SelectAllAction(tc));
  }
  
  private static class CopyLinkAction extends AbstractAction {
    private static final long serialVersionUID = -2488488417295967890L;
    private String mLink;
    
    public CopyLinkAction(String link) {
      super(mLocalizer.msg("copyLink", "Copy link address"), TVBrowserIcons.copy(TVBrowserIcons.SIZE_SMALL));
      mLink = link;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

      clipboard.setContents(new StringSelection(mLink), null);
    }
  }

  private static class CutAction extends AbstractAction {
    private static final long serialVersionUID = 566170125220848189L;
    JTextComponent comp;

    public CutAction(JTextComponent comp) {
      super(mLocalizer.msg("cut", "Cut"));
      this.comp = comp;
    }
    
    public void actionPerformed(ActionEvent e) {
      comp.cut();
    }

    public boolean isEnabled() {
      return comp.isEditable() && comp.isEnabled() && comp.getSelectedText() != null;
    }
  }

  private static class PasteAction extends AbstractAction {
    private static final long serialVersionUID = 9221100128433030987L;
    JTextComponent comp;

    public PasteAction(JTextComponent comp) {
      super(mLocalizer.msg("paste", "Paste"), IconLoader.getInstance()
          .getIconFromTheme("actions", "edit-paste"));
      this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {
      comp.paste();
    }

    public boolean isEnabled() {
      if (comp.isEditable() && comp.isEnabled()) {
        Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
        return contents.isDataFlavorSupported(DataFlavor.stringFlavor);
      } else {
        return false;
      }
    }
  }

  private static class DeleteAction extends AbstractAction {
    private static final long serialVersionUID = 9008236494552230397L;
    JTextComponent comp;

    public DeleteAction(JTextComponent comp) {
      super(Localizer.getLocalization(Localizer.I18N_DELETE), TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
      this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {
      comp.replaceSelection(null);
    }

    public boolean isEnabled() {
      return comp.isEditable() && comp.isEnabled() && comp.getSelectedText() != null;
    }
  }

  private static class CopyAction extends AbstractAction {
    private static final long serialVersionUID = -8850316291381010884L;
    JTextComponent comp;

    public CopyAction(JTextComponent comp) {
      super(mLocalizer.msg("copy", "Copy"), TVBrowserIcons.copy(TVBrowserIcons.SIZE_SMALL));
      this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {
      comp.copy();
    }

    public boolean isEnabled() {
      return comp.isEnabled() && comp.getSelectedText() != null && !(comp instanceof JPasswordField);
    }
  }

  private static class SelectAllAction extends AbstractAction {
    private static final long serialVersionUID = 1542606356598380639L;
    JTextComponent comp;

    public SelectAllAction(JTextComponent comp) {
      super(mLocalizer.msg("selectAll", "Select All"));
      this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {
      comp.selectAll();
    }

    public boolean isEnabled() {
      return comp.isEnabled() && comp.getText().length() > 0;
    }
  }
}