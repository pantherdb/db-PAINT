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

package org.paint.gui.matrix;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;
import org.geneontology.db.model.Term;
import org.obo.datamodel.LinkDatabase;
import org.obo.datamodel.LinkedObject;
import org.paint.datamodel.GeneNode;
import org.paint.go.GO_Util;
import org.paint.gui.evidence.PaintAction;
import org.paint.gui.familytree.TreePanel;
import org.paint.main.PaintManager;

public class AnnotationTransferHandler extends TransferHandler {

	private static final long serialVersionUID = 1L;

	private static AnnotDragGestureRecognizer recognizer = null;

	private static Logger log = Logger.getLogger(AnnotationTransferHandler.class);

	public static final DataFlavor TERM_FLAVOR =
			new DataFlavor(Term.class, "Term");

	private Set<GeneNode> visitedNodes;

	public AnnotationTransferHandler() {
		super();
		visitedNodes = new HashSet<GeneNode>();
	}

	public AnnotationTransferHandler(String property)
	{
		super(property);
		visitedNodes = new HashSet<GeneNode>();
	}

	@Override
	public boolean canImport(TransferHandler.TransferSupport support) {
            return false;
//		boolean canImport = false;
//		String because = null;
//		TreePanel tree = null;
//
//		if (support.isDrop() 
//				&& support.isDataFlavorSupported(TERM_FLAVOR) 
//				&& support.getComponent() instanceof TreePanel) {
//			canImport = true;
//			tree = (TreePanel) support.getComponent();
//		}			
//
//		GeneNode node = null;
//		if (canImport && tree != null) {
//			Point p = support.getDropLocation().getDropPoint();
//			if (!tree.pointInNode(p)) {
//				canImport = false;
//			}
//			node = tree.getClickedInNodeArea(p);
//			if (node != null) {
//				if (node.isLeaf()) {
//					canImport = false;
//				}
//				else {
//					try {
//						because = PaintAction.inst().isValidTerm((Term)support.getTransferable().getTransferData(TERM_FLAVOR), node);
//						if (because != null)  {
//							canImport = false;
//						}
//					} catch (UnsupportedFlavorException e) {
//						canImport = false;
//					} catch (IOException e) {
//						canImport = false;
//					}
//				}
//			}
//		}
//		clearVisitedNodes(tree);
//
//		if (tree != null) {
//			String drop_label = null;
//			if (node != null) {
//				visitedNodes.add(node);
//				if (canImport) {
//					node.setDropColor(Color.BLACK);
//					drop_label = node.getDatabaseID();
//				}
//				else {
//					node.setDropColor(Color.RED);
//					if (because != null)
//						drop_label = node.getDatabaseID() + " " + because;
//				}
//				tree.repaint();
//			}
//
//			Point dropPoint = support.getDropLocation().getDropPoint();
//			dropPoint.x += 10;
//			dropPoint.y += 2;
//
//			tree.setDropInfo(node, dropPoint, drop_label);
//		}
//
//		return canImport;
	}

	private void clearVisitedNodes(TreePanel tree) {
		for (GeneNode currentNode : visitedNodes) {
			if (currentNode != null)
				currentNode.setDropColor(null);
		}
		if (tree != null) {
			tree.repaint();
		}
		visitedNodes.clear();
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		AnnotMatrix table = (AnnotMatrix)c;
		int column = table.getSelectedColumn();
		AnnotMatrixModel model = (AnnotMatrixModel) table.getModel();
		Term term = model.getTermForColumn(column);
		if (term == null) {
			log.debug ("No term for column " + column);
		}
		Transferable transferable = new AnnotationTransferable(term);
		return transferable;
	}

	@Override
	public void exportAsDrag(JComponent comp, InputEvent e, int action) {
		int srcActions = getSourceActions(comp);

		// only mouse events supported for drag operations
		if (!(e instanceof MouseEvent)
				// only support known actions
				|| !(action == COPY || action == MOVE || action == LINK)
				// only support valid source actions
				|| (srcActions & action) == 0)
		{

			action = TransferHandler.NONE;
		}

		if (action != TransferHandler.NONE && !GraphicsEnvironment.isHeadless())
		{
			if (recognizer == null)
			{
				recognizer = new AnnotDragGestureRecognizer(new DragHandler());
			}
			recognizer.gestured(comp, (MouseEvent) e, srcActions, action);
		}
		else
		{
			exportDone(comp, null, TransferHandler.NONE);
		}	
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		// TODO Auto-generated method stub
		super.exportDone(source, data, action);
	}

