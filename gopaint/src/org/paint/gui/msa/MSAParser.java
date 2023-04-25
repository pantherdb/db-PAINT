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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.paint.datamodel.GeneNode;
import org.paint.main.PaintManager;

import com.sri.panther.paintCommon.Constant;

public class MSAParser{
	private static final String PREFIX_SEQ_START = ">";

	private static Logger log = Logger.getLogger(MSAParser.class);

	private int seq_length;
	private int hmm_length;
        
        private ArrayList<Boolean> matchPosList;
        private ArrayList<Integer> matchPosIndex;

	private final int    SEGMENTS = 25;
	private final int    SUB_SEGMENTS = 5;

	private char [] full_ruler;
	private char [] condense_ruler;	
        
        private String sampleSeq;
        private String sampleHmmSeq;



	/**
	 * Constructor declaration
	 *
	 *
	 * @see
	 */
	public MSAParser() {

	}



	/**
	 * Method declaration
	 *
	 * @param msaInfo
	 *
	 * @return
	 *
	 * @see
	 */
    public void parseSeqs(String[] seqInfo) {

        seq_length = 0;
        List<GeneNode> nodes = new ArrayList<GeneNode>();

        if ((null == seqInfo) || (0 == seqInfo.length)) {
            return;
        }

        PaintManager manager = PaintManager.inst();

        for (int line = 0; line < seqInfo.length; line++) {
            if ((seqInfo[line].startsWith(PREFIX_SEQ_START))
                    && (line + 1 < seqInfo.length)
                    && (!seqInfo[line + 1].startsWith(PREFIX_SEQ_START))) {

                String paint_id = seqInfo[line].replaceFirst(PREFIX_SEQ_START, Constant.STR_EMPTY);

                GeneNode node = manager.getGeneByPaintId(paint_id);
                if (node == null) {
                    log.error("Unable to get gene " + paint_id + " for MSA data");

                    continue;
                }
                nodes.add(node);
                // Check for case where sequence does not end in current line.
                int start = line + 1;
                StringBuffer sb = new StringBuffer();

                while (start < seqInfo.length) {
                    if (null != seqInfo[start]) {
                        if (true == seqInfo[start].startsWith(PREFIX_SEQ_START)) {
                            break;
                        }
                        sb.append(seqInfo[start].trim());
                        start++;
                        line++;
                    }
                }
                String seq = sb.toString();
                node.setSequence(seq);
                StringBuffer hmmBuf = new StringBuffer();
                for (int i = 0; i < seq.length(); i++) {
                    char c = seq.charAt(i);
                    if (((c >= 'A') && (c <= 'Z')) || (c == '-')) {
                        hmmBuf.append(c);
                    }
                }
                node.setHMMSeq(hmmBuf.toString());
                this.sampleHmmSeq = node.getHMMSeq();

                // Get the maximum sequence length
                if (seq_length != 0 && seq_length != seq.length()) {
                    log.debug("The length of the MSA sequences are not the same");
                }
                if (seq_length < seq.length()) {
                    seq_length = seq.length();
                    this.sampleSeq = seq;
                }
            }
        }
        full_ruler = setRuler(seq_length);
        condense_ruler = setCondensedSequences(nodes);
        if (seq_length < SEGMENTS) {
            condense_ruler = "Sequence".toCharArray();
        }
        return;
    }

	private char [] setRuler(int seqMaxLen) {
		char [] ruler;
		if (seqMaxLen < SEGMENTS){
			ruler = "Sequence".toCharArray();
		}
		else {
			ruler = new char [seqMaxLen];
			for (int i = 0; i < seqMaxLen; i++){
				if (0 == (i + 1) % SEGMENTS){
					String  s = Integer.toString(i + 1);
					for (int j = 0; j < s.length(); j++) {
						ruler[i - s.length() + j] = s.charAt(j);
					}
					ruler[i] = '|';
				}
				else {
					if (0 == (i + 1) % SUB_SEGMENTS) {
						ruler[i] = '\'';
					}
					else {
						ruler[i] = ' ';
					}
				}
			}
		}
		return ruler;
	}

	protected int getSeqLength(boolean uncondensed) {
		if (uncondensed)
			return seq_length;
		else
			return hmm_length;
	}

	protected char [] getRuler(boolean uncondensed) {
		if (uncondensed)
			return full_ruler;
		else
			return condense_ruler;
	}

//	protected boolean parseWts(String[] wtsInfo) {
//		if (wtsInfo != null && wtsInfo.length > 0) {
//			String[]  tmpWts = wtsInfo;
//			// Ignore first two lines
//			for (int i = 2; i < tmpWts.length; i++){
//				String  seqWt[] = Utils.tokenize(tmpWts[i], Constant.STR_SPACE);
//				GeneNode gene = PantherParseUtil.inst().findThatNode(seqWt[0]);
//				if (gene != null)
//					gene.setSequenceWt((new Double(seqWt[1]).doubleValue()));
//				else
//					log.warn ("Unable to parse ID from wts row " + seqWt[0]);
//			}
//			return true;
//		} 
//		else {
//			return false;
//		}
//	}
        
