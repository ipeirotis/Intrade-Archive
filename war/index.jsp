<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="intrade.entities.EventClass" %>
<%@ page import="intrade.utils.PMF" %>

<html><head><title>Intrade Archive</title></head>
  <body>
  <center>
<h1>Intrade Archive</h1>
<a href="last_trades.jsp">Last trades</a>
<%


    PersistenceManager pm = PMF.get().getPersistenceManager();
    String query = "select from " + EventClass.class.getName() + " order by displayOrder";
    List<EventClass> eventclass = (List<EventClass>) pm.newQuery(query).execute();
    if (eventclass.isEmpty()) {
%>
<p>No Categories found!</p>
<%
    } else {
%>
<table border=1 cellpadding=5>
<tr>
<th>Category</th>
<th>Last Fetched</th>
</tr>
<%
   for (EventClass ec: eventclass) {
%>
<tr><td><a href="/eventclass.jsp?eventclass=<%= ec.getId() %>"><%= ec.getName() %></a></td><td><%= java.text.DateFormat.getDateTimeInstance().format(ec.getLastretrieved()) %></td>
</tr>
<%
}
    }
    
    pm.close();
%>
</table>
<p>
For any bug reports or feature requests, contact <a href="http://www.stern.nyu.edu/~panos">Panos Ipeirotis</a>.<br>
The code is available at <a href="http://code.google.com/p/intrade-archive/">Google Code</a>.<br>
<img src="http://code.google.com/appengine/images/appengine-noborder-120x30.gif" 
alt="Powered by Google App Engine" />
</center>

<script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script>
<script type="text/javascript">
try {
var pageTracker = _gat._getTracker("UA-89122-13");
pageTracker._trackPageview();
} catch(err) {}</script>

  </body>
</html>