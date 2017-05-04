/**
 *  Copyright 2016 University Of Southern California
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

public class AnnotationDetail implements Serializable {
    private Node annotatedNode;
    
    private HashSet<Annotation> withAnnotSet;
    private HashSet<Node> withNodeSet;      // PANTHER node     // IKR, IRD
    private HashSet<DBReference> withOtherSet;          // PMID, etc
    
    // Qualifier value calculated as follows:
    // First go through items from inheritedQualifierLookup
    // Add items from addedQualifierLookup
    // Remove items from removedQualifierLookup
    private LinkedHashMap<Qualifier, HashSet<Annotation>> inheritedQualifierLookup = new LinkedHashMap<Qualifier, HashSet<Annotation>>();
    private LinkedHashMap<Qualifier, HashSet<Annotation>> addedQualifierLookup = new LinkedHashMap<Qualifier, HashSet<Annotation>>();
    private LinkedHashMap<Qualifier, HashSet<Annotation>> removedQualifierLookup = new LinkedHashMap<Qualifier, HashSet<Annotation>>();
    
    public Set<Qualifier> getQualifiers() {
        return getQualifierLookup().keySet();
    }
    
    public  LinkedHashMap<Qualifier, HashSet<Annotation>> getQualifierLookup() {
        LinkedHashMap<Qualifier, HashSet<Annotation>> rtnLookup = new LinkedHashMap<Qualifier, HashSet<Annotation>>();
        // Need to make a copy of inheritedAualifierLookup - Cant do clone
        Set<Qualifier> qSet = inheritedQualifierLookup.keySet();
        for (Iterator<Qualifier> qIter = qSet.iterator(); qIter.hasNext();) {
            Qualifier q = qIter.next();
            rtnLookup.put(q, (HashSet<Annotation>)inheritedQualifierLookup.get(q).clone());
        }
        
        
        
        qSet = addedQualifierLookup.keySet();
        for (Iterator<Qualifier> qIter = qSet.iterator(); qIter.hasNext();) {
            Qualifier q = qIter.next();
            HashSet<Annotation> annotSet = addedQualifierLookup.get(q);
            for (Annotation a: annotSet) {
                addQualifier(q, a, rtnLookup);
            }
        }

        qSet = removedQualifierLookup.keySet();
        for (Iterator<Qualifier> qIter = qSet.iterator(); qIter.hasNext();) {
            removeQualifier(qIter.next(), rtnLookup);
        }
        
        return rtnLookup;
    }
    
    
    private void addQualifier(Qualifier q, Annotation a, LinkedHashMap<Qualifier, HashSet<Annotation>> lookup) {
        if (null == q || null == q.getText() || null == a || null == lookup) {
            return;
        }
        String qText = q.getText();
        HashSet<Annotation> associatedAnnot = null;
        Set<Qualifier> qSet = lookup.keySet();
        for (Iterator<Qualifier> qIter = qSet.iterator(); qIter.hasNext();) {
            Qualifier curQ = qIter.next();
            if (qText.equals(curQ.getText())) {
                associatedAnnot = lookup.get(curQ);
                break;
            }
        }
        
        if (null == associatedAnnot) {
            associatedAnnot = new HashSet<Annotation>();
            lookup.put(q, associatedAnnot);
        }
        associatedAnnot.add(a);
    }
    
    private void removeQualifier(Qualifier q, LinkedHashMap<Qualifier, HashSet<Annotation>> lookup) {
        if (null == q || null == q.getText() || null == lookup) {
            return;
        }
        String qText = q.getText();
        Set<Qualifier> qSet = lookup.keySet();
        for (Iterator<Qualifier> qIter = qSet.iterator(); qIter.hasNext();) {
            Qualifier curQ = qIter.next();
            if (qText.equals(curQ.getText())) {
                qIter.remove();
                break;
            }
        }
    }

    public HashSet<Annotation> getWithAnnotSet() {
        return withAnnotSet;
    }

    public void setWithAnnotSet(HashSet<Annotation> withAnnotSet) {
        this.withAnnotSet = withAnnotSet;
    }
    
    public void addWith(Annotation a) {
        if (null == a) {
            return;
        }
        if (null == withAnnotSet) {
            withAnnotSet = new HashSet<Annotation>();
        }
        withAnnotSet.add(a);
    }

    public HashSet<Node> getWithNodeSet() {
        return withNodeSet;
    }

    public void setWithNodeSet(HashSet<Node> withNodeSet) {
        this.withNodeSet = withNodeSet;
    }
    
    public void addNode(Node n) {
        if (null == n) {
            return;
        }
        if (null == withNodeSet) {
            withNodeSet = new HashSet<Node>();
        }
        withNodeSet.add(n);
    }

    public HashSet<DBReference> getWithOtherSet() {
        return withOtherSet;
    }

    public void setWithOtherSet(HashSet<DBReference> withOtherSet) {
        this.withOtherSet = withOtherSet;
    }
    
    public void addOther(DBReference dbRef) {
        if (null == dbRef) {
            return;
        }
        if (null == withOtherSet) {
            withOtherSet = new HashSet<DBReference>();
        }
        withOtherSet.add(dbRef);
    }

    public Node getAnnotatedNode() {
        return annotatedNode;
    }

    public void setAnnotatedNode(Node annotatedNode) {
        this.annotatedNode = annotatedNode;
    }

    public LinkedHashMap<Qualifier, HashSet<Annotation>> getInheritedQualifierLookup() {
        return inheritedQualifierLookup;
    }

    public void setInheritedQualifierLookup(LinkedHashMap<Qualifier, HashSet<Annotation>> inheritedQualifierLookup) {
        this.inheritedQualifierLookup = inheritedQualifierLookup;
    }
    
    public void addToInheritedQualifierLookup(Qualifier q, Annotation a) {
        addQualifier(q, a, inheritedQualifierLookup);
    }

    public LinkedHashMap<Qualifier, HashSet<Annotation>> getAddedQualifierLookup() {
        return addedQualifierLookup;
    }

    public void setAddedQualifierLookup(LinkedHashMap<Qualifier, HashSet<Annotation>> addedQualifierLookup) {
        this.addedQualifierLookup = addedQualifierLookup;
    }
    
    public void addToAddedQualifierLookup(Qualifier q, Annotation a) {
        addQualifier(q, a, addedQualifierLookup);
    }

    public LinkedHashMap<Qualifier, HashSet<Annotation>> getRemovedQualifierLookup() {
        return removedQualifierLookup;
    }

    public void setRemovedQualifierLookup(LinkedHashMap<Qualifier, HashSet<Annotation>> removedQualifierLookup) {
        this.removedQualifierLookup = removedQualifierLookup;
    }
    
    public void addToRemovedQualifierLookup(Qualifier q, Annotation a) {
        addQualifier(q, a, removedQualifierLookup);
    }
    
    public boolean annotationHasNOTqualifier() {
        LinkedHashMap<Qualifier, HashSet<Annotation>> lookup = getQualifierLookup();
        Set<Qualifier> qSet = lookup.keySet();
        for (Qualifier q: qSet) {
            if (q.isNot()) {
                return true;
            }
        }
        return false;
    }
    
    // Use Annotation class to remove with annotation.  This will handle the qualifiers
    protected Annotation removeWithAnnotation(Annotation with) {
        Annotation removedAnnot = null;
        if (null != withAnnotSet) {
            if (true == withAnnotSet.remove(with)) {
                removedAnnot = with;
            }
            if (withAnnotSet.isEmpty()) {
                withAnnotSet = null;
            }
        }
        
        if (null != removeWith(inheritedQualifierLookup, with)) {
            removedAnnot = with;
        }
        if (null != removeWith(addedQualifierLookup, with)) {
            removedAnnot = with;
        }        
        if (null != removeWith(removedQualifierLookup, with)) {
            removedAnnot = with;
        }
        
        return removedAnnot;
    }
    
    private Annotation removeWith(LinkedHashMap<Qualifier, HashSet<Annotation>> lookup, Annotation with) {
        Annotation returnAnnot = null;
        Set<Qualifier> qSet = lookup.keySet();
        HashSet<Qualifier> removeSet = new HashSet<Qualifier>();
        for (Qualifier q: qSet) {
            HashSet<Annotation> associatedAnnotSet = lookup.get(q);
            if (true == associatedAnnotSet.contains(with)) {
                associatedAnnotSet.remove(with);
                if (true == associatedAnnotSet.isEmpty()) {
                    removeSet.add(q);
                }
                returnAnnot = with;
            }
        }
        for (Qualifier q: removeSet) {
            lookup.remove(q);
        }        
        
        return returnAnnot;
    } 
    
    
    
}
