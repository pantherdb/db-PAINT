package org.paint.dialog;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class CurationStatusColorDialog extends JDialog {

	private static final long serialVersionUID = 1L;


	public CurationStatusColorDialog(Frame frame) {
		super(frame, "Choose color");
		this.setModal(true);
		initLayout();
		pack();
		setLocationRelativeTo(frame);
		setVisible(true);
	}

	private void initLayout() {
		JPanel everything = new JPanel();
		everything.setLayout(new BoxLayout(everything, BoxLayout.Y_AXIS));

		final CurationStatusColorPane annot_color;
		final AspectColorPane aspect_color;
		final JTabbedPane tab_pane;

		annot_color = new CurationStatusColorPane();
		aspect_color = new AspectColorPane();
		tab_pane = new JTabbedPane();
		tab_pane.addTab("Annotation Colors", annot_color);
		tab_pane.addTab("Aspect Colors", aspect_color);
		tab_pane.setOpaque(true);

		JButton ok = new JButton("Ok");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CurationStatusColorDialog.this.dispose();
			}
		});

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				annot_color.cancelColorChange();
				aspect_color.cancelColorChange();
				CurationStatusColorDialog.this.dispose();
			}
		});

		JButton reset = new JButton("Reset");
		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selIndex = tab_pane.getSelectedIndex();
				if (selIndex == 0) 
					annot_color.resetColorChange();
				else
					aspect_color.resetColorChange();
			}
		});

		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
		controls.add(ok);
		controls.add(cancel);
		controls.add(reset);
		everything.add(tab_pane);
		everything.add(controls);

		add(everything);
	}

}
