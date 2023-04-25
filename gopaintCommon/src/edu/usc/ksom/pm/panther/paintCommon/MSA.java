/**
 *  Copyright 2022 University Of Southern California
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

/**
 *
 * @author muruganu
 */
public class MSA implements Serializable{
    String[] msaContents;
    String[] weightsContents;
    ArrayList<KeyResidue> keyResidueList;

    public String[] getMsaContents() {
        return msaContents;
    }

    public void setMsaContents(String[] msaContents) {
        this.msaContents = msaContents;
    }

    public String[] getWeightsContents() {
        return weightsContents;
    }

    public void setWeightsContents(String[] weightsContents) {
        this.weightsContents = weightsContents;
    }

    public ArrayList<KeyResidue> getKeyResidueList() {
        return keyResidueList;
    }

    public void setKeyResidueList(ArrayList<KeyResidue> keyResidueList) {
        this.keyResidueList = keyResidueList;
    }
    
}

