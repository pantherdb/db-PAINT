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

import java.awt.Component;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.paint.gui.event.EventManager;
import org.paint.gui.event.NodeScrollEvent;
import org.paint.gui.event.NodeScrollListener;

public class NodeScroller extends JScrollPane
implements 
AdjustmentListener, 
NodeScrollListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

//	private static Logger log = Logger.getLogger(NodeScroller.class);

	public Component nodePanel;

	public NodeScroller(Component nodePanel) {
		super(nodePanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.nodePanel = nodePanel;
		JScrollBar vsb = getVerticalScrollBar();
		JScrollBar hsb = getHorizontalScrollBar();
		vsb.addAdjustmentListener(this);
		hsb.addAdjustmentListener(this);
		EventManager.inst().registerNodeScrollListener(this);
	}

	public void adjustmentValueChanged(AdjustmentEvent e) {
		if (e.getValueIsAdjusting() || EventManager.inst().isAdjusting()) {
			return;
		}
		JScrollBar scroller = getVerticalScrollBar();
		if (e.getSource() == scroller) {
			int position = e.getValue();
			int range = scroller.getMaximum() - scroller.getMinimum() - scroller.getModel().getExtent();
			if (range > 0) {
				float percent = (float)position / (float) range;
				NodeScrollEvent event = new NodeScrollEvent(e, percent);
				EventManager.inst().fireNodeScrollEvent(event);
			}
		} else {
			scroller = getHorizontalScrollBar();
//			if (e.getSource() == scroller) {
				repaint();
//			}
		}
	}

	@Override
	public void handleNodeScrollEvent(NodeScrollEvent e) {
		JScrollBar scroller = getVerticalScrollBar();
		if (e.getSource() != scroller) {
			float percent = e.getPosition();
			int range = scroller.getMaximum() - scroller.getMinimum() - scroller.getModel().getExtent();
			int new_position = (int) (percent * range);
			scroller.setValue(new_position);
		}		
	}
}
