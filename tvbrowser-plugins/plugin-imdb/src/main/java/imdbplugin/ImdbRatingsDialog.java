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

  public ImdbRatingsDialog(final JDialog parent, final Program program) {
    super(parent);
    initialize(program);
  }

	public ImdbRatingsDialog(final JFrame parent, final Program program) {
    super(parent);
    initialize(program);
	}

	private void initialize(final Program prog) {
		setModal(true);
    mProgram = prog;
    createGui();
    UiUtilities.registerForClosing(this);
	}

  private void createGui() {
    setTitle(mLocalizer.msg("title", "IMDb Rating for {0}", mProgram.getTitle()));

    CellConstraints cc = new CellConstraints();
    final FormLayout layout = new FormLayout("fill:min:grow");
    final PanelBuilder panel = new PanelBuilder(layout, (JPanel) getContentPane());

    panel.setBorder(Borders.DLU4_BORDER);

    ImdbRating episodeRating = ImdbPlugin.getInstance().getDatabase().getEpisodeRating(mProgram);
    JComponent mainComponent;

    if (episodeRating != null) {
    	ImdbRating seriesRating = ImdbPlugin.getInstance().getDatabase().getSeriesRating(mProgram);
    	
    	if ((seriesRating != null) && (episodeRating.getMovieId().compareTo(seriesRating.getMovieId()) != 0)) {
	    	JTabbedPane pane = new JTabbedPane();
	    	JComponent editor = createEditor(seriesRating, mProgram.getTitle(), mProgram.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE));
			pane.addTab(mLocalizer.msg("rating", "Rating"), editor);
			editor = createEditor(episodeRating, mProgram.getTextField(ProgramFieldType.EPISODE_TYPE), mProgram.getTextField(ProgramFieldType.ORIGINAL_EPISODE_TYPE));
	    	pane.addTab(mLocalizer.msg("episodeRating", "Rating of Episode"), editor);
	    	mainComponent = pane;
    	}
    	else {
    		if (seriesRating != null) {
    			mainComponent = createEditor(seriesRating, mProgram.getTitle(), mProgram.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE));
    		} else {
    			ImdbRating rating = ImdbPlugin.getInstance().getRatingFor(mProgram);
    			mainComponent = createEditor(rating, mProgram.getTitle(), mProgram.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE));    			
    		}
    	}
    }
	else {
		ImdbRating rating = ImdbPlugin.getInstance().getRatingFor(mProgram);
		mainComponent = createEditor(rating, mProgram.getTitle(), mProgram.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE));
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

	private JComponent createEditor(final ImdbRating rating, final String title, final String origTitle) {
    return new JScrollPane(new ImdbRatingPanel(ImdbPlugin.getInstance().getDatabase().getMovieForId(rating.getMovieId(), (title != null) && (title.length() > 0) ? title : origTitle), rating));
	}

  public void close() {
    setVisible(false);
  }
}