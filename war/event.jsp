<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="intrade.entities.EventClass" %>
<%@ page import="intrade.entities.EventGroup" %>
<%@ page import="intrade.entities.Event" %>
<%@ page import="intrade.entities.Contract" %>
<%@ page import="intrade.utils.PMF" %>

<html><head><title>Intrade Archive</title></head>
  <body>
  <center>
<h1>Intrade Archive</h1>


<%
String eid = request.getParameter("event");

    PersistenceManager pm = PMF.get().getPersistenceManager();
    String query = "select from " + Contract.class.getName() + " where eventid==\""+eid+"\"";
    List<Contract> contracts = (List<Contract>) pm.newQuery(query).execute();
    if (contracts.isEmpty()) {
%>
<p>No groups found!</p>
<%
    } else { 
%>
<a href="/visualize_event.jsp?id=<%= eid %>">Chart of contract prices</a>

<table border=1 cellpadding=5>
<tr>
<th>Symbol</th>
<th>Description</th>
<th>Event Starts</th>
<th>Event Ends</th>
<th>Last Fetched Prices</th>
</tr>
<%
   for (Contract c: contracts) {
%>
<tr>
<td><%=c.getSymbol()%> <a href="/contract.jsp?contract=<%= c.getId() %>">Closing Prices</a>, <a href="/trades.jsp?contract=<%= c.getId() %>">Trades</a></td>
<td><%= c.getName() %></a></td>
<td><%= java.text.DateFormat.getDateTimeInstance().format(c.getStartDate()) %></td>
<td><%= java.text.DateFormat.getDateTimeInstance().format(c.getEndDate()) %></td>
<td><%= java.text.DateFormat.getDateTimeInstance().format(c.getLaststoredCSV()) %></td>
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