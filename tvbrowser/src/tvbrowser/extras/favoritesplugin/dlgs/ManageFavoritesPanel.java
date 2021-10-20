/*
 * TV-Browser
 * Copyright (C) 2012 TV-Browser team (dev@tvbrowser.org)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.extras.favoritesplugin.dlgs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.jgoodies.forms.factories.Borders;

import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginCommunication;
import devplugin.PluginManager;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.ProgressMonitorExtended;
import devplugin.SettingsItem;
import tvbrowser.core.ChannelList;
import tvbrowser.core.filters.FilterComponentList;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.UserFilter;
import tvbrowser.core.filters.filtercomponents.ProgramInfoFilterComponent;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.extras.common.ReminderConfiguration;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.favoritesplugin.core.AdvancedFavorite;
import tvbrowser.extras.favoritesplugin.core.Exclusion;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.favoritesplugin.core.FilterFavorite;
import tvbrowser.extras.favoritesplugin.core.TitleFavorite;
import tvbrowser.extras.favoritesplugin.core.TopicFavorite;
import tvbrowser.extras.favoritesplugin.wizards.TypeWizardStep;
import tvbrowser.extras.favoritesplugin.wizards.WizardHandler;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.ui.mainframe.MainFrame;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.i18n.Localizer;
import util.program.ProgramUtilities;
import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import util.ui.DragAndDropMouseListener;
import util.ui.ExtensionFileFilter;
import util.ui.FilterableProgramListPanel;
import util.ui.ListDragAndDropHandler;
import util.ui.ListDropAction;
import util.ui.ProgramList;
import util.ui.SearchFormSettings;
import util.ui.SendToPluginDialog;
import util.ui.TVBrowserIcons;
import util.ui.TabListenerPanel;
import util.ui.UiUtilities;
import util.ui.persona.Persona;
import util.ui.persona.PersonaListener;

/**
 * A panel for managing of the TV-Browser favorites.
 * 
 * @author René Mach
 */
public class ManageFavoritesPanel extends TabListenerPanel implements ListDropAction<Favorite>, TreeSelectionListener, PersonaListener {
  public static final int FILTER_START_LAST_TYPE = -1;
  
  private static final int MAX_SHOWN_PROGRAMS = 6000;
  private static final Localizer LOCALIZER = ManageFavoritesDialog.LOCALIZER;
  private DefaultListModel<Favorite> mFavoritesListModel;
  private JList<Favorite> mFavoritesList;
  private FavoriteTree mFavoriteTree;
  private ProgramList mProgramList;
  private JSplitPane mSplitPane;
  private JButton mNewBt, mEditBt, mDeleteBt, mUpBt, mDownBt, mSortAlphaBt, mSortCountBt, mImportBt, mSendBt, mUpdateBt, mImportApp;
  private JButton mCloseBt;

  private boolean mShowNew = false;
  private JCheckBox mBlackListChb;
  private Thread mSplitDividerThread;
  private long mLastDividerChange;
  
  private FilterableProgramListPanel mProgramListPanel;
  
  private JButton mScrollToPreviousDay, mScrollToNextDay, mScrollToFirstNotExpired;
  
  public ManageFavoritesPanel(Favorite[] favoriteArr,
      int splitPanePosition, boolean showNew, Favorite initialSelection, boolean border, boolean isMainPanel) {
    init(favoriteArr, splitPanePosition, showNew, initialSelection,border,isMainPanel);
  }
  
