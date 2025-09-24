/**
 * Copyright 2025 University Of Southern California
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

import edu.usc.ksom.pm.panther.paintCommon.DataTransferObj;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.Organism;
import edu.usc.ksom.pm.panther.paintCommon.PAINTVersion;
import edu.usc.ksom.pm.panther.paintCommon.TaxonomyHelper;
import edu.usc.ksom.pm.panther.paintCommon.VersionContainer;
import edu.usc.ksom.pm.panther.paintCommon.VersionInfo;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
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

    Runnable mainRun = new Runnable() {
        // this thread runs in the AWT event queue
        public void run() {
            try {
                
                // Initialize GUI manager
                GUIManager.getManager().addStartupTask(new PaintStartupTask(args));
                GUIManager.getManager().start();
                // While the GUI manager is initializing, get fixed information from the server.  If we cannot connect or get information exit.
                Preferences preference = Preferences.inst();
                PantherServer pServer = PantherServer.inst();
                String pantherURL = preference.getPantherURL();
                // Ensure client and server versions of data and software are in sync
                VersionContainer vc = pServer.getVersions(pantherURL);
                if (null == vc) {
                    JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Unable to verify version information from server", "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(-1);                    
                }
                boolean clientVersionOk = checkVersion(vc, PAINTVersion.getPAINTClientversion());
                VersionInfo commonInfo = new VersionInfo();
                commonInfo.setVersionId(PAINTVersion.getPAINTCommonVersion());
                if (null != commonInfo.compareTo(vc.get(VersionContainer.VersionedObj.CLIENT_SERVER_COMMON_VERSION))) {
                    JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Please download latest version of software", "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(-1);
                }

                if (false == clientVersionOk) {
                    int response = JOptionPane.showConfirmDialog(GUIManager.getManager().getFrame(), "PAINT version is not the same as version available for download from the server, continue?", "Warning", JOptionPane.YES_NO_OPTION);
                    if (JOptionPane.NO_OPTION == response) {
                        System.exit(0);
                    }
                }

                GOTermHelper gth = pServer.getGOTermHelper(pantherURL);
                TaxonomyHelper th = pServer.getTaxonomyHelper(pantherURL);
                // Get list of curatable books
                DataTransferObj dto = new DataTransferObj();
                dto.setVc(vc);

//                System.out.println(new Date() + " Going to retrieve curatable book information from server");
//                DataTransferObj serverObj = pServer.getCuratableBooks(pantherURL, dto);                
//                System.out.println(new Date() + " Retrieved curatable book information from server");                
//                if (null == serverObj) {
//                    JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Unable to retrieve list of curatable books from server", "Error", JOptionPane.ERROR_MESSAGE);
//                    System.exit(-1);
//                }
//                StringBuffer sb = serverObj.getMsg();
//                if (null != sb && 0 != sb.length()) {
//                    JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Download new version from server \n" + sb.toString(), "Error", JOptionPane.ERROR_MESSAGE);
//                    System.exit(-1);
//                }
//                
//                HashSet<String> bookSet = (HashSet<String>)serverObj.getObj();
                
                // All genomes
                //DataTransferObj serverObj = pServer.getCuratableBooks(pantherURL, dto);
                dto = new DataTransferObj();
                dto.setVc(vc);
                DataTransferObj serverObj = pServer.getAllOrganisms(pantherURL, dto);
                ArrayList<Organism> orgList = (ArrayList<Organism>)serverObj.getObj();
                System.out.println(new Date() + " Retrieved organism list from server");                
                if (null == serverObj) {
                    JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Unable to retrieve list of organisms from server", "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(-1);
                }
                StringBuffer sb = serverObj.getMsg();
                if (null != sb && 0 != sb.length()) {
                    JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Error retrieving list of supported organisms \n" + sb.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(-1);
                }
                
                // Bacterial genomes
                dto = new DataTransferObj();
                dto.setVc(vc);
                serverObj = pServer.getBacterialGenomes(pantherURL, dto);
                ArrayList<String> bacteriaList = (ArrayList<String>)serverObj.getObj();
                System.out.println(new Date() + " Retrieved bacterial genomes list from server");                
                if (null == serverObj) {
                    JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Unable to retrieve bacterial genomes from server", "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(-1);
                }
                sb = serverObj.getMsg();
                if (null != sb && 0 != sb.length()) {
                    JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Error retrieving plant genomes \n" + sb.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(-1);
                }                

                // Repeat for plant genomes
                dto = new DataTransferObj();
                dto.setVc(vc);
                serverObj = pServer.getPlantGenomes(pantherURL, dto);
                ArrayList<String> plantList = (ArrayList<String>)serverObj.getObj();
                System.out.println(new Date() + " Retrieved plant genomes list from server");                
                if (null == serverObj) {
                    JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Unable to retrieve plant genomes from server", "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(-1);
                }
                sb = serverObj.getMsg();
                if (null != sb && 0 != sb.length()) {
                    JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Error retrieving plant genomes \n" + sb.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(-1);
                }                 
                
                if (null == vc.get(VersionContainer.VersionedObj.CLS_VERSION) || null == gth) {
                    JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Unable to get static information from server", "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(-1);
                }
                if (null == th) {
                    JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Unable to retrieve taxonomy constraints information, annotate without taxonomy constraints?", "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(-1);
                }
                
                
                PaintManager pm = PaintManager.inst();
                pm.setupFixedInfo(gth, th, vc, orgList, bacteriaList, plantList);
                System.out.println(new Date() + " Done with initialization");

                GUIManager.addVetoableShutdownListener(new VetoableShutdownListener() {
                    public boolean willShutdown() {
                        if (true == DirtyIndicator.inst().bookUpdated()) {
                            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(GUIManager.getManager().getFrame(), "Book has been updated, save before closing?", "Book Updated", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)) {
                                PaintManager.inst().saveCurrent();
                            }
                        }
                        return true;
                    }
                });
                //Preferences.inst().warnUserDevEnv();

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
            } catch (Exception e) { // should catch RuntimeException
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
        
        
    public boolean checkVersion(VersionContainer vc, String clientVersion) {
        VersionInfo vo = vc.get(VersionContainer.VersionedObj.CLIENT_VERSION);
        if (null == vo) {
            return false;
        }
        String versionId = vo.getVersionId();
        if (null != versionId && null != clientVersion && versionId.equals(clientVersion)) {
            return true;
        }
        return false;
    }

    public static String getAppName() {
        /*
         * If you want the version # included, then use getAppId
         */
        return "Paint";
    }
}
