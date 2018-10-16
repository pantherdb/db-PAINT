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
package edu.usc.ksom.pm.panther.paintCommon;

import java.io.Serializable;


public class DataTransferObj implements Serializable {
    private Object obj;
    private StringBuffer msg;
    
    public DataTransferObj() {
        
    }
    
    public DataTransferObj(Object obj, StringBuffer msg) {
        this.obj = obj;
        this.msg =  msg;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public StringBuffer getMsg() {
        return msg;
    }

    public void setMsg(StringBuffer msg) {
        this.msg = msg;
    }

}
