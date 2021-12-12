/*
* TV-Browser
* Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
*  $RCSfile$
*   $Source$
*     $Date: 2011-08-31 20:45:46 +0200 (Mi, 31 Aug 2011) $
*   $Author: ds10 $
* $Revision: 7107 $
*/
package webplugin;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFrame;

import compat.MenuCompat;
import compat.PluginCompat;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ContextMenuAction;
import devplugin.ContextMenuSeparatorAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;
import util.browserlauncher.Launch;
import util.paramhandler.ParamLibrary;
import util.paramhandler.ParamParser;
import util.program.ProgramUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;

/**
 * This Plugin is a generic Web-Tool.
 * A User can configure his favorite Search-Engines and search for the given Movie
 */
public class WebPlugin extends Plugin {
  private static final Version VERSION = new Version(3,21);

  private static final Logger LOGGER = java.util.logging.Logger
  .getLogger(WebPlugin.class.getName());

  private static final String CHANNEL_SITE = "channelSite";
  private static final String PROGRAM_SITE = "programSite";
  private static final String SITE_VOD = "vodSite";

  private int mLastKnownId = 0;
  
/** Localizer */
  private static final Localizer LOCALIZER = Localizer
      .getLocalizerFor(WebPlugin.class);

  /** parameter to be replaced by all searchable strings */
  private static final String WEBSEARCH_ALL = "anytext";

  private static int ID_DEFAULT_NEXT = Integer.MIN_VALUE;
  
  /** Default-Addresses */
  final static WebAddress[] DEFAULT_ADRESSES = {
      new WebAddress("OFDb",
          "http://www.ofdb.de/view.php?page=suchergebnis&Kat=All&SText={urlencode(" + WEBSEARCH_ALL + ", \"UTF-8\")}",
          "http://www.ofdb.de/view.php?page=suchergebnis&Kat=Titel&SText={urlencode(" + WEBSEARCH_ALL + ", \"UTF-8\")}",
          "http://www.ofdb.de/view.php?page=suchergebnis&Kat=Person&SText={urlencode(" + WEBSEARCH_ALL + ", \"UTF-8\")}",
          null, false, true, ID_DEFAULT_NEXT++),
      new WebAddress("IMDb", 
          "http://imdb.com/find?q={urlencode(" + WEBSEARCH_ALL + ", \"UTF-8\")}", 
          "http://imdb.com/find?q={urlencode(" + WEBSEARCH_ALL + ", \"UTF-8\")}&s=tt", 
          "http://imdb.com/find?q={urlencode(" + WEBSEARCH_ALL + ", \"UTF-8\")}&s=nm", 
          null, false, true, ID_DEFAULT_NEXT++),
      new WebAddress("DuckDuckGo", "https://duckduckgo.com/html?q={urlencode(" + WEBSEARCH_ALL + ", \"UTF-8\")}", null, false, true, ID_DEFAULT_NEXT++),
      new WebAddress("Google", "http://www.google.com/search?q=%22{urlencode(" + WEBSEARCH_ALL + ", \"UTF-8\")}%22", null, false, true, ID_DEFAULT_NEXT++),
      new WebAddress("Yahoo", "http://search.yahoo.com/search?p={urlencode(" + WEBSEARCH_ALL + ", \"ISO-8859-1\")}", null, false, true, ID_DEFAULT_NEXT++),
      new WebAddress("Wikipedia (DE)",
          "http://de.wikipedia.org/wiki/Spezial:Search?search={urlencode(" + WEBSEARCH_ALL + ", \"ISO-8859-1\")}",
          null, false, Locale.getDefault().equals(Locale.GERMAN), ID_DEFAULT_NEXT++),
      new WebAddress("Wikipedia (EN)",
          "http://en.wikipedia.org/wiki/Special:Search?search={urlencode(" + WEBSEARCH_ALL + ", \"ISO-8859-1\")}",
          null, false, Locale.getDefault().equals(Locale.ENGLISH), ID_DEFAULT_NEXT++),
      new WebAddress("moviepilot", 
          "http://www.moviepilot.de/suche?q={urlencode(" + WEBSEARCH_ALL + ", \"UTF-8\")}", 
          "http://www.moviepilot.de/suche?q={urlencode(" + WEBSEARCH_ALL + ", \"UTF-8\")}&type=movie", 
          "http://www.moviepilot.de/suche?q={urlencode(" + WEBSEARCH_ALL + ", \"UTF-8\")}&type=person",          
          null, false, true, ID_DEFAULT_NEXT++),
      new WebAddress("omdb", 
          "http://www.omdb.org/search?search%5Btext%5D={urlencode(" + WEBSEARCH_ALL + ", \"UTF-8\")}", 
          "http://www.omdb.org/search/movies?search%5Btext%5D={urlencode(" + WEBSEARCH_ALL + ", \"UTF-8\")}", 
          "http://www.omdb.org/search/people?search%5Btext%5D={urlencode(" + WEBSEARCH_ALL + ", \"UTF-8\")}",
          null, false, true, ID_DEFAULT_NEXT++),
      new WebAddress(LOCALIZER.msg("programPage", "Open website of program"),PROGRAM_SITE,null,false,true, ID_DEFAULT_NEXT++),
      new WebAddress(LOCALIZER.msg("vodPage", "Open VOD link"),SITE_VOD,null,false,true, ID_DEFAULT_NEXT++),
      new WebAddress(LOCALIZER.msg("channelPageGeneral", "Open website of channel"),CHANNEL_SITE,null,false,true, ID_DEFAULT_NEXT++),
  };

