package util.ui.customizableitems;

import javax.swing.event.ChangeListener;

/**
 * A filter interface that is to be used
 * to support filtering in SelectableItemLists.
 * 
 * @author René Mach
 * @since 2.7
 */
public interface ItemFilter {
  /**
   * Gets if the filter accepts the given Object.
   * <p>
   * @param o The Object to check.
   * @return <code>True</code> if the filter accepts the Object,
   * <code>false</code> otherwise.
   */
  public boolean accept(Object o);
  
  /**
   * @param listener The change listener to set for the component that should trigger updates.
   * @since 4.2.2
   */
  default public void setChangeListener(ChangeListener listener) {}
}
