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

package org.paint.dialog;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import org.paint.config.Preferences;


public class MSAColorDialog extends JDialog{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Frame             frame;
	protected float             orig_thresholds[];
	protected Color             orig_colorList[];
	protected boolean			weighted;
	protected String            description;
	protected JPanel            mainPanel;
	protected JList             choiceList;
	protected DefaultListModel  listModel;
	protected JScrollPane       listScroller;
	protected JTextField        percentField;
	protected Hashtable<String, Color>         percentToColor;
	protected boolean           choice = false;

	/**
	 * Constructor declaration
	 *
	 *
	 * @param f frame
	 * @param percentage thresholds
	 * @param colors colors
	 * @param sfColor subfamily color
	 * @param desc description
	 *
	 * @see
	 */
	public MSAColorDialog(Frame f, boolean weighted){
		super(f, true);
		setTitle("Update MSA parameters");
		this.frame = f;
		this.weighted = weighted;
		
		Preferences prefs = Preferences.inst();
		float thresholds[] = orig_thresholds = prefs.getMSAThresholds(weighted);
		Color colorList[] = orig_colorList = prefs.getMSAColors(weighted);
		if (weighted) 
			description = "Color based on sequence weights being greater than a given thresholds";
		else
			description = "Color based on percentage of entries for a column being greater than given thresholds";

		percentToColor = new Hashtable<String, Color>();
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		JTextArea text = new JTextArea(description);

		text.setRows(1);
		text.setEditable(false);
		text.setAlignmentX(Component.LEFT_ALIGNMENT);
		JScrollPane areaScrollPane = new JScrollPane(text);

		areaScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		areaScrollPane.setPreferredSize(new Dimension(250, 40));
		mainPanel.add(areaScrollPane);
		JPanel  middlePanel = new JPanel();

		middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
		JPanel  percentPanel = new JPanel();

		percentPanel.setLayout(new BoxLayout(percentPanel, BoxLayout.X_AXIS));
		String  strPercent[] = new String[thresholds.length];

		for (int i = 0; i < thresholds.length; i++){
			strPercent[i] = Float.toString(thresholds[i]);
			percentToColor.put(strPercent[i], colorList[i]);
		}
		listModel = new DefaultListModel();
		for (int i = 0; i < strPercent.length; i++){
			listModel.addElement(strPercent[i]);
		}
		choiceList = new JList(listModel);
		choiceList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		choiceList.setCellRenderer(new CellRenderer());
		listScroller = new JScrollPane(choiceList);
		listScroller.setPreferredSize(new Dimension(50, 60));
		listScroller.setMinimumSize(new Dimension(50, 60));
		listScroller.setAlignmentX(Component.LEFT_ALIGNMENT);
		listScroller.setAlignmentY(Component.TOP_ALIGNMENT);
		percentPanel.add(listScroller);
		JPanel  operationsPanel = new JPanel();

		operationsPanel.setLayout(new BoxLayout(operationsPanel, BoxLayout.Y_AXIS));
		JButton changeColor = new JButton("Change Color");

		changeColor.addActionListener(new ChangeColorActionListener());
		changeColor.setAlignmentX(Component.LEFT_ALIGNMENT);
		operationsPanel.add(changeColor);
		JPanel  addThreshPanel = new JPanel();

		addThreshPanel.setLayout(new BoxLayout(addThreshPanel, BoxLayout.X_AXIS));
		JButton addButton = new JButton("Add");

		addButton.addActionListener(new AddActionListener());
		addButton.setAlignmentY(Component.TOP_ALIGNMENT);
		percentField = new JTextField();
		percentField.setColumns(3);
		percentField.setPreferredSize(new Dimension(50, 20));
		percentField.setAlignmentY(Component.TOP_ALIGNMENT);
		addThreshPanel.add(addButton);
		addThreshPanel.add(percentField);
		addThreshPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		operationsPanel.add(addThreshPanel);
		JButton remove = new JButton("Remove");

		remove.addActionListener(new RemoveActionListener());
		remove.setAlignmentX(Component.LEFT_ALIGNMENT);
		operationsPanel.add(remove);
		operationsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		percentPanel.add(operationsPanel);
		percentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		middlePanel.add(percentPanel);

		// Set the bounds of the dialog, increase height, if the subfamily color is displayed
		Rectangle r = frame.getBounds();

		setBounds(r.x + r.width / 2, r.y + r.height / 2, 300, 200);
		middlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(middlePanel);
		JPanel  decisionPanel = new JPanel();

		decisionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		JButton okButton = new JButton("OK");

		okButton.addActionListener(new OKButtonActionListener());
		decisionPanel.add(okButton);
		JButton cancelButton = new JButton("Cancel");

		cancelButton.addActionListener(new CancelButtonActionListener());
		decisionPanel.add(cancelButton);
		mainPanel.add(decisionPanel);
		this.setContentPane(mainPanel);
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public boolean display(){
		setVisible(true);
		return choice;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public float[] getThresholds(){
		String  values[] = new String[listModel.getSize()];

		listModel.copyInto(values);
		float percentages[] = new float[values.length];

		for (int i = 0; i < values.length; i++){
			percentages[i] = Float.parseFloat(values[i]);
		}
		return percentages;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public Color[] getColors(){
		String  values[] = new String[listModel.getSize()];

		listModel.copyInto(values);
		Color colors[] = new Color[values.length];

		for (int i = 0; i < values.length; i++){
			colors[i] = (Color) percentToColor.get(values[i]);
		}
		return colors;
	}

	/**
	 * Class declaration
	 *
	 *
	 * @author
	 * @version %I%, %G%
	 */
	private class CellRenderer extends JLabel implements ListCellRenderer{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor declaration
		 *
		 *
		 * @see
		 */
		public CellRenderer(){
			setOpaque(true);
		}

		/**
		 * Method declaration
		 *
		 *
		 * @param list
		 * @param value
		 * @param index
		 * @param isSelected
		 * @param chf
		 *
		 * @return
		 *
		 * @see
		 */
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean chf){     // cell has focus?
			setText((String) value);
			Color c = (Color) MSAColorDialog.this.percentToColor.get(value);

			if (null == c){
				System.out.println("Color not found for percent");
				return this;
			}
			setBackground(c);
			if (isSelected){
				setForeground(list.getSelectionForeground());
			}
			else{
				setForeground(Color.black);
			}
			return this;
		}

	}

	/**
	 * Class declaration
	 *
	 *
	 * @author
	 * @version %I%, %G%
	 */
	private class ChangeColorActionListener implements ActionListener{

		/**
		 * Method declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		public void actionPerformed(ActionEvent e){
			String  o = (String) choiceList.getSelectedValue();

			if (null == o){
				JOptionPane.showMessageDialog(frame, "Please select an entry from the list");
				return;
			}
			Color c = JColorChooser.showDialog(MSAColorDialog.this, "Choose Color", (Color) percentToColor.get(o));

			if (null != c){
				percentToColor.put(o, c);
			}
		}

	}

	/**
	 * Class declaration
	 *
	 *
	 * @author
	 * @version %I%, %G%
	 */
	private class AddActionListener implements ActionListener{

		/**
		 * Method declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		public void actionPerformed(ActionEvent e){
			String  s = percentField.getText();

			if ((null == s) || (0 == s.length())){
				JOptionPane.showMessageDialog(frame, "Please enter a percentage value in the text box");
				return;
			}
			try{

				// Make sure value entered is a valid float
				Float f = new Float(s);

				if ((f.floatValue() <= 0) || (f.floatValue() > 100)){
					JOptionPane.showMessageDialog(frame, "Please enter a value that is between 0 and 100");
					return;
				}
				s = f.toString();

				// Make sure it is not already in the list
				if (null != percentToColor.get(s)){
					JOptionPane.showMessageDialog(frame, "Please enter a value that is not currently in the list");
					return;
				}
				Color c = JColorChooser.showDialog(MSAColorDialog.this, "Choose Color", Color.black);

				if (null == c){
					return;
				}

				// Add entry to percentage to color table
				percentToColor.put(s, c);

				// Have to add users choice back into the list.  But, first list has to be sorted in numerical order.
				float entries[] = new float[choiceList.getModel().getSize() + 1];

				for (int i = 0; i < choiceList.getModel().getSize(); i++){
					entries[i] = Float.parseFloat((String) choiceList.getModel().getElementAt(i));
				}
				entries[entries.length - 1] = f.floatValue();
				Arrays.sort(entries);

				// Remove all elements in list and add newly sorted list.  Note:  Thresholds are stored in reverse order
				listModel.removeAllElements();
				for (int i = entries.length - 1; i >= 0; i--){
					listModel.addElement(Float.toString(entries[i]));
				}
			}
			catch (NumberFormatException nfe){
				JOptionPane.showMessageDialog(frame, "Please enter a valid float value");
				return;
			}
		}

	}

	/**
	 * Class declaration
	 *
	 *
	 * @author
	 * @version %I%, %G%
	 */
	private class RemoveActionListener implements ActionListener{

		/**
		 * Method declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		public void actionPerformed(ActionEvent e){
			Object  o = choiceList.getSelectedValue();

			if (null == o){
				JOptionPane.showMessageDialog(frame, "Please select an entry from the list");
				return;
			}
			percentToColor.remove(o);
			listModel.remove(choiceList.getSelectedIndex());
		}

	}

	/**
	 * Class declaration
	 *
	 *
	 * @author
	 * @version %I%, %G%
	 */
	private class OKButtonActionListener implements ActionListener{

		/**
		 * Method declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		public void actionPerformed(ActionEvent e){
			choice = true;
			Preferences.inst().setMSAColors(weighted, getColors());
			Preferences.inst().setMSAThresholds(weighted, getThresholds());
			MSAColorDialog.this.setVisible(false);
		}
	}

	/**
	 * Class declaration
	 *
	 *
	 * @author
	 * @version %I%, %G%
	 */
	private class CancelButtonActionListener implements ActionListener{

		/**
		 * Method declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		public void actionPerformed(ActionEvent e){
			choice = false;
			MSAColorDialog.this.setVisible(false);
		}
	}
}
