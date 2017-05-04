package org.paint.main;

import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bbop.dataadapter.DataAdapter;
import org.bbop.framework.AbstractApplicationStartupTask;
import org.bbop.framework.GUIComponentFactory;
import org.bbop.framework.GUIManager;
import org.bbop.framework.GUITask;
import org.bbop.framework.ScreenLockTask;
import org.bbop.framework.dock.LayoutDriver;
import org.bbop.framework.dock.idw.IDWDriver;
import org.bbop.util.CollectionUtil;
import org.paint.config.Preferences;
import org.paint.dialog.AboutDialog;
import org.paint.factory.AssociationsPanelFactory;
import org.paint.factory.EvidenceFactory;
import org.paint.factory.GeneTreeFactory;
import org.paint.factory.StatusViewFactory;
import org.paint.gui.DirtyIndicator;
import org.paint.gui.PaintDockingTheme;


public class PaintStartupTask extends AbstractApplicationStartupTask {

	private static Logger LOG = Logger.getLogger(PaintStartupTask.class);
	private String[] args;

	PaintStartupTask(String[] args) {
		this.args = args;
	}

	@Override
	protected Collection<GUIComponentFactory<?>> getDefaultComponentFactories() {
		// Collection<GUIComponentFactory<?>> factories =
		// super.getDefaultComponentFactories();
		Collection<GUIComponentFactory<?>> factories = new ArrayList<GUIComponentFactory<?>>();
		factories.add(new AssociationsPanelFactory());
		factories.add(new EvidenceFactory());
		factories.add(new GeneTreeFactory());
		factories.add(new StatusViewFactory());
		return factories;
	}

	/** this is called at initialization i believe */
	@Override
	protected void doOtherInstallations() {
	}

	@Override
	protected Collection<DataAdapter> getDefaultDataAdapters() {
		List<DataAdapter> adapters = new LinkedList<DataAdapter>();
		return adapters;
	}


	@Override
	protected Action getAboutAction() {
		return new AbstractAction("About PAINT") {
			public void actionPerformed(ActionEvent actionEvent) {
				AboutDialog about= new AboutDialog(GUIManager.getManager().getFrame());
				about.showDialog();
			}
		};
	}

	/** perhaps this might change in the future so both oboedit and phenote will
	 *  use the same default toolbars with save, exit, etc. buttons
	 */
	@Override
	protected Collection<JToolBar> getDefaultToolBars() {
		Collection<JToolBar> toolbars = new ArrayList<JToolBar>();
		return toolbars;
	}

	@Override
	protected String getAppID() {
		/*
		 * This is used as the DB source of the annotations in the GAF files,
		 * so it can not be changed without changing any previous GAF files
		 * that have been generated and conferring with the curators first.
		 */
		return PAINT.getAppName() + Preferences.inst().getVersion();
	}

	@Override
	protected String getAppName() {
		return PAINT.getAppName();
	}

	/** in this directory obo/idw expects to find a file named perspectives.xml
      which then points to idw files in this same directory 
      obo can get this from a jar but doesnt work from webstart jar???
      FileNotFoundException printed to stdout if not found and no perspectives
      come up */
	@Override
	protected String getPerspectiveResourceDir() { // not using org
		return getPrefsDir() + File.separator + "perspectives"; // need to add this
	}

	@Override
	protected String getDefaultPerspectiveResourcePath() {
		if (getPerspectiveResourceDir() != null)
			return getPerspectiveResourceDir() + File.separator + getAppName() + ".idw";
		else
			return null;
	}

	@Override
	public File getPrefsDir() {
		// .paint/layout?
            Path currentRelativePath = Paths.get("");
            String s = currentRelativePath.toAbsolutePath().toString();         
//		File prefsDir = new File(s + File.separator + "config");            
		return new File(s);
	}

	@Override
	protected Collection<? extends JMenuItem> getDefaultMenus() {
		return PaintDefaultComponentsFactory.createDefaultMenus();
	}

