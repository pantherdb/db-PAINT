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
package edu.usc.ksom.pm.panther.paintCommon;

import com.sri.panther.paintCommon.Constant;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;


public class Comment implements Serializable {
    private String classificationId;
    private String nodeId;
    
    // Comments to be split into 4 sections (mf, cc, bp and notes section) without "//" prefix:
    //# molecular_function
    //20161117  userName    Save/Obsolete   EvidenceCode    publicId    term-(qualifier delimited by comma)
    //20161117  user1234    Save    IBD PTN000001285   GO:0030276
    //20161117  user1234    Save    IKR PTN000001677l   GO:0030276-(NOT)
    //# cellular_component
    //...
    //# biological_process
    //...
    //# Notes
    //1. There are a few C2CD proteins in the middle of the tree. These proteins contain C2 domain that are shared among all Syt proteins. Functionally they do not share any function with the Syt members except for Ca binding.
    //2. A paper (PMID:17190793) shows that Syt12 does not bind to Ca and is not involved in Ca dependent exocytosis. 
    
    private String text;
    private String commentUserNotes;
    private String commentMfSection;
    private String commentBpSection;
    private String commentCcSection;
    
    private static final String PREFIX_SECTION_MF = "# molecular_function";
    private static final String PREFIX_SECTION_CC = "# cellular_component";
    private static final String PREFIX_SECTION_BP = "# biological_process";
    private static final String PREFIX_SECTION_NOTES = "# Notes";
    
    protected static final String DELIM_PARTS = "\t";
    protected static final int NUM_PARTS = 6;
    protected static int SIZE_DATE = 8;        // yyyymmdd

    public Comment(String classificationId, String nodeId, String text) {
        this.classificationId = classificationId;
        this.nodeId = nodeId;
        if (null != text) {
            text = text.trim();
        }
        this.text = text;
        parse();
    }

    public String getClassificationId() {
        return classificationId;
    }

