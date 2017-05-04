package org.paint.config;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CustomTermList {

	private static CustomTermList singleton;

	private Set<String> exclusionList;
	
	public static CustomTermList getSingleton() {
		if (singleton == null) {
			singleton = new CustomTermList("config/custom_term_list.xml");
		}
		return singleton;
	}
	
	public Set<String> getExclusionList() {
		return exclusionList;
	}
	
	private CustomTermList(String xml) throws CustomTermListException {
		this(new File(xml));
	}
	
	private CustomTermList(File xml) throws CustomTermListException {
		exclusionList = new HashSet<String>();
		parseXML(xml);
	}
	
	private void parseXML(File xml) throws CustomTermListException {
		Document doc = null;
		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = db.parse(xml);
		}
		catch (Exception e) {
			throw new CustomTermListException("Error reading custom term list: " + e.getMessage());
		}
		NodeList exclusionNodes = doc.getElementsByTagName("term_exclusion");
		for (int i = 0; i < exclusionNodes.getLength(); ++i) {
			Node exclusionNode = exclusionNodes.item(i);
			NodeList termNodes = exclusionNode.getChildNodes();
			for (int j = 0; j < termNodes.getLength(); ++j) {
				Node term = termNodes.item(j);
				if (term.getNodeName().equals("term")) {
					NamedNodeMap attrs = term.getAttributes();
					Node id = attrs.getNamedItem("id");
					if (id != null) {
						exclusionList.add(id.getNodeValue());
					}
				}
			}
		}
	}
	
	public class CustomTermListException extends RuntimeException {
		
		private static final long serialVersionUID = 1L;

		public CustomTermListException(String msg) {
			super(msg);
		}
		
	}
}
