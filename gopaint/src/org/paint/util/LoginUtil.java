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

package org.paint.util;

import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.paint.config.PantherDbInfo;
import org.paint.config.Preferences;
import org.paint.dataadapter.PantherServer;
import org.paint.dialog.PantherURLSelectionDlg;

import com.sri.panther.paintCommon.FixedInfo;
import com.sri.panther.paintCommon.TransferInfo;

public class LoginUtil {

	// stored when the settings are written into a property file
	private static boolean       logged_in = false;  // Indicates user login status.  Note:  This information is not stored when the settings are written into a property file
	private static String username = "gouser";
	private static String pw = "welcome";

	public static boolean login() {
		if (!logged_in) {
			login(username, pw);
		}
		return logged_in;
	}

	public static void logout() {
		logged_in = false;
	}
	
	private static Logger log = Logger.getLogger(LoginUtil.class);

	/**
	 * 
	 * @param results The first element in the vector is a String holding the user name to use.  The Second it an character array that contains the password.
	 * 
	 */
	public static void login(String username, String pw) {
		// Determine what databases and upl's are available from the server
		// Get information that does not change.
		logged_in = false;

		if (!InternetChecker.getInstance().isConnectionPresent(true)) {
			return;
		}
		String loginErrorMsg = "";

		String pantherURL = Preferences.inst().getPantherURL();
		log.debug("Logging in to Panther URL: " + pantherURL);
		FixedInfo fi = PantherServer.inst().getFixedInfoFromServer(pantherURL);
		if (fi == null){
			loginErrorMsg = PantherServer.inst().getServerStatus();
		} else {
			loginErrorMsg = PantherDbInfo.setFixedInfo(fi);
			if (loginErrorMsg.length() == 0) {
				String upload_version = PantherDbInfo.getDbAndVersionName();
				// Ensure user property file contains database and upl that is currently available from the server.
				if (!fi.dbUploadValid(upload_version)) {
					PantherURLSelectionDlg seldlg = new PantherURLSelectionDlg(GUIManager.getManager().getFrame());
					seldlg.display();
				}
			}
		}
		if (loginErrorMsg.length() == 0) {
			// Now that a valid database has been specified.  Verify user name and password from that database
			Vector<Object> results = new Vector<Object>();
			results.addElement(username);
			results.addElement(pw.toCharArray());

			Vector objs = new Vector();
			objs.addElement(results);
			objs.addElement(FixedInfo.getDb(PantherDbInfo.getDbAndVersionName()));

			Object  o = PantherServer.inst().sendAndReceive(Preferences.inst().getPantherURL(), "GetUserInfo", objs, null, null);

			if (o == null) {
				loginErrorMsg = "Unable to get user information";
			} else {
				Vector output = (Vector) o;
				TransferInfo  ti = (TransferInfo) output.elementAt(0);
				if (ti.getInfo() == null)
					loginErrorMsg = "Unable to verify user information";
				else
					loginErrorMsg = ti.getInfo();
			}
		}
		logged_in = loginErrorMsg.length() == 0;
		if (!logged_in)
			JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), loginErrorMsg);
	}

	public static boolean getLoggedIn() {
		return logged_in;
	}

	public static Vector<Object> getUserInfo() {
		Vector<Object> userInfo = new Vector<Object>();
		userInfo.addElement(username);
		userInfo.addElement(pw.toCharArray());
		return userInfo;
	}
}