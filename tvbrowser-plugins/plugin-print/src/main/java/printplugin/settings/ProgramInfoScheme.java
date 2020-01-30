package printplugin.settings;

import devplugin.Plugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import printplugin.PrintPlugin;

import util.exc.ErrorHandler;
import util.io.stream.StreamUtilities;

@SuppressWarnings("nls")
public class ProgramInfoScheme extends Scheme<ProgramInfoPrintSettings> {

  private static final String SCHEME_FILE = "printplugin.programinfo.schemes";

  public ProgramInfoScheme(final String name) {
    super(name);
  }

  @Override
  protected void store(final ObjectOutputStream out) throws IOException {
    getSettings().writeData(out);
  }

  @Override
  protected void read(final ObjectInputStream in) throws IOException, ClassNotFoundException {
    final ProgramInfoPrintSettings settings = new ProgramInfoPrintSettings();
    settings.readData(in);
    setSettings(settings);
  }

  public static Scheme<? extends Settings>[] loadSchemes() {
    final String home = Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome();
    final File schemeFile = new File(home, SCHEME_FILE);
    try (ObjectInputStream in = new ObjectInputStream(
        new BufferedInputStream(new FileInputStream(schemeFile), 0x4000))) {
      return readSchemesFromStream(in);
    } catch (Exception e) {
      return getDefaultScheme();
    }
  }

  private static Scheme<? extends Settings>[] getDefaultScheme() {

    final ProgramInfoScheme scheme = new ProgramInfoScheme(
        PrintPlugin.mLocalizer.msg("defaultScheme", "Default Scheme"));
    final ProgramInfoPrintSettings oldProgramInfoPrintSettings = PrintPlugin.getInstance()
        .getOldProgramInfoPrintSettings();
    if (oldProgramInfoPrintSettings != null) {
      scheme.setSettings(oldProgramInfoPrintSettings);
    } else {
      scheme.setSettings(new ProgramInfoPrintSettings());
    }
    return new ProgramInfoScheme[] {scheme};
  }

  private static Scheme<? extends Settings>[] readSchemesFromStream(final ObjectInputStream in)
      throws IOException, ClassNotFoundException {
    in.readInt(); // read version
    final int cnt = in.readInt();
    final Scheme<ProgramInfoPrintSettings>[] schemes = new ProgramInfoScheme[cnt];
    for (int i = 0; i < cnt; i++) {
      final String name = (String) in.readObject();
      schemes[i] = new ProgramInfoScheme(name);
      schemes[i].read(in);
    }
    return schemes;
  }

  public static <S extends Settings> void storeSchemes(final Scheme<S>[] schemes) {
    final String home = Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome();
    final File schemeFile = new File(home, SCHEME_FILE);
    try {
      StreamUtilities.objectOutputStream(schemeFile,
          out -> {
            out.writeInt(1); // write version
            out.writeInt(schemes.length);
            for (final Scheme<S> scheme : schemes) {
              out.writeObject(scheme.getName());
              scheme.store(out);
            }
            out.close();
          });
    } catch (IOException e) {
      ErrorHandler.handle("Could not store program info scheme settings.", e);
    }
  }
}