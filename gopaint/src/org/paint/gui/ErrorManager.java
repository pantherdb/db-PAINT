package org.paint.gui;

import java.awt.Color;
import java.awt.Frame;

import javax.swing.JOptionPane;

import org.bbop.framework.GUIManager;

public class ErrorManager {

	@SuppressWarnings("static-access")
	public static void showWarning(String message, String title) {
		JOptionPane pane = new JOptionPane();
		pane.setBackground(Color.white);
		Frame frame = GUIManager.getManager().getFrame();
		pane.showMessageDialog(frame, message, title, JOptionPane.WARNING_MESSAGE);
	}
}
