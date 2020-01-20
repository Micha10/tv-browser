package printplugin.util;

import devplugin.Date;
import devplugin.ProgramFieldType;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

@SuppressWarnings("nls")
public final class Utils {

  public static final int DPI = 72;
  public static final double INCH_IN_MM = 25.4;
  public static final double CENTIMETER = 10 * DPI / INCH_IN_MM;

  Utils() {}

  @SuppressWarnings("boxing")
  public static String encodeFont(final Font chosenFont) {
    return String.format(Locale.US, "%s-%s-%d", chosenFont.getFamily(), extractStyle(chosenFont),
        chosenFont.getSize());
  }

  public static double mm(final double px) {
    return px * INCH_IN_MM / DPI;
  }

  public static String extractStyle(final Font font) {
    final String style;
    if (font.getStyle() == Font.BOLD) {
      style = "BOLD";
    } else if (font.getStyle() == Font.ITALIC) {
      style = "ITALIC";
    } else if (font.getStyle() == (Font.BOLD | Font.ITALIC)) {
      style = "BOLDITALIC";
    } else {
      style = "PLAIN";
    }
    return style;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Component> T findFirst(final Class<T> type, final Component root) {
    T result = null;
    if (type.isInstance(root)) {
      result = (T) root;
    } else if (root instanceof Container) {
      for (Component component : ((Container) root).getComponents()) {
        if (component instanceof Container) {
          component = findFirst(type, component);
        }
        if (type.isInstance(component)) {
          result = (T) component;
          break;
        }
      }
    }
    return result;
  }

  public static void setOpaque(final JComponent parent, final boolean opaque) {
    if (parent == null) {
      return;
    }
    synchronized (parent.getTreeLock()) {
      if (parent instanceof JPanel || parent instanceof JRadioButton || parent instanceof JCheckBox) {
        parent.setOpaque(opaque);
      }
      for (Component component : parent.getComponents()) {
        if (component instanceof JComponent) {
          setOpaque((JComponent) component, opaque);
        }
      }
    }
  }

  @SuppressWarnings("boxing")
  public static int[] getPrimes(final int val) {
    final List<Integer> list = new ArrayList<>();
    for (int i = 1; i <= val / 2; i++) {
      if (val % i == 0) {
        list.add(i);
      }
    }
    return list.stream().mapToInt(i -> i).toArray();
  }

  @SuppressWarnings("boxing")
  public static Integer[] createIntegerArray(int from, int cnt) {
    Integer[] result = new Integer[cnt];
    for (int i = 0; i < result.length; i++) {
      result[i] = i + from;
    }
    return result;
  }

  @SuppressWarnings("boxing")
  public static Integer[] createIntegerArray(int from, int to, int step) {
    Integer[] result = new Integer[(to - from) / step + 1];
    int cur = from;
    for (int i = 0; i < result.length; i++) {
      result[i] = cur;
      cur += step;
    }
    return result;
  }

  public static Date[] createDateObjects(final int days) {
    Date[] result = new Date[days];
    Date today = Date.getCurrentDate();

    for (int i = 0; i < result.length; i++) {
      result[i] = today.addDays(i);
    }

    return result;
  }

  public static double getFactor() {
    return Toolkit.getDefaultToolkit().getScreenResolution() / 72.0;
  }

  @SuppressWarnings("boxing")
  public static String getCopyrightFooter() {
    return String.format(Locale.getDefault(), "\u00A9 %d\u2013%d TV-Browser (https://www.tvbrowser.org)", 2003,
        Calendar.getInstance().get(Calendar.YEAR));
  }

  public static Component getWidestComponent(final Component... components) {
    if (components == null) {
      return null;
    }
    int lastWidth = 0;
    Component lastComponent = null;
    for (final Component component : components) {
      final Dimension dimension = component.getPreferredSize();
      if (dimension.width > lastWidth) {
        lastWidth = dimension.width;
        lastComponent = component;
      }
    }
    return lastComponent;
  }

  public static void alignWidth(final int width, final Component... components) {
    if (components == null) {
      return;
    }
    for (final Component component : components) {
      final Dimension dimension = component.getPreferredSize();
      dimension.width = width;
      // component.setMinimumSize(dimension);
      // component.setPreferredSize(dimension);
      component.setSize(dimension);
    }
  }

  public static void alignWidth(final Component... components) {
    if (components == null) {
      return;
    }
    final Component widestComponent = getWidestComponent(components);
    alignWidth(widestComponent.getPreferredSize().width, components);
  }

  /**
   * Registers supported fonts from the resource folder /printplugin/fonts/,
   * or from archives within the folder (.jar, .zip).
   *
   * @param fontInfo
   *                   a list of {@link FontInfo} entries for successful loaded
   *                   and registered fonts.
   */
  public static void registerFontsAsync(final List<FontInfo> fontInfo) {
    new Thread(() -> {
      if (Stream.of(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts())
          .filter(f -> f.getPSName().startsWith("RobotoCondensed-")).distinct().count() < 6) {
      }
      try {
        List<String> resources = getResources("/printplugin/fonts/");
        if (resources == null) {
          return;
        }

        for (String resource : resources) {
          if (resource == null) {
            continue;
          }

          final String name = resource.toLowerCase();
          if (name.endsWith(".jar") || name.endsWith(".zip")) {
            fontInfo.addAll(registerFontsFromArchive(resource));
            continue;
          }

          final int fontType = getFontTypeFromName(name);
          if (fontType >= -1) {
            try (InputStream in = getResourceAsStream(resource)) {
              final Font font = registerFont(fontType, in);
              if (font != null) {
                System.err.println("Registered " + resource);
                fontInfo.add(new FontInfo(resource, font, fontType));
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }).start();
  }

  /**
   * Loads and registers a {@link Font} using the given {@link InputStream} and
   * type.
   *
   * @param fontType
   *                   the font format (one of {@link Font#TRUETYPE_FONT} or
   *                   {@link Font#TYPE1_FONT})
   * @param in
   *                   the {@link InputStream} to load from
   * @return the registered {@link Font} or <code>null</code> if the font could
   *           not be registered, i. e. is already installed on the local system
   * @see GraphicsEnvironment#registerFont(Font)
   * @throws FontFormatException
   *                               if the {@link InputStream} contains invalid
   *                               data
   *                               or the format does not match
   * @throws IOException
   *                               if reading has failed due an I/O exception
   */
  private static Font registerFont(final int fontType, final InputStream in)
      throws FontFormatException, IOException {
    final Font font = Font.createFont(fontType, in);
    return GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font) ? font : null;
  }

  private static int getFontTypeFromName(final String name) {
    final int fontType;
    if (name.endsWith(".otf") || name.endsWith(".ttf")) {
      fontType = Font.TRUETYPE_FONT;
    } else if (name.endsWith(".pfa") || name.endsWith(".pfb") || name.endsWith(".pfm")
        || name.endsWith(".afm")) {
      fontType = Font.TYPE1_FONT;
    } else {
      fontType = -1;
    }
    return fontType;
  }

  /**
   * Tries to load and register all fonts from a given JAR or ZIP archive
   * identified by its resource name.
   *
   * @param resource
   *                   the resource name of the archive containing the fonts
   */
  private static List<FontInfo> registerFontsFromArchive(final String resource) {
    final List<FontInfo> fontInfo = new ArrayList<>(6);
    try (final ZipInputStream in = new ZipInputStream(getResourceAsStream(resource))) {
      ZipEntry entry;
      while ((entry = in.getNextEntry()) != null) {
        try {
          if (!entry.isDirectory()) {
            final int fontType = getFontTypeFromName(entry.getName().toLowerCase());
            if (fontType >= 0) {
              final Font font = registerFont(fontType, in);
              if (font != null) {
                System.err.println("Registered " + entry.getName() + " from " + resource);
                fontInfo.add(new FontInfo(resource + "!" + entry.getName(), font, fontType));
              }
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          in.closeEntry();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return fontInfo;
  }

  /**
   * Returns a list of absolute classpath entries for a given path (this can
   * include sub-folder/sub-package names, but not their children).
   *
   * @param path
   *               the package or folder that contains the resource entries
   * @returns list of {@link String} representing the absolute class path entries
   *            for a given package.
   */
  private static List<String> getResources(String path) throws IOException {
    final List<String> resources = new ArrayList<>();
    if (path != null) {
      if (!path.endsWith("/")) {
        path = path.concat("/");
      }
      try (BufferedReader br = new BufferedReader(
          new InputStreamReader(getResourceAsStream(path)))) {
        String resource;
        while ((resource = br.readLine()) != null) {
          resources.add(path.concat(resource));
        }
      }
    }
    return resources;
  }

  /**
   * Returns an {@link InputStream} for a given resource name. In case of a folder
   * or package,
   * the stream contains the entries as line separated character stream.
   *
   * @param resource
   *                   the resource path to read from
   * @return an {@link InputStream} of the given resource
   * @see #getResources(String)
   */
  private static InputStream getResourceAsStream(final String resource) {
    final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
    return in == null ? Utils.class.getResourceAsStream(resource) : in;
  }

  /**
   * Information of a loaded and registered custom font.
   */
  public static class FontInfo implements Serializable {

    private static final long serialVersionUID = 6342927145231372858L;

    /**
     * The local resource path (source) of the font file.
     */
    public final String resource;
    /**
     * The loaded and registered custom font.
     */
    public final Font font;
    /**
     * The font format (one of {@link Font#TRUETYPE_FONT} or
     * {@link Font#TYPE1_FONT}).
     */
    public final int type;

    /**
     * Private constructor.
     */
    private FontInfo(final String resource, final Font font, final int type) {
      this.resource = resource;
      this.font = font;
      this.type = type;
    }
  }

  public static ProgramFieldType[] getAvailableTypes() {
    final List<ProgramFieldType> typeList = new ArrayList<>();

    final Iterator<ProgramFieldType> typeIter = ProgramFieldType.getTypeIterator();
    while (typeIter.hasNext()) {
      final ProgramFieldType type = typeIter.next();
      if (type.getFormat() != ProgramFieldType.FORMAT_BINARY
          && type != ProgramFieldType.INFO_TYPE
          && type != ProgramFieldType.START_TIME_TYPE
          && type != ProgramFieldType.END_TIME_TYPE
          && type != ProgramFieldType.TITLE_TYPE) {
        typeList.add(type);
      }
    }

    final ProgramFieldType[] typeArr = new ProgramFieldType[typeList.size()];
    typeList.toArray(typeArr);
    return typeArr;
  }
}