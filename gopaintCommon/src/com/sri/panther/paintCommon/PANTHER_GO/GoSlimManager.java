package com.sri.panther.paintCommon.PANTHER_GO;

import java.io.Serializable;

import java.util.Hashtable;
import java.util.Vector;

public class GoSlimManager implements Serializable {
    boolean isInitialized = false;
    
    Hashtable<Integer, PantherGoSlim> goTypeToRoot = new Hashtable<Integer, PantherGoSlim>();
    
    public GoSlimManager(Vector <PantherGoSlim> roots) {
        setRoots(roots);
    }
    

    
    
    public void setRoots (Vector <PantherGoSlim> roots) {
        if (null == roots || 0 == roots.size()) {
            isInitialized = false;
            return;
        }
        
        for (int i = 0; i < roots.size(); i++) {
            PantherGoSlim root = roots.get(i);
            int type = root.getGO_Type();
            goTypeToRoot.put(new Integer(type), root);
        }
        isInitialized = true;
        
    }
    
    public PantherGoSlim getRoot(int type) {
        if (null == goTypeToRoot) {
            return null;
        }
        return goTypeToRoot.get(new Integer(type));
    }
    
    public boolean isInitialized() {
        return isInitialized;
    }
    
    
}
