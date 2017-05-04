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

package org.paint.gui.evidence;

import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import com.sri.panther.paintCommon.Constant;
import edu.usc.ksom.pm.panther.paintCommon.Evidence;
import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
import com.sri.panther.paintCommon.util.QualifierDif;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Scrollable;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;
import org.paint.config.Preferences;
import org.paint.datamodel.GeneNode;
import org.paint.go.GO_Util;
import org.paint.gui.AbstractPaintGUIComponent;
import org.paint.gui.AspectSelector;
import org.paint.gui.event.AnnotationChangeEvent;
import org.paint.gui.event.AnnotationChangeListener;
import org.paint.gui.event.CommentChangeEvent;
import org.paint.gui.event.CommentChangeListener;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.FamilyChangeEvent;
import org.paint.gui.event.FamilyChangeListener;
import org.paint.main.PaintManager;
import org.paint.util.GeneNodeUtil;
import org.paint.util.RenderUtil;

public class EvidencePanel  extends AbstractPaintGUIComponent implements
        CommentChangeListener,
        AnnotationChangeListener, 
        FamilyChangeListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static EvidencePanel singleton;

	private JTextArea comment_panel;
	private JTextArea warning_panel;
        
        private HashMap<String, JTextArea> aspectPanelLookup = new HashMap<String, JTextArea>();
//	private LoggingPanel mf_panel;
//	private LoggingPanel cc_panel;
//	private LoggingPanel bp_panel;
	private JTextArea prune_panel;

	private static Logger logger = Logger.getLogger(EvidencePanel.class);

	/*
	 * Separated into sections by aspect ?
	 * Include dates?
	 * Section for References
	 * 	http://en.wikipedia.org/wiki/SKI_protein
	 * 	PMID: 19114989 
	 * Phylogeny
	 * 	Two main clades, SKOR and SKI/SKIL, plus an outlier from Tetrahymena which aligns poorly, so I have not annotated AN0.

	 * Propagate GO:0004647 "phosphoserine phosphatase activity" to AN1 and GO:0016791 "phosphatase activity" to AN0.
	 * -Propagate "cytoplasm" to AN1 based on 3 annotations.
	 * -Propagate "chloroplast" to plants/chlamy.
	 * 
	 * Need to also log all NOTs
	 * 
	 * Challenge mechanism
	 * 	
	 */
	public EvidencePanel() {
		super("evidence:evidence");

                
		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(Preferences.inst().getBackgroundColor());

		JPanel pane = new JPanel();
		pane.setOpaque(true);
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

                aspectPanelLookup.put(AspectSelector.aspects.get(AspectSelector.Aspect.BIOLOGICAL_PROCESS.toString()), initAspectPanel(AspectSelector.Aspect.BIOLOGICAL_PROCESS.toString()));
                aspectPanelLookup.put(AspectSelector.aspects.get(AspectSelector.Aspect.MOLECULAR_FUNCTION.toString()), initAspectPanel(AspectSelector.Aspect.MOLECULAR_FUNCTION.toString()));
                aspectPanelLookup.put(AspectSelector.aspects.get(AspectSelector.Aspect.CELLULAR_COMPONENT.toString()), initAspectPanel(AspectSelector.Aspect.CELLULAR_COMPONENT.toString()));

		for (JTextArea textArea: aspectPanelLookup.values()) {
                    pane.add(textArea);
                }
                
                initPrunePane();
		pane.add(prune_panel);

		initCommentPane();
		pane.add(comment_panel);

		initWarningPane();
		pane.add(warning_panel);

		JScrollPane scrollPane = new JScrollPane(new OnlyVerticalScrollPanel(pane));
		add(scrollPane, BorderLayout.CENTER);
		super.setTitle(GO_Util.inst().getPaintEvidenceAcc());
		EventManager.inst().registerCommentChangeListener(this);
		//EventManager.inst().registerTermListener(this);
		EventManager.inst().registerGeneAnnotationChangeListener(this);
                EventManager.inst().registerFamilyListener(this);
	}

	public static EvidencePanel inst() {
		if (singleton == null) 
			singleton = new EvidencePanel();
		return (EvidencePanel) singleton;
	}

