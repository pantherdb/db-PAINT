 /* Copyright (C) 2009 SRI International
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
package com.sri.panther.paintServer.util;

import java.util.*;
import com.sri.panther.paintCommon.util.*;

public class ConfigFile{
  // only place the commonly used properties here
  // If you need to add a property used in only one site, e.g. paint,
  // please place it in that constant class file for that site, e.g. paint.properties.
  public static final String  KEY_DB_JDBC_URL = "db.jdbc.url";
  public static final String  KEY_DB_JDBC_USERNAME = "db.jdbc.username";
  public static final String  KEY_DB_JDBC_PASSWORD = "db.jdbc.password";
  public static final String  KEY_DB_JDBC_DBSID = "db.jdbc.dbsid";
  public static final String  KEY_DB_JDBC_POOL_MINSIZE = "db.connectionpool.minsize";
  public static final String  KEY_DB_JDBC_POOL_MAXSIZE = "db.connectionpool.maxsize";
  
  public static final String PROPERTY_CLASSIFICATION_VERSION_SID = "cls_version_sid";
  public static final String PROPERTY_PANTHER_CLS_TYPE_SID = "panther_cls_type_sid";  
  


  // application variables
  public static final String  KEY_LOG_LEVEL = "application.log.level";
  public static final String  KEY_LOG_FILE = "application.log.file";

  // member variables
  public static final String  CONFIG_FILE = "CONFIG_FILE";
  private static final String ENVIRONMENT_VAR = CONFIG_FILE + '=';

  // new feature Anish 10/9/03 - array of property files is passed to 
  //readResources.  "Layering" of properties (first file in array overrides
  //property in second file if both have the same name)

  protected static String[] propertyFiles = {"paint", "database"};


  protected static Properties m_Properties = System.getProperties();
  protected static ReadResources rr = null;
  static{
    try{
      rr = new ReadResources(propertyFiles);
    }
    catch(Exception ex){
      ex.printStackTrace();
      rr = null;
    }
  }

  public static String getProperty(String key){
    if(rr == null)
      return OptionConverter.substVars(m_Properties.getProperty(key),m_Properties);
    else{
      try{
        return OptionConverter.substVars(rr.getKey(key),rr.getBundle());
      }
      catch(Exception ex){
        ex.printStackTrace();
      }
      return null;
    }
  }

  public static Enumeration getProperties(){
    try {
      return rr.getKeys();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

}
