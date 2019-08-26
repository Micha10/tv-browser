package tvbrowser;

import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import javax.swing.SwingUtilities;

import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.tray.SystemTray;

public final class UdpThread extends Thread {
  private DatagramSocket mSocket;
  private boolean mRun;
  private byte[] buf = new byte[8];
  private SystemTray mTray;
  private int mState;
  
  UdpThread() throws SocketException {
    mSocket = new DatagramSocket();
    mState = Frame.NORMAL;
  }
  
  void initMainFrame() {
    MainFrame.getInstance().addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        int state = MainFrame.getInstance().getExtendedState();

        if ((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
          mState = Frame.MAXIMIZED_BOTH;
        } else if ((state & Frame.ICONIFIED) != Frame.ICONIFIED) {
          mState = Frame.NORMAL;
        }
      }
    });
  }
  
  void setTray(final SystemTray tray) {
    mTray = tray;
  }
  
  @Override
  public synchronized void start() {
    setPriority(MIN_PRIORITY);
    mRun = true;
    super.start();
  }
  
  public void halt() {
    if(!mSocket.isClosed()) {
      mSocket.close();
    }
    
    mRun = false;
  }
  
  public void run() {
    while(mRun && !mSocket.isClosed()) {
      DatagramPacket packet = new DatagramPacket(buf, buf.length);
      
      try {
        mSocket.receive(packet);
        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        packet = new DatagramPacket(buf, buf.length, address, port);
        String received = new String(packet.getData(), 0, packet.getLength());
        
        if (address.equals(InetAddress.getByName("localhost")) && received.equals("open_tvb")) {
          if(mTray != null && mTray.isTrayUsed()) {
            mTray.show();
          }
          else if(((MainFrame.getInstance().getExtendedState() & Frame.ICONIFIED) == Frame.ICONIFIED)) {
            SwingUtilities.invokeLater(() -> {
              MainFrame.getInstance().showFromTray(mState);
            });
            
            MainFrame.getInstance().toFront();
          }
          else {
            MainFrame.getInstance().toFront();
          }
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    if(!mSocket.isClosed()) {
      mSocket.close();
    }
  }
  
  public DatagramSocket getSocket() {
    return mSocket;
  }
}
