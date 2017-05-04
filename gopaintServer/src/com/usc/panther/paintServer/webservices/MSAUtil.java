package com.usc.panther.paintServer.webservices;

import com.sri.panther.paintCommon.util.Utils;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Copied class into this package and made applicable updates
 */
public class MSAUtil {

    protected static final String PREFIX_SEQ_START = ">";
    protected static final int    SEGMENTS = 25;
    protected static final int    SUB_SEGMENTS = 5;
    protected static final int    FIRST_ROW = 0;
    protected static int    PIR_INDEX = 0;
    protected static int    WTS_INDEX = 1;
    protected static final String DELIM_WTS = " ";
    protected static final String STR_SPACE = " ";
    protected static final String STR_ID_PREFIX = "id";
    
    // Temporary until sequence id problem is resolved
    public static final String TOKEN_SEQ = "\n"; // Temporarily assume sequence does not have any other information appended to it //", \n/|";
    public static final String DELIM_DB_IDENTIFIER = "=";
    protected static final String SEQ_DELIM = "\\n";
    protected static final String NAME_DELIM = "\\|";
    protected static final String DBNAME = "\\S*=";
    
    public static void parsePIRForNonGO(Vector msaInfo,  Vector<AnnotationNode> nodes){
      if (null == msaInfo || null == nodes){
        return;
      }
      

      //Vector    pirInfo = new Vector();
      
      String[]  seqInfo = (String[]) msaInfo.elementAt(PIR_INDEX);

//      int       maxIdDIsplayLen = 0;
//      int       maxLen = 0;

      for (int i = 0; i < seqInfo.length; i++){
        if ((true == seqInfo[i].startsWith(PREFIX_SEQ_START)) && (i + 1 < seqInfo.length)
                && (false == seqInfo[i + 1].startsWith(PREFIX_SEQ_START))){
          //Vector          seq = new Vector();
          String          seqLbl = seqInfo[i].substring(1, seqInfo[i].length());

          // Handle case where sequence label has other information appended to it
          StringTokenizer st = new StringTokenizer(seqLbl, TOKEN_SEQ);

          if (st.hasMoreTokens()){
            seqLbl = st.nextToken();
          }
          //seqLbl = seqLbl.substring(seqLbl.lastIndexOf(DELIM_DB_IDENTIFIER) + 1);
          //seq.addElement(seqLbl);

          // Check for case where sequence does not end in current line.
          int           start = i + 1;
          StringBuffer  sb = new StringBuffer();

          while (start < seqInfo.length){
            if (null != seqInfo[start]){
              if (true == seqInfo[start].startsWith(PREFIX_SEQ_START)){
                break;
              }
              sb.append(seqInfo[start].trim());
              start++;
              i++;
            }
          }
          String seqStr = sb.toString();
          //seq.addElement(seqStr);
          //pirInfo.addElement(seq);


          
          // Add sequence information to node
          boolean found = false;
          for (int j = 0; j < nodes.size(); j++) {
            AnnotationNode aNode = nodes.elementAt(j);
            //String nodeName = aNode.getNodeName();
            //String annotationId = aNode.getAccession();
             if ( true ==seqLbl.equals(aNode.getAnnotationNodeId())) {

//            if (true == seqLbl.equals(aNode.getNodeName()) || true == seqLbl.equals(aNode.getAccession()) || true ==seqLbl.equals(aNode.getAnnotationNodeId())) {
                aNode.setSequence(seqStr);
//                // Set the display length
//                String displayName = aNode.getDisplayName();
//                if (null == displayName) {
//                    displayName = annotationId;
//                }
//                int length = displayName.length();
//                if (maxIdDIsplayLen < length) {
//                    maxIdDIsplayLen = length;
//                }
                found = true;
                //System.out.println(annotationId + " has sequence information");
                break;
            }
            
//            if (null != nodeName) {
//                Double weight = seqToWts.get(nodeName);
//                if (null != weight) {
//                    aNode.setSequenceWt(weight.doubleValue());
//                }
//            }
          }
          if (found == false) {
              System.out.println("MSA parser error " + seqLbl + " does not have corresponding node in tree file");
          }
          


//            // Get the maximum sequence length
//            if (maxLen < sb.toString().length()){
//              maxLen = sb.toString().length();
//            }
        }
      }
      
//      msa.setSeqMaxLen(maxLen);

//      // Add header
//      Vector<String>        header = new Vector<String>(2);
//      StringBuffer  idStr = new StringBuffer(STR_ID_PREFIX);
//
//    
//      // Add space between label and alignment
//      maxIdDIsplayLen++;
//      
//      while (idStr.length() < maxIdDIsplayLen){
//        idStr.append(STR_SPACE);
//      }
//      idStr.append(STR_SPACE);    // Add an extra space as a separator
//      header.addElement(idStr.toString());
//      header.addElement(getSeqHeader(maxLen));
//      pirInfo.insertElementAt(header, 0);
//      msa.setHeader(0, (String)header.elementAt(0));
//      msa.setHeader(1, (String)header.elementAt(1));
//      Vector  rtnInfo = new Vector();
//
//      rtnInfo.addElement(pirInfo);
//      if (true ==  msa.seqWtAvailable()){
//        rtnInfo.addElement(seqToWts);
//      }
//      else{
//        rtnInfo.addElement(new Hashtable());
//      }
//      return rtnInfo;
    }

}