	@Override
	protected Collection<GUITask> getDefaultTasks() {
		ScreenLockTask screenLockTask = new ScreenLockTask(
				GUIManager.getManager().getScreenLockQueue(), 
				GUIManager.getManager().getFrame(), 
				getUseModalProgressMonitors());
		return CollectionUtil.list((GUITask) screenLockTask);
	}

	@Override
	protected void installSystemListeners() {
		GUIManager.addVetoableShutdownListener(DirtyIndicator.inst());
	}

	@Override
	protected JFrame createFrame() {
//		JFrame out = new PaintFrame(getAppID());
                // For now use current date so that users know that they are using the correct build
                JFrame out = new PaintFrame("Paint Version " + Preferences.inst().getPAINTversion());
		return out;
	}

	@Override
	protected LayoutDriver createLayoutDriver() {
		final LayoutDriver driver = super.createLayoutDriver();
		if (driver instanceof IDWDriver) {
			((IDWDriver)driver).setCustomTheme(new PaintDockingTheme());
		}
		driver.setSaveLayoutOnExit(false);
		return driver;
	}

	public boolean getUseModalProgressMonitors() {
		return !System.getProperty("useModalProgressMonitors", "true").equals("false");
	}

	@Override
	protected void configureLogging() {
		//		Check whether the root logger has an appender; there does not appear
		//		to be a more direct way to check whether Log4J has already been 
		//		initialized.  Note that the root logger's appender can always be
		//		set to a NullAppender, so this does not restrict the utility of
		//		the logging in any way.
		Logger rl = LogManager.getRootLogger();
		Enumeration<?> appenders = rl.getAllAppenders();
		if (appenders==null || !appenders.hasMoreElements()) {
			System.out.println("Log4J configuration is empty, using default configuration settings");
			BasicConfigurator.configure(); 
			rl.setLevel(Level.DEBUG);
			LOG = LogManager.getLogger(PAINT.class);
		}

		//		hmm dont know about this code, tries to write out filepath of log file to
		//		stdout, but assumes its configured with MAIN which may not be the case.
		//		maybe should just get whatever appender it can get rather than just MAIN
		//		maybe do getAllAppenders and do this for each one? in any case taking out
		//		misleading error messages
		org.apache.log4j.FileAppender a = (org.apache.log4j.FileAppender)rl.getAppender("MAIN");
		if (a != null) 
			System.out.println("log file: "+a.getFile());

		if (a != null && a.getFile() != null) {
			File f = new File(a.getFile()); //"phenote_log4j.log");
			System.out.println("path of file "+f.getPath()+" absolute "+f.getAbsolutePath()
					+" canWrite "+f.canWrite());
		}
		//		writes error events to log - this should probably be refactored to just use
		//		log4j straight up, and have log4j appender for an error window
		//		error manager used to go to term info but no longer
		//		ErrorManager.inst().addErrorListener(new LogErrorListener());
		//LOG.info("Loading Paint version " + Preferences.inst().getVersion().toString());
                LOG.info("Loading Paint version " + Preferences.inst().getPAINTversion());
	}

	@Override
	protected void configureUI() {
		//		// overriding the odd colors in BBOP framework among other things
		// try {
		final String lookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
		if (lookAndFeelClassName.equals("apple.laf.AquaLookAndFeel") ||
				lookAndFeelClassName.equals("com.apple.laf.AquaLookAndFeel")) {
			// We are running on Mac OS X - use the Quaqua look and feel
			System.setProperty("apple.laf.useScreenMenuBar", "true");
                        return;
		}
                // For Nimbus
//                try {
//                    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//                        if ("Nimbus".equals(info.getName())) {
//                            UIManager.setLookAndFeel(info.getClassName());
//                            break;
//                        }
//                    }
//                } catch (Exception e) {
//                    // If Nimbus is not available, you can set the GUI to another look and feel.
//                }
                
                
	}

}
