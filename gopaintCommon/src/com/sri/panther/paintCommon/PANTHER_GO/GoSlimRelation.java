package com.sri.panther.paintCommon.PANTHER_GO;

import com.sri.panther.paintCommon.util.Utils;

import java.io.Serializable;

import java.util.Hashtable;

import org.apache.log4j.Logger;

public class GoSlimRelation implements Serializable, Comparable {
    
    public static final int RELATIONSHIP_TYPE_IS_A = 0;
    public static final int RELATIONSHIP_TYPE_PART_OF = 1;
    public static final int RELATIONSHIP_TYPE_REGULATES = 2;
    public static final int RELATIONSHIP_TYPE_NEGATIVELY_REGULATES = 3;
    
    public static final String GO_RELATIONSHIP_TYPE_IS_A = "is_a";
    public static final String GO_RELATIONSHIP_TYPE_PART_OF = "part_of";
    public static final String GO_RELATIONSHIP_TYPE_REGULATES = "regulates";
    public static final String GO_RELATIONSHIP_TYPE_NEGATIVELY_REGULATES = "negatively_regulates";
    
    // Both of these have corresponding data
    protected static Hashtable <String, Integer> GORelationshipTypeTbl;
    
    public static final String MSG_ENCOUNTERED_UNSUPPORTED_RELATIONSHIP_TYPE = "Encountered unsupported relationship type ";
    
    protected static Logger logger = Logger.getLogger(GoSlimRelation.class.getName());
    
    protected int relationshipId;
    protected PantherGoSlim node;
    
    static {
        GORelationshipTypeTbl = new Hashtable<String, Integer>();
        GORelationshipTypeTbl.put(GO_RELATIONSHIP_TYPE_IS_A, new Integer(RELATIONSHIP_TYPE_IS_A));
        GORelationshipTypeTbl.put(GO_RELATIONSHIP_TYPE_PART_OF, new Integer(RELATIONSHIP_TYPE_PART_OF));
        GORelationshipTypeTbl.put(GO_RELATIONSHIP_TYPE_REGULATES, new Integer(RELATIONSHIP_TYPE_REGULATES));
        GORelationshipTypeTbl.put(GO_RELATIONSHIP_TYPE_NEGATIVELY_REGULATES, new Integer(RELATIONSHIP_TYPE_NEGATIVELY_REGULATES));
    }
    
    public GoSlimRelation() {
    
    }
    
    public int compareTo (Object o) {
        GoSlimRelation relation = (GoSlimRelation)o;
        if (null != node && null != relation.node) {
            int value = node.compareTo(relation.node);
            if (0 == value) {
                return Integer.valueOf(relationshipId).compareTo(Integer.valueOf(relation.relationshipId));
            }
            return value;
        }
        else if (node == null && relation.node != null) {
            return -1;
        }
        else {
            return 1;
        }
    }
    
    
    public static int convertRelationShipType(String type) {
        Integer i = GORelationshipTypeTbl.get(type);
        if (null != i) {
            return i.intValue();
        }
        return -1;
    }
    
    
    public String getRelationshipTypeStr(int type) {
        if (type == RELATIONSHIP_TYPE_IS_A) {
            return GO_RELATIONSHIP_TYPE_IS_A;
        }
        else if (type == RELATIONSHIP_TYPE_PART_OF) {
            return GO_RELATIONSHIP_TYPE_PART_OF;
        }
        else {
            return null;
        }
    }

    public void setRelationshipId(int id) {
        this.relationshipId = id;
    }

    public int getRelationshipId() {
        return relationshipId;
    }

    public void setNode(PantherGoSlim node) {
        this.node = node;
    }

    public PantherGoSlim getNode() {
        return node;
    }
}
