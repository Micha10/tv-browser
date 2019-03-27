package imdbplugin;

/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import util.io.IOUtilities;
import util.ui.Localizer;
import util.ui.progress.ProgressInputStream;
import devplugin.ProgressMonitor;


public class ImdbParser {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ImdbParser.class);
  
  private static final int STEPS_TO_REPORT_PROGRESS = 10000;

  private ImdbDatabase mDatabase;
  private String mServer;
  private boolean mRunParser = true;
  private HashMap<String, Boolean> ratingIds = null;
  ProgressInputStream progressInputStream = null;
  private int position = 0;

  public ImdbParser(final ImdbDatabase db, final String server) {
    mDatabase = db;
    mServer = server;
  }

  public void startParsing(final ProgressMonitor monitor) throws IOException {
    int ratingCount = 0;
    	
    monitor.setMaximum((60 + 100 + 20 + 5) * 1024 * 1024); // title.akas.tsv.gz + title.basic.tsv.gz + title.episode.tsv.gz + title.ratings.tsv.gz
    position = 0;
    
    mDatabase.deleteDatabase();
    mRunParser = true;
	ratingIds = new HashMap<String, Boolean>();

    if (mRunParser) {
      ratingCount = importIMDbRatings(monitor);
    }
	
    if (mRunParser) {
      importIMDbTitles(monitor);
    }

    if (mRunParser) {
      importIMDbAkas(monitor);
    }
    
    if (mRunParser) {
        importIMDbEpisodes(monitor);
    }
    
    if (mRunParser) {
      ImdbPlugin.getInstance().setCurrentDatabaseVersion(ratingCount);
    } else {
      // Cancel was pressed, all Files have to be deleted
      mDatabase.deleteDatabase();
    }
    
    ratingIds.clear();
    ratingIds = null;
    mDatabase.openForReading();
  }

  private BufferedReader downloadFile(final ProgressMonitor monitor, final String fileName) throws IOException {
    monitor.setMessage(mLocalizer.msg("download", "Downloading {0}", fileName));

    final URL url = new URL(mServer + "/" + fileName);
    final File tempFile = File.createTempFile("imdb", null);
    
    IOUtilities.download(url, tempFile);

    final BufferedInputStream stream = new BufferedInputStream(new FileInputStream(tempFile));
    tempFile.deleteOnExit();
    
    progressInputStream = new ProgressInputStream(stream, monitor, position);
    InputStream gzipInputStream = new GZIPInputStream(progressInputStream);
    InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, "UTF-8");
    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
    
    return bufferedReader;
  }

  private void importIMDbTitles(final ProgressMonitor monitor) throws IOException {
    int titleCount = 0;
     
	final BufferedReader basicsTitleReader = downloadFile(monitor, "title.basics.tsv.gz");
	String basicLine = basicsTitleReader.readLine();

	if (basicLine.startsWith("tconst")) {
		basicLine = basicsTitleReader.readLine();
	}
	
	while (mRunParser && (basicLine != null)) {
	  String[] basicFields = basicLine.split("\t");
      String titleId = basicFields[0].trim();
      String titleType = basicFields[1].trim();
      String primaryTitle = basicFields[2].trim();	// the more popular title
      String originalTitle = basicFields[3].trim();	// original title, in the original language
      int startYear = basicFields[5].equals("\\N") ? -1 : Integer.parseInt(basicFields[5].trim());
      ImdbDatabase.TitleType tType = ImdbDatabase.TitleType.movie;	
   		
      if (titleType.equals("tvEpisode")) {
       	tType = ImdbDatabase.TitleType.episode;
      }

      if (titleType.equals("tvSeries") || titleType.equals("tvMiniSeries")) {
    	tType = ImdbDatabase.TitleType.series;
      }

      if ((ratingIds.containsKey(titleId) || (tType == ImdbDatabase.TitleType.series)) && !titleType.equals("videoGame")) {
      
    	mDatabase.addOriginalTitle(tType, titleId, originalTitle, startYear);

        if (!primaryTitle.equals(originalTitle)) {
          mDatabase.addAkaTitle(tType, titleId, primaryTitle, startYear);
        }
     		
    	if (++titleCount % STEPS_TO_REPORT_PROGRESS == 0 || titleCount == 1) {
          monitor.setMessage(mLocalizer.msg("titles", "Title {0}", titleCount));
        }   	  
      }

      basicLine = basicsTitleReader.readLine();		
	}

	position = progressInputStream.getCurrentPosition();
	basicsTitleReader.close();
    monitor.setMessage(mLocalizer.msg("titles", "Title {0}", titleCount));
  }

  private void importIMDbAkas(final ProgressMonitor monitor) throws IOException {
    int akaCount = 0;
    ImdbDatabase.ImdbTitle imdbTitle = null;
	     
	final BufferedReader akasTitleReader = downloadFile(monitor, "title.akas.tsv.gz");
	String akaLine = akasTitleReader.readLine();

	if (akaLine.startsWith("titleId")) {
	  akaLine = akasTitleReader.readLine();
	}
	
	mDatabase.openForReadingDuringImport();

	while (mRunParser && (akaLine != null)) {
	  String[] akaFields = akaLine.split("\t");
	  String titleId = akaFields[0].trim();
	  
      if (ratingIds.containsKey(titleId)) {

	    String akaTitle = akaFields[2].trim();
	  
	    if ((imdbTitle == null) || !titleId.equals(imdbTitle.getTitleId())) {
	      imdbTitle = mDatabase.getImdbMovie(titleId);
	    }
	
	    if ((imdbTitle != null) && titleId.equals(imdbTitle.getTitleId()) && !imdbTitle.hasTitle(akaTitle)) {
	      mDatabase.addAkaTitle(imdbTitle.getTitleType(), titleId, akaTitle, imdbTitle.getReleaseYear());
	      imdbTitle.addTitle(akaTitle);

	      if (++akaCount % STEPS_TO_REPORT_PROGRESS == 0 || akaCount == 1) {
		    monitor.setMessage(mLocalizer.msg("akaTitles", "Alternative title {0}", akaCount));
		  }
	    }
      }
	  akaLine = akasTitleReader.readLine();
	}

	position = progressInputStream.getCurrentPosition();
	akasTitleReader.close();
    monitor.setMessage(mLocalizer.msg("akaTitles", "Alternative title {0}", akaCount));
  }

  private int importIMDbRatings(final ProgressMonitor monitor) throws IOException {
	int ratingsCount = 0;
	final ImdbHistogram histogram = new ImdbHistogram();
	final BufferedReader ratingsTitleReader = downloadFile(monitor, "title.ratings.tsv.gz");
	String ratingLine = ratingsTitleReader.readLine();
	
	if (ratingLine.startsWith("tconst")) {
	  ratingLine = ratingsTitleReader.readLine();
	}
	
    while (mRunParser && (ratingLine != null))  {
	  String[] ratingFields = ratingLine.split("\t");
	  String titleId = ratingFields[0];
	  String averageRating = ratingFields[1];
	  String numVotes = ratingFields[2];

      int rating = Integer.parseInt(averageRating.replaceAll("\\.", ""));
      int votes = Integer.parseInt(numVotes);

      mDatabase.addRating(titleId, rating, votes);
      ratingIds.put(titleId, Boolean.TRUE);
      histogram.addRating(rating, votes);
        
	  if (++ratingsCount % STEPS_TO_REPORT_PROGRESS == 0 || ratingsCount == 1) {
        monitor.setMessage(mLocalizer.msg("ratings", "Rating {0}", ratingsCount));
      }
	  ratingLine = ratingsTitleReader.readLine();
	}

	position = progressInputStream.getCurrentPosition();
    ratingsTitleReader.close();
    ImdbPlugin.getInstance().storeHistogram(histogram);
    monitor.setMessage(mLocalizer.msg("ratings", "Rating {0}", ratingsCount));
    return ratingsCount;
  }

  private void importIMDbEpisodes(final ProgressMonitor monitor) throws IOException {
	int episodesCount = 0;

	final BufferedReader episodesReader = downloadFile(monitor, "title.episode.tsv.gz");
	String episodeLine = episodesReader.readLine();

	if (episodeLine.startsWith("tconst")) {
	  episodeLine = episodesReader.readLine();
	}
	
    while (mRunParser && (episodeLine != null))  {
	  String[] episodeFields = episodeLine.split("\t");
	  String episodeId = episodeFields[0];
	  String seriesId = episodeFields[1];

      if (ratingIds.containsKey(episodeId)) {
	  
        mDatabase.addSeriesEpisode(seriesId, episodeId);
        
	    if (++episodesCount % STEPS_TO_REPORT_PROGRESS == 0 || episodesCount == 1) {
          monitor.setMessage(mLocalizer.msg("episodes", "Episode {0}", episodesCount));
        }
      }
	  episodeLine = episodesReader.readLine();
	}

	position = progressInputStream.getCurrentPosition();
    episodesReader.close();
    monitor.setMessage(mLocalizer.msg("episodes", "Episode {0}", episodesCount));
  }

  public void stopParsing() {
    mRunParser = false;
  }
}
