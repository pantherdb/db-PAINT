/**
 * Copyright 202 University Of Southern California
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
package com.sri.panther.paintCommon;


import java.io.Serializable;

public class User implements Serializable {
    protected String firstName;
    protected String lastName;
    protected String email;
    protected int privilegeLevel = Constant.USER_PRIVILEGE_NOT_SET;
    protected String loginName;
    protected String groupName;
    protected String userId;
    protected String name;
    
    public User(String firstName, String lastName, String email, String loginName, int privilegeLevel, String groupName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.loginName = loginName;
        this.privilegeLevel = privilegeLevel;
        this.groupName = groupName;
    }
    
    public User() {
        
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    
    
    public int getprivilegeLevel() {
        return privilegeLevel;
    }
    
    
    public void setPrivilegeLevel(int privilegeLevel) {
        this.privilegeLevel = privilegeLevel;
    }
    
    
    public String getloginName() {
        return loginName;
    }
    
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public static boolean isGOUser(boolean isLogged, String groupName) {
        if (false == isLogged) {
            return false;
        }
        
        if (null == groupName) {
            return false;
        }
        
        if (0 != groupName.compareTo(Constant.GROUP_NAME_GO_USER)){
                return false;
        }
        return true;
        
        
    }
    
    
    public Object clone() {
        return new User(firstName, lastName, email, loginName, privilegeLevel, groupName);

    }


}
