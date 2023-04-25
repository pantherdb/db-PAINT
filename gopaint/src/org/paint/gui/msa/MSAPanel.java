/**
 *  Copyright 2022 University Of Southern California
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

import com.sri.panther.paintCommon.Constant;
import com.sri.panther.paintCommon.util.Utils;
import edu.usc.ksom.pm.panther.paintCommon.Domain;
import edu.usc.ksom.pm.panther.paintCommon.KeyResidue;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.paint.datamodel.GeneNode;
import org.paint.gui.FamilyViews;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.GeneSelectEvent;
import org.paint.gui.event.GeneSelectListener;
import org.paint.gui.event.NodeReorderEvent;
import org.paint.gui.event.NodeReorderListener;
import org.paint.main.PaintManager;
import org.paint.util.HTMLUtil;

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
        Graphics g = this.getGraphics();
        Point p = e.getPoint();
        // Left click on Domain to bring up pfam domain page
        if (MSA.MSA_DISPLAY.DOMAIN == msa.getDisplayType() || MSA.MSA_DISPLAY.DOMAIN_TRIMMED == msa.getDisplayType()) {
            int modifiers = e.getModifiers();
            if ((modifiers & InputEvent.BUTTON1_MASK) != 0 && (modifiers & InputEvent.BUTTON3_MASK) == 0) {
                ArrayList<Domain> domainList = msa.getDomains(p, g);
                HashSet<String> processedSet = new HashSet<String>();
                if (null != domainList) {
                    for (Domain d : domainList) {
                        String acc = d.getHmmAcc();
                        if (false == processedSet.contains(acc)) {
                            try {
                                HTMLUtil.bringUpInBrowser(new URL(Domain.getPFAMDomainUrl(acc)));
                            }
                            catch(MalformedURLException mfe) {
                                mfe.printStackTrace();
                            }
                        }
                        processedSet.add(acc);
                    }
                }
            }
        }
        
        
        if (MSA.MSA_DISPLAY.DOMAIN_TRIMMED == msa.getDisplayType()) {
            return;
        }

        // Handle only right click
        if (InputEvent.BUTTON3_MASK != (e.getModifiers() & InputEvent.BUTTON3_MASK)) {
            GeneNode node = msa.getSelectedGene(p, g);
            if (node != null) {
                ArrayList<GeneNode> selection = new ArrayList<GeneNode>();
                selection.add(node);
                GeneSelectEvent ge = new GeneSelectEvent(this, selection, node);
                EventManager.inst().fireGeneEvent(ge);
            }
        } else {
//            JScrollPane scrollPane = (JScrollPane) (this.getParent().getParent());
//            JScrollBar hScrollBar = scrollPane.getHorizontalScrollBar();
            Rectangle rect = ((JScrollPane) (this.getParent().getParent())).getViewport().getViewRect();
//            System.out.println("rect x =  " + rect.x + " y " + rect.y);
//            System.out.println("mouse click point " + e.getX() + " value is " + hScrollBar.getValue() + " width " + hScrollBar.getWidth());
//            System.out.println("x = " + p.x + "y = " + p.y + " x on xcreen " + e.getXOnScreen() + " y on screen " + e.getYOnScreen());
//            SwingUtilities.convertPoint(this, p, scrollPane);
//            System.out.println("Once converted to screen position x = " + p.x + "y = " + p.y);
//            int verticalScrollValue = scrollPane.getVerticalScrollBar().getValue();


//            int horizontalScrollValue = scrollPane.getHorizontalScrollBar().getValue();

            if (true == msa.setSelectedColInfo(p, g, rect)) {
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
        
//        public void handleDomainData(HashMap<String, HashMap<String, ArrayList<Domain>>> domainLookup) {
//            if (null == msa) {
//                return;
//            }
//            msa.handleDomainData(domainLookup);
//            revalidate();
//        }

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

            Graphics g = this.getGraphics();
            Point p = e.getPoint();
            if (MSA.MSA_DISPLAY.DOMAIN == msa.getDisplayType() || MSA.MSA_DISPLAY.DOMAIN_TRIMMED == msa.getDisplayType()) {

                ArrayList<Domain> domainList = msa.getDomains(p, g);
                if (null == domainList) {
                    return;
                }
                HashSet<String> domainSet = new HashSet<String>();
                for (Domain d : domainList) {
                    domainSet.add(d.getHmmName() + " (" + d.getHmmAcc() + " Range " + d.getStart() + " - " + d.getEnd() + " )");

                }
                GeneNode node = msa.getSelectedGene(p, g);
                String label = Constant.STR_EMPTY;
                if (null != node && false == domainSet.isEmpty()) {
                    label = node.getNodeLabel() + " " + node.getNode().getStaticInfo().getPublicId() + " - ";
                    this.setToolTipText(label + Utils.listToString(new Vector(domainSet), "", ", "));
                }
                else {
                    this.setToolTipText(label);
                }
            }
            else if (MSA.MSA_DISPLAY.KEY_RESIDUE == msa.getDisplayType()) {
                Rectangle viewRect = ((JScrollPane) (this.getParent().getParent())).getViewport().getViewRect();
                ArrayList<KeyResidue> applicableList = msa.getKeyResidue(p, g, viewRect);
                if (null != applicableList) {
                    ArrayList<String> dispList = new ArrayList<String>();
                    for (KeyResidue kr: applicableList) {
                        dispList.add(kr.getResidueType().toString() + " " + kr.getDescription() + kr.getAlignPos());
                    }
                    GeneNode node = msa.getSelectedGene(p, g);
                    String label = node.getNodeLabel() + " " + node.getNode().getStaticInfo().getPublicId() + " - " + String.join(Constant.STR_COMMA, dispList);
                    this.setToolTipText(label);
                }
                else {
                    this.setToolTipText(Constant.STR_EMPTY);
                }
            }
            else {
                this.setToolTipText(Constant.STR_EMPTY);
            }
        }
    }

}
