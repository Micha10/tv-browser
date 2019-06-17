/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

/**
 * A class with alphabetically sorted properties in properties file.
 * <p>
 * @author René Mach
 * @since 4.1
 */
public class PropertiesSorted extends Properties {
	private static final Comparator<Object> COMPARATOR = new Comparator<Object>() {
		@Override
		public int compare(Object o1, Object o2) {
			return String.valueOf(o1).compareToIgnoreCase(String.valueOf(o2));
		}
	};
	
	private static final Comparator<java.util.Map.Entry<Object, Object>> COMPARATOR_ENTRIES = new Comparator<>() {
		@Override
		public int compare(java.util.Map.Entry<Object, Object> o1, java.util.Map.Entry<Object, Object> o2) {			
			return String.valueOf(o1.getKey()).compareToIgnoreCase(String.valueOf(o2.getKey()));
		}
	};
	
	/**
     * Loads properties from a properties file.
     * <p>
     * @param source The properties file to load from.
     * @return The Properties loaded from the file.
     */
    public static final PropertiesSorted load(final File source) {
    	final PropertiesSorted prop = new PropertiesSorted();
    	
    	try(FileInputStream in = new FileInputStream(source)) {
    		prop.load(in);
    	} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return prop;
    }
	
    @Override
    public Set<java.util.Map.Entry<Object, Object>> entrySet() {
    	final TreeSet<java.util.Map.Entry<Object, Object>> result = new TreeSet<>(COMPARATOR_ENTRIES);
    	
    	for(java.util.Map.Entry<Object, Object> entry : super.entrySet()) {
    	  result.add(entry);
    	}
    	
    	return Collections.synchronizedSet(result);
    }
    
    @Override
    public Set<Object> keySet() {
    	final TreeSet<Object> result = new TreeSet<>(COMPARATOR);
    	
    	for(Object key : super.keySet()) {
    		result.add(key);
    	}
    	
    	return Collections.synchronizedSet(result);
    }
    
    @Override
	public Enumeration<Object> keys() {
	    final Enumeration<Object> keysEnum = super.keys();
	    final Vector<Object> keyList = new Vector<Object>();
	    
	    while(keysEnum.hasMoreElements()){
	      keyList.add(keysEnum.nextElement());
	    }
	    
	    Collections.sort(keyList, COMPARATOR);
	    
	    for(Object o : keyList) {
	    	System.out.println(o);
	    }
	    
	    return keyList.elements();
	}
	
	/**
     * Stores properties to file.
     * <p>
     * @param target The target file to store the properties in.
     * @param comments The comments for the properties file's header.
     * @return <code>true</code> if the properties could be stored, <code>false</code> otherwise.
     */
    public final boolean store(final File target, final String comments) {
    	boolean result = false;
    	
    	try(FileOutputStream out = new FileOutputStream(target)) {
    		store(out, comments);
    		result = true;
    	} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return result;
    }
}
