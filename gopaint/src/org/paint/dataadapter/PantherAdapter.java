/* 
 * 
 * Copyright (c) 2010, Regents of the University of California 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Neither the name of the Lawrence Berkeley National Lab nor the names of its contributors may be used to endorse 
 * or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package org.paint.dataadapter;

import java.util.Vector;

import org.paint.datamodel.Family;
import org.paint.util.LoginUtil;

import com.sri.panther.paintCommon.RawComponentContainer;


public class PantherAdapter extends FamilyAdapter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String book;

	/**
	 * Constructor declaration
	 *
	 *
	 * @param rcc
	 * @param commonProperties
	 *
	 * @see
	 */
	public PantherAdapter(String book){
		this.book = book;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	@Override
	public boolean fetchFamily(Family family){
//		if (book != null) {
//			RawComponentContainer rcc;
//			rcc = PantherServer.inst().getRawPantherFam(LoginUtil.getUserInfo(), book);
//			if (rcc != null){
//				family.setRCC(rcc);
//
//				Vector<String[]>        treeInfo = (Vector<String[]>) rcc.getTree();
//				String                  treeStrings[] = treeInfo.elementAt(RawComponentContainer.INDEX_TREE_STR);
//				String                  sfAnInfo[] = treeInfo.elementAt(RawComponentContainer.INDEX_SF_AN);
//				String[]                attrContents = rcc.getAttributeTable();
//				Vector<String[]>        msaContents = (Vector<String[]>) rcc.getMSA();
//
//				String[] msaInfo;
//				msaInfo = (msaContents != null && msaContents.size() > 0) ? msaContents.elementAt(0) : null;
//				
//				String[] wtsInfo;
//				wtsInfo = (msaContents != null && msaContents.size() > 1) ? msaContents.elementAt(1) : null;
//				
//				// Init the family with the data retrieved data
//				return initFamily(family, treeStrings, sfAnInfo, attrContents, msaInfo, wtsInfo);
//			}
//		}
		return false;
	}

}
