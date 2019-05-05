package imdbplugin;

public class ImdbAka {
  private String akaTitle;
  private int year;

  public ImdbAka(String title, int year) {
    this.akaTitle = title;  
    this.year = year;
  }

  public String getTitle() {
    return akaTitle;
  }

  public int getYear() {
    return year;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof ImdbAka)) {
      return false;
    }
    ImdbAka otherAka = (ImdbAka) obj;
    if (year != otherAka.year) {
      return false;
    }
    if (!akaTitle.equalsIgnoreCase(otherAka.akaTitle)) {
      return false;
    }
    return true;
  }
  
  @Override
  public int hashCode() {
    int hash = akaTitle.hashCode() + year;

    return hash;
  }
}
