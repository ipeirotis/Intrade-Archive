<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Date"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="intrade.entities.EventClass"%>
<%@ page import="intrade.entities.EventGroup"%>
<%@ page import="intrade.entities.Event"%>
<%@ page import="intrade.entities.Contract"%>
<%@ page import="intrade.entities.ContractTrade"%>
<%@ page import="intrade.utils.PMF"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.TreeSet"%>
<%@ page import="javax.jdo.Query"%>


<%
String contractid = request.getParameter("id");
if (contractid == null)		return;

int s = 0;
String start = request.getParameter("start");
if (start != null) {
	s=Integer.parseInt(start);
}

int e = 1000;
String end = request.getParameter("end");
if (end != null) {
	e=Integer.parseInt(end);
}

PersistenceManager pm = PMF.get().getPersistenceManager();
Contract c;
try {
	c = pm.getObjectById(Contract.class, Contract.generateKeyFromID(contractid));
} catch (Exception ex) {
	return;
}

String query = "select from " + ContractTrade.class.getName() + " where contractid==\""+contractid+"\"  order by date DESC";

Query q = pm.newQuery(query);
q.setRange(s, e);
List<ContractTrade> trades = (List<ContractTrade>)q.execute();
if (trades.isEmpty()) return;

String vol = request.getParameter("volume");
if (vol == null)		return;

boolean volume = false;
if (vol.equals("y")) volume=true;


String bar = request.getParameter("rangebar");
if (bar == null) bar ="n";

boolean rangebar = false;
if (bar.equals("y")) rangebar=true;


%>

<html>
<head>

<script type='text/javascript' src='http://www.google.com/jsapi'></script>
<script type='text/javascript'> 
      google.load('visualization', '1', {'packages':['annotatedtimeline']});
      google.setOnLoadCallback(drawChart);
 
     
      function drawChart() {
        var data = new google.visualization.DataTable();

        data.addColumn('datetime', 'Date');
		data.addColumn('number', 'Price');
		data.addColumn('number', 'Volume');
		
		<% if (false) {%> data.addColumn('number', 'Volume'); <%} %>

		data.addRows(<%= trades.size() %>);	<%

		int r=0;
		for (ContractTrade t: trades) {
			Date d = new Date(t.getDate());
				%>
				data.setValue(<%= r %>,0, new Date(<%= d.getYear()+1900 %>,<%= d.getMonth() %>,<%= d.getDate() %>,<%= d.getHours() %>,<%= d.getMinutes() %>,<%= d.getSeconds() %>,00));
				data.setValue(<%= r %>,1 , <%= t.getPrice() %>);
				data.setValue(<%= r %>,2 , <%= t.getVolume() %>);
				<%	
			r++;
		} 
		%>
        var chart = new google.visualization.AnnotatedTimeLine(document.getElementById('chart_div'));
        chart.draw(data, {
            displayAnnotations: false, 
            displayAnnotationsFilter: false,
            <% if (!rangebar) {%>displayRangeSelector: false, <%} else {%>displayRangeSelector: true, <%}%>
            scaleType: 'allfixed', 
            legendPosition: 'sameRow',
            scaleColumns: [0,1],
            displayZoomButtons: false
        }
        
        );
        <% if (!volume) {%> chart.hideDataColumns(1); <%} %>
      }


      
      
    </script>
</head>

<body>
<center>
<div id='chart_div' style='width: 350px; height: 190px;'></div><br>
</center>
</body>
<script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script>
<script type="text/javascript">
try {
var pageTracker = _gat._getTracker("UA-89122-13");
pageTracker._trackPageview();
} catch(err) {}</script>
</html>
