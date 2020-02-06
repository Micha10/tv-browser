package printplugin.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

import printplugin.PrintPlugin;

import util.ui.Localizer;

@SuppressWarnings("nls")
public abstract class BaseAction extends AbstractAction {

  private static final long serialVersionUID = -1384470120938475405L;

  public static final String CANCEL = "cancel";
  public static final String CLOSE = "close";
  public static final String OK = "ok";
  public static final String SELECT = "select";
  public static final String SETTINGS = "settings";

  public static class Builder {

    private final BaseAction mBaseAction;

    Builder(final String command, final ActionListener actionListener) {
      mBaseAction = new BaseAction() {

        private static final long serialVersionUID = 8099674887905602838L;

        @Override
        public void actionPerformed(final ActionEvent e) {
          actionListener.actionPerformed(e);
        }
      };
      mBaseAction.putValue(Action.ACTION_COMMAND_KEY, command);
    }

    public Builder actionMap(final ActionMap actionMap) {
      actionMap.put(mBaseAction.getValue(Action.ACTION_COMMAND_KEY), mBaseAction);
      return this;
    }

    public Builder bindKeys(final InputMap inputMap, final int modifiers, final int... keyEvents) {
      for (final int keyEvent : keyEvents) {
        inputMap.put(KeyStroke.getKeyStroke(keyEvent, modifiers), mBaseAction.getValue(Action.ACTION_COMMAND_KEY));
      }
      return this;
    }

    public BaseAction build() {
      return mBaseAction;
    }

    public Builder description(final String text) {
      mBaseAction.putValue(Action.LONG_DESCRIPTION, text);
      return this;
    }

    public Builder enabled(final boolean enabled) {
      mBaseAction.setEnabled(enabled);
      return this;
    }

    public Builder icon(final Icon icon) {
      mBaseAction.putValue(Action.SMALL_ICON, icon);
      return this;
    }

    public Builder largeIcon(final Icon icon) {
      mBaseAction.putValue(Action.LARGE_ICON_KEY, icon);
      mBaseAction.putValue(devplugin.Plugin.BIG_ICON, icon);
      return this;
    }

    public Builder put(final String key, final Object value) {
      mBaseAction.putValue(key, value);
      return this;
    }

    public Builder text(final String text) {
      mBaseAction.putValue(Action.NAME, text);
      return this;
    }

    public Builder tooltip(final String text) {
      mBaseAction.putValue(Action.SHORT_DESCRIPTION, text);
      return this;
    }
  }

  @Override
  public abstract void actionPerformed(final ActionEvent e);

  public static Builder builder(final String command, final ActionListener actionListener) {
    return new Builder(command, actionListener);
  }

  public static Builder cancel(final ActionListener actionListener) {
    return new Builder(CANCEL, actionListener).text(Localizer.getLocalization(Localizer.I18N_CANCEL));
  }

  public static Builder close(final ActionListener actionListener) {
    return new Builder(CLOSE, actionListener).text(Localizer.getLocalization(Localizer.I18N_CLOSE));
  }

  public static Builder ok(final ActionListener actionListener) {
    return new Builder(OK, actionListener).text(Localizer.getLocalization(Localizer.I18N_OK));
  }

  public static Builder select(final ActionListener actionListener) {
    return new Builder(SELECT, actionListener).text(Localizer.getLocalization(Localizer.I18N_SELECT));
  }

  public static Builder settings(final ActionListener actionListener) {
    return new Builder(SETTINGS, actionListener)
        .icon(PrintPlugin.instance().createImageIcon("categories", "preferences-system", 16))
        .largeIcon(PrintPlugin.instance().createImageIcon("categories", "preferences-system", 22))
        .text(Localizer.getLocalization(Localizer.I18N_SETTINGS))
        .tooltip(Localizer.getLocalization(Localizer.I18N_SETTINGS));
  }
}