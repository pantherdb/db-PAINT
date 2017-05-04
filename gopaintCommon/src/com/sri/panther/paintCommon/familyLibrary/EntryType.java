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



public class EntryType implements Serializable {
  protected boolean entry;
  protected String entryType;

  public void setEntryType(String entryType) {
    if ((null == entryType) || (0 == entryType.length())) {
      entry = false;
      return;
    }
    this.entryType = entryType;
    entry = true;
  }
  public String getEntryType() {
    return entryType;
  }

  public boolean getEntry () {
    return entry;
  }
  public void resetEntry() {
    entry = false;
    entryType = null;
  }

  public EntryType() {
    resetEntry();
  }
}