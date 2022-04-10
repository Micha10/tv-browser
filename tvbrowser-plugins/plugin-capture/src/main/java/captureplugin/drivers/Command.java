package captureplugin.drivers;

public final class Command {
  private int mActionId;
  private String mActionName;
  
  public Command(int actionId, String actionName) {
    mActionId = actionId;
    mActionName = actionName;
  }
  
  public int getActionId() {
    return mActionId;
  }
  
  public String getActionName() {
    return mActionName;
  }
}