	@Override
	public void exportToClipboard(JComponent comp, Clipboard clip, int action)
			throws IllegalStateException {
		// TODO Auto-generated method stub
		super.exportToClipboard(comp, clip, action);
	}

	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY;
	}

	@Override
	public Icon getVisualRepresentation(Transferable t) {
		//		return (Preferences.inst().getIconByName("inherited"));

		return super.getVisualRepresentation(t);
	}

	@Override
	public boolean importData(TransferHandler.TransferSupport support) {
            return true;
//		Term term = null;
//		TreePanel tree = null;
//		try {
//			term = (Term)support.getTransferable().getTransferData(TERM_FLAVOR);
//		} catch (UnsupportedFlavorException e) {
//			return false;
//		} catch (IOException e) {
//			return false;
//		}
//		if (support.getComponent() instanceof TreePanel) {
//			tree = (TreePanel) support.getComponent();
//		}
//		Point p = support.getDropLocation().getDropPoint();
//		GeneNode node = tree.getClickedInNodeArea(p);
//
//		PaintAction.inst().propagateAssociation(node, term); 
//
//		clearVisitedNodes(tree);
//
//		List<LinkedObject> terms = new LinkedList<LinkedObject>();
//		LinkDatabase goRoot = PaintManager.inst().getGoRoot().getLinkDatabase();
//		LinkedObject oboTerm = (LinkedObject) GO_Util.inst().getObject(goRoot, term.getAcc());
//		terms.add(oboTerm);
//
//		return true;
	}

	class AnnotationTransferable implements Transferable {

		private Term term;

		public AnnotationTransferable(Term term) {
			this.term = term;
		}

		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException, IOException {
			if (!isDataFlavorSupported(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			}
			return term;
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { TERM_FLAVOR };
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor.equals(TERM_FLAVOR);
		}

	}

	private static class AnnotDragGestureRecognizer extends DragGestureRecognizer
	{
		AnnotDragGestureRecognizer(DragGestureListener dgl)
		{
			super(DragSource.getDefaultDragSource(), null, NONE, dgl);
		}

		void gestured(JComponent c, MouseEvent e, int srcActions, int action)
		{
			setComponent(c);
			setSourceActions(srcActions);
			appendEvent(e);
			fireDragGestureRecognized(action, e.getPoint());
		}

		/**
		 * register this DragGestureRecognizer's Listeners with the Component
		 */
		protected void registerListeners()
		{
		}

		/**
		 * unregister this DragGestureRecognizer's Listeners with the Component
		 * <p/>
		 * subclasses must override this method
		 */
		protected void unregisterListeners()
		{
		}

	}

	/**
	 * This is the default drag handler for drag and drop operations that
	 * use the <code>TransferHandler</code>.
	 */
	private static class DragHandler implements DragGestureListener, DragSourceListener
	{

		private boolean scrolls;

		// --- DragGestureListener methods -----------------------------------

		/**
		 * a Drag gesture has been recognized
		 */
		public void dragGestureRecognized(DragGestureEvent dge)
		{
			JComponent c = (JComponent) dge.getComponent();
			AnnotationTransferHandler th = (AnnotationTransferHandler) c.getTransferHandler();
			Transferable t = th.createTransferable(c);
			if (t != null)
			{
				scrolls = c.getAutoscrolls();
				c.setAutoscrolls(false);
				FontMetrics fm = c.getGraphics().getFontMetrics();
				String term_name;
				try {
					Term term = (Term) t.getTransferData(TERM_FLAVOR);
					term_name = term.getName();
					int height = fm.getHeight() + 2;
					int width = fm.stringWidth("    " + term_name);
					Image img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
					Graphics g = img.getGraphics();
					g.setColor(Color.black);
					g.drawString(term_name, 0, height - 2);
					dge.startDrag(null, img, new Point(0, -1 * img.getHeight(null)), t, this);
					//							Cursor cursor = c.getToolkit().createCustomCursor(img, new Point(0,0), "usr");
					//							dge.startDrag(cursor, t, this);
				} catch (UnsupportedFlavorException e) {
					th.exportDone(c, t, TransferHandler.NONE);
					log.error("Unable to get term name, bad flavor");
				} catch (IOException e) {
					th.exportDone(c, t, TransferHandler.NONE);
					//					dge.startDrag(null, t, this);
					log.error("Unable to get term name, io problem");
				} catch (Exception e) {
					th.exportDone(c, t, TransferHandler.NONE);
					log.error(e.getMessage());
				}
			} else
			{
				th.exportDone(c, t, TransferHandler.NONE);
			}
		}

		// --- DragSourceListener methods -----------------------------------

		/**
		 * as the hotspot enters a platform dependent drop site
		 */
		public void dragEnter(DragSourceDragEvent dsde)
		{
		}

		/**
		 * as the hotspot moves over a platform dependent drop site
		 */
		public void dragOver(DragSourceDragEvent dsde)
		{
		}

		/**
		 * as the hotspot exits a platform dependent drop site
		 */
		public void dragExit(DragSourceEvent dsde)
		{
		}

		/**
		 * as the operation completes
		 */
		public void dragDropEnd(DragSourceDropEvent dsde)
		{
			DragSourceContext dsc = dsde.getDragSourceContext();
			JComponent c = (JComponent) dsc.getComponent();
			if (dsde.getDropSuccess())
			{
				((AnnotationTransferHandler)c.getTransferHandler()).exportDone(c, dsc.getTransferable(), dsde.getDropAction());
			}
			else
			{
				((AnnotationTransferHandler)c.getTransferHandler()).exportDone(c, dsc.getTransferable(), TransferHandler.NONE);
			}
			c.setAutoscrolls(scrolls);
		}

		public void dropActionChanged(DragSourceDragEvent dsde)
		{
		}
	}
}
