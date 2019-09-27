/**
 * Copyright 2019 University Of Southern California
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
package edu.usc.ksom.pm.panther.paintCommon;

import com.sri.panther.paintCommon.User;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author muruganu
 */
public class SaveBookInfo implements Serializable {
    private ArrayList <Node> prunedList;
    private ArrayList<Annotation> annotationList;
    private String bookId;
    private Comment comment;
    private String familyName;
    private User user;
    private Integer saveStatus;

    public ArrayList<Node> getPrunedList() {
        return prunedList;
    }

    public void setPrunedList(ArrayList<Node> prunedList) {
        this.prunedList = prunedList;
    }

    public ArrayList<Annotation> getAnnotationList() {
        return annotationList;
    }

    public void setAnnotationList(ArrayList<Annotation> annotationList) {
        this.annotationList = annotationList;
    }



    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getSaveStatus() {
        return saveStatus;
    }

    public void setSaveStatus(Integer saveStatus) {
        this.saveStatus = saveStatus;
    }
    
    
}

