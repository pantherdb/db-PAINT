/**
 *  Copyright 2019 University Of Southern California
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.paint.dataadapter;

import com.sri.panther.paintCommon.Book;
import com.sri.panther.paintCommon.FixedInfo;
import edu.usc.ksom.pm.panther.paintCommon.DataTransferObj;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.MSA;
import com.sri.panther.paintCommon.RawComponentContainer;
import edu.usc.ksom.pm.panther.paintCommon.SaveBookInfo;
import edu.usc.ksom.pm.panther.paintCommon.TaxonomyHelper;
import com.sri.panther.paintCommon.TransferInfo;
import com.sri.panther.paintCommon.User;
import edu.usc.ksom.pm.panther.paintCommon.VersionContainer;
import edu.usc.ksom.pm.panther.paintCommon.VersionInfo;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.swing.JOptionPane;
import org.bbop.framework.GUIManager;
import org.paint.config.PantherDbInfo;
import org.paint.config.Preferences;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.ProgressEvent;
import org.paint.main.PaintManager;

public class PantherServer {

    public static final String CHAR_ENCODING = "UTF-8";
    public static final String STRING_EMPTY = "";
    private static final String SUFFIX_PATH_VERSIONS = "/servlet/edu.usc.ksom.pm.panther.paintServer.servlet.DataServlet?action=versions";    

    public static final String MSG_ERROR_UNABLE_TO_LOCK_BOOKS = "Error unable to lock books";
    public static final String MSG_ERROR_CONCAT = "Server has returned the following error:  ";
    public static final String MSG_SUCCESS = new String();

    private static final String SERVLET_CONNECTION_CONTENT_TYPE = "Content-Type";
    private static final String SERVLET_CONNECTION_OBJECT_TYPE_JAVA = "java/object";
    private static final String SERVLET_REQUEST_PROPERTY_COOKIE = "Cookie";
    private static final String SERVLET_PATH = "/servlet/edu.usc.ksom.pm.panther.paintServer.servlet.DataServlet?action=";

    public static final String ACTION_GET_GO_HIERARCHY = "goHierarchy";
    public static final String ACTION_GET_VERSION_INFO = "versionInfo";
    public static final String ACTION_GET_MSA = "msa";
    public static final String ACTION_GET_TREE = "tree";
    public static final String ACTION_GET_NODES = "nodes";
    public static final String ACTION_GET_FAMILY_NAME = "familyName";
    public static final String ACTION_GET_FAMILY_DOMAIN = "familyDomain";    
    public static final String ACTION_GET_FAMILY_COMMENT = "familyComment";
    public static final String ACTION_SAVE_BOOK = "saveBook";

    public static final String REQUEST_SEARCH_GENE_NAME = "searchGeneName";
    public static final String REQUEST_SEARCH_GENE_EXT_ID = "searchGeneExtId";
    public static final String REQUEST_SEARCH_PROTEIN_EXT_ID = "searchProteinExtId";
    public static final String REQUEST_SEARCH_DEFINITION = "searchDefinition";
    public static final String REQUEST_SEARCH_BOOK_ID = "searchBookId";
    public static final String REQUEST_SEARCH_BOOK_PTN = "searchBookPTN";
    public static final String REQUEST_SEARCH_ALL_BOOKS = "allBooks";
    public static final String REQUEST_LOCK_BOOKS = "LockBooks";
    public static final String REQUEST_UNLOCK_BOOKS = "UnlockBooks";
    public static final String REQUEST_LOCK_UNLOCK_BOOKS = "LockUnLockBooks";
    public static final String REQUEST_MY_BOOKS = "MyBooks";
    public static final String REQUEST_SEARCH_UNCURATED_BOOKS = "uncuratedBooks";
    public static final String REQUEST_SEARCH_REQUIRE_PAINT_REVIEW_UNLOCKED = "requirePaintReviewUnlocked";

    public static final String REQUEST_OPEN_BOOK = "OpenBook";
//	public static final String REQUEST_OPEN_BOOK_FOR_GO_USER = "openBookForGOUsr";
//	public static final String REQUEST_CLS_INFO = "requestClsInfo";

//	public static final String REQUEST_GET_EVIDENCE_SF_LOCK = "getEvidenceSubfamilyLock";
//	public static final String REQUEST_GET_EVIDENCE_LEAF_LOCK = "getEvidenceALeafLock";
//	public static final String REQUEST_EVIDENCE_SAVE_SUBFAMILY = "saveSubFamilyEvidence";
//	public static final String REQUEST_EVIDENCE_SAVE_SEQUENCE = "saveSequenceEvidence";
//	public static final String REQUEST_UNLOCK_SEQUENCE = "unlockSequence";
//	public static final String REQUEST_UNLOCK_SUBFAMILY = "unlockSubFamily";
    public static final String SERVER_ERROR = "Server cannot access information, please contact Systems Administrator";

    public static String server_status;

    private static PantherServer INSTANCE = null;

    public static synchronized PantherServer inst() {
        if (INSTANCE == null) {
            INSTANCE = new PantherServer();
        }
        return INSTANCE;
    }

//	/**
//	 * Method declaration
//	 *
//	 *
//	 * @param fi
//	 * @param cp
//	 * @param userInfo
//	 * @param uplVersion
//	 * @param familyID
//	 *
//	 * @return
//	 *
//	 * @see
//	 */
//    public RawComponentContainer getRawPantherFam(Vector<? extends Object> userInfo, String familyID) {
//
//        String progressMessage = "Fetching protein family";
//
//        fireProgressChange(progressMessage, 0, ProgressEvent.Status.START);
//
//        Vector objs = new Vector();
//
//        objs.addElement(userInfo);
//        objs.addElement(PantherDbInfo.getDbAndVersionKey());
//        objs.addElement(familyID);
//        DataTransferObj dto = new DataTransferObj();
//        dto.setVc(PaintManager.inst().getVersionContainer());
//        String servletURL = Preferences.inst().getPantherURL();
//        DataTransferObj serverOutput = (DataTransferObj) sendAndReceive(servletURL, REQUEST_OPEN_BOOK, dto, null, null);
//
//        if (null == serverOutput) {
//            JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(),
//                    "Unable to open book",
//                    "Book Error",
//                    JOptionPane.ERROR_MESSAGE);
//            return null;
//        }
//        StringBuffer sb = serverOutput.getMsg();
//        if (null != sb && 0 != sb.length()) {
//            JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(),
//                    sb.toString(),
//                    "Book Error",
//                    JOptionPane.ERROR_MESSAGE);
//            return null;
//        }
//
//        Vector output = (Vector) serverOutput.getObj();
//        TransferInfo ti = (TransferInfo) output.elementAt(0);
//
//        if (0 != ti.getInfo().length()) {
//            System.out.println("Server cannot access information for transfer: " + ti.getInfo());
//            return null;
//        }
//
//        RawComponentContainer container = (RawComponentContainer) output.elementAt(1);
//
//        fireProgressChange(progressMessage, 100);
//
//        return container;
//
//    }

	public DataTransferObj searchGeneName(String servletURL, Object sendInfo,
			String sessionIdName,
			String sessionIdValue) {
		return doSearch(servletURL, REQUEST_SEARCH_GENE_NAME,
				sendInfo, sessionIdName, sessionIdValue);
	}

	public DataTransferObj searchGeneExtId(String servletURL, Object sendInfo,
			String sessionIdName,
			String sessionIdValue) {
		return doSearch(servletURL, REQUEST_SEARCH_GENE_EXT_ID,
				sendInfo, sessionIdName, sessionIdValue);
	}

	public DataTransferObj searchProteinExtId(String servletURL, Object sendInfo,
			String sessionIdName,
			String sessionIdValue) {
		return doSearch(servletURL, REQUEST_SEARCH_PROTEIN_EXT_ID,
				sendInfo, sessionIdName, sessionIdValue);
	}

	public DataTransferObj searchDefinition(String servletURL, Object sendInfo,
			String sessionIdName,
			String sessionIdValue) {
		return doSearch(servletURL, REQUEST_SEARCH_DEFINITION,
				sendInfo, sessionIdName, sessionIdValue);
	}
	public DataTransferObj searchBookId(String servletURL, Object sendInfo,
			String sessionIdName,
			String sessionIdValue) {
		return doSearch(servletURL, REQUEST_SEARCH_BOOK_ID,
				sendInfo, sessionIdName, sessionIdValue);
	}        
	public DataTransferObj searchBookPTN(String servletURL, Object sendInfo,
			String sessionIdName,
			String sessionIdValue) {
		return doSearch(servletURL, REQUEST_SEARCH_BOOK_PTN,
				sendInfo, sessionIdName, sessionIdValue);
	}
        
        public DataTransferObj searchRequirePaintReviewUnlocked(String servletURL, Object sendInfo,
			String sessionIdName,
			String sessionIdValue) {
		return doSearch(servletURL, REQUEST_SEARCH_REQUIRE_PAINT_REVIEW_UNLOCKED,
				sendInfo, sessionIdName, sessionIdValue);
	}

	public DataTransferObj searchAllBooks(String servletURL, Object sendInfo,
			String sessionIdName,
			String sessionIdValue) {
		return doSearch(servletURL, REQUEST_SEARCH_ALL_BOOKS,
				sendInfo, sessionIdName, sessionIdValue);
	}
        
    public  DataTransferObj getMyBooks(String servletURL, Object sendInfo,
                                           String sessionIdName,
                                           String sessionIdValue) {
        return (DataTransferObj)sendAndReceiveZip(servletURL, REQUEST_MY_BOOKS, sendInfo, sessionIdName, sessionIdValue);
        
    }
    
    public  DataTransferObj searchUncuratedBooks(String servletURL, Object sendInfo,
                    String sessionIdName,
                    String sessionIdValue) {
            return doSearch(servletURL, REQUEST_SEARCH_UNCURATED_BOOKS,
                            sendInfo, sessionIdName, sessionIdValue);
    }
    
    public DataTransferObj lockAndUnLockBooks(String servletURL, String actionRequest, Object sendInfo,
            String sessionIdName,
            String sessionIdValue) {
        return (DataTransferObj)sendAndReceiveZip(servletURL, actionRequest, sendInfo, sessionIdName, sessionIdValue);

//        if (null == serverOutput) {
//            return SERVER_ERROR;
//        }
//        TransferInfo ti = (TransferInfo) ((Vector) serverOutput).elementAt(0);
//        return ti.getInfo();
    }

    public DataTransferObj unlockBooks(String servletURL, Object sendInfo,
            String sessionIdName,
            String sessionIdValue) {
        return (DataTransferObj)lockAndUnLockBooks(servletURL, REQUEST_UNLOCK_BOOKS, sendInfo, sessionIdName, sessionIdValue);
    }

    private void fireProgressChange(String message, int percentageDone, ProgressEvent.Status status) {
//		ProgressEvent event = new ProgressEvent(PantherServer.class, message, percentageDone, status);
//		EventManager.inst().fireProgressEvent(event);
    }

    private void fireProgressChange(String message, int percentageDone) {
//		fireProgressChange(message, percentageDone, ProgressEvent.Status.RUNNING);
    }

    private DataTransferObj doSearch(String servletURL, String actionRequest, Object sendInfo, String sessionIdName, String sessionIdValue) {
        return (DataTransferObj) sendAndReceiveZip(servletURL, actionRequest, sendInfo, sessionIdName, sessionIdValue);
    }

    public String getServerStatus() {
        return server_status;
    }

    public static void setServerStatus(String serverStatus) {
        server_status = serverStatus;
    }
        
    protected Object sendAndReceiveZip(String completeServletPath, Object sendInfo, String sessionIdName, String sessionIdValue) {
        String message = null;
        Object outputFromServlet = null;
        try {

            String progressMessage = "Fetching zip data";
            fireProgressChange(progressMessage, 0, ProgressEvent.Status.START);

            // connect to the servlet
            URL servlet
                    = new URL(completeServletPath);
            java.net.URLConnection servletConnection = servlet.openConnection();

            servletConnection.setRequestProperty(SERVLET_CONNECTION_CONTENT_TYPE, SERVLET_CONNECTION_OBJECT_TYPE_JAVA);

            // Set the session id, if necessary
            if ((null != sessionIdName) && (null != sessionIdValue)) {
                servletConnection.setRequestProperty(SERVLET_REQUEST_PROPERTY_COOKIE, sessionIdName + "=".concat(sessionIdValue));
            }

            // Connection should ignore caches if any
            servletConnection.setUseCaches(false);

            // Indicate sending and receiving information from the server
            servletConnection.setDoInput(true);
            servletConnection.setDoOutput(true);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new GZIPOutputStream(servletConnection.getOutputStream()));
            fireProgressChange(progressMessage, 50);

            objectOutputStream.writeObject(sendInfo);
            objectOutputStream.flush();
            objectOutputStream.close();
            ObjectInputStream servletOutput = new ObjectInputStream(new GZIPInputStream(servletConnection.getInputStream()));
            outputFromServlet = servletOutput.readObject();
            fireProgressChange(progressMessage, 100, ProgressEvent.Status.END);

            servletOutput.close();
            return outputFromServlet;
        } catch (MalformedURLException muex) {
            message = ("MalformedURLException " + muex.getMessage()
                    + " has been returned while sending and receiving information from server");
            System.out.println(message);
            muex.printStackTrace();
        } catch (IOException ioex) {
            message = ("IOException " + ioex.getMessage()
                    + " has been returned while sending and receiving information from server");
            System.out.println(message);
        } catch (Exception e) {
            message = ("Exception " + e.getMessage()
                    + " has been returned while sending and receiving information from server");
            System.out.println(message);
        }
        if (message != null) {
            // Oh dear
            EventManager.inst().fireProgressEvent(new ProgressEvent(this, message, 0, ProgressEvent.Status.FAIL));
        }
        return outputFromServlet;
    }

    protected Object sendAndReceiveZip(String servletURL, String actionRequest, Object sendInfo, String sessionIdName, String sessionIdValue) {
        String message = null;
        Object outputFromServlet = null;
        try {

            String progressMessage = "Fetching zip data";
            fireProgressChange(progressMessage, 0, ProgressEvent.Status.START);

            // connect to the servlet
            URL servlet
                    = new URL(servletURL + SERVLET_PATH + actionRequest);
            java.net.URLConnection servletConnection = servlet.openConnection();

            servletConnection.setRequestProperty(SERVLET_CONNECTION_CONTENT_TYPE, SERVLET_CONNECTION_OBJECT_TYPE_JAVA);

            // Set the session id, if necessary
            if ((null != sessionIdName) && (null != sessionIdValue)) {
                servletConnection.setRequestProperty(SERVLET_REQUEST_PROPERTY_COOKIE, sessionIdName + "=".concat(sessionIdValue));
            }

            // Connection should ignore caches if any
            servletConnection.setUseCaches(false);
            servletConnection.setRequestProperty("Accept-Encoding", "gzip");        // Indicate we are accepting gzip format

            // Indicate sending and receiving information from the server
            servletConnection.setDoInput(true);
            servletConnection.setDoOutput(true);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new GZIPOutputStream(servletConnection.getOutputStream()));
            fireProgressChange(progressMessage, 50);

            objectOutputStream.writeObject(sendInfo);
            objectOutputStream.flush();
            objectOutputStream.close();
            ObjectInputStream servletOutput = new ObjectInputStream(new GZIPInputStream(servletConnection.getInputStream()));
            System.out.println("Received object from server for action request " + actionRequest);
            System.out.println("Going to start reading the object");
//                        System.out.println("Number of bytes available for reading " + servletOutput.available());
            outputFromServlet = servletOutput.readObject();
            System.out.println("Finished reading the object from the server");
            fireProgressChange(progressMessage, 100, ProgressEvent.Status.END);

            servletOutput.close();
            return outputFromServlet;
        } catch (MalformedURLException muex) {
            message = ("MalformedURLException " + muex.getMessage()
                    + " has been returned while sending and receiving information from server");
            System.out.println(message);
            muex.printStackTrace();
        } catch (IOException ioex) {
            message = ("IOException " + ioex.getMessage()
                    + " has been returned while sending and receiving information from server");
            System.out.println(message);
            ioex.printStackTrace();
        } catch (Exception e) {
            message = ("Exception " + e.getMessage()
                    + " has been returned while sending and receiving information from server");
            System.out.println(message);
            e.printStackTrace();
        }
        if (message != null) {
            // Oh dear
            EventManager.inst().fireProgressEvent(new ProgressEvent(this, message, 0, ProgressEvent.Status.FAIL));
        }
        return outputFromServlet;
    }

	/**
	 * Method declaration
	 *
	 *
	 * @param servletURL
	 * @param actionRequest
	 * @param sendInfo
	 * @param sessionIdName
	 * @param sessionIdValue
	 *
	 * @return
	 *
	 * @see
	 */
	public Object sendAndReceive(String servletURL, String actionRequest, Object sendInfo,
			String sessionIdName, String sessionIdValue){
		String message = null; // if no message, then it's all lovely
		Object            outputFromServlet = null;
		try{
			// connect to the servlet
			URL                     servlet =
				new URL(servletURL +  SERVLET_PATH + actionRequest);
			java.net.URLConnection  servletConnection = servlet.openConnection();

			servletConnection.setRequestProperty("Content-Type", "application/octet-stream");

			// Set the session id, if necessary
			if ((null != sessionIdName) && (null != sessionIdValue)){
				servletConnection.setRequestProperty("Cookie", sessionIdName + "=".concat(sessionIdValue));
			}

			// Connection should ignore caches if any
			servletConnection.setUseCaches(false);

			// Indicate sending and receiving information from the server
			servletConnection.setDoInput(true);
			servletConnection.setDoOutput(true);
			ObjectOutputStream  objectOutputStream = new ObjectOutputStream(servletConnection.getOutputStream());

			objectOutputStream.writeObject(sendInfo);
			objectOutputStream.flush();
			objectOutputStream.close();
			ObjectInputStream servletOutput = new ObjectInputStream(new GZIPInputStream(servletConnection.getInputStream()));
			outputFromServlet= servletOutput.readObject();

			servletOutput.close();
		}
		catch (MalformedURLException muex){
			message = ("MalformedURLException " + muex.getMessage()
					+ " has been returned while sending and receiving information from server");
			System.out.println(message);
			muex.printStackTrace();
		}
		catch (IOException ioex){
			message = ("IOException " + ioex.getMessage()
					+ " has been returned while sending and receiving information from server");
			System.out.println(message);
		}
		catch (Exception e){
			message = ("Exception " + e.getMessage()
					+ " has been returned while sending and receiving information from server");
			System.out.println(message);
		}
		if (message != null) {
			// Oh dear
			EventManager.inst().fireProgressEvent(new ProgressEvent(this, message, 0, ProgressEvent.Status.FAIL));
		}
		return outputFromServlet;			
	}
        
        public DataTransferObj getFamilyComment(String serverPath, DataTransferObj dto) {
            server_status = "";
            return (DataTransferObj)sendAndReceiveZip(serverPath, ACTION_GET_FAMILY_COMMENT, dto, null, null);
        }
        
        public DataTransferObj getFamilyName(String serverPath, DataTransferObj dto) {
            server_status = "";
            return (DataTransferObj)sendAndReceiveZip(serverPath, ACTION_GET_FAMILY_NAME, dto, null, null);
        }
        
        public DataTransferObj getFamilyDomain(String serverPath, DataTransferObj dto) {
            server_status = "";
            return (DataTransferObj)sendAndReceiveZip(serverPath, ACTION_GET_FAMILY_DOMAIN, dto, null, null);
        }        
        
        public DataTransferObj getTree(String serverPath, DataTransferObj dto) {
            server_status = "";
            return (DataTransferObj)sendAndReceiveZip(serverPath, ACTION_GET_TREE, dto, null, null);
        }
        
        public DataTransferObj getNodes(String serverPath, DataTransferObj dto) {
            server_status = "";
            Object serverOutput = sendAndReceiveZip(serverPath, ACTION_GET_NODES, dto, null, null);
            if (null == serverOutput) {
                System.out.println("Nodes information from server is null");
            }
            return (DataTransferObj)serverOutput;
        }
        
        public DataTransferObj getMSA(String serverPath, DataTransferObj dto) {
            server_status = "";
            return (DataTransferObj)sendAndReceiveZip(serverPath, ACTION_GET_MSA, dto, null, null);
//            return (MSA)serverOutput;
        }

        public DataTransferObj saveBook(String serverPath, DataTransferObj dto) {
            server_status = "";
            return (DataTransferObj)sendAndReceiveZip(serverPath, ACTION_SAVE_BOOK, dto, null, null);
//            return (String)serverOutput;
        }        

    public VersionContainer getVersions(String serverPath) {
        server_status = "";
        try {
			// try to get if from the session
            // connect to the servlet
            URL servlet = new URL(serverPath + SUFFIX_PATH_VERSIONS);
            System.out.println("Getting versions from " + serverPath);
            URLConnection servletConnection = servlet.openConnection();

            // Don't used a cached version of URL connection.
            servletConnection.setUseCaches(false);
            servletConnection.setDefaultUseCaches(false);
            //
            // The servlet will return a Data Transfer Object 
            //
            ObjectInputStream inputFromServlet = new ObjectInputStream(new GZIPInputStream(servletConnection.getInputStream()));
            DataTransferObj dto = (DataTransferObj) inputFromServlet.readObject();
            inputFromServlet.close();
            return dto.getVc();
        } catch (MalformedURLException muex) {
            muex.printStackTrace();
            setServerStatus(muex.getLocalizedMessage());
        } catch (IOException ioex) {
            ioex.printStackTrace();
            setServerStatus(ioex.getLocalizedMessage());
        } catch (ClassNotFoundException cnfex) {
            cnfex.printStackTrace();
            setServerStatus(cnfex.getLocalizedMessage());
        }
        return null;
    }  
        
        public VersionInfo getVersionInfo(String serverPath) {
		VersionInfo vi = null;
		server_status = "";
		try {
			// try to get if from the session
			// connect to the servlet
			URL servlet = new URL(serverPath + "/servlet/edu.usc.ksom.pm.panther.paintServer.servlet.DataServlet?action=versionInfo");
                        System.out.println("Getting version info  from " + serverPath);
			URLConnection servletConnection = servlet.openConnection();

			// Don't used a cached version of URL connection.
			servletConnection.setUseCaches(false);
			servletConnection.setDefaultUseCaches(false);
			// Read the input from the servlet.
			//
			// The servlet will return a serialized vector containing a DataTransfer object
			//
			ObjectInputStream   inputFromServlet = new ObjectInputStream(new GZIPInputStream(servletConnection.getInputStream()));
			vi = (VersionInfo) inputFromServlet.readObject();
			inputFromServlet.close();
            } catch (MalformedURLException muex) {
                muex.printStackTrace();
                setServerStatus(muex.getLocalizedMessage());
            } catch (IOException ioex) {
                ioex.printStackTrace();
                setServerStatus(ioex.getLocalizedMessage());
            } catch (ClassNotFoundException cnfex) {
                cnfex.printStackTrace();
                setServerStatus(cnfex.getLocalizedMessage());
            }
		
            return vi;            
        }
        
        public GOTermHelper getGOTermHelper(String serverPath) {
		GOTermHelper goTermHelper  = null;

		try {
			// try to get if from the session
			// connect to the servlet
			URL servlet = new URL(serverPath + "/servlet/edu.usc.ksom.pm.panther.paintServer.servlet.DataServlet?action=goHierarchy");
                        System.out.println("Connecting to " + serverPath);
			URLConnection servletConnection = servlet.openConnection();

			// Don't used a cached version of URL connection.
			servletConnection.setUseCaches(false);
			servletConnection.setDefaultUseCaches(false);
			// Read the input from the servlet.
			//
			// The servlet will return a serialized vector containing a DataTransfer object
			//
			ObjectInputStream   inputFromServlet = new ObjectInputStream(new GZIPInputStream(servletConnection.getInputStream()));
			goTermHelper = (GOTermHelper) inputFromServlet.readObject();
			inputFromServlet.close();
		}
		catch (MalformedURLException muex){
			setServerStatus(muex.getLocalizedMessage());
		}
		catch (IOException ioex) {
			setServerStatus(ioex.getLocalizedMessage());
		}
		catch (ClassNotFoundException cnfex){
			setServerStatus(cnfex.getLocalizedMessage());
		}
		
		return goTermHelper;            
        }
        
    public DataTransferObj getCuratableBooks(String serverPath, DataTransferObj dto) {
        return (DataTransferObj)sendAndReceiveZip(serverPath + "/servlet/edu.usc.ksom.pm.panther.paintServer.servlet.DataServlet?action=booksWithExpEvdnce", dto, null, null);
//        HashSet<String> bookSet = null;
//        try {
//			// try to get if from the session
//            // connect to the servlet
//            URL servlet = new URL(serverPath + "/servlet/edu.usc.ksom.pm.panther.paintServer.servlet.DataServlet?action=booksWithExpEvdnce");
//            System.out.println("Connecting to " + serverPath + " Get curatable books information");
//            URLConnection servletConnection = servlet.openConnection();
//
//            // Don't used a cached version of URL connection.
//            servletConnection.setUseCaches(false);
//            servletConnection.setDefaultUseCaches(false);
//            
//            // Read the input from the servlet.
//            ObjectInputStream inputFromServlet = new ObjectInputStream(new GZIPInputStream(servletConnection.getInputStream()));
//            bookSet = (HashSet<String>) inputFromServlet.readObject();
//            inputFromServlet.close();
//        } catch (MalformedURLException muex) {
//            setServerStatus(muex.getLocalizedMessage());
//            muex.printStackTrace();
//            bookSet = null;
//        } catch (IOException ioex) {
//            setServerStatus(ioex.getLocalizedMessage());
//            ioex.printStackTrace();
//            bookSet = null;
//        } catch (ClassNotFoundException cnfex) {
//            setServerStatus(cnfex.getLocalizedMessage());
//            cnfex.printStackTrace();
//            bookSet = null;
//        }
//
//        return bookSet;
    }    
        
    public TaxonomyHelper getTaxonomyHelper(String serverPath) {
        TaxonomyHelper taxonHelper = null;

        try {
			// try to get if from the session
            // connect to the servlet
            URL servlet = new URL(serverPath + "/servlet/edu.usc.ksom.pm.panther.paintServer.servlet.DataServlet?action=taxonomyConstraints");
            System.out.println("Connecting to " + serverPath + " Get taxonomy constraints information");
            URLConnection servletConnection = servlet.openConnection();

            // Don't used a cached version of URL connection.
            servletConnection.setUseCaches(false);
            servletConnection.setDefaultUseCaches(false);
            
            // Read the input from the servlet.
            ObjectInputStream inputFromServlet = new ObjectInputStream(new GZIPInputStream(servletConnection.getInputStream()));
            taxonHelper = (TaxonomyHelper) inputFromServlet.readObject();
            inputFromServlet.close();
        } catch (MalformedURLException muex) {
            setServerStatus(muex.getLocalizedMessage());
            muex.printStackTrace();
            taxonHelper = null;
        } catch (IOException ioex) {
            setServerStatus(ioex.getLocalizedMessage());
            ioex.printStackTrace();
            taxonHelper = null;
        } catch (ClassNotFoundException cnfex) {
            setServerStatus(cnfex.getLocalizedMessage());
            cnfex.printStackTrace();
            taxonHelper = null;
        }

        return taxonHelper;
    }        
        

	public FixedInfo getFixedInfoFromServer(String serverPath) {
		Vector objs = null;
		server_status = "";
		try {
			// try to get if from the session
			// connect to the servlet
			URL servlet = new URL(serverPath + "/servlet/com.sri.panther.paintServer.servlet.Client2Servlet?action=FixedInfo");
			URLConnection servletConnection = servlet.openConnection();

			// Don't used a cached version of URL connection.
			servletConnection.setUseCaches(false);
			servletConnection.setDefaultUseCaches(false);
			// Read the input from the servlet.
			//
			// The servlet will return a serialized vector containing a DataTransfer object
			//
			ObjectInputStream   inputFromServlet = new ObjectInputStream(new GZIPInputStream(servletConnection.getInputStream()));
			objs = (Vector) inputFromServlet.readObject();
			inputFromServlet.close();
		}
		catch (MalformedURLException muex){
			setServerStatus(muex.getLocalizedMessage());
		}
		catch (IOException ioex) {
			setServerStatus(ioex.getLocalizedMessage());
		}
		catch (ClassNotFoundException cnfex){
			setServerStatus(cnfex.getLocalizedMessage());
		}
		if (null != objs) {
			TransferInfo ti = (TransferInfo)objs.elementAt(0);
			if (0 != ti.getInfo().length()) {
				setServerStatus("Server cannot access information for transfer: " + ti.getInfo());
				return null;
			}
			return (FixedInfo)objs.elementAt(1);
		}
		return null;
	}         

