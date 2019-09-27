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
package edu.usc.ksom.pm.panther.paintCommon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class GOTerm implements Serializable {
    private String acc;
    private String name;
    private String id;
    private String description;
    private ArrayList <GOTerm> parents;
    private ArrayList <GOTerm> children;
    private String aspect;

    public String getAcc() {
        return acc;
    }

    public void setAcc(String acc) {
        this.acc = acc;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<GOTerm> getParents() {
        ArrayList<GOTerm> parentsWithSameAspect = new ArrayList<GOTerm>();
        if (null == parents) {
            return parentsWithSameAspect;
        }
        for (GOTerm parent: parents) {
            if (aspectSame(parent)) {
                parentsWithSameAspect.add(parent);
            }
        }
        return parentsWithSameAspect;
    }

    public List<GOTerm> getParentsAcrossAllAspects() {
        return parents;
    }

//    public void setParents(ArrayList<GOTerm> parents) {
//        this.parents = parents;
//    }
    
    public void addParent(GOTerm parent) {
        if (null == this.parents) {
            this.parents = new ArrayList<GOTerm>();
        }
        this.parents.add(parent);
    }
    
    public List<GOTerm> getChildren() {
        ArrayList<GOTerm> childrenSameAsp = new ArrayList<GOTerm>();
        if (null == children) {
            return childrenSameAsp;
        }
        for (GOTerm child: children) {
            if (aspectSame(child)) {
                childrenSameAsp.add(child);
            }
        }
        return childrenSameAsp;
    }

    public List<GOTerm> getChildrenAcrossAllAspects() {
        return children;
    }

//    public void setChildren(ArrayList<GOTerm> children) {
//        this.children = children;
//    }
    
    public void addChild(GOTerm child) {
        if (null == this.children) {
            this.children = new ArrayList<GOTerm>();
        }
        this.children.add(child);        
    }

    public String getAspect() {
        return aspect;
    }

    public void setAspect(String aspect) {
        this.aspect = aspect;
    }
    
    // Just compare acc for now
    public boolean equals(GOTerm term) {
        if (this.acc != null && term.acc != null && this.acc != null & term.acc != null & this.acc.equals(term.acc)) {
            return true;
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public boolean aspectSame(GOTerm compTerm) {
        if (null == compTerm) {
            return false;
        }
        if (null == aspect && null == compTerm.aspect) {
            return true;
        }
        if (null != aspect && null != compTerm.aspect && aspect.equals(compTerm.aspect)) {
            return true;
        }
        if (null != aspect && null != compTerm.aspect && false == aspect.equals(compTerm.aspect)) {
            return false;
        }
        return false;
    }
    
}
