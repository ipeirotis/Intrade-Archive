package intrade.scripts;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;

@SuppressWarnings("serial")
public class TruncateServlet extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp) {

		String cl = req.getParameter("class");
		int l = 200;
		try {
			l = Integer.parseInt(req.getParameter("limit"));
		} catch (Exception e) {
		}

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Query q = new Query(cl);
		ArrayList<Key> keys = new ArrayList<Key>();

		List<Entity> it = datastore.prepare(q).asList(withLimit(l));
		for (Entity entity : it) {
			keys.add(entity.getKey());
		}
		datastore.delete(keys);

		resp.setContentType("text/plain");
		try {
			resp.getWriter().println("Deleting items of type " + cl);
			resp.getWriter().println("Found " + keys.size() + " items");
			resp.getWriter().flush();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}
}
