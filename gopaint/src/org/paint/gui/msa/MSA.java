/**
 *  Copyright 2022 University Of Southern California
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.paint.gui.msa;

import edu.usc.ksom.pm.panther.paintCommon.Domain;
import edu.usc.ksom.pm.panther.paintCommon.KeyResidue;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.paint.config.Preferences;
import org.paint.datamodel.GeneNode;
import org.paint.main.PaintManager;
import org.paint.util.DuplicationColor;

/**
 * Class declaration
 *
 *
 * @author
 * @version %I%, %G%
 */
public class MSA {

	public enum MSA_DISPLAY {
//		SIGNIFICANT,
		//		SF_CONSERVED,
                ENTIRE_ALIGNMENT,
                TRIMMED,
                DOMAIN,
                DOMAIN_TRIMMED,
                WEIGHTED,
		NONE,
                KEY_RESIDUE;

		public String toString() {
			return super.toString().toLowerCase();
		}
	}
        
        public static final char CHAR_DOT = '.';
        public static final char CHAR_DASH = '-';        

	private boolean full_length;
        private HashMap<String, HashMap<String, ArrayList<Domain>>> domainLookup;
        private ArrayList<KeyResidue> keyResidueList;
	private MSA_DISPLAY  displayType = MSA_DISPLAY.NONE;
	private boolean have_weights;
        private boolean haveSeq;
        private boolean haveDomain;
        private boolean haveKeyResidue;
        private MSAParser msaParser = null;
        


	/* by row and then by column */
	private Hashtable<GeneNode, Color[]> nodeToColor = new Hashtable<GeneNode, Color[]>();

	private static final String msaFont = Font.MONOSPACED;         // Has to be fixed width font

	private static Logger log = Logger.getLogger(MSA.class);

	private boolean colors_initialized = false;
	private Font          font;
	private AminoAcidStats[] aminoAcidStats;

	private int selectedCol = -1;               // Selected column

	// Refers to sequence range that is currently visible (x-coord is the start position and y-coord is the end position
	private int [] seq_range;

	private static final int START_BASE = 0;
	private static final int END_BASE = 1;
        

        private HashMap<String, Color> domainToColorLookup;
        private HashMap<String, Color> lightDomainColorLookup;
        
        private int DEFAULT_DOMAIN_HEIGHT = 5;
        
        private HashSet<String> domainSet;
        private int maxDomainRows = 0;   // Determine maximum number of domain lines for display.        

        private static final Color selectedColor = Color.PINK;
        private static final Color COLOR_GREEN = new Color(50, 168, 82);
        private static final Color COLOR_MAGENTA = new Color(255,0,255);
        private static final HashMap<KeyResidue.ResidueType, Color> RESIDUE_LOOKUP = initResidueLookup();
        
        private static HashMap<KeyResidue.ResidueType, Color> initResidueLookup() {
            HashMap<KeyResidue.ResidueType, Color> rtnLookup = new HashMap<KeyResidue.ResidueType, Color>();
            rtnLookup.put(KeyResidue.ResidueType.ACT_SITE, Color.BLACK);
            rtnLookup.put(KeyResidue.ResidueType.BINDING, Color.RED);
            rtnLookup.put(KeyResidue.ResidueType.METAL, Color.ORANGE);
            return rtnLookup;
        }
	/**
	 * Constructor declaration
	 *
	 *
	 * @param msaInfo
	 * @param gridInfo
	 *
	 * @see
	 */
	public MSA(String[] msaInfo, String[] wtsInfo, HashMap<String, HashMap<String, ArrayList<Domain>>> domainLookup, ArrayList<KeyResidue> keyResidueList) {
		msaParser = new MSAParser();
                if (null != msaInfo && 0 != msaInfo.length) {
                    haveSeq = true;
                    msaParser.parseSeqs(msaInfo);
                    displayType = MSA_DISPLAY.ENTIRE_ALIGNMENT;
                }
                this.domainLookup = domainLookup;
                setupDomainData();
                this.keyResidueList = keyResidueList;
                setupKeyResidueList();
		
		have_weights = false;//parser.parseWts(wtsInfo);

		full_length = true;


		Font f = Preferences.inst().getFont();
		setFont(new Font(Font.MONOSPACED, f.getStyle(), f.getSize()));
		setColors();
	}
        
//        public void handleDomainLookup() {
//            this.domainLookup = domainLookup;
//            setupDomainData(domainLookup);
//        }
        
    protected void setupKeyResidueList() {
        if (null == keyResidueList) {
            haveKeyResidue = false;
            return;
        }
        PaintManager manager = PaintManager.inst();
        List<GeneNode> nodes = manager.getTree().getAllNodes();
        for (GeneNode gn : nodes) {
            String protId = gn.getSeqId();
            for (KeyResidue kr : keyResidueList) {
                String protein = kr.getProtein();
                if (null != protId && protId.equals(protein)) {
                    ArrayList<KeyResidue> list = gn.getKeyResidueList();
                    if (null == list) {
                        list = new ArrayList<KeyResidue>();
                        gn.setKeyResidueList(list);
                        haveKeyResidue = true;
                    }
                    list.add(kr);
                }
            }
        }
    }
        
          
        
