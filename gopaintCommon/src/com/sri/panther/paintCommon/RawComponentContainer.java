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


import com.sri.panther.paintCommon.GO.Evidence;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;


public class RawComponentContainer implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8486029820148179781L;
	protected static final Integer TREE = new Integer (0);
	protected static final Integer ATTRIBUTE_TABLE = new Integer (1);
	protected static final Integer MSA = new Integer( 2);
        protected static final Integer GO_INFO = new Integer( 3);
        protected static final Integer PANTHER_ANNOT = new Integer(4);
	protected Hashtable<Integer, Object> components = null;
	protected String book;      // Accession
	protected String name;      // Name


	// Tree object contains the following items in a Vector
	public static final int INDEX_TREE_STR = 0;
	public static final int INDEX_SF_NODES = 1;
	public static final int INDEX_SF_AN = 2;
        
        
        // GO information contains the following items in a vector
        public static final int INDEX_GO_ANNOT = 0;
        public static final int INDEX_GO_INFER = 1;
        
        
        // PANTHER annot contains the following items in a vector
        public static final int INDEX_ANNOT_CURRENT = 0;
        public static final int INDEX_ANNOT_ADDED = 1;      
        public static final int INDEX_ANNOT_REMOVED = 2;

	public RawComponentContainer() {
	}

	public void setTree(Object tree) {
		if (null == tree) {
			return;
		}
		if (null == components) {
			components = new Hashtable<Integer, Object>();
		}
		components.put(TREE, tree);
	}

	public Object getTree() {
		if (null == components) {
			return null;
		}
		return (Object)components.get(TREE);
	}

	public void setAttributeTable(Object table) {
		if (null == table) {
			return;
		}
		if (null == components) {
			components = new Hashtable<Integer, Object>();
		}
		components.put(ATTRIBUTE_TABLE, table);
	}

	public String[] getAttributeTable() {
		if (null == components) {
			return null;
		}
		return (String [])components.get(ATTRIBUTE_TABLE);
	}

	public void setMSA(Object msa) {
		if (null == msa) {
			return;
		}
		if (null == components) {
			components = new Hashtable<Integer, Object>();
		}
		components.put(MSA, msa);
	}

	public Object getMSA() {
		if (null == components) {
			return null;
		}
		return (Object)components.get(MSA);
	}
        
        
        public void setGOInfo(Object goInfo) {
            if (null == goInfo) {
                return;
            }
            if (null == components) {
                components = new Hashtable<Integer, Object>();
            }
            components.put(GO_INFO, goInfo);
        }
        
        public Object getGOInfo() {
            if (null == components) {
                return null;
            }
            return components.get(GO_INFO);
        }


    public void setPANTHERInfo(Object pantherInfo) {
        if (null == pantherInfo) {
            return;
        }
        if (null == components) {
            components = new Hashtable<Integer, Object>();
        }
        components.put(PANTHER_ANNOT, pantherInfo);
    }

    public Object getPANTHERInfo() {
        if (null == components) {
            return null;
        }
        return components.get(PANTHER_ANNOT);
    }
        

	public void setBook(String book) {
		this.book = book;
	}

	public String getBook() {
		return book;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}