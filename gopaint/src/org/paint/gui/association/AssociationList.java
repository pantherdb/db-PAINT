package org.paint.gui.association;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.paint.datamodel.GeneNode;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.FamilyChangeEvent;
import org.paint.gui.event.FamilyChangeListener;
import org.paint.gui.event.GeneSelectEvent;
import org.paint.gui.event.GeneSelectListener;
import org.paint.gui.event.TermSelectEvent;
import org.paint.gui.event.TermSelectionListener;
import org.paint.gui.familytree.TreePanel;
import org.paint.main.PaintManager;

public class AssociationList extends JPanel 
implements GeneSelectListener, FamilyChangeListener, TermSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private TitledBorder border;

	private GeneNode node;	
	
	private AssociationTable assoc_table;
	
	final static int HEADER_HEIGHT = 20;

	final static int HEADER_MARGIN = 2;

	protected String dragTitle = "";
	
	public AssociationList() {
		super();	
		
		setLayout(new BorderLayout());

		assoc_table = new AssociationTable(); //new AssociationsTable();
		
		JScrollPane annot_scroll = new JScrollPane(assoc_table);

		border = createBorder("");
		setBorder(border);

		add(annot_scroll, BorderLayout.CENTER);
				
		EventManager.inst().registerGeneListener(this);
		EventManager.inst().registerFamilyListener(this);
		EventManager.inst().registerTermListener(this);
	}

	private TitledBorder createBorder(String title) {
		Border raisedbevel = BorderFactory.createRaisedBevelBorder();
		Border loweredbevel = BorderFactory.createLoweredBevelBorder();
		Border border = BorderFactory.createCompoundBorder(
				raisedbevel, loweredbevel);
		return BorderFactory.createTitledBorder(border, title);
	}

	public void handleGeneSelectEvent(GeneSelectEvent event) {
            GeneNode ancestor = event.getAncestor();
            if (null != ancestor) {
                setNode(ancestor);
            }
//		if (event.getGenes().size() > 0)
//			setNode(event.getGenes().get(0));
		else
			setNode(null);
		repaint();
	}

	public void newFamilyData(FamilyChangeEvent e) {
		TreePanel tree = PaintManager.inst().getTree();
		GeneNode root = tree.getRoot();
		setNode(tree.getTopLeafNode(root));
	}
	
	public void handleTermEvent(TermSelectEvent e) {
		GeneNode mrca = EventManager.inst().getAncestralSelection();
		if (!mrca.equals(node) && !e.getSource().equals(assoc_table))
			this.setNode(mrca);
	}
	
	private void setNode(GeneNode node) {
		this.node = node;
		if (node == null)
			border.setTitle("");
		else {
			border.setTitle(node.getNodeLabel() + node.getNode().getStaticInfo().getPublicId());
		}
		repaint();
	}
	
	
}

