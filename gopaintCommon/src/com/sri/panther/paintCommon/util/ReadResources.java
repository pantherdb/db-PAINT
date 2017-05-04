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

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

public class ReadResources
{
	protected boolean isInitialized = false;
	protected String[] resourceNames;
	private ResourceBundle[] bundles;
	private ResourceBundle bundle;  //bundle with found property


	public ReadResources()
	{
	}

	public ReadResources(String name) throws Exception {
		String[] names = {name};
		initialize(names);
	}

	public ReadResources(String[] names) throws Exception {
		initialize(names);
	}

	protected void initialize(String[] names) throws Exception{
		resourceNames = names;
		isInitialized = false;               // default to false
		bundles = new ResourceBundle[names.length];

		String resourceName = null;
		try{
			for (int i=0; i < resourceNames.length; i++) {
				resourceName = resourceNames[i];
				bundles[i] = ResourceBundle.getBundle(resourceName);
			}
		} catch(MissingResourceException mrex){
			StringBuffer error = new StringBuffer("Can't find ");
			error.append(resourceName).append(" Maybe not in your CLASSPATH").append("\r\n");
			error.append("CLASSPATH used : ").append(System.getProperty("java.class.path"));
			throw new Exception(error.toString());
		}    
		isInitialized = true;
	}

	public void setResource(String name) throws Exception {
		String[] names = {name};
		setResource(names);
	}

	public void setResource(String[] names) throws Exception 
	{
		boolean reinitializeFlag = false;
		if (resourceNames == null)
			reinitializeFlag = true;
		else if (names.length != resourceNames.length)
			reinitializeFlag = true;
		else {
			//loop through both arrays of resource names and make sure they are identical
			for (int i=0; i < names.length; i++) {
				if (! names[i].equals(resourceNames[i])) {
					reinitializeFlag = true;
					break;
				}
			}
		}

		if (reinitializeFlag)
			initialize(names);
	}


	public boolean isInitialized() {
		return isInitialized;
	}

	public String getKey(String key) throws Exception { 
		return getKey(key,null);
	}

	public String getKey(String key, String defaultValue) throws Exception{
		bundle = null;
		String result = null;
		for (int i=0; i < bundles.length; i++) {
			try {  
				result = bundles[i].getString(key);
				if (result != null) {
					//for result, need to know corresponding bundle
					//store so can get later
					bundle = bundles[i];
					break;
				}
			}  catch(MissingResourceException mrex){
				//no point in doing anything with te exception.  if not get result
				//print throw exception below
			}
		}

		if (result == null) {
			if (defaultValue == null) 
				throw new Exception("Key " + key + " doesn't exist in property file(s)"); 
			else
				result = defaultValue;
		}

		if(bundle == null)
			throw new Exception("resource Name not Setup");

		return result;
	}


	public Enumeration<String> getKeys() throws Exception{
		if (bundles == null){
			throw new Exception("resource Name not Setup");
		}
		else{
			Vector<String> allKeys = new Vector<String> ();
			//loop through all bundles and get all keys
			Enumeration<String> e;
			for (int i=0; i < bundles.length; i++) { 
				e = bundles[i].getKeys();
				while(e.hasMoreElements()) {
					allKeys.addElement(e.nextElement());
				}
			}
			return allKeys.elements();
		}
	}

	//use to get at bundle with property
	public ResourceBundle getBundle(){
		return bundle;
	}


}
