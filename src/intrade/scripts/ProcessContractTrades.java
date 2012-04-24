package intrade.scripts;

import intrade.entities.Contract;
import intrade.entities.ContractTrade;
import intrade.utils.PMF;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Blob;

@SuppressWarnings("serial")
public class ProcessContractTrades extends HttpServlet {

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

			String t = req.getParameter("time_threshold_minutes");
			if (t != null) {
				try {
					int i = Integer.parseInt(t);
					Contract.setTrade_time_threshold_minutes(i);
				} catch (Exception e) {
					;
				}
			}

			r = resp;

			r.setContentType("text/plain");

			fetchTradesForContract(contract);

		} catch (com.google.apphosting.api.DeadlineExceededException e) {
			print("Reached execution time limit. Press refresh to continue.");

		}
	}

	private boolean fetchTradesForContract(String contractid) {

		Blob b = storeTradesfile(contractid);
		boolean fetchedTrades = fetchContractTrades(b, contractid);
		if (!fetchedTrades) {
			return true;

		}

		PersistenceManager pm = PMF.get().getPersistenceManager();
		Contract stored_con = pm.getObjectById(Contract.class, Contract.generateKeyFromID(contractid));

		Long now = (new Date()).getTime();
		if (fetchedTrades) {
			stored_con.setLastretrievedTrades(now);
		}

		pm.close();

		return true;
	}

	private Blob storeTradesfile(String id) {

		Contract c = null;

		PersistenceManager pm = PMF.get().getPersistenceManager();

		c = pm.getObjectById(Contract.class, Contract.generateKeyFromID(id));
		c.debug_setResponse(r);
		if (c.fetchTrades()) {
			print("Fetched Trades for contract " + id);
		}
		pm.close();

		return (c != null) ? c.getFileTrades() : null;

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
	 * generates the appropriate ContractTrade objects, which are
	 * then stored in the datastore of the Google App Engine.
	 * 
	 * @param csv
	 *          The Blob object
	 * @param contractid
	 *          The contract ID
	 * @return True if everything was processed without error. False is there
	 *         was an error
	 */
	public boolean fetchContractTrades(Blob csv, String contractid) {

		if (csv == null) {
			print("Could not get blob for contract " + contractid);
			return false;
		}

		String trades = new String(csv.getBytes());

		// We have the file, but there are no CSV data from Intrade. We just
		// stop here...
		if (trades.contains("An error has occurred") || trades.contains("No trades found")) {
			print("No trades for contract " + contractid);
			return true;
		}

		// pm = PMF.get().getPersistenceManager();
		// Contract c = pm.getObjectById(Contract.class,
		// Contract.generateKeyFromID(contractid));
		// pm.close();

		String[] entries = trades.split("\n");

		print("Adding " + (entries.length - 1) + " entries for contract " + contractid);

		Long lastUTC = new Long(-1);
		ArrayList<ContractTrade> prices = new ArrayList<ContractTrade>();
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].startsWith("Date"))
				continue;

			ContractTrade ct = parseContractTradeCSV(entries[i], contractid);
			prices.add(ct);
			print(ct.toString());

			if (ct.getDate() > lastUTC)
				lastUTC = ct.getDate();

		}

		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistentAll(prices);
		Contract con = pm.getObjectById(Contract.class, Contract.generateKeyFromID(contractid));
		con.setLastUTCInsertedTrades(lastUTC);
		pm.close();

		return true;
	}

	private static ContractTrade parseContractTradeCSV(String l, String contract_id) {

		String[] entries = l.split(",");

		Long timeStamp = Long.parseLong(entries[0].trim());
		Double price = Double.parseDouble(entries[2].trim());
		Long volume = Long.parseLong(entries[3].trim());

		ContractTrade ct = new ContractTrade(contract_id, timeStamp, price, volume);

		return ct;
	}

}
