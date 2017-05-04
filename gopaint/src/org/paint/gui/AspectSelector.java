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

package org.paint.gui;

import java.util.HashMap;

import org.paint.gui.event.AspectChangeEvent;
import org.paint.gui.event.EventManager;

public class AspectSelector {

	private static final long serialVersionUID = 1L;
	private static AspectSelector selector;

	private Aspect aspect;

	public enum Aspect {
		BIOLOGICAL_PROCESS,
		CELLULAR_COMPONENT,
		MOLECULAR_FUNCTION;

		public String toString() {
			return super.toString().toLowerCase();
		}
	}

	public final static HashMap<String, String> aspects = new HashMap<String, String> ();
	static	{
		aspects.put(Aspect.BIOLOGICAL_PROCESS.toString(), "P");
		aspects.put(Aspect.CELLULAR_COMPONENT.toString(), "C");
		aspects.put(Aspect.MOLECULAR_FUNCTION.toString(), "F");
	}
        
        public static final HashMap<String, String> LETTER_TO_ASPECT = new HashMap<String, String> ();
        static {
		LETTER_TO_ASPECT.put("P", Aspect.BIOLOGICAL_PROCESS.toString());
		LETTER_TO_ASPECT.put("C", Aspect.CELLULAR_COMPONENT.toString());
		LETTER_TO_ASPECT.put("F", Aspect.MOLECULAR_FUNCTION.toString());            
        }


	private AspectSelector() {
		aspect = Aspect.MOLECULAR_FUNCTION;
	}

	public static AspectSelector inst() {
		if (selector == null) {
			selector = new AspectSelector();
		}
		return selector;
	}

	public Aspect getAspect() {
		return aspect;
	}

	public void setAspect(Aspect new_aspect) {
		if (aspect != new_aspect) {
			this.aspect = new_aspect;
			EventManager.inst().fireAspectChangeEvent(new AspectChangeEvent(this));
		}
	}

}
