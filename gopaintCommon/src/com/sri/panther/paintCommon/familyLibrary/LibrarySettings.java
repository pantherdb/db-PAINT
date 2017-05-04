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
package com.sri.panther.paintCommon.familyLibrary;

import java.io.Serializable;


public class LibrarySettings implements Serializable{
  protected String libraryRoot;
  protected String libraryName;
  protected EntryType entryType;
  protected String bookSuffix;
  protected String bookName;
  protected String libURL;
  protected boolean url;
  public String getLibraryRoot() {
    return libraryRoot;
  }
  public void setLibraryRoot(String root) {
    libraryRoot = root;
  }
  public String getLibraryName() {
    return libraryName;
  }
  public void setLibraryName(String name) {
    libraryName = name;
  }
  public EntryType getEntryType() {
    return entryType;
  }

  public void setEntryType(EntryType type) {
    entryType = type;
  }
  
  
  public String getBookSufix() {
      return bookSuffix;
  }
  
  public void setBookSUffix(String bookSuffix) {
      this.bookSuffix = bookSuffix;
  }
  
  

  public String getBookName() {
    return bookName;
  }

  public void setBookName(String name) {
    bookName = name;
  }

  public boolean getURL() {
    return url;
  }

  public void setURL(boolean b) {
    url = b;
  }

  public String getLibURL() {
    return libURL;
  }

  public LibrarySettings() {
  }

  public LibrarySettings(String bookName, EntryType entryType, String bookSufix, String libraryName, String libraryRoot, boolean url) {
    this.bookName = bookName;
    this.entryType = entryType;
    this.bookSuffix = bookSufix;
    this.libraryName = libraryName;
    this.libraryRoot = libraryRoot;
    this.url = url;
  }

  public LibrarySettings(String bookName, EntryType entryType, String bookSuffix, String libURL) {
    this.bookName = bookName;
    this.entryType = entryType;
    this.bookSuffix = bookSuffix;
    this.libURL = libURL;
  }
}