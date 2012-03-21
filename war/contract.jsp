<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="intrade.entities.EventClass" %>
<%@ page import="intrade.entities.EventGroup" %>
<%@ page import="intrade.entities.Event" %>
<%@ page import="intrade.entities.Contract" %>
<%@ page import="intrade.entities.ContractClosingPriceCSV" %>
<%@ page import="intrade.utils.PMF" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="javax.jdo.Query" %>

<html>
  <body>

<%
String cid = request.getParameter("contract");
%>
<a href="http://data.intrade.com/graphing/jsp/downloadClosingPrice.jsp?contractId=<%=cid %>">Download prices from Intrade.com</a>
<%
    PersistenceManager pm = PMF.get().getPersistenceManager();
    String query = "select from " + ContractClosingPriceCSV.class.getName() + " where contractid==\""+cid+"\" order by date";
    Query q = pm.newQuery(query);
    q.setRange(0, 1000);
    List<ContractClosingPriceCSV> prices = (List<ContractClosingPriceCSV>) q.execute();
    if (prices.isEmpty()) {
%>
<p>No prices found!</p>
<%
    } else {
%>
<center>
<table border=1 cellpadding=5>
<tr>
<th>Date</th>
<th>Open</th>
<th>High</th>
<th>Low</th>
<th>Close</th>
<th>Volume</th>
</tr>
<%
   for (ContractClosingPriceCSV p: prices) {
%>
<tr>
<td><%= DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(p.getDate()) %></a></td>
<td><%= p.getOpen() %></a></td>
<td><%= p.getHigh() %></a></td>
<td><%= p.getLow() %></a></td>
<td><%= p.getClose() %></a></td>
<td><%= p.getVolume() %></a></td>
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
</p></center>

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