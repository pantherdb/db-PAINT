/**
 *  Copyright 2023 University Of Southern California
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

public class Organism implements Serializable {
    private String id;
    private String longName;
    private String conversion;
    private String shortName;
    private String name;
    private String commonName;
    private int logicalRank;
    private boolean refGenome = false;
    private String taxonId;
    
    public Organism(String id, String longName, String conversion, String shortName, String name, String commonName, int logicalRank, boolean refGenome, String taxonId) {
        this.id = id;
        this.longName = longName;
        this.conversion = conversion;
        this.shortName = shortName;
        this.name = name;
        this.commonName = commonName;
        this.logicalRank = logicalRank;
        this.refGenome = refGenome;
        this.taxonId = taxonId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public String getLongName() {
        return longName;
    }

    public void setConversion(String conversion) {
        this.conversion = conversion;
    }

    public String getConversion() {
        return conversion;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setLogicalRank(int logicalRank) {
        this.logicalRank = logicalRank;
    }

    public int getLogicalRank() {
        return logicalRank;
    }

    public void setRefGenome(boolean refGenome) {
        this.refGenome = refGenome;
    }

    public boolean isRefGenome() {
        return refGenome;
    }

    public void setTaxonId(String taxonId) {
        this.taxonId = taxonId;
    }

    public String getTaxonId() {
        return taxonId;
    }
}
