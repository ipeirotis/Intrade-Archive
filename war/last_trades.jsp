<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="intrade.entities.EventClass" %>
<%@ page import="intrade.entities.EventGroup" %>
<%@ page import="intrade.entities.Event" %>
<%@ page import="intrade.entities.Contract" %>
<%@ page import="intrade.entities.ContractTrade" %>
<%@ page import="intrade.utils.PMF" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="javax.jdo.Query" %>

<html>
  <body>

<%
String cid = request.getParameter("contract");
%>
<%
    PersistenceManager pm = PMF.get().getPersistenceManager();
    String query = "select from " + ContractTrade.class.getName() + " order by date DESC";
    Query q = pm.newQuery(query);
    q.setRange(0, 100);
    List<ContractTrade> prices = (List<ContractTrade>) q.execute();
    if (prices.isEmpty()) {
%>
<p>No prices found!</p>
<%
    } else {
%>
<center>
<h1>Latest trades on Intrade.com</h1>

<table border=1 cellpadding=5>
<tr>
<th>Contract</th>
<th>Date</th>
<th>Price</th>
<th>Volume</th>
</tr>
<%
   for (ContractTrade p: prices) {
	   Contract c = pm.getObjectById(Contract.class, Contract.generateKeyFromID(p.getContractid()));
	   
%>
<tr>
<td><a href="trades.jsp?contract=<%= p.getContractid() %>"><%= c.getSymbol() %></a></td>
<td><%= DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(p.getDate()) %></a></td>
<td><%= p.getPrice() %></a></td>
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