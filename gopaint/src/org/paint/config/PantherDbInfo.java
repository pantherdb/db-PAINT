/* 
 * 
 * Copyright (c) 2010, Regents of the University of California 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Neither the name of the Lawrence Berkeley National Lab nor the names of its contributors may be used to endorse 
 * or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package org.paint.config;

import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.paint.util.StringConstant;

import com.sri.panther.paintCommon.FixedInfo;

public class PantherDbInfo {

	private static PantherDbInfo INSTANCE = null;

	protected static Logger log = Logger.getLogger(PantherDbInfo.class);

	private static FixedInfo fixedInfo;    // Information from server/file system

	/*
	 * Members associated with upload version. 
	 * Hashtable of hashtables.  
	 * First hashtable level contains database names to upload hashtable.  
	 * Second hashtable level contains upload version number to vector of upload version string and release date.
	 */ 
	protected static Hashtable<String, Hashtable<String, Vector<String>>> dbToUPLInfo;
	private static String currentDB;
	private static String currentVersionKey;

	// Exists only to defeat instantiation.
	private PantherDbInfo() {
		fixedInfo = FixedInfo.createFixedInfo();
	}

	public static synchronized PantherDbInfo getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PantherDbInfo();
		}
		return INSTANCE;
	}

	/**
	 * Method declaration
	 * dbClsID is the db name concatenated with a pipe to the version. e.g. dev_3_panther_upl|UPL 8.0
	 *
	 * @return
	 *
	 * @see
	 */
	public static String getDbAndVersionName() {
		if (currentDB != null && currentVersionKey != null)
			return currentDB + StringConstant.PIPE + getCurrentVersionName();
		else 
			return null;
	}

	public static String getVersionKey() {
		return currentVersionKey;
	}
	
	public static String getCurrentDB() {
		if (currentDB != null)
			return currentDB;
		else
			return "";
	}

	public static String getDbAndVersionKey() {
		if (currentDB != null && currentVersionKey != null)
			return currentDB + StringConstant.PIPE + currentVersionKey;
		else 
			return null;	
	}
	
	public static String getCurrentVersionName() {
		if (dbToUPLInfo != null && currentDB != null && currentVersionKey != null)
			return dbToUPLInfo.get(currentDB).get(currentVersionKey).firstElement();
		else
			return "";
	}

	public static Hashtable<String, Hashtable<String, Vector<String>>> getVersions() {
		return dbToUPLInfo;
	}

	public static String setFixedInfo(FixedInfo fi) {
//		String error_msg = "";
//		dbToUPLInfo = null;
//		currentDB = null;
//		currentVersionKey = null;
//		String version_pref = Preferences.inst().getUploadVersion();
//		if (fi != null) {
//			fixedInfo = fi;
//			dbToUPLInfo = (Hashtable<String, Hashtable<String, Vector<String>>>) fixedInfo.getDbToUploadInfo();
//			if (dbToUPLInfo != null) {
//				// Get upl version from server
//				Set<String> db_names = dbToUPLInfo.keySet();
//				for (String db_name : db_names) {
//					if (currentDB == null)
//						currentDB = db_name;
//					Hashtable<String, Vector<String>> versions = dbToUPLInfo.get(db_name);
//					Set<String> version_keys = versions.keySet();
//					for (String version_key : version_keys) {
//						Vector<String> version_date = versions.get(version_key);
//						if (currentVersionKey == null) {
//							currentVersionKey = version_key;
//						}
//						if (version_pref == null || (version_pref != null && version_date.firstElement().equals(version_pref))) {
//							currentDB = db_name;
//							currentVersionKey = version_key;
//						}
//					}
//				}
//				Preferences.inst().setUploadVersion(getDbAndVersionName());
//			} else {
//				error_msg = "Unable to retrieve upload version information from Panther DB server";
//			}
//		} else {
//			error_msg = "Unable to find Panther DB server";
//		}
//		return error_msg;
            return null;
	}

	public static FixedInfo getFixedInfo() {
		return fixedInfo;
	}

}