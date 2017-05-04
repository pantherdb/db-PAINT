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
package org.paint.gui.msa;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.paint.config.Preferences;
import org.paint.datamodel.GeneNode;
import org.paint.main.PaintManager;

/**
 * Class declaration
 *
 *
 * @author
 * @version %I%, %G%
 */
public class MSA {

	private enum MSA_DISPLAY {
		SIGNIFICANT,
		//		SF_CONSERVED,
		TYPE_WTS;

		public String toString() {
			return super.toString().toLowerCase();
		}
	}

	private boolean full_length;
	private MSA_DISPLAY  display_type;
	private boolean have_weights;

	/* by row and then by column */
	private Hashtable<GeneNode, Color[]> nodeToColor = new Hashtable<GeneNode, Color[]>();

	private static final String              msaFont = "Monospaced";          // Has to be fixed width font

	private static Logger log = Logger.getLogger(MSA.class);

	private boolean colors_initialized = false;
	private Font          font;
	private AminoAcidStats[] aminoAcidStats;

	private int selectedCol = -1;               // Selected column

	// Refers to sequence range that is currently visible (x-coord is the start position and y-coord is the end position
	private int [] seq_range;

	private static final int START_BASE = 0;
	private static final int END_BASE = 1;

	/**
	 * Constructor declaration
	 *
	 *
	 * @param msaInfo
	 * @param gridInfo
	 *
	 * @see
	 */
	public MSA(String[] msaInfo, String[] wtsInfo) {
		MSAParser parser = MSAParser.inst();
		parser.parseSeqs(msaInfo);
		have_weights = parser.parseWts(wtsInfo);
		full_length = true;
		display_type = MSA_DISPLAY.SIGNIFICANT;

		Font f = Preferences.inst().getFont();
		setFont(new Font(msaFont, f.getStyle(), f.getSize()));
		setColors();
	}

	protected void reorderRows(List<GeneNode> node_list) {
		// keep a local copy of the currently visible nodes (i.e. rows)
		colors_initialized = false;	// Reordering should not change colors, but might?
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
		if (full != full_length) {
			full_length = full;
			colors_initialized = false;
		}
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
		Rectangle   r = new Rectangle();
		int row_height = PaintManager.inst().getRowHeight();
		int colWidth = getColumnWidth(g);
		if (row_height == 0) {
			log.warn("Row height is not set");
			row_height = 16;
		}
		r.height = row_height;
		r.width = 0;
		List<GeneNode> contents = PaintManager.inst().getTree().getTerminusNodes();
		if (contents != null){
			r.height += contents.size() * row_height;
			r.width += (MSAParser.inst().getSeqLength(full_length)) * colWidth;
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
		if (contents == null || contents.size() == 0)
			return;

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

		seq_range = setSeqRange(viewport, charWidth, aminoAcidStats.length);
		int seq_x_position = viewport.x; // + seq_range[0] * charWidth + 1;

		int column_x = seq_x_position;
		char [] ruler = MSAParser.inst().getRuler(full_length);
		for (int column = seq_range[START_BASE]; column < seq_range[END_BASE]; column++) {
			g.drawChars(ruler, column, 1, column_x, header_y);
			column_x += charWidth;
		}

		for (int row = 0; row < contents.size(); row++){
			GeneNode  node = contents.get(row);
			if (!node.isTerminus())
				continue;

			// Display this row if it is within the viewport range, but otherwise continue
			if ((viewport.y > (curHeight + row_height)) || ((viewport.y + viewport.height) < curHeight)){
				curHeight += row_height;
				continue;
			}

			String seq = (full_length ? node.getSequence() : node.getHMMSeq());
			if ((null == seq) || (0 == seq.length())){
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
			if (node.isSelected()) {
				f = new Font(msaFont, Font.BOLD, font.getSize());
			}
			else {
				f = font;
			}

			column_x = seq_x_position;
			int seq_y = curHeight + row_height - topInset - 1;
			char [] seq_chars = seq.toCharArray();
			for (int column = seq_range[START_BASE]; column < seq_range[END_BASE]; column++) {
				Color color = nodeColors[column];
				if (column == selectedCol) {
					g.setColor(Color.pink);
					g.fillRect(column_x, curHeight, charWidth, row_height);
				}
				else {
					g.setColor(color);
					g.fillRect(column_x, curHeight, charWidth, row_height);
				}
				g.setColor(Color.black);
				g.setFont(f);
				g.drawChars(seq_chars, column, 1, column_x, seq_y);
				column_x += charWidth;
			}
			curHeight += row_height;
		}
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
		if (colors_initialized)
			return;

		double totalWt = initColumnWeights(isWeighted());
		if (isWeighted()) {
			colors_initialized = setWeightedColor(totalWt);
		} else {
			colors_initialized = setUnweightedColor();
		}
	}

	private boolean setUnweightedColor() {
		nodeToColor.clear();
		List<GeneNode> contents = PaintManager.inst().getTree().getTerminusNodes();
		Color colors [] = Preferences.inst().getMSAColors(false);
		float threshold [] = Preferences.inst().getMSAThresholds(false);
		int row_count = contents.size();
		for (int row = 0; row < row_count; row++) {
			GeneNode node = contents.get(row);
			String  seq = (full_length ? node.getSequence() : node.getHMMSeq());
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
					AminoAcidStats alignStats = aminoAcidStats[column];
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
			nodeToColor.put(node, columnColors);
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

	protected void setSelectedColInfo(Point p, Graphics g) {
		if (null == g) {
			return;
		}
		// Set character width and height, if not already set
		int	charWidth = getColumnWidth(g);

		// Header row
		int length = MSAParser.inst().getSeqLength(full_length) * charWidth;
		if (0 <= p.x - length) {
			selectedCol = (p.x - length)/ charWidth;
		}
	}

	protected GeneNode getSelectedGene(Point p, Graphics g) {
		if (null == g) {
			return null;
		}
		// Header row
		int	charWidth = getColumnWidth(g);
		int header_width = MSAParser.inst().getSeqLength(full_length) * charWidth;

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
		return g.getFontMetrics(font).stringWidth("W") + 3;
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

		int seq_length = MSAParser.inst().getSeqLength(full_length);
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
				String  sequence = (full_length ? node.getSequence() : node.getHMMSeq());
				if (sequence == null) {
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
		MSA_DISPLAY type = weighted ? MSA_DISPLAY.TYPE_WTS : MSA_DISPLAY.SIGNIFICANT;
		colors_initialized = (display_type == type);
		display_type = type;
	}

	protected boolean isWeighted() {
		return have_weights && display_type == MSA_DISPLAY.TYPE_WTS;
	}

	protected boolean haveWeights() {
		return have_weights;
	}

}