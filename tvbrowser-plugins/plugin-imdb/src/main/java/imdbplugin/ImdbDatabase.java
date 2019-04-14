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
 *
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import devplugin.Program;
import devplugin.ProgramFieldType;

public final class ImdbDatabase {

  private static final String ITEM_TYPE = "ITEM_TYPE";
  private static final String TYPE_TITLE = "TYPE_TITLE";
  private static final String TYPE_RATING = "TYPE_RATING";
  private static final String TYPE_SERIES_EPISODE = "TYPE_SERIES_EPISODE";

  private static final String NAME_TYPE = "NAME_TYPE";
  private static final String TYPE_ORIGINAL_NAME = "TYPE_ORIGINAL_NAME";
  private static final String TYPE_AKA_NAME = "TYPE_AKA_NAME";
  
  public enum TitleType { movie, series, episode }
  private static final String TITLE_TYPE = "TITLE_TYPE";
  private static final String TYPE_MOVIE = "TYPE_MOVIE";
  private static final String TYPE_SERIES = "TYPE_SERIES";
  private static final String TYPE_EPISODE = "TYPE_EPISODE";  
  
  private static final String TITLE_ID = "TITLE_ID";
  private static final String TITLE_NAME = "TITLE_NAME";
  private static final String TITLE_NAME_NORMALIZED = "TITLE_NAME_NORMALIZED";
  private static final String RELEASE_YEAR = "RELEASE_YEAR";

  private static final String AVERAGE_RATING = "AVERAGE_RATING";
  private static final String NUM_VOTES = "NUM_VOTES";

  private static final String SERIES_ID = "SERIES_ID";
  private static final String EPISODE_ID = "EPISODE_ID";  
  
  private File mCurrentPath;

  private IndexSearcher mSearcher = null;
  private IndexWriter mWriter = null;
  
  private static String[] episodesDelimiter = { " - ", ": " };
  
  public class ImdbTitle {
	private String titleId;
    private int releaseYear;
    private TitleType titleType;
    private String titleTypeStr;
    private ArrayList<String> titles;
    
    public ImdbTitle(final String tId, final String tType, final String tName, final int relYear) {
      titleId = tId;
      releaseYear = relYear;
      titleType = TitleType.movie;
      titleTypeStr = tType;
      titles = new ArrayList<String>();
      addTitle(tName);

      if (tType.equals(TYPE_SERIES)) {
        titleType = TitleType.series;
      }
      if (tType.equals(TYPE_EPISODE)) {
          titleType = TitleType.episode;
      }      
    }
    
    public void addTitle(final String tName) {
      titles.add(tName);
    }
    
    public boolean hasTitle(final String tName) {
      return titles.contains(tName);
    }
    
    public String getTitleId() {
      return titleId;
    }

    public String getTitleTypeStr() {
      return titleTypeStr;
    }

    public int getReleaseYear() {
      return releaseYear;
    }

    public TitleType getTitleType() {
      return titleType;
    }    
  }
  
  private class ImdbCandidate {
	 private ArrayList<String> candidates = new ArrayList<String>();;
	 
	 public ImdbCandidate() {
	 }
	 
	 public void reset() {
		 candidates.clear();
	 }
	 
	 public void add(Document document) {
		 String titleId = document.get(TITLE_ID);
		 
		 if (!candidates.contains(titleId)) {
			 candidates.add(titleId);
		 }
	 }
	 
	 public boolean isEmpty() {
		 return candidates.isEmpty();
	 }
	 
	 public int size() {
		 return candidates.size();
	 }
	 
	 public String getTitleId() {
		 if (isEmpty()) {
			 return null;
		 }
		 if (size() == 1) {
			 return candidates.get(0);
		 }
		 return null;
	 }
	 
	 public Iterator<String> getTitleIds() {
    	return candidates.iterator();
    }
  }

  private ImdbCandidate mCandidates = new ImdbCandidate();
  private ImdbCandidate mCandidatesRange = new ImdbCandidate();
  private ImdbCandidate mCandidatesExact = new ImdbCandidate();
  private int mCandidatesYear;
  