  private void init(Favorite[] favoriteArr, int splitPanePosition, boolean showNew, Favorite initialSelection, boolean border, boolean isMainPanel) {try {
    mShowNew = showNew;
    mLastDividerChange = System.currentTimeMillis();

    String msg;
    Icon icon;
    
    setLayout(new BorderLayout(5, 5));
    
    if(border) {
      setBorder(Borders.DLU4);
    }
    
    setOpaque(false);

    JToolBar toolbarPn = new JToolBar() {
	  protected void paintComponent(Graphics g) {
	    if(!UiUtilities.isGTKLookAndFeel() || Persona.getInstance().getHeaderImage() == null) {
	      super.paintComponent(g);
	    }
	  }
    };
    toolbarPn.setFloatable(false);
    toolbarPn.setOpaque(false);
    toolbarPn.setBorder(BorderFactory.createEmptyBorder());
    
    if(mShowNew) {
      JEditorPane info =  UiUtilities.createHtmlHelpTextArea(FavoritesPlugin.LOCALIZER.msg("newPrograms.description","After updating TV listings, programs matching your favorites were found.\nSelect a favorite to view the new programs."));
      
      JPanel northPanel = new JPanel(new BorderLayout(0,5));
      northPanel.add(info, BorderLayout.NORTH);
      northPanel.add(toolbarPn, BorderLayout.SOUTH);
      
      add(northPanel, BorderLayout.NORTH);
    }
    else {
      add(toolbarPn, BorderLayout.NORTH);
    }

    if(!mShowNew) {
      if(favoriteArr == null) {
        JButton newFolder = UiUtilities.createToolBarButton(LOCALIZER.msg("newFolder", "New folder"),
            IconLoader.getInstance().getIconFromTheme("actions", "folder-new", 22));
        newFolder.setOpaque(false);
        newFolder.addActionListener(e -> {
          TreePath path = mFavoriteTree.getSelectionPath();
          
          if(path != null) {
            FavoritesPlugin.getInstance().newFolder((FavoriteNode)path.getLastPathComponent());
          } else {
            FavoritesPlugin.getInstance().newFolder(mFavoriteTree.getRoot());
          }
        });

        toolbarPn.add(newFolder);
      }

      addToolbarSeperator(toolbarPn);

      msg = LOCALIZER.ellipsisMsg("new", "Create a new favorite");
      icon = TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_LARGE);
      mNewBt = UiUtilities.createToolBarButton(msg, icon);
      mNewBt.setOpaque(false);
      mNewBt.addActionListener(e -> {
        TreePath path = mFavoriteTree.getSelectionPath();

        if(path == null) {
          newFavorite(mFavoriteTree.getRoot());
        } else {
          FavoriteNode node = (FavoriteNode)path.getLastPathComponent();

          newFavorite(node.isDirectoryNode() ? node : (FavoriteNode)node.getParent());
        }
      });

      toolbarPn.add(mNewBt);
    }

    msg = LOCALIZER.ellipsisMsg("edit", "Edit the selected favorite");
    icon = TVBrowserIcons.edit(TVBrowserIcons.SIZE_LARGE);
    mEditBt = UiUtilities.createToolBarButton(msg, icon);
    mEditBt.setOpaque(false);
    mEditBt.addActionListener(e -> {
      if(mShowNew) {
        editSelectedFavorite();
      } else {
        FavoriteNode node = (FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent();

        if(node.isDirectoryNode()) {
          mFavoriteTree.renameFolder(node);
        } else {
          editSelectedFavorite();
        }
      }
    });
    toolbarPn.add(mEditBt);

    msg = LOCALIZER.ellipsisMsg("delete", "Delete selected favorite");
    icon = TVBrowserIcons.delete(TVBrowserIcons.SIZE_LARGE);
    mDeleteBt = UiUtilities.createToolBarButton(msg, icon);
    mDeleteBt.setOpaque(false);
    mDeleteBt.addActionListener(e -> {
      if(mShowNew) {
        deleteSelectedFavorite();
      } else {
        FavoriteNode node = (FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent();

        if(node.isDirectoryNode()) {
          mFavoriteTree.delete(node);
          
          FavoritesPlugin.getInstance().updateRootNode(true);
        } else {
          deleteSelectedFavorite();
        }
      }
    });
    toolbarPn.add(mDeleteBt);

    msg = LOCALIZER.ellipsisMsg("update", "Search for all Favorites again");
    icon = TVBrowserIcons.update(TVBrowserIcons.SIZE_LARGE);
    mUpdateBt = UiUtilities.createToolBarButton(msg, icon);
    mUpdateBt.setOpaque(false);
    mUpdateBt.addActionListener(e -> {
      ProgressMonitorExtended m = PluginManagerImpl.getInstance().createProgressMonitor();
      m.setValue(0);
      
      final Favorite[] favs = mFavoriteTree.getModel().getFavoriteArr();
      
      m.setMaximum(favs.length);
      m.setMessage(LOCALIZER.ellipsisMsg("update", "Search for all Favorites again"));
      m.setVisible(true);
    
      for(int i= 0; i < favs.length; i++) {
        m.setValue(i);
        if(favs[i] != null) {
          try {
            favs[i].updatePrograms();
          } catch (TvBrowserException e1) {}
        }
      }
      
      FavoritesPlugin.getInstance().saveFavorites();
      m.setMessage("");
      m.setVisible(false);
    });
    
    if(!mShowNew) {
      toolbarPn.add(mUpdateBt);
    }
    
    msg = LOCALIZER.msg("up", "Move the selected favorite up");
    icon = TVBrowserIcons.up(TVBrowserIcons.SIZE_LARGE);
    mUpBt = UiUtilities.createToolBarButton(msg, icon);
    mUpBt.setOpaque(false);
    mUpBt.addActionListener(e -> {
      mFavoriteTree.moveSelectedFavorite(-1);
    });

    if(!mShowNew) {
      addToolbarSeperator(toolbarPn);
      toolbarPn.add(mUpBt);
    }

    msg = LOCALIZER.msg("down", "Move the selected favorite down");
    icon = TVBrowserIcons.down(TVBrowserIcons.SIZE_LARGE);
    mDownBt = UiUtilities.createToolBarButton(msg, icon);
    mDownBt.setOpaque(false);
    mDownBt.addActionListener(e -> {
      mFavoriteTree.moveSelectedFavorite(1);
    });

    if(!mShowNew) {
      toolbarPn.add(mDownBt);
    }

    msg = LOCALIZER.msg("sort", "Sort favorites alphabetically");
    icon = FavoritesPlugin.getIconFromTheme("actions", "sort-list", 22);
    final String titleAlpha = msg;
    mSortAlphaBt = UiUtilities.createToolBarButton(msg, icon);
    mSortAlphaBt.setOpaque(false);
    mSortAlphaBt.addActionListener(e -> {
      sortFavorites(FavoriteNodeComparator.getInstance(), titleAlpha);
    });

    msg = LOCALIZER.msg("sortCount", "Sort favorites by number of programs");
    icon = FavoritesPlugin.getIconFromTheme("actions", "sort-list-numerical", 22);
    final String titleCount = msg;
    mSortCountBt = UiUtilities.createToolBarButton(msg, icon);
    mSortCountBt.setOpaque(false);
    mSortCountBt.addActionListener(e -> {
      sortFavorites(FavoriteNodeCountComparator.getInstance(), titleCount);
    });

    if(!mShowNew) {
      toolbarPn.add(mSortAlphaBt);
      toolbarPn.add(mSortCountBt);
    }

    msg = LOCALIZER.msg("send", "Send Programs to another Plugin");
    icon = TVBrowserIcons.copy(TVBrowserIcons.SIZE_LARGE);
    mSendBt = UiUtilities.createToolBarButton(msg, icon);
    mSendBt.setOpaque(false);
    mSendBt.addActionListener(e -> {
       showSendDialog();
    });

    addToolbarSeperator(toolbarPn);
    toolbarPn.add(mSendBt);

    msg = LOCALIZER.msg("import", "Import favorites from TVgenial");
    icon = FavoritesPlugin.getIconFromTheme("actions", "document-open", 22);
    mImportBt = UiUtilities.createToolBarButton(msg, icon);
    mImportBt.setOpaque(false);
    mImportBt.addActionListener(e -> {
      importFavorites();
    });
    
    msg = LOCALIZER.msg("androidSync.import", "Import favorites with AndroidSync");
    icon = FavoritesPlugin.getIconFromTheme("actions", "document-open", 22);
    mImportApp = UiUtilities.createToolBarButton(msg, icon);
    mImportApp.setOpaque(false);
    mImportApp.addActionListener(e -> {
      importFavoritesAndroid();
    });
    
    updateAndroidSyncImportButton();
    
    if(!mShowNew) {
      toolbarPn.add(mImportBt);
      toolbarPn.add(mImportApp);
    }

    msg = LOCALIZER.msg("settings","Open settings");
    icon = TVBrowserIcons.preferences(TVBrowserIcons.SIZE_LARGE);
    
    if(ManageFavoritesDialog.getInstance() != null) {
      JButton settings = UiUtilities.createToolBarButton(msg, icon);
  
      settings.addActionListener(e -> {
          if(ManageFavoritesDialog.getInstance() != null && ManageFavoritesDialog.getInstance().isVisible()) {
            ManageFavoritesDialog.getInstance().close();
          }

          MainFrame.getInstance().showSettingsDialog(SettingsItem.FAVORITE);
        });
  
      addToolbarSeperator(toolbarPn);
      toolbarPn.add(settings);
    }
    
    toolbarPn.add(Box.createGlue());
    
    mScrollToFirstNotExpired = UiUtilities.createToolBarButton(LOCALIZER.msg("scrollToFirstNotExpired", "Scroll to first not expired program."),TVBrowserIcons.scrollToNow(TVBrowserIcons.SIZE_LARGE));
    mScrollToFirstNotExpired.setOpaque(false);
    toolbarPn.add(mScrollToFirstNotExpired);
    mScrollToFirstNotExpired.addActionListener(e -> {
      scrollToFirstNotExpiredIndex(false);
    });
    
    toolbarPn.add(Box.createRigidArea(new Dimension(15,0)));
    
    mScrollToPreviousDay = UiUtilities.createToolBarButton(ProgramList.getPreviousActionTooltip(),TVBrowserIcons.left(TVBrowserIcons.SIZE_LARGE));
    mScrollToPreviousDay.setOpaque(false);
    toolbarPn.add(mScrollToPreviousDay);
    mScrollToPreviousDay.addActionListener(e -> {
      mProgramList.scrollToPreviousDayIfAvailable();
    });

    mScrollToNextDay = UiUtilities.createToolBarButton(ProgramList.getNextActionTooltip(),TVBrowserIcons.right(TVBrowserIcons.SIZE_LARGE));
    mScrollToNextDay.setOpaque(false);
    toolbarPn.add(mScrollToNextDay);
    mScrollToNextDay.addActionListener(e -> {
      mProgramList.scrollToNextDayIfAvailable();
    });   
    
    mSplitPane = new JSplitPane();
    
    for(int i = 0; i < mSplitPane.getComponentCount(); i++) {
      (mSplitPane.getComponent(i)).setBackground(new Color(0,0,0,0));
    }
    
    mSplitPane.setBorder(BorderFactory.createEmptyBorder());
    mSplitPane.setDividerLocation(splitPanePosition);
    mSplitPane.setContinuousLayout(true);
    mSplitPane.setOpaque(false);
    
    if(isMainPanel) {
      mSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,new PropertyChangeListener() {
        @Override
        public synchronized void propertyChange(PropertyChangeEvent evt) {
          mLastDividerChange = System.currentTimeMillis();
          
          if(mSplitDividerThread == null || !mSplitDividerThread.isAlive()) {
            mSplitDividerThread = new Thread("FAVORITE SPLIT DIVIDER THREAD") {
              @Override
              public void run() {
                while(System.currentTimeMillis() - mLastDividerChange < 500) {
                  try {
                    sleep(500);
                  } catch (InterruptedException e) {
                    // ignore
                  }
                }
                FavoritesPlugin.getInstance().store();
              };
            };
            mSplitDividerThread.start();
          }
        }
      });
    }
    
    add(mSplitPane, BorderLayout.CENTER);

    JScrollPane scrollPane;

    if(favoriteArr != null) {
      mFavoritesListModel = new DefaultListModel<>();
      mFavoritesListModel.ensureCapacity(favoriteArr.length);
      for (Favorite element : favoriteArr) {
        if(element.getNewPrograms().length >= 0) {
          mFavoritesListModel.addElement(element);
        }
      }

      mFavoritesList = new JList<>(mFavoritesListModel);
      mFavoritesList.setCellRenderer(new FavoriteListCellRenderer());
      ListSelectionModel selModel = mFavoritesList.getSelectionModel();
      selModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      selModel.addListSelectionListener(evt -> {
        if(!evt.getValueIsAdjusting()) {
          favoriteSelectionChanged(true);
        }
      });

      if(!mShowNew) {
        ListDragAndDropHandler dnDHandler = new ListDragAndDropHandler(mFavoritesList,mFavoritesList,this);
        new DragAndDropMouseListener<Favorite>(mFavoritesList,mFavoritesList,this,dnDHandler);
      }

      mFavoritesList.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          if (e.isPopupTrigger()) {
            showFavoritesPopUp(e.getX(), e.getY());
          }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
          if (e.isPopupTrigger()) {
            showFavoritesPopUp(e.getX(), e.getY());
          }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
          if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
            editSelectedFavorite();
          }
        }
      });

      mFavoritesList.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          if (e.getKeyCode() == KeyEvent.VK_RIGHT && e.isControlDown()) {
            mProgramList.grabFocus();
            if (mProgramList.getSelectedIndex() == -1) {
              mProgramList.setSelectedIndex(0);
            }
          }
        }
      });

      scrollPane = new JScrollPane(mFavoritesList);
    }
    else {
      mFavoriteTree = new FavoriteTree();
      mFavoriteTree.addTreeSelectionListener(this);

      mFavoriteTree.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          if (e.getKeyCode() == KeyEvent.VK_RIGHT && e.isControlDown()) {
            mProgramList.grabFocus();
            if (mProgramList.getSelectedIndex() == -1) {
              mProgramList.setSelectedIndex(0);
            }
          }
        }
      });

      scrollPane = new JScrollPane(mFavoriteTree);

      mFavoritesList = null;
    }

    scrollPane.setBorder(null);
    scrollPane.setMinimumSize(new Dimension(200,100));
    mSplitPane.setLeftComponent(scrollPane);
    
    if(FavoritesPlugin.getInstance().getFilterStartType() == FILTER_START_LAST_TYPE) {
      mProgramListPanel = new FilterableProgramListPanel(true, new Program[0], true, FavoritesPlugin.getInstance().showDateSeparators(), new ProgramPanelSettings(new PluginPictureSettings(PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE), false, ProgramPanelSettings.X_AXIS),FavoritesPlugin.getInstance().getLastSelectedProgramFilter());
    }
    else {
      mProgramListPanel = new FilterableProgramListPanel(FilterableProgramListPanel.TYPE_NAME_AND_PROGRAM_FILTER, new Program[0], true, FavoritesPlugin.getInstance().showDateSeparators(), new ProgramPanelSettings(new PluginPictureSettings(PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE), false, ProgramPanelSettings.X_AXIS),FavoritesPlugin.getInstance().getFilterStartType());
    }
    
    mProgramListPanel.setBorder(Borders.DLU2);
    
    mProgramList = mProgramListPanel.getProgramList();
    setDefaultFocusOwner(mProgramList);
    mProgramList.addMouseAndKeyListeners(null);

    mProgramList.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          if (e.getKeyCode() == KeyEvent.VK_LEFT && e.isControlDown()) {
            if (mFavoritesList != null) {
              mFavoritesList.grabFocus();
              if (mFavoritesList.getSelectedIndex() == -1) {
                mFavoritesList.setSelectedIndex(0);
              }
            } else if (mFavoriteTree != null) {
              mFavoriteTree.grabFocus();
              if (mFavoriteTree.getSelectionCount() == 0) {
                mFavoriteTree.setSelectionRow(0);
              }
            }
          }
        }
      });
    
    mSplitPane.setRightComponent(mProgramListPanel);

    msg = LOCALIZER.msg("showBlack", "Show single removed programs");
    mBlackListChb = new JCheckBox(msg);
    mBlackListChb.setOpaque(false);
    mBlackListChb.setSelected(FavoritesPlugin.getInstance().isShowingBlackListEntries());
    mBlackListChb.setOpaque(false);
    mBlackListChb.addActionListener(e -> {
      FavoritesPlugin.getInstance().setIsShowingBlackListEntries(mBlackListChb.isSelected());
      favoriteSelectionChanged();
    });

    JPanel buttonPn = new JPanel(new BorderLayout());
    buttonPn.setOpaque(false);

    if(!mShowNew) {
      buttonPn.add(mBlackListChb, BorderLayout.WEST);
    }

    add(buttonPn, BorderLayout.SOUTH);

    mCloseBt = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    
    if(ManageFavoritesDialog.getInstance() != null) {
      mCloseBt.addActionListener(e -> {
        if(ManageFavoritesDialog.getInstance() != null && ManageFavoritesDialog.getInstance().isVisible()) {
          ManageFavoritesDialog.getInstance().close();
        }
      });
      buttonPn.add(mCloseBt, BorderLayout.EAST);
      ManageFavoritesDialog.getInstance().getRootPane().setDefaultButton(mCloseBt);
    }

    if(mFavoriteTree != null) {
      FavoriteNode initialNode = mFavoriteTree.getRoot();

      if (initialSelection != null) {
        initialNode = mFavoriteTree.findFavorite(initialSelection);
      }
      TreePath treePath = new TreePath(initialNode.getPath());
      mFavoriteTree.setSelectionPath(treePath);
      mFavoriteTree.scrollPathToVisible(treePath);
    }

    favoriteSelectionChanged(true);}catch(Throwable t) {t.printStackTrace();}
  }

  private void addToolbarSeperator(JToolBar toolbarPn) {
    JPanel p = new JPanel();
    p.setOpaque(false);
    p.setSize(10,10);
    p.setMaximumSize(new Dimension(10,10));
    toolbarPn.add(p);
    toolbarPn.addSeparator();

    p = new JPanel();
    p.setOpaque(false);
    p.setSize(4,10);
    p.setMaximumSize(new Dimension(4,10));
    toolbarPn.add(p);
  }

  /**
   * Show the Popup-Menu
   * @param x X-Position for the popup
   * @param y Y-Position for the popup
   */
  protected void showFavoritesPopUp(int x, int y) {
    JPopupMenu menu = new JPopupMenu();

    mFavoritesList.setSelectedIndex(mFavoritesList.locationToIndex(new Point(x,y)));

    if (!mShowNew) {
      JMenuItem createNew = new JMenuItem(LOCALIZER.ellipsisMsg("new", "Create a new favorite"),
          TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_SMALL));

      createNew.addActionListener(e -> {
        newFavorite(mFavoriteTree.getRoot());
      });

      menu.add(createNew);
      menu.addSeparator();
    }

    JMenuItem edit = new JMenuItem(LOCALIZER.ellipsisMsg("edit", "Edit the selected favorite"),
        TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));

    edit.addActionListener(e -> {
      editSelectedFavorite();
    });

    menu.add(edit);

    JMenuItem delete = new JMenuItem(LOCALIZER.ellipsisMsg("delete", "Delete selected favorite"),
        TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));

    delete.addActionListener(e -> {
      deleteSelectedFavorite();
    });

    menu.add(delete);
    menu.addSeparator();

    JMenuItem sendPrograms = new JMenuItem(LOCALIZER.msg("send", "Send Programs to another Plugin"),
        TVBrowserIcons.copy(TVBrowserIcons.SIZE_SMALL));

    sendPrograms.addActionListener(e -> {
      showSendDialog();
    });
    
    sendPrograms.setEnabled(mProgramList.getModel().getSize() > 0);

    menu.add(sendPrograms);

    menu.show(mFavoritesList, x, y);
  }


  public int getSplitpanePosition() {
    return mSplitPane.getDividerLocation();
  }

  public void setSplitpanePosition(int val) {
    mSplitPane.setDividerLocation(val);
  }

  /**
   * Refresh the program list.
   * @param scrollToFirst <code>true</code> if it should be scrolled to first index, <code>false</code> if not.
   */
  public synchronized void favoriteSelectionChanged(final boolean scrollToFirst) {
    if(mFavoritesList != null) {
      int selection = mFavoritesList.getSelectedIndex();
      int size = mFavoritesListModel.getSize();

      mEditBt.setEnabled(selection != -1);
      mDeleteBt.setEnabled(selection != -1);

      mEditBt.setToolTipText(LOCALIZER.ellipsisMsg("edit", "Edit the selected favorite"));
      mDeleteBt.setToolTipText(LOCALIZER.ellipsisMsg("delete", "Delete selected favorite"));

      mUpBt.setEnabled(selection > 0);
      mDownBt.setEnabled((selection != -1) && (selection < (size - 1)));

      mSortAlphaBt.setEnabled(size >= 2);
      mSortCountBt.setEnabled(mSortAlphaBt.isEnabled());

      if (selection == -1) {
        mProgramListPanel.clearPrograms();
        mSendBt.setEnabled(false);
      } else {
        changeProgramList((Favorite)mFavoritesList.getSelectedValue(),scrollToFirst);
      }
      
      Rectangle rect = mFavoritesList.getCellBounds(selection, selection);
      
      if(rect != null) {
        mFavoritesList.paintImmediately(rect);
      }
    }
    else {
      if(mFavoriteTree != null && mFavoriteTree.getSelectionPath() != null) {
        Favorite fav = ((FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent()).getFavorite();

        if(fav != null) {
          enableButtons(true);
          changeProgramList(fav,scrollToFirst);
          mDeleteBt.setEnabled(true);
          mEditBt.setToolTipText(LOCALIZER.ellipsisMsg("edit", "Edit the selected favorite"));
          mDeleteBt.setToolTipText(LOCALIZER.ellipsisMsg("delete", "Delete selected favorite"));
        }
        else {
          Program[] p = ((FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent()).getAllPrograms(false);

          int firstNotExpiredIndex = -1;
          
          if(p != null && p.length > 0) {
            enableButtons(true);

            Arrays.sort(p,ProgramUtilities.getProgramComparator());

            int maxSize = Math.min(p.length,MAX_SHOWN_PROGRAMS);
            
            ArrayList<Program> programs = new ArrayList<Program>(maxSize);
            
            Hashtable<Channel,ArrayList<Program>> test = new Hashtable<Channel,ArrayList<Program>>();
            
            int i = 0;
            
            while (i < p.length && programs.size() < MAX_SHOWN_PROGRAMS) {
              // don't list programs twice, if they are marked by different favorites
              ArrayList<Program> testList = test.get(p[i].getChannel());
              if(testList == null) {
                testList = new ArrayList<Program>();
                test.put(p[i].getChannel(), testList);
              }
              
              if (!testList.contains(p[i])) {
                testList.add(p[i]);
                programs.add(p[i]);

                if(firstNotExpiredIndex == -1 && !p[i].isExpired()) {
                  firstNotExpiredIndex = programs.size()-1;
                }
              }
              
              i++;
            }
            
            mProgramListPanel.setPrograms(programs.toArray(new Program[programs.size()]));
            
            if (scrollToFirst) {
              scrollInProgramListToIndex(firstNotExpiredIndex);
            }

            mSendBt.setEnabled(true);
            mScrollToPreviousDay.setEnabled(true);
            mScrollToNextDay.setEnabled(true);
            mDeleteBt.setEnabled(false);
          }
          else {
            mProgramListPanel.clearPrograms();

            FavoriteNode node = (FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent();

            enableButtons(node.isDirectoryNode());

            mDeleteBt.setEnabled(node.isDirectoryNode() && node.getChildCount() < 1);
            mSendBt.setEnabled(false);
            mScrollToPreviousDay.setEnabled(false);
            mScrollToNextDay.setEnabled(false);
          }
          mEditBt.setToolTipText(LOCALIZER.ellipsisMsg("renameFolder", "Rename selected folder"));
          mDeleteBt.setToolTipText(LOCALIZER.msg("deleteFolder", "Delete selected folder"));
        }
      }
      else {
        if(mProgramListPanel != null) {
          mProgramListPanel.clearPrograms();
          mDeleteBt.setEnabled(false);
          mSendBt.setEnabled(false);
          enableButtons(false);
        }
      }
    }
  }

  public void scrollToFirstNotExpiredIndex(boolean check) {
    mProgramListPanel.scrollToFirstNotExpiredIndex(check);
  }
  
  public void scrollInProgramListToIndex(final int index) {
    mProgramListPanel.scrollToIndex(index);
  }

  private void enableButtons(boolean enabled) {
    TreePath path = mFavoriteTree.getSelectionPath();

    mEditBt.setEnabled(enabled && path != null && !path.getLastPathComponent().equals(mFavoriteTree.getRoot()));

    mUpBt.setEnabled((enabled || (path != null && ((FavoriteNode)path.getLastPathComponent()).isDirectoryNode())) && !path.getLastPathComponent().equals(mFavoriteTree.getRoot()) && mFavoriteTree.getRowForPath(mFavoriteTree.getSelectionPath()) > 0 );
    mDownBt.setEnabled((enabled || (path != null && ((FavoriteNode)path.getLastPathComponent()).isDirectoryNode())) && !path.getLastPathComponent().equals(mFavoriteTree.getRoot()) && mFavoriteTree.getRowForPath(mFavoriteTree.getSelectionPath()) < mFavoriteTree.getRowCount() -1);

    if(path != null && !((FavoriteNode)path.getLastPathComponent()).isDirectoryNode() && path.getParentPath().getLastPathComponent().equals(mFavoriteTree.getRoot())) {
      path = path.getParentPath();
    }

    mSortAlphaBt.setEnabled(path == null || (enabled && (path != null && ((FavoriteNode)path.getLastPathComponent()).isDirectoryNode() && ((FavoriteNode)path.getLastPathComponent()).getChildCount() > 1 || path.getLastPathComponent().equals(mFavoriteTree.getRoot()))));
    mSortCountBt.setEnabled(mSortAlphaBt.isEnabled());
  }

  private void changeProgramList(Favorite fav, boolean scrollToFirstIndex) {
    Program[] programArr = mShowNew ? fav.getNewPrograms() : fav.getWhiteListPrograms();
    Program[] blackListPrograms = fav.getBlackListPrograms();

    Program[] programs = programArr;
    
    if(!mShowNew && mBlackListChb.isSelected()) {
      programs = new Program[programArr.length + blackListPrograms.length];
      
      System.arraycopy(programArr, 0, programs, 0, programArr.length);
      System.arraycopy(blackListPrograms, 0, programs, programArr.length, blackListPrograms.length);
    }
    
    mProgramListPanel.setPrograms(programs);
    
    mSendBt.setEnabled(mProgramList.getModel().getSize() > 0);
    mScrollToPreviousDay.setEnabled(mSendBt.isEnabled());
    mScrollToNextDay.setEnabled(mSendBt.isEnabled());
    
    if(scrollToFirstIndex) {
      scrollToFirstNotExpiredIndex(false);
    }
  }

  public void showSendDialog() {
    if(mFavoritesList != null) {
      int selection = mFavoritesList.getSelectedIndex();

      if(selection == -1) {
        return;
      }
    }
    else if(mFavoriteTree.getSelectionPath() == null) {
      return;
    }

    Program[] programs = mProgramList.getSelectedPrograms();

    Favorite fav;

    if(mFavoritesList != null) {
      fav = (Favorite) mFavoritesListModel.get(mFavoritesList.getSelectedIndex());
    }
    else if(programs == null) {
      programs = ((FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent()).getAllPrograms(true);

      if(programs.length < 1) {
        programs = ((FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent()).getAllPrograms(false);
      }

      fav = null;
    }
    else {
      fav = null;
    }

    if (fav != null && (programs == null || programs.length == 0)) {
      programs = mShowNew ? fav.getNewPrograms() : fav.getWhiteListPrograms();
    }

    SendToPluginDialog send = new SendToPluginDialog(null, (ManageFavoritesDialog.getInstance() != null && ManageFavoritesDialog.getInstance().isVisible()) ? (Window)ManageFavoritesDialog.getInstance() : MainFrame.getInstance(), programs);

    send.setVisible(true);
  }


  public void newFavorite(FavoriteNode parent) {
    Favorite favorite;
    if (FavoritesPlugin.getInstance().isUsingExpertMode()) {
      if(FavoritesPlugin.getInstance().showTypeSelection() && JOptionPane.showConfirmDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), FavoritesPlugin.LOCALIZER.msg("askType.message", "Create a filter favorite?"), FavoritesPlugin.LOCALIZER.msg("askType.title", "Type selection"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
        favorite = new FilterFavorite();
      }
      else {
        favorite = new AdvancedFavorite("");
      }
      
      EditFavoriteDialog dlg = new EditFavoriteDialog((ManageFavoritesDialog.getInstance() != null && ManageFavoritesDialog.getInstance().isVisible()) ? (Window)ManageFavoritesDialog.getInstance() : MainFrame.getInstance(), favorite);
      UiUtilities.centerAndShow(dlg);
      
      if (!dlg.getOkWasPressed()) {
        favorite = null;
      }

    } else {
      WizardHandler handler = new WizardHandler((ManageFavoritesDialog.getInstance() != null && ManageFavoritesDialog.getInstance().isVisible()) ? (Window)ManageFavoritesDialog.getInstance() : MainFrame.getInstance(), new TypeWizardStep(null,parent));
      favorite = (tvbrowser.extras.favoritesplugin.core.Favorite)handler.show();
    }

    // in case of AdvancedFavorite: search not necessary (because already done)
    addFavorite(favorite, !(favorite instanceof AdvancedFavorite), parent);
  }

  public void addFavorite(Favorite fav, boolean update, FavoriteNode parent) {
    FavoriteNode newNode = null;
    if (fav != null) {
      try {
        if (update) {
          fav.updatePrograms();
        }
        if(mFavoritesList != null) {
          mFavoritesListModel.addElement(fav);
          int idx = mFavoritesListModel.size() - 1;
          mFavoritesList.setSelectedIndex(idx);
          mFavoritesList.ensureIndexIsVisible(idx);
        }
        newNode = FavoriteTreeModel.getInstance().addFavorite(fav, parent);
      } catch (TvBrowserException e) {
        ErrorHandler.handle("Creating favorites failed.", e);
      }
    }
    
    if (newNode != null) {
      if (parent != null) {
        mFavoriteTree.reload(parent);
      }
      TreePath path = new TreePath(newNode.getPath());
      mFavoriteTree.scrollPathToVisible(path);
      mFavoriteTree.setSelectionPath(path);
      favoriteSelectionChanged();
    }
  }

  public void addFavorite(Favorite fav, Object dummy) {
    if(mFavoritesListModel != null) {
      mFavoritesListModel.addElement(fav);
    }
  }
  
  public void reload() {
    reload(false);
  }
  
  public void reload(boolean keepPath) {
    if(mFavoriteTree != null) {
      final TreePath path = mFavoriteTree.getSelectionPath();
      mFavoriteTree.reload(mFavoriteTree.getRoot());
      
      if(path != null && keepPath) {
        mFavoriteTree.setSelectionPath(path);
      }
    }
  }
  
  public int getSelectedProgramIndex() {
    return mProgramList.getSelectedIndex();
  }

  public void editSelectedFavorite() {
    Favorite fav = null;
    FavoriteNode node = null;
    
    int index = mProgramList.getSelectedIndex();
    
    if(mFavoritesList != null) {
      fav = (Favorite) mFavoritesList.getSelectedValue();
      index = mFavoritesList.getSelectedIndex();
    }
    else {
      if (mFavoriteTree.getSelectionCount() > 0) {
        node = (FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent();
        fav = node.getFavorite();
      }
    }

    if (fav != null) {
        EditFavoriteDialog dlg = new EditFavoriteDialog((ManageFavoritesDialog.getInstance() != null && ManageFavoritesDialog.getInstance().isVisible()) ? (Window)ManageFavoritesDialog.getInstance() : MainFrame.getInstance(), fav);
        UiUtilities.centerAndShow(dlg);
        if (dlg.getOkWasPressed()) {
          if(mFavoritesList != null) {
            mFavoritesList.repaint();
          }
          //favoriteSelectionChanged();
          FavoritesPlugin.getInstance().updateRootNode(true);
        }

        if (node != null) {
          mFavoriteTree.reload(node);
          mFavoriteTree.repaint();
        }
        
        scrollInProgramListToIndex(index);
    }
  }

  public void deleteSelectedFavorite() {
    int selection = -1;
    
    if(mFavoritesList != null) {
      selection = mFavoritesList.getSelectedIndex();
    }
    else {
      if(mFavoriteTree.getSelectionPath() != null && ((FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent()).containsFavorite()) {
        selection = 0;
      }
    }
    if (selection != -1) {
      Favorite fav;
      FavoriteNode parent = null;
      if(mFavoritesList != null) {
        fav = (Favorite) mFavoritesListModel.get(selection);
        mFavoritesListModel.remove(selection);
      }
      else {
        FavoriteNode node = ((FavoriteNode)mFavoriteTree.getSelectionPath().getLastPathComponent());
        fav = node.getFavorite();
        parent = (FavoriteNode) node.getParent();
      }

      if (JOptionPane.showConfirmDialog(this,
              FavoritesPlugin.LOCALIZER.msg("reallyDelete", "Really delete favorite '{0}'?", fav.getName()),
              LOCALIZER.msg("delete", "Delete selected favorite..."),
              JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

        FavoriteTreeModel.getInstance().deleteFavorite(fav);
        
        if (parent != null) {
          mFavoriteTree.setSelectionPath(new TreePath(parent.getPath()));
          mFavoriteTree.reload(parent);
        }
        
        favoriteSelectionChanged();
      }
    }
  }

  protected void sortFavorites(Comparator<TreeNode> comp, String title) {
    TreePath path = mFavoriteTree.getSelectionPath();

    if(path != null && !((FavoriteNode)path.getLastPathComponent()).isDirectoryNode() && path.getParentPath().getLastPathComponent().equals(mFavoriteTree.getRoot())) {
      path = path.getParentPath();
    }

    if(path == null) {
      path = new TreePath(mFavoriteTree.getRoot());
    }
    
    if(((FavoriteNode)path.getLastPathComponent()).isDirectoryNode()) {
      FavoriteTreeModel.getInstance().sort((FavoriteNode)path.getLastPathComponent(), comp, title);
      mFavoriteTree.reload((FavoriteNode)path.getLastPathComponent());
      FavoritesPlugin.getInstance().store();
    }
  }

  @SuppressWarnings("unchecked")
  private void importFavoritesAndroid() {
    PluginProxy androidSync = PluginProxyManager.getInstance().getActivatedPluginForId("java.androidsync.AndroidSync");
    PluginCommunication c = androidSync.getCommunicationClass();
    
    if(c != null && c.getVersion() >= 2) {
      final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      cal.set(2021, 12, 24);
      
      final Calendar my = Calendar.getInstance(TimeZone.getDefault());
      
      final int KEYWORD_ONLY_TITLE_TYPE = 0;
      final int KEYWORD_TYPE = 1;
      final int RESTRICTION_RULES_TYPE = 2;
      
      try {
        Method getFavorites = c.getClass().getDeclaredMethod("getFavorites");
        getFavorites.setAccessible(true);
        Object result = getFavorites.invoke(c);
        
        if(result == null) {
          UiUtilities.showMessageDialogOnMouseScreen(LOCALIZER.msg("androidSync.noData", "AndroidSync has not provided the needed data.\nMake sure the Favorites were uploaded in the Android app,\nyou're connected to the Internet and try again later."), Localizer.getLocalization(Localizer.I18N_ERROR), JOptionPane.ERROR_MESSAGE);
        }
        else if(result instanceof String[]) {
          String[] favorites = (String[])result;
          
          if(favorites.length == 0) {
            UiUtilities.showMessageDialogOnMouseScreen(LOCALIZER.msg("androidSync.noFavorites", "AndroidSync has found no Favorites.\nMake sure the Favorites were uploaded in the Android app\nand check if the correct credentials were entered in the\nAndroidSync plugin."), Localizer.getLocalization(Localizer.I18N_INFO), JOptionPane.INFORMATION_MESSAGE);
          }
          else {
            JCheckBox expert = new JCheckBox(LOCALIZER.msg("androidSync.importAsExpert", "Import all as expert Favorites"));
            
            JRadioButton add = new JRadioButton(LOCALIZER.msg("androidSync.duplicates.msg.add", "Add imported Favorite"),true);
            JRadioButton replace = new JRadioButton(LOCALIZER.msg("androidSync.duplicates.msg.replace", "Replace existing with imported Favorite"));
            JRadioButton ignore = new JRadioButton(LOCALIZER.msg("androidSync.duplicates.msg.ignore", "Ignore Favorite to import"));
            JRadioButton ask = new JRadioButton(LOCALIZER.msg("androidSync.duplicates.msg.ask", "Separately ask for each existing Favorite"));
            
            ButtonGroup bg = new ButtonGroup();
            
            bg.add(add);
            bg.add(replace);
            bg.add(ignore);
            bg.add(ask);
            
            Object[] message = new Object[] {
              LOCALIZER.msg("androidSync.duplicates.msg.global", "How to handle existing imported Favorites?\n\n"),
              add,
              replace,
              ignore,
              ask,
              " ",
              expert
            };
            
            if(UiUtilities.showConfirmDialogOnMouseScreen(message, LOCALIZER.msg("androidSync.duplicates.title", "How to handle duplicate Favorites?"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
              final ImportInfo importInfo = new ImportInfo(add.isSelected(), replace.isSelected(), ask.isSelected());
              FavoriteNode folderAdded = null;
              
              HashMap<String, ArrayList<Favorite>> existingFavorites = new HashMap<String, ArrayList<Favorite>>();
              
              if(!importInfo.mAdd) {
                Favorite[] favs = mFavoriteTree.getModel().getFavoriteArr();
                
                for(Favorite fav : favs) {
                  ArrayList<Favorite> list = existingFavorites.get(fav.getName());
                  
                  if(list == null) {
                    list = new ArrayList<Favorite>();
                    existingFavorites.put(fav.getName(), list);
                  }
                  
                  list.add(fav);
                }
              }
              
              int count = 0;
              
              for(String fav : favorites) {
                String[] values = fav.split(";;");
                
                String name = null;
                String search = null;
                
                int type = KEYWORD_ONLY_TITLE_TYPE;
                boolean remind = false;
                ArrayList<Channel> excludedChannels = null;
                int timeRestrictionStart = -1;
                int timeRestrictionEnd = -1;
                int[] restrictedDays = null;
                String[] exculdedKeywords = null;
                int shorterThan = -1;
                int longerThan = -1;
                int categories = 0;
                
                if(values.length > 2) {
                  name = values[0];
                  search = values[1];
                  
                  try {
                    type = Integer.parseInt(values[2]);
                  }catch(NumberFormatException e) {
                    boolean onlyTitle = Boolean.valueOf(values[2]);
                    
                    if(onlyTitle) {
                      type = KEYWORD_ONLY_TITLE_TYPE;
                    }
                    else {
                      type = KEYWORD_TYPE;
                    }
                  }
                  
                  if(type == RESTRICTION_RULES_TYPE) {
                    search = ".*";
                  }
                  
                  ArrayList<Favorite> duplicates = new ArrayList<Favorite>();
                  
                  if(!importInfo.mAdd) {
                    ArrayList<Favorite> list = existingFavorites.get(name);
                    
                    if(list != null) {
                      for(Favorite entry : list) {
                        if(entry.getSearchText().equals(search)) {
                          duplicates.add(entry);
                        }
                      }
                    }
                  }
                  
                  ImportInfo importInfoTemp = null;
                  
                  if(!duplicates.isEmpty() && importInfo.mAsk) {
                    add.setSelected(true);
                    message = new Object[] {
                        LOCALIZER.msg("androidSync.duplicates.msg.single","Duplicate Favorites found: '{0}'\nWhat should be done with found Favorite?\n\n",name),
                        add,
                        replace,
                        ignore
                      };
                      
                    if(UiUtilities.showConfirmDialogOnMouseScreen(message, LOCALIZER.msg("androidSync.duplicates.title", "How to handle duplicate Favorites?"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
                      importInfoTemp = new ImportInfo(add.isSelected(), replace.isSelected(), false);
                    }
                    else {
                      importInfoTemp = new ImportInfo(false, false, false);
                    }
                  }
                  else {
                    importInfoTemp = importInfo;
                  }
                  
                  if(duplicates.isEmpty() || importInfoTemp.mReplace || importInfoTemp.mAdd) {
                    if(values.length > 3) {
                      remind = Boolean.valueOf(values[3]);
                    }
                    
                    if(values.length > 4) {
                      if(!values[4].equals("null")) {
                        String[] parts = values[4].split(",");
                        
                        try {
                          timeRestrictionStart = Integer.parseInt(parts[1])+1;
                          timeRestrictionEnd = Integer.parseInt(parts[0])-1;
                          
                          if(timeRestrictionStart >= 1440) {
                            timeRestrictionStart -= 1440;
                          }
                          if(timeRestrictionEnd < 0) {
                            timeRestrictionEnd += 1440;
                          }
                          
                          
                          cal.set(Calendar.HOUR_OF_DAY, timeRestrictionStart/60);
                          cal.set(Calendar.MINUTE, timeRestrictionStart%60);
                          
                          my.setTimeInMillis(cal.getTimeInMillis());
                          
                          timeRestrictionStart = my.get(Calendar.HOUR_OF_DAY)*60+my.get(Calendar.MINUTE);
                          
                          cal.set(Calendar.HOUR_OF_DAY, timeRestrictionEnd/60);
                          cal.set(Calendar.MINUTE, timeRestrictionEnd%60);
                          
                          my.setTimeInMillis(cal.getTimeInMillis());
                          
                          timeRestrictionEnd = my.get(Calendar.HOUR_OF_DAY)*60+my.get(Calendar.MINUTE);
                        }catch(NumberFormatException nfe) {
                          timeRestrictionStart = -1;
                          timeRestrictionEnd = -1;
                        }
                      }
                      
                      Object dayRestriction = parseArray(DAY_RESTRICTION_TYPE, values[5]);
                      
                      if(dayRestriction != null && dayRestriction instanceof int[]) {
                        int[] temp = (int[])dayRestriction;
                        
                        ArrayList<Integer> days = new ArrayList<Integer>();
                        days.add(Calendar.MONDAY);
                        days.add(Calendar.TUESDAY);
                        days.add(Calendar.WEDNESDAY);
                        days.add(Calendar.THURSDAY);
                        days.add(Calendar.FRIDAY);
                        days.add(Calendar.SATURDAY);
                        days.add(Calendar.SUNDAY);
                        
                        for(int test : temp) {
                          days.remove((Integer)test);
                        }
                        
                        if(!days.isEmpty()) {
                          restrictedDays = new int[days.size()];
                          
                          for(int i = 0; i < days.size(); i++) {
                            restrictedDays[i] = days.get(i);
                          }
                        }
                      }
                      
                      Object exclCh = parseArray(CHANNEL_RESTRICTION_TYPE, values[6]);
                      
                      if(exclCh != null && exclCh instanceof ArrayList<?>) {
                        excludedChannels = (ArrayList<Channel>)exclCh;
                      }
                    }
                    
                    if(values.length > 7 && !values[7].equals("null")) {
                      if(values[7].contains(",")) {
                        exculdedKeywords = values[7].split(",");
                      }
                      else {
                        exculdedKeywords = new String[1];
                        exculdedKeywords[0] = values[7];
                      }
                    }
                    
                    if(values.length > 8) {
                      if(!values[8].equals("null")) {
                        String[] parts = values[8].split(",");
                        
                        try {
                          longerThan = Integer.parseInt(parts[0]);
                          shorterThan = Integer.parseInt(parts[1]);
                          
                          if(longerThan != -1) {
                            longerThan--;
                          }
                          if(shorterThan != -1) {
                            shorterThan++;
                          }
                        }catch(NumberFormatException nfe) {
                          longerThan = shorterThan = -1;
                        }
                      }
                    }
                    
                    if(values.length > 9) {
                      Object temp = parseArray(ATTRIBUTE_RESTRICTION_TYPE, values[9]);
                      
                      if(temp != null && temp instanceof int[]) {
                        int[] cats = (int[])temp;
                        
                        for(int cat : cats) {
                          categories |= (1 << (cat+1));
                        }
                      }
                    }
                    
                    Favorite toAdd = null;
                    
                    if(type == KEYWORD_ONLY_TITLE_TYPE) {
                      if(expert.isSelected()) {
                        toAdd = new AdvancedFavorite(search,SearchFormSettings.SEARCH_IN_TITLE,PluginManager.TYPE_SEARCHER_KEYWORD,false);
                      }
                      else {
                        toAdd = new TitleFavorite(search);
                      }
                    }
                    else if(type == KEYWORD_TYPE) {
                      if(expert.isSelected()) {
                        toAdd = new AdvancedFavorite(search,SearchFormSettings.SEARCH_IN_ALL,PluginManager.TYPE_SEARCHER_KEYWORD,false);
                      }
                      else {
                        toAdd = new TopicFavorite(search);
                      }
                    }
                    else if(type == RESTRICTION_RULES_TYPE) {
                      toAdd = new AdvancedFavorite(".*",SearchFormSettings.SEARCH_IN_TITLE,PluginManager.TYPE_SEARCHER_REGULAR_EXPRESSION,false);
                    }
                    
                    if(toAdd != null) {
                      toAdd.setName(name);
                      ArrayList<Exclusion> exclusionList = new ArrayList<Exclusion>();
                      
                      if(remind) {
                        toAdd.setReminderMinutesDefault(ReminderPlugin.getInstance().getDefaultReminderTime());
                        toAdd.getReminderConfiguration().setReminderServices(new String[] { ReminderConfiguration.REMINDER_DEFAULT });
                      }
                      else {
                        toAdd.getReminderConfiguration().setReminderServices(new String[0]);
                      }
                      
                      if(excludedChannels != null) {
                        for(Channel ch : excludedChannels) {
                          exclusionList.add(new Exclusion(null, null, ch, -1, -1, -1, null, null, 0, null, Exclusion.TYPE_DURATION_NONE, -1));
                        }
                      }
                      
                      if(timeRestrictionStart != -1 && timeRestrictionEnd != -1) {
                        exclusionList.add(new Exclusion(null, null, null, timeRestrictionStart, timeRestrictionEnd, -1, null, null, 0, null, Exclusion.TYPE_DURATION_NONE, -1));
                      }
                      
                      if(restrictedDays != null) {
                        for(int day : restrictedDays) {
                          exclusionList.add(new Exclusion(null, null, null, -1, -1, day, null, null, 0, null, Exclusion.TYPE_DURATION_NONE, -1));
                        }
                      }
                      
                      if(exculdedKeywords != null) {
                        for(String keyword : exculdedKeywords) {
                          exclusionList.add(new Exclusion(null, keyword, null, -1, -1, -1, null, null, 0, null, Exclusion.TYPE_DURATION_NONE, -1));
                        }
                      }
                      
                      if(shorterThan != -1) {
                        exclusionList.add(new Exclusion(null, null, null, -1, -1, -1, null, null, 0, null, Exclusion.TYPE_DURATION_TOO_LONG, shorterThan));
                      }
        
                      if(longerThan != -1) {
                        exclusionList.add(new Exclusion(null, null, null, -1, -1, -1, null, null, 0, null, Exclusion.TYPE_DURATION_TOO_SHORT, longerThan));
                      }
                      
                      if(categories != 0) {
                        try(ByteArrayOutputStream bOut = new ByteArrayOutputStream(); ObjectOutputStream out = new ObjectOutputStream(bOut);) {
                          out.writeInt(categories);
                          out.flush();
                          try(ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bOut.toByteArray()))) {
                            ProgramInfoFilterComponent infoFilter = new ProgramInfoFilterComponent("AndroidSync-Import_"+name.replaceAll("\\s+|\\p{Punct}", "_")+"_"+DateFormat.getDateTimeInstance().format(new java.util.Date()).replaceAll("\\s+|\\p{Punct}", "_"), "");
                            infoFilter.read(in, 1);
                            
                            FilterComponentList.getInstance().add(infoFilter);
                            
                            UserFilter filter = new UserFilter("AndroidSync-Import_"+name.replaceAll("\\s+|\\p{Punct}", "_")+"_"+DateFormat.getDateTimeInstance().format(new java.util.Date()).replaceAll("\\s+|\\p{Punct}", "_"));
                            filter.setRule("NOT "+infoFilter.getName());
                            
                            FilterList.getInstance().addProgramFilter(filter);
                            
                            exclusionList.add(new Exclusion(null, null, null, -1, -1, -1, filter.getName(), null, 0, null, Exclusion.TYPE_DURATION_NONE, -1));
                          }
                        }catch(IOException ioe) {
                          ioe.printStackTrace();
                        }
                      }
        
                      if(!exclusionList.isEmpty()) {
                        toAdd.setExclusions(exclusionList.toArray(new Exclusion[0]));
                      }
                      
                      if(!duplicates.isEmpty() && importInfoTemp.mReplace) {
                        FavoriteNode parent = null;
                        
                        for(Favorite dup : duplicates) {
                          if(parent == null) {
                            FavoriteNode temp = mFavoriteTree.findFavorite(dup);
                            if(temp != null) {
                              parent = (FavoriteNode)temp.getParent();
                            }
                          }
                          
                          FavoriteTreeModel.getInstance().deleteFavorite(dup);
                        }
                        
                        duplicates.clear();
                        
                        if(parent == null) {
                          parent = folderAdded;
                        }
                        duplicates.add(toAdd);
                        
                        addFavorite(toAdd, true, parent);
                      }
                      else {
                        if(folderAdded == null) {
                          TreePath path = mFavoriteTree.getSelectionPath();
                          
                          if(path != null) {
                            FavoritesPlugin.getInstance().newFolder((FavoriteNode)path.getLastPathComponent(),"AndroidSync-Import "+DateFormat.getDateTimeInstance().format(new java.util.Date()));
                          } else {
                            FavoritesPlugin.getInstance().newFolder(mFavoriteTree.getRoot(),"AndroidSync-Import "+DateFormat.getDateTimeInstance().format(new java.util.Date()));
                          }
                          
                          path = mFavoriteTree.getSelectionPath();
                          folderAdded = (FavoriteNode)path.getLastPathComponent();
                          favoriteSelectionChanged();
                        }
                        
                        addFavorite(toAdd, true, folderAdded);
                      }
                      
                      count++;
                      
                      if(!importInfo.mAdd) {
                        ArrayList<Favorite> list = existingFavorites.get(toAdd.getName());
                        
                        if(list == null) {
                          list = new ArrayList<Favorite>();
                          existingFavorites.put(toAdd.getName(), list);
                        }
                        
                        list.add(toAdd);
                      }
                    }
                  }
                }
              }
              
              if(count > 0) {
                UiUtilities.showMessageDialogOnMouseScreen(LOCALIZER.msg("androidSync.success", "{0} Favorites were imported.", count), Localizer.getLocalization(Localizer.I18N_INFO), JOptionPane.INFORMATION_MESSAGE);
                MainFrame.getInstance().updateFilterMenu();
              }
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  private static final int DAY_RESTRICTION_TYPE = 0;
  private static final int CHANNEL_RESTRICTION_TYPE = 1;
  private static final int ATTRIBUTE_RESTRICTION_TYPE = 2;
  
  private Object parseArray(int type, String value) {
    Object result = null;
    
    if(!value.equals("null")) {
      if(type == CHANNEL_RESTRICTION_TYPE && value.contains("#_#")) {
        ArrayList<Channel> channelList = new ArrayList<Channel>();
        channelList.addAll(Arrays.asList(ChannelList.getSubscribedChannels()));
        
        String[] parts = value.split(",");
        
        for(String part : parts) {
          String[] subParts = part.split("#_#");
          
          for(int i = channelList.size()-1; i >= 0; i--) {
            Channel ch = channelList.get(i);
            if(ch.getDataServiceId().equals("tvbrowserdataservice.TvBrowserDataService") && subParts[0].equals("1")) {
              if(ch.getGroup().getId().equals(subParts[1]) && ch.getId().equals(subParts[2])) {
                channelList.remove(i);
                break;
              }
            }
            else if(ch.getDataServiceId().equals("epgdonatedata.EPGdonateData") && subParts[0].equals("2")) {
              if(ch.getId().equals(subParts[1])) {
                channelList.remove(i);
                break;
              }
            }
          }
        }
        
        if(!channelList.isEmpty()) {
          result = channelList;
        }
      }
      else {
        String[] parts = value.split(",");
        
        int[] array = new int[parts.length];
        
        for(int i = 0; i < parts.length; i++) {
          array[i] = Integer.parseInt(parts[i]);
        }
        
        result = array;
      }
    }
    
    return result;
  }

  protected void importFavorites() {
    JFileChooser fileChooser = new JFileChooser();
    String[] extArr = { ".txt" };
    String msg = LOCALIZER.msg("importFile.TVgenial", "Text file (from TVgenial) (.txt)");
    fileChooser.setFileFilter(new ExtensionFileFilter(extArr, msg));
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

      File file = fileChooser.getSelectedFile();
      if (file != null) {
        FileReader reader = null;
        int importedFavoritesCount = 0;
        BufferedReader lineReader = null;
        try {
          reader = new FileReader(file);
          lineReader = new BufferedReader(reader);
          String line;
          while ((line = lineReader.readLine()) != null) {
            line = line.trim();
            if ((line.length() > 0) && (! line.startsWith("***"))) {
              // This is a favorite -> Check whether we already have such a favorite
              boolean alreadyKnown = false;
              Favorite[] favs = null;
              if (mFavoritesListModel != null) {
                favs = (Favorite[]) mFavoritesListModel.toArray();
              } else if (mFavoriteTree != null) {
                favs = FavoriteTreeModel.getInstance().getFavoriteArr();
              }
              if (favs != null) {
                for (Favorite favorite : favs) {
                  String favName = favorite.getName();
                  if (line.equalsIgnoreCase(favName)) {
                    alreadyKnown = true;
                    break;
                  }
                }
                // Import the favorite if it is new
                if (! alreadyKnown) {
                  line = line.replace(" *", " OR ").replace(" |", " OR ")
                      .replace(" ODER ", " OR ").replace(" +", " AND ")
                      .replace(" &", " AND ").replace(" UND ", " AND ")
                      .replace(" \\", " NOT ").replace(" NICHT ", " NOT ")
                      .replace("_", " ").trim();
                  while (line.indexOf("  ") >= 0) {
                    line = line.replace("  ", " ");
                  }
                  AdvancedFavorite fav = new AdvancedFavorite(line);
                  fav.updatePrograms();
                  if (mFavoritesListModel != null) {
                    mFavoritesListModel.addElement(fav);
                  }
                  else {
                    FavoriteTreeModel.getInstance().addFavorite(fav);
                  }
                  importedFavoritesCount++;
                }
              }
            }
          }
        }
        catch (Exception exc) {
          msg = LOCALIZER.msg("error.1", "Importing text file failed: {0}.",
                               file.getAbsolutePath());
          ErrorHandler.handle(msg, exc);
        }
        finally {
          if (reader != null) {
            try { reader.close(); } catch (IOException exc) {
              // ignore
            }
          }
        }

        if (importedFavoritesCount == 0) {
          msg = LOCALIZER.msg("error.2", "There are no new favorites in {0}.",
                               file.getAbsolutePath());
          JOptionPane.showMessageDialog(this, msg);
        } else {
          // Scroll to the end
          if (mFavoritesListModel != null) {
            mFavoritesList.ensureIndexIsVisible(mFavoritesListModel.size() - 1);
            // Select the first new favorite
            int firstNewIdx = mFavoritesListModel.size() - importedFavoritesCount;
            mFavoritesList.setSelectedIndex(firstNewIdx);
            mFavoritesList.ensureIndexIsVisible(firstNewIdx);
          }
          msg = LOCALIZER.msg("importDone", "There were {0} new favorites imported.", importedFavoritesCount);
          JOptionPane.showMessageDialog(this, msg);
        }
      }
    }
  }

  public Favorite[] getFavorites() {
    Favorite[] favoriteArr = new Favorite[mFavoritesListModel.size()];
    mFavoritesListModel.copyInto(favoriteArr);
    return favoriteArr;
  }


  // inner class FavoriteListCellRenderer


  class FavoriteListCellRenderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus)
    {
      Component c = super.getListCellRendererComponent(list, value, index, isSelected,
          cellHasFocus);
      
      if (value instanceof Favorite && c instanceof JLabel) {
        Favorite fav = (Favorite)value;
        ((JLabel)c).setText(fav.getName() + " (" + (mShowNew ? fav.getNewPrograms().length : fav.getWhiteListPrograms().length) + ")");
        
        if(!fav.isValidSearch()) {
          c.setForeground(Color.orange);
          ((JLabel)c).setText("<html><strike>"+((JLabel)c).getText()+"</strike></html>");
        }
        else if(mShowNew && fav.getNewPrograms().length > 0 && !isSelected) {
          c.setForeground(Color.red);
        }
      }
      return c;
    }
  }
  
  @Override
  public void drop(JList<Favorite> source, JList<Favorite> target, int rows, boolean move) {
    UiUtilities.moveSelectedItems(target,rows,true);
    FavoritesPlugin.getInstance().updateRootNode(true);
  }
  
  @Override
  public void valueChanged(TreeSelectionEvent e) {
    favoriteSelectionChanged(true);
  }
  
  public void close() {
    if (mFavoriteTree != null) {
      mFavoriteTree.removeTreeSelectionListener(this);
    }
  }
  
  public boolean programListIsEmpty() {
    return mProgramList.getModel().getSize() < 1;
  }

  /**
   * Gets if this dialog shows the new found programs after data update.
   * @return <code>True</code> if this dialog shows the new found programs after data update.
   */
  public boolean isShowingNewFoundPrograms() {
    return mShowNew;
  }

  public void favoriteSelectionChanged() {
    favoriteSelectionChanged(false);
  }
  
  public void handleFavoriteEvent() {
    SwingUtilities.invokeLater(() -> {
      mFavoriteTree.updateUI();
      favoriteSelectionChanged();
    });
  }
  
  public void newFolder(FavoriteNode parent, Window partenWindow, String name) {
    mFavoriteTree.newFolder(parent,partenWindow,name);
  }

  @Override
  public void updatePersona() {
    if(Persona.getInstance().getHeaderImage() != null) {
      mBlackListChb.setForeground(Persona.getInstance().getTextColor());
    }
    else {
      mBlackListChb.setForeground(UIManager.getColor("Label.foreground"));
    }
  }
  
  public void setShowDateSeparators(boolean showDateSeparators) {
    mProgramListPanel.setShowDateSeparators(showDateSeparators);
  }
  
  public void registerPersonaListener() {
    Persona.getInstance().registerPersonaListener(mProgramListPanel);
    mProgramListPanel.updatePersona();
  }
  
  public void removePersonaListener() {
    Persona.getInstance().removePersonaListener(mProgramListPanel);
    mProgramListPanel.updatePersona();
  }
  
  public void scrollToDate(Date date) {
    mProgramList.scrollToNextDateIfAvailable(date);
  }
  
  public void scrollToNow() {
    mProgramListPanel.scrollToFirstNotExpiredIndex(false);
  }
  
  public void scrollToTime(int time, boolean scrollToNext) {
    if(scrollToNext) {
      mProgramList.scrollToFirstOccurrenceOfTimeFromCurrentViewOnwardIfAvailable(time);
    }
    else {
      mProgramList.scrollToTimeFromCurrentViewIfAvailable(time);
    }
  }
  
  public void selectFilter(ProgramFilter filter) {
    mProgramListPanel.selectFilter(filter);
  }
  
  public String getSelectedProgramFilterName() {
    return mProgramListPanel.getSelectedProgramFilterName();
  }
  
  @Override
  public void tabShown() {
    super.tabShown();
    scrollToFirstNotExpiredIndex(false);
    updateAndroidSyncImportButton();
  }
  
  private void updateAndroidSyncImportButton() {
    PluginProxy androidSync = PluginProxyManager.getInstance().getActivatedPluginForId("java.androidsync.AndroidSync");
    
    if(androidSync != null) {
      PluginCommunication c = androidSync.getCommunicationClass();
      mImportApp.setIcon(TVBrowserIcons.getMenuIcon(androidSync.getButtonAction(), Plugin.BIG_ICON));
      mImportApp.setEnabled(c != null && c.getVersion() >= 2);
    }
    else {
      mImportApp.setEnabled(false);
    }
  }
  
  private static final class ImportInfo {
    private final boolean mAdd;
    private final boolean mReplace;
    private final boolean mAsk;
    
    private ImportInfo(final boolean add, final boolean replace, final boolean ask) {
      mAdd = add;
      mReplace = replace;
      mAsk = ask;
    }
  }
}