    public void setClassificationId(String classificationId) {
        this.classificationId = classificationId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCommentUserNotes() {
        return commentUserNotes;
    }

    public void setCommentUserNotes(String commentUserNotes) {
        this.commentUserNotes = commentUserNotes;
    }
    
    public void prependCommentUserNotes(String prependStr) {
        if (null == commentUserNotes) {
            this.commentUserNotes = prependStr;
            return;
        }
        this.commentUserNotes = prependStr + Constant.STR_NEWLINE + this.commentUserNotes;        
    }

    public String getCommentMfSection() {
        return commentMfSection;
    }

    public void setCommentMfSection(String commentMfSection) {
        this.commentMfSection = commentMfSection;
    }
    
    public void prependCommentMfSection(String prependStr) {
        if (null == commentMfSection) {
            this.commentMfSection = prependStr;
            return;
        }
        this.commentMfSection = prependStr + Constant.STR_NEWLINE + this.commentMfSection;
    }    

    public String getCommentBpSection() {
        return commentBpSection;
    }

    public void setCommentBpSection(String commentBpSection) {
        this.commentBpSection = commentBpSection;
    }
    
    public void prependCommentBpSection(String prependStr) {
        if (null == commentBpSection) {
            this.commentBpSection = prependStr;
            return;
        }
        this.commentBpSection = prependStr + Constant.STR_NEWLINE + this.commentBpSection;
    }

    public String getCommentCcSection() {
        return commentCcSection;
    }

    public void setCommentCcSection(String commentCcSection) {
        this.commentCcSection = commentCcSection;
    }
    
    public void prependCommentCcSection(String prependStr) {
        if (null == commentCcSection) {
            this.commentCcSection = prependStr;
            return;
        }
        this.commentCcSection = prependStr + Constant.STR_NEWLINE + this.commentCcSection;
    }    
    
    public void parse() {
        if (null == text) {
            return;
        }
        int indexMf = text.indexOf(PREFIX_SECTION_MF);
        int indexCc = text.indexOf(PREFIX_SECTION_CC);
        int indexBp = text.indexOf(PREFIX_SECTION_BP);        
        int indexNotes = text.indexOf(PREFIX_SECTION_NOTES);
        
        if (0 > indexMf && 0 > indexBp && 0 > indexCc && 0 > indexNotes) {
            commentUserNotes = text;
            commentUserNotes = commentUserNotes.trim();
        }
        else {
            if (indexMf == 0) {
                if (indexCc > 0) {
                    commentMfSection = text.substring(indexMf + PREFIX_SECTION_MF.length(), indexCc).trim();
                    commentMfSection = orderEntries(commentMfSection).trim();
                    commentMfSection = commentMfSection + Constant.STR_NEWLINE;
                }
                else if (indexBp > 0) {
                    commentMfSection = text.substring(indexMf + PREFIX_SECTION_MF.length(), indexBp).trim();
                    commentMfSection = orderEntries(commentMfSection).trim();                    
                    commentMfSection = commentMfSection + Constant.STR_NEWLINE;                    
                }
                else if (indexNotes > 0) {
                    commentMfSection = text.substring(indexMf + PREFIX_SECTION_MF.length(), indexNotes).trim();
                    commentMfSection = orderEntries(commentMfSection).trim();                    
                    commentMfSection = commentMfSection + Constant.STR_NEWLINE;                    
                }
                else {
                    commentMfSection = text.substring(indexMf + PREFIX_SECTION_MF.length()).trim();
                    commentMfSection = orderEntries(commentMfSection).trim();                    
                    commentMfSection = commentMfSection + Constant.STR_NEWLINE;                    
                }
            }
            if (indexCc >= 0) {
                if (indexBp > indexCc) {
                    commentCcSection = text.substring(indexCc + PREFIX_SECTION_CC.length(), indexBp).trim();
                    commentCcSection = orderEntries(commentCcSection).trim();                    
                    commentCcSection = commentCcSection + Constant.STR_NEWLINE;
                }
                else if (indexNotes > indexCc) {
                    commentCcSection = text.substring(indexCc + PREFIX_SECTION_CC.length(), indexNotes).trim();
                    commentCcSection = orderEntries(commentCcSection).trim();                    
                    commentCcSection = commentCcSection + Constant.STR_NEWLINE;                    
                }
                else {
                    commentCcSection = text.substring(indexCc + PREFIX_SECTION_CC.length()).trim();
                    commentCcSection = orderEntries(commentCcSection).trim();                    
                    commentCcSection = commentCcSection + Constant.STR_NEWLINE;                    
                }
            }
            if (indexBp >= 0) {
                if (indexNotes > indexBp) {
                    commentBpSection = text.substring(indexBp + + PREFIX_SECTION_BP.length(), indexNotes).trim();
                    commentBpSection = orderEntries(commentBpSection).trim();                    
                    commentBpSection = commentBpSection + Constant.STR_NEWLINE;
                }
                else {
                    commentBpSection = text.substring(indexBp + PREFIX_SECTION_BP.length()).trim();
                    commentBpSection = orderEntries(commentBpSection).trim();                    
                    commentBpSection = commentBpSection + Constant.STR_NEWLINE;                    
                }                
            }
            if (indexNotes >= 0) {
                commentUserNotes = text.substring(indexNotes + PREFIX_SECTION_NOTES.length()).trim();
//                commentUserNotes = orderEntries(commentUserNotes).trim();                
                commentUserNotes = commentUserNotes + Constant.STR_NEWLINE;                
            }
        }
    }
    /**
     * Some of the entries are not ordered correctly.  Also there are some where there is no line break between entries.
     * @param s
     * @return 
     */
    public static String orderEntries(String s) {
        if (null == s) {
            return s;
        }
        String lines[] = s.split(Constant.STR_NEWLINE);
        if (null == lines) {
            return s;
        }
        try {
            HashMap<String, ArrayList<String>> dateToCmtLookup = new HashMap<String, ArrayList<String>>();
            for (String line : lines) {
                if (null == line) {
                    continue;
                }
                String parts[] = line.split(DELIM_PARTS);
                if (null == parts || 0 == parts.length) {
                    ArrayList<String> values = dateToCmtLookup.get(line);
                    if (null == values) {
                        values = new ArrayList<String>();
                        dateToCmtLookup.put(line, values);
                    }
                    values.add(line);
                } else {
                    if (NUM_PARTS >= parts.length) {
                        ArrayList<String> values = dateToCmtLookup.get(parts[0]);
                        if (null == values) {
                            values = new ArrayList<String>();
                            dateToCmtLookup.put(parts[0], values);
                        }
                        values.add(line);

                    } else if (NUM_PARTS < parts.length) {
                        // Handle 20190424        user123 Obsolete        IBD     PTN001308314    GO:000551520190701      user456  Obsolete        IBD     PTN000036886    GO:0004435
                        int fixIndex = NUM_PARTS - 1;
                        ArrayList<String> fixedList = new ArrayList<String>(Arrays.asList(parts));
                        boolean error = false;
                        while (fixIndex < fixedList.size()) {
                            String fixPart = fixedList.get(fixIndex);
                            if (SIZE_DATE < fixPart.length()) {
                                String newPart = fixPart.substring(fixPart.length() - SIZE_DATE);
                                try {
                                    Integer.parseInt(newPart);
                                    String oldPart = fixPart.substring(0, fixPart.length() - SIZE_DATE);
                                    fixedList.set(fixIndex, oldPart);
                                    fixedList.add(fixIndex + 1, newPart);
                                    fixIndex = fixIndex + 1;
                                    if (fixIndex + NUM_PARTS == fixedList.size()) {
                                        break;
                                    }
                                    fixIndex = fixIndex + NUM_PARTS - 1;
                                    continue;
                                } catch (Exception e) {

                                    error = true;
                                    break;
                                }
                            } else {
                                error = true;
                                break;
                            }
                        }
                        if (false == error && 0 == fixedList.size() % NUM_PARTS) {
                            for (int i = 0; i < fixedList.size() / NUM_PARTS; i++) {
                                int index = i * NUM_PARTS;
                                StringBuffer sb = new StringBuffer();
                                for (int j = index; j < index + NUM_PARTS; j++) {
                                    sb.append(fixedList.get(j));
                                    sb.append(DELIM_PARTS);
                                }
                                ArrayList<String> values = dateToCmtLookup.get(fixedList.get(index));
                                if (null == values) {
                                    values = new ArrayList<String>();
                                    dateToCmtLookup.put(fixedList.get(index), values);
                                }
                                values.add(sb.toString().trim());
                            }
                        } else {
                            ArrayList<String> values = dateToCmtLookup.get(parts[0]);
                            if (null == values) {
                                values = new ArrayList<String>();
                                dateToCmtLookup.put(parts[0], values);
                            }
                            values.add(line);
                        }
                    }
                }
            }
            Set<String> keys = dateToCmtLookup.keySet();
            ArrayList<String> keyList = new ArrayList<String>(keys);
            Collections.sort(keyList, Collections.reverseOrder());
            StringBuffer sb = new StringBuffer();
            for (String key : keyList) {
                ArrayList<String> values = dateToCmtLookup.get(key);
                for (String value : values) {
                    sb.append(value);
                    sb.append(Constant.STR_NEWLINE);
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return s;
        }
        
    }
    
    public String getFormattedComment() {
        StringBuffer sb = new StringBuffer();
        if (null != commentMfSection) {
            sb.append(PREFIX_SECTION_MF + Constant.STR_NEWLINE);
            sb.append(commentMfSection.trim());
            sb.append(Constant.STR_NEWLINE);
        }
        if (null != commentBpSection) {
            sb.append(PREFIX_SECTION_BP + Constant.STR_NEWLINE);
            sb.append(commentBpSection.trim());
            sb.append(Constant.STR_NEWLINE);
        }
        if (null != commentCcSection) {
            sb.append(PREFIX_SECTION_CC + Constant.STR_NEWLINE);
            sb.append(commentCcSection.trim());
            sb.append(Constant.STR_NEWLINE);
        }
        if (null != commentUserNotes) {
            sb.append(PREFIX_SECTION_NOTES + Constant.STR_NEWLINE);
            sb.append(commentUserNotes.trim());
            sb.append(Constant.STR_NEWLINE);
        }         
        return sb.toString();
    }
    
    public String getRevisionHistoryInfo() {
        StringBuffer sb = new StringBuffer();
        if (null != commentMfSection) {
            sb.append(PREFIX_SECTION_MF + Constant.STR_NEWLINE);
            sb.append(commentMfSection.trim());
            sb.append(Constant.STR_NEWLINE);
        }
        if (null != commentBpSection) {
            sb.append(PREFIX_SECTION_BP + Constant.STR_NEWLINE);
            sb.append(commentBpSection.trim());
            sb.append(Constant.STR_NEWLINE);
        }
        if (null != commentCcSection) {
            sb.append(PREFIX_SECTION_CC + Constant.STR_NEWLINE);
            sb.append(commentCcSection.trim());
            sb.append(Constant.STR_NEWLINE);
        }       
        return sb.toString();        
    }
    
    public static void main(String args[]) {
        String test1 = "# molecular_function\n" +
"20141117: Euteleostomi_PTN000002099 lost/modified clathrin binding (GO:0030276) capacity\n" +
"20141117: Euteleostomi_PTN000001654 lost/modified clathrin binding (GO:0030276) capacity\n" +
"20141117: Euteleostomi_PTN000001677 lost/modified clathrin binding (GO:0030276) capacity\n" +
"20141117: root_PTN000001285 has function clathrin binding (GO:0030276)\n" +
"20141117: Tetrapoda_PTN000001864 has function phosphatidylinositol-4,5-bisphosphate binding (GO:0005546)\n" +
"20141117: Euteleostomi_PTN000002099 lost/modified syntaxin binding (GO:0019905) capacity\n" +
"20141117: Euteleostomi_PTN000001654 lost/modified syntaxin binding (GO:0019905) capacity\n" +
"20141117: Euteleostomi_PTN000001677 lost/modified syntaxin binding (GO:0019905) capacity\n" +
"20141117: root_PTN000001285 has function syntaxin binding (GO:0019905)\n" +
"20141117: Bilateria_PTN000001972 lost/modified calcium ion binding (GO:0005509) capacity\n" +
"20141117: Euteleostomi_PTN000740742 lost/modified calcium ion binding (GO:0005509) capacity\n" +
"20141117: root_PTN000001283 has function calcium ion binding (GO:0005509)\n" +
"20141117: Bilateria_PTN000001972 lost/modified calcium-dependent phospholipid binding (GO:0005544) capacity\n" +
"20141117: Euteleostomi_PTN000740742 lost/modified calcium-dependent phospholipid binding (GO:0005544) capacity\n" +
"20141117: Eumetazoa_PTN000797547 lost/modified calcium-dependent phospholipid binding (GO:0005544) capacity\n" +
"20141117: Euteleostomi_PTN000001422 lost/modified calcium-dependent phospholipid binding (GO:0005544) capacity\n" +
"20141117: root_PTN000001283 has function calcium-dependent phospholipid binding (GO:0005544)\n" +
"# cellular_component\n" +
"20141117: Eumetazoa_PTN000797746 located in exocytic vesicle (GO:0070382)\n" +
"20141117: root_PTN000001283 located in plasma membrane (GO:0005886)\n" +
"20141117: Bilateria_PTN000740739 located in synaptic vesicle membrane (GO:0030672)\n" +
"20141117: Bilateria_PTN000740739 located in synaptic vesicle (GO:0008021)\n" +
"# biological_process\n" +
"20141117: Euteleostomi_PTN000001485 lost/modified calcium ion-dependent exocytosis of neurotransmitter (GO:0048791) capacity\n" +
"20141117: Euteleostomi_PTN000001485 lost/modified regulation of calcium ion-dependent exocytosis (GO:0017158) capacity\n" +
"20141117: Bilateria_PTN000740739 participates in synaptic vesicle endocytosis (GO:0048488)\n" +
"20141117: Euteleostomi_PTN000002099 lost/modified calcium ion-dependent exocytosis of neurotransmitter (GO:0048791) capacity\n" +
"20141117: Euteleostomi_PTN000001654 lost/modified calcium ion-dependent exocytosis of neurotransmitter (GO:0048791) capacity\n" +
"20141117: Euteleostomi_PTN000001677 lost/modified calcium ion-dependent exocytosis of neurotransmitter (GO:0048791) capacity\n" +
"20141117: Eumetazoa_PTN000797547 lost/modified calcium ion-dependent exocytosis of neurotransmitter (GO:0048791) capacity\n" +
"20141117: root_PTN000001285 participates in calcium ion-dependent exocytosis of neurotransmitter (GO:0048791)\n" +
"20141117: Euteleostomi_PTN000002099 lost/modified vesicle fusion (GO:0006906) capacity\n" +
"20141117: Euteleostomi_PTN000001654 lost/modified vesicle fusion (GO:0006906) capacity\n" +
"20141117: Euteleostomi_PTN000001677 lost/modified vesicle fusion (GO:0006906) capacity\n" +
"20141117: root_PTN000001285 participates in vesicle fusion (GO:0006906)\n" +
"20141117: Eumetazoa_PTN000797547 lost/modified regulation of calcium ion-dependent exocytosis (GO:0017158) capacity\n" +
"20141117: Euteleostomi_PTN000740742 lost/modified regulation of calcium ion-dependent exocytosis (GO:0017158) capacity\n" +
"20141117: Bilateria_PTN000001972 lost/modified regulation of calcium ion-dependent exocytosis (GO:0017158) capacity\n" +
"20141117: Euteleostomi_PTN000002099 lost/modified regulation of calcium ion-dependent exocytosis (GO:0017158) capacity\n" +
"20141117: Euteleostomi_PTN000001654 lost/modified regulation of calcium ion-dependent exocytosis (GO:0017158) capacity\n" +
"20141117: Euteleostomi_PTN000001677 lost/modified regulation of calcium ion-dependent exocytosis (GO:0017158) capacity\n" +
"20141117: root_PTN000001285 participates in regulation of calcium ion-dependent exocytosis (GO:0017158)\n" +
"20141117: Euteleostomi_PTN000001677 lost/modified synaptic vesicle exocytosis (GO:0016079) capacity\n" +
"20141117: Euteleostomi_PTN000001654 lost/modified synaptic vesicle exocytosis (GO:0016079) capacity\n" +
"20141117: Euteleostomi_PTN000002099 lost/modified synaptic vesicle exocytosis (GO:0016079) capacity\n" +
"20141117: root_PTN000001285 participates in synaptic vesicle exocytosis (GO:0016079)\n" +
"# Notes\n" +
"1. There are a few C2CD proteins in the middle of the tree. These proteins contain C2 domain that are shared among all Syt proteins. Functionally they do not share any function with the Syt members except for Ca binding.\n" +
"2. A paper (PMID:17190793) shows that Syt12 does not bind to Ca and is not involved in Ca dependent exocytosis. \n" +
"\n" +
"Nov. 17, 2014 - HM\n" +
"\n" +
"This family was checked into svn in Feb. 2015, but it should be recurated once the paint software is updated, because some of the NOT annotations are not properly done due to the tool problem. See emails and PAINT call minutes back in November for details.\n" +
"\n" +
"Feb. 12, 2015, HM\n" +
"\n" +
"\n" +
"Annotation inferences using phylogenetic trees\n" +
"\n" +
"The goal of the GO Reference Genome Project, described in PMID 19578431, is to provide accurate, complete and consistent GO annotations for all genes in twelve model organism genomes. To this end, GO curators are annotating evolutionary trees from the PANTHER database with GO terms describing molecular function, biological process and cellular component. GO terms based on experimental data from the scientific literature are used to annotate ancestral genes in the phylogenetic tree by sequence similarity (ISS), and unannotated descendants of these ancestral genes are inferred to have inherited these same GO annotations by descent. The annotations are done using a tool called PAINT (Phylogenetic Annotation and INference Tool).\n" +
"";
        Comment c1 = new Comment(null, null, test1);
        System.out.println("MF section " + c1.getCommentMfSection());
        System.out.println("BP section " + c1.getCommentBpSection());
        System.out.println("CC section " + c1.getCommentCcSection());
        System.out.println("Notes section " + c1.getCommentUserNotes());
        
        String test2 = "1. There are a few C2CD proteins in the middle of the tree. These proteins contain C2 domain that are shared among all Syt proteins. Functionally they do not share any function with the Syt members except for Ca binding.\n" +
"2. A paper (PMID:17190793) shows that Syt12 does not bind to Ca and is not involved in Ca dependent exocytosis. \n" +
"\n" +
"Nov. 17, 2014 - HM\n" +
"\n" +
"This family was checked into svn in Feb. 2015, but it should be recurated once the paint software is updated, because some of the NOT annotations are not properly done due to the tool problem. See emails and PAINT call minutes back in November for details.\n" +
"\n" +
"Feb. 12, 2015, HM\n" +
"\n" +
"\n" +
"Annotation inferences using phylogenetic trees\n" +
"\n" +
"The goal of the GO Reference Genome Project, described in PMID 19578431, is to provide accurate, complete and consistent GO annotations for all genes in twelve model organism genomes. To this end, GO curators are annotating evolutionary trees from the PANTHER database with GO terms describing molecular function, biological process and cellular component. GO terms based on experimental data from the scientific literature are used to annotate ancestral genes in the phylogenetic tree by sequence similarity (ISS), and unannotated descendants of these ancestral genes are inferred to have inherited these same GO annotations by descent. The annotations are done using a tool called PAINT (Phylogenetic Annotation and INference Tool).\n" +
"";
        System.out.println("TEST 2 -----------------------------------");
        Comment c2 = new Comment(null, null, test2);
        System.out.println("MF section " + c2.getCommentMfSection());
        System.out.println("BP section " + c2.getCommentBpSection());
        System.out.println("CC section " + c2.getCommentCcSection());
        System.out.println("Notes section " + c2.getCommentUserNotes());
        
        
        String test3 = "# cellular_component\n" +
"20141117: Eumetazoa_PTN000797746 located in exocytic vesicle (GO:0070382)\n" +
"20141117: root_PTN000001283 located in plasma membrane (GO:0005886)\n" +
"20141117: Bilateria_PTN000740739 located in synaptic vesicle membrane (GO:0030672)\n" +
"20141117: Bilateria_PTN000740739 located in synaptic vesicle (GO:0008021)\n" +
"# biological_process\n" +
"20141117: Euteleostomi_PTN000001485 lost/modified calcium ion-dependent exocytosis of neurotransmitter (GO:0048791) capacity\n" +
"20141117: Euteleostomi_PTN000001485 lost/modified regulation of calcium ion-dependent exocytosis (GO:0017158) capacity\n" +
"20141117: Bilateria_PTN000740739 participates in synaptic vesicle endocytosis (GO:0048488)\n" +
"20141117: Euteleostomi_PTN000002099 lost/modified calcium ion-dependent exocytosis of neurotransmitter (GO:0048791) capacity\n" +
"20141117: Euteleostomi_PTN000001654 lost/modified calcium ion-dependent exocytosis of neurotransmitter (GO:0048791) capacity\n" +
"20141117: Euteleostomi_PTN000001677 lost/modified calcium ion-dependent exocytosis of neurotransmitter (GO:0048791) capacity\n" +
"20141117: Eumetazoa_PTN000797547 lost/modified calcium ion-dependent exocytosis of neurotransmitter (GO:0048791) capacity\n" +
"20141117: root_PTN000001285 participates in calcium ion-dependent exocytosis of neurotransmitter (GO:0048791)\n";
        System.out.println("TEST 3 -----------------------------------");
        Comment c3 = new Comment(null, null, test3);
        System.out.println("MF section " + c3.getCommentMfSection());
        System.out.println("BP section " + c3.getCommentBpSection());
        System.out.println("CC section " + c3.getCommentCcSection());
        System.out.println("Notes section " + c3.getCommentUserNotes());       
        
        
        String test4 = "# molecular_function\n" +
"20190424	pgaudet	Obsolete	IBD	PTN002569912	GO:0005515\n" +
"20190424	pgaudet	Obsolete	IBD	PTN002569942	GO:0005515\n" +
"20190424	pgaudet	Obsolete	IBD	PTN002569901	GO:0005515\n" +
"20190424	pgaudet	Obsolete	IBD	PTN001308314	GO:000551520190701	mihn_s  Obsolete	IBD	PTN000036886	GO:000443520190424	pgaudet	Obsolete	IBD	PTN001308314	GO:0005515fgdfg20190424	pgaudet	Obsolete	IBD	PTN001308314	GO:0005515\n" +
"20190701	mihn_s	Obsolete	IRD	PTN000818466	GO:0004435-(NOT)\n" +
"20190701	mihn_s	Obsolete		Unknown	GO:0005515\n" +
"20190701	mihn_s	Save	IRD	PTN000818466	GO:0004435-(NOT)\n" +
"# cellular_component\n" +
"20190424	pgaudet	Obsolete	IBD	PTN000818645	GO:0005886\n" +
"20190424	pgaudet	Obsolete	IBD	PTN002569901	GO:0005737\n" +
"20190424	pgaudet	Obsolete	IBD	PTN002569901	GO:0005829\n" +
"20190424	pgaudet	Obsolete	IBD	PTN000818423	GO:0005886\n" +
"# Notes\n" +
"2018-11-22 Reviewed PG\n" +
"2019-04-24 Removed protein binding annotations PG\n" +
"\\n2019-06-14: In the PANTHER14.1 update, PTN002569824 can not be directly mapped but their direct child nodes PTN000036886 can be mapped to . These nodes were annotated with GO terms GO:0004435 (IBD).\\n\\n2019-06-14: In the PANTHER14.1 update, PANTHER14.1 family PTHR10336 lost IBD annotations.  PTN002569901 (Eukaryota - SPECIATION, contains HUMAN_PLCD1), PTN002569912 (Opisthokonts - SPECIATION, contains HUMAN_PLCE1) that was annotated with GO terms GO:0005546 (IBD), GO:0007265 (IBD), GO:0017016 (IBD), GO:0004435 (IBD) can not be mapped to PANTHER14.1.\\n";
        
System.out.println("TEST 4 -----------------------------------");
        Comment c4 = new Comment(null, null, test4);
        System.out.println("MF section " + c4.getCommentMfSection());
        System.out.println("BP section " + c4.getCommentBpSection());
        System.out.println("CC section " + c4.getCommentCcSection());
        System.out.println("Notes section " + c4.getCommentUserNotes());                       
                
    }
    
    
    
}
