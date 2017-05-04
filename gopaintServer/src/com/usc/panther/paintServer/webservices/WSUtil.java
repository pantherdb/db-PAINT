package com.usc.panther.paintServer.webservices;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class WSUtil {
    public static Element createTextNode(Document doc, String elementTag, String text) {
        if (null == doc || null == elementTag || null == text) {
            return null;
        }
        Element elem = doc.createElement(elementTag);
        Text t = doc.createTextNode(text);
        elem.appendChild(t);
        return elem;
    }
}
