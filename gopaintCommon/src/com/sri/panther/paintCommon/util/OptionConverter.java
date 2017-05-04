 /* Copyright (C) 2009 SRI International
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

import java.util.Properties;
import java.util.ResourceBundle;

public class OptionConverter{
	static String DELIM_START = "${";
	static char   DELIM_STOP = '}';
	static int DELIM_START_LEN = 2;
	static int DELIM_STOP_LEN  = 1;
	
	/* OptionConverter is a static class */
	private OptionConverter(){}
	
	public static String findAndSubst(String key, Properties props){
		String value = props.getProperty(key);
		if(value == null)
			return null;
		return substVars(value, props);
	}
	
	public static String findAndSubst(String key, ResourceBundle bundle){
		String value = bundle.getString(key);
		if(value == null)
			return null;
		return substVars(value, bundle);
	}


	public static String substVars(String val, Properties props){
		int j, k; // indexes for DELIM_START & DELIM_STOP
		
		j = val.indexOf(DELIM_START, 0);
		k = val.indexOf(DELIM_STOP, j);
		
		String key = val.substring(j + DELIM_START_LEN,k);
		// try the system properties
		String replacement = System.getProperty(key,null);
		// try the props parameter
		if(replacement == null && props != null){
			replacement = props.getProperty(key);
		}
		if(replacement != null){
			return (val.substring(0,j) + replacement + val.substring(k + DELIM_STOP_LEN,val.length()));
		}    
		
		return replacement;	
	}
	
	public static String substVars(String val, ResourceBundle bundle){
		int j, k; // indexes for DELIM_START & DELIM_STOP
		
		j = val.indexOf(DELIM_START, 0);
		if(j < 0)
			return val;
		k = val.indexOf(DELIM_STOP, j);
		
		String key = val.substring(j + DELIM_START_LEN,k);
		// try the system properties
		String replacement = System.getProperty(key,null);

		// try the props parameter
		if(replacement == null && bundle != null){
			replacement = bundle.getString(key);
		}
		if(replacement != null){
			return (val.substring(0,j) + replacement + val.substring(k + DELIM_STOP_LEN,val.length()));
		}
		
		return replacement;
	}
}