//	public String getEvidenceText() {
//		StringBuffer buf = new StringBuffer();
//		buf.append("# " + Aspect.MOLECULAR_FUNCTION + '\n');
//		buf.append(mf_panel.getEntry() + '\n');
//		buf.append("# " + Aspect.CELLULAR_COMPONENT + '\n');
//		buf.append(cc_panel.getEntry() + '\n');
//		buf.append("# " + Aspect.BIOLOGICAL_PROCESS + '\n');
//		buf.append(bp_panel.getEntry() + '\n');
//		if (prune_panel.getEntry().length() > 0) {
//			buf.append("# Pruned from tree\n");
//			buf.append(prune_panel.getEntry() + '\n');
//		}
//		buf.append("# Notes\n");
//		buf.append(comment_panel.getText() + '\n');
//		return buf.toString();
//	}

	public String getWarnings() {
		return warning_panel.getText();
	}

//	public void setComment(String text) {
//		warning_panel.setVisible(false);
//		warning_panel.setText("");
//		mf_panel.clearLog();
//		cc_panel.clearLog();
//		bp_panel.clearLog();
//		prune_panel.clearLog();
//		String comment = "";
//		if (text != null) {
//			/* 
//			 * The following is a patch for notes that were missing the trailing crlf between sections
//			 */
//			int index;
//			index = text.indexOf("# " + Aspect.CELLULAR_COMPONENT);
//			if (index > 0 && text.charAt(index - 1) != '\n')
//				text = text.replace("# " + Aspect.CELLULAR_COMPONENT, "\n# " + Aspect.CELLULAR_COMPONENT);
//			index = text.indexOf("# " + Aspect.BIOLOGICAL_PROCESS);
//			if (index > 0 && text.charAt(index - 1) != '\n')
//				text = text.replace("# " + Aspect.BIOLOGICAL_PROCESS, "\n# " + Aspect.BIOLOGICAL_PROCESS);
//			index = text.indexOf("# Pruned from tree");
//			if (index > 0 && text.charAt(index - 1) != '\n')
//				text = text.replace("# Pruned from tree", "\n# Pruned from tree");
//			index = text.indexOf("# Notes");
//			if (index > 0 && text.charAt(index - 1) != '\n')
//				text = text.replace("# Notes", "\n# Notes");
//
//			String [] lines = text.split("\\n");
//			StringBuffer notes = new StringBuffer();
//			boolean logging_on = true;
//			for (String line : lines) {
//				if (line.startsWith("#")) {
//					logging_on = this.getLogPane(line.substring(1)) == null;
//				}
//				if (logging_on && !line.startsWith("# Notes")) {
//					notes.append(line + '\n');
//				}
//			}
//			comment = notes.toString();
//		}
//		new CommentThread(comment_panel, comment).start();
//	}

//	public void addWarning(String text) {
//		String current = warning_panel.getText();
//		if (current == null || current.length() == 0) {
//			warning_panel.setText(text);
//			warning_panel.setVisible(true);
//		} else {
//			warning_panel.setText(warning_panel.getText() + '\n' + text);
//		}
//	}

