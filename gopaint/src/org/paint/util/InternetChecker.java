package org.paint.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.CharBuffer;
import java.util.Date;

import org.apache.log4j.Logger;

public class InternetChecker {

	private static final String URL_TO_CHECK = "http://www.google.com/";
	private static final long CHECK_EVERY = 30 * 60 * 1000; /* 30 minutes */

	private static final int BUF_SIZE = 1024;
	private static InternetChecker ref;
	private Date lastCheck;
	private Boolean lastState;
	private final Logger log = Logger.getLogger(InternetChecker.class.getName());

	public InternetChecker() {
	}

	public synchronized static InternetChecker getInstance() {
		if (ref == null) {
			ref = new InternetChecker();
		}
		return ref;
	}

	private boolean isStateValid() {
		if (lastState == null)
			return false;
		if (lastCheck.getTime() > System.currentTimeMillis() + CHECK_EVERY)
			return false;
		return true;
	}

	public boolean isConnectionPresent() {
		if (!isStateValid()) {
			checkConnection();
		}
		return lastState;
	}

	public boolean isConnectionPresent(boolean forceCheck) {
		if (forceCheck)
			invalidateState();
		return isConnectionPresent();
	}

	private void invalidateState() {
		lastCheck = null;
		lastState = null;
	}

	public void checkConnection() {
		lastCheck = new Date();
		try {
			URL url = new URL(URL_TO_CHECK);
			URLConnection urlConnection = url.openConnection();

			InputStream inputStream = urlConnection.getInputStream();
			Reader reader = new InputStreamReader(inputStream);

			StringBuilder contents = new StringBuilder();
			CharBuffer buf = CharBuffer.allocate(BUF_SIZE);

			while (true) {
				reader.read(buf);
				if (!buf.hasRemaining())
					break;

				contents = contents.append(buf);
			}
			inputStream.close();
			lastState = true;
		} catch (Exception e) {
			System.out.println("Internet connectivity not present.");
			lastState = false;
		}
	}

}
