//package org.paint.go;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Set;
//
//import org.apache.log4j.Logger;
//import org.geneontology.db.factory.GOobjectFactory;
//import org.geneontology.db.model.Association;
//import org.geneontology.db.model.DBXref;
//import org.geneontology.db.model.Evidence;
//import org.geneontology.db.model.GeneProduct;
//import org.geneontology.db.model.ProductSeq;
//import org.geneontology.db.model.Sequence;
//import org.geneontology.db.model.Species;
//import org.hibernate.Session;
//import org.paint.datamodel.GeneNode;
//import org.paint.gui.event.EventManager;
//import org.paint.gui.event.ProgressEvent;
//import org.paint.main.PaintManager;
//
//
//public class GeneProductThread extends Thread {
//
//	private static Logger log = Logger.getLogger(GeneProductThread.class);
//
//	private List<String []> go_genes;
//	private String valid;
//	private GOobjectFactory factory;
//
//	public GeneProductThread(GOobjectFactory factory) {
//		valid = null;
//		this.factory = factory;
//	}
//
//	public boolean isValid() {
//		return valid == null;
//	}
//
//	public void setValid(String error) {
//		this.valid = error;
//	}
//
//	public void run() {
//		String progressMessage = "Initializing GO experimental annotations";
//		try {
//			populateGeneProducts(progressMessage);
//		}
//		catch (Exception e) {
//			log.error(e.getMessage());
//			fireProgressChange(progressMessage + ": " + e.getMessage(), -1, ProgressEvent.Status.FAIL);
//			setValid(e.getMessage());
//		}
//	}
//
//	private void populateGeneProducts(String progressMessage) {
//		if (go_genes.size() > 0) {
//			Session session = factory.startSession();
//			fireProgressChange(progressMessage, 0, ProgressEvent.Status.RUNNING);
//			if (!isValid()) {
//				fireProgressChange(progressMessage + ": " + valid, -1, ProgressEvent.Status.FAIL);
//				session.close();
//				return;
//			}
//			PaintManager manager = PaintManager.inst();
//			/*
//			 * This search should pick up the common MOD submitted annotations from the GODB
//			 */
//			int count = pullGeneProductsByDBID(go_genes, session, manager);
//			if (!isValid()) {
//				fireProgressChange(progressMessage + ": " + valid, -1, ProgressEvent.Status.FAIL);
//				session.close();
//				return;
//			}
//			if (go_genes.size() != count) {
//				List<String []> go_again = winnow(go_genes, manager);
//
//				if (go_again.size() > 0) {
//					if (!isValid()) {
//						fireProgressChange(progressMessage + ": " + valid, -1, ProgressEvent.Status.FAIL);
//						session.close();
//						return;
//					}
//					/*
//					 * If the common MOD submitted annotations from the GODB fails,
//					 * then try picking up any submitted annotations from GOA by using the
//					 * Uniprot ID as the key
//					 */
//					count += pullGeneProductsByDBID(go_again, session, manager);
//					if (go_genes.size() != count) {
//						go_again = winnow(go_genes, manager);
//						if (go_again.size() > 0) {
//							if (!isValid()) {
//								fireProgressChange(progressMessage + ": " + valid, -1, ProgressEvent.Status.FAIL);
//								session.close();
//								return;
//							}
//							/*
//							 * If both of the above fail then it is likely that there is an ID synchronization problem
//							 * between the PANTHER DB and the GODB
//							 * Some of these can be picked up by using the sequence ID to identify the correct GP
//							 */
//							count += pullGeneProductsBySeqID(go_again, session, manager);
//						}
//					}
//				}
//			}
//			//			if (go_genes.size() != count) {
//			//				/*
//			//				 * Report what's missing
//			//				 */
//			//				log.debug("Requested " + go_genes.size() + " leaf gene products and retrieved " + count + " from GO-DB");
//			//			}
//			session.close();
//		}
//	}
//	public void interrupt() {
//		setValid("Transfer interrupted");
//		fireProgressChange(null, -1, ProgressEvent.Status.FAIL);
//		super.interrupt();
//	}
//
//	private static void fireProgressChange(String message, int percentageDone, ProgressEvent.Status status) {
//		ProgressEvent event = new ProgressEvent(GeneProductThread.class, message, percentageDone, status);
//		EventManager.inst().fireProgressEvent(event);
//	}
//
//	private int pullGeneProductsByDBID(List<String []> go_genes, Session session, PaintManager manager) {
//		Iterator<GeneProduct> gene_products;
//		gene_products = factory.getGPListByDBXref(go_genes, session);
//		int count = 0;
//		if (gene_products != null) {
//			while (gene_products.hasNext()) {
//				GeneProduct gp = gene_products.next();
//				count++;
//				List <GeneNode> nodes = null;
//				GeneNode node = null;
//				String acc = gp.getDbxref().getAccession();
//				String db = gp.getDbxref().getDb_name();
//				node = manager.getGeneByDbId(db, acc);
//				if (node == null) {
//					nodes = manager.getGenesBySeqId(db, acc);
//					if (nodes == null) {
//						String symbol = gp.getSymbol();
//						node = manager.getGeneByDbId(db, symbol);				
//						if (nodes == null) {
//							node = manager.getGeneByPaintId(acc);
//						}
//					}
//				}
//				if (node != null && nodes == null) {
//					nodes = new ArrayList<GeneNode>();
//					nodes.add(node);
//				}
//				if (nodes == null)
//					log.error("Unable to find matching node for " + db + ':' + acc);
//				else
//					initGP(gp, nodes);
//			}
//		}
//		return count;
//	}
//
//	private int pullGeneProductsBySeqID(List<String []> go_genes, Session session, PaintManager manager) {
//		Iterator<GeneProduct> gene_products;
//		gene_products = factory.getGPListBySeqXref(go_genes, session);
//
//		int count = 0;
//		if (gene_products != null) {
//			while (gene_products.hasNext()) {
//				GeneProduct gp = gene_products.next();
//				count++;
//				List <GeneNode> nodes = null;
//				String acc = gp.getDbxref().getAccession();
//				String db = gp.getDbxref().getDb_name();
//				GeneNode node = manager.getGeneByDbId(db, acc);
//				if (node == null) {
//					nodes = manager.getGenesBySeqId(db, acc);							
//					if (nodes == null) {
//						String symbol = gp.getSymbol();
//						node = manager.getGeneByDbId(db, symbol);
//					}
//				}
//				if (node == null && nodes == null) {
//					Set<ProductSeq> links = gp.getSeqs();
//					for (ProductSeq link : links) {
//						Sequence seq = link.getSeq();
//						Set<DBXref> xrefs = seq.getDbxrefs();
//						for (Iterator<DBXref> it = xrefs.iterator(); it.hasNext() && nodes == null; ) {
//							DBXref xref = it.next();
//							db = xref.getDb_name();
//							acc = xref.getAccession();
//							db = GO_Util.inst().dbNameHack(db);
//
//							if (node == null) {
//								nodes = manager.getGenesBySeqId(db, acc);
//							}
//						}
//					}
//				}
//				if (node != null && nodes == null) {
//					nodes = new ArrayList<GeneNode>();
//					nodes.add(node);
//				}
//				initGP(gp, nodes);
//			}
//		}
//		return count;
//	}
//
//	private void getAssociations(GeneNode node) {
//		final AssociationsTask task = new AssociationsTask();
//		task.setNode(node);
//		try {
//			task.execute();
//		}
//		catch (Exception e) {
//		}
//	}
//
//	private void initGP(GeneProduct gp, List <GeneNode> nodes) {
//		if (nodes == null || (nodes != null && nodes.size() == 0))	{
//			log.debug("unable to find node for " + gp.getDbxref().getDb_name() + ":" + gp.getDbxref().getAccession() + ", " + gp.getSymbol());
//		} else {
//			if (nodes.size() > 1) {
//				log.debug("Got " + nodes.size() + " for " + gp.getSymbol());
//			}
//			getFullgp(gp);
//
//			for (GeneNode n : nodes) {
//				int count = 0;
//				if (n.getGeneProduct() != null) {
//					count = n.getGeneProduct().getAssociations().size();
//				}
//				n.setGeneProduct(gp);
//				/*
//				 * While the gene products are retrieved in a single query
//				 * the associations are retrieved individually
//				 */
//				getAssociations(n);
//				if (count > 0 && count != n.getGeneProduct().getAssociations().size()) {
//					log.debug(n + " had " + count + " associations and now has " + n.getGeneProduct().getAssociations().size());
//				}
//			} 
//		}
//	}
//
//	private void getFullgp(GeneProduct gp) {
//		String acc = gp.getDbxref().getAccession();
//		String db = gp.getDbxref().getDb_name();
//		String symbol = gp.getSymbol();
//		Set<String> syns = gp.getSynonyms();
//		/*
//		 * All three of the following are attempts to
//		 * circumvent hibernate's lazy loading.
//		 */
//		Set<String> non_redundant = new HashSet<String>();
//		for (String syn : syns) {
//			if (!syn.toLowerCase().equals(symbol.toLowerCase()) && !syn.toLowerCase().equals(acc.toLowerCase())) {
//				non_redundant.add(syn);
//			}
//		}
//		gp.setSynonyms(non_redundant);
//
//		Species sp = gp.getSpecies();
//		if (sp != null) {
//			String species = (sp.getGenus() + " " + sp.getSpecies()).trim();
//		}
//		String so = gp.getSO_type().getAcc();
//	}
//
//	private List<String []> winnow(List<String []> gene_ids, PaintManager manager) {
//		List<String []> retry_list = new ArrayList<String []> ();
//		for (Iterator<String []> it = gene_ids.iterator(); it.hasNext(); ) {
//			String [] dbxref = it.next();
//			GeneNode node = manager.getGeneByDbId(dbxref[0], dbxref[1]);
//			if (node != null && node.getGeneProduct() == null) {
//				String [] seqxref = new String[2];
//				seqxref[0] = node.getSeqDB();
//				seqxref[1] = node.getSeqId();
//				retry_list.add(seqxref);
//			}
//		}
//		return retry_list;
//	}
//
//	public void setGeneList(List<String []> go_genes) {
//		this.go_genes = go_genes;
//	}
//}
