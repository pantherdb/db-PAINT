/**
 *  Copyright 2024 University Of Southern California
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
import java.util.List;
import org.apache.log4j.Logger;

public class HorizontalTransferColor {
    private static Logger log = Logger.getLogger(HorizontalTransferColor.class);
    private static List<Color> colorList = DuplicationColor.inst().getPastelColors();
    private static HorizontalTransferColor instance;

    private HorizontalTransferColor() {
    }

    public static HorizontalTransferColor getInstance() {
        if (instance == null) {
            instance = new HorizontalTransferColor();
        }
        return instance;
    }    
    
    public Color getHorizontalTransferColor(int index) {
        if (index >= 0 && index < colorList.size()) {
            return colorList.get(index);
        }

        int next = index % colorList.size();
        log.debug("Horizontal transfer coloring - reguested color index=" + index + " only " + colorList.size() + " colors available, using " + next);
        return colorList.get(next);
    }

}
