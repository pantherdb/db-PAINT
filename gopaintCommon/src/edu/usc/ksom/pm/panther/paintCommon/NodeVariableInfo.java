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
import java.util.ArrayList;

/**
 *
 * @author muruganu
 */
public class NodeVariableInfo implements Serializable {
    private ArrayList<Comment> commentList;
    private ArrayList<Annotation> goAnnotationList;
    private boolean pruned = false;

    public ArrayList<Comment> getCommentList() {
        return commentList;
    }

    public void setCommentList(ArrayList<Comment> commentList) {
        this.commentList = commentList;
    }

    public ArrayList<Annotation> getGoAnnotationList() {
        return goAnnotationList;
    }

    public void setGoAnnotationList(ArrayList<Annotation> goAnnotationList) {
        this.goAnnotationList = goAnnotationList;
    }



    public void addGOAnnotation(Annotation goAnnotation) {
        if (null == goAnnotationList) {
            this.goAnnotationList = new ArrayList<Annotation>();
        }
        this.goAnnotationList.add(goAnnotation);
    
    }
    
    public boolean isPruned() {
        return pruned;
    }

    public void setPruned(boolean pruned) {
        this.pruned = pruned;
    }




    
}