  /** The WebAddresses */
  private ArrayList<WebAddress> mAddresses;

  private boolean mHasRightToDownload = false;

  private static WebPlugin INSTANCE;

  /** list of items to be searched if any searchable item shall be put into the context menu */
  private ArrayList<String> listActors = null;
  private ArrayList<String> listScripts = null;
  private ArrayList<String> listDirectors = null;

  private PluginInfo mPluginInfo;

  /**
   * show all available search items in menu, not only title search
   */
  private boolean mShowDetails = true;

  private boolean mTvb423 = false;
  /**
   * Creates the Plugin
   */
  public WebPlugin() {
    INSTANCE = this;
  }
  
  @Override
	public void onActivation() {
	  mTvb423 = getPluginManager().getTVBrowserVersion().compareTo(new Version(4,23,true)) == 0;
	}

  /**
   * Returns the Instance of the Plugin
   * @return Plugin-Instance
   */
  public static WebPlugin getInstance() {
    return INSTANCE;
  }

  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("actions", "web-search", 16);
  }

  public static Version getVersion() {
    return VERSION;
  }

  /**
   * Returns the Plugin-Info
   */
  public PluginInfo getInfo() {
    if(mPluginInfo == null) {
      mPluginInfo = new PluginInfo(WebPlugin.class, LOCALIZER.msg("name", "WebPlugin"),
          LOCALIZER.msg("desc","Searches on the Web for a Program"),
          "Bodo Tasche");
    }

    return mPluginInfo;
  }

  /**
   * Loads the Data
   */
  public void readData(final ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    mAddresses = new ArrayList<WebAddress>();

    final int version = in.readInt();

    final int size = in.readInt();

    final ArrayList<WebAddress> defaults = new ArrayList<WebAddress>(Arrays
        .asList(DEFAULT_ADRESSES));

    for (int i = 0; i < size;i++) {
      WebAddress newone = new WebAddress(in);

      if (!newone.isUserEntry()) {
        for (int v = 0; v < defaults.size(); v++) {
          final WebAddress def = defaults.get(v);
          // Replace Default Webaddresses with Default Settings
          if (def.getName().equals(newone.getName()) || def.getUrl().equals(newone.getUrl())) {
            // Copy needed Data
            def.setActive(newone.isActive());
            def.setIconFile(newone.getIconFile());
            newone = def;
            defaults.remove(v);
          }
        }
      }

      mAddresses.add(newone);
    }

    for (int i = 0; i < defaults.size();i++) {
      mAddresses.add(defaults.get(i));
    }

    if (version >= 2) {
      mShowDetails = in.readBoolean();
    }
    
    if(version >= 3) {
      mLastKnownId = in.readInt();
    }
  }

  /**
   * Saves the Data
   */
  public void writeData(final ObjectOutputStream out) throws IOException {
    out.writeInt(3);
    if (mAddresses == null) {
      createDefaultSettings();
    }

    out.writeInt(mAddresses.size());

    for (int i = 0; i < mAddresses.size(); i++) {
      (mAddresses.get(i)).writeData(out);
    }

    out.writeBoolean(mShowDetails);
    out.writeInt(mLastKnownId);
  }

  /**
   * Creates the Settings-Tab
   */
  public SettingsTab getSettingsTab() {
    if (mAddresses == null) {
      createDefaultSettings();
    }
    return new WebSettingsTab((JFrame)getParentFrame(), mAddresses, this);
  }


  /**
   * Create the Default-Settings
   */
  private void createDefaultSettings() {
    mAddresses = new ArrayList<WebAddress>();
    mAddresses.addAll(Arrays.asList(DEFAULT_ADRESSES));
  }

  /**
   * Creates the Context-Menu-Entries
   */
  public ActionMenu getContextMenuActions(final Program program) {
    if (mAddresses == null) {
      createDefaultSettings();
    }
    Action mainAction = getMainContextMenuAction();
    final boolean isExampleProgram = program.equals(getPluginManager().getExampleProgram());
  /*  if (program == getPluginManager().getExampleProgram()) {
    	return new ActionMenu(mainAction);
    }*/

    final String programPage = LOCALIZER.msg("programPage", "Open page of program");
    final ArrayList<ActionMenu> actionList = new ArrayList<ActionMenu>();
    listActors = null;
    
    for (int i = 0; i < mAddresses.size(); i++) {
     try {
    	  WebAddress address = mAddresses.get(i);
        String actionName = LOCALIZER.msg("SearchOn", "Search on ") + " " + address.getName();

        if (address.getUrl().equals(PROGRAM_SITE)) {
          final String url = program.getTextField(ProgramFieldType.URL_TYPE);
          if (url != null && url.length() > 0) {
            address = new WebAddress(programPage,url,null,false,address.isActive(),MenuCompat.ID_ACTION_NONE);
            actionName = address.getName();
          }
          else {
            address = null;
          }
        }
        // create address of channel on the fly
        if (address != null && address.getUrl().equals(CHANNEL_SITE)) {
        	final Channel channel = program.getChannel();
          address = new WebAddress(LOCALIZER.msg("channelPage",
              "Open page of {0}", channel.getName()), isExampleProgram ? "DUMMY" : channel.getWebpage(),
              null, false, address.isActive(), mAddresses.get(i).getMenuId());
        	actionName = address.getName();
/*
        	// automatically add separator if it is the last menu item (as it is by default)
        	if (i == mAddresses.size() - 1) {
        	  actionList.add(ContextMenuSeparatorAction.getInstance());
        	}
*/
        }
        
        if (address != null && address.getUrl() != null && address.getUrl().equals(SITE_VOD)) {
          try {
            Field mediathekLink = ProgramFieldType.class.getDeclaredField("VOD_LINK");
            String link = program.getTextField((ProgramFieldType)mediathekLink.get(null));
            
            if(link == null && isExampleProgram) {
              link = "DUMMY";
            }
            
            if(link != null) {
              address = new WebAddress(LOCALIZER.msg("vodPage",
                  "Open VOD link"), link,
                  null, false, address.isActive(), mAddresses.get(i).getMenuId());
              actionName = address.getName();
            }
            else {
              address = null;
            }
          } catch (Exception e) {
            address = null;
          }
        }
        
        if (address != null && address.getUrl() != null && address.isActive()) {
          // create items for a possible sub menu
          if (address.getUrl().contains(WEBSEARCH_ALL) && listActors == null) {
            findSearchItems(program);
          }
          if (address.getUrl().contains(WEBSEARCH_ALL) && (listActors.size() + listDirectors.size() + listScripts.size() > 0) && mShowDetails) {
            final ArrayList<ActionMenu> categoryList = new ArrayList<ActionMenu>();
            // title
            final WebAddress adrTitle = new WebAddress(address.getName(), address.getUrl(WebAddress.MOVIE_SEARCH).replace(WEBSEARCH_ALL, "\"" + program.getTitle() + "\""), null, false, true, mAddresses.get(i).getMenuId());
            categoryList.add(createSearchAction(program, adrTitle, program.getTitle()));
            
            if(isExampleProgram) {
              Action a = categoryList.get(categoryList.size()-1).getAction();
              a.putValue(Action.NAME, LOCALIZER.msg("SearchOn", "Search on ")+" "+mAddresses.get(i).getName());
              a.putValue(Action.SMALL_ICON, address.getIcon());
            }
            
            String orgTitle = program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE);
            if (orgTitle != null && !orgTitle.equals(program.getTitle())) {
              final WebAddress adrOrgTitle = new WebAddress(address.getName(), address.getUrl(WebAddress.MOVIE_SEARCH).replace(WEBSEARCH_ALL, "\"" + orgTitle + "\""), null, false, true, MenuCompat.ID_ACTION_NONE);
              ActionMenu orgTitleAction = createSearchAction(program, adrOrgTitle, "("+orgTitle+")");
              orgTitleAction.getAction().putValue(Plugin.DISABLED_ON_TASK_MENU, true);
              categoryList.add(orgTitleAction);
            }
            categoryList.add(new ActionMenu(ContextMenuSeparatorAction.getDisabledOnTaskMenuInstance()));
            createSubMenu(program, address, categoryList, LOCALIZER.msg("actor", "Actor"), listActors, WebAddress.PERSON_SEARCH);
            createSubMenu(program, address, categoryList, LOCALIZER.msg("director","Director"), listDirectors, WebAddress.PERSON_SEARCH);
            createSubMenu(program, address, categoryList, LOCALIZER.msg("script","Script"), listScripts, WebAddress.PERSON_SEARCH);
            if (categoryList.size() == 2) {
              categoryList.remove(1);
            }

            final ActionMenu searchMenu = MenuCompat.createActionMenu(MenuCompat.ID_ACTION_NONE, actionName, address.getIcon(), categoryList.toArray(new ActionMenu[0]));
            actionList.add(searchMenu);
          }
          else if (address.getName().equals(programPage) && address.getUrl().contains("\n")) {
            final String[] urls = address.getUrl().split("\n+");
            
            final ActionMenu[] subActions = new ActionMenu[urls.length];
            
            for(int j = 0; j < urls.length; j++) {
              final WebAddress link = new WebAddress(urls[j], urls[j], null, false, true, j == 0 ? mAddresses.get(i).getMenuId() : MenuCompat.ID_ACTION_NONE);
              
              subActions[j] = createSearchAction(program, link, link.getName());
              
              if(isExampleProgram) {
                Action a = subActions[j].getAction();
                a.putValue(Action.NAME, LOCALIZER.msg("SearchOn", "Search on ") + " " + mAddresses.get(i).getName());
                a.putValue(Action.SMALL_ICON, address.getIcon());
              }
            }
            
            final ActionMenu searchMenu = new ActionMenu(actionName, address.getIcon(), subActions);
            actionList.add(searchMenu);
          }
          // create only a single menu item for this search
          else {
            final WebAddress adrTitle = new WebAddress(address.getName(), address.getUrl(WebAddress.MOVIE_SEARCH).replace(WEBSEARCH_ALL, "\"" + program.getTitle() + "\""), null, address.isUserEntry(), true, mAddresses.get(i).getMenuId());
            final ActionMenu action = createSearchAction(program, adrTitle,
                actionName);
            action.getAction().putValue(Action.SMALL_ICON, address.getIcon());
            
            if(isExampleProgram && adrTitle.isUserEntry()) {
              action.getAction().putValue(Action.NAME, LOCALIZER.msg("SearchOn", "Search on ") + " " + mAddresses.get(i).getName());
            }
            
            actionList.add(action);
          }
        }
      } catch (RuntimeException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    if (actionList.size() == 1) {
      final Object action = actionList.get(0);
      if (action instanceof ActionMenu) {
        return (ActionMenu) action;
      }
      else if (action instanceof Action) {
        return new ActionMenu((Action)action);
      }
    }

    final Object[] actions = new Object[actionList.size()];
    actionList.toArray(actions);
    return new ActionMenu((String)mainAction.getValue(Action.NAME), (Icon)mainAction.getValue(Action.SMALL_ICON), actions);
  }

	private Action getMainContextMenuAction() {
		final Action mainAction = new devplugin.ContextMenuAction();
    mainAction.putValue(Action.NAME, LOCALIZER.msg("contextMenu", "Web search"));
    mainAction.putValue(Action.SMALL_ICON, createImageIcon("actions", "web-search", 16));
		return mainAction;
	}

  private void createSubMenu(final Program program, final WebAddress address,
      final ArrayList<ActionMenu> categoryList, final String label,
      final ArrayList<String> subItems, final int searchType) {
    if (subItems.size() > 0) {
      ActionMenu[] subActions = new ActionMenu[subItems.size()];
      for (int index = 0; index < subActions.length; index++) {
        final WebAddress modifiedAddress = new WebAddress(address.getName(), address.getUrl(searchType).replace(WEBSEARCH_ALL, "\"" + subItems.get(index) + "\""), null, false, true, program.equals(getPluginManager().getExampleProgram()) ? MenuCompat.ID_ACTION_NONE : address.getMenuId());
        subActions[index] = createSearchAction(program, modifiedAddress, subItems.get(index));
        subActions[index].getAction().putValue(Plugin.DISABLED_ON_TASK_MENU, true);
      }
      if (subItems.size() > 1) {
        final ContextMenuAction menuAction = new ContextMenuAction(label);
        final ActionMenu menu = new ActionMenu((String)menuAction.getValue(Action.NAME), (Icon)menuAction.getValue(Action.SMALL_ICON), subActions);
        menu.getAction().putValue(Plugin.DISABLED_ON_TASK_MENU, true);
        categoryList.add(menu);
      }
      else {
        subActions[0].getAction().putValue(Action.NAME, subActions[0].getAction().getValue(Action.NAME) + " (" + label +")");
        categoryList.add(subActions[0]);
      }
    }
  }

  private ActionMenu createSearchAction(final Program program,
      final WebAddress address, final String actionName) {
    final WebAddress adr = address;
    final AbstractAction action = new AbstractAction() {

      public void actionPerformed(final ActionEvent evt) {
        openUrl(program, adr);
      }
    };
    action.putValue(Action.NAME, actionName);
    
    return MenuCompat.createActionMenu(address.getMenuId(), action);
  }

  private void findSearchItems(final Program program) {
    listActors = new ArrayList<String>();
    listDirectors = new ArrayList<String>();
    listScripts = new ArrayList<String>();
    // director
    final String directorField = program
        .getTextField(ProgramFieldType.DIRECTOR_TYPE);
    if (directorField != null) {
      final String[] directors = directorField.split(",");
      for (String director : directors) {
        addSearchItem(listDirectors, director);
      }
    }
    // script
    final String scriptField = program
        .getTextField(ProgramFieldType.SCRIPT_TYPE);
    if (scriptField != null) {
      final String[] scripts = scriptField.split(",");
      for (String script : scripts) {
        addSearchItem(listScripts, script);
      }
    }
    // actors
    final String[] actors = ProgramUtilities.getActorNames(program);
    if (actors != null) {
      Arrays.sort(actors);
      listActors = new ArrayList<String>();
      // build the final list of sub menus
      for (String actor : actors) {
        if (actor.contains(" ") && !actor.equalsIgnoreCase("und andere") && !listActors.contains(actor)) {
          addSearchItem(listActors, actor);
        }
      }
    }
  }

  private void addSearchItem(final ArrayList<String> list, String search) {
    if (search != null) {
      // remove additional bracket parts from script and director fields
      final int leftBracket = search.indexOf('(');
      final int rightBracket = search.lastIndexOf(')');
      if (leftBracket > 0 && rightBracket > leftBracket) {
        search = search.substring(0, leftBracket);
      }
      search = search.trim();
      if (search.length() > 0) {
        list.add(search);
      }
    }
  }

  public boolean canReceiveProgramsWithTarget() {
    return true;
  }

  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    final ArrayList<ProgramReceiveTarget> list = new ArrayList<ProgramReceiveTarget>();

    for (int i = 0; i < mAddresses.size(); i++) {
      final WebAddress adr = mAddresses.get(i);

      if (adr.isActive() && !(adr.getUrl().equals(PROGRAM_SITE) ||
            adr.getUrl().equals(SITE_VOD) ||
            adr.getUrl().equals(CHANNEL_SITE))) {
        list.add(new ProgramReceiveTarget(this,LOCALIZER.msg("SearchOn", "Search on ") + " " + adr.getName(),adr.getName() + "." + adr.getUrl()));
      }
    }

    return list.toArray(new ProgramReceiveTarget[list.size()]);
  }

  public boolean receiveValues(final String[] values,
      final ProgramReceiveTarget target) {
    for (int i = 0; i < mAddresses.size(); i++) {
      final WebAddress adr = mAddresses.get(i);

      if (adr.isActive() && target.isReceiveTargetWithIdOfProgramReceiveIf(this,adr.getName() + "." + adr.getUrl())) {
        for(String value : values) {
          try {
            final String url = adr.getUrl().replaceAll("[{].*[}]",
                URLEncoder.encode(value, "UTF-8").replace("+", "%20"));

            if(url.startsWith("http://")) {
              Launch.openURL(url);
            }
          } catch (UnsupportedEncodingException e) {}
        }

        return true;
      }
    }

    return false;
  }

  public boolean receivePrograms(final Program[] programArr, final ProgramReceiveTarget target) {
    for (int i = 0; i < mAddresses.size(); i++) {
      final WebAddress adr = mAddresses.get(i);

      if (adr.isActive() && target.isReceiveTargetWithIdOfProgramReceiveIf(this,adr.getName() + "." + adr.getUrl())) {
        for(Program p : programArr) {
          openUrl(p, adr, p.getTitle());
        }

        return true;
      }
    }

    return false;
  }

  protected void openUrl(final Program program, WebAddress address, final String search) {
    if(address.getUrl().contains(WEBSEARCH_ALL)) {
      address = new WebAddress(address.getName(), address.getUrl(WebAddress.MOVIE_SEARCH).replace(WEBSEARCH_ALL, "\"" + program.getTitle() + "\""), null, false, true, MenuCompat.ID_ACTION_NONE);
    }
    
    openUrl(program, address);
  }
  
  /**
   * Opens the Address in a browser
   * @param program Program to search on the Web
   * @param address Search-Engine to use
   */
  protected void openUrl(final Program program, final WebAddress address) {
    try {
      final ParamParser parser = new ParamParser();
      
      if(mTvb423) {
    	  parser.setParamLibrary(new ParamLibrary() {
    		  @Override
    		public String getStringForFunction(Program prg, String function, String[] params) {
    			String result = super.getStringForFunction(prg, function, params);
    			
    			if(function.equals("urlencode")) {
    				result = result.replace("%", "%%");
    			}
    			
    			return result;
    		}
    	  });
      }
      
      System.out.println(address.getUrl());
      
      final String result = parser.analyse(address.getUrl(), program);
      
      
      if (parser.hasErrors()) {
        final String errorString = parser.getErrorString();
        LOGGER.warning("URL parse error " + errorString+ " in " + address.getUrl());
        parser.showErrors(UiUtilities.getLastModalChildOf(getParentFrame()));
      } else {
        Launch.openURL(result);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void handleTvBrowserStartFinished() {
    mHasRightToDownload = true;
  }

  @Override
  public void handleTvDataUpdateFinished() {
    if(mHasRightToDownload) {
      final FavIconFetcher fetcher = new FavIconFetcher();

      if (mAddresses != null) {
        for (WebAddress address : mAddresses) {
          if ((address.getIconFile() == null) && ! address.getUrl().equals(CHANNEL_SITE) && ! address.getUrl().equals(PROGRAM_SITE)) {
            final String file = fetcher.fetchFavIconForUrl(address.getUrl());
            if (file != null) {
              address.setIconFile(file);
            } else {
              address.setIconFile("");
            }
          }
        }
      }

    }
  }

  protected boolean getShowDetailMenus() {
    return mShowDetails;
  }

  protected void setShowDetailMenus(final boolean showDetails) {
    mShowDetails = showDetails;
  }
  
  public String getPluginCategory() {
    return PluginCompat.CATEGORY_REMOTE_CONTROL_SOFTWARE;
  }
  
  int getNextMenuId() {
    return mLastKnownId++;
  }
  
  int getMenuIdForDefault(final String name) {
    int result = MenuCompat.ID_ACTION_NONE;
    
    for(WebAddress a : DEFAULT_ADRESSES) {
      if(name.equals(a.getName())) {
        result = a.getMenuId();
        break;
      }
    }
    
    return result;
  }
}