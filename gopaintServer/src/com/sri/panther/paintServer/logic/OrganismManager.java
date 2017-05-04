package com.sri.panther.paintServer.logic;

import com.sri.panther.paintServer.database.DataServer;

import com.sri.panther.paintServer.database.DataServerManager;

import com.sri.panther.paintServer.datamodel.Organism;

import com.usc.panther.paintServer.webservices.WSConstants;

import java.util.ArrayList;

// Singleton class to mnage list of supported organisms
public class OrganismManager {
    
    private static ArrayList<Organism> organismList = null;
    private static OrganismManager instance = null;
    private OrganismManager() {
    }
    
    public static OrganismManager getInstance() {
        if (null != instance) {
            return instance;
        }
        init();
        return instance;
    }
    
    private static void init() {
        DataServer ds = DataServerManager.getDataServer(WSConstants.PROPERTY_DB_STANDARD);
        ArrayList orgs = ds.getOrganismList(WSConstants.PROPERTY_CLS_VERSION);
        organismList = orgs;
        instance = new OrganismManager();
    }
    
    public ArrayList<Organism> getOrgList() {
        return organismList;
    }
    
    public String getTaxonId (String longName) {
        if (null == longName || null == organismList) {
            return null;
        }
        for (Organism org : organismList) {
            if (longName.equalsIgnoreCase(org.getLongName())) {
                return org.getTaxonId();
            }
        }
        return null;
    }
    
    public Organism getOrganismForShortName(String shortName) {
        if (null == shortName || null == organismList) {
            return null;
        }
        for (Organism org : organismList) {
            if (shortName.equalsIgnoreCase(org.getShortName())) {
                return org;
            }
        }
        return null;
    }
}
