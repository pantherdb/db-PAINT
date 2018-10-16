/**
 *  Copyright 2017 University Of Southern California
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
package edu.usc.ksom.pm.panther.paint.matrix;

import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import java.util.ArrayList;
import org.paint.datamodel.Association;

    public class TermToAssociation {
        private GOTerm term;        // Maybe an ancestor term.  Not necessarily same as term specified in Association.Annotation.
                                    // Use annotatedToTermLookup to determine if annotated term and 'term' are the same
        private ArrayList <Association> asnList = new ArrayList<Association>();
//        private HashMap<Association, Boolean> annotatedToTermLookup = new HashMap<Association, Boolean>();


        
        public TermToAssociation(GOTerm term) {
            this.term = term;
//            if (term.getAcc().equals("GO:0004222")) {
//                System.out.println("Here");
//            }
        }
        
        public TermToAssociation(GOTerm term, ArrayList <Association> asnList) {
//                        if (term.getAcc().equals("GO:0004222")) {
//                System.out.println("Here");
//            }
            this.term = term;
            this.asnList = asnList;
        }
        
        
        public void addAsn(Association a) {
            asnList.add(a);
           
        }

        public GOTerm getTerm() {
            return term;
        }



        public ArrayList<Association> getAsnList() {
            return asnList;
        }
        
    public void setAsnList(ArrayList<Association> asnList) {
        this.asnList = asnList;    
    }        

        public ArrayList<Node> getNodesForAssociation() {
            if (null == asnList) {
                return null;
            }
            ArrayList<Node> rtnList = new ArrayList<Node>(asnList.size());
            for (Association a: asnList) {
                rtnList.add(a.getNode());
            }
            return rtnList;
        }
        
        
        public ArrayList<Node> getExperimentalNodesForAssociation() {
            if (null == asnList) {
                return null;
            }
            ArrayList<Node> rtnList = new ArrayList<Node>(asnList.size());
            for (Association a: asnList) {
                if (true == a.getAnnotation().isExperimental()) {
                    rtnList.add(a.getNode());
                }
            }
            return rtnList;            
        }
        
        public boolean isAssociationDirect(Association a) {
            return !a.getAnnotation().isAnnotIsToChildTerm();
        }
        
    }
