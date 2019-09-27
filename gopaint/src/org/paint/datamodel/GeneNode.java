/**
 *  Copyright 2019 University Of Southern California
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

package org.paint.datamodel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bbop.swing.HyperlinkLabel;
import org.geneontology.db.model.Association;
import org.geneontology.db.model.GeneProduct;
import org.geneontology.db.model.Species;
import org.paint.config.Preferences;
import org.paint.go.GOConstants;
import org.paint.go.GO_Util;
import org.paint.gui.event.TermHyperlinkListener;
import org.paint.gui.table.GeneTableModel;
import org.paint.gui.table.OrthoCell;
import org.paint.main.PaintManager;
import org.paint.util.DuplicationColor;
import org.paint.util.HTMLUtil;
import org.paint.util.RenderUtil;

import com.sri.panther.paintCommon.Constant;
import edu.usc.ksom.pm.panther.paintCommon.Domain;
import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeStaticInfo;
import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
import java.util.HashMap;
import org.paint.gui.familytree.TreeModel;
import org.paint.gui.familytree.TreeModel.TreeColorSchema;
import org.paint.util.SpeciesClsColor;
import org.paint.util.AnnotationUtil;

public class GeneNode {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static final int nodeToTextDist = 10;
    private static final int TextToLineDist = 5;
    public static final int GLYPH_DIAMETER = 8;
    public static final int GLYPH_RADIUS = GLYPH_DIAMETER / 2;

    private final static BasicStroke dashed = new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1f, new float[]{2f, 2f}, 0f);

    private static final String NODE_TYPE_DUPLICATION = "1>0";
    private static final String NODE_TYPE_HORIZONTAL_TRANSFER = "0>0";
    private static final String STR_UNDERSCORE = "_";

    //Attributes
    private GeneNode parent = null;
    private List<GeneNode> children;
    private List<GeneNode> originalChildrenOrder;

    /* 
     * nodeName is magic and should not be touched. 
     * Basically, nodeName is used as the key for indexing into the table of attributes via column 0
     * If nodeName and column 0 do not match, then the indexing fails
     */
    private String seq_db = "";
    private String seq_id = "";
    private String seq_name;
    private String db;
    private String db_id = "";
    private String paint_id = "";                  // Really annotation node id
    private String persistantNodeID;               // PTN id
    private String species;                        // Species - currently read from tree file
    private List<String> species_labels;
    private String species_shortname;
    private String sequence;
    private HashMap<String, ArrayList<Domain>> domainLookup;
    private ArrayList<ArrayList<String>> domainRows;
    private String nodeType;        // Indicates speciation, duplication, species, annotation id etc
    private String nodeNote;
    private String description;
    
    private String geneId;          // Calculated from long gene name
    private String geneSource;      // Calculated from long gene name
    private String proteinId;       // Calculated from long gene name
    private String proteinSource;   // Calcualted from long gene name
    /*
     * Use one variable for all of the different possible programs 
     * and just use the appropriate bit mask to determine whether it belongs or not
     */
    private String ortho_mcl;

    private float distanceFromParent;
    private float distanceFromRoot;

    private boolean is_leaf = true;
    private boolean is_subfamily;
    private boolean is_expanded;
