package org.paint.gui.matrix;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import org.apache.log4j.Logger;
import org.geneontology.db.model.Term;
import org.obo.datamodel.Link;
import org.obo.datamodel.LinkDatabase;
import org.obo.datamodel.LinkedObject;
import org.paint.config.CustomTermList;
import org.paint.go.GO_Util;
import org.paint.main.PaintManager;

public class ColumnTermData extends JPopupMenu implements ActionListener {
	private AnnotMatrix matrix;
	private ButtonGroup term_buttons;
	private List<Term>term_list;
	private boolean odd_column = false;
	private Term narrower_term = null;
	
	private static final Logger log = Logger.getLogger(PaintManager.class);

	public ColumnTermData() {
		super("");
	}

	public void showMenu(MouseEvent e, AnnotMatrix matrix) {
		this.matrix = matrix;
		show(e.getComponent(), e.getX(), e.getY());
	}

	public boolean isOddColumn() {
		return odd_column;
	}
	
	public void setOddColumn(boolean odd) {
		odd_column = odd;
	}
	
	protected void setNarrowTerm(Term t) {
		narrower_term = t;
	}
	
	protected boolean isDeletable() {
		return narrower_term != null;
	}
	
	protected void initTermMenu(Term column_term, List<Term> column_terms) {
		if (isDeletable()) {
			JMenuItem del_column = new JMenuItem("Remove column");
			del_column.addActionListener(this);
			add(del_column);
			del_column.setEnabled(true);
			addSeparator();
		}
		LinkDatabase go_root = PaintManager.inst().getGoRoot().getLinkDatabase();
		LinkedObject go_leaf = (LinkedObject) GO_Util.inst().getObject(go_root, column_term.getAcc());
		term_buttons = new ButtonGroup();
		term_list = new ArrayList<Term>();
		JRadioButtonMenuItem radio = addTermMenuItem(column_term, term_list);
		radio.setSelected(true);
		Font font = radio.getFont();
		radio.setFont(new Font(font.getFontName(), Font.BOLD, font.getSize()));
		addParentTerms (column_term, go_root, go_leaf, column_terms, term_list);
	}

	private void addParentTerms(Term term, LinkDatabase go_root, LinkedObject go_leaf, List<Term> column_terms, List<Term> term_set) {
		LinkedObject ont_term = (LinkedObject)GO_Util.inst().getObject(go_root, term.getAcc());
		Collection<Link> parents = ont_term.getParents();
		Set<String> exclusionTerms = CustomTermList.getSingleton().getExclusionList();
		if (parents != null) {
			for (Iterator<Link> it = parents.iterator(); it.hasNext();) {
				Link link = it.next();
				LinkedObject go_parent = link.getParent();
				Term term_parent = GO_Util.inst().getTerm(go_parent, true);
				if (go_parent != go_leaf 
						&& !exclusionTerms.contains(term_parent.getAcc())
						&& !term_parent.getName().endsWith(" part")
						&& !column_terms.contains(term_parent)
						&& !term_set.contains(term_parent)) {
					addTermMenuItem(term_parent, term_set);
					addParentTerms(term_parent, go_root, go_leaf, column_terms, term_set);
				}
			}
		}		
	}

	private JRadioButtonMenuItem addTermMenuItem(Term term, List<Term> term_set) {
		JRadioButtonMenuItem radio = null;
		if (!term_set.contains(term)) {
			radio = new JRadioButtonMenuItem(term.getName());
			term_buttons.add(radio);
			radio.setActionCommand(term.getName());
			radio.addActionListener(this);
			add(radio);
			term_set.add(term);
		}
		return radio;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String term_name = e.getActionCommand();
		Term term = null;
		for (Iterator<Term> iter = term_list.iterator(); iter.hasNext() && term == null; ) {
			Term check = iter.next();
			if (check.getName().equals(term_name)) {
				term = check;
			}
		}
		Term col_term = term_list.get(0);
		Term [] origin = new Term[2];
		if (term == null) {
			// remove this column
			origin[AnnotMatrixModel.BROADER] = col_term;
			origin[AnnotMatrixModel.NARROWER] = narrower_term;
			matrix.modifyColumns (origin, true);
		} 
		else if (term != col_term) {
			// Need to add another column for the broader term that has been selected
			origin[AnnotMatrixModel.BROADER] = term;
			origin[AnnotMatrixModel.NARROWER] = col_term;
			matrix.modifyColumns (origin, false);
		}
		setVisible(false);
	}
}