        private char[] setCondensedSequences(List<GeneNode> nodes) {
            GeneNode node = nodes.get(0);
            String sequence = node.getSequence();
            if (null == sequence) {
                return null;
            }
            int seqLen = sequence.length();
            if (seqLen != seq_length) {
                return null;
            }        
            matchPosList = new ArrayList<Boolean>(sequence.length());
            matchPosIndex = new ArrayList<Integer>();
            for (int i = 0; i < seq_length; i++) {
                char  c = sequence.charAt(i);
                if (((c >= 'A') && (c <= 'Z')) || (c == '-')) {
                    matchPosList.add(true);
                    matchPosIndex.add(i);
                }
                else {
                    matchPosList.add(false);
                }
            }
            hmm_length = matchPosIndex.size();
            
            StringBuffer ruler = new StringBuffer();
            int counter = 0;
            for (int i = 0; i < matchPosList.size();i++) {
                if (false == matchPosList.get(i)) {
                    continue;
                }
                counter++;

                if (i > 0 && counter % 10 == 0) {
                   ruler.append("|");
                   String replace = Integer.toString(i) + "|";
                   int length = replace.length();
                   ruler.replace(ruler.length() - length, ruler.length(), replace);               
                }
                else {
                    ruler.append(" ");
                }
            }
            condense_ruler = new char[ruler.length()];
            for (int i = 0; i < ruler.length(); i++) {
                condense_ruler[i] = ruler.charAt(i);
            }
            return condense_ruler;
        }
        
        // DOES NOT WORK - Includes dots, lower case letters
	private char [] setCondensedSequencesOld(List<GeneNode> nodes) {
		int gap_size = 0;
		boolean column_needed;
		StringBuffer ruler = new StringBuffer();

		/* 
		 * Working through the primary sequence one column (amino acid) at a time
		 */
		for (int seq_position = 0; seq_position < seq_length; seq_position++) {
			column_needed = false;
			/*
			 * Find out whether there is any amino acid of significance at this column position
			 * among all the rows 
			 */
			for (int i = 0; i < nodes.size() && !column_needed; i++) {
				GeneNode node = nodes.get(i);
				if (node.getSequence() != null) {
					String sequence = node.getSequence();
					char  c = sequence.charAt(seq_position);
					if (((c >= 'A') && (c <= 'Z')) || (c == '-')) {
						column_needed = true;
					}
				}
			}
			if (column_needed) {
				gap_size = 0;
			} else {
				gap_size++;
			}

			/*
			 * If just reentering good stuff then need to put the starting position in the ruler
			 */
			if (gap_size < 6 ) {
				if (0 == (seq_position + 1) % 10){
					String  s = Integer.toString(seq_position + 1);
					int pos = ruler.length() - s.length();
					ruler.replace(pos, pos + s.length(), s);
					ruler.append('|');
				}
				else if (0 == (seq_position + 1) % 5) {
					ruler.append('\'');
				}
				else {
					ruler.append(' ');
				}
			} else {
				if (ruler.charAt(ruler.length() - 1) != '~') {
					/*
					 * If just leaving good stuff then need to put the ending position in the ruler
					 */
					int pos = ruler.length() - 5;
					boolean digit = true;
					int end_gap = ruler.lastIndexOf("~");
					if (end_gap > 0) {
						int start_gap = end_gap - 1;
						while (start_gap > 0 && ruler.charAt(start_gap) == '~')
							start_gap--;
						if (ruler.charAt(start_gap) != '~')
							start_gap++;
						if (end_gap - start_gap < 4 && (pos - 5) < end_gap)
							ruler.replace(start_gap,  start_gap+5, "~~~~~");
					}
					for (int i = pos - 1; i >= 0 && digit; i--) {
						digit = Character.isDigit(ruler.charAt(i));
						if (digit) {
							ruler.setCharAt(i, ' ');
						}
					}
					ruler.replace(pos,  pos+5, "~~~~~");
				}
			}

			/* 
			 * Now go through every row (gene) and see what they have at this position
			 */
			for (GeneNode node : nodes) {
				switch (gap_size) {
				case 0:
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
					String sequence = node.getSequence();
					char  c = sequence.charAt(seq_position);
					if (node.getHMMSeq() == null) {
						node.setHMMSeq(String.valueOf(c));
					} else {
						node.setHMMSeq(node.getHMMSeq() + c);
					}
					break;
				default:
					// assuming the gap length never goes beyond 10K
					String condensed = node.getHMMSeq();
					//					if (condensed.charAt(ruler.length() - 1) != '.') {
					/*
					 * If just leaving good stuff then need to put the ending position in the ruler
					 */
					int pos = ruler.length() - 5;
					condensed = condensed.substring(0, pos) + ".....";
					node.setHMMSeq(condensed);
					//					}
				}
			}
		}

		hmm_length = 0;
		for (GeneNode node : nodes) {
			if (node.getHMMSeq().length() > hmm_length)
				hmm_length = node.getHMMSeq().length();
		}
		condense_ruler = new char [ruler.length()];
		for (int i = 0; i < ruler.length(); i++)
			condense_ruler[i] = ruler.charAt(i);
		return condense_ruler;
	}

    public ArrayList<Integer> getMatchPosIndex() {
        return matchPosIndex;
    }

    public int getSeq_length() {
        return seq_length;
    }

    public int getHmm_length() {
        return hmm_length;
    }

    public String getSampleSeq() {
        return sampleSeq;
    }

    public String getSampleHmmSeq() {
        return sampleHmmSeq;
    }
}
