/* Copyright (C) 2008 SRI International
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
    
    public User(String firstName, String lastName, String email, String loginName, int privilegeLevel, String groupName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.loginName = loginName;
        this.privilegeLevel = privilegeLevel;
        this.groupName = groupName;
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
