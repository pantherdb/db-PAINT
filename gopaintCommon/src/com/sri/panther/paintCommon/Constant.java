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


public class Constant {
    public static final int USER_PRIVILEGE_NOT_SET = -10;
    public static final int USER_PRIVILEGE_UNLOGGED = -1;
    public static final int USER_PRIVILEGE_SAVE_LOCAL = 6;
    public static final int USER_PRIVILEGE_OVERWRITE_OTHER_EXP_EVIDENCE = 9;    
    
    
    public static final String STR_EMPTY = "";
    public static final String STR_DASH = "-";
    public static final String STR_COLON = ":";
    public static final String STR_SEMI_COLON = ";";
    public static final String STR_PIPE = "|";
    public static final String STR_EQUAL = "=";
    public static final String STR_SPACE = " " ;
    public static final String STR_TAB = "\t";
    public static final String STR_COMMA = ",";
    public static final String STR_PERIOD = ".";
    public static final String STR_NEWLINE = "\n";
    public static final String STR_BRACKET_ROUND_OPEN = "(";
    public static final String STR_BRACKET_ROUND_CLOSE = ")";
    public static final String STR_QUOTE_SINGLE = "'";
    public static final String STR_QUOTE_DOUBLE = "\"";
    public static final String STR_DOUBLE_SLASH = "\\";

    public static final String INFO_WINDOW = "tavInfo.jsp";
    public static final String TARGET_INFO_WINDOW = "TAVINFO";

    public static final String LNKS_WINDOW = "links.jsp";
    public static final String TARGET_LNKS_WINDOW = "TAVLNKS";


    public static final String NODE_ANNOTATION_PREFIX = "AN";
    public static final int NODE_ANNOTATION_PREFIX_LENGTH = NODE_ANNOTATION_PREFIX.length();
    public static final int NODE_ANNOTATION_MIN_NUM = 0;
    public static final String DELIM_BOOK_ANNOT_NODE = STR_COLON;
    public static final String DELIM_BOOK_SF_NODE = STR_COLON;
    public static final String DELIM_BOOK_LEAF_NODE = STR_COLON;

    public static final String NODE_SUBFAMILY_PREFIX = "SF";
    public static final int NODE_SUBFAMILY_PREFIX_LENGTH = NODE_SUBFAMILY_PREFIX.length();
    public static final int NODE_SUBFAMILY_MIN_NUM = 0;
    
    
    


    public static final String SF_AN_INFO_SEPARATOR = "\t";
    public static final int SF_AN_INDEX_SF = 0;
    public static final int SF_AN_INDEX_AN = 1;


    // User fields
    public static final String GROUP_NAME_GO_USER = "GO Curator";
    
    // GAF file fields
    public static final String GAF_FIELD_SEPARATOR = STR_TAB;
    public static final String GAF_SEPARATOR_WITH = "|";
    public static final String GAF_PANTHER_CODE_EVIDENCE = "ISS";
    public static final String GAF_EVIDENCE_CODE_SEPARATOR = STR_COMMA;
    public static final String GAF_EVIDENCE_PMID_PANTHER = "17208035";
    public static final String GAF_PANTHER_METHOD = "PMID:" + GAF_EVIDENCE_PMID_PANTHER;
    public static final String GAF_PANTHER_OBJECT_TYPE = "gene";
    
    
    public static final int SAVE_OPTION_MARK_CURATED_AND_UNLOCK = 0;
    public static final int SAVE_OPTION_SAVE_AND_KEEP_LOCKED = 1;
    public static final int SAVE_OPTION_SAVE_AND_UNLOCK = 2;
    
}
