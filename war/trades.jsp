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

<%
String cid = request.getParameter("contract");

    PersistenceManager pm = PMF.get().getPersistenceManager();
Contract c = pm.getObjectById(Contract.class, Contract.generateKeyFromID(cid));
// Event e = pm.getObjectById(Event.class, Event.generateKeyFromID(c.getEventid()));
%>
<head>
<title>Trade history for <%= c.getSymbol()%>: <%=c.getName() %></title>
</head>
 <body><center>
 <h1>Trades for <%= c.getSymbol() %></h1>
<p><small><%= c.getName() %></small></p>
<p><small>Total Volume: <%= c.getTotalVolume() %></small></p>
<a href="/processContractTrades?contract=<%=c.getId() %>&time_threshold_minutes=0">Refresh</a><br>
 
<% 
    String query = "select from " + ContractTrade.class.getName() + " where contractid==\""+cid+"\" order by date";
    Query q = pm.newQuery(query);
    q.setRange(0, 1000);
    List<ContractTrade> prices = (List<ContractTrade>) q.execute();
    if (prices.isEmpty()) {
%>

<p>No trades found!</p>
<%
    } else {
    	
    	
    	
%>
 

<object id="chart" type="text/html" data="http://intrade-archive.appspot.com/embed_visualize_contract_trades.jsp?id=<%=c.getId()%>&volume=n" style="width:400px;height:250px;">
</object>


<br>
Embed code:<br>
<textarea rows="4" cols="60" style="font-size:11px">
&lt;object id="chart" type="text/html" data="http://intrade-archive.appspot.com/embed_visualize_contract_trades.jsp?id=<%=c.getId()%>&volume=n" style="width:400px;height:250px;"&gt;&lt;/object&gt;
</textarea>
<br>
<table border=1 cellpadding=5>
<tr>
<th>Date</th>
<th>Price</th>
<th>Volume</th>
</tr>
<%
   for (ContractTrade p: prices) {
%>
<tr>
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