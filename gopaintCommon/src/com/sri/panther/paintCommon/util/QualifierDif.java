/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sri.panther.paintCommon.util;


import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author muruganu
 */
public class QualifierDif {
    public static final int QUALIFIERS_SAME = 0;
    public static final int QUALIFIERS_DIFFERENT_WITHOUT_NOT = 1;
    public static final int QUALIFIERS_DIFFERENT_ALL_NOT = 2;
    public static final int QUALIFIERS_DIFFERENT_WITH_NOT_AND_OTHERS = 3;
    
    private int difference;
    private HashSet<Qualifier> differentSet;
    
    public QualifierDif(HashSet<Qualifier> set1, HashSet<Qualifier> set2) {
        // Empty or null
        if ((null == set1 && null == set2) || (null == set1 && 0 == set2.size()) || (0 == set1.size() && null == set2) ||(0 == set1.size() && 0 == set2.size())) {
            difference = QUALIFIERS_SAME;
            return;
        }
        if (null == set1 && null != set2 ) {
            differentSet = new HashSet<Qualifier>();
            differentSet.addAll(set2);
            difference = calculateDifference();
            return;
        }
        if (null == set2 && null != set1) {
            differentSet = new HashSet<Qualifier>();
            differentSet.addAll(set1);            
            difference = calculateDifference();
            return;
        }
        if (true == allQualifiersSame(set1, set2)) {
            difference = QUALIFIERS_SAME;
            return;
        }
        differentSet = new HashSet<Qualifier>();
        
        for (Qualifier q: set1) {
            addIfNotPresent(differentSet, q);
        }
        for (Qualifier q: set2) {
            addIfNotPresent(differentSet, q);
        }        
        difference = calculateDifference();

    }

    public QualifierDif() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public int getDifference() {
        return difference;
    }

// Can have both positive and negative
//    public static boolean qualifierSetOkayForAnnotation (HashSet<Qualifier> qualifierSet) {
//        if (null == qualifierSet || 0 == qualifierSet.size()) {
//            return true;
//        }
//        if (true == allPositive(qualifierSet)) {
//            return true;
//        }
//        if (true == allNot(qualifierSet)) {
//            return true;
//        }
//        return false;
//    }
    
    public static HashSet<Qualifier> getUniqueQualifiers(HashSet<Qualifier> qualifierSet) {
        if (null == qualifierSet || 0 == qualifierSet.size()) {
            return null;
        }
        HashSet<Qualifier> uniqueSet = new HashSet<Qualifier>();
        HashSet<String> handled = new HashSet<String>();
        boolean nullHandled = false;
        for (Qualifier q: qualifierSet) {
            if (null == q.getText()) {
                if (true == nullHandled) {
                    continue;
                }
                nullHandled = true;
                uniqueSet.add(q);
                continue;
            }
            String curText = q.getText();
            if (false == handled.contains(curText)) {
                uniqueSet.add(q);
                handled.add(curText);
            }
        }
        if (uniqueSet.isEmpty()) {
            return null;
        }
        return uniqueSet;
    }
    /**
     * Use when one set1 size is different from set2 or if one of the sets is empty and the other is not
     * @return 
     */
    private int calculateDifference() {
        if (null == differentSet || 0 == differentSet.size()) {
            return QUALIFIERS_SAME;
        }
        if (allPositive(differentSet)) {
            return QUALIFIERS_DIFFERENT_WITHOUT_NOT;
           
        }
            if (allNot(differentSet)) {
                return QUALIFIERS_DIFFERENT_ALL_NOT;
            }
            return QUALIFIERS_DIFFERENT_WITH_NOT_AND_OTHERS;
    }
    
    
    public static boolean contains(HashSet<Qualifier> set1, HashSet<Qualifier> set2) {
        if (null == set2) {
            return true;
        }
        if (null == set1 && set2.size() > 0) {
            return false;
        }
        if (null == set1 && set2.size() == 0) {
            return true;
        }        
        if (null == set1 && null == set2) {
            return true;
        }

        for (Qualifier q1: set1) {
            String text1 = q1.getText();
            boolean found = false;
            for (Qualifier q2: set2) {
                String text2 = q2.getText();
                if (null != text1 && text1.equals(text2)) {
                    found = true;
                    break;
                }
                if (null == text1 && null == text2) {
                    found = true;
                    break;
                }
            }
            if (false == found) {
                return false;
            }
        }
        return true;
    }
    
    public static void addIfNotPresent(HashSet<Qualifier> addSet, HashSet<Qualifier> toBeAddedSet) {
        if (null == addSet || null == toBeAddedSet) {
            return;
        }
        for (Qualifier q: toBeAddedSet) {
            addIfNotPresent(addSet, q);
        }
    }
    
