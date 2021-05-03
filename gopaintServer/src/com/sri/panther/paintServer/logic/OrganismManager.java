/**
 * Copyright 2020 University Of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.sri.panther.paintServer.logic;

import com.sri.panther.paintServer.database.DataServer;

import com.sri.panther.paintServer.database.DataServerManager;

import com.sri.panther.paintServer.datamodel.Organism;

import edu.usc.ksom.pm.panther.paintServer.webservices.WSConstants;

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
