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
