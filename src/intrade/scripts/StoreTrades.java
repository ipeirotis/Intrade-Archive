package intrade.scripts;

import intrade.entities.Contract;
import intrade.utils.PMF;

import java.io.IOException;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;

@SuppressWarnings("serial")
public class StoreTrades extends HttpServlet {

	private HttpServletResponse	r;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		try {
			String t = req.getParameter("time_threshold_minutes");
			// Integer threshold = Contract.getTrade_time_threshold_minutes();
			if (t != null) {
				try {
					int thr = Integer.parseInt(t);
					Contract.setTrade_time_threshold_minutes(thr);
				} catch (Exception e) {
					;
				}
			}

			r = resp;

			r.setContentType("text/plain");

			String query = "";

			// First get the expired contracts that have not been archived yet
			// Get the prices and archive them
			query = "SELECT FROM " + Contract.class.getName() + " WHERE archived==false ORDER BY lastretrievedTrades";
			print(query);
			PersistenceManager pm = PMF.get().getPersistenceManager();
			Query q = pm.newQuery(query);
			// q.setRange(0, 2000);
			Queue queue = QueueFactory.getDefaultQueue();
			@SuppressWarnings("unchecked")
			List<Contract> results = (List<Contract>) q.execute();
			print("Putting in queue for processing " + results.size() + " contracts.");
			for (Contract c : results) {
				String contractid = c.getId();
				// print("Adding contract "+contractid+
				// " in the queue. Will not archive.");
				queue.add(Builder.withUrl("/processContractTrades").param("contract", contractid)
						.param("time_threshold_minutes", "" + Contract.getTrade_time_threshold_minutes())
						.method(TaskOptions.Method.GET));
			}
			pm.close();

			print("Done!");

		} catch (com.google.apphosting.api.DeadlineExceededException e) {
			print("Reached execution time limit. Press refresh to continue.");

		}
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

}