//	public void displayEntry(LogEntry entry) {
//		LoggingPanel pane = entry.getLogPane();
//		if (pane != null)
//			pane.logEntry(entry.getDescription());
//		else
//			logger.debug("Couldn't locate a log panel for " + entry.getAction() + '.');
//	}
//
//	public void clearEntry(LogEntry entry) {
//		LoggingPanel pane = entry.getLogPane();
//		if (pane == null)
//			logger.debug("Couldn't locate a log panel for " + entry.getAction() + '.');
//		else
//			pane.logErase(entry.getDescription());
//	}
        
        private void initPrunePane() {
		prune_panel = new JTextArea("");
                prune_panel.setEditable(false);
              
		prune_panel.setOpaque(true);
		prune_panel.setLineWrap(true);
		prune_panel.setWrapStyleWord(true);
		Border titled = loggerBorder(Preferences.inst().getBackgroundColor(), "Pruned");
		prune_panel.setBorder(titled);
        }
        
	private void initCommentPane() {
		comment_panel = new JTextArea("");
                comment_panel.setEditable(false);
              
		comment_panel.setOpaque(true);
		comment_panel.setLineWrap(true);
		comment_panel.setWrapStyleWord(true);
		Border titled = loggerBorder(Preferences.inst().getBackgroundColor(), "Notes");
		comment_panel.setBorder(titled);
//		comment_panel.getDocument().addDocumentListener(new DocumentListener() {
//			@Override
//			public void removeUpdate(DocumentEvent e) {
//				DirtyIndicator.inst().dirtyGenes(true);
//			}
//
//			@Override
//			public void insertUpdate(DocumentEvent e) {
//				DirtyIndicator.inst().dirtyGenes(true);
//			}
//
//			@Override
//			public void changedUpdate(DocumentEvent e) {
//				DirtyIndicator.inst().dirtyGenes(true);
//			}
//		});
	}

	private void initWarningPane() {
		warning_panel = new JTextArea();
		warning_panel.setOpaque(true);
		warning_panel.setEditable(false);
		warning_panel.setLineWrap(true);
		warning_panel.setWrapStyleWord(true);
		Border titled = loggerBorder(Color.RED.darker(), "WARNINGS");
		warning_panel.setBorder(titled);
		//warning_panel.setVisible(false);
	}
        
        private JTextArea initAspectPanel(String category) {
		JTextArea aspect_panel = new JTextArea();
		aspect_panel.setOpaque(true);
		aspect_panel.setEditable(false);
		aspect_panel.setLineWrap(true);
		aspect_panel.setWrapStyleWord(true);            
		Color border_color = RenderUtil.getAspectColor(category);
                Border border = loggerBorder(border_color, category);
                aspect_panel.setBorder(border);
		return aspect_panel;     
        }

//	private LoggingPanel initLogPane(int aspect, String log_category) {
//		Color border_color;
//		if (aspect >= 0)
//			border_color = Preferences.inst().getAspectColor(aspect);
//		else
//			border_color = Color.LIGHT_GRAY;
//		Border border = loggerBorder(border_color, log_category);
//		return (new LoggingPanel(border));
//	}

	private Border loggerBorder(Color border_color, String category) {
		Border raisedbevel = BorderFactory.createBevelBorder(BevelBorder.RAISED, border_color, border_color.darker());
		Border loweredbevel = BorderFactory.createBevelBorder(BevelBorder.LOWERED, border_color, border_color.darker());
		Border compound = BorderFactory.createCompoundBorder(raisedbevel, loweredbevel);
		Border titled = BorderFactory.createTitledBorder(
				compound, category,
				TitledBorder.LEFT,
				TitledBorder.TOP);		
		return titled;
	}

    @Override
    public void handleAnnotationChangeEvent(AnnotationChangeEvent event) {
        handleAnnotationChange();
    }
        


    public void handleAnnotationChange() {
        PaintManager pm = PaintManager.inst();
        GOTermHelper gth = pm.goTermHelper();
        ArrayList<Annotation> annotList = pm.getAnnotatedList();
        StringBuffer prunedBuf = new StringBuffer();
        StringBuffer warningBuf = new StringBuffer();
        HashMap<String, StringBuffer> aspectLookup = new HashMap<String, StringBuffer>();
        for (Annotation a: annotList) {
            GOTerm term = gth.getTerm(a.getGoTerm());
            String aspect = term.getAspect();
            StringBuffer sb = aspectLookup.get(aspect);
            if (null == sb) {
                sb = new StringBuffer();
                aspectLookup.put(aspect, sb);
            }
            if (0 != sb.length()) {
                sb.append(Constant.STR_NEWLINE);
            }
            String publicId = a.getAnnotationDetail().getAnnotatedNode().getStaticInfo().getPublicId();

            GeneNode gn = pm.getGeneByPTNId(publicId);
            if (true == Evidence.CODE_IBD.equals(a.getEvidence().getEvidenceCode()) && false == GeneNodeUtil.isTermValidForNode(gn, term.getAcc())) {
                if (0 != warningBuf.length()) {
                    warningBuf.append(Constant.STR_NEWLINE);
                }
                warningBuf.append(gn.getNodeLabel() + publicId + " annotated to term " + term.getName() + Constant.STR_BRACKET_ROUND_OPEN + term.getAcc() + Constant.STR_BRACKET_ROUND_CLOSE + " violates taxonomy constraint");
            }            
            sb.append(gn.getNodeLabel() + publicId + Constant.STR_TAB + a.getEvidence().getEvidenceCode() + Constant.STR_TAB + term.getName() + Constant.STR_BRACKET_ROUND_OPEN + term.getAcc() + Constant.STR_BRACKET_ROUND_CLOSE);
            if (true == QualifierDif.containsNegative(a.getQualifierSet())) {
                sb.append(Constant.STR_TAB);
                sb.append(Qualifier.QUALIFIER_NOT);
            }
        }
        
        
        // First clear what we have
        for (JTextArea ta: aspectPanelLookup.values()) {
            ta.setText(Constant.STR_EMPTY);
        }
        
        for (String aspect: aspectLookup.keySet()) {
            JTextArea textArea = aspectPanelLookup.get(aspect);
            textArea.setText(aspectLookup.get(aspect).toString());
        }
        
    
        warning_panel.setText(warningBuf.toString());
        
        
        
        ArrayList<Node> prunedList = pm.getPrunedList();
        for (Node n: prunedList) {
            if (0 != prunedBuf.length()) {
                prunedBuf.append(Constant.STR_NEWLINE);
            }
            String publicId = n.getStaticInfo().getPublicId();
            GeneNode gn = pm.getGeneByPTNId(publicId);            
            prunedBuf.append(gn.getNodeLabel() + publicId);
        }
        prune_panel.setText(prunedBuf.toString());
    }

    @Override
    public void handleCommentChangeEvent(CommentChangeEvent event) {
        handleCommentChange();
    }
    
    private void handleCommentChange() {
        PaintManager pm = PaintManager.inst();
        String curComment = pm.getComment();
        if (null == curComment) {
            curComment = Constant.STR_EMPTY;
        }
        comment_panel.setText(curComment);
    }

    @Override
    public void newFamilyData(FamilyChangeEvent e) {
        handleAnnotationChange();
        handleCommentChange();
    }

