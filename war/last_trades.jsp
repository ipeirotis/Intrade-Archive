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
<head><style type="text/css">
 #trade {
    display: table;
    }

  #row  {
    display: table-row;
    }

  #date, #price, #volume {
    display: table-cell;
    }
</style></head>
  <body>

<%
String cid = request.getParameter("contract");
%>
<%
    PersistenceManager pm = PMF.get().getPersistenceManager();
    String query = "select from " + ContractTrade.class.getName() + " order by date DESC";
    Query q = pm.newQuery(query);
    q.setRange(0, 1000);
    List<ContractTrade> prices = (List<ContractTrade>) q.execute();
    if (prices.isEmpty()) {
%>
<p>No prices found!</p>
<%
    } else {
%>
<center>
<h1>Latest 1,000 trades on Intrade.com</h1>

<div id="trades">
<div id="row">
<div id="contract">Contract</div>
<div id="date">Date</div>
<div id="price">Price</div>
<div id="volume">Volume</div>
</div>
<%
   for (ContractTrade p: prices) {
	   Contract c = pm.getObjectById(Contract.class, Contract.generateKeyFromID(p.getContractid()));
	   
%>
<div id="row">
<div id="contract"><a href="trades.jsp?contract=<%= p.getContractid() %>"><%= c.getSymbol() %></a></div>
<div id="date"><%= DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(p.getDate()) %></a></div>
<div id="price"><%= p.getPrice() %></a></div>
<div id="volume"><%= p.getVolume() %></a></div>
</div>
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