//	public Vector<? extends Object> listFamilies(Vector<? extends Object> vector, String dbClsId) {
//		Vector  objs = new Vector();
//
//		objs.addElement(vector);
//		objs.addElement(dbClsId);
//		Vector  returnInfo = new Vector();
//
//		try{
//
//			// connect to the servlet
//			URL               servlet =
//				new URL(Preferences.inst().getPantherURL()
//						+ "/servlet/com.sri.panther.paintServer.servlet.Client2Servlet?action=BookList");
//			HttpURLConnection servletConnection = (HttpURLConnection) servlet.openConnection();
//
//			servletConnection.setRequestMethod("POST");
//
//			// Connection should ignore caches if any
//			servletConnection.setUseCaches(false);
//
//			// Indicate sending and receiving information from the server
//			servletConnection.setDoInput(true);
//			servletConnection.setDoOutput(true);
//			servletConnection.setRequestProperty("Content-Type", "java/object");
//			ObjectOutputStream  objectOutputStream = new ObjectOutputStream(servletConnection.getOutputStream());
//
//			objectOutputStream.writeObject(objs);
//			objectOutputStream.flush();
//			objectOutputStream.close();
//
//			ObjectInputStream inputFromServlet =
//				new ObjectInputStream(new GZIPInputStream(servletConnection.getInputStream()));
//			Object            inputFromServer = inputFromServlet.readObject();
//
//			inputFromServlet.close();
//			if (null != inputFromServer){
//				TransferInfo  ti = (TransferInfo) ((Vector) inputFromServer).elementAt(0);
//
//				if (0 != ti.getInfo().length()){
//					returnInfo.addElement(ti.getInfo());
//					return returnInfo;
//				}
//				Vector  books = (Vector) ((Vector) inputFromServer).elementAt(1);
//				String  bookList[] = new String[books.size()];
//
//				books.copyInto(bookList);
//				returnInfo.addElement("");
//				returnInfo.addElement(bookList);
//				return returnInfo;
//			}
//			returnInfo.addElement("Server did not return any information");
//			return returnInfo;
//		}
//		catch (MalformedURLException muex){
//			muex.printStackTrace();
//			returnInfo.addElement("System error, please contact system administrator");
//			return returnInfo;
//		}
//		catch (IOException ioex){
//			ioex.printStackTrace();
//			returnInfo.addElement("System error, please contact system administrator");
//			return returnInfo;
//		}
//		catch (Exception e){
//			e.printStackTrace();
//			returnInfo.addElement("System error, please contact system administrator");
//			return returnInfo;
//		}
//	}
        
    	 public User getUserInfo(String serverPath, Vector userInfo){
            Vector userInfoList = (Vector)sendAndReceiveZip(serverPath, "userInfo", userInfo, null, null); 
            if (null == userInfoList || 0 == userInfoList.size()) {
                            return null;
            }
            return (User)userInfoList.get(0);            
                
	 }    

}

