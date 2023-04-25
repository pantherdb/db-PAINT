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
package com.sri.panther.paintServer.logic;

import com.sri.panther.paintServer.util.ConfigFile;
import edu.usc.ksom.pm.panther.paintCommon.KeyResidue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class KeyResiduesManager {
    public static final String URL_ACTIVE_SITE_INFO = ConfigFile.getProperty("msa_active_site");
    public static final String DELIM_RESIDUE_INFO = "\t";
//    //public static final Pattern BOOK_START_PATTERN = Pattern.compile("PTHR10000");
    //public static final Pattern BOOK_START_PATTERN = Pattern.compile("PTHR\\d\\d\\d\\d\\d");
    public static String patternStr = "^PTHR[0-9]{5}";
    public static final Pattern BOOK_START_PATTERN = Pattern.compile(patternStr);
    //public static final Pattern BOOK_START_PATTERN = Pattern.compile("PTHR[\\d]{5}");
    
    public static final int NUM_FIELDS = 7;
    public static final int INDEX_BOOK = 0;
    public static final int INDEX_PROTEIN = 1;
    public static final int INDEX_RESIDUE = 2;
    public static final int INDEX_POSITION = 3;
    public static final int INDEX_DESC = 4;
    public static final int INDEX_AMINO_ACID = 5;
    public static final int INDEX_ALIGN_POS = 6;    
    
    
    public static final ArrayList<KeyResidue> getSitesForFamily(String familyId) {
        if (null == familyId) {
            return null;
        }
        ArrayList<KeyResidue> infoList = new ArrayList<KeyResidue>();
        BufferedReader in = null;
        try {
            URL website = new URL(URL_ACTIVE_SITE_INFO);
            URLConnection connection = website.openConnection();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String curLine;
            while ((curLine = in.readLine()) != null) {
//                if (curLine.contains(familyId)) {
////                    System.out.println(curLine + " contains " + familyId);
//                }
//                else {
//                    //System.out.println(curLine + " does not contain " + familyId);
//                    continue;
//                }
                Matcher m = BOOK_START_PATTERN.matcher(curLine);
                if (true == m.find()) {
                    // Output is sorted
                    String curBook = curLine.substring(m.start(), m.end());
                    int comp = familyId.compareTo(curBook);
                    if (comp > 0) {
                        continue;
                    }
                    else if (comp < 0) {
                        break;
                    }
                    String parts[] = curLine.split(DELIM_RESIDUE_INFO);
                    if (parts.length >= NUM_FIELDS) {
                        KeyResidue ke = new KeyResidue();
                        ke.setProtein(parts[INDEX_PROTEIN]);
                        ke.setResidueType(KeyResidue.ResidueType.valueOf(parts[INDEX_RESIDUE]));
                        ke.setPos(Integer.parseInt(parts[INDEX_POSITION]));
                        ke.setDescription(parts[INDEX_DESC]);
                        ke.setAminoAcid(parts[INDEX_AMINO_ACID].charAt(0));
                        ke.setAlignPos(Integer.parseInt(parts[INDEX_ALIGN_POS]));
                        infoList.add(ke);
                    }
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException ie) {
            ie.printStackTrace();
            return null;
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return infoList;
    }
    
    public static void main(String args[]) {
        ArrayList<KeyResidue> residues = KeyResiduesManager.getSitesForFamily("PTHR10000");
        residues = KeyResiduesManager.getSitesForFamily("PTHR10004");        
        residues = KeyResiduesManager.getSitesForFamily("PTHR10003");
        residues = KeyResiduesManager.getSitesForFamily("PTHR100ABC");        
    }
}
