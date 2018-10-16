/**
 * Copyright 2017 University Of Southern California
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
package com.sri.panther.paintServer.datamodel;

public class Annotation {
    int annotId = -1;
    int nodeId = -1;
    int clsId = -1;
    int annotTypeId = -1;
    
    public Annotation(int annotId, int nodeId, int clsId, int annotTypeId) {
        this.annotId = annotId;
        this.nodeId = nodeId;
        this.clsId = clsId;
        this.annotTypeId = annotTypeId;
    }


    public void setAnnotId(int annotId) {
        this.annotId = annotId;
    }

    public int getAnnotId() {
        return annotId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setClsId(int clsId) {
        this.clsId = clsId;
    }

    public int getClsId() {
        return clsId;
    }

    public void setAnnotTypeId(int annotTypeId) {
        this.annotTypeId = annotTypeId;
    }

    public int getAnnotTypeId() {
        return annotTypeId;
    }
}
