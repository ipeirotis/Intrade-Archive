package intrade.scripts;

import intrade.entities.EventGroup;
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

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;

@SuppressWarnings("serial")
public class ProcessEventGroup extends HttpServlet {

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
					ProcessEventGroup.time_threshold_minutes = i;
				} catch (Exception e) {
					;
				}
			}

			String eventclass = req.getParameter("eventclass");
			if (eventclass != null) {
				resp.getWriter().println("Processing class " + eventclass);
			} else {
				resp.getWriter().println("No eventclass given");
				return;
			}

			String eventgroup = req.getParameter("eventgroup");
			{
				resp.getWriter().println("Processing group " + eventgroup);
			}
			// Do not process the financial contracts for Dow Jones. We do not need
			// prediction markets for financial events. They are too many in any case and add needless load
			if (eventgroup.equals("4409")) { // 4409 is the Dow Jones code on Intrade
				resp.getWriter().println("We skip the Dow Jones contracts.");
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
				// System.out.println(m.getURL());
				Document d = m.getXML();
				NodeList n = d.getElementsByTagName("EventGroup");
				for (int i = 0; i < n.getLength(); i++) {
					Node nd = n.item(i);

					String group_id = nd.getAttributes().getNamedItem("id").getNodeValue();
					if (!group_id.equals(eventgroup))
						continue;

					storeEventGroup(nd, eventclass);
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

	private long lastRetrieved_group(String group_id) {

		EventGroup eventgroup = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			eventgroup = pm.getObjectById(EventGroup.class, EventGroup.generateKeyFromID(group_id));
		} catch (Exception e) {
			eventgroup = null;
		}
		pm.close();
		return (eventgroup == null) ? 0 : eventgroup.getLastretrieved();
	}

	/*
	 * private long lastRetrieved_event(String event_id) { Event event = null;
	 * 
	 * PersistenceManager pm = PMF.get().getPersistenceManager(); try { event =
	 * pm.getObjectById(Event.class, Event.generateKeyFromID(event_id)); } catch
	 * (Exception e) { event = null; } pm.close(); return (event == null) ? 0 :
	 * event.getLastretrieved(); }
	 */

	private void storeEventGroup(Node eventGroup, String parentClassId) {

		String group_id = eventGroup.getAttributes().getNamedItem("id").getNodeValue();

		long now = (new Date()).getTime();
		long lastretrieval = lastRetrieved_group(group_id);
		if (now - lastretrieval < time_threshold()) {
			print("Event Group:" + group_id + " is cached and last fetched at "
					+ DateFormat.getDateTimeInstance().format(lastretrieval));
			return;
		}

		String group_name = "";
		String group_displayorder = "";
		NodeList nl_eventclass = eventGroup.getChildNodes();

		List<Node> events = new ArrayList<Node>();
		for (int j = 0; j < nl_eventclass.getLength(); j++) {
			Node nd = nl_eventclass.item(j);
			String nd_name = nd.getNodeName();
			if (nd_name.equals("name")) {
				group_name = nd.getTextContent();
			} else if (nd_name.equals("displayOrder")) {
				group_displayorder = nd.getTextContent();
			} else if (nd_name.equals("Event")) {
				events.add(nd);
			}
		}

		Queue queue = QueueFactory.getDefaultQueue();
		for (Node event : events) {
			String event_id = event.getAttributes().getNamedItem("id").getNodeValue();
			queue.add(Builder.withUrl("/processEvent").method(TaskOptions.Method.GET).param("event", event_id));

		}

		EventGroup eg = new EventGroup(group_id, group_name, group_displayorder, parentClassId);
		eg.setLastretrieved(now);
		print("Storing:" + eg.toString());

		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(eg);
		pm.close();

		return;

	}

	private void print(String message) {

		try {
			r.getWriter().println(message);
			r.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
