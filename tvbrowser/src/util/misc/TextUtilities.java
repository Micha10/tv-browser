package util.misc;

import java.util.HashSet;

/**
 * A class with useful functions for text.
 * 
 * @author René Mach
 * @since 4.2.7
 */
public final class TextUtilities {
  
  /**
   * Removes multiple identical entries from haystack with entries separated by lineSeparatorHaystack
   * and returns the cleaned text with lines separated by lineSeparatorTarget.
   * 
   * @param haystack The text to search for multiple identical entries.
   * @param lineSeparatorHaystackRegEx The separator used in haystack to separate entries from each other as regular expression.
   * @param lineSeparatorTarget The separator used in result to separate entries from each other.
   * @return The cleaned text with lines separated by lineSeparatorTarget.
   */
  public static String removeDoubletLines(final String haystack, final String lineSeparatorHaystackRegEx, final String lineSeparatorTarget) {
    final String[] parts = haystack.strip().split(lineSeparatorHaystackRegEx);
    final HashSet<String> added = new HashSet<String>();
    final StringBuilder result = new StringBuilder();
    
    for(String part : parts) {
      if(!added.contains(part)) {
        added.add(part);
        
        if(result.length() > 0) {
          result.append(lineSeparatorTarget);
        }
        
        result.append(part);
      }
    }
    
    return result.toString();
  }
}
