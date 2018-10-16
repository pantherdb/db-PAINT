/**
 *  Copyright 2016 University Of Southern California
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


/**
 *
 * @author muruganu
 */
public class Node implements Serializable, IWith {
    private NodeStaticInfo staticInfo;
    private NodeVariableInfo variableInfo;

    public NodeStaticInfo getStaticInfo() {
        return staticInfo;
    }

    public void setStaticInfo(NodeStaticInfo staticInfo) {
        this.staticInfo = staticInfo;
    }

    public NodeVariableInfo getVariableInfo() {
        return variableInfo;
    }

    public void setVariableInfo(NodeVariableInfo variableInfo) {
        this.variableInfo = variableInfo;
    }
    

}
