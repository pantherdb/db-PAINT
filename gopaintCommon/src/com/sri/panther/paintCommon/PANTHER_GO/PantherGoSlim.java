package com.sri.panther.paintCommon.PANTHER_GO;

import com.sri.panther.paintCommon.Constant;
import com.sri.panther.paintCommon.util.FileUtils;

import com.sri.panther.paintCommon.util.Utils;

import java.io.Serializable;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

public class PantherGoSlim implements Serializable, Comparable {

    public static final int GO_BIOLOGICAL_PROCESS = 0;
    public static final int GO_CELLULAR_COMPONENT = 1;
    public static final int GO_MOLECULAR_FUNCTION = 2;
    
    public static final String GO_TYPE_MOLECULAR_FUNCTION = "molecular_function";
    public static final String GO_TYPE_CELLULAR_COMPONENT = "cellular_component";
    public static final String GO_TYPE_BIOLOGICAL_PROCESS = "biological_process";
    
    public static final String TST_MOLECULAR_FUNCTION = "mol";
    public static final String TST_BIOLOGICAL_PROCESS = "bio";
    public static final String TST_CELLULAR_COMPONENT = "cel";
    
    public static final String GO_DATA_SEPARATOR = Constant.STR_TAB;
    protected static final int GO_DATA_INDEX_ID = 0;
    protected static final int GO_DATA_INDEX_DESCRIPTION = 1;
    protected static final int GO_DATA_INDEX_PARENT_ID = 2;
    protected static final int GO_DATA_INDEX_RELATION_TYPE = 3;
    protected static final int GO_DATA_INDEX_TYPE = 4;
    
    
    protected static Logger logger = Logger.getLogger(PantherGoSlim.class.getName());
    
    public static final String MSG_ENCOUNTERED_UNHANDLED_GO_TYPE = "Encountered unhandled GO type ";
    
    

    
    protected String id;
    protected String description;
    protected int GO_Type;
    
    protected Vector<GoSlimRelation> parents;
    protected Vector<GoSlimRelation> children;
    
    public PantherGoSlim() {
        
    }
    
    /**
     * Compare based on type followed by description
     * @param o
     * @return
     */
    public int compareTo (Object o) {
        PantherGoSlim node = (PantherGoSlim)o;
        if (this.GO_Type == node.GO_Type) {
            if (this.description != null && node.description != null) {
                return this.description.compareTo(node.description); 
            }
            else if (this.description == null && node.description == null) {
                return 0;
            }
            else if (this.description == null && node.description != null) {
                return -1;
            }
            else {
                return 1;
            }
        }
        else if (this.GO_Type > node.GO_Type) {
            return 1;
        }
        else {
            return -1;
        }
        
    }
    
    
    
 
    public static Vector<PantherGoSlim> parseData(String goData[]) {
        Hashtable<String, PantherGoSlim> GOInfoTbl = new Hashtable<String, PantherGoSlim>();
        
        for (int i = 0; i < goData.length; i++) {
            String infoStr = goData[i];
            String parts[] = Utils.tokenize(infoStr, GO_DATA_SEPARATOR);
            String id = parts[GO_DATA_INDEX_ID];
            PantherGoSlim node = GOInfoTbl.get(id);
            if (null == node) {
                node = new PantherGoSlim();
                GOInfoTbl.put(id, node);
                node.id = id;
            }
            node.description = parts[GO_DATA_INDEX_DESCRIPTION];
            node.GO_Type = getType(parts[GO_DATA_INDEX_TYPE]);
            
                
            String parentId = parts[GO_DATA_INDEX_PARENT_ID];
            PantherGoSlim parent = GOInfoTbl.get(parentId);
            if (null == parent) {
                parent = new PantherGoSlim();
                parent.id = parentId;
                GOInfoTbl.put(parentId, parent);
            }
            parent.GO_Type = node.GO_Type;
            
            
            
            GoSlimRelation relation = new GoSlimRelation();
            relation.setNode(parent);
            relation.setRelationshipId(GoSlimRelation.convertRelationShipType(parts[GO_DATA_INDEX_RELATION_TYPE]));
            node.addParent(relation);
            
            GoSlimRelation childRelation = new GoSlimRelation();
            childRelation.setNode(node);
            childRelation.setRelationshipId(relation.getRelationshipId());
            parent.addChild(childRelation);
        }
        
        Vector<PantherGoSlim> allNodes = new Vector<PantherGoSlim>(GOInfoTbl.values());
        Vector<PantherGoSlim> roots = new Vector<PantherGoSlim>();
        for (int i = 0; i < allNodes.size(); i++) {
            PantherGoSlim aNode = allNodes.get(i);
            if (null == aNode.parents) {
                roots.add(aNode);
            }
        }
        return roots;
//        StringBuffer sb = new StringBuffer();
//        for (int i = 0; i < allNodes.size(); i++) {
//            PantherGoSlim aNode = allNodes.get(i);
//            Vector<GoSlimRelation> parents = aNode.parents;
//            if (null == parents) {
//                sb.append("ROOT\n");
//                outputTree(aNode, sb, Constant.STR_EMPTY);
//            }
//        }
//        System.out.println(sb.toString());
    }
    