    public static Qualifier find(HashSet<Qualifier> set, Qualifier q) {
        String qText = q.getText();
        for (Qualifier listQualifier: set) {
            String curText = listQualifier.getText();
            if (null == curText && null == q.getText()) {
                return listQualifier;
            }
            if ((null == curText && null != qText) || (null != curText && null == qText)) {
                continue;
            }
            if (curText.equals(qText)) {
                return listQualifier;
            }
        }

        return null;
    }
    
    
    public static void addIfNotPresent(HashSet<Qualifier> set, Qualifier q) {

        String qText = q.getText();
        for (Qualifier listQualifier: set) {
            String curText = listQualifier.getText();
            if (null == curText && null == q.getText()) {
                return;
            }
            if ((null == curText && null != qText) || (null != curText && null == qText)) {
                continue;
            }
            if (curText.equals(qText)) {
                return;
            }
        }

        set.add(q);

    }
    
    public static boolean exists(HashSet<Qualifier> set, Qualifier q) {
        if (null == set && null != q) {
            return false;
        }
        if (null == q) {
            return true;
        }
        String qText = q.getText();
        for (Qualifier listQualifier: set) {
            String curText = listQualifier.getText();
            if (null == curText && null == q.getText()) {
                return true;
            }
            if ((null == curText && null != qText) || (null != curText && null == qText)) {
                continue;
            }
            if (curText.equals(qText)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean allPositive(HashSet<Qualifier> qualifierSet) {
        if (null == qualifierSet) {
            return true;
        }
        for (Qualifier qualifier: qualifierSet) {
            if (true == qualifier.isNot()) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean allNot(HashSet<Qualifier> qualifierSet) {
        for (Qualifier qualifier: qualifierSet) {
            if (false == qualifier.isNot()) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean containsPositive(HashSet<Qualifier> qualifierSet) {
        if (null == qualifierSet) {
            return false;
        }
        for (Qualifier qualifier: qualifierSet) {
            if (true == qualifier.isNot()) {
                continue;
            }
            return true;
        }
        return false;
    }
    
    public static boolean containsNegative(Set<Qualifier> qualifierSet) {
        if (null == qualifierSet) {
            return false;
        }
        for (Qualifier qualifier: qualifierSet) {
            if (true == qualifier.isNot()) {
                return true;
            }
        }
        return false;        
    }
    
    public static Qualifier getNOT(HashSet<Qualifier> qualifierSet) {
        if (null == qualifierSet) {
            return null;
        }
        for (Qualifier qualifier: qualifierSet) {
            if (true == qualifier.isNot()) {
                return qualifier;
            }
        }
        return null;
    } 
    
    public static boolean allQualifiersSame(HashSet<Qualifier> set1, HashSet<Qualifier> set2) {
        // Empty or null
        if ((null == set1 && null == set2) || ((null != set1 && 0 == set1.size()) && (null != set2 && 0 == set2.size()))) {
            return true;
        }
        
        // Only one is null, then size of other should be zero
        if ((null == set1 && (null != set2 && 0 != set2.size())) || (null != set1 && (set1.size() != 0 && null == set2))) {
            return false;
        }
        
        // Sizes do not match
        if (set1.size() != set2.size()) {
            return false;
        }
        
        // Handle case where there are duplicates
        HashSet<Qualifier> copy2 = (HashSet<Qualifier>)set2.clone();
        for (Qualifier q1: set1) {
            boolean found = false;
            for (Iterator<Qualifier> q2Iter = copy2.iterator(); q2Iter.hasNext();) {
                if (q1.getText().equals(q2Iter.next().getText())) {
                    found = true;
                    q2Iter.remove();
                    break;
                }
            }
            if (false == found) {
                return false;
            }
        }

        return true;
    }
    
    // Check if one set has a NOT and the other does not have a NOT
    public static boolean areOpposite(HashSet<Qualifier> set1, HashSet<Qualifier> set2) {
        if (null == set1 && null == set2) {
            return false;
        }
        if (null == set1 && null != set2) {
            if (true == containsNegative(set2)) {
                return true;
            }
            return false;
        }
        if (null != set1 && null == set2) {
            if (true == containsNegative(set1)) {
                return true;
            }
            return false;
        }
        
        // both sets have something.
        if ((true == allPositive(set1) && true == containsNegative(set2)) || (true == allPositive(set2) && true == containsNegative(set1))) {
            return true;
        }
        return false;
    }
    
    public static boolean containsOnlyOneNOT(HashSet<Qualifier> qualifierSet) {
        if (null == qualifierSet) {
            return false;
        }
        if (1 != qualifierSet.size()) {
            return false;
        }
        for (Qualifier q: qualifierSet) {
            if (true == q.isNot()) {
                return true;
            }
            return false;
        }
        return false;
    }
    
}
//        // Handle case where the sizes of the sets are the same, but contents is not same.  Example NOT and colocalizes in both sets
//        if ((set1.size() == set2.size()) && (set1.size() == differentSet.size())) {
//            Qualifier compQualifier = differentSet.iterator().next();
//            String compText = compQualifier.getText();
//            for (Qualifier q: differentSet) {
//                String curText = q.getText();
//                if (null == curText && null == compText) {
//                    continue;
//                }
//                if (null == curText && null != compText) {
//                    return;
//                }
//                if (null != curText && null == compText) {
//                    return;
//                }
//                if (false == curText.equals(compText)) {
//                    return;
//                }
//            }
//            difference = QUALIFIERS_SAME;
//        }