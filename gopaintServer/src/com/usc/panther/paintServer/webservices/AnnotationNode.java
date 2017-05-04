package com.usc.panther.paintServer.webservices;



import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnnotationNode {

    String accession;   //PTHR10000:AN5
    String publicId;    // PTN1234
    String eventType;   // Speciation, duplication
    String nodeType;    // Information from tree file
    String familyId;    // PTHR10000
    String familyName;  // protease
    String annotationNodeId;    // AN5
    String branchLength;    //0.123
    String parentId;    // public id, null for root nodes
    String sequence;    // DELAXXXGVXXXVAXITXXAMXGEXDFXXXLXXRLXLLXGXXXXXXXXXXXKXXXTXGXXXLXXXLKXXGXXXXLVSGGFXXXAXXVAXXLGXDYAYANXLEFXXDXGXL
    Vector children;    // Vector of annotation nodes
    AnnotationNode parent;
    String type;        // Info from tree file such as [&&NHX:Ev=0>1:S=LUCA:ID=AN109]
    String nodeName;    // YEAST|SGD=S000003440|UniProtKB=P42941 (for leaves) or SGD:S000003440
    String longName;    // YEAST|SGD=S000003440|UniProtKB=P42941
    String referenceSpeciationEvent;
    String geneSymbol;
    String geneName;
    String sfId;
    String sfName;
    Node node;              // Only reason for this is because the annotation information is stored in this structure
    public static final String NODE_TYPE_DUPLICATION = "1>0";
    public static final String NODE_TYPE_HORIZONTAL_TRANSFER="0>0";
    public static final String NODE_TYPE_SPECIES = "S=";
    public static final String NODE_TYPE_ANNOTATION = "ID=";
    public static final int NODE_TYPE_ANNOTATION_LENGTH = NODE_TYPE_ANNOTATION.length();
    public static final String NODE_TYPE_INFO_SEPARATOR = ":";
    public static final String NODE_TYPE_INFO_PREFIX = "[";
    public static final String NODE_TYPE_INFO_SUFFIX = "]";
    
    public static final String DELIM_FAMILY_AN_ID = ":";
    
    
    //public static final Pattern PATTERN_LONG_GENE_NAME = Pattern.compile("[A-Z0-9]{3,5}\\|[A-Za-z0-9_\\-]+=[A-Za-z0-9=_\\-.]+\\|[A-Za-z0-9_\\-]+=[A-Za-z0-9_\\-.]+");
    //                                                                  3 - 5 upper case letters followed by |   [A-Z]{3,5}\|
    //                                                                  one or more times letters or numbers, _ , - followed by =     [A-Za-z0-9_\-]+=
    //                                                                  one or more times letters or numbers, =, _, -, ., followed by | [A-Za-z0-9=_\-.]+\|
    //                                                                  one or more times letters or numbers, _ , - followed by = 
    //                                                                  one or more times letters or numbers, _, -, .| [A-Za-z0-9=_\-.]+ 
    
     public static final String DELIM_NAME = "\\|";


    public AnnotationNode() {
    }
    
    public static String constructFullAnnotationId(String familyId, String anId) {
        return familyId + DELIM_FAMILY_AN_ID + anId;
    }
    
    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventType() {
        return eventType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setFamilyId(String familyId) {
        this.familyId = familyId;
    }

    public String getFamilyId() {
        return familyId;
    }

    public void setAnnotationNodeId(String annotationNodeId) {
        this.annotationNodeId = annotationNodeId;
    }

    public String getAnnotationNodeId() {
        return annotationNodeId;
    }

    public void setBranchLength(String branchLength) {
        this.branchLength = branchLength;
    }

    public String getBranchLength() {
        return branchLength;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getSequence() {
        return sequence;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getAccession() {
        return accession;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setChildren(Vector children) {
        this.children = children;
    }

    public Vector getChildren() {
        return children;
    }

    public void setParent(AnnotationNode parent) {
        this.parent = parent;
    }

    public AnnotationNode getParent() {
        return parent;
    }

    public void setType(String type) {
        this.nodeType = type;
        String annotId = this.getAnnotIdFromNodeInfo();
        if (null != annotId) {
            this.annotationNodeId = annotId;
        }
    }

    private String getAnnotIdFromNodeInfo() {
        if (null == nodeType) {
            return null;
        }
        int index = nodeType.indexOf(NODE_TYPE_ANNOTATION);
        if (index < 0) {
            return null;
        }
        int endIndex = nodeType.indexOf(NODE_TYPE_INFO_SEPARATOR, index);
        if (-1 == endIndex) {
            endIndex = nodeType.indexOf(NODE_TYPE_INFO_SUFFIX);
        }
        return new String(nodeType.substring(index + NODE_TYPE_ANNOTATION_LENGTH, endIndex));

    }

    public boolean isDuplicationNode() {
        if (null == nodeType) {
            return false;
        }
        int index = nodeType.indexOf(NODE_TYPE_DUPLICATION);
        if (index < 0) {
            return false;
        }
        return true;
    }

    public boolean isHorizontalTransferNode() {
        if (null == nodeType) {
            return false;
        }
        int index = nodeType.indexOf(NODE_TYPE_HORIZONTAL_TRANSFER);
        if (index < 0) {
            return false;
        }
        return true;
    }

    public String getSpecies() {
        if (null == nodeType) {
            return null;
        }
        int length = nodeType.length();
        int index = nodeType.indexOf(NODE_TYPE_SPECIES);

        if (index < 0 || index >= length) {
            return null;
        }
        index +=  NODE_TYPE_SPECIES.length();
        int endIndex = nodeType.indexOf(NODE_TYPE_INFO_SEPARATOR, index);
        if (-1 == endIndex) {
            endIndex = nodeType.indexOf(NODE_TYPE_INFO_SUFFIX);
        }
        if (endIndex < index || endIndex >= length) {
            return null;
        }
        return new String(nodeType.substring(index, endIndex));
    }

    public String getType() {
        return type;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setReferenceSpeciationEvent(String referenceSpeciationEvent) {
        this.referenceSpeciationEvent = referenceSpeciationEvent;
    }

    public String getReferenceSpeciationEvent() {
        return referenceSpeciationEvent;
    }

    public void setGeneSymbol(String geneSymbol) {
        this.geneSymbol = geneSymbol;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public void setSfId(String sfId) {
        this.sfId = sfId;
    }

    public String getSfId() {
        return sfId;
    }

    public void setSfName(String sfName) {
        this.sfName = sfName;
    }

    public String getSfName() {
        return sfName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public String getGeneName() {
        return geneName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }
    
    public String getSpeciesFromLongName() {
        if (null == longName) {
            return null;
        }
        String parts[] = longName.split(DELIM_NAME);
        int length = parts.length;
        if (length < 2) {
            return null;
        }
        return parts[0];
    }
    
    public String getProteinPartFromLongName() {
        if (null == longName) {
            return null;
        }
        String parts[] = longName.split(DELIM_NAME);
        int length = parts.length;
        if (length < 3) {
            return null;
        }
        return parts[2];
    }
    


    public String getLongName() {
        return longName;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }
    
    public boolean isPruned() {
        if (null == node) {
            return false;
        }
        NodeVariableInfo nvi = node.getVariableInfo();
        if (null != nvi) {
            return nvi.isPruned();
        }
        return false;
    }
}

