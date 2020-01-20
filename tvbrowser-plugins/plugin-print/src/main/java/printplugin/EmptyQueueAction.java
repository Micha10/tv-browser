package printplugin;

import devplugin.ButtonAction;
import devplugin.PluginTreeNode;

import java.awt.event.ActionEvent;

import util.ui.Localizer;
import util.ui.TVBrowserIcons;

@SuppressWarnings("nls")
public class EmptyQueueAction extends ButtonAction {

  private static final long serialVersionUID = 7693165119674784176L;
  /** The localizer for this class. */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(EmptyQueueAction.class);

  public EmptyQueueAction() {
    super.setText(mLocalizer.msg("emptyQueue", "Clear printer queue"));
    super.setSmallIcon(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    PluginTreeNode root = PrintPlugin.getInstance().getRootNode();
    root.removeAllChildren();
    root.update();
  }
}