/**
 * Copyright 2017 University Of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.paint.main;

import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.TaxonomyHelper;
import edu.usc.ksom.pm.panther.paintCommon.VersionInfo;
import java.util.HashSet;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.bbop.framework.GUIManager;
import org.bbop.framework.VetoableShutdownListener;
import org.bbop.util.OSUtil;
import org.paint.config.Preferences;
import org.paint.dataadapter.PantherServer;
import org.paint.gui.DirtyIndicator;

public class PAINT {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Thread runner;

	private static String[] args;


	/**
	 * Method declaration
	 *
	 *
	 * @param args
	 *
	 * @see
	 */
	public static void main(String[] args) {
		PAINT.args = args;
		//if PAINT is launched on a Mac, set the system property to setup the correct
		//application menu name
		if (OSUtil.isMacOSX()) {
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", getAppName());
		}

		PAINT theRunner = new PAINT();

		SwingUtilities.invokeLater(theRunner.mainRun);
	}

	Runnable mainRun =
		new Runnable() {
		// this thread runs in the AWT event queue
		public void run() {
			try {                                                                
                            GUIManager.getManager().addStartupTask(new PaintStartupTask(args));
                            GUIManager.getManager().start();

                            // While the GUI manager is initializing, get fixed information from the server.  If we cannot connect or get information exit.
                            Preferences preference = Preferences.inst();
                            PantherServer pServer = PantherServer.inst();
                            String pantherURL = preference.getPantherURL();
                            VersionInfo vi = pServer.getVersionInfo(pantherURL);
                            GOTermHelper gth = pServer.getGOTermHelper(pantherURL);
                            TaxonomyHelper th = pServer.getTaxonomyHelper(pantherURL);
                            HashSet<String> bookSet = pServer.getCuratableBooks(pantherURL);
                            if (null == vi || null == gth) {
                                JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Unable to get static information from server", "Error", JOptionPane.ERROR_MESSAGE);
                                System.exit(-1);
                            }
                            if (null == th) {
                                int response = JOptionPane.showConfirmDialog(GUIManager.getManager().getFrame(), "Unable to retrieve taxonomy constraints information, annotate without taxonomy constraints?", "Warning", JOptionPane.YES_NO_OPTION);
                                if (JOptionPane.NO_OPTION == response) {
                                    System.exit(0);
                                }

                            }
                            PaintManager pm = PaintManager.inst();
                            pm.setupFixedInfo(vi, gth, th, bookSet);
                            
                            GUIManager.addVetoableShutdownListener(new VetoableShutdownListener () {
                                public boolean willShutdown() {
                                    if (true == DirtyIndicator.inst().bookUpdated()) {
                                        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(GUIManager.getManager().getFrame(), "Book has been updated, save before closing?", "Book Updated", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)) {
                                            PaintManager.inst().saveCurrent();
                                        }
                                    }
                                    return true;
                                }
                            });
                            
                               // These don't seem to work
                                
//                            JFrame frame = GUIManager.getManager().getFrame();
//                            frame.addWindowListener(new java.awt.event.WindowAdapter() {
//                                @Override
//                                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
//                                    if (true == DirtyIndicator.inst().bookUpdated()) {
//                                        PaintManager.inst().saveCurrent();
//                                    }
//                                }
//                            });                               
                                
//                                
//                                GUIManager.addShutdownHook(new Runnable() {
//                                        public void run() {
//                                            if (true == DirtyIndicator.inst().bookUpdated()) {
//                                                PaintManager.inst().saveCurrent();
//                                            }
//                                        }
//                                });                                
                                
                                
                                
			}
			catch (Exception e) { // should catch RuntimeException
				JOptionPane.showMessageDialog(
						null,
						e,
						"Warning",
						JOptionPane.WARNING_MESSAGE
				);
				e.printStackTrace();
				System.exit(2);
			}
		}
	};

	public static String getAppName() {
		/*
		 * If you want the version # included, then use getAppId
		 */
		return "Paint";
	}
}