        protected void setupDomainData() {
            if (null == domainLookup) {
                haveDomain = false;
                return;
            }
            PaintManager manager = PaintManager.inst();
            // Handle case where domain ranges can overlap. If we have this condition, we want to create a separate row for each overlapping domain
            domainToColorLookup = new HashMap<String, Color>();
            lightDomainColorLookup = new HashMap<String, Color>();
            
            domainSet = new HashSet<String>();

            for (Entry<String, HashMap<String, ArrayList<Domain>>> entry : domainLookup.entrySet()) {
                HashMap<String, ArrayList<Domain>> geneDomainLookup = entry.getValue();
                if (null == geneDomainLookup) {
                    continue;
                }
                GeneNode gn = manager.getGeneByPaintId(entry.getKey());
                if (null == gn) {
                    continue;
                }
//                if ("PTN000644027".equals(gn.getPersistantNodeID())) {
//                    System.out.println("Here");
//                }
                gn.setDomainLookup(geneDomainLookup);
                domainSet.addAll(geneDomainLookup.keySet());
                ArrayList<Domain> domainsForGene = new ArrayList<Domain>();
                for (ArrayList<Domain> dList: geneDomainLookup.values()) {
                    domainsForGene.addAll(dList);
                }

                ArrayList<ArrayList<Domain>> domainRows = groupWithoutOverlap(domainsForGene);
                gn.setDomainRows(domainRows);
            }
            
            
            
            List<Color> colors = DuplicationColor.inst().getPastelColors();
            int i = 0;
            for (String domainStr: domainSet) {
                if (i == 0) {
                    domainToColorLookup.put(domainStr, DuplicationColor.LIGHT_STEEL_BLUE);
                    lightDomainColorLookup.put(domainStr, DuplicationColor.LIGHT_STEEL_BLUE);
                    i++;
                    continue;
                }
                if (i >= colors.size()) {
                    int index = i / colors.size();
                    Color atIndex = DuplicationColor.inst().getDarkerColor(colors.get(index));
                    domainToColorLookup.put(domainStr, DuplicationColor.inst().getDarkerColor(atIndex));
                    lightDomainColorLookup.put(domainStr, atIndex);                        
                    
                }
                else {
                    domainToColorLookup.put(domainStr, DuplicationColor.inst().getDarkerColor(colors.get(i)));
                    lightDomainColorLookup.put(domainStr, colors.get(i));
                }
                i++;
            }
        }

     
        protected static boolean domainsOverlap(ArrayList<Domain> list1, ArrayList<Domain> list2) {
            for (Domain l1d: list1) {
                for (Domain l2d: list2) {
                    if (true == overlap(l1d.getStart(), l1d.getEnd(), l2d.getStart(), l2d.getEnd())) {
                        return true;
                    }
                }
            }
            return false;
        }
        
//        protected boolean overlap (int a, int b, int c, int d) {
//            if (a > d || b < c) {
//                return false;
//            } 
//            return true;
//        }
        
        
        

