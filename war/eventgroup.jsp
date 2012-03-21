<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="intrade.entities.EventClass" %>
<%@ page import="intrade.entities.EventGroup" %>
<%@ page import="intrade.entities.Event" %>
<%@ page import="intrade.utils.PMF" %>

<html><head><title>Intrade Archive</title></head>
  <body>
  <center>
<h1>Intrade Archive</h1>

<%
String gid = request.getParameter("group");

    PersistenceManager pm = PMF.get().getPersistenceManager();
    String query = "select from " + Event.class.getName() + " where groupId==\""+gid+"\" order by displayOrder";
    List<Event> event = (List<Event>) pm.newQuery(query).execute();
    if (event.isEmpty()) {
%>
<p>No groups found!</p>
<%
    } else {
%>
<table border=1 cellpadding=5>
<tr>
<th>Event</th>
<th>Description</th>
<th>Event Starts</th>
<th>Event Ends</th>
<th>Last Fetched</th>
</tr>
<%
   for (Event ev: event) {
%>
<tr>
<td><a href="/event.jsp?event=<%= ev.getId() %>"><%= ev.getName() %></a></td>
<td><%= ev.getDescription() %></a></td>
<td><%= java.text.DateFormat.getDateTimeInstance().format(ev.getStartDate()) %></td>
<td><%= java.text.DateFormat.getDateTimeInstance().format(ev.getEndDate()) %></td>
<td><%= java.text.DateFormat.getDateTimeInstance().format(ev.getLastretrieved()) %></td>
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