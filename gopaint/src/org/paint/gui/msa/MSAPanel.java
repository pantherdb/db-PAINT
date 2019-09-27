/**
 *  Copyright 2019 University Of Southern California
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.paint.gui.msa;

import com.sri.panther.paintCommon.util.Utils;
import edu.usc.ksom.pm.panther.paintCommon.Domain;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;
import org.paint.datamodel.GeneNode;
import org.paint.gui.FamilyViews;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.GeneSelectEvent;
import org.paint.gui.event.GeneSelectListener;
import org.paint.gui.event.NodeReorderEvent;
import org.paint.gui.event.NodeReorderListener;
import org.paint.main.PaintManager;

public class MSAPanel extends JPanel
        implements
        MouseListener,
        MouseMotionListener,
        Scrollable,
        GeneSelectListener,
        NodeReorderListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MSA msa;

	private static Logger log = Logger.getLogger(MSAPanel.class);

	/**
	 * Constructor declaration
	 *
	 *
	 * @param msa
	 * @param dvm
	 *
	 * @see
	 */
	public MSAPanel(){
		setBackground(Color.white);
		addMouseListener(this);
                addMouseMotionListener(this);
		EventManager manager = EventManager.inst();
		manager.registerGeneListener(this);
		manager.registerNodeReorderListener(this);
	}

	protected void paintComponent(Graphics g){      
		super.paintComponent(g);
		if (null == msa) {
			return;
		}
		msa.draw(g, ((JScrollPane) (this.getParent().getParent())).getViewport().getViewRect());
	}

	// MouseListener implementation methods

	/**
	 * Method declaration
	 *
	 *
	 * @param e
	 *
	 * @see
	 */
    public void mouseClicked(MouseEvent e) {
        if (null == msa) {
            return;
        }
        if (MSA.MSA_DISPLAY.DOMAIN_TRIMMED == msa.getDisplayType()) {
            return;
        }

        // Handle only right click
        Graphics g = this.getGraphics();
        Point p = e.getPoint();
        if (InputEvent.BUTTON3_MASK != (e.getModifiers() & InputEvent.BUTTON3_MASK)) {
            GeneNode node = msa.getSelectedGene(p, g);
            if (node != null) {
                ArrayList<GeneNode> selection = new ArrayList<GeneNode>();
                selection.add(node);
                GeneSelectEvent ge = new GeneSelectEvent(this, selection, node);
                EventManager.inst().fireGeneEvent(ge);
            }
        } else {
            if (true == msa.setSelectedColInfo(p, g)) {
                super.paintComponent(g);
                msa.draw(g, ((JScrollPane) (this.getParent().getParent())).getViewport().getViewRect());
            }
        }
    }

	/**
	 * Method declaration
	 *
	 *
	 * @param e
	 *
	 * @see
	 */
	public void mousePressed(MouseEvent e) {}

	/**
	 * Method declaration
	 *
	 *
	 * @param e
	 *
	 * @see
	 */
	public void mouseReleased(MouseEvent e) {}

	/**
	 * Method declaration
	 *
	 *
	 * @param e
	 *
	 * @see
	 */
	public void mouseEntered(MouseEvent e) {}

	/**
	 * Method declaration
	 *
	 *
	 * @param e
	 *
	 * @see
	 */
	public void mouseExited(MouseEvent e) {}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	private Dimension getDrawAreaSize(){
		Dimension d = new Dimension(0, 0);

		if (msa == null || getGraphics() == null){
			return null;
		}
		Rectangle newRect = msa.getGridSize(this.getGraphics());

		if (null != newRect){
			d.width = newRect.width;
			d.height = newRect.height;
		}
		return d;
	}

	public Dimension getPreferredSize() {
		Dimension d = getDrawAreaSize();
		if (d == null || (d.width == 0 && d.height == 0)) {
			d = super.getPreferredSize();
		}
		else {
			int pad = FamilyViews.inst().getBottomMargin(FamilyViews.MSA_PANE);
			d.height += pad;
		}
		return d;
	}

	public void handleGeneSelectEvent (GeneSelectEvent e) {
		if (msa == null || e.getPrevious() == e.getGenes())
			return;
		repaintSelection(e.getPrevious());
		repaintSelection(e.getGenes());
	}

	public void handlePruning (GeneNode node) {
		List<GeneNode> temp = new ArrayList<GeneNode>();
		temp.add(node);
		repaintSelection(temp);
	}
	
	private void repaintSelection(Collection<GeneNode> nodes) {
		/*
		 * Not assuming that the nodes are in rank order
		 * from top to bottom
		 */
		Graphics  g = this.getGraphics();
		Rectangle rect = msa.getSelectionRect(g, nodes);
		if (rect != null)
			repaint(rect);
	}

	public void handleNodeReorderEvent(NodeReorderEvent e) {
		if (null == msa) {
			return;
		}
		msa.reorderRows((List<GeneNode>) e.getNodes());
		revalidate();
	}

	public void setWeighted(boolean weighted) {
		if (msa != null) {
			msa.setWeighted(weighted);
			repaint();
		}
	}
	
	public void setModel(MSA msa) {
		this.msa = msa;
		revalidate();
	}
        
        public void handleDomainData(HashMap<String, HashMap<String, ArrayList<Domain>>> domainLookup) {
            if (null == msa) {
                return;
            }
            msa.handleDomainData(domainLookup);
            revalidate();
        }

	public boolean isWeighted() {
		if (msa != null)
			return msa.isWeighted();
		else
			return false;
	}

	public boolean haveWeights() {
            if (null == msa) {
                return false;
            }
		return msa.haveWeights();
	}

	public void updateColors() {
            if (null == msa) {
                return;
            }
		msa.updateColors();
		repaint();
	}

	public boolean isFullLength() {
            if (null == msa) {
                return false;
            }
		return msa.isFullLength();
	}

	public void setFullLength(boolean full) {
		if (msa != null) {
			msa.setFullLength(full);
			repaint();
		}
	}
        
	public Dimension getPreferredScrollableViewportSize() {
		if (getGraphics() != null && msa != null) {
			Rectangle msa_rect = msa.getGridSize(this.getGraphics());
			return msa_rect.getSize();
		} else
			return new Dimension();
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		if (orientation == SwingConstants.VERTICAL) {
			int row_height = PaintManager.inst().getRowHeight();
			int rows = visibleRect.height / row_height;
			return (rows + 1) * row_height;
		} else {
			int col_width = msa.getColumnWidth(getGraphics());
			int cols = visibleRect.width / col_width;
			return (cols + 1) * col_width;
		}
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		int currentPosition;
		int maxUnitIncrement;
		if (orientation == SwingConstants.VERTICAL) {
			currentPosition = visibleRect.y;
			maxUnitIncrement = PaintManager.inst().getRowHeight();
		} else {
			currentPosition = visibleRect.x;
			maxUnitIncrement = msa.getColumnWidth(getGraphics());
		}

		//Return the number of pixels between currentPosition
		//and the nearest tick mark in the indicated direction.
		int increment;
		if (direction < 0) {
			int newPosition = currentPosition -
					(currentPosition / maxUnitIncrement)
					* maxUnitIncrement;
			increment = (newPosition == 0) ? maxUnitIncrement : newPosition;
		} else {
			increment = ((currentPosition / maxUnitIncrement) + 1)
					* maxUnitIncrement
					- currentPosition;
		}
		return increment;
	}
        
        public MSA getModel() {
            return msa;
        }

    @Override
    public void mouseDragged(MouseEvent e) {
        
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (null != msa) {
//            if (MSA.MSA_DISPLAY.DOMAIN != msa.getDisplayType()) {
//                return;
//            }
            Graphics g = this.getGraphics();
            Point p = e.getPoint();
            ArrayList<Domain> domainList = msa.getDomains(p, g);
            if (null == domainList) {
                return;
            }
            HashSet<String> domainSet = new HashSet<String>();
            for (Domain d : domainList) {
                domainSet.add(d.getHmmName() + " (" + d.getHmmAcc() + " Range " + d.getStart() + " - " + d.getEnd() + " )");

            }
            this.setToolTipText(Utils.listToString(new Vector(domainSet), "", ", "));
        }
    }

}
