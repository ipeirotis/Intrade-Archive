<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Date" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="intrade.entities.EventClass" %>
<%@ page import="intrade.entities.EventGroup" %>
<%@ page import="intrade.entities.Event" %>
<%@ page import="intrade.entities.Contract" %>
<%@ page import="intrade.entities.ContractClosingPriceCSV" %>
<%@ page import="intrade.utils.PMF" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="javax.jdo.Query" %>

<html>
  <head>

    <script type='text/javascript' src='http://www.google.com/jsapi'></script> 
    <script type='text/javascript'> 
      google.load('visualization', '1', {'packages':['annotatedtimeline']});
      google.setOnLoadCallback(drawChart);
 

      function drawChart() {
        var data = new google.visualization.DataTable();

        data.addColumn('date', 'Date');
<%
	String eventid = request.getParameter("id");
	
	        
        Event event = null;
        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
        	event = pm.getObjectById(Event.class, Event.generateKeyFromID(eventid));
    	} catch (Exception e) {
        	event = null;
    	}

		HashMap<Date,List<ContractClosingPriceCSV>> timeline= new HashMap<Date,List<ContractClosingPriceCSV>>();

    String query = "select from " + Contract.class.getName() + " where eventid==\""+eventid+"\"";
    List<Contract> contracts = (List<Contract>) pm.newQuery(query).execute();
    if (contracts.isEmpty()) {
    %>
	<p>No contracts found!</p>
	<%
    
    } else {
    
    	HashMap<String, Integer> contractMap = new HashMap<String, Integer>(contracts.size());
    	int i=0;
		for (Contract c: contracts) {
    		%>
  		      data.addColumn('number', '<%= c.getSymbol() %>');
			<%
			contractMap.put(c.getId(), i++);
			String qr = "select from " + ContractClosingPriceCSV.class.getName() + " where contractid==\""+c.getId()+"\" order by date";
    Query q = pm.newQuery(qr);
    q.setRange(0, 1000);
    List<ContractClosingPriceCSV> prices = (List<ContractClosingPriceCSV>) q.execute();
			for (ContractClosingPriceCSV p: prices) {
			Date d = new Date(p.getDate()); 
				if (!timeline.containsKey(d)) {
					timeline.put(d, new ArrayList<ContractClosingPriceCSV>());
				}
				timeline.get(d).add(p);
			
			}
		}
		
		TreeSet<Date> dates = new TreeSet(timeline.keySet());
		%>
		
		data.addRows(<%= dates.size() %>);
		<%
		int r=0;
		for (Date d: dates) {
			%>
				data.setValue(<%= r %>,0, new Date(<%= d.getYear()+1900 %>,<%= d.getMonth() %>,<%= d.getDate() %>,00,00,00,00));
			<%
			
			List<ContractClosingPriceCSV> prices = timeline.get(d);
			if (prices == null) continue;
			
			for (ContractClosingPriceCSV p: prices) {
				int contract_column = contractMap.get(p.getContractid());
    					%>
						data.setValue(<%= r %>,<%= contract_column+1 %>, <%= p.getClose() %>);
						<%
			}
			
			r++;
		} 
		
	

	}
	%>

 
        var chart = new google.visualization.AnnotatedTimeLine(document.getElementById('chart_div'));
        chart.draw(data, {displayAnnotations: false, max:100, min:1, scaleType: 'fixed', legendPosition: 'newRow' });
      }
    </script> 
  </head> 
 
  <body> 
  <center>
    <div id='chart_div' style='width: 600px; height: 400px;'></div> 
    </center>
  </body> 
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