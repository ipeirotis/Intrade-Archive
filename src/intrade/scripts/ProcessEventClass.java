package intrade.scripts;

import intrade.entities.EventClass;
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
public class ProcessEventClass extends HttpServlet {

	public static String url = FetchMarketOverview.url;

	public static int time_threshold_minutes = 180;

	private static int time_threshold() {
		return time_threshold_minutes * 60 * 1000;
	}

	private HttpServletResponse r;

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		doGet(req, resp);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		PersistenceManager pm = null;
		try {
			this.r = resp;

			resp.setContentType("text/plain");

			String t = req.getParameter("time_threshold_minutes");
			if (t != null) {
				try {
					int i = Integer.parseInt(t);
					ProcessEventClass.time_threshold_minutes = i;
				} catch (Exception e) {
					;
				}
			}

			String eventclass = req.getParameter("eventclass");
			if (eventclass != null) {
				resp.getWriter()
						.println("Processing event class " + eventclass);
			} else {
				resp.getWriter().println("No class specified");
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
				m = pm.getObjectById(MarketXML.class,
						MarketXML.generateKeyFromID(url));
			} catch (Exception e) {
				m = null;
			}
			if (m != null) {
				System.out.println(m.getURL());
				Document d = m.getXML();
				NodeList n = d.getElementsByTagName("EventClass");
				for (int i = 0; i < n.getLength(); i++) {
					Node nd = n.item(i);
					String event_id = nd.getAttributes().getNamedItem("id")
							.getNodeValue();

					if (!event_id.equals(eventclass)) {
						continue;
					}
					storeEventClass(nd);
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

	private long lastRetrieved_eventclass(String event_id) {
		EventClass eventclass = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			eventclass = pm.getObjectById(EventClass.class,
					EventClass.generateKeyFromID(event_id));
		} catch (Exception e) {
			eventclass = null;
		}
		pm.close();
		return (eventclass == null) ? 0 : eventclass.getLastretrieved();
	}

	private void storeEventClass(Node nd) {

		String eventclass_id = nd.getAttributes().getNamedItem("id")
				.getNodeValue();

		long now = (new Date()).getTime();

		long lastretrieval = lastRetrieved_eventclass(eventclass_id);
		if (now - lastretrieval < time_threshold()) {
			print("Event Class:" + eventclass_id
					+ " is cached and last fetched at "
					+ DateFormat.getDateTimeInstance().format(lastretrieval));
			return;
		}

		String event_name = "";
		int event_displayorder = 0;
		NodeList nl_eventclass = nd.getChildNodes();

		List<Node> groups = new ArrayList<Node>();
		for (int j = 0; j < nl_eventclass.getLength(); j++) {
			Node node = nl_eventclass.item(j);
			String nd_name = node.getNodeName();
			if (nd_name.equals("name")) {
				event_name = node.getTextContent();
			} else if (nd_name.equals("displayOrder")) {
				event_displayorder = Integer.parseInt(node.getTextContent());
			} else if (nd_name.equals("EventGroup")) {
				groups.add(node);
			}
		}

		Queue queue = QueueFactory.getDefaultQueue();
		for (Node group : groups) {

			String group_id = group.getAttributes().getNamedItem("id")
					.getNodeValue();
			queue.add(Builder.withUrl("/processEventGroup")
					.param("eventgroup", group_id)
					.param("eventclass", eventclass_id)
					.method(TaskOptions.Method.GET));

		}

		EventClass ec = new EventClass(eventclass_id, event_name,
				event_displayorder);
		ec.setLastretrieved(now);
		print("Storing:" + ec.toString());

		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(ec);
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