//    private boolean blocked;
    private boolean selected;
    private boolean visible;

    private Rectangle screenRectangle;
    private Point screenPosition;

    private static Logger log = Logger.getLogger(GeneNode.class);

    private String subFamilyName;
    private Color subFamilyColor = Color.black;

    private int depthInTree;
    private int dupColorIndex;
    private String speciesClassification;

    private double sequenceWt = 0;
    private String hmm_seq;

    private HyperlinkLabel accLabel;
    private HyperlinkLabel modLabel;
    private OrthoCell ortho_cell;
    private HyperlinkLabel permaID;

    /*
     * The following two booleans are to indicate respectively
     * 1. whether or not this gene has any annotations recorded in the GO database
     * 2. whether or not these annotations have been retrieved yet from the GO database
     */
    private boolean gotten_from_go;
    private Thread thread;
    private long startTime;
    private int bpCount;
    private int ccCount;
    private int mfCount;

    private GeneProduct gene_product;

    private Node node;          // Information from server about the node

    private Hashtable<String, String> attrLookup = new Hashtable<String, String>();

    private Color dropColor;

    // Methods
    public GeneNode(boolean isExpanded) {
        this.is_expanded = isExpanded;
        this.visible = true;
        this.gotten_from_go = false;
        this.ortho_mcl = Constant.STR_EMPTY;
    }

    public String getSeqName() {
        return seq_name;
    }

    public void setSeqName(String node_name) {
        if (node_name != null && node_name.length() > 0) {

            String[] name = node_name.split(",");
            this.seq_name = name[0];
        }
    }

    public String getSeqId() {
        return seq_id;
    }

    public void setSeqId(String seqdb, String acc) {
        if (acc != null && acc.length() > 0 && seqdb != null && seqdb.length() > 0) {
            this.seq_db = seqdb;
            if (seq_id != null && seq_id.length() > 0 && !seq_id.equals(acc)) {
                log.debug("Changing seq_id from " + this.seq_id + " to " + acc);
            }
            this.seq_id = acc;
//            PaintManager.inst().indexBySeqID(this);
        }
    }

    public void setDatabaseID(String name, String id) {
        if (name != null && name.length() > 0 && id != null && id.length() > 0) {
            this.db = GO_Util.inst().dbNameHack(name);
            this.db_id = id;
//            PaintManager.inst().indexByDBID(this);
        }
    }

    public String getDatabase() {
        return (db == null ? Constant.STR_EMPTY : this.db);
    }

    public String getDatabaseID() {
        return (db_id == null ? Constant.STR_EMPTY : this.db_id);
    }

    public void setDepthInTree(int depth) {
        depthInTree = depth;
    }

    public int getDepthInTree() {
        return depthInTree;
    }

    /**
     * Method declaration
     *
     *
     * @param dsn
     * @param g
     * @param r
     *
     * @see
     */
    private void connectToParent(GeneNode currentRoot, Graphics g) {
        GeneNode parent = getParent();
        if ((null != parent) && (currentRoot != this)) {
            Point parentPos = new Point(parent.getScreenPosition());
            Point currentPos = new Point(getScreenPosition());
            g.setColor(RenderUtil.getLineColor(this));
            /* first draw a vertical hook up/down from the parent 
             * try to avoid drawing on top of the parent glyph
             */
            parentPos.x += GLYPH_RADIUS;
            if (parentPos.y < currentPos.y) {
                parentPos.y += Math.min(GLYPH_RADIUS, currentPos.y - parentPos.y);
            } else if (parentPos.y > currentPos.y) {
                parentPos.y -= Math.min(GLYPH_RADIUS, parentPos.y - currentPos.y);
            }
            if (parentPos.y != currentPos.y) {
                g.drawLine(parentPos.x, parentPos.y, parentPos.x, currentPos.y);
            }

            /*
             * and then draw a horizontal line from the parent to the child
             * don't need to worry about drawing on top of child glyph because
             * it hasn't been drawn yet
             */
            g.drawLine(parentPos.x, currentPos.y, currentPos.x, currentPos.y);
            boolean containsDirectNot = AnnotationUtil.hasDirectNot(this, PaintManager.inst().goTermHelper());
            if (true == containsDirectNot) {
                int distance = currentPos.x - parentPos.x;
                int spacing = distance / 2;
                int x = parentPos.x + spacing;
                int top = currentPos.y - 4;
                g.setColor(Color.red);
                g.fillRect(x, top, 3, 9);
            }
            

//            if (getGeneProduct() != null) {
//                boolean stopped = false;
//                Set<Association> all_assocs = getGeneProduct().getAssociations();
//
//                for (Iterator<Association> assoc_it = all_assocs.iterator(); assoc_it.hasNext() && !stopped;) {
//                    Association assoc = assoc_it.next();
//                    stopped |= assoc.isDirectNot();
//                }
//                if (stopped) {
//                    int distance = currentPos.x - parentPos.x;
//                    int spacing = distance / 2;
//                    int x = parentPos.x + spacing;
//                    int top = currentPos.y - 4;
//                    g.setColor(Color.red);
//                    g.fillRect(x, top, 3, 9);
//                }
//            }
        }
    }

    final static BasicStroke wideStroke = new BasicStroke(8.0f);

    /**
     * Method declaration
     *
     *
     * @param dsn
     * @param g
     * @param triangle
     * @param screenWidth
     *
     * @see Called from DrawableTree as it does a full branch and bound over the
     * tree. dsn.drawMarker(g, r, ((currentRoot == dsn) && (currentRoot !=
     * root)), screenWidth);
     */
    public void drawMarker(GeneNode currentRoot, Graphics g, boolean triangle, Rectangle viewport) {
        if (!nodeAndParentFallInViewport(viewport)) {
            return;
        }
        Rectangle r = new Rectangle(this.getScreenRectangle());
        Point p = new Point(this.getScreenPosition());
        Preferences user_settings = Preferences.inst();
        Color fillColor = dropColor != null ? dropColor
                : RenderUtil.annotationStatusColor(this, user_settings.getForegroundColor());
        Color drawColor = user_settings.getForegroundColor();

        connectToParent(currentRoot, g);

        // Used for drawing lines from the node to the end of the drawing area
        FontMetrics fm;

        if (isSubfamily()) {
            // Draw diamond
            int[] xCoords = {
                // going anti-clockwise from 3 o'clock
                p.x, p.x + GLYPH_RADIUS, p.x + GLYPH_DIAMETER, p.x + GLYPH_RADIUS
            };
            int[] yCoords = {
                p.y, p.y - GLYPH_RADIUS, p.y, p.y + GLYPH_RADIUS
            };
            g.setColor(fillColor);
            g.fillPolygon(xCoords, yCoords, 4);
            g.setColor(drawColor);
            g.drawPolygon(xCoords, yCoords, 4);
        } else if (!isLeaf()) {
            if (triangle) {
                /* 
                 * If the root has been reset to descendant node in the tree
                 * then draw a triangle
                 */
                int[] xCoords = {p.x, p.x, p.x + GLYPH_DIAMETER};
                int[] yCoords = {p.y - GLYPH_RADIUS, p.y + GLYPH_RADIUS, p.y};

                g.setColor(fillColor);
                g.fillPolygon(xCoords, yCoords, 3);
                g.setColor(drawColor);
                g.drawPolygon(xCoords, yCoords, 3);
            } else if (isExpanded() && !isPruned()) {
                if (isDuplication()) {
                    /*
                     * This is a duplication event
                     * Draw this as a square
                     */
                    g.setColor(fillColor);
                    g.fillRect(p.x, p.y - GLYPH_RADIUS, GLYPH_DIAMETER, GLYPH_DIAMETER);
                    g.setColor(drawColor);
                    g.drawRect(p.x, p.y - GLYPH_RADIUS, GLYPH_DIAMETER, GLYPH_DIAMETER);
                } else if (isHorizontalTransfer()) {
                    // Draw diamond
                    p.x = p.x - 1;
                    int[] xCoords = {
                        // going clockwise from 9 o'clock
                        p.x, p.x + GLYPH_RADIUS + 1, p.x + GLYPH_DIAMETER + 2, p.x + GLYPH_RADIUS + 1
                    };
                    int[] yCoords = {
                        p.y, p.y - GLYPH_RADIUS - 1, p.y, p.y + GLYPH_RADIUS + 1
                    };
                    g.setColor(fillColor);
                    g.fillPolygon(xCoords, yCoords, 4);
                    g.setColor(drawColor);
                    g.drawPolygon(xCoords, yCoords, 4);
                } else {
                    /* 
                     * This is a speciation node
                     * Draw this as a circle
                     * circle starts a little higher, from upper left
                     */
                    g.setColor(fillColor);
                    g.fillOval(p.x, p.y - GLYPH_RADIUS, GLYPH_DIAMETER, GLYPH_DIAMETER);
                    g.setColor(drawColor);
                    g.drawOval(p.x, p.y - GLYPH_RADIUS, GLYPH_DIAMETER, GLYPH_DIAMETER);
                }
            } else if (isPruned()) {
                /* 
                 * This is if the user has said the tree is wrong and
                 * an entire branch should be removed
                 * Not sure what to draw here, trying a cone for now, very crude
                 */
                g.setColor(Color.gray.brighter());
                g.fillRect(p.x, p.y - GLYPH_RADIUS, GLYPH_DIAMETER, GLYPH_DIAMETER);
                g.setColor(drawColor);
                g.drawRect(p.x, p.y - GLYPH_RADIUS, GLYPH_DIAMETER, GLYPH_DIAMETER);
                g.drawLine(p.x, p.y - GLYPH_RADIUS, p.x + GLYPH_DIAMETER, p.y + GLYPH_RADIUS);
                g.drawLine(p.x, p.y + GLYPH_RADIUS, p.x + GLYPH_DIAMETER, p.y - GLYPH_RADIUS);
            } else {
                /* 
                 * This is if the user has collapsed the node
                 * Not sure what to draw here, trying a small vertical rectangle for now
                 */
                g.setColor(fillColor);
                g.fillRect(p.x, p.y - GLYPH_RADIUS, GLYPH_RADIUS, GLYPH_DIAMETER);
                g.setColor(drawColor);
                g.drawRect(p.x, p.y - GLYPH_RADIUS, GLYPH_RADIUS, GLYPH_DIAMETER);
            }
        }

        if (isTerminus()) {
            /*
             * if this is a terminus then there is a label
             * draw the little line that connects the node to the label
             */
            int x = p.x + nodeToTextDist;

            /*
             */
            Font f = RenderUtil.getNodeFont(this);
            g.setFont(f);
            String s = getNodeLabel();
            if (isPruned()) {
                s = "XXX-" + s;
                g.setColor(Preferences.inst().getBackgroundColor());
            } else {
                TreeColorSchema tcs = PaintManager.inst().getTree().getTreeModel().getTreeColorSchema();
                if (TreeColorSchema.DUPLICATION == tcs) {
                    g.setColor(DuplicationColor.inst().getDupColor(getDupColorIndex()));
                }
                else {
                    g.setColor(SpeciesClsColor.getInst().getColorForSpecies(speciesClassification));
                }
            }
            g.fillRect(x, p.y - GLYPH_RADIUS, viewport.width, GLYPH_DIAMETER * 2);

            AttributedString as = new AttributedString(s);
            if (null != s && 0 != s.length()) {
                as.addAttribute(TextAttribute.FONT, f);
            }
            
            // Cannot see white on white
            Color labelColor = RenderUtil.annotationStatusColor(this, user_settings.getBackgroundColor());
            if (labelColor.equals(user_settings.getBackgroundColor())) {
                labelColor = Color.BLACK;
            }
            g.setColor(labelColor);
            if (null != s) {
                int text_x = p.x + nodeToTextDist;
                int text_y = p.y + (r.height / 2);
                g.drawString(as.getIterator(), text_x, text_y);
                fm = g.getFontMetrics(f);
                int text_width = fm.stringWidth(s);
                x += text_width;
            } else {
                log.debug("Why is label null for " + this.toString());
            }

            x += TextToLineDist;

            g.setColor(drawColor);
            if (!(g instanceof Graphics2D)) {
                g.drawLine(x, p.y, viewport.width, p.y);
            } else {
                Graphics2D g2 = (Graphics2D) g;
                Stroke oldStroke = g2.getStroke();
                g2.setStroke(dashed);
                g2.drawLine(x, p.y, viewport.x + viewport.width, p.y);
                g2.setStroke(oldStroke);
            }
        }

    }

    public boolean isPruned() {
        NodeVariableInfo nvi = node.getVariableInfo();
        if (null != nvi) {
            return nvi.isPruned();
        }
        return false;
    }

    public void setPrune(boolean prune) {
        NodeVariableInfo nvi = node.getVariableInfo();
        if (null == nvi) {
            nvi = new NodeVariableInfo();
            node.setVariableInfo(nvi);
        }
        nvi.setPruned(prune);
//        this.blocked = prune;
    }

    /**
     * Returns if node and or its parent fall in the viewport. If information
     * cannot be determined, return true.
     *
     * @param node
     * @param viewport
     */
    private boolean nodeAndParentFallInViewport(Rectangle viewport) {
        GeneNode parent = (GeneNode) this.parent;
        if (parent == null) {
            return true;
        }

        // Do not want to consider the x-coordinate, since, horizontal dotted line has to be drawn
        Rectangle parentRect = parent.getScreenRectangle();
        if (screenRectangle != null) {
            if ((screenRectangle.y < viewport.y && screenRectangle.y + screenRectangle.height < viewport.y
                    && parentRect.y < viewport.y && parentRect.y + parentRect.height < viewport.y)
                    || (viewport.y + viewport.height < screenRectangle.y
                    && viewport.y + viewport.height < parentRect.y)) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public String getNodeLabel() {
        String s = null;
        if (isLeaf()) {
            NodeStaticInfo nsi = node.getStaticInfo();
            ArrayList<String> geneSymbols = nsi.getGeneSymbol();
            if (null != geneSymbols && 0 < geneSymbols.size()) {
                s = geneSymbols.get(0);
            }
            else {
                String longGeneName = nsi.getLongGeneName();
                if (null != longGeneName) {
                    s = getGeneId();
                }
            }
//            
//            // only display the node name, as requested
//            if (getGeneProduct() == null) {
//                s = getSeqName();
//            } else {
//                s = getGeneProduct().getSymbol();
//            }
            String sp = takeFive();
            if (sp.length() <= 5 && sp.length() > 0) {
                s = takeFive() + '_' + s;
            }
        } else {
            if (getSpeciesLabel().length() > 0) {
                s = getSpeciesLabel() + '_';
            } else {
                s = Constant.STR_EMPTY;
            }
        }
        if (null == s) {
            s = getDatabaseID();
        }
        return s;
    }
    
    public String getNodeLabelWithPTN() {
        String s = null;
        NodeStaticInfo nsi = node.getStaticInfo();
        if (isLeaf()) {    
            ArrayList<String> geneSymbols = nsi.getGeneSymbol();
            if (null != geneSymbols && 0 < geneSymbols.size()) {
                s = geneSymbols.get(0);
            }
            else {
                String longGeneName = nsi.getLongGeneName();
                if (null != longGeneName) {
                    s = getGeneId();
                }
            }

            String sp = takeFive();
            if (sp.length() <= 5 && sp.length() > 0) {
                s = takeFive() + STR_UNDERSCORE + s;
            }
        } else {
            if (getSpeciesLabel().length() > 0) {
                s = getSpeciesLabel() + STR_UNDERSCORE;
            } else {
                s = Constant.STR_EMPTY;
            }
        }
        if (null == s) {
            s = getDatabaseID();
        }
        if (s.isEmpty() || s.endsWith(STR_UNDERSCORE)) {
            return s + nsi.getPublicId();
        }
        return s + STR_UNDERSCORE + nsi.getPublicId();
    }    

    public void setNodeArea(double x, double y, double w, double h) {
        int x_spot = (int) Math.round(x);
        int y_spot = (int) Math.round(y);
        int width = (int) Math.round(w);
        int height = (int) Math.round(h);
        Point p = new Point(x_spot, y_spot);
        setScreenPosition(p);
        screenRectangle = new Rectangle(x_spot, y_spot, height, width);
    }

    public String getSpeciesLabel() {
        String species = null;

        GeneProduct gp = getGeneProduct();
        if (gp != null) {
            Species sp = gp.getSpecies();
            if (sp != null) {
                species = (sp.getGenus() + " " + sp.getSpecies()).trim();
            }
        }

        if (species == null && this.species_labels != null) {
            /* 
             * Not sure which one to get
             * Longest? Last one added?
             */
            species = species_labels.get(species_labels.size() - 1);
        }
        return (species == null ? Constant.STR_EMPTY : species);
    }
    
    
    public void setSpecies(String species) {
        this.species = species;
    }
    
    public String getSpecies() {
            if (null != species && TreeModel.SPECIES_CLASSIFICATION.contains(species.toUpperCase())) {
                System.out.println("HERE - Found " + species);
            }        
        return species;
    }
    
    public String getCalculatedSpecies() {
        if (null != species) {
            return species;
        }       
        if (null != parent) {
            return parent.getCalculatedSpecies();
        }
        return null;
    }

    public List<String> getSpeciesList() {
        return species_labels;
    }

    //Why are we adding a label.  Is it possible to have multiple species labels for a node?
    public void addSpeciesLabel(String species) {
        species.trim();
        if (species != null && species.length() > 0) {
            if (TreeModel.SPECIES_CLASSIFICATION.contains(species.toUpperCase())) {
                System.out.println("HERE - Found " + species);
            }
            if (species_labels == null) {
                species_labels = new ArrayList<String>();
            }
            boolean duplicate = false;
            for (String label : species_labels) {
                duplicate |= species.equals(label);
            }
            if (!duplicate) {
                species_labels.add(species);
            }
            if (species_labels.size() > 1) {
                System.out.println("Multiple species labels for a node");
            }
            if (species.length() <= 5) {
                this.species_shortname = species;
            }
        }
    }

    private String takeFive() {
        return species_shortname != null ? species_shortname : "";
    }

    public Color getSubFamilyColor() {
        return subFamilyColor;
    }

    public void setSubFamilyColor(Color subFamilyColor) {
        this.subFamilyColor = subFamilyColor;
    }

    public Rectangle getScreenRectangle() {
        if (screenRectangle == null) {
            log.debug(this + " has not had its screen rectangle set!");
        }
        return screenRectangle;
    }

    private void setScreenPosition(Point position) {
        screenPosition = position;
    }

    private Point getScreenPosition() {
        return screenPosition;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public void setSequenceWt(double sequenceWt) {
        this.sequenceWt = sequenceWt;
    }

    public double getSequenceWt() {
        return sequenceWt;
    }
    
    public void setDomainLookup(HashMap<String, ArrayList<Domain>> domainLookup) {
        this.domainLookup = domainLookup;
    }
    
    public HashMap<String, ArrayList<Domain>>  getDomainLookup() {
        return domainLookup;
    }

    public ArrayList<ArrayList<String>> getDomainRows() {
        return domainRows;
    }

    public void setDomainRows(ArrayList<ArrayList<String>> domainRows) {
        this.domainRows = domainRows;
    }
    
    public HyperlinkLabel getAccLabel() {
        if (accLabel == null) {
            accLabel = HTMLUtil.makeHyperlinkField(new TermHyperlinkListener());
            HTMLUtil.setHyperlinkField(this, accLabel, GeneTableModel.ACC_COL_NAME);
        }
        return accLabel;
    }

    public HyperlinkLabel getModLabel() {
        if (modLabel == null) {
            modLabel = HTMLUtil.makeHyperlinkField(new TermHyperlinkListener());
            HTMLUtil.setHyperlinkField(this, modLabel, this.getDatabase());
        }
        return modLabel;
    }

    public HyperlinkLabel getPermaCell() {
        if (permaID == null) {
            permaID = HTMLUtil.makeHyperlinkField(new TermHyperlinkListener());
            HTMLUtil.setHyperlinkField(this, permaID, GeneTableModel.PERMNODEID_COL_NAME);
        }
        return permaID;
    }

    public OrthoCell getOrthoCell() {
        if (ortho_cell == null) {
            Color color = ortho_mcl.equals(Constant.STR_EMPTY) ? Preferences.inst().getBackgroundColor() : RenderUtil.getOrthoColor(ortho_mcl);
            ortho_cell = new OrthoCell(color, ortho_mcl);
        }
        return ortho_cell;
    }

    public OrthoCell getOrthoCell(String heading) {
        if (ortho_cell == null) {
            String value = this.getAttrLookup(heading);
            if (null == value) {
                value = Constant.STR_EMPTY;
            }
            Color color = value.equals(Constant.STR_EMPTY) ? Preferences.inst().getBackgroundColor() : RenderUtil.getOrthoColor(value);
            ortho_cell = new OrthoCell(color, ortho_mcl);
        }
        return ortho_cell;
    }

    public void setOrthoMCL(String ortho_mcl) {
        this.ortho_mcl = ortho_mcl;
    }

    public String getOrthoMCL() {
        return this.ortho_mcl;
    }

    public GeneProduct getGeneProduct() {
        return gene_product;
    }

    /*
     * This is only called from the GeneProductThread once the gene product
     * has been successfully retrieved from the GO database
     * In turn, it creates another thread to retrieve the annotations. 
     */
    public void setGeneProduct(GeneProduct gene_product) {
        this.gene_product = gene_product;
        PaintManager.inst().indexByGP(this);
    }

    public String getDescription() {
        if (!isLeaf() && description == null) {
            StringBuffer about_me = new StringBuffer();
            myChildren(this, about_me);
            description = about_me.toString();
        }
        if (description == null) {
            description = "";
        }
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private void myChildren(GeneNode node, StringBuffer about_me) {
        List<GeneNode> nodes_children = node.getChildren();
        for (Iterator<GeneNode> it = nodes_children.iterator(); it.hasNext();) {
            GeneNode child = it.next();
            if (child.isLeaf()) {
                about_me.append(child.getDatabaseID() + " ");
            } else {
                myChildren(child, about_me);
            }
        }
    }

    public void setAttrLookup(String type, String value) {
        if (null != type && null != value) {
            attrLookup.put(type, value);
        }
    }

    public String getAttrLookup(String type) {
        return attrLookup.get(type);
    }

    public Set<Association> getAssociations() {
        if (!gotten_from_go && thread != null) {
            /*
             * This returns one of 3 values
             * PENDING, SUCCESS, FAILED
             */
            int status = GO_Util.inst().threadFinished(thread, startTime);
            while (status == GO_Util.PENDING) {
                status = GO_Util.inst().threadFinished(thread, startTime);
            }
            gotten_from_go = (status == GO_Util.SUCCESS);
        }
        return (gene_product != null ? gene_product.getAssociations() : null);
    }

    public String getSeqDB() {
        return seq_db;
    }

    public void setParent(GeneNode parent) {
        this.parent = parent;
    }

    public GeneNode getParent() {
        return parent;
    }

    public boolean isLeaf() {
        return is_leaf;
    }

    public void setSubFamilyName(String name) {
        subFamilyName = name;
    }

    public String getSubFamilyName() {
        return subFamilyName;
    }

    public void setIsSubfamily(boolean subfamily) {
        is_subfamily = subfamily;
    }

    public boolean isSubfamily() {
        return is_subfamily;
    }

    public boolean isTerminus() {
        return is_leaf || (!is_leaf && !is_expanded);
    }

    public void setType(String s) {
        this.nodeType = s;
    }

    public String getType() {
        return nodeType;
    }

    public void setNodeNote(String note) {
        nodeNote = note;
    }

    public String getNodeNote() {
        return nodeNote;
    }

    public void setExpanded(boolean expanded) {
        is_expanded = expanded;
    }

    public boolean isExpanded() {
        return is_expanded;
    }

    // Setter/Getter methods
    public boolean initChildren(List<GeneNode> children) {
        if (null == children) {
            this.children = children;
            is_leaf = true;
            return true;
        }
        if (children.isEmpty()) {
            return false;
        }

        this.children = children;
        is_leaf = false;
        return true;
    }

    public List<GeneNode> getOriginalChildrenOrder() {
		// Return a copy of the vector and not the original vector, else
        // the information can be changed.
        if (null == originalChildrenOrder) {
            return null;
        }
        List<GeneNode> copy = new ArrayList<GeneNode>(originalChildrenOrder.size());
        copy.addAll(originalChildrenOrder);
        return copy;
    }

    public boolean setChildren(List<GeneNode> children) {
        if (null == originalChildrenOrder) {
            if (true == initChildren(children)) {
                setOriginalChildrenToCurrentChildren();
                return true;
            }
        }
        boolean returnVal = initChildren(children);
        setOriginalChildrenToCurrentChildren();
        return returnVal;
    }

    public List<GeneNode> getChildren() {
        return children;
    }

    public void getTermini(List<GeneNode> leaves) {
        if (leaves != null) {
            if (this.isTerminus()) {
                leaves.add(this);
            } else {
                for (int i = 0; i < children.size(); i++) {
                    GeneNode child = children.get(i);
                    child.getTermini(leaves);
                }
            }
        }
    }

    public void setOriginalChildrenToCurrentChildren() {
        if (children == null) {
            return;
        }
        if (originalChildrenOrder == null) {
            originalChildrenOrder = new ArrayList<GeneNode>();
        }
        originalChildrenOrder.clear();
        originalChildrenOrder.addAll(children);
    }

    public boolean isDuplication() {
        if (null == nodeType) {
            return false;
        }
        int index = nodeType.indexOf(NODE_TYPE_DUPLICATION);
        if (index < 0) {
            return false;
        }
        return true;
    }

    public boolean isHorizontalTransfer() {
        if (null == nodeType) {
            return false;
        }
        int index = nodeType.indexOf(NODE_TYPE_HORIZONTAL_TRANSFER);
        if (index < 0) {
            return false;
        }
        return true;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setDistanceFromParent(float dist) {
        distanceFromParent = dist;
    }

    public float getDistanceFromParent() {
        return distanceFromParent;
    }

    public void setDistanceFromRoot(float dist) {
        distanceFromRoot = dist;
    }

    public float getDistanceFromRoot() {
        return distanceFromRoot;
    }

    public String getPaintId() {
        return paint_id;
    }

    public void setPaintId(String an_number) {
        if (an_number == null || (an_number != null && an_number.length() == 0)) {
            log.debug("Trying to set PAINT id to null");
        } else {
            if (this.paint_id != null && (this.paint_id.length() > 0)) {
                log.debug("Changing paint_id from " + this.paint_id + " to " + an_number);
            }
            this.paint_id = an_number;
            PaintManager.inst().indexByPaintID(this);
        }
    }

    public String toString() {
        if (db != null && db_id != null && (!db.equals(GOConstants.PANTHER_DB))) {
            return db + ":" + db_id;
        }
        if (persistantNodeID != null && db != null && db.equals(GOConstants.PANTHER_DB)) {
            return this.persistantNodeID;
        }
        if (paint_id != null) {
            return paint_id;
        } else {
            return "Unidentified node";
        }
    }

    public String getHMMSeq() {
        return hmm_seq;
    }

    public void setHMMSeq(String hmm_seq) {
        this.hmm_seq = hmm_seq;
    }
    
    public boolean hasBiologicalProcessEvidence() {
            Node n = this.getNode();
            NodeVariableInfo nvi = n.getVariableInfo();
            if (null ==  nvi) {
                return false;
            }
            ArrayList<edu.usc.ksom.pm.panther.paintCommon.Annotation> annotList = nvi.getGoAnnotationList();
            if (null == annotList || 0 == annotList.size()) {
                return false;
            }
            for (edu.usc.ksom.pm.panther.paintCommon.Annotation annot: annotList) {
                String term = annot.getGoTerm();
                GOTerm gTerm = PaintManager.inst().goTermHelper().getTerm(term);
                if (GOTermHelper.ASPECT_BP.equals(gTerm.getAspect())) {
                    return true;
                }
            }
            return false;    
    }

    public boolean hasBiologicalProcessEvidenceOld() {
        return bpCount > 0;
    }

    public int getBiologicalProcessCount() {
        return bpCount;
    }

    public void setBiologicalProcessCount(int bpCount) {
        this.bpCount = bpCount;
    }
    
    public boolean hasCellularComponentEvidence() {
            Node n = this.getNode();
            NodeVariableInfo nvi = n.getVariableInfo();
            if (null ==  nvi) {
                return false;
            }
            ArrayList<edu.usc.ksom.pm.panther.paintCommon.Annotation> annotList = nvi.getGoAnnotationList();
            if (null == annotList || 0 == annotList.size()) {
                return false;
            }
            for (edu.usc.ksom.pm.panther.paintCommon.Annotation annot: annotList) {
                String term = annot.getGoTerm();
                GOTerm gTerm = PaintManager.inst().goTermHelper().getTerm(term);
                if (GOTermHelper.ASPECT_CC.equals(gTerm.getAspect())) {
                    return true;
                }
            }
            return false;    
    }

    public boolean hasCellularComponentEvidenceOld() {
        return ccCount > 0;
    }

    public int getCellularComponentCount() {
        return ccCount;
    }

    public void setCellularComponentCount(int ccCount) {
        this.ccCount = ccCount;
    }
    public boolean hasMolecularFunctionEvidence() {
        Node n = this.getNode();
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null == nvi) {
            return false;
        }
        ArrayList<edu.usc.ksom.pm.panther.paintCommon.Annotation> annotList = nvi.getGoAnnotationList();
        if (null == annotList || 0 == annotList.size()) {
            return false;
        }
        for (edu.usc.ksom.pm.panther.paintCommon.Annotation annot : annotList) {
            String term = annot.getGoTerm();
            GOTerm gTerm = PaintManager.inst().goTermHelper().getTerm(term);
            if (GOTermHelper.ASPECT_MF.equals(gTerm.getAspect())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMolecularFunctionEvidenceOld() {
        return mfCount > 0;
    }

    public int getMolecularFunctionCount() {
        return mfCount;
    }

    public void setMolecularFunctionCount(int mfCount) {
        this.mfCount = mfCount;
    }

    public void setDropColor(Color dropColor) {
        this.dropColor = dropColor;
    }

    public String getPersistantNodeID() {
        return persistantNodeID;
    }

    public void setPersistantNodeID(String persistantNodeID) {
        if (persistantNodeID == null) {
            log.debug(this + " PTN is being set to null");
        } else {
            if (persistantNodeID.equals("PTN000657503")) {
                log.debug("Check this out " + this);
            }
            if (this.persistantNodeID == null) {
                this.persistantNodeID = persistantNodeID;
                PaintManager.inst().indexNodeByPTN(this);
                if (db == null || (db != null && db.length() == 0)) {
                    db = GOConstants.PANTHER_DB;
                    db_id = persistantNodeID;
//                    PaintManager.inst().indexByDBID(this);
                }
            } else {
                if (this.persistantNodeID != null && (this.persistantNodeID.length() > 0)) {
                    log.debug("Changing PTN from " + this.persistantNodeID + " to " + persistantNodeID);
                }
            }
        }
    }

    public String getGeneId() {
        return geneId;
    }

    public void setGeneId(String geneId) {
        this.geneId = geneId;
    }

    public String getGeneSource() {
        return geneSource;
    }

    public void setGeneSource(String geneSource) {
        this.geneSource = geneSource;
    }

    public String getProteinId() {
        return proteinId;
    }

    public void setProteinId(String proteinId) {
        this.proteinId = proteinId;
    }

    public String getProteinSource() {
        return proteinSource;
    }

    public void setProteinSource(String proteinSource) {
        this.proteinSource = proteinSource;
    }

    public int getDupColorIndex() {
        return dupColorIndex;
    }

    public void setDupColorIndex(int sfColorIndex) {
        this.dupColorIndex = sfColorIndex;
    }

    public String getSpeciesClassification() {
        return speciesClassification;
    }

    public void setSpeciesClassification(String speciesClassification) {
        this.speciesClassification = speciesClassification;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

}
