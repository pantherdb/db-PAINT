 /**
 * Copyright 2023 University Of Southern California
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
package edu.usc.ksom.pm.panther.paintServer.listener;


import com.sri.panther.paintServer.database.DataServerManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ServerStartupShutdownListener implements ServletContextListener {

    private ServletContext context;

    public void contextInitialized(ServletContextEvent contextEvent) {
        System.out.println("Context Created");
        context = contextEvent.getServletContext();
        executeTimedTasks();
    }
    
    public void contextDestroyed(ServletContextEvent contextEvent) {
        System.out.println("Context Destroyed ");
        // Delete temporary tables on shutdown.
        //QueryUtility.deleteTempTables();
        context = contextEvent.getServletContext();
        System.out.println("Going to release db connections");
        DataServerManager.closeConnectionPool();
    }

    public ServletContext getContext() {
        return context;
    }
    
    private void executeTimedTasks() {
        // Define a task for connecting to the database and executing a query.  And keeps on repeating every 10 minutes
//        TimerTask task = new TimerTask() {
//            public void run() {
//                System.out.println("Repeatable task executing at " + new Date());
//                DataServer ds = DataServerManager.getDataServer();
//                ds.executeQueryToVerifyConnection();
//            }
//        };
//        Timer t = new Timer("Test connection");
//        long delay = 1000L;
//        long period = 1000L * 60L * 10L; // Repeat every 10 minutes
//        t.scheduleAtFixedRate(task, delay, period);        
    }
}
