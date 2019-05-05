package imdbplugin;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import util.ui.LinkButton;
import util.ui.Localizer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class ImdbRatingPanel extends JPanel {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ImdbRatingPanel.class);

  private ImdbRating rating;
  private ImdbMovie movie;
  private int releaseYear;

  public ImdbRatingPanel(final ImdbMovie movie, final ImdbRating rating, final int releaseYear) {
    this.rating = rating;
    this.movie = movie;
    this.releaseYear = releaseYear > 0 ? releaseYear : movie.getYear();
    createGui();
  }

  private void createGui() {
    setBackground(Color.WHITE);

    FormLayout layout = new FormLayout("fill:min:grow");

    setLayout(layout);
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    CellConstraints cc = new CellConstraints();

    JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    titlePanel.setBackground(Color.WHITE);

    JLabel title = new JLabel(movie.getTitle());
    title.setFont(title.getFont().deriveFont(20f).deriveFont(Font.BOLD));
    title.setForeground(Color.BLACK);
    titlePanel.add(title);

    JLabel year = new JLabel("(" + Integer.toString(releaseYear) + ")");
    year.setFont(year.getFont().deriveFont(20f).deriveFont(Font.BOLD));
    // year.setForeground(new Color(166, 166, 166));
    titlePanel.add(year);

    layout.appendRow(RowSpec.decode("pref"));
    add(titlePanel, cc.xy(1,layout.getRowCount()));
    layout.appendRow(RowSpec.decode("3dlu"));

    layout.appendRow(RowSpec.decode("pref"));
    JPanel diagramPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    diagramPanel.setBackground(Color.WHITE);
    diagramPanel.add(new RatingDiagram(rating));
    add(diagramPanel, cc.xy(1,layout.getRowCount()));
    layout.appendRow(RowSpec.decode("3dlu"));

    ImdbHistogram histogram = ImdbPlugin.getInstance().getHistogram();
    if (histogram != null && histogram.isValid()) {
      layout.appendRow(RowSpec.decode("pref"));
      JLabel percentile = new JLabel(mLocalizer.msg("percentile", "Better than {0} percent of all rated movies.", histogram.getPercentile(rating)));
      add(percentile, cc.xy(1,layout.getRowCount()));
      layout.appendRow(RowSpec.decode("10dlu"));
    }

    try {
        layout.appendRow(RowSpec.decode("pref"));
        String link = "https://www.imdb.com/title/" + URLEncoder.encode(rating.getMovieId(), "ISO-8859-1") + "/";
        LinkButton imdbLink = new LinkButton(mLocalizer.msg("imdbEntry","IMDb entry"), link);
        add(imdbLink, cc.xy(1,layout.getRowCount()));
        layout.appendRow(RowSpec.decode("10dlu"));
      } catch (UnsupportedEncodingException e) {
        //ignore
      }
    
    // originalTitle - original title, in the original language
    if (movie.getOriginalTitle() != null) {
	    layout.appendRow(RowSpec.decode("pref"));
	    JLabel originalTitle = new JLabel(mLocalizer.msg("originalTitle", "Original Title") + ":");
	    originalTitle.setToolTipText(mLocalizer.msg("originalTitleToolTip", "Original title, in the original language."));    
	    originalTitle.setForeground(Color.black);
	    originalTitle.setFont(originalTitle.getFont().deriveFont(Font.BOLD));
	    add(originalTitle, cc.xy(1,layout.getRowCount()));
	    layout.appendRow(RowSpec.decode("3dlu"));
	
	    layout.appendRow(RowSpec.decode("pref"));
	    JTextArea originalLabel = new JTextArea(movie.getOriginalTitle() + " (" + movie.getYear() + ")");
	    originalLabel.setEditable(false);
	    originalLabel.setBackground(Color.WHITE);
	    originalLabel.setForeground(Color.BLACK);
	    originalLabel.setFont(originalTitle.getFont().deriveFont(12f).deriveFont(Font.PLAIN));
	    add(originalLabel, cc.xy(1,layout.getRowCount()));
	    layout.appendRow(RowSpec.decode("3dlu"));
    }
    
    ImdbAka[] akas = movie.getAkas();

    if (akas.length > 0) {
      layout.appendRow(RowSpec.decode("pref"));

      JLabel alternativeHead = new JLabel(mLocalizer.msg("alternativeTitle", "Alternative Titles") + ":");
      alternativeHead.setToolTipText(mLocalizer.msg("alternativeTitleToolTip", "The localized titles."));    
      alternativeHead.setForeground(Color.black);
      alternativeHead.setFont(alternativeHead.getFont().deriveFont(Font.BOLD));

      add(alternativeHead, cc.xy(1,layout.getRowCount()));

      layout.appendRow(RowSpec.decode("3dlu"));
      layout.appendRow(RowSpec.decode("pref"));

      StringBuilder akaString = new StringBuilder();

      for (ImdbAka aka:akas) {
        if (akaString.length() > 0) {
          akaString.append(",\n");
        }
        akaString.append(aka.getTitle() + " (" + aka.getYear() + ")");
      }

      JTextArea akaLabel = new JTextArea(akaString.toString());
      akaLabel.setEditable(false);
      akaLabel.setBackground(Color.WHITE);
      akaLabel.setForeground(Color.BLACK);
      akaLabel.setFont(alternativeHead.getFont().deriveFont(12f).deriveFont(Font.PLAIN));

      add(akaLabel, cc.xy(1,layout.getRowCount()));
    }
  }
}