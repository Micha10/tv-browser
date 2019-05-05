package imdbplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.Program;
import devplugin.ProgramFieldType;

public class ImdbRatingsDialog extends JDialog implements WindowClosingIf {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ImdbRatingsDialog.class);

  private Program mProgram;
  private ImdbRating mRating;

  public ImdbRatingsDialog(final JDialog parent, final Program program, final ImdbRating rating) {
    super(parent);
    initialize(program, rating);
  }

	public ImdbRatingsDialog(final JFrame parent, final Program program, final ImdbRating rating) {
		super(parent);
    	initialize(program, rating);
	}

	private void initialize(final Program prog, final ImdbRating rating) {
		setModal(true);
		mProgram = prog;
		mRating = rating;
		createGui();
		UiUtilities.registerForClosing(this);
	}

  private void createGui() {
    setTitle(mLocalizer.msg("title", "IMDb Rating for {0}", mProgram.getTitle()));

    CellConstraints cc = new CellConstraints();
    final FormLayout layout = new FormLayout("fill:min:grow");
    final PanelBuilder panel = new PanelBuilder(layout, (JPanel) getContentPane());

    panel.setBorder(Borders.DLU4_BORDER);

    JComponent mainComponent;

    if (mRating.isEpisode() && (mRating.getSeriesId() != null) && (mRating.getMovieId().compareTo(mRating.getSeriesId()) != 0)) {
    	String seriesTitle = mProgram.getTitle();
    	String episodeTitle = mProgram.getTextField(ProgramFieldType.EPISODE_TYPE);
    	
    	//sometimes the title and/or the original title is "<series name> - <episode name>"
    	if ((episodeTitle == null) || (episodeTitle.length() < 1)) {
    		seriesTitle = ImdbPlugin.getInstance().getDatabase().getSeriesFromTitle(mProgram);
    		episodeTitle = ImdbPlugin.getInstance().getDatabase().getEpisodeFromTitle(mProgram);
    	}
    	
    	ImdbRating seriesRating = ImdbPlugin.getInstance().getDatabase().getRatingForId(mRating.getSeriesId());
		JTabbedPane pane = new JTabbedPane();
		JComponent editor = createEditor(seriesRating, seriesTitle, -1);
		pane.addTab(mLocalizer.msg("seriesRating", "Rating of Series"), editor);
		editor = createEditor(mRating, episodeTitle, ImdbPlugin.getInstance().getDatabase().getReleaseYear(mProgram));
		pane.addTab(mLocalizer.msg("episodeRating", "Rating of Episode"), editor);
		mainComponent = pane;
    } else {
		mainComponent = createEditor(mRating, mProgram.getTitle(), ImdbPlugin.getInstance().getDatabase().getReleaseYear(mProgram));
	}

    layout.appendRow(RowSpec.decode("fill:min:grow"));
    panel.add(mainComponent, cc.xy(1,panel.getRowCount()));

    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("pref"));

    JButton okButton = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    okButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });

    ButtonBarBuilder2 buttonBuilder = new ButtonBarBuilder2();
    buttonBuilder.addGlue();
    buttonBuilder.addButton(new JButton[] {okButton});

    panel.add(buttonBuilder.getPanel(), cc.xy(1,panel.getRowCount()));

    getRootPane().setDefaultButton(okButton);
    pack();
    UiUtilities.setSize(this, 500, 450);
    okButton.requestFocusInWindow();
  }

	private JComponent createEditor(final ImdbRating rating, final String title, final int releaseYear) {
	  return new JScrollPane(new ImdbRatingPanel(ImdbPlugin.getInstance().getDatabase().getMovieForId(rating.getMovieId(), title), rating, releaseYear));
	}

  public void close() {
    setVisible(false);
  }
}