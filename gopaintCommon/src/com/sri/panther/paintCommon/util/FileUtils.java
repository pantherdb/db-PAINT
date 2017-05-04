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



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import org.apache.log4j.Logger;


public class FileUtils {
	protected static Logger log = Logger.getLogger(FileUtils.class.getName());
	public static String[] readFile(String fileName) {
		Vector contents = new Vector();
		BufferedReader bufReader = null;
		String line;
		boolean error = false;

		try {
			bufReader = new BufferedReader(new FileReader(fileName));
			line = bufReader.readLine();
			while (line != null) {
				contents.addElement(line);
				line = bufReader.readLine();
			}
		}
		catch (IOException ioex) {
			error = true;

			log.error("Exception " + ioex.getMessage() + " returned while attempting to read file " + fileName);

		}
		finally {
			try {
				if (null != bufReader) {
					bufReader.close();
				}
			}
			catch (IOException ioex2) {
				error = true;

				log.error("Exception " + ioex2.getMessage() + " returned while attempting to close file " + fileName);

			}
		}
		if (true == error) {
			return null;
		}
		String [] returnArray = new String[contents.size()];
		contents.copyInto(returnArray);
		return returnArray;
	}

	public static String[] readFileFromURL(URL url) throws IOException {
		BufferedReader    bufReader = null;
		String            line = "not inititialized";
		Vector            v = new Vector();
		String[]        returnArray;

		try {

			bufReader = new BufferedReader(new InputStreamReader(url.openStream()));
			line = bufReader.readLine();
			while (line != null) {
				v.addElement(line);
				line = bufReader.readLine();
			}
		}
		catch (IOException ioex) {
			System.out.println("Exception while reading");
			ioex.printStackTrace();
			throw ioex;
		}
		finally {
			try {
				bufReader.close();
			}
			catch (IOException ioex2) {
				System.out.println("Exception while closing");
				ioex2.printStackTrace();
				throw ioex2;
			}
		}
		returnArray = new String[v.size()];
		v.copyInto(returnArray);
		return returnArray;
	}


	/**
	 * **************** METHOD HAS NOT BEEN TESTED
	 * @throws IOException 
	 */
	public static void writeFile(String fileName, String[] contents) throws IOException {
		BufferedWriter bufWriter = null;
		String line;

		bufWriter = new BufferedWriter(new FileWriter(fileName));
		for (int i = 0; i < contents.length; i++) {
			line = contents[i];
			bufWriter.write(line);
		}

		if (null != bufWriter) {
			bufWriter.close();
		}

	}

	public static String getPath (String fileName) {
		File f = new File (fileName);
		if (f.isDirectory()) {
			return f.getPath();
		}
		int separatorIndex = f.getPath().lastIndexOf(f.separator);
		if (-1 == separatorIndex) {
			return null;
		}
		return f.getPath().substring(0, separatorIndex);
	}

	public static String getFileName(String path) {
		File f = new File(path);
		return f.getName();
	}

	public static String getURLPath(String url) {
		int separatorIndex = url.lastIndexOf(File.separator);
		if (-1 == separatorIndex) {
			return null;
		}
		return url.substring(0, separatorIndex);
	}

	public static String appendFileToPath(String path, String fileName) {
		return path + File.separator + fileName;
	}

	public static String appendPathToPath(String path1, String path2) {
		return path1 + File.separator + path2;
	}

	public static boolean validPath(String path) {
		if (null == path) {
			return false;
		}
		File f = new File(path);
		return f.canRead();
	}

	public static boolean validURLPath(String url) {

		URL newURL;
		try {
			newURL = new URL(url);
		}
		catch (MalformedURLException e) {
			return false;
		}

		InputStream is = null;
		boolean returnVal = false;

		try {
			is = newURL.openStream();
			returnVal = true;
		}
		catch (IOException ioex) {
			ioex.printStackTrace();
		}
		finally {
			try {
				if (null != is) {
					is.close();
				}
			}
			catch (IOException ioex2) {
				ioex2.printStackTrace();
			}
		}
		return returnVal;
	}

	public FileUtils() {
	}
        
    public static String[] getSubDirectories(File dir){
            String[]  directories = null;
            Vector<String>    dirVector;

            if (dir.isDirectory()){
                    directories = dir.list();
                    dirVector = new Vector<String> (directories.length);
                    for (int i = 0; i < directories.length; i++){
                            if ((new File(dir.getAbsolutePath() + File.separator + directories[i])).isDirectory()){
                                    dirVector.addElement(directories[i]);
                            }
                    }
                    directories = new String[dirVector.size()];
                    dirVector.copyInto(directories);

            }
            return directories;
    }
}