  public ImdbDatabase(final File imdbDatabase) {
    mCurrentPath = imdbDatabase;
    // make sure the directory exists
    if (!mCurrentPath.exists()) {
      try {
        mCurrentPath.mkdirs();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void deleteDatabase() {
    close();
    for (File f : mCurrentPath.listFiles()) {
      try {
        f.delete();
      } catch (SecurityException e) {
        e.printStackTrace();
      }
    }
    openForWriting();
  }

  public void init() {
    openForReading();
  }

  public boolean isInitialised() {
    if (mSearcher == null) {
    	return false;
    }
    
    IndexReader reader = mSearcher.getIndexReader();
    
    if ((reader !=null) && (reader.maxDoc() > 1)) {
      return true;
    }
    return false;
  }

  private String getTitleTypeString(final TitleType titleType) { 
	switch (titleType) {
      case movie: return TYPE_MOVIE;
      case series:return TYPE_SERIES;
      case episode:return TYPE_EPISODE;
	}
	return null;
  }
  
  private void addTitle(final TitleType titleType, final String titleId, final String titleName, final int releaseYear, boolean isAka) {
    try {
      final Document doc = new Document();
      String typeStr = getTitleTypeString(titleType);
         
      doc.add(new StringField(ITEM_TYPE, TYPE_TITLE, Field.Store.YES));
      doc.add(new StringField(TITLE_ID, titleId, Field.Store.YES));
      doc.add(new StringField(NAME_TYPE, isAka ? TYPE_AKA_NAME : TYPE_ORIGINAL_NAME, Field.Store.YES));      
      doc.add(new StringField(TITLE_TYPE, typeStr, Field.Store.YES));
      doc.add(new StringField(TITLE_NAME, titleName, Field.Store.YES));
      doc.add(new StringField(TITLE_NAME_NORMALIZED, normalise(titleName), Field.Store.YES));
      doc.add(new StringField(RELEASE_YEAR, Integer.toString(releaseYear), Field.Store.YES));

      mWriter.addDocument(doc);
    } catch (IOException e) {
        e.printStackTrace();
    }
  }
  
  public void addOriginalTitle(final TitleType titleType, final String titleId, final String titleName, final int releaseYear) {
	  addTitle(titleType, titleId, titleName, releaseYear, false);
  }

  public void addAkaTitle(final TitleType titleType, final String titleId, final String titleName, final int releaseYear) {
	  addTitle(titleType, titleId, titleName, releaseYear, true);
  }

  public void addRating(final String titleId, final int averageRating, final int numVotes) {
	if ((titleId != null) && (titleId.length() > 0)) {		
	  try {
	    final Document doc = new Document();
	
	    doc.add(new StringField(ITEM_TYPE, TYPE_RATING, Field.Store.YES));
	    doc.add(new StringField(TITLE_ID, titleId, Field.Store.YES));
	    doc.add(new StringField(AVERAGE_RATING, Integer.toString(averageRating), Field.Store.YES));
	    doc.add(new StringField(NUM_VOTES, Integer.toString(numVotes), Field.Store.YES));
		
	    mWriter.addDocument(doc);
	  } catch (IOException e) {
	    e.printStackTrace();
	  }
    }
  }

  public void addSeriesEpisode(final String seriesId, final String episodeId) {
	if ((seriesId != null) && (episodeId != null) && (seriesId.length() > 0) && (episodeId.length() > 0)) {	
      try {
	    final Document doc = new Document();

        doc.add(new StringField(ITEM_TYPE, TYPE_SERIES_EPISODE, Field.Store.YES));
        doc.add(new StringField(SERIES_ID, seriesId, Field.Store.YES));
        doc.add(new StringField(EPISODE_ID, episodeId, Field.Store.YES));
	
	    mWriter.addDocument(doc);
      } catch (IOException e) {
        e.printStackTrace();
      }
	}
  }
  
  private String normalise(final String str) {
    if (str == null) {
      return "";
    }
    if (str.length() == 0) {
      return str;
    }
    final String lowerCase = str.toLowerCase();
    final int length = lowerCase.length();
    final StringBuilder builder = new StringBuilder(length + 4);
    for (int i = 0; i < length; i++) {
      final char character = lowerCase.charAt(i);
      switch (character) {
      // replace umlauts
      case '\u00E4': {
        builder.append("ae");
        break;
      }
      case '\u00F6': {
        builder.append("oe");
        break;
      }
      case '\u00FC': {
        builder.append("ue");
        break;
      }
      case '\u00DF': {
        builder.append("ss");
        break;
      }
        // remove some special characters
      case ',':
      case '\'':
      case ':':
      case '-': {
        break;
      }
        // remove all double spaces
      case ' ': {
        if (builder.length() > 0 && builder.charAt(builder.length() - 1) != ' ') {
          builder.append(' ');
        }
        break;
      }
      default:
        builder.append(character);
      }
    }
    // remove dots at the end
    while (builder.length() > 0 && builder.charAt(builder.length() - 1) == '.') {
      builder.setLength(builder.length() - 1);
    }
    String result = builder.toString();
    // remove blank before the final ? or !
    if (result.length() > 2 && (result.endsWith("!") || result.endsWith("?"))
        && result.charAt(result.length() - 2) == ' ') {
      result = result.substring(0, result.length() - 2) + result.charAt(result.length() - 1);
    }
    if (result.length() == 0) {
      return str;
    }
    return result;
  }

  private Document[] getTitleCandidates(final TitleType titleType, final String title, final int releaseYear, final String nameType, final String field) { 

	if (!isInitialised() || (title == null) || (title.length() < 1)) {
      return null;
    }
    
	BooleanQuery.Builder  booleanQuery = new BooleanQuery.Builder();

	booleanQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_TITLE)), BooleanClause.Occur.MUST);    
	booleanQuery.add(new TermQuery(new Term(TITLE_TYPE, getTitleTypeString(titleType))), BooleanClause.Occur.MUST);
	booleanQuery.add(new TermQuery(new Term(NAME_TYPE, nameType)), BooleanClause.Occur.MUST);
	booleanQuery.add(new TermQuery(new Term(field, title)), BooleanClause.Occur.MUST);
	
    if (releaseYear > 0) {
      booleanQuery.add(new TermQuery(new Term(RELEASE_YEAR, Integer.toString(releaseYear-2))), BooleanClause.Occur.SHOULD);
      booleanQuery.add(new TermQuery(new Term(RELEASE_YEAR, Integer.toString(releaseYear-1))), BooleanClause.Occur.SHOULD);
      booleanQuery.add(new TermQuery(new Term(RELEASE_YEAR, Integer.toString(releaseYear))), BooleanClause.Occur.SHOULD);
      booleanQuery.add(new TermQuery(new Term(RELEASE_YEAR, Integer.toString(releaseYear+1))), BooleanClause.Occur.SHOULD);
      booleanQuery.add(new TermQuery(new Term(RELEASE_YEAR, Integer.toString(releaseYear+2))), BooleanClause.Occur.SHOULD);
      booleanQuery.setMinimumNumberShouldMatch(1);
    }

  	try {
  	  // title name 'Pilot' gets appr. 1700 hits
	  TopDocs topDocs = mSearcher.search(booleanQuery.build(), 2000);
	    
	  if (topDocs.totalHits.value > 0) {
	    Document[] documents = new Document[topDocs.scoreDocs.length];
	      
	    for (int i = 0; i < topDocs.scoreDocs.length; i++) {
	      ScoreDoc scoreDoc = topDocs.scoreDocs[i];
	      if (scoreDoc != null) {
	        documents[i] = mSearcher.doc(scoreDoc.doc);
	      }
	    }
	    return documents;
	  }
  	} catch (IOException e) {
		e.printStackTrace();
	}
	return null;	
  }
  