    public static void outputTree(PantherGoSlim node, StringBuffer sb, String indent) {
        if (null == node) {
            return;
        }
        
        if (null == node.children) {
            return;
        }
        sb.append(indent + convertType(node.GO_Type) + " " + node.id + " " + node.description + " has children\n");
        indent += Constant.STR_DASH;
        int num = node.children.size();
        GoSlimRelation childList [] = new GoSlimRelation[num];
        node.children.copyInto(childList);
        Arrays.sort(childList);
        for (int i = 0; i < num; i++) {
            GoSlimRelation relation = childList[i];
            PantherGoSlim relationNode = relation.getNode();
            sb.append(indent + relationNode.id + " " + relationNode.description + " " + relation.getRelationshipTypeStr(relation.getRelationshipId()) + " " + node.description + " child of " + node.id +  " \n");
            outputTree(relationNode, sb, indent + Constant.STR_DASH);
            
        }
        
    }
    
    public static int getType(String type) {
        if (true == type.equals(GO_TYPE_MOLECULAR_FUNCTION)) {
            return GO_MOLECULAR_FUNCTION;
        }
        else if (true == type.equals(GO_TYPE_CELLULAR_COMPONENT)) {
            return GO_CELLULAR_COMPONENT;
        }
        else if (true == type.equals(GO_TYPE_BIOLOGICAL_PROCESS)) {
            return GO_BIOLOGICAL_PROCESS;
        }
        else {
            logger.error(MSG_ENCOUNTERED_UNHANDLED_GO_TYPE + type);
            return -1;
        }
    }
    
    
    public static String convertType(int type) {
        if (type == GO_MOLECULAR_FUNCTION) {
            return GO_TYPE_MOLECULAR_FUNCTION;
        }
        else if (type == GO_CELLULAR_COMPONENT) {
            return GO_TYPE_CELLULAR_COMPONENT;
        }
        else if (type == GO_BIOLOGICAL_PROCESS) {
            return GO_TYPE_BIOLOGICAL_PROCESS;
        }
        else {
            return null;
        }
    }
    
    
    public void addParent(GoSlimRelation parent) {
        if (null == parent){
            return;
        }
        if (null == parents) {
            parents = new Vector<GoSlimRelation>(1);
        }
        
        if (parents.contains(parent)) {
            return;
        }
        parents.add(parent);
    }
    
    public void addChild(GoSlimRelation child) {
        if (null == child){
            return;
        }
        if (null == children) {
            children = new Vector<GoSlimRelation>(1);
        }
        if (children.contains(child)) {
            return;
        }
        children.add(child);        
    }
 
    public static void main(String[] args) {
    
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Vector<GoSlimRelation> getChildren() {
        if (null == children) {
            return null;
        }
        return (Vector)children.clone();
    }
    
    public static int goTypeIndex(String s) {
        if (null == s) {
            return -1;
        }
        s = s.toLowerCase();
        if (-1 != s.indexOf(TST_MOLECULAR_FUNCTION)) {
            return GO_MOLECULAR_FUNCTION;
        }
        if (-1 != s.indexOf(TST_BIOLOGICAL_PROCESS)) {
            return GO_BIOLOGICAL_PROCESS;
        }
        if (-1 != s.indexOf(TST_CELLULAR_COMPONENT)) {
            return GO_CELLULAR_COMPONENT;
        }
        return -1;
    }

    public int getGO_Type() {
        return GO_Type;
    }
}
