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


public class KeyResidue implements Serializable{
    private String protein;
    private ResidueType residueType;

    public enum ResidueType {
        ACT_SITE,
        METAL,
        BINDING;

        public String toString() {
            return super.toString().toLowerCase();
        }
    }
    private String book;
    private int pos;
    private int alignPos;
    private Character aminoAcid;
    private String description;

    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public String getProtein() {
        return protein;
    }

    public void setProtein(String protein) {
        this.protein = protein;
    }

    public ResidueType getResidueType() {
        return residueType;
    }

    public void setResidueType(ResidueType residueType) {
        this.residueType = residueType;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getAlignPos() {
        return alignPos;
    }

    public void setAlignPos(int alignPos) {
        this.alignPos = alignPos;
    }

    public Character getAminoAcid() {
        return aminoAcid;
    }

    public void setAminoAcid(Character aminoAcid) {
        this.aminoAcid = aminoAcid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
}
