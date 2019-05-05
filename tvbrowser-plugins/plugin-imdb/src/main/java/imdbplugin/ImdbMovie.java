package imdbplugin;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

public class ImdbMovie {
  private String title;
  private String originalTitle;
  private int year;
  private HashSet<ImdbAka> akaList = new HashSet<ImdbAka>();
 
  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public void setOriginalTitle(String originalTitle) {
	this.originalTitle = originalTitle;
  }

  public String getOriginalTitle() {
   return originalTitle;
  }
  
  public void setYear(int year) {
    this.year = year;
  }

  public int getYear() {
    return year;
  }

  public void addAka(ImdbAka imdbAka) {
    akaList.add(imdbAka);
  }

  public ImdbAka[] getAkas() {
    ImdbAka[] result = akaList.toArray(new ImdbAka[akaList.size()]);
    Arrays.sort(result, new Comparator<ImdbAka>() {

      public int compare(ImdbAka aka1, ImdbAka aka2) {
        return aka1.getTitle().compareToIgnoreCase(aka2.getTitle());
      }});
    return result;
  }
}
