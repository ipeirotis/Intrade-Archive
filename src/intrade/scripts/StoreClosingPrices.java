package intrade.scripts;

import intrade.entities.Contract;
import intrade.utils.PMF;

import java.io.IOException;
import java.util.Date;
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
public class StoreClosingPrices extends HttpServlet {

	private HttpServletResponse	r;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		try {
			String t = req.getParameter("time_threshold_minutes");
			Integer threshold = Contract.getTime_threshold_minutes();
			if (t != null) {
				try {
					int thr = Integer.parseInt(t);
					Contract.setTime_threshold_minutes(thr);
				} catch (Exception e) {
					;
				}
			}

			r = resp;

			r.setContentType("text/plain");

			String query = "";

			// First get the expired contracts that have not been archived yet
			// Get the prices and archive them
			query = "SELECT FROM " + Contract.class.getName() + " WHERE expiryDate>0 && archived==false ORDER BY expiryDate";
			print(query);
			PersistenceManager pm = PMF.get().getPersistenceManager();
			Query q = pm.newQuery(query);
			Queue queue = QueueFactory.getDefaultQueue();
			@SuppressWarnings("unchecked")
			List<Contract> results = (List<Contract>) q.execute();
			print("Putting in queue for processing " + results.size() + " contracts.");
			for (Contract c : results) {
				String contractid = c.getId();
				// print("Adding contract "+contractid+
				// " in the queue. Will not archive.");
				queue.add(Builder.withUrl("/processContractClosingPrices").param("contract", contractid).param("archive", "y")
						.param("time_threshold_minutes", threshold.toString()).method(TaskOptions.Method.GET));
				queue.add(Builder.withUrl("/processContractTrades").param("contract", contractid)
						.param("time_threshold_minutes", "1").method(TaskOptions.Method.GET));
			}
			pm.close();

			// Now get the remaining active contracts (that have not been
			// archived yet)
			// Do no archive yet.

			long now = (new Date()).getTime();
			query = "SELECT FROM " + Contract.class.getName() + " WHERE laststoredCSV<" + (now - Contract.time_threshold())
					+ " && expiryDate==0 && archived==false  ORDER BY laststoredCSV";
			print(query);
			pm = PMF.get().getPersistenceManager();
			q = pm.newQuery(query);

			@SuppressWarnings("unchecked")
			List<Contract> results_remaining = (List<Contract>) q.execute();

			print("Putting in queue for processing " + results_remaining.size() + " contracts.");
			for (Contract c : results_remaining) {
				String contractid = c.getId();
				// print("Adding contract "+contractid+
				// " in the queue. Will not archive.");
				queue.add(Builder.withUrl("/processContractClosingPrices").param("contract", contractid).param("archive", "n")
						.param("time_threshold_minutes", threshold.toString()).method(TaskOptions.Method.GET));
				queue.add(Builder.withUrl("/processContractTrades").param("contract", contractid)
						.param("time_threshold_minutes", "1").method(TaskOptions.Method.GET));
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
