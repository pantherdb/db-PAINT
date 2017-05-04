package com.sri.panther.paintServer.datamodel;

public class Organism {
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
