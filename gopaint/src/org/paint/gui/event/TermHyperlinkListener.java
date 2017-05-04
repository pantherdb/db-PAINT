package org.paint.gui.event;

import java.net.URL;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.paint.util.HTMLUtil;


/**
 * TermHyperlink Listener, listens for clicks on term & external
 * hyper links and brings up the term or brings up the external web page
 */

public class TermHyperlinkListener implements HyperlinkListener {

	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (!(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)) 
			return;

		URL url = e.getURL();

		if (url == null) { // relative urls are null
			return;
		}

		HTMLUtil.bringUpInBrowser(url);

	}

} // end of class TermHyperlinkListener


