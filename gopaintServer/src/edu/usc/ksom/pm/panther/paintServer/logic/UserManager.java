/**
 * Copyright 2022 University Of Southern California
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
package edu.usc.ksom.pm.panther.paintServer.logic;

import com.sri.panther.paintCommon.User;
import com.sri.panther.paintServer.database.DataIO;
import java.util.ArrayList;
import java.util.HashMap;


public class UserManager {
    private static UserManager instance = null;
    private static ArrayList<User> allUsers;
    private static HashMap<String, User> userIdToUserLookup;
    private static DataIO dataIO = DataAccessManager.getInstance().getDataIO();
    
    private UserManager() {
        
    }
    
    public static UserManager getInstance() {
        if (null != instance) {
            return instance;
        }
        init();
        return instance;
    }
    
    private static synchronized void init() {
        allUsers = dataIO.getAllUsers();
        if (null == allUsers) {
            System.out.println("Unable to retrieve all user list");
            return;
        }
        userIdToUserLookup = new HashMap<String, User>();
        for (User u: allUsers) {
            userIdToUserLookup.put(u.getUserId(), u);
        }
        instance = new UserManager();
    }
    
    public User getUser(String userId) {
        if (null == userId || null == userIdToUserLookup) {
            return null;
        }
        return userIdToUserLookup.get(userId);
    }
}
