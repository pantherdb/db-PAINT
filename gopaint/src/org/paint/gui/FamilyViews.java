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

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JScrollBar;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import org.apache.log4j.Logger;
import org.paint.gui.event.AspectChangeEvent;
import org.paint.gui.event.AspectChangeListener;
import org.paint.gui.event.EventManager;
import org.paint.gui.familytree.TreePanel;
import org.paint.gui.matrix.AnnotationMatrix;
import org.paint.gui.matrix.AnnotationTransferHndlr;
import org.paint.gui.msa.MSAPanel;
import org.paint.gui.table.GeneTable;
import org.paint.main.PaintManager;
import org.paint.util.RenderUtil;

public class FamilyViews extends AbstractPaintGUIComponent 
implements AspectChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static FamilyViews singleton;

	private NodeScroller treePanel;
	private NodeScroller tablePanel;
	private NodeScroller  msaPanel;
	private NodeScroller matrixPanel;

	private JTabbedPane gtabbedPane;
	private JTabbedPane ttabbedPane;

	private static Logger log = Logger.getLogger(FamilyViews.class);

	private List<NodeScroller> scrollables;
	public static final int TREE_PANE = 0;
	public static final int MATRIX_PANE = 1;
	public static final int TABLE_PANE = 2;
	public static final int MSA_PANE = 3;

	/**
	 * Constructor declaration
	 *
	 *
	 * @param doc
	 * @param panel
	 *
	 * @see
	 */
	public FamilyViews() {
		super("tree-info:tree-info");
		this.initializeInterface();
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param frameDim
	 *
	 * @see
	 */
	private void initializeInterface() {

		if (treePanel == null) {
			GeneTable gene_table = new GeneTable();
			tablePanel = new NodeScroller(gene_table);

			MSAPanel msa = new MSAPanel();
			msaPanel = new NodeScroller(msa);

			AnnotationMatrix annot_matrix = new AnnotationMatrix();
			matrixPanel = new NodeScroller(annot_matrix);

			gtabbedPane = new JTabbedPane();
			gtabbedPane.addTab("Annotation Matrix", matrixPanel);
			gtabbedPane.addTab("Protein Information", tablePanel);
			gtabbedPane.addTab("MSA", msaPanel);
			gtabbedPane.setOpaque(true);

			TreePanel tree_pane = new TreePanel();
			treePanel = new NodeScroller(tree_pane);

			ttabbedPane = new JTabbedPane();
			ttabbedPane.addTab("Tree", treePanel);
			ttabbedPane.setOpaque(true);

			setBackground();

			PaintManager.inst().setGeneTable(gene_table);
			PaintManager.inst().setTreePane(tree_pane);
			PaintManager.inst().setMSAPane(msa);
			PaintManager.inst().setMatrix(annot_matrix);

			//TODO: move the logic to PaintManager
			//tree_pane.setTransferHandler(new AnnotationTransferHandler());
                        tree_pane.setTransferHandler(new AnnotationTransferHndlr());

			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, ttabbedPane, gtabbedPane);
			splitPane.setResizeWeight(0.75);

			setOpaque(true); // Frame content panes must be opaque
			this.setLayout(new BorderLayout());
			add(new AspectSelectorPanel(), BorderLayout.NORTH);
			add(splitPane, BorderLayout.CENTER);

			scrollables = new ArrayList<NodeScroller>();
			scrollables.add(TREE_PANE, treePanel);
			scrollables.add(MATRIX_PANE, matrixPanel);
			scrollables.add(TABLE_PANE, tablePanel);
			scrollables.add(MSA_PANE, msaPanel);

			EventManager.inst().registerAspectChangeListener(this);
		}
	}

	public static FamilyViews inst() {
		if (singleton == null) 
			singleton = new FamilyViews();
		return singleton;
	}

	public int getBottomMargin(int scroller_index) {
		NodeScroller scrolling_pane = scrollables.get(scroller_index);
		JScrollBar scroller = scrolling_pane.getHorizontalScrollBar();
		int scrollHeight = 0;
		if (scroller == null || (scroller != null && !scroller.isVisible())) {
			for (int i = 0; i < scrollables.size() && scrollHeight == 0; i++) {
				NodeScroller alt_pane = scrollables.get(i);
				scroller = alt_pane.getHorizontalScrollBar();
				if (i != scroller_index && scroller.isVisible()) {
					scrollHeight = scroller.getHeight();
				}
			}
		}
		if (scrollHeight > 0)
			scrollHeight += 1;
		return scrollHeight;
	}

	public int getHScrollerHeight(int scroller_index) {
		NodeScroller scrolling_pane = scrollables.get(scroller_index);
		JScrollBar scroller = scrolling_pane.getHorizontalScrollBar();
		int scrollHeight = scroller.getHeight();
		if (scrollHeight > 0)
			scrollHeight += 1;
		return scrollHeight;
	}

	@Override
	public void handleAspectChangeEvent(AspectChangeEvent event) {
		setBackground();
	}

	private void setBackground() {
		Color bg_color = RenderUtil.getAspectColor();
		setBackground(bg_color);
		gtabbedPane.setBackground(bg_color);
		ttabbedPane.setBackground(bg_color);
	}

}