  private static boolean isEpisode(final Program program) {

	  if ((program.getTextField(ProgramFieldType.EPISODE_TYPE) != null) && (program.getTextField(ProgramFieldType.EPISODE_TYPE).length() > 0)) {
		  return true;
	  }
	  if ((program.getTextField(ProgramFieldType.ORIGINAL_EPISODE_TYPE) != null) && (program.getTextField(ProgramFieldType.ORIGINAL_EPISODE_TYPE).length() > 0)) {
		  return true;
	  }

	  if (program.getIntField(ProgramFieldType.EPISODE_NUMBER_TYPE) > 0) {
		  return true;
	  }

	  if (program.getIntField(ProgramFieldType.EPISODE_TOTAL_NUMBER_TYPE) > 0) {
		  return true;
	  }

	  if (program.getIntField(ProgramFieldType.SEASON_NUMBER_TYPE) > 0) {
		  return true;
	  }
	  
	  return false;
  }
  
  private ArrayList<String> getEpisodeIds(final String seriesId) {
	if ((seriesId == null) || (seriesId.length() < 1)) {
	  return null;
	}

	BooleanQuery.Builder  booleanQuery = new BooleanQuery.Builder();
		
	booleanQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_SERIES_EPISODE)), BooleanClause.Occur.MUST);  
	booleanQuery.add(new TermQuery(new Term(SERIES_ID, seriesId)), BooleanClause.Occur.MUST);

	try {
	  // load 10.000 episodes: series 'Coronation Street' with appr. 9.500 episodes and Lindenstraße with appr. 1.700 episodes
	  TopDocs topDocs = mSearcher.search(booleanQuery.build(), 10000);
	
	  if (topDocs.totalHits.value < 1) {
		return null;		
	  }

	  ArrayList<String> episodeIds = new ArrayList<String>();
		      
	  for (int i = 0; i < topDocs.scoreDocs.length; i++) {
	    ScoreDoc scoreDoc = topDocs.scoreDocs[i];
		if (scoreDoc != null) {
		  episodeIds.add(mSearcher.doc(scoreDoc.doc).get(EPISODE_ID));
		}
	  }
	  return episodeIds;
	
	} catch (IOException e) {
	  e.printStackTrace();
	}    
	
	return null;	
  }
  
  public String removeSuffix(final String title) {
	if ((title == null) || (title.length() < 1)) {
	  return title;
	}
	  
	// https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html
  	String titelSuffix = "(\\([0-9]+\\)|\\([0-9]+/[0-9]+\\)|\\((Teil|Part) \\p{Digit}+\\)|(Teil|Part) \\p{Digit}+|\\(Pilot\\)|\\(Fortsetzung\\)|\\(WH von \\p{Upper}\\p{Upper}\\))$";
  	boolean hasSuffix = title.matches(".* " + titelSuffix);
  	
  	if (hasSuffix) {
      String [] titleParts = title.split(titelSuffix);

      if (titleParts.length == 1) {
        return titleParts[0].trim();
      }
  	}
  	return title;	  
  }
    
  private String searchTitleWithoutSuffix(final TitleType titleType, final String titleName, final String originalTitleName, final int releaseYear) {
	String titleNameWithoutSuffix = removeSuffix(titleName);
	String originalTitleNameWithoutSuffix = removeSuffix(originalTitleName);
	
	if (titleName.equals(titleNameWithoutSuffix) && originalTitleName.equals(originalTitleNameWithoutSuffix)) {
	  return null;
	}
	
	return getTitleId(titleType, titleNameWithoutSuffix, originalTitleNameWithoutSuffix, releaseYear);	
  }
    
  private String getTitleId(final TitleType titleType, final String title, final String originalTitle, final int releaseYear) {     
	  final String titleName = title == null ? "" : title.trim();
	  final String originalTitleName = originalTitle == null ? "" : originalTitle.trim();
	  final String normalizedTitle = normalise(titleName);
      final String normalisedOriginalTitle = normalise(originalTitleName);
      
	  resetCandidates(releaseYear);

      // search exact original title
      if((originalTitleName.length() > 0) && addCandidates(getTitleCandidates(titleType, originalTitleName, releaseYear, TYPE_ORIGINAL_NAME, TITLE_NAME))) {
        return getTitleIdOfCandidate();
      }
	  	  
      // search exact title
      if(addCandidates(getTitleCandidates(titleType, titleName, releaseYear, TYPE_ORIGINAL_NAME, TITLE_NAME))) {
        return getTitleIdOfCandidate();
      }

      // search exact title in A.K.A. list
      if (addCandidates(getTitleCandidates(titleType, titleName, releaseYear, TYPE_AKA_NAME, TITLE_NAME))) {
        return getTitleIdOfCandidate();
      }

      // search exact original title in A.K.A. list
      if((originalTitleName.length() > 0) && addCandidates(getTitleCandidates(titleType, originalTitleName, releaseYear, TYPE_AKA_NAME, TITLE_NAME))) {
        return getTitleIdOfCandidate();
      }
      
      // search normalized original title
      if((normalisedOriginalTitle.length() > 0) &&  addCandidates(getTitleCandidates(titleType, normalisedOriginalTitle, releaseYear, TYPE_ORIGINAL_NAME, TITLE_NAME_NORMALIZED))) {
        return getTitleIdOfCandidate();
      }
      
      // search normalized title
      if(addCandidates(getTitleCandidates(titleType, normalizedTitle, releaseYear, TYPE_ORIGINAL_NAME, TITLE_NAME_NORMALIZED))) {
        return getTitleIdOfCandidate();
      }

      // search normalized title in A.K.A. list
      if (addCandidates(getTitleCandidates(titleType, normalizedTitle, releaseYear, TYPE_AKA_NAME, TITLE_NAME_NORMALIZED))) {
        return getTitleIdOfCandidate();
      }
      
      // search normalized original title in A.K.A. list
      if((normalisedOriginalTitle.length() > 0) &&  addCandidates(getTitleCandidates(titleType, normalisedOriginalTitle, releaseYear, TYPE_AKA_NAME, TITLE_NAME_NORMALIZED))) {
        return getTitleIdOfCandidate();
      }
       
      // and now try with shortened title if there is a common suffix
      String titleId = searchTitleWithoutSuffix(titleType, titleName, originalTitleName, releaseYear);
      if (titleId != null) {
        return titleId;
      }

      if (mCandidates.size() == 1) {
        return mCandidates.getTitleId();
      }

      // nothing found yet, so try everything again without year
      if (releaseYear > 0) {
        titleId =  getTitleId(titleType, titleName, originalTitleName, 0);
        if (titleId != null) {
          return titleId;
        }
      }

	  return null;
  }

  private int getReleaseYear(final Program program) {
	  int releaseYear = program.getIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE);
	  
	  if (releaseYear > 0) {
		  return releaseYear;
	  }
	  
	  // search release year in short description
	  String shortDesc = program.getTextField(ProgramFieldType.SHORT_DESCRIPTION_TYPE);
	  
	  if ((null != shortDesc) && (shortDesc.length() > 3)) {
		  Pattern p = Pattern.compile("[12][0-9][0-9][0-9]");
		  Matcher m = p.matcher(shortDesc);

		  if (m.find()) {
			  releaseYear = Integer.parseInt(shortDesc.substring(m.start(), m.start()+4));
			  return releaseYear;
		  }
	  }
	  return -1;
  }

  private String getTitleId(final TitleType titleType, final Program program) {
	  return getTitleId(titleType, program.getTitle(), program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE), titleType == TitleType.series ? -1 : getReleaseYear(program));
  }
     
  public ImdbRating getRatingForId(final String titleId) {
	  if ((titleId == null) || !isInitialised()) {
	    return null;
	  }
	
	  BooleanQuery.Builder  booleanQuery = new BooleanQuery.Builder();
	  booleanQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_RATING)), BooleanClause.Occur.MUST);    
	  booleanQuery.add(new TermQuery(new Term(TITLE_ID, titleId)), BooleanClause.Occur.MUST);
		  
	  try {  				
		TopDocs topDocs = mSearcher.search(booleanQuery.build(), 1);
	
	    if (topDocs.totalHits.value > 0) {
	      Document doc = mSearcher.doc(topDocs.scoreDocs[0].doc);
	      byte averageRating = Byte.parseByte(doc.get(AVERAGE_RATING));
		  int numVotes = Integer.parseInt(doc.get(NUM_VOTES));

		  BooleanQuery.Builder  titleQuery = new BooleanQuery.Builder();
		  titleQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_TITLE)), BooleanClause.Occur.MUST);    
		  titleQuery.add(new TermQuery(new Term(NAME_TYPE, TYPE_ORIGINAL_NAME)), BooleanClause.Occur.MUST);
		  titleQuery.add(new TermQuery(new Term(TITLE_ID, titleId)), BooleanClause.Occur.MUST);	  
		  
	      topDocs = mSearcher.search(titleQuery.build(), 1);
		  	
	  	  if (topDocs.totalHits.value > 0) {
	  		  doc = mSearcher.doc(topDocs.scoreDocs[0].doc);
	    	  String dTitleType = doc.get(TITLE_TYPE);
	    	  Boolean isEpisode = TYPE_EPISODE.equals(dTitleType);
	    	  
		      ImdbRating rating = new ImdbRating(averageRating, numVotes, titleId, isEpisode);
		      
		      if (isEpisode) {
		    	  rating.setSeriesId(getSeriesId(titleId));
		      }
		      return rating;
	  	  }
		}
	  } catch (IOException e) {
		e.printStackTrace();
	  }    
	  return null;
  }
  
  private ArrayList<String> getTitleIdsOfCandidates() {
	ArrayList<String> titleIds = new ArrayList<String>();

	for(Iterator<String> titleIdIter = mCandidates.getTitleIds(); titleIdIter.hasNext();) {
		String titleId = titleIdIter.next();
		if (!titleIds.contains(titleId)) {
			titleIds.add(titleId);
		}
	}

	for(Iterator<String> titleIdIter = mCandidatesExact.getTitleIds(); titleIdIter.hasNext();) {
		String titleId = titleIdIter.next();
		if (!titleIds.contains(titleId)) {
			titleIds.add(titleId);
		}
	}

	for(Iterator<String> titleIdIter = mCandidatesRange.getTitleIds(); titleIdIter.hasNext();) {
		String titleId = titleIdIter.next();
		if (!titleIds.contains(titleId)) {
			titleIds.add(titleId);
		}
	}
	
	return titleIds;
  }
  
  //sometimes the title and/or the original title is "<series name> - <episode name>"
  // returns the <series name> part of title
  private String getSeriesPartOfTitle(final String title, final String delimiter) {
	  int delimIndex = 0;
	
	  if ((delimiter != null) && (title != null) && (title.length() > 5) && ((delimIndex = title.indexOf(delimiter)) > 0)) {
		  return title.substring(0, delimIndex).trim();
	  }
	  
	  return null;	  
  }

  //sometimes the title and/or the original title is "<series name> - <episode name>"
  // returns the <episode name> part of title
  private String getEpisodesPartOfTitle(final String title, final String delimiter) {
	  int delimIndex = 0;
	
	  if ((delimiter != null) && (title != null) && (title.length() > 5) && ((delimIndex = title.indexOf(delimiter)) > 0)) {
		  return title.substring(delimIndex + delimiter.length()).trim();
	  }
	  
	  return null;	  
  }
  
  // sometimes the title is "<title> - <some string>"
  private String searchSeriesInTitle(final String seriesTitle, final String seriesOriginalTitle, int releaseYear) {
	  for(int i = 0; i < episodesDelimiter.length; i++) {
		  String possibleSeriesTitle = getSeriesPartOfTitle(seriesTitle, episodesDelimiter[i]);
		  String possibleSeriesOriginalTitle = getSeriesPartOfTitle(seriesOriginalTitle, episodesDelimiter[i]);

		  if ((possibleSeriesTitle != null) || ((possibleSeriesOriginalTitle != null))) {
			  String titleId = getTitleId(TitleType.series, possibleSeriesTitle, possibleSeriesOriginalTitle,
					  // von 1985 - 1989 gab es eine österr. Series gleichen Namens
					  (possibleSeriesTitle != null) && possibleSeriesTitle.equals("Tatort") && ((releaseYear < 1985) || (releaseYear > 1989)) ? 1970 : releaseYear);
		  
			  if (titleId != null) {
				  return titleId;
			  }
		  }
	  }
	  return null;
  }

  // sometimes the title is "<series> - <episode>" or "<series>: <episode>"
  private ImdbRating searchEpisodeInTitle(final Program program) {
	  int releaseYeaer = getReleaseYear(program);

	  for(int i = 0; i < episodesDelimiter.length; i++) {
		  String possibleSeriesTitle = getSeriesPartOfTitle(program.getTitle(), episodesDelimiter[i]); 
		  String possibleEpisodeTitle = getEpisodesPartOfTitle(program.getTitle(), episodesDelimiter[i]);
		  String possibleSeriesOriginalTitle = getSeriesPartOfTitle(program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE), episodesDelimiter[i]);
		  String possibleEpisodeOriginalTitle = getEpisodesPartOfTitle(program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE), episodesDelimiter[i]);
	  

		  if (((possibleSeriesTitle != null) && (possibleEpisodeTitle != null)) || ((possibleSeriesOriginalTitle != null) && (possibleEpisodeOriginalTitle != null))) {
			  return getEpisodeRating(possibleSeriesTitle, possibleEpisodeTitle, possibleSeriesOriginalTitle, possibleEpisodeOriginalTitle, releaseYeaer);
		  }
	  }

	  return null;
  }
  
  private String getSeriesTitleId(final Program program, boolean ignoreReleaseYear) {
	  String seriesTitle = program.getTitle();
	  String seriesOriginalTitle = program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE);
	  int releaseYear = ignoreReleaseYear ? -1 : getReleaseYear(program);

	  return getSeriesTitleId(seriesTitle, seriesOriginalTitle, releaseYear);
  }

  private String getSeriesTitleId(final String seriesTitle, final String seriesOriginalTitle, int releaseYear) {
	  String titleId = getTitleId(TitleType.series, seriesTitle, seriesOriginalTitle, releaseYear);
	  
	  if (titleId != null) {
	    return titleId;
	  }
	  
	  return searchSeriesInTitle(seriesTitle, seriesOriginalTitle, releaseYear);
  }
  
  public ImdbRating getSeriesRating(final Program program) {
	  if (isEpisode(program)) {
		  String seriesId = getSeriesTitleId(program, false);
		  
		  return seriesId == null ? null : getRatingForId(seriesId);
	  }

	  String seriesId = searchSeriesInTitle(program.getTitle(), program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE), getReleaseYear(program));

	  return seriesId == null ? null : getRatingForId(seriesId);
  }
  
  private ImdbRating getEpisodeRating(final String seriesTitle, final String episodeTitle, final String seriesOriginalTitle, final String episodeOriginalTitle, int releaseYear) {
	  String seriesId = getSeriesTitleId(seriesTitle, seriesOriginalTitle, -1);
	  ArrayList<String> seriesCandidateIds = getTitleIdsOfCandidates();

	  // series not found - ignore episode
	  if (seriesCandidateIds.size() < 1) {
		  return null;
	  }

	  getTitleId(TitleType.episode, episodeTitle, episodeOriginalTitle, releaseYear);
	  ArrayList<String> episodeCandidateIds = getTitleIdsOfCandidates();
	  
	  if (episodeCandidateIds.size() < 1) {
		seriesId = getSeriesTitleId(seriesTitle, seriesOriginalTitle, releaseYear);
	    return seriesId == null ? null : getRatingForId(seriesId);
	  }
	  
	  for(int s = 0; s < seriesCandidateIds.size(); s++) {
	    String sId = seriesCandidateIds.get(s);
		ArrayList<String> episodeIdsOfSeries = getEpisodeIds(sId);

		if (episodeIdsOfSeries == null) {
		  continue;
		}

		for(int se = 0; se < episodeIdsOfSeries.size(); se++) {
	      String seId = episodeIdsOfSeries.get(se);

		  for(int ec = 0; ec < episodeCandidateIds.size(); ec++) {
			String ecId = episodeCandidateIds.get(ec);
				
			if (seId.equals(ecId)) {
			  ImdbRating rating = getRatingForId(ecId);
			  
			  if (rating != null) {
				  return rating;
			  }
			  seriesId = getSeriesTitleId(seriesTitle, seriesOriginalTitle, releaseYear);			  
			  return seriesId == null ? null : getRatingForId(seriesId);
			}
		  }
	    }
	  }

	  // episode candidates in series not found - search rating of the series
	  seriesId = getSeriesTitleId(seriesTitle, seriesOriginalTitle, releaseYear);
	  return seriesId == null ? null : getRatingForId(seriesId);

  }
    
  public ImdbRating getEpisodeRating(final Program program) {
	  int releaseYeaer = getReleaseYear(program);

	  if (isEpisode(program)) {
		  String seriesTitle = program.getTitle();	  
		  String episodeTitle = program.getTextField(ProgramFieldType.EPISODE_TYPE);
		  String seriesOriginalTitle = program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE);
		  String episodeOriginalTitle = program.getTextField(ProgramFieldType.ORIGINAL_EPISODE_TYPE);
		  
		  return getEpisodeRating(seriesTitle, episodeTitle, seriesOriginalTitle, episodeOriginalTitle, releaseYeaer);
	  }

      // sometimes the title is "<series> - <episode>"
	  return searchEpisodeInTitle(program);
  }

  public ImdbRating getRating(final Program program) {
	  	  
	  if (isEpisode(program)) {
		  return getEpisodeRating(program);
	  }

	  ImdbRating rating = null;
	  String titleId = getTitleId(TitleType.movie, program);
	  
	  if (titleId != null) {
		  rating = getRatingForId(titleId);
	  }
	  
	  if (rating != null) {
		  return rating;
	  }

      // sometimes the title is "<series> - <episode>"
	  if ((rating = searchEpisodeInTitle(program)) != null) {
		  return rating;
	  }
	  
	  // sometimes it is a series, e.g. Nachtmagazin or Tagesschau
	  titleId = getSeriesTitleId(program, false);
	  return titleId == null ? null : getRatingForId(titleId);
  }
  
  private void setSearcher() {
    if (mCurrentPath.exists() && (mCurrentPath.listFiles().length > 0)) {
	  try {
	      Directory dir = FSDirectory.open(Paths.get(mCurrentPath.getAbsolutePath()));
	      final IndexReader reader = DirectoryReader.open(dir);
	      mSearcher = new IndexSearcher(reader);
	  } catch (IOException e) {
	      mSearcher = null;
	      e.printStackTrace();
	  }
    }
  }
  private void setWriter() {
    try {
	  	Directory dir = FSDirectory.open(Paths.get(mCurrentPath.getAbsolutePath()));
	  	Analyzer analyzer = new SimpleAnalyzer();
	  	IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
	  	iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
	  	iwc.setRAMBufferSizeMB(256.0);
	  	mWriter = new IndexWriter(dir, iwc);
    } catch (IOException e) {
    	mWriter = null;
        e.printStackTrace();
    }
  }
  
  public void openForReadingDuringImport() {
    if (mWriter != null) {
	  try {
	    mWriter.commit();
	  } catch (IOException e) {
	    e.printStackTrace();
	  }
    }
    setSearcher();
  }
  
  public void openForReading() {
	    close();
	    setSearcher();
	  }

  private void openForWriting() {
    if (mCurrentPath.exists() && (mCurrentPath.listFiles().length < 2)) {
    	setWriter();
    	if (mWriter != null) {
	      try {
		      mWriter.addDocument(new Document());
		      mWriter.close();
	      } catch (IOException e) {
	          e.printStackTrace();
	      }	  
    	}
    } else if (new File(mCurrentPath, "write.lock").exists()) {
      try {
        new File(mCurrentPath, "write.lock").delete();
      } catch (SecurityException e) {
        e.printStackTrace();
      }
    }

    if (mWriter != null) {
      try {
        mWriter.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    setWriter();
    setSearcher();
  }

  public void close() {
    try {
      if (mWriter != null) {
        mWriter.commit();
        mWriter.close();
        mWriter = null;
      }
      if (mSearcher != null) {
    	IndexReader reader = mSearcher.getIndexReader();
    	
    	if (reader != null) {
   		  reader.close();
    	}
        mSearcher = null;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private boolean addCandidates(final Document[] movies) {
	mCandidatesExact.reset();
	mCandidatesRange.reset();

	if (movies == null) {
      return false;
    }

    for (Document document : movies) {
      if (document != null) {
        String releaseYearStr = document.get(RELEASE_YEAR);
        int releaseYear = (releaseYearStr != null) && (releaseYearStr.length() > 0) ? Integer.parseInt(releaseYearStr) : -1;

        if ((mCandidatesYear == -1) || (mCandidatesYear == releaseYear)) {
          mCandidatesExact.add(document);
        }
        if ((mCandidatesYear != -1) && (releaseYear >= (mCandidatesYear - 2)) && (releaseYear <= (mCandidatesYear + 2))) {
          mCandidatesRange.add(document);
        }
        mCandidates.add(document);
      }
    }

    return (mCandidatesExact.size() == 1) || (mCandidatesRange.size() == 1) ? true : false;
  }

  private String getTitleIdOfCandidate() {
	  if (mCandidatesExact.size() == 1) {
		  return mCandidatesExact.getTitleId();		  
	  }
	  return mCandidatesRange.getTitleId();	
  }
  
  private void resetCandidates(final int year) {
    mCandidatesYear = year;
    mCandidates.reset();
    mCandidatesRange.reset();
    mCandidatesExact.reset();
  }

  public ImdbTitle getImdbMovie(final String titleId) { 

	if (!isInitialised()) {
      return null;
    }
    
	BooleanQuery.Builder  booleanQuery = new BooleanQuery.Builder();

	booleanQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_TITLE)), BooleanClause.Occur.MUST);    
	booleanQuery.add(new TermQuery(new Term(TITLE_ID, titleId)), BooleanClause.Occur.MUST);

  	try {  				
	  TopDocs topDocs = mSearcher.search(booleanQuery.build(), 1000);	// 'Pokémon' has appr. 80 aka titles
	    
	  if (topDocs.totalHits.value > 0) {
		ImdbTitle imdbTitle = null;
	      
	    for (int i = 0; i < topDocs.scoreDocs.length; i++) {
	      ScoreDoc scoreDoc = topDocs.scoreDocs[i];
	      if (scoreDoc != null) {
	    	Document doc = mSearcher.doc(scoreDoc.doc);
	    	String dTitleName = doc.get(TITLE_NAME);

	        if (imdbTitle == null) {
	    	  String dTitleId = doc.get(TITLE_ID);
	    	  String dRelYearStr = doc.get(RELEASE_YEAR);
	    	  String dTitleType = doc.get(TITLE_TYPE);
	    	  int dRelYear = (dRelYearStr != null) && (dRelYearStr.length() > 0) ? Integer.parseInt(dRelYearStr) : -1;

	    	  imdbTitle = new ImdbTitle(dTitleId, dTitleType, dTitleName, dRelYear);
	        } else {
	          imdbTitle.addTitle(dTitleName);
	        }
	      }
	    }
	    return imdbTitle;
	  }
  	} catch (IOException e) {
		e.printStackTrace();
	}
	return null;	
  }
  
  
  @SuppressWarnings("unused")
  private void printDocument(final Document document) {
    
      System.out.print(document.getField(TITLE_NAME).stringValue());     
      System.out.println(" : " + document.getField(RELEASE_YEAR).stringValue() +
      " : " + document.getField(TITLE_ID).stringValue());
     
  }

  @SuppressWarnings("unused")
  private void logProgram(final Program program) {
	  for(Iterator<ProgramFieldType> it = program.getFieldIterator(); it.hasNext();) {
		  ProgramFieldType fieldType = (ProgramFieldType)it.next();
		  
		  if (program.hasFieldValue(fieldType)) {
			  String value = "";
			  if (ProgramFieldType.FORMAT_TEXT == fieldType.getFormat()) {
				  value = program.getTextField(fieldType);
			  }
			  if (ProgramFieldType.FORMAT_INT == fieldType.getFormat()) {
				  value = program.getIntFieldAsString(fieldType);
			  }
			  if (ProgramFieldType.FORMAT_TIME == fieldType.getFormat()) {
				  value = program.getTimeFieldAsString(fieldType);
			  }
			  System.out.println("  Feld " + fieldType.getLocalizedName() + ": <" + value + ">");
		  }
	  }
  }
  
  private String getSeriesId(final String episodeId) {
    if (!isInitialised() || (episodeId == null)) {
        return null;
    }

    BooleanQuery.Builder  booleanQuery = new BooleanQuery.Builder();
    	
    booleanQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_SERIES_EPISODE)), BooleanClause.Occur.MUST);    
    booleanQuery.add(new TermQuery(new Term(EPISODE_ID, episodeId)), BooleanClause.Occur.MUST);
    	
	try {
      final TopDocs topDocs = mSearcher.search(booleanQuery.build(), 1);
	
	  if (topDocs.totalHits.value > 0) {
		  ScoreDoc scoreDoc = topDocs.scoreDocs[0];
		  Document document = mSearcher.doc(scoreDoc.doc);
	  	  
		  return document.get(SERIES_ID);
	  }
  	} catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public ImdbMovie getMovieForId(final String movieId, final String title) {
    if (!isInitialised() || (movieId == null)) {
      return null;
    }

  	BooleanQuery.Builder  titleQuery = new BooleanQuery.Builder();
  	
  	titleQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_TITLE)), BooleanClause.Occur.MUST);    
  	titleQuery.add(new TermQuery(new Term(NAME_TYPE, TYPE_ORIGINAL_NAME)), BooleanClause.Occur.MUST);
  	titleQuery.add(new TermQuery(new Term(TITLE_ID, movieId)), BooleanClause.Occur.MUST);
  	
  	try {
      TopDocs topDocs = mSearcher.search(titleQuery.build(), 1);
  	
  	  if (topDocs.totalHits.value < 1) {
  	    return null;
  	  }

      ImdbMovie movie = new ImdbMovie();
      int releaseYear = -1;
	  ScoreDoc scoreDoc = topDocs.scoreDocs[0];
	  Document document = mSearcher.doc(scoreDoc.doc);
	  
	  if (document.get(RELEASE_YEAR).length() > 0) {
          releaseYear = Integer.parseInt(document.get(RELEASE_YEAR));
      }
      movie.setYear(releaseYear);
      if (title == null) {
          movie.setTitle(document.get(TITLE_NAME));
      } else {
          movie.setTitle(title);
          if (title.compareTo(document.get(TITLE_NAME)) != 0) {
        	  movie.addAka(new ImdbAka(document.get(TITLE_NAME), "", releaseYear));	
          }
      }

	  BooleanQuery.Builder  akaQuery = new BooleanQuery.Builder();
  	
	  akaQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_TITLE)), BooleanClause.Occur.MUST);
	  akaQuery.add(new TermQuery(new Term(NAME_TYPE, TYPE_AKA_NAME)), BooleanClause.Occur.MUST);
	  akaQuery.add(new TermQuery(new Term(TITLE_ID, movieId)), BooleanClause.Occur.MUST);

      topDocs = mSearcher.search(akaQuery.build(), 1000);
      
      for (int i = 0; i < topDocs.scoreDocs.length; i++) {
	    scoreDoc = topDocs.scoreDocs[i];
	    if (scoreDoc != null) {
	      document = mSearcher.doc(scoreDoc.doc);
	      
          if ( movie.getTitle().compareTo(document.get(TITLE_NAME)) != 0) {
    	      movie.addAka(new ImdbAka(document.get(TITLE_NAME), "", releaseYear));	    	  
          }	      
	    }
	  }
      return movie;
  	  
  	} catch (IOException e) {
  	  e.printStackTrace();
  	}
  	return null;
  }


  public String getDatabaseSizeMB() {
    if (!isInitialised()) {
      return "0";
    }
    long size = 0;
    for (File f : mCurrentPath.listFiles()) {
      size = size + f.length();
    }
    return String.valueOf(size / (1024 * 1024));
  }

}
