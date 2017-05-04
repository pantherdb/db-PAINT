package org.paint.gui;

import java.awt.Component;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import net.infonode.docking.View;

import org.paint.gui.event.EventManager;
import org.paint.gui.event.ProgressEvent;
import org.paint.gui.event.ProgressEvent.Status;
import org.paint.gui.event.ProgressListener;

public class StatusView extends AbstractPaintGUIComponent implements ProgressListener {

	private static final long serialVersionUID = 1L;
	private static StatusView singleton;

	private JLabel messageLabel;
	private JProgressBar progressBar;
	
	public StatusView() {
		super("status-info:status-info");
		initLayout();
	}
	
	public static StatusView getSingleton() {
		if (singleton == null) {
			singleton = new StatusView();
		}
		EventManager.inst().registerProgressListener(singleton);
		return singleton;
	}
	
	public String getMessage() {
		return messageLabel.getText();
	}
	
	public void setMessage(String message) {
		if (message != null) {
			messageLabel.setText(message);
		}
	}
	
	public void setPercentageDone(int percentage) {
		if (percentage == 0) {
			progressBar.setIndeterminate(true);
		}
		else {
			progressBar.setIndeterminate(false);
		}
		progressBar.setValue(percentage);
		if (percentage == 100) {
			setMessage(getMessage() + " (Done)");
		}
	}

	public void handleProgressEvent(ProgressEvent event) {
		setMessage(event.getMessage());
		setPercentageDone(event.getPercentageDone());
		if (event.getStatus() == Status.START) {
			restore();
		}
		else if (event.getStatus() == Status.END) {
			minimize();
		}
		else if (event.getStatus() == Status.FAIL) {
			setMessage(getMessage() + " (Fail)");
		}
	}
	
	private void restore() {
		final View view = getIdwView();
		if (view.isMinimized()) {
			if (!EventQueue.isDispatchThread()) {
				try {
					EventQueue.invokeAndWait(new Runnable() {
						public void run() {
							view.restore();
						}
					});
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void minimize() {
		final View view = getIdwView();
		if (!view.isMinimized()) {
			if (!EventQueue.isDispatchThread()) {
				try {
					EventQueue.invokeAndWait(new Runnable() {
						public void run() {
							view.minimize();
						}
					});
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void initLayout() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		messageLabel = new JLabel(" ");
		messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		progressBar = new JProgressBar(0, 100);
		add(messageLabel);
		add(progressBar);
	}
}