	protected void reorderRows(List<GeneNode> node_list) {
		// keep a local copy of the currently visible nodes (i.e. rows)
		//colors_initialized = false;	// Reordering should not change colors, but might?
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param f
	 *
	 * @see
	 */
	protected void setFont(Font f){
		font = f;
	}

	protected boolean isFullLength() {
		return full_length;
	}

    protected void setFullLength(boolean full) {
        full_length = full;
        //colors_initialized = false;
    }

	protected void updateColors() {
		colors_initialized = false;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param g
	 *
	 * @see
	 */
	public void draw(Graphics g, Rectangle viewport){
		if (g == null) {
			return;
		}
		setColors();
		if (!colors_initialized) {
			g.drawString("No display specified", 100, 100);
			return;
		}
                if (MSA_DISPLAY.DOMAIN == this.displayType) {
                    displayDomain(g, viewport);
                    return;
                }
                if (MSA_DISPLAY.DOMAIN_TRIMMED == this.displayType) {
                    displayDomainTrimmed(g, viewport);
                    return;
                }              
		displayPid(g, viewport);
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param g
	 *
	 * @return
	 *
	 * @see
	 */
	public Rectangle getGridSize(Graphics g){
		if (null == font){
			return null;
		}
                PaintManager pm = PaintManager.inst();
		Rectangle   r = new Rectangle();
		int row_height = pm.getRowHeight();
		int colWidth = getColumnWidth(g);
		if (row_height == 0) {
			log.warn("Row height is not set");
			row_height = 16;
		}
		r.height = row_height + pm.getTopMargin();
		r.width = 0;
		List<GeneNode> contents = PaintManager.inst().getTree().getTerminusNodes();
		if (contents != null){
			r.height += contents.size() * row_height;
			r.width += (msaParser.getSeqLength(full_length)) * colWidth;
		}
		return r;
	}
        

	/**
	 * Method declaration
	 *
	 *
	 * @param g
	 *
	 * @see
	 */
    private void displayPid(Graphics g, Rectangle viewport) {
        List<GeneNode> contents = PaintManager.inst().getTree().getTerminusNodes();
        if (contents == null || contents.size() == 0) {
            return;
        }

        g.setFont(font);

        int row_height = PaintManager.inst().getRowHeight();
        int curHeight = PaintManager.inst().getTopMargin();

        int charWidth = this.getColumnWidth(g);

        int headerHeight = PaintManager.inst().getTopMargin();
        int topInset = (headerHeight - font.getSize()) / 2;

        int header_y = viewport.y + headerHeight - topInset - 1;
        // Display the header at the top of the viewport
        if (Color.white != g.getColor()) {
            g.setColor(Color.white);
        }
        g.fillRect(viewport.x, viewport.y, viewport.width, headerHeight - topInset - 1);

        g.setColor(Color.black);

        seq_range = setSeqRange(viewport, charWidth, msaParser.getSeqLength(full_length));
        int seq_x_position = viewport.x; // + seq_range[0] * charWidth + 1;

        int column_x = seq_x_position;
        char[] ruler = msaParser.getRuler(full_length);
        for (int column = seq_range[START_BASE]; column < seq_range[END_BASE]; column++) {
            g.drawChars(ruler, column, 1, column_x, header_y);
            column_x += charWidth;
        }

        for (int row = 0; row < contents.size(); row++) {
            GeneNode node = contents.get(row);
            if (!node.isTerminus()) {
                continue;
            }

            // Display this row if it is within the viewport range, but otherwise continue
            if ((viewport.y > (curHeight + row_height)) || ((viewport.y + viewport.height) < curHeight)) {
                curHeight += row_height;
                continue;
            }
            String seq = null;
            boolean hmm = false;
            if (true == full_length) {
                seq = node.getSequence();
            } else {
                seq = node.getHMMSeq();
                hmm = true;
            }
            if ((null == seq) || (0 == seq.length())) {
                curHeight += row_height;
                continue;
            }

            Color nodeColors[] = nodeToColor.get(node);
            if (null == nodeColors) {
                curHeight += row_height;
                continue;
            }

            if (node.isPruned()) {
                curHeight += row_height;
                continue;
            }

            Font f;
            Font bold = new Font(msaFont, Font.BOLD, font.getSize());
            ArrayList<KeyResidue> krList = node.getKeyResidueList();
            if (node.isSelected() || (MSA_DISPLAY.KEY_RESIDUE == this.displayType && null != krList && 0 != krList.size())) {
                f = bold;
            } else {
                f = font;
            }

            column_x = seq_x_position;
            int seq_y = curHeight + row_height - topInset - 1;
            char[] seq_chars = seq.toCharArray();
            for (int column = seq_range[START_BASE]; column < seq_range[END_BASE]; column++) {
                Color color = null;
                if (false == hmm) {
                    color = nodeColors[column];
                } else {
                    color = nodeColors[msaParser.getMatchPosIndex().get(column)];
                }
                if (column == selectedCol) {
                    color = selectedColor;
                }
//                if (MSA_DISPLAY.KEY_RESIDUE == this.displayType) {
//                    color = color.brighter();
//                }
                g.setColor(color);
                g.fillRect(column_x, curHeight, charWidth, row_height);
            
                if (MSA_DISPLAY.KEY_RESIDUE == this.displayType) {
//                    if ("P54947".equals(node.getSeqId()) && column > 300 && column < 315) {
//                        System.out.println("Here");
//                    }
                    g.setColor(Color.LIGHT_GRAY);
                    if (node.isSelected()) {
                        g.setColor(Color.GRAY);
                    }
                    
                    if (null != krList && 0 != krList.size()) {
                        HashSet<Color> multiples = new HashSet<Color>();
                        for (KeyResidue kr : krList) {
                            if (column == (kr.getAlignPos() - 1)) {
                                g.setColor(RESIDUE_LOOKUP.get(kr.getResidueType()));
                                multiples.add(g.getColor());
                            }
                        }
                        if (multiples.size() > 1) {
                            g.setColor(COLOR_MAGENTA);
                        }
                    }
                } else {
                    g.setColor(Color.BLACK);
                }
                g.setFont(f);
                g.drawChars(seq_chars, column, 1, column_x, seq_y);
                column_x += charWidth;
            }
            curHeight += row_height;
        }
    }
        
    private void displayDomain(Graphics g, Rectangle viewport) {
        List<GeneNode> contents = PaintManager.inst().getTree().getTerminusNodes();
        if (contents == null || contents.isEmpty()) {
            return;
        }

        g.setFont(font);

        int row_height = PaintManager.inst().getRowHeight();
        int curHeight = PaintManager.inst().getTopMargin();
        double domainHeight = DEFAULT_DOMAIN_HEIGHT;
        if (0 != maxDomainRows) {
            domainHeight = row_height / maxDomainRows;
            if (1 == maxDomainRows || domainHeight > DEFAULT_DOMAIN_HEIGHT) {
                domainHeight = DEFAULT_DOMAIN_HEIGHT;
            }
        }
        int domainHeightInt = new Double(domainHeight).intValue();

        int charWidth = this.getColumnWidth(g);

        int headerHeight = PaintManager.inst().getTopMargin();
        int topInset = (headerHeight - font.getSize()) / 2;

        int header_y = viewport.y + headerHeight - topInset - 1;
        // Display the header at the top of the viewport
        if (Color.white != g.getColor()) {
            g.setColor(Color.white);
        }
        g.fillRect(viewport.x, viewport.y, viewport.width, headerHeight - topInset - 1);

        g.setColor(Color.black);

        seq_range = setSeqRange(viewport, charWidth, aminoAcidStats.length);
        int seq_x_position = viewport.x; // + seq_range[0] * charWidth + 1;

        int column_x = seq_x_position;
        char[] ruler = msaParser.getRuler(full_length);
        for (int column = seq_range[START_BASE]; column < seq_range[END_BASE]; column++) {
            g.drawChars(ruler, column, 1, column_x, header_y);
            column_x += charWidth;
        }

        for (int row = 0; row < contents.size(); row++) {
            GeneNode node = contents.get(row);
            if (!node.isTerminus()) {
                continue;
            }

            // Display this row if it is within the viewport range, but otherwise continue
            if ((viewport.y > (curHeight + row_height)) || ((viewport.y + viewport.height) < curHeight)) {
                curHeight += row_height;
                continue;
            }

            String seq = (full_length ? node.getSequence() : node.getHMMSeq());
            if ((null == seq) || (0 == seq.length())) {
                curHeight += row_height;
                continue;
            }

            if (node.isPruned()) {
                curHeight += row_height;
                continue;
            }

//            // Color everything white first and also draw the selected col
//            for (int column = seq_range[START_BASE]; column < seq_range[END_BASE]; column++) {
//                Color color = Color.WHITE;
//                if (column == selectedCol) {
//                    g.setColor(selectedColor);
//                    g.fillRect(column_x, curHeight, charWidth, row_height);
//                } else {
//                    g.setColor(color);
//                    g.fillRect(column_x, curHeight, charWidth, row_height);
//                }
//                g.setColor(Color.black);
//                column_x += charWidth;
//            }

            ArrayList<ArrayList<Domain>> domainRows = node.getDomainRows();
            HashMap<String, ArrayList<Domain>> domainLookup = node.getDomainLookup();
            if (null == domainRows || null == domainLookup || 0 == domainRows.size() || 0 == domainLookup.size()) {
                curHeight += row_height;
                continue;
            }
//                        if (1 < domainRows.size()) {
//                            System.out.println("here");
//                        }
            for (int i = 0; i < domainRows.size(); i++) {
                ArrayList<Domain> curRow = domainRows.get(i);
                double domainPosStart = (domainHeight * (i));
                int height = new Double(domainPosStart).intValue() + curHeight;
                for (Domain d : curRow) {

                    g.setColor(domainToColorLookup.get(d.getHmmAcc()));
                    int startPos = getPosition(seq, d.getStart());
                    int endPos = getPosition(seq, d.getEnd());

                    if (startPos < 0 || endPos < 0) {
                        continue;
                    }

                    g.fillRect((startPos) * charWidth, height, (endPos - startPos + 1) * charWidth, domainHeightInt);
                    ArrayList<Point> dotDashRanges = getDotDashRanges(seq, startPos, endPos);
                    if (null != dotDashRanges) {
                        for (Point range : dotDashRanges) {
                            g.setColor(lightDomainColorLookup.get(d.getHmmAcc()));
                            g.fillRect(range.x * charWidth, height, ((range.y - range.x) + 1) * charWidth, domainHeightInt);
                        }
                    }

                    //g.fillOval(startPos * charWidth, curHeight + height, (endPos - startPos + 1) * charWidth, height);
//                                    g.setColor(Color.red);
//                                    if (startPos == endPos) {
//                                        g.fillOval(startPos * charWidth, height, charWidth, domainHeightInt);
//                                    }
//                                    else {
//                                        g.fillArc(startPos * charWidth, height, charWidth * 2, domainHeightInt, 90, 180);
//                                        if (startPos + 1 < endPos) {
////                                    g.setColor(domainToColorLookup.get(domainForRow));
//                                            g.fillRect((startPos + 1) * charWidth, height, (endPos - startPos) * charWidth, domainHeightInt);
//                                        }
//                                        if (startPos + 1 == endPos) {
//                                            // Do not draw anything here
//                                        }
//
////                                    g.setColor(Color.green);
//                                        g.fillArc(endPos * charWidth, height, charWidth * 2, domainHeightInt, 270, 180);
//                                    }
                }

            }

            Font f;
            if (node.isSelected()) {
                f = new Font(msaFont, Font.BOLD, font.getSize());
            } else {
                f = font;
            }

            column_x = seq_x_position;
            int seq_y = curHeight + row_height - topInset - 1;
            char[] seq_chars = seq.toCharArray();
            for (int column = seq_range[START_BASE]; column < seq_range[END_BASE]; column++) {
                g.setColor(Color.LIGHT_GRAY);
                g.setFont(f);
                g.drawChars(seq_chars, column, 1, column_x, seq_y);
                column_x += charWidth;
            }
            curHeight += row_height;
        }
    }

    private void displayDomainTrimmed(Graphics g, Rectangle viewport) {
        List<GeneNode> contents = PaintManager.inst().getTree().getTerminusNodes();
        if (contents == null || contents.size() == 0) {
            return;
        }

        g.setFont(font);

        int row_height = PaintManager.inst().getRowHeight();
        int curHeight = PaintManager.inst().getTopMargin();
        double domainHeight = DEFAULT_DOMAIN_HEIGHT;
        if (0 != maxDomainRows) {
            domainHeight = row_height / maxDomainRows;
            if (1 == maxDomainRows || domainHeight > DEFAULT_DOMAIN_HEIGHT) {
                domainHeight = DEFAULT_DOMAIN_HEIGHT;
            }
        }
        int domainHeightInt = new Double(domainHeight).intValue();

        int charWidth = this.getColumnWidth(g);

//		int headerHeight = PaintManager.inst().getTopMargin();
//		int topInset = (headerHeight - font.getSize()) / 2;
//		int header_y = viewport.y + headerHeight - topInset - 1;
//		// Display the header at the top of the viewport
//		if (Color.white != g.getColor()) {
//			g.setColor(Color.white);
//		}
//		g.fillRect(viewport.x, viewport.y, viewport.width, headerHeight - topInset - 1);
//
//		g.setColor(Color.black);
//
        seq_range = setSeqRange(viewport, charWidth, aminoAcidStats.length);
        int seq_x_position = viewport.x; // + seq_range[0] * charWidth + 1;

        int column_x = seq_x_position;
//		char [] ruler = MSAParser.inst().getRuler(full_length);
//		for (int column = seq_range[START_BASE]; column < seq_range[END_BASE]; column++) {
//			g.drawChars(ruler, column, 1, column_x, header_y);
//			column_x += charWidth;
//		}

        for (int row = 0; row < contents.size(); row++) {
            GeneNode node = contents.get(row);
            if (!node.isTerminus()) {
                continue;
            }

            // Display this row if it is within the viewport range, but otherwise continue
            if ((viewport.y > (curHeight + row_height)) || ((viewport.y + viewport.height) < curHeight)) {
                curHeight += row_height;
                continue;
            }

            String seq = (full_length ? node.getSequence() : node.getHMMSeq());
            if ((null == seq) || (0 == seq.length())) {
                curHeight += row_height;
                continue;
            }

            if (node.isPruned()) {
                curHeight += row_height;
                continue;
            }

//                        // Color everything white first and also draw the selected col
//            for (int column = seq_range[START_BASE]; column < seq_range[END_BASE]; column++) {
//                Color color = Color.WHITE;
//                if (column == selectedCol) {
//                    g.setColor(selectedColor);
//                    g.fillRect(column_x, curHeight, charWidth, row_height);
//                } else {
//                    g.setColor(color);
//                    g.fillRect(column_x, curHeight, charWidth, row_height);
//                }
//                g.setColor(Color.black);
//                column_x += charWidth;
//            }

            ArrayList<ArrayList<Domain>> domainRows = node.getDomainRows();
            HashMap<String, ArrayList<Domain>> domainLookup = node.getDomainLookup();
            if (null == domainRows || null == domainLookup || 0 == domainRows.size() || 0 == domainLookup.size()) {
                curHeight += row_height;
                continue;
            }
//                        if (1 < domainRows.size()) {
//                            System.out.println("here");
//                        }
            for (int i = 0; i < domainRows.size(); i++) {
                ArrayList<Domain> curRow = domainRows.get(i);
                double domainPosStart = (domainHeight * (i));
                int height = new Double(domainPosStart).intValue() + curHeight;
                for (Domain d : curRow) {
                    g.setColor(domainToColorLookup.get(d.getHmmAcc()));
                    int startPos = getPosition(seq, d.getStart());
                    int endPos = getPosition(seq, d.getEnd());
                    if (startPos < 0 || endPos < 0) {
                        continue;
                    }
                    g.fillRect((startPos) * charWidth, height, (endPos - startPos + 1) * charWidth, domainHeightInt);
//                                    //g.fillOval(startPos * charWidth, curHeight + height, (endPos - startPos + 1) * charWidth, height);
////                                    g.setColor(Color.red);
//                                    if (startPos + 5 >= endPos) {
//                                        g.fillOval(startPos * charWidth, height, charWidth, domainHeightInt);
//                                    }
//                                    else {
//                                        g.fillArc(startPos * charWidth, height, charWidth * 2, domainHeightInt, 90, 180);
//                                        if (startPos + 1 < endPos) {
////                                    g.setColor(domainToColorLookup.get(domainForRow));
//                                            g.fillRect((startPos + 1) * charWidth, height, (endPos - startPos) * charWidth, domainHeightInt);
//                                        }
//                                        if (startPos + 1 == endPos) {
//                                            // Do not draw anything here
//                                        }
//
////                                    g.setColor(Color.green);
//                                        g.fillArc(endPos * charWidth, height, charWidth * 2, domainHeightInt, 270, 180);
//                                    }
                }

            }

//			Font f;
//			if (node.isSelected()) {
//				f = new Font(msaFont, Font.BOLD, font.getSize());
//			}
//			else {
//				f = font;
//			}
//
//			column_x = seq_x_position;
//			int seq_y = curHeight + row_height - topInset - 1;
//			char [] seq_chars = seq.toCharArray();
//			for (int column = seq_range[START_BASE]; column < seq_range[END_BASE]; column++) {
//				g.setColor(Color.LIGHT_GRAY);
//				g.setFont(f);
//				g.drawChars(seq_chars, column, 1, column_x, seq_y);
//				column_x += charWidth;
//			}
            curHeight += row_height;
        }
    }
        
    public static ArrayList<Point> getDotDashRanges(String seq, int start, int end) {
//        System.out.println("Seq start " + start + " end " + end + " " + seq.substring(start, end + 1));
        StringBuffer sb = new StringBuffer();
        ArrayList<Point> ranges = new ArrayList<Point>();
        Point p = null;
        for (int i = start; i <= end; i++) {

            char c = seq.charAt(i);
            if (CHAR_DOT == c || CHAR_DASH == c) {
                if (null == p) {
                    p = new Point();
                    p.x = i;
                }
                p.y = i;
                continue;
            }
            sb.append(c);

            if (null != p) {
//                p.y++;
//                System.out.println("dot dash range " + seq.substring(p.x, p.y + 1) + " p.x" + p.x + " p.y " + p.y);

                ranges.add((Point) p.clone());
                p = null;
            }

        }
        if (null != p) {
//                p.y++;
//            System.out.println("dot dash range " + seq.substring(p.x, p.y + 1) + " p.x" + p.x + " p.y " + p.y);            
            ranges.add(p);
        }
//        System.out.println("Domain " + sb.toString());
        return ranges;
    }

	/**
	 * Method declaration
	 *
	 *
	 * @param startPos
	 * @param viewport
	 *
	 * @return
	 *
	 * @see
	 */
	private int [] setSeqRange(Rectangle viewport, int charWidth, int seqMaxLen){
		seq_range  = new int [2];
		// Set the range start value
		seq_range[START_BASE] = ((viewport.x + 1) / charWidth) - 1;
		seq_range[START_BASE] = Math.max(0, seq_range[START_BASE]);

		// Set the end value
		seq_range[END_BASE] = seq_range[START_BASE] + (viewport.width / charWidth) + 1;

		if (seq_range[END_BASE] >= seqMaxLen){
			//seq_range[END_BASE] = seq.length();
			seq_range[END_BASE] = seqMaxLen - 1;
		}
		if (seq_range[START_BASE] >= seqMaxLen) {
			seq_range[START_BASE] = seqMaxLen - 1;
		}
		return seq_range;
	}

    private void setColors() {
        if (colors_initialized) {
            return;
        }
        initColumnWeights(false);
        colors_initialized = setUnweightedColor();
    }

	private boolean setUnweightedColor() {
		nodeToColor.clear();
		List<GeneNode> contents = PaintManager.inst().getTree().getTerminusNodes();
		Color colors [] = Preferences.inst().getMSAColors(false);
		float threshold [] = Preferences.inst().getMSAThresholds(false);
		int row_count = contents.size();
		for (int row = 0; row < row_count; row++) {
			GeneNode node = contents.get(row);
//                        boolean hmm = false;
                        String seq = node.getSequence();
//                        if (true == full_length) {
//                            seq = node.getSequence();
//                        }
//                        else {
//                            seq = node.getHMMSeq();
//                            hmm = true;
//                        }
			int seqLength;
			Color [] columnColors = null;
			if (seq == null) {
				seqLength = -1;
			} else {
				seqLength = seq.length();
				columnColors = new Color[seqLength];
			}
			for (int column = 0; column < seqLength; column++) {   
				columnColors[column] = Color.WHITE;
				char c = seq.charAt(column);
				if (c != '.' && c != '-' && c != ' ') {
                                        AminoAcidStats alignStats = null;
//                                        if (false == hmm) {
                                            alignStats = aminoAcidStats[column];
//                                        }
//                                        else {
//                                            int index = msaParser.getMatchPosIndex().get(column);
//                                            if (index >= aminoAcidStats.length) {
//                                                System.out.println("Cannot find index " + index);
//                                                continue;
//                                            }
//                                            alignStats = aminoAcidStats[index];
//                                        }
					double frequency = alignStats.getAAFrequency(c);
					// calculate percentage in this column with same aa
					double weight = (frequency * 100) / row_count;
					for (int k = 0; k < threshold.length; k++) {
						if (weight > threshold[k]) {
							columnColors[column] = colors[k];
							break;
						}
					}

				}
			}
                        if (null != node && null != columnColors) {
                            nodeToColor.put(node, columnColors);
                        }
		}
		return true;
	}

	private boolean setWeightedColor(double totalWt) {
		nodeToColor.clear();
		List<GeneNode> contents = PaintManager.inst().getTree().getTerminusNodes();
		Color colors [] = Preferences.inst().getMSAColors(true);
		float threshold [] = Preferences.inst().getMSAThresholds(true);
		for (int row = 0; row < contents.size(); row++) {
			GeneNode node = contents.get(row);
			String  seq = full_length ? node.getSequence() : node.getHMMSeq();
			if (!node.isTerminus() || seq == null)
				continue;

			int seqLength = seq.length();
			Color []columnColors = new Color[seqLength];
			for (int column = 0; column < seqLength; column++) {
				columnColors[column] = Color.WHITE;
				char c = seq.charAt(column);
				if (c != '-' && c != '.') {
					// Get total weight of chars that count
					AminoAcidStats alignStats = aminoAcidStats[column];
					double weight = alignStats.getAAFrequency(c);
					double percent = 0;
					percent = (weight / totalWt) * 100;
					for (int k = 0; k < threshold.length; k++) {
						if (percent > threshold[k]) {
							columnColors[column] = colors[k];
							break;
						}
					}
				}
			}
			nodeToColor.put(node, columnColors);
		}
		return true;
	}
        
    public int getCol(Point p, Graphics g, Rectangle viewport) {
        if (null == g || null == p || null == viewport) {
            return -1;
        }
        double sampleWidth = getExactColumnWidth(g, msaParser.getSampleSeq());
        int size = msaParser.getSeq_length();
        return (int) ((p.x - viewport.x) / (sampleWidth / size)) + (seq_range[0]);
    }

    protected boolean setSelectedColInfo(Point p, Graphics g, Rectangle viewport) {
        int col = getCol(p, g, viewport);
        if (col < 0) {
            return false;
        }
        selectedCol = col;
        return true;
    }
        
    public ArrayList<Domain> getDomains(Point p, Graphics g) {
        if (null == g || null == p) {
            return null;
        }

        GeneNode node = getSelectedGene(p, g);
        if (null == node) {
            return null;
        }

        int charWidth = getColumnWidth(g);
        int selected = -1;
        int length = msaParser.getSeqLength(full_length) * charWidth;
        if (0 <= p.x && p.x <= length) {
            selected = p.x / charWidth;
        }

//        int position = getPosition(node.getSequence(), selected);
//        if (position < 0) {
//            return null;
//        }
        ArrayList<Domain> rtnList = new ArrayList<Domain>();
        HashMap<String, ArrayList<Domain>> lookup = node.getDomainLookup();
        if (null == lookup) {
            return rtnList;
        }
        for (ArrayList<Domain> domainArray : lookup.values()) {
            for (Domain d : domainArray) {
                if (getPosition(node.getSequence(), d.getStart()) <= selected && selected <= getPosition(node.getSequence(), d.getEnd())) {
                    rtnList.add(d);
                }
            }
        }
        return rtnList;
    }
    
    public ArrayList<KeyResidue> getKeyResidue(Point p, Graphics g, Rectangle viewport) {
        if (null == g || null == p || MSA_DISPLAY.KEY_RESIDUE != this.getDisplayType()) {
            return null;
        }

        GeneNode node = getSelectedGene(p, g);
        if (null == node) {
            return null;
        }

        ArrayList<KeyResidue> krList = node.getKeyResidueList();
        if (null == krList || 0 == krList.size()) {
            return null;
        }
     
        int selected = getCol(p, g, viewport);        
        ArrayList<KeyResidue> rtnList = new ArrayList<KeyResidue>();
        for (KeyResidue kr: krList) {
            if ((kr.getAlignPos() - 1) == selected) {
                rtnList.add(kr);
            }
        }
        if (rtnList.isEmpty()) {
            return null;
        }
        return rtnList;
    }    

	protected GeneNode getSelectedGene(Point p, Graphics g) {
		if (null == g) {
			return null;
		}
		// Header row
		int	charWidth = getColumnWidth(g);
		int header_width = msaParser.getSeqLength(full_length) * charWidth;

		int cur_y = PaintManager.inst().getTopMargin();
		// right most x position for the header (where the click should be)
		int right_x = header_width + seq_range[START_BASE] * charWidth;
		if (p.x > right_x || p.y <= cur_y) {
			// user did not click on the header
			return null;
		}
		List<GeneNode> contents = PaintManager.inst().getTree().getTerminusNodes();
		int row = 0;
		int row_height = PaintManager.inst().getRowHeight();
		while (p.y > cur_y + row_height && row < contents.size()) {
			cur_y += row_height;
			row++;
		}
		if (row < contents.size())
			return contents.get(row);
		else
			return null;
	}
        
    protected int getColumnWidth(Graphics g) {
        if (MSA_DISPLAY.DOMAIN_TRIMMED == displayType) {
            return 1;
        }
        return g.getFontMetrics(font).stringWidth("W");
    }

    protected double getExactColumnWidth(Graphics g, String text) {
        if (MSA_DISPLAY.DOMAIN_TRIMMED == displayType) {
            return 1;
        }
        if (false == g instanceof Graphics2D) {
            return g.getFontMetrics(font).stringWidth(text);
        }
        Graphics2D graphics2d = (Graphics2D)g;
        FontRenderContext context = graphics2d.getFontRenderContext();
        return font.getStringBounds(text, context).getWidth();
        
    }

	protected Rectangle getSelectionRect(Graphics g, Collection<GeneNode> nodes) {
		if (nodes != null && !nodes.isEmpty() && seq_range != null) {
			int min_row = -1;
			int max_row = -1;
			List<GeneNode> contents = PaintManager.inst().getTree().getTerminusNodes();
			for (Iterator<GeneNode> it = nodes.iterator(); it.hasNext(); ) {
				GeneNode node = (GeneNode) it.next();
				int row = contents.indexOf(node);
				if (min_row < 0 || row < min_row) {
					min_row = row;						
				}
				if (max_row < 0 || row > max_row) {
					max_row = row;
				}					
			}
			int	charWidth = getColumnWidth(g);
			int x = seq_range[START_BASE] * charWidth;
			int width = (seq_range[END_BASE] - seq_range[START_BASE]) * charWidth;
			int row_height = PaintManager.inst().getRowHeight();
			int y = PaintManager.inst().getTopMargin() + (min_row * row_height);
			int height = (max_row - min_row + 1) * row_height;
			Rectangle rect = new Rectangle(x, y, width, height);
			return rect;

		} else {
			return null;
		}
	}
        
	/**
	 * Saves information about the counts at each position of the sequence
	 */
	private double initColumnWeights(boolean weighted) {

		int seq_length = msaParser.getSeq_length();
		/* this keeps the overall totals for each count of an AA in a column */
		aminoAcidStats = new AminoAcidStats[seq_length];

		// Calculate total weight of sequences for all nodes
		double totalWt = 0;
		// This use to start at one, trying with 0 instead, no need to skip first node
		List<GeneNode> contents = PaintManager.inst().getTree().getTerminusNodes();
		for (int column = 0; column < seq_length; column++){
			AminoAcidStats alignStats = aminoAcidStats[column];
			if (alignStats == null) {
				alignStats = new AminoAcidStats();
				aminoAcidStats[column] = alignStats;
			}
			for (GeneNode node : contents) {
				if (column == 0)
					totalWt += node.getSequenceWt();
				/*
				 * this is the aligned sequence, with dashes inserted, so all of them are the same length
				 * and so we don't have to worry about which column we are counting
				 */
				String  sequence = node.getSequence();
				if (sequence == null || sequence.length() <= column) {
					continue;
				}
				char aa = sequence.charAt(column);
				double align_frequency = alignStats.getAAFrequency(aa);
				if (weighted) {
					align_frequency += node.getSequenceWt();
				} else {
					align_frequency++;
				}
				alignStats.setAAFrequency(aa, align_frequency);
			}
		}
		return totalWt;
	}

	protected void setWeighted(boolean weighted) {
		MSA_DISPLAY type = weighted ? MSA_DISPLAY.WEIGHTED : MSA_DISPLAY.ENTIRE_ALIGNMENT;
		colors_initialized = (displayType == type);
		displayType = type;
	}

	protected boolean isWeighted() {
		return have_weights && displayType == MSA_DISPLAY.WEIGHTED;
	}

	protected boolean haveWeights() {
		return have_weights;
	}
        
        public boolean haveFullAlignData() {
            return this.haveSeq;
        }

        public boolean haveTrimmedAlignData() {
            return this.haveSeq;
        }
        
        public boolean haveDomainInfo() {
            if (null != domainLookup) {
                return true;
            }
            return false;
        }
        
        public void setDisplayType(MSA_DISPLAY displayType) {
            this.displayType= displayType;
        }
        
        public MSA_DISPLAY getDisplayType() {
            return displayType;
        }
        

        public static int getPosition(String seq, int pos) {
            int seqPos = -1;
            if (null == seq || pos < 0) {
                return seqPos;
            }
            seqPos = 0;
            for (int i = 0; i < seq.length(); i++) {
                char c = seq.charAt(i);
                if (c != CHAR_DOT && c != CHAR_DASH) {
                    seqPos++;
                }
                if (seqPos == pos) {
                    return i;
                }                
            }
            return -1;
        }
        
    public static ArrayList<ArrayList<Domain>> groupWithoutOverlap(ArrayList<Domain> domainList) {
        if (null == domainList) {
            return null;
        }
        
        // First group according to domain acc
        HashMap<String, ArrayList<Domain>> domainLookup = new HashMap<String, ArrayList<Domain>>();
        
        for (Domain d: domainList) {
            ArrayList<Domain> astList = domainLookup.get(d.getHmmAcc());
            if (null == astList) {
                astList = new ArrayList<Domain>();
                domainLookup.put(d.getHmmAcc(), astList);
            }
            astList.add(d);
        }
        
        // Can have operlap within the same domain or with different domains.
        // First handle those that do not have overlap within the same domain
        ArrayList<ArrayList<Domain>> rtnList = new ArrayList<ArrayList<Domain>>();
        for (String domainAcc: domainLookup.keySet()) {
            DomainGroup dg = new DomainGroup(domainLookup.get(domainAcc));
            if (1 == dg.entries.size()) {
                boolean inserted = false;
                for (int i = 0; i < rtnList.size(); i++) {
                    if (false == domainsOverlap(rtnList.get(i), domainLookup.get(domainAcc))) {
                        ArrayList<Domain> cur = rtnList.get(i);
                        cur.addAll(domainLookup.get(domainAcc));
                        inserted = true;
                        break;
                    }
                }
                if (false == inserted) {
                    rtnList.add(domainLookup.get(domainAcc));
                }
                continue;
            }
            for (ArrayList<Domain> entry: dg.entries) {
                boolean inserted = false;
                for (int i = 0; i < rtnList.size(); i++) {
                    if (false == domainsOverlap(rtnList.get(i), entry)) {
                        ArrayList<Domain> cur = rtnList.get(i);
                        cur.addAll(entry);
                        inserted = true;
                        break;
                    }
                }
                if (false == inserted) {
                    rtnList.add(entry);
                }                
            }
        }
        return rtnList;
    }
    
//    protected static boolean domainsOverlap(ArrayList<Domain> list1, ArrayList<Domain> list2) {
//        for (Domain l1d : list1) {
//            for (Domain l2d : list2) {
//                if (true == overlap(l1d.getEnvStart(), l1d.getEnvEnd(), l2d.getEnvStart(), l2d.getEnvEnd())) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    protected static boolean overlap(int a, int b, int c, int d) {
        if (a > d || b < c) {
            return false;
        }
        return true;
    }
    
    public static class DomainGroup {

        public ArrayList<Domain> domainList;

        public ArrayList<ArrayList<Domain>> entries;

        public DomainGroup(ArrayList<Domain> domainList) {
            if (null == domainList) {
                return;
            }
            this.domainList = domainList;
            entries = new ArrayList<ArrayList<Domain>>();
            entries.add(this.domainList);
            for (int i = 0; i < entries.size(); i++) {
                ArrayList<Domain> current = entries.get(i);
                Domain first = current.get(0);
                for (int j = 1; j < current.size(); j++) {
                    if (true == overlap(first.getStart(), first.getEnd(), current.get(j).getStart(), current.get(j).getEnd())) {
                        ArrayList<Domain> newRow = new ArrayList<Domain>();
                        newRow.add(current.remove(j));
                        j--;
                        entries.add(newRow);
                    }
                }
            }
        }
    }           
        
//        public class DomainGroup {
//            public ArrayList<Domain> domainList;
//            
//            public ArrayList<ArrayList<Domain>> entries;
//            
//            public DomainGroup(ArrayList<Domain> domainList) {
//                if (null == domainList) {
//                    return;
//                }
//                this.domainList = domainList;
//                entries = new ArrayList<ArrayList<Domain>>();
//                entries.add(this.domainList);
//                for (int i = 0; i < entries.size(); i++) {
//                    ArrayList<Domain> current = entries.get(i);
//                    Domain first = current.get(0);
//                    for (int j = 1; j < current.size(); j++) {
//                        if (true == MSA.this.overlap(first.getStart(), first.getEnd(), current.get(j).getStart(), current.get(j).getEnd())) {
//                            ArrayList<Domain> newRow = new ArrayList<Domain>();
//                            newRow.add(current.remove(j));
//                            j--;
//                            entries.add(newRow);
//                        }
//                    }
//                }
//            }
//            
//        }
        
        public static void main(String args[]) {
            String seq = "AFERERWRWRIOURWPIZVNMCGFGFG";
            System.out.println("Seq =  " + seq);
            int start = 5;
            int end = 8;
            ArrayList<Point> ranges = MSA.getDotDashRanges(seq, getPosition(seq, start), getPosition(seq, end));
            
            
            seq = "A......AFER...ER..WRWRIOURWPIZVNMCGFGFG";
            System.out.println("Seq =  " + seq);
            start = 1;
            end = 8;
            ranges = MSA.getDotDashRanges(seq, getPosition(seq, start), getPosition(seq, end));             
            
            
            seq = ".......AFER...ER..WRWRIOURWPIZVNMCGFGFG";
            System.out.println("Seq =  " + seq);
            start = 1;
            end = 8;
            ranges = MSA.getDotDashRanges(seq, getPosition(seq, start), getPosition(seq, end));         
            

            seq = "ABC.....AFER...ER..WRWRIOURWPIZVNMCGFGFG";
            System.out.println("Seq =  " + seq);
            start = 1;
            end = 8;
            ranges = MSA.getDotDashRanges(seq, getPosition(seq, start), getPosition(seq, end));            
            
        }

    public boolean haveHaveKeyResidueInfo() {
        return haveKeyResidue;
    }
}