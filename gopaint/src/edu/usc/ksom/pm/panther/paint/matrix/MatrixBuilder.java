/**
 *  Copyright 2020 University Of Southern California
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

import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import java.util.ArrayList;
import java.util.List;
import org.paint.datamodel.Family;
import org.paint.datamodel.GeneNode;
import org.paint.gui.familytree.TreeModel;
import org.paint.main.PaintManager;


public class MatrixBuilder {
    public static MatrixInfo getMatrixInfo(TreeModel tm) {
        if (null == tm) {
            return null;
        }
        
        List<GeneNode> nodes = tm.getCurrentNodes();
        PaintManager pm = PaintManager.inst();
        GOTermHelper gth = pm.goTermHelper();
        ArrayList<TermAncestor> termAncestorList = pm.getTermAncestorList();
        if (null == termAncestorList) {
            termAncestorList = new ArrayList<TermAncestor>();
        }
        String familyId = null;
        Family fam = pm.getFamily();
        if (null != fam) {
            familyId = fam.getFamilyID();
        }
        MatrixInfo mi = new MatrixInfo(gth, nodes, termAncestorList, familyId);
//        System.out.println("Calculated matrix");
        outputInfo(mi);
        // Reorder according to previous ordering if possible
        MatrixInfo previousMi = pm.getMatrixInnfo();
        if (null == previousMi) {
            pm.setMatrixInfo(mi);
            return mi;
        }
        String previousFamId = previousMi.getFamilyId();
        if (null != previousFamId && previousFamId.equals(familyId)) {
//            System.out.println("Previous matrix");
//            outputInfo(mi);            
            MatrixInfo.reorderMatrix(previousMi, mi);
//            System.out.println("after reorder matrix");
//            outputInfo(mi);            
        }
        pm.setMatrixInfo(mi);
        return mi;
    }
    

    
    public static void outputInfo(MatrixInfo mi) {
//        mi.printInfo();
    }
    

}
