package printplugin.util;

import devplugin.Date;
import devplugin.ProgramFieldType;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import util.misc.OperatingSystem;
import util.ui.Localizer;
import util.ui.UiUtilities;

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

  @SuppressWarnings("unchecked")
  public static <T extends Component> List<T> findAll(final Class<T> type, final Component root) {
    final List<T> result = new ArrayList<>();
    if (type.isInstance(root)) {
      result.add((T) root);
    }
    if (root instanceof Container) {
      for (Component component : ((Container) root).getComponents()) {
        if (component instanceof Container) {
          result.addAll(findAll(type, component));
        } else {
          result.add((T) component);
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
   * @return {@link InputStream} of the given resource
   */
  public static InputStream getResourceAsStream(final String resource) {
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

  public static void adjustButtonMargin(final JButton button, final int minMargin) {
    if (button == null) {
      return;
    }

    Insets insets = button.getMargin();
    if (insets == null) {
      insets = new Insets(minMargin, minMargin, minMargin, minMargin);
    } else {
      insets.bottom = insets.bottom < minMargin ? insets.bottom + minMargin -
          insets.bottom : insets.bottom;
      insets.left = insets.left < minMargin ? insets.left + minMargin - insets.left
          : insets.left;
      insets.right = insets.right < minMargin ? insets.right + minMargin -
          insets.right : insets.right;
      insets.top = insets.top < minMargin ? insets.top + minMargin - insets.top : insets.top;
    }
    button.setMargin(insets);
  }

  /**
   * Shows a {@link JOptionPane} based input dialog with localized option buttons.
   * <p>
   * Starting with Java version 11 the Oracle SDK does not support localization
   * for UI elements (Swing) anymore except Chinese and English; this is a
   * replacement.
   *
   * @param parent
   *                       the parent frame
   * @param title
   *                       the dialog's title
   * @param message
   *                       the dialog's message
   * @param initialValue
   *                       the initial value
   * @return the entered data, or <code>null</code>
   * @see JOptionPane#showInputDialog(Component, Object, String, int,
   *        javax.swing.Icon, Object[], Object)
   * @see JOptionPane#UNINITIALIZED_VALUE
   */
  public static Object showInputDialog(final Frame parent, final String title, final String message,
      final Object initialValue) {
    final JOptionPane optionPane = new JOptionPane(
        message,
        JOptionPane.PLAIN_MESSAGE,
        JOptionPane.OK_CANCEL_OPTION,
        null,
        new String[] {Localizer.getLocalization(Localizer.I18N_OK), Localizer.getLocalization(Localizer.I18N_CANCEL)},
        null);
    optionPane.setWantsInput(true);
    optionPane.setInitialSelectionValue(initialValue);
    optionPane.selectInitialValue();
    optionPane.createDialog(parent, title).setVisible(true);
    return optionPane.getInputValue() == JOptionPane.UNINITIALIZED_VALUE ? null : optionPane.getInputValue();
  }

  /**
   * Checks if the given text can be displayed with the given font. That is
   * <code>true</code> if the font contains glyphs to print all characters
   * of the string.
   *
   * @param str
   *               the text to check for non-printable characters
   * @param font
   *               the font that shall be used
   * @return <code>true</code> if the given string can be displayed with the given
   *           font
   * @see Font#canDisplayUpTo(String)
   */
  public static boolean isDisplayable(final String str, Font font) {
    boolean isDisplayable;
    if (OperatingSystem.isMacOs()) {
      try {
        isDisplayable = macCanDisplayUpTo(font, str) != -1;
      } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
          | InvocationTargetException e) {
        isDisplayable = font.canDisplayUpTo(str) != -1;
      }
    } else {
      isDisplayable = font.canDisplayUpTo(str) != -1;
    }
    return isDisplayable;
  }

  /**
   * Fix based on
   * https://stackoverflow.com/questions/17008081/font-candisplay-always-returns-true-on-mac/56065355#56065355
   *
   * @see #isDisplayable(String, Font)
   */
  @SuppressWarnings("boxing")
  private static int macCanDisplayUpTo(Font font, String str) throws NoSuchMethodException, SecurityException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Method getFontMethod = Font.class.getDeclaredMethod("getFont2D");
    getFontMethod.setAccessible(true);
    Object font2d = getFontMethod.invoke(font);
    Method getMapperMethod = font2d.getClass().getDeclaredMethod("getMapper");
    getMapperMethod.setAccessible(true);
    Object mapper = getMapperMethod.invoke(font2d);
    Method charToGlyphMethod = mapper.getClass().getDeclaredMethod("charToGlyph", char.class);

    int len = str.length();
    int i = 0;
    while (i < len) {
      char c = str.charAt(i);
      int glyph = (int) charToGlyphMethod.invoke(mapper, c);
      if (glyph >= 0) {
        i++;
        continue;
      }
      if (!Character.isHighSurrogate(c)
          || (int) charToGlyphMethod.invoke(mapper, str.codePointAt(i)) < 0) {
        return i;
      }
      i += 2;
    }
    return -1;
  }

  /*
   * Reads the textual contents of a given {@link InputStream} into a string.
   *
   * @param inputStream
   * stream to read from
   *
   * @return a string representation of the stream
   *
   * @throws IOException
   * on errors during reading
   *
   * public static String readAll(final InputStream inputStream) throws
   * IOException {
   * final ByteArrayOutputStream result = new ByteArrayOutputStream();
   * final byte[] buffer = new byte[8192];
   * int length;
   * while ((length = inputStream.read(buffer)) != -1) {
   * result.write(buffer, 0, length);
   * }
   * return result.toString(StandardCharsets.UTF_8.name());
   * }
   *
   * public static String readStringFromResource(final String filename) {
   * String s = null;
   * try (final InputStream in = getResourceAsStream(filename)) {
   * s = readAll(in);
   * } catch (Exception e) {
   * e.printStackTrace();
   * }
   * return s;
   * }
   */

  /**
   * Creates a translucent non-editable {@link JEditorPane} pane with a given
   * margin and loads the contents of
   * the provided {@link URL}.
   *
   * @param url
   *                 HTML content to load into the editor pane
   * @param margin
   *                 the margin around the HTML body
   * @return an editor pane inside a {@link JScrollPane}
   *
   */
  public static JScrollPane getHtmlPane(final URL url, final int margin) {

    final Color foreground = UIManager.getColor("Label.foreground");
    final Font font = UIManager.getFont("Label.font");

    final HTMLEditorKit kit = new PreWrapHTMLEditorKit();
    final StyleSheet styleSheet = kit.getStyleSheet();
    styleSheet.addRule("body {"
        + "margin: " + margin + "px"
        + "; color:" + UiUtilities.getHTMLColorCode(foreground)
        + "; font-family:" + font.getName()
        + "; font-size:" + font.getSize()
        + ";}");

    final JEditorPane pane = new JEditorPane();
    pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    pane.setBackground(null);
    pane.setBorder(BorderFactory.createEmptyBorder());
    pane.setContentType("text/html;charset=UTF-8");
    pane.setDocument(kit.createDefaultDocument());
    pane.setEditable(false);
    pane.setEditorKit(kit);
    pane.setFocusable(false);
    pane.setFont(font);
    pane.setForeground(foreground);
    pane.setOpaque(false);
    try {
      pane.setPage(url);
    } catch (IOException e) {
      pane.setContentType("text/plain");
      pane.setText(
          Localizer.getLocalization(Localizer.I18N_ERROR) + ": " + url + " (" + e.getLocalizedMessage() + ")");
      e.printStackTrace();
    }

    final JScrollPane scrollPane = new JScrollPane(pane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setBorder(null);
    scrollPane.getViewport().setOpaque(false);

    return scrollPane;
  }

  // Based on https://stackoverflow.com/a/3813605
  public static class PreWrapHTMLEditorKit extends HTMLEditorKit {

    private static final long serialVersionUID = 6075779031846652680L;

    final ViewFactory viewFactory = new HTMLFactory() {

      @Override
      public View create(final Element elem) {
        final AttributeSet attrs = elem.getAttributes();
        final Object elementName = attrs.getAttribute(AbstractDocument.ElementNameAttribute);
        final Object o = elementName != null ? null : attrs.getAttribute(StyleConstants.NameAttribute);
        if (o instanceof HTML.Tag) {
          final HTML.Tag kind = (HTML.Tag) o;
          if (kind == HTML.Tag.IMPLIED) {
            return new javax.swing.text.html.ParagraphView(elem);
          }
        }
        return super.create(elem);
      }
    };

    @Override
    public ViewFactory getViewFactory() {
      return viewFactory;
    }
  }
}