package org.paint.util;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

public class SeqMapper {
	private static final String UNIPROT_MAPPER = "http://www.uniprot.org/mapping/?";
	private static final Logger log = Logger.getLogger(SeqMapper.class);

	/** perhaps singleton pattern is overkill here, but what the hell
    it is gonna bring up a gui at some point */
	private static SeqMapper singleton = null;

	public SeqMapper() {
	}

	public static SeqMapper inst() {
		if (singleton == null)
			singleton = new SeqMapper();
		return singleton;
	}

	private String queryMappingService(ParameterNameValue[] params) {
		String seqID = null;
		StringBuilder locationBuilder = new StringBuilder(UNIPROT_MAPPER);
		for (int i = 0; i < params.length; i++)
		{
			if (i > 0)
				locationBuilder.append('&');
			locationBuilder.append(params[i].name).append('=').append(params[i].value);
		}
		String location = locationBuilder.toString();
		try {
			URL url = new URL(location);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			HttpURLConnection.setFollowRedirects(true);
			conn.setDoInput(true);
			conn.connect();

			int status = conn.getResponseCode();
			while (true)
			{
				int wait = 0;
				String header = conn.getHeaderField("Retry-After");
				if (header != null)
					wait = Integer.valueOf(header);
				if (wait == 0)
					break;
				log.info("Waiting (" + wait + ")...");
				conn.disconnect();
				Thread.sleep(wait * 1000);
				conn = (HttpURLConnection) new URL(location).openConnection();
				conn.setDoInput(true);
				conn.connect();
				status = conn.getResponseCode();
			}
			if (status == HttpURLConnection.HTTP_OK)
			{
				InputStream reader = conn.getInputStream();
				URLConnection.guessContentTypeFromStream(reader);
				StringBuilder builder = new StringBuilder();
				int a = 0;
				while ((a = reader.read()) != -1)
				{
					builder.append((char) a);
				}
				seqID = builder.toString();
				if (seqID.endsWith("\n")) {
					seqID = seqID.substring(0, seqID.indexOf('\n'));
				}
				if (seqID.length() == 0)
					log.info("No results for " + location);
			}
			else
				log.fatal("Failed, got " + conn.getResponseMessage() + " for "
						+ location);
			conn.disconnect();
		}
		catch (Exception e) {
			log.error(e.getMessage());
		}
		return seqID;
	}

	public String getSeqID(String db, String db_id) {
		String seqID = null;
		String from;
		if (db_id.startsWith("ENS"))
			from = "ENSEMBL_PRO_ID";
		else if (db.equals("RGD"))
			from = "RGD_ID";
		else if (db.equals("NCBI"))
			from = "EMBL_ID";
		else if (db.equals("WB"))
			from = "WORMBASE_ID";
		else if (db.equals("ENSEMBL"))
			from = "ENSEMBLGENOME_PRO_ID";
		else if (db.equals("UniProtKB"))
			from = "ID";
		else if (db.equals("dictyBase"))
			from = "DICTYBASE_ID";
		else {
			log.error("Unfamiliar db " + db + " will tack on _ID");
			from = db.toUpperCase() + "_ID";
		}
		try {
			ParameterNameValue[] params = new ParameterNameValue[] {
					new ParameterNameValue("from", from),
					new ParameterNameValue("to", "ACC"),
					new ParameterNameValue("format", "list"),
					new ParameterNameValue("query", db_id)
			};
			seqID = queryMappingService(params);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return seqID;
	}

	private static class ParameterNameValue
	{
		private final String name;
		private final String value;

		public ParameterNameValue(String name, String value)
				throws UnsupportedEncodingException
				{
			this.name = URLEncoder.encode(name, "UTF-8");
			this.value = URLEncoder.encode(value, "UTF-8");
				}
	}
}

