package intrade.scripts;

import intrade.entities.Contract;
import intrade.entities.ContractClosingPriceCSV;
import intrade.entities.ContractClosingPriceXML;
import intrade.utils.PMF;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Node;

import com.google.appengine.api.datastore.Blob;

@SuppressWarnings("serial")
public class ProcessContractClosingPrices extends HttpServlet {

	private HttpServletResponse	r;

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		doGet(req, resp);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		try {
			String contract = req.getParameter("contract");
			if (contract != null) {
				resp.getWriter().println("Storing closing prices for contract:" + contract);
			} else {
				return;
			}

			boolean arc = false;
			String archive = req.getParameter("archive");
			if (archive == null) {
				return;
			} else if (archive.toLowerCase().equals("y")) {
				arc = true;
				resp.getWriter().println("The contract should be archived");
			} else if (archive.toLowerCase().equals("n")) {
				arc = false;
				resp.getWriter().println("The contract should not be archived");
			} else {
				return;
			}

			String t = req.getParameter("time_threshold_minutes");
			if (t != null) {
				try {
					int i = Integer.parseInt(t);
					Contract.setTime_threshold_minutes(i);
				} catch (Exception e) {
					;
				}
			}

			r = resp;

			r.setContentType("text/plain");

			fetchClosingPricesForContract(contract, arc);

		} catch (com.google.apphosting.api.DeadlineExceededException e) {
			print("Reached execution time limit. Press refresh to continue.");

		}
	}

	private boolean fetchClosingPricesForContract(String contractid, boolean setArchive) {

		Blob b = storeCSVfile(contractid);
		boolean fetchedCSV = fetchContractClosingPricesCSV(b, contractid);
		if (!fetchedCSV) {
			// If we should not archive this, and there was a problem with the
			// CSV do not do anything else
			if (!setArchive)
				return true;

			// If we should archive the contract and we cannot fetch the CSV,
			// then we will proceed below
			// where we will archive the contract, without updating the
			// lastFetchedCSV

		}

		PersistenceManager pm = PMF.get().getPersistenceManager();
		Contract stored_con = pm.getObjectById(Contract.class, Contract.generateKeyFromID(contractid));
		if (setArchive) {
			stored_con.setArchived(true);
		} else {
			stored_con.setArchived(false);
		}
		Long now = (new Date()).getTime();
		if (fetchedCSV) {
			stored_con.setLaststoredCSV(now);
		}
		if (setArchive) {
			print("Processed and archived contract:" + stored_con.toString());
		} else {
			print("Processed (but not archived) contract:" + stored_con.toString());
		}
		pm.close();

		return true;
	}

	private Blob storeCSVfile(String id) {

		Contract c = null;

		PersistenceManager pm = PMF.get().getPersistenceManager();

		c = pm.getObjectById(Contract.class, Contract.generateKeyFromID(id));
		c.debug_setResponse(r);
		if (c.fetchCSV()) {
			print("Fetched CSV for contract " + id);
		}
		pm.close();

		return (c != null) ? c.getFileCSV() : null;

	}

	private void print(String message) {

		try {
			r.getWriter().println(message);
			r.getWriter().flush();
			r.flushBuffer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This routine parses the Blob that contains the actual CSV file and
	 * generates the appropriate ContractClosingPricesCSV objects, which are
	 * then stored in the datastore of the Google App Engine.
	 * 
	 * Since big CSV files may take too much time to process, we keep the last
	 * line processed and we process only lines after that
	 * 
	 * @param csv
	 *          The Blob object
	 * @param contractid
	 *          The contract ID
	 * @return True if everything was processed without error. False is there
	 *         was an error
	 */
	public boolean fetchContractClosingPricesCSV(Blob csv, String contractid) {

		if (csv == null) {
			print("Could not get blob for contract " + contractid);
			return false;
		}

		String contractsCP = new String(csv.getBytes());

		// We have the file, but there are no CSV data from Intrade. We just
		// stop here...
		if (contractsCP.contains("There is no data for download."))
			return true;

		PersistenceManager pm = PMF.get().getPersistenceManager();
		Contract c = pm.getObjectById(Contract.class, Contract.generateKeyFromID(contractid));
		Integer limit = c.getLastLineInsertedCSV();
		pm.close();

		if (limit == null)
			limit = 0;

		String[] entries = contractsCP.split("\n");

		if (entries.length > 2)
			print("Adding " + (entries.length - 1) + "entries, starting from " + limit + ", for contract " + contractid);

		ArrayList<ContractClosingPriceCSV> prices = new ArrayList<ContractClosingPriceCSV>();
		for (int i = limit; i < entries.length; i++) {
			if (entries[i].startsWith("Date"))
				continue;

			ContractClosingPriceCSV cp = parseContractClosingPriceCSV(entries[i], contractid);
			prices.add(cp);

			// Every 50 added prices, persist, update contract, and flush...

			if (i % 50 == 0) {
				pm = PMF.get().getPersistenceManager();
				pm.makePersistentAll(prices);
				Contract con = pm.getObjectById(Contract.class, Contract.generateKeyFromID(contractid));
				con.setLastLineInsertedCSV(i);
				pm.close();
				prices = new ArrayList<ContractClosingPriceCSV>();

			}

		}

		pm = PMF.get().getPersistenceManager();
		pm.makePersistentAll(prices);
		Contract con = pm.getObjectById(Contract.class, Contract.generateKeyFromID(contractid));
		con.setLastLineInsertedCSV(entries.length - 1);
		pm.close();

		return true;
	}

	/*
	 * public void fetchContractClosingPricesXML() throws FileNotFoundException
	 * {
	 * 
	 * if (this.fileXML == null) { if (!fetchXML()) return; }
	 * 
	 * Document d = Utilities.getXMLFromString(this.fileXML.getBytes());
	 * NodeList n = d.getElementsByTagName("cp"); for (int i = 0; i <
	 * n.getLength(); i++) { ContractClosingPriceXML cp =
	 * parseContractClosingPriceXML(n.item(i), this.id); if (cp != null) {
	 * this.addClosingPriceXML(cp); //
	 * System.out.println("Added:"+cp.toString()); }
	 * 
	 * } this.lastFetchedClosingXML = (new Date()).getTime(); }
	 */
	private static ContractClosingPriceCSV parseContractClosingPriceCSV(String l, String contract_id) {

		StreamTokenizer st = new StreamTokenizer(new StringReader(l));

		Date date = null;

		try {
			st.nextToken();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {

			String date_str = st.sval;
			if (date_str != null) {
				date = DateFormat.getDateInstance(DateFormat.MEDIUM).parse(date_str);
			} else {
				return null;
			}
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}

		try {
			st.nextToken();
			st.nextToken();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Double open = st.nval;

		try {
			st.nextToken();
			st.nextToken();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Double low = st.nval;

		try {
			st.nextToken();
			st.nextToken();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Double high = st.nval;

		try {
			st.nextToken();
			st.nextToken();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Double close = st.nval;

		try {
			st.nextToken();
			st.nextToken();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Long volume = (long) st.nval;

		ContractClosingPriceCSV cp = new ContractClosingPriceCSV(contract_id, date.getTime(), open, low, high, close,
				volume);

		return cp;
	}

	@SuppressWarnings("unused")
	private static ContractClosingPriceXML parseContractClosingPriceXML(Node cp, String contract_id) {

		String cp_dt_str = cp.getAttributes().getNamedItem("dt").getNodeValue();
		Long cp_dt = Long.parseLong(cp_dt_str);

		String cp_price_str = cp.getAttributes().getNamedItem("price").getNodeValue();
		Double cp_price;
		try {
			cp_price = Double.parseDouble(cp_price_str);
		} catch (NumberFormatException e) {
			cp_price = null;
		}

		String cp_high_str = cp.getAttributes().getNamedItem("sessionHi").getNodeValue();
		Double cp_high;
		try {
			cp_high = Double.parseDouble(cp_high_str);
		} catch (NumberFormatException e) {
			cp_high = null;
		}

		String cp_low_str = cp.getAttributes().getNamedItem("sessionLo").getNodeValue();
		Double cp_low;
		try {
			cp_low = Double.parseDouble(cp_low_str);
		} catch (NumberFormatException e) {
			cp_low = null;
		}

		ContractClosingPriceXML cpxml = new ContractClosingPriceXML(contract_id, cp_dt, cp_price, cp_low, cp_high);

		return cpxml;

	}

}
