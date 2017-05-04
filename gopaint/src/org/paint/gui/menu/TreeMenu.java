package org.paint.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.bbop.swing.DynamicMenu;
import org.paint.config.Preferences;
import org.paint.dialog.ScaleTreeDlg;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.FamilyChangeEvent;
import org.paint.gui.event.FamilyChangeListener;
import org.paint.gui.familytree.TreePanel;
import org.paint.main.PaintManager;

public class TreeMenu extends DynamicMenu implements FamilyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static Logger log = Logger.getLogger(TreeMenu.class.getName());

	private static final String expand = "Expand all nodes";
	private static final String collapse = "Collapse nodes without experimental data";
	private static final String reset = "Reset root to main";
	private static final String distance = "Use distances";
	private static final String order = "Order leaves ";
	private static final String ladder_top = "Most leaves above";
	private static final String ladder_bottom = "Most leaves below";
	private static final String species = "By species";
	private static final String scale = "Scale...";

	private JRadioButtonMenuItem species_order;

	public TreeMenu() {
		super("Tree");
		this.setMnemonic('t');

		JMenuItem expandAllNodesItem = new JMenuItem(expand);
		expandAllNodesItem.addActionListener(new TreeActionListener(TreePanel.TREE_EXPAND_ALL_NODES));
		this.add(expandAllNodesItem);

		JMenuItem collapseNonExpNodesItem = new JMenuItem(collapse);
		collapseNonExpNodesItem.addActionListener(new TreeActionListener(TreePanel.TREE_COLLAPSE_NONEXP_NODES));
		this.add(collapseNonExpNodesItem);

		// Separator line
		this.addSeparator();

		JMenuItem resetRootToMain = new JMenuItem(reset);
		resetRootToMain.addActionListener(new TreeActionListener(TreePanel.TREE_RESET_ROOT_TO_MAIN));
		this.add(resetRootToMain);

		// Separator line
		this.addSeparator();

		JCheckBoxMenuItem useDistances = new JCheckBoxMenuItem(distance);
		useDistances.setSelected(Preferences.inst().isUseDistances());
		useDistances.addActionListener(new TreeActionListener(TreePanel.TREE_USE_DISTANCES));
		this.add(useDistances);

		JMenuItem scaleTree = new JMenuItem(scale);
		scaleTree.addActionListener(new ScaleTreeActionListener());
		this.add(scaleTree);

		// Separator line
		this.addSeparator();

		JMenu tree_ordering = new JMenu(order);
		species_order = new JRadioButtonMenuItem(species);
		JRadioButtonMenuItem top_order = new JRadioButtonMenuItem(ladder_top);
		JRadioButtonMenuItem bottom_order = new JRadioButtonMenuItem(ladder_bottom);

		species_order.setSelected(true);

		ButtonGroup group = new ButtonGroup();
		group.add(species_order);
		group.add(top_order);
		group.add(bottom_order);

		species_order.addItemListener(new TreeReorderListener(TreePanel.TREE_SPECIES));
		top_order.addItemListener(new TreeReorderListener(TreePanel.TREE_TOP));
		bottom_order.addItemListener(new TreeReorderListener(TreePanel.TREE_BOTTOM));

		tree_ordering.add(species_order);
		tree_ordering.add(top_order);
		tree_ordering.add(bottom_order);

		this.add(tree_ordering);

		EventManager.inst().registerFamilyListener(this);
	}

	/**
	 * Class declaration
	 *
	 *
	 * @author
	 * @version %I%, %G%
	 * 
	 * Since this has to open a dialog to allow the user to enter the new scaling factor it gets
	 * its own listener, rather than the all-purpose one below.
	 * 
	 */
	private class ScaleTreeActionListener implements ActionListener{

		/**
		 * Method declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		public void actionPerformed(ActionEvent e){
			ScaleTreeDlg scaleTreeDlg = new ScaleTreeDlg(GUIManager.getManager().getFrame(), Preferences.inst().getTree_distance_scaling());
			Double  d = scaleTreeDlg.display();
			if (null == d){
				return;
			}
			TreePanel tree = PaintManager.inst().getTree();
			if (tree != null) {
				tree.scaleTree(d);
			} else {
				Preferences.inst().setTree_distance_scaling(d);
			}
		}
	}

	private class TreeActionListener implements ActionListener{
		int action;
		TreeActionListener(int action) {
			this.action = action;
		}
		/**
		 * Method declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		public void actionPerformed(ActionEvent e) {
			TreePanel tree = PaintManager.inst().getTree();
			if (tree != null) {
				switch (action) {
				case TreePanel.TREE_USE_DISTANCES:
					Preferences.inst().toggleUseDistances();
					tree.adjustTree();
					break;
				case TreePanel.TREE_EXPAND_ALL_NODES:
					tree.expandAllNodes();
					break;
				case TreePanel.TREE_COLLAPSE_NONEXP_NODES:
					tree.collapseNonExperimental();
					break;
				case TreePanel.TREE_RESET_ROOT_TO_MAIN:
					tree.resetRootToMain();
					break;
				}
			}
		}
	}

	private class TreeReorderListener implements ItemListener {
		int action;
		TreeReorderListener(int action) {
			this.action = action;
		}
		/**
		 * Method declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		public void itemStateChanged(ItemEvent e){
			if (e.getStateChange() == ItemEvent.SELECTED) {
				TreePanel tree = PaintManager.inst().getTree();
				if (tree != null) {
					switch (action) {
					case TreePanel.TREE_SPECIES:
						tree.speciesOrder();
						break;
					case TreePanel.TREE_TOP:
						tree.descendentCountLadder(true);
						break;
					case TreePanel.TREE_BOTTOM:
						tree.descendentCountLadder(false);
						break;
					}
				}	
			}
		}
	}

	@Override
	public void newFamilyData(FamilyChangeEvent e) {
		species_order.setSelected(true);		
	}

}

