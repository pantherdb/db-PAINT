package org.paint.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import org.bbop.io.FileUtil;
import org.paint.config.Preferences;


/**
 * Displays information about the product.
 */
public class AboutDialog extends JDialog implements ActionListener {

	public AboutDialog(Frame owner) {
		super(owner, "About", false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static String paint_fullname = "PAINT: Protein Annotion and INference Tool";

	private final static String developers = 
		"Developed by: " +
		"Suzanna Lewis, " +
		"Ed Lee, " + 
		"Paul Thomas, " + 
		"Pascale Gaudet, " +
		"and Michael Livstone\n\n" +
		"Former developers: " +
		"Kara Dolinski, " + 
		"Anushya Muruganujan";

	private static final String logoFile = "images/paints.jpg";    
	private ImageIcon image;
	
	/**
	 * Set up and show the dialog. 
	 */
	public void showDialog() {
		configure();
		setLocationRelativeTo(null);
		this.setVisible(true);
		pack();
	}


	/*
	 * (non-Javadoc) Method declared on Window.
	 */
	protected void configure() {

		try {
			try {
				image = new ImageIcon(FileUtil.findUrl(logoFile));
			}	catch (FileNotFoundException ex) {
				System.out.println("Unable to open logoFile");
			}

			//this.setLayout(new GridLayout(3, 1));
			this.setLayout(new GridBagLayout());
			
			JPanel icon_panel = new JPanel();
			icon_panel.setLayout(new BorderLayout());
			JLabel program = new JLabel(paint_fullname, image, SwingConstants.CENTER);
			JLabel version = new JLabel(Preferences.inst().getVersion().toString(), image, SwingConstants.CENTER);
			program.setFont(new Font("Arial", Font.BOLD, 14));
			version.setFont(new Font("Arial", Font.BOLD, 12));
			icon_panel.add(program, BorderLayout.CENTER);
			icon_panel.add(version, BorderLayout.SOUTH);

			//Create a container so that we can add a title around
			//the list of developers.
			JPanel listPane = new JPanel();
			listPane.setLayout(new BorderLayout());
			listPane.add(Box.createRigidArea(new Dimension(0,5)));
			JTextArea dev = new JTextArea(developers);
			dev.setRows(3);
			dev.setLineWrap(true);
			dev.setWrapStyleWord(true);
			dev.setMargin(new Insets(4, 4, 4, 4));
			dev.setEditable(false);
			listPane.add(dev);

			//Lay out the buttons from left to right.
			JPanel buttonPane = new JPanel();
			//buttonPane.setLayout(new SpringLayout());
			//Create and initialize the buttons.
			JButton closeButton = new JButton("Close");
			closeButton.addActionListener(this);
			buttonPane.add(closeButton);

			//Put everything together, using the content pane's BorderLayout.
			/*
			this.add(icon_panel, BorderLayout.CENTER);
			this.add(listPane, BorderLayout.EAST);
			this.add(buttonPane, BorderLayout.SOUTH);
			*/
			GridBagConstraints c = new GridBagConstraints();
			add(icon_panel, c);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridy = 1;
			add(listPane, c);
			c.gridy = 2;
			add(buttonPane, c);

			pack();

		} catch (Exception e) {
			System.out.println("Some kind of error loading about dialog");
			e.printStackTrace();
		}

	}

	public void actionPerformed(ActionEvent e) {
		this.setVisible(false);
		this.dispose();
	}

}

