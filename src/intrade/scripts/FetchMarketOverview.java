package intrade.scripts;

import intrade.entities.MarketXML;
import intrade.utils.PMF;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;

@SuppressWarnings("serial")
public class FetchMarketOverview extends HttpServlet {

	public static String url = "http://pages.stern.nyu.edu/~panos/intrade/intrade.xml";

	// public static String url = "http://pages.stern.nyu.edu/~panos/test2.xml";

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		resp.setContentType("text/plain");

		// DEBUG
		// MarketXML.time_threshold_minutes = 15;

		String t = req.getParameter("time_threshold_minutes");
		if (t != null) {
			try {
				int i = Integer.parseInt(t);
				MarketXML.time_threshold_minutes = i;
			} catch (Exception e) {

			}
		}

		String u = req.getParameter("url");
		if (u != null) {
			try {
				URL param = new URL(u);
				url = param.toString();
			} catch (Exception e) {

			}
		}

		PersistenceManager pm = PMF.get().getPersistenceManager();

		MarketXML m = null;
		Key k = KeyFactory.createKey(MarketXML.class.getSimpleName(), url);
		try {
			// Check if the file is fetched already

			resp.getWriter().println("Checking if URL is fetched: " + url);
			m = pm.getObjectById(MarketXML.class, k);
			resp.getWriter().println("It is fetched...");
			resp.getWriter().println(
					"----------------------------------------------------");
			resp.getWriter().println(
					"Current timestamp:"
							+ DateFormat.getDateTimeInstance().format(
									m.getTimestamp()));
			resp.getWriter().println(
					"Last retrieved:"
							+ DateFormat.getDateTimeInstance().format(
									m.getLastretrieved()));
			resp.getWriter().println(
					"----------------------------------------------------");
			Long last = m.getLastretrieved();

			// It is fetched. Refresh it.
			resp.getWriter().print("Refreshing...");
			resp.getWriter().flush();
			m.refresh();

			if (m.getLastretrieved() == last) {
				resp.getWriter()
						.println(
								"Not enough time passed since last retrieval. Current limit is "
										+ MarketXML.time_threshold_minutes
										+ " minutes");
			} else {
				resp.getWriter().println("Got new copy");
			}
			resp.getWriter().println(
					"----------------------------------------------------");
			resp.getWriter().println(
					"New timestamp:"
							+ DateFormat.getDateTimeInstance().format(
									m.getTimestamp()));
			resp.getWriter().println(
					"New retrieval:"
							+ DateFormat.getDateTimeInstance().format(
									m.getLastretrieved()));
			resp.getWriter().println(
					"----------------------------------------------------");

		} catch (Exception e) {
			// URL not fetched before
			// Create object

			resp.getWriter().println("It is not fetched...fetching");
			resp.getWriter().flush();
			m = new MarketXML(url);

			System.out.println("Start fetching");

			m.fetch();

			resp.getWriter().println(
					"New timestamp:"
							+ DateFormat.getDateTimeInstance().format(
									m.getTimestamp()));
			resp.getWriter().println("Storing...");
			resp.getWriter().flush();
			pm.makePersistent(m);

		}

		pm.close();

		Document d = m.getXML();
		Queue queue = QueueFactory.getDefaultQueue();
		NodeList n = d.getElementsByTagName("EventClass");
		for (int i = 0; i < n.getLength(); i++) {

			Node nd = n.item(i);
			String event_id = nd.getAttributes().getNamedItem("id")
					.getNodeValue();
			resp.getWriter().println("Adding in queue EventClass:" + event_id);
			queue.add(Builder.withUrl("/processEventClass")
					.method(TaskOptions.Method.GET)
					.param("eventclass", event_id));
		}

		resp.getWriter().println("Done!");

	}

}
