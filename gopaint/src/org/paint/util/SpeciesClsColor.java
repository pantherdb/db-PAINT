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
package org.paint.util;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.paint.gui.familytree.TreeModel;


public class SpeciesClsColor {
    private static SpeciesClsColor inst = null;
    private static List<Color> colorList = null;
    
    private static Color DEFAULT_OTHER = Color.WHITE;
    private static HashMap<String, Color> orgLookup = null;
    
    private SpeciesClsColor() {
        
    }
    
    public static synchronized SpeciesClsColor getInst() {
        if (null == inst) {
            inst = new SpeciesClsColor();
            colorList = DuplicationColor.inst().getPastelColors();
            int counter = 0;
            orgLookup = new HashMap<String, Color>(); 
            for (String org: TreeModel.SPECIES_CLASSIFICATION) {
                Color c = colorList.get(counter);
                if (c.equals(DEFAULT_OTHER)) {
                    counter++;
                }
                orgLookup.put(org.toUpperCase(), colorList.get(counter));
                counter++;
            }
            
        }
        return inst;
    }
    
    public Color getColorForSpecies(String speciesCls) {
        if (null == speciesCls) {
            return DEFAULT_OTHER;
        }
        Color c = orgLookup.get(speciesCls.toUpperCase());
        if (null == c) {
            return DEFAULT_OTHER;
        }
        return c;
    }
    
    
    
    
}
