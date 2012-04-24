package intrade.scripts;

import intrade.entities.Contract;
import intrade.entities.Event;
import intrade.entities.MarketXML;
import intrade.utils.PMF;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("serial")
public class ProcessEvent extends HttpServlet {

	public static String	url											= FetchMarketOverview.url;

	public static int			time_threshold_minutes	= 180;

	private static int time_threshold() {

		return time_threshold_minutes * 60 * 1000;
	}

	private HttpServletResponse	r;

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		doGet(req, resp);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		PersistenceManager pm = null;
		try {
			this.r = resp;

			resp.setContentType("text/plain");

			String t = req.getParameter("time_threshold_minutes");
			if (t != null) {
				try {
					int i = Integer.parseInt(t);
					ProcessEvent.time_threshold_minutes = i;
				} catch (Exception e) {
					;
				}
			}

			String event = req.getParameter("event");
			if (event != null) {
				resp.getWriter().println("Processing event " + event);
			} else {
				return;
			}

			String u = req.getParameter("url");
			if (u != null) {
				try {
					URL param = new URL(u);
					url = param.toString();
				} catch (Exception e) {

				}
			}

			pm = PMF.get().getPersistenceManager();

			MarketXML m = null;
			try {
				m = pm.getObjectById(MarketXML.class, MarketXML.generateKeyFromID(url));
			} catch (Exception e) {
				m = null;
			}
			if (m != null) {
				System.out.println(m.getURL());
				Document d = m.getXML();
				NodeList n = d.getElementsByTagName("Event");
				for (int i = 0; i < n.getLength(); i++) {
					Node nd = n.item(i);
					String event_id = nd.getAttributes().getNamedItem("id").getNodeValue();
					if (!event_id.equals(event))
						continue;
					storeEvent(nd);
				}
			} else {
				// ... no results ...
			}

			pm.close();

		} catch (com.google.apphosting.api.DeadlineExceededException e) {
			print("Reached execution time limit. Press refresh to continue.");

		} finally {
			if (pm != null && !pm.isClosed()) {
				pm.close();
			}
		}

	}

	private long lastRetrieved_event(String event_id) {

		Event event = null;

		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			event = pm.getObjectById(Event.class, Event.generateKeyFromID(event_id));
		} catch (Exception e) {
			event = null;
		}
		pm.close();
		return (event == null) ? 0 : event.getLastretrieved();
	}

	private Contract parseContract(Node contract, String eventid, Long start, Long end) {

		String contract_id = contract.getAttributes().getNamedItem("id").getNodeValue();
		String contract_ccy = contract.getAttributes().getNamedItem("ccy").getNodeValue();
		String contract_inRunning = contract.getAttributes().getNamedItem("inRunning").getNodeValue();
		String contract_state = contract.getAttributes().getNamedItem("state").getNodeValue();
		String contract_tickSize = contract.getAttributes().getNamedItem("tickSize").getNodeValue();
		String contract_tickValue = contract.getAttributes().getNamedItem("tickValue").getNodeValue();
		String contract_type = contract.getAttributes().getNamedItem("type").getNodeValue();

		String contract_name = "";
		String contract_symbol = "";
		String contract_totalVolume = "";
		String contract_expiryDate = null;
		String contract_expiryPrice = null;

		NodeList nl_eventclass = contract.getChildNodes();

		for (int j = 0; j < nl_eventclass.getLength(); j++) {
			Node nd = nl_eventclass.item(j);
			String nd_name = nd.getNodeName();
			if (nd_name.equals("name")) {
				contract_name = nd.getTextContent();
			} else if (nd_name.equals("symbol")) {
				contract_symbol = nd.getTextContent();
			} else if (nd_name.equals("totalVolume")) {
				contract_totalVolume = nd.getTextContent();
			} else if (nd_name.equals("date")) {
				if (nd.getAttributes().getNamedItem("name").getNodeValue().equals("expiryDate")) {

					contract_expiryDate = nd.getAttributes().getNamedItem("val").getNodeValue();
				} else {
					System.err.println("Found unexpected date type:" + nd.getAttributes().getNamedItem("name").getNodeValue());
				}
			} else if (nd_name.equals("expiryPrice")) {
				contract_expiryPrice = nd.getTextContent();
			}
		}

		Contract con = new Contract(contract_id, eventid, contract_name, contract_symbol, contract_totalVolume,
				contract_ccy, contract_inRunning, contract_state, contract_tickSize, contract_tickValue, contract_type, start,
				end);

		if (contract_expiryDate != null) {
			con.setExpiryDate(Long.parseLong(contract_expiryDate));
		}

		if (contract_expiryPrice != null) {
			con.setExpiryPrice(Double.parseDouble(contract_expiryPrice));
		}

		return con;
	}

	private long storeContracts(Node contract, String eventid, Long start, Long end) {

		long now = (new Date()).getTime();

		Contract con = parseContract(contract, eventid, start, end);

		PersistenceManager pm = PMF.get().getPersistenceManager();
		Contract stored_contract = null;

		try {
			stored_contract = pm.getObjectById(Contract.class, Contract.generateKeyFromID(con.getId()));
		} catch (Exception e) {
			stored_contract = null;
		}

		if (stored_contract == null) {
			con.setLastprocessed(now);
			pm.makePersistent(con);
		} else {
			updateStoredContract(stored_contract, con);
		}
		pm.close();

		return now;

	}

	private void print(String message) {

		try {
			r.getWriter().println(message);
			r.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private long storeEvent(Node nd) {

		String event_id = nd.getAttributes().getNamedItem("id").getNodeValue();

		long now = (new Date()).getTime();
		long lastretrieval = lastRetrieved_event(event_id);
		if (now - lastretrieval < time_threshold()) {
			print("Event:" + event_id + " is cached and last fetched at "
					+ DateFormat.getDateTimeInstance().format(lastretrieval));
			return lastretrieval;
		}

		String event_gid = nd.getAttributes().getNamedItem("groupID").getNodeValue();
		String event_start = nd.getAttributes().getNamedItem("StartDate").getNodeValue();
		String event_end = nd.getAttributes().getNamedItem("EndDate").getNodeValue();

		String event_name = "";
		String event_displayorder = "";
		String event_description = "";
		NodeList nl_event = nd.getChildNodes();

		List<Node> contracts = new ArrayList<Node>();
		for (int j = 0; j < nl_event.getLength(); j++) {
			Node node = nl_event.item(j);
			String nd_name = node.getNodeName();
			if (nd_name.equals("name")) {
				event_name = node.getTextContent();
			} else if (nd_name.equals("displayOrder")) {
				event_displayorder = node.getTextContent();
			} else if (nd_name.equals("Description")) {
				event_description = node.getTextContent();
			} else if (nd_name.equals("contract")) {
				contracts.add(node);
			}
		}

		Long start = Long.parseLong(event_start);
		Long end = Long.parseLong(event_end);
		long oldest_updatetime = (new Date()).getTime();
		for (Node contract : contracts) {
			long updated = storeContracts(contract, event_id, start, end);
			if (updated < oldest_updatetime) {
				oldest_updatetime = updated;
			}
		}

		Event ev = new Event(event_id, event_gid, event_name, event_displayorder, event_description, start, end);
		ev.setLastretrieved(oldest_updatetime);
		print("Storing:" + ev.toString());

		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(ev);
		pm.close();

		return oldest_updatetime;

	}

	private boolean updateStoredContract(Contract stored, Contract retrieved) {

		// private Boolean archived;

		// EventID should be the same
		stored.setEventid(retrieved.getEventid());

		// The name should be the same
		stored.setName(retrieved.getName());

		// CCY (currency) should be the same
		stored.setCcy(retrieved.getCcy());

		// startDate and endDate should be the same
		stored.setStartDate(retrieved.getStartDate());
		stored.setEndDate(retrieved.getEndDate());
		stored.setSymbol(retrieved.getSymbol());

		stored.setTickSize(retrieved.getTickSize());
		stored.setTickValue(retrieved.getTickValue());
		stored.setType(retrieved.getType());

		// Expiry date and price should most probably be updated
		// TODO: for contracts with expiry date, retrieve all transaction data
		// and marked as "archived"
		stored.setExpiryDate(retrieved.getExpiryDate());
		stored.setExpiryPrice(retrieved.getExpiryPrice());

		// Status of the market should be updated
		stored.setInRunning(retrieved.getInRunning());
		stored.setState(retrieved.getState());
		stored.setTotalVolume(retrieved.getTotalVolume());

		// The contract was last modified now...
		stored.setLastprocessed((new Date()).getTime());

		return true;
	}

}