//	protected LoggingPanel getLogPane(String line) {
//		LoggingPanel current_log = null;
//		if (line != null) {
//			if (line.contains(Aspect.MOLECULAR_FUNCTION.toString())) {
//				current_log = mf_panel;
//			} else if (line.contains(Aspect.CELLULAR_COMPONENT.toString())) {
//				current_log = cc_panel;
//			} else if (line.contains(Aspect.BIOLOGICAL_PROCESS.toString())) {
//				current_log = bp_panel;
//			} else if (line.toLowerCase().contains(LogEntry.Action.PRUNE.toString().toLowerCase())){
//				current_log = prune_panel;
//			} else if (!line.contains("Notes")) {
//				logger.debug("Couldn't find panel for logging " + line);
//			}
//		}
//		return current_log;
//	}

//	protected LoggingPanel getPrunePanel() {
//		return prune_panel;
//	}

	/**
	 * A panel that, when placed in a {@link JScrollPane}, only scrolls vertically and resizes horizontally as needed.
	 */
	@SuppressWarnings("serial")
	private class OnlyVerticalScrollPanel extends JPanel implements Scrollable
	{
		public OnlyVerticalScrollPanel()
		{
			this(new GridLayout(0, 1));
		}

		public OnlyVerticalScrollPanel(LayoutManager lm)
		{
			super(lm);
		}

		public OnlyVerticalScrollPanel(Component comp)
		{
			this();
			add(comp);
		}

		@Override
		public Dimension getPreferredScrollableViewportSize()
		{
			return(getPreferredSize());
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect,
				int orientation, int direction)
		{
			return(10);
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect,
				int orientation, int direction)
		{
			// might want to change this to be governed by amount of text
			return(100);
		}

		@Override
		public boolean getScrollableTracksViewportWidth()
		{
			return(true);
		}

		@Override
		public boolean getScrollableTracksViewportHeight()
		{
			return(false);
		}

	}

//	private class CommentThread extends Thread {
//		private JTextArea text_area;
//		private String comment;
//
//		public CommentThread (JTextArea text_area, String comment) {
//			this.text_area = text_area;
//			this.comment = comment;
//		}
//
//		public void run() {
//			if (SwingUtilities.isEventDispatchThread()) {
//				text_area.setText(comment);
//			} else {
//				try {
//					SwingUtilities.invokeAndWait(new Runnable() {
//						@Override
//						public void run() {
//							text_area.setText(comment);
//						}
//					});
//				} catch (InterruptedException ex) {
//					logger.error(ex.toString());
//				} catch (InvocationTargetException ex) {
//					logger.error(ex.toString());
//				}
//			}
//		}
//	};
}
