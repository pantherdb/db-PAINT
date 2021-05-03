/* 
 * 
 * Copyright (c) 2019, Regents of the University of California 
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

package org.paint.util;

import edu.usc.ksom.pm.panther.paintCommon.Evidence;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.paint.config.Preferences;
import org.paint.datamodel.GeneNode;
import org.paint.gui.AspectSelector;
import org.paint.main.PaintManager;

public class RenderUtil {

	private static final Logger LOG = Logger.getLogger(RenderUtil.class);

	private static final String STR_DOT_DOT = "...";
	private static final String STR_EMPTY = "";

	private static HashMap<String, Color> ortho_colors;

	public static void paintBorder(Graphics g, Rectangle r, Color bgColor, boolean selected) {
		if (bgColor != null) {
			g.setColor(bgColor);
			g.fillRect(r.x, r.y, r.width, r.height);
		}
		if (selected) {
			Preferences prefs = Preferences.inst();
			Color color = prefs.getForegroundColor();
			g.setColor(color);
			// line across the top of the cell in the table
			g.drawLine(r.x, r.y, r.x + r.width, r.y);
		} else {
			g.setColor(Color.lightGray);
		}
		// line across the bottom of the cell in the table
		int bottom_y = r.y + r.height - 1;
		g.drawLine(r.x, bottom_y, r.x + r.width, bottom_y);
	}

	/**
	 *  If string does not fit into cell, replace the end of the string with '...' to denote missing text
	 */
	public static String formatText(Graphics g, Insets insets, int boxWidth, String text, Font font) {
		if (text == null) {
			return "";
		}
		FontMetrics fm = g.getFontMetrics(font);

		int neededWidth = RenderUtil.getTextWidth(fm, text);

		if (boxWidth >= neededWidth) {
			return text;
		}

		String finalStr = STR_DOT_DOT;
		neededWidth = RenderUtil.getTextWidth(fm, finalStr);

		// Return empty string if the ellipsis cannot fit into the column.
		if (neededWidth > boxWidth) {
			return STR_EMPTY;
		}

		StringBuffer sb = new StringBuffer(finalStr);
		int i = 0;
		while ((neededWidth < boxWidth) && (i <= text.length() - 1)) {
			sb.insert(i, text.charAt(i));
			try {
				neededWidth = RenderUtil.getTextWidth(fm, sb.toString());
			}
			catch (ArrayIndexOutOfBoundsException e) {

				LOG.error("ArrayIndexOutOfBoundsException " + e.getMessage() + " returned while attempting to calculate Text size.");

			}
			i++;
		}

		// Remove last added character
		if (neededWidth > boxWidth) {
			try {
				sb.setLength(i - 1);
				sb.append(finalStr);
			}
			catch (StringIndexOutOfBoundsException  e) {

				LOG.error("StringIndexOutOfBoundsException " + e.getMessage() + " returned while attempting to delete character from string buffer.");

			}
		}
		return  sb.toString();
	}

	public static int getTextWidth(FontMetrics fm, String s) {
		int width = 0;
		try {
			width = fm.stringWidth(s);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			LOG.error("ArrayIndexOutOfBoundsException " + e.getMessage() + 
			" returned while attempting to calculate Text size.");
		}
		return width;
	}

	public static int getWidth(FontMetrics fm, String text, Insets insets) {
		return getTextWidth(fm, text + insets.left + insets.right);
	}

	public static Font getNodeFont(GeneNode node) {
		Font f = Preferences.inst().getFont();
		//if (node.hasExpEvidence())
		if ((node.hasBiologicalProcessEvidence() &&
				AspectSelector.inst().getAspect() == AspectSelector.Aspect.BIOLOGICAL_PROCESS) ||
				(node.hasCellularComponentEvidence() &&
						AspectSelector.inst().getAspect() == AspectSelector.Aspect.CELLULAR_COMPONENT) ||
						(node.hasMolecularFunctionEvidence() &&
								AspectSelector.inst().getAspect() == AspectSelector.Aspect.MOLECULAR_FUNCTION))
			f = new Font(f.getFontName(), Font.BOLD, f.getSize());
		return f;
	}

	public static Color annotationStatusColor(GeneNode node, Color c) {
		/*
		 * Default is to make it the same as the background
		 */
		Color color = new Color(c.getRGB());
		Preferences prefs = Preferences.inst();
                String go_aspect = AspectSelector.aspects.get(AspectSelector.inst().getAspect().toString());
                Node n = node.getNode();
                NodeVariableInfo nvi = n.getVariableInfo();
                if (null != nvi) {
                    boolean foundExp = false;
                    boolean foundNonExp = false;
                    boolean foundCurated = false;
                    GOTermHelper gth = PaintManager.inst().goTermHelper();
                    ArrayList<edu.usc.ksom.pm.panther.paintCommon.Annotation> annotList = nvi.getGoAnnotationList();
                    if (null != annotList) {
                        for (edu.usc.ksom.pm.panther.paintCommon.Annotation annot: annotList) {
//                            String gTerm = annot.getGoTerm();
//                            if (null == gTerm) {
//                                System.out.println("Here");
//                            }
//                            GOTerm goTerm = gth.getTerm(gTerm);
//                            if (null == goTerm) {
//                                System.out.println("Here..");
//                            }
//                            if (null == goTerm.getAspect()) {
//                                System.out.println("Aspect is null");
//                            }
//                            if (go_aspect == null) {
//                                System.out.println("This is null");
//                            }
                            if (true == go_aspect.equals(gth.getTerm(annot.getGoTerm()).getAspect())) {
                                if (true == annot.isExperimental()) {
                                    foundExp = true;
                                    break;
                                }
                                else {
                                    foundNonExp = true;
                                }
                                String code = annot.getSingleEvidenceCodeFromSet();
                                if (true == Evidence.CODE_IBD.equals(code) || (true == Evidence.CODE_IKR.equals(code) && false == node.isLeaf()) || true == Evidence.CODE_IRD.equals(code)|| true == Evidence.CODE_TCV.equals(code)) {
                                    foundCurated = true;
                                }
                            }
                        }
                    }
                    if (true == foundNonExp) {
                        color = prefs.getInferPaintColor();
                    }                    
                    if (true == foundExp) {
                        color = prefs.getExpPaintColor();
                    }
                    if (true == foundCurated) {
                        color = prefs.getCuratedPaintColor();
                    }
                }

//		if ((node.hasBiologicalProcessEvidence() &&
//				AspectSelector.inst().getAspect() == AspectSelector.Aspect.BIOLOGICAL_PROCESS) ||
//				(node.hasCellularComponentEvidence() &&
//						AspectSelector.inst().getAspect() == AspectSelector.Aspect.CELLULAR_COMPONENT) ||
//						(node.hasMolecularFunctionEvidence() &&
//								AspectSelector.inst().getAspect() == AspectSelector.Aspect.MOLECULAR_FUNCTION)) {
//			color = prefs.getExpPaintColor();
//		}
//                else 
//                if (GO_Util.inst().isPainted(node, false) && (false == node.isLeaf())) {
//                    if (true == GeneNodeUtil.hasDirectAnnotation(node)) {
//                        color = prefs.getCuratedPaintColor();
//                    }
//                    if (true == GeneNodeUtil.hasAllPropagatedAnnotation(node)) {
//                        color = prefs.getInferPaintColor();
//                    }
//			Set<Association> associations = node.getGeneProduct().getAssociations();
//			for (Iterator<Association> assoc_it = associations.iterator(); assoc_it.hasNext();) {
//				Association assoc = assoc_it.next();
//				String termCv = assoc.getTerm().getCv();
//				if ((AspectSelector.inst().getAspect() ==
//					AspectSelector.Aspect.BIOLOGICAL_PROCESS &&
//					termCv.equals(AspectSelector.Aspect.BIOLOGICAL_PROCESS.toString())) ||
//					(AspectSelector.inst().getAspect() ==
//						AspectSelector.Aspect.CELLULAR_COMPONENT &&
//						termCv.equals(AspectSelector.Aspect.CELLULAR_COMPONENT.toString())) ||
//						AspectSelector.inst().getAspect() ==
//							AspectSelector.Aspect.MOLECULAR_FUNCTION &&
//							termCv.equals(AspectSelector.Aspect.MOLECULAR_FUNCTION.toString())) {
//					if (assoc.isMRC() && !node.isLeaf()) {
//						color = prefs.getCuratedPaintColor();
//					}
//					else if (!color.equals(prefs.getCuratedPaintColor())) {
//						color = prefs.getInferPaintColor();
//					}
//				}
//			}
		
		color = selectedColor(node.isSelected(), color, c);
		return color;
	}

	public static Color selectedColor(boolean selected, Color color, Color c) {
		if (selected) {
			if (color.equals(c)) {
				color = Color.gray;
			} else {
				color = color.brighter().brighter();
			}
		}
		return color;
	}

	public static Color getAspectColor() {
		return getAspectColor(AspectSelector.inst().getAspect().toString());
	}

	public static Color getAspectColor(String cv) {
		if (cv != null) {
//                    System.out.println(AspectSelector.Aspect.BIOLOGICAL_PROCESS.toString());
			if (cv.equals(AspectSelector.Aspect.BIOLOGICAL_PROCESS.toString()))
				return Preferences.inst().getAspectColor(Preferences.HIGHLIGHT_BP);
			if (cv.equals(AspectSelector.Aspect.CELLULAR_COMPONENT.toString()))
				return Preferences.inst().getAspectColor(Preferences.HIGHLIGHT_CC);
			if (cv.equals(AspectSelector.Aspect.MOLECULAR_FUNCTION.toString()))
				return Preferences.inst().getAspectColor(Preferences.HIGHLIGHT_MF);
		}
		return Preferences.inst().getBackgroundColor();
	}

	public static Color getLineColor(GeneNode node) {
		return ((GeneNode) node.getParent()).getSubFamilyColor();
	}

	public static Color getOrthoColor(String ortho_name) {
		if (ortho_colors == null) {
			ortho_colors = new HashMap<String, Color> ();
		}
		Color color = ortho_colors.get(ortho_name);
		if (color == null) {
			int red_val = 0;
			int green_val = 0;
			int blue_val = 0;
			while ((red_val + green_val + blue_val) < 128) {
				red_val = (int)(Math.random() * 255);
				green_val = (int)(Math.random() * 255);
				blue_val = (int)(Math.random() * 255);
			}
			color = new Color(red_val, green_val, blue_val);
			ortho_colors.put(ortho_name, color);
		}
		return color;
	}

}
