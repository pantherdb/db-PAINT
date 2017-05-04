/* Copyright (C) 2008 SRI International
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.sri.panther.paintCommon.util;


import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

public class StringUtils {

	protected static Logger log = Logger.getLogger(StringUtils.class);

	public StringUtils() {
	}


    public static String[] formatString(String s, int len) {
        int lastIndex = (s.length() / len) + 1;
        String[] strArray = new String[lastIndex];

        for (int i = 0; i < (lastIndex - 1); i++) {
            strArray[i] = s.substring((i * len), ((i + 1) * len));
        }
        strArray[lastIndex - 1] =
                s.substring(((lastIndex - 1) * len), s.length());
        return strArray;
    }

    /**
     * Only replace the first occurance
     */
	public static String replaceFirst(String sOriginal, String sToken, String sReplace) {
		int index = sOriginal.indexOf(sToken);
		String result = sOriginal;
		if (index != -1)
			result = sOriginal.substring(0, index) + sReplace + sOriginal.substring(index + sToken.length());
		return result;
	}

	public static Vector compareStringToVectorHash(Hashtable newTbl, Hashtable origTbl) {
		Hashtable toAddTbl = new Hashtable();
		Hashtable toRemoveTbl = new Hashtable();
		Enumeration eList = newTbl.keys();
		// Create a list of items to add
		while (eList.hasMoreElements()) {
			String key = (String)eList.nextElement();
			Vector newList = (Vector)newTbl.get(key);
			Vector origList = (Vector)origTbl.get(key);
			if (null == origList) {
				toAddTbl.put(key, newList);
				continue;
			}
			if (newList.size() != origList.size()) {
				toAddTbl.put(key, newList);
				toRemoveTbl.put(key, origList);
				continue;
			}
			// The sizes of the lists are same, make sure the lists contain the same elements
			String newProtArray[] = new String[newList.size()];
			String origArray[] = new String [origList.size()];
			newList.copyInto(newProtArray);
			origList.copyInto(origArray);

			Arrays.sort(newProtArray);
			Arrays.sort(origArray);
			for (int i = 0; i < newProtArray.length; i++) {
				if (0 != newProtArray[i].compareTo(origArray[i])) {
					// Since the associated list has changed, this entry has to be removed from the old table
					// and added into the new table
					toAddTbl.put(key, newList);
					toRemoveTbl.put(key, origList);
					break;
				}
			}
		}

		// Now create list of items to remove
		eList = origTbl.keys();
		while (eList.hasMoreElements()) {
			String sfName = (String)eList.nextElement();
			if (null == newTbl.get(sfName)) {
				toRemoveTbl.put(sfName, origTbl.get(sfName));
			}
		}

		if(toAddTbl.isEmpty() && (toRemoveTbl.isEmpty())) {
			return null;
		}

		Vector returnList = new Vector(2);
		returnList.addElement(toAddTbl);
		returnList.addElement(toRemoveTbl);
		return returnList;
	}



	/**
	 * transform a Vector of Strings into a delimiter-separated list in a String
	 *
	 * @param list -- the Vector of Strings
	 * @param wrapper -- enclose each element in a pair of these characters (ie, single quotes)
	 * @param delim -- the list delimiter (ie, comma)
	 * @return the delimiter-separated String
	 */
	public static String listToString(Vector list, String wrapper, String delim){
		int           size = list.size();
		StringBuffer  selection = new StringBuffer();

		// add each item in the Vector to the SB with wrapper and delimiter
		for (int i = 0; i < size - 1; i++){
			selection.append(wrapper);
			selection.append((String) list.elementAt(i));
			selection.append(wrapper);
			selection.append(delim);
		}

		// add last item in the Vector to the SB with wrapper but no delimiter
		selection.append(wrapper);
		selection.append((String) list.elementAt(size - 1));
		selection.append(wrapper);
		return selection.toString();
	}



  
 
  
    public synchronized static String[] concatenateArrays(String[] firstPart, String[] secondPart){

        int fpl = firstPart.length;
        int spl = secondPart.length;
    
        String[] ret = new String[fpl + spl];
                      
        System.arraycopy(firstPart, 0, ret, 0, fpl);
        System.arraycopy(secondPart, 0, ret, fpl, spl);
    
        return ret;

    }

	/**
	 * Compares two lists of strings
	 * @param list1
	 * @param list2
	 * @return  Returns true if two arrays of strings contain the same strings.  Returns false if either list is null.
	 */
	public static boolean compareLists(String list1[], String list2[]) {
		if (null == list1 ||
				null == list2) {
			return false;
		}
		int size = list1.length;
		if (size != list2.length) {
			return false;
		}

		java.util.Arrays.sort(list1);
		java.util.Arrays.sort(list2);
		boolean found = false;
		for (int i = 0; i < size; i++) {
			found = false;
			for (int j = 0; j < size; j++) {
				if (0 == list1[i].compareTo(list2[j])) {
					found = true;
					break;
				}
			}
			if (false == found) {
				return false;
			}
		}
		return true;
	}
        
        
        public static boolean stringsSame(String s1, String s2) {
            return s1 == null ? s2 == null : s1.equals(s2); 
        }
        
        
        

}