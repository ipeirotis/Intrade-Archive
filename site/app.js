"use strict";
const $ = id => document.getElementById(id);
const el = (tag, text, cls) => { const n=document.createElement(tag); if(text!==undefined)n.textContent=text; if(cls)n.className=cls; return n; };
const labels={observed:"Trade rows available",known_missing:"Documented missing trades",missing_expected:"Missing expected trades",metadata_reports_zero:"Metadata reports zero trades",empty_file:"Empty trade file"};
let catalog=[], matches=[], page=0, selected=null, request=0;
const size=40, fmt=n=>Number(n).toLocaleString("en-US");
async function getJSON(path){const r=await fetch(path);if(!r.ok)throw new Error("Failed to load "+path+" ("+r.status+")");return r.json();}
function filter(){
 const q=$("query").value.toLowerCase().trim(), cat=$("category").value, status=$("status").value, start=$("from").value,end=$("to").value;
 matches=catalog.filter(c=>(!q||[c.contract_id,c.name,c.symbol,c.event,c.event_group].join(" ").toLowerCase().includes(q))&&(!cat||c.category===cat)&&(!status||c.trade_status===status)&&(!start||(c.last_observed_date&&c.last_observed_date>=start))&&(!end||(c.first_observed_date&&c.first_observed_date<=end))&&!(start&&end&&start>end));
 page=0;renderResults();
}
function renderResults(){
 $("result-count").textContent=fmt(matches.length)+" contracts";
 $("results").replaceChildren();
 for(const c of matches.slice(page*size,(page+1)*size)){
  const b=el("button",undefined,"result");b.type="button";b.dataset.id=c.contract_id;b.setAttribute("aria-current",String(c.contract_id===selected));
  b.append(el("span",c.name||c.symbol,"title"),el("span",c.category+" · #"+c.contract_id+" · "+(c.first_observed_date||"0 observations")+" – "+(c.last_observed_date||"—"),"meta"),el("span",labels[c.trade_status], "badge"+(c.trade_status.includes("missing")?" warning":"")));
  b.addEventListener("click",()=>select(c.contract_id));$("results").append(b);
 }
 if(!matches.length)$("results").append(el("p","0 contracts match these filters. Try a broader search.","error"));
 $("page").textContent=matches.length? (page+1)+" / "+Math.ceil(matches.length/size):"0 / 0";
 $("prev").disabled=page===0;$("next").disabled=(page+1)*size>=matches.length;
}
function fact(list,label,value){const div=el("div");div.append(el("dt",label),el("dd",value));list.append(div);}
function link(text,url,download=false){const a=el("a",text);a.href=url;if(download)a.download="";return a;}
function csvDownload(cid,kind,rows){
 const blob=new Blob(["date,close,volume,source_type\n"+rows.map(r=>[...r,kind].join(",")).join("\n")+"\n"],{type:"text/csv;charset=utf-8"});
 const url=URL.createObjectURL(blob),a=link("",url);a.download=cid+"-"+kind+"-chart.csv";a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);
}
async function select(cid){
 const c=catalog.find(r=>r.contract_id===cid);if(!c)return;
 selected=cid;history.replaceState(null,"","#contract="+encodeURIComponent(cid));renderResults();
 const d=$("detail");d.replaceChildren(el("p",c.category+" / "+c.event_group,"eyebrow"),el("h2",c.name||c.symbol),el("p",c.symbol+" · Contract "+cid,"symbol"));
 const facts=el("dl",undefined,"facts");
 fact(facts,"Trade rows",fmt(c.trade_rows)+" / "+fmt(c.reported_trade_count||0)+" reported");
 fact(facts,"Daily observations",fmt(c.daily_rows));
 fact(facts,"Observed dates",(c.first_observed_date||"—")+" – "+(c.last_observed_date||"—"));
 fact(facts,"Trade coverage",labels[c.trade_status]);d.append(facts);
 const warn=[];
 if(c.known_missing_trade_file)warn.push("The historical notes identify this contract's trade file as missing.");
 if(c.trade_count_mismatch)warn.push("Observed trade rows differ from the spreadsheet's reported count.");
 if(c.summary_count_mismatch)warn.push("Main summary rows differ from the spreadsheet's reported count.");
 if(c.duplicate_candidate_trade_rows)warn.push(fmt(c.duplicate_candidate_trade_rows)+" trade rows share timestamp, price and quantity; all are retained.");
 if(c.overlapping_daily_source_dates)warn.push("The two daily sources overlap on "+fmt(c.overlapping_daily_source_dates)+" dates. Compare them separately.");
 if(c.duplicate_daily_keys)warn.push("Repeated dates occur within a daily source type.");
 if(warn.length)d.append(el("div",warn.join(" "),"warning-text"));
 const downloads=el("div",undefined,"downloads");
 if(c.source_file_ids)downloads.append(link("Download original contract files","downloads/"+cid+".zip",true));
 downloads.append(link("Data dictionary","https://github.com/ipeirotis/Intrade-Archive/blob/master/docs/DATA_DICTIONARY.md"));d.append(downloads);
 const loading=el("p","Loading daily observations…","chart-note");d.append(loading);
 const current=++request;
 try{
  const data=await getJSON("data/"+cid+".json");if(current!==request)return;loading.remove();
  const options=Object.keys(data).filter(k=>data[k].length);
  if(!options.length){d.append(el("p","Daily-price observations: 0."+(c.source_file_ids?" The original-file download contains the available source records.":" This contract has a metadata entry."),"chart-note"));return;}
  const controls=el("div",undefined,"controls"),label=el("label","Daily source"),sel=el("select");sel.id="series";sel.setAttribute("aria-label","Daily source");
  for(const k of options){const o=el("option",k==="summary"?"Main archive summary":"Supplemental closing prices");o.value=k;sel.append(o);}
  label.append(sel);controls.append(label);
  const checkLabel=el("label",undefined,"check"),check=el("input");check.type="checkbox";check.id="active-only";checkLabel.append(check,document.createTextNode("Only days with positive volume"));controls.append(checkLabel);d.append(controls);
  const chart=el("div"),note=el("p",undefined,"chart-note"),button=el("button","Download chart CSV");button.type="button";button.addEventListener("click",()=>csvDownload(cid,sel.value,data[sel.value]));
  const details=el("details"),summary=el("summary","Inspect first and last observations"),table=el("table");details.append(summary,table);
  d.append(chart,note,button,details);
  function draw(){
   const all=data[sel.value],rows=all.filter(r=>!check.checked||(r[2]!==null&&r[2]>0));
   chart.replaceChildren();drawChart(chart,rows);
   note.textContent=fmt(rows.length)+" of "+fmt(all.length)+" rows shown. Close prices retain source zeros and repeated closes. Daily observations use source business dates. Chart CSV includes the full selected source series.";
   table.replaceChildren();const tr=el("tr");for(const h of ["Date","Close","Volume"])tr.append(el("th",h));const head=el("thead");head.append(tr);table.append(head);const body=el("tbody");const sample=all.length>12?[...all.slice(0,6),...all.slice(-6)]:all;for(const r of sample){const row=el("tr");for(const x of r)row.append(el("td",x===null?"Missing":String(x)));body.append(row);}table.append(body);
  }
  sel.addEventListener("change",draw);check.addEventListener("change",draw);draw();
 }catch(e){if(current===request)loading.textContent=e.message+". Reload the page to retry.";}
}
function drawChart(container,rows){
 if(!rows.length){container.append(el("p","0 observations meet this display filter."));return;}
 const ns="http://www.w3.org/2000/svg",svg=document.createElementNS(ns,"svg");svg.setAttribute("viewBox","0 0 680 360");svg.setAttribute("role","img");svg.setAttribute("aria-label","Archived closing price and daily volume");svg.classList.add("chart");
 const node=(tag,attrs,text)=>{const n=document.createElementNS(ns,tag);for(const [k,v]of Object.entries(attrs))n.setAttribute(k,String(v));if(text!==undefined)n.textContent=text;svg.append(n);return n;};
 const times=rows.map(r=>Date.parse(r[0]+"T00:00:00Z")),lo=Math.min(...times),hi=Math.max(...times);
 const x=t=>52+(t-lo)/(hi-lo||1)*606;
 const prices=rows.map(r=>r[1]).filter(v=>v!==null),pmin=Math.min(0,...prices),pmax=Math.max(100,...prices),y=p=>210-(p-pmin)/(pmax-pmin||1)*180;
 for(let n=0;n<=4;n++){const v=pmin+(pmax-pmin)*n/4;node("line",{x1:52,x2:658,y1:y(v),y2:y(v),stroke:"#dfe5dc"});node("text",{x:44,y:y(v)+4,"text-anchor":"end"},String(Math.round(v)));}
 node("text",{x:52,y:16},"Archived close");node("text",{x:52,y:244},"Daily volume");
 const vmax=Math.max(1,...rows.map(r=>r[2]||0));let path="",prev=null;
 rows.forEach((r,i)=>{const px=x(times[i]);if(r[1]===null){prev=null;}else{const gap=prev===null||times[i]-prev>86400000;path+=(gap?"M":"L")+px.toFixed(2)+","+y(r[1]).toFixed(2)+" ";prev=times[i];}
 if(r[2]!==null){const line=node("line",{x1:px,x2:px,y1:315,y2:315-r[2]/vmax*60,stroke:"#bc9050","stroke-width":1.5});const title=document.createElementNS(ns,"title");title.textContent=r[0]+": volume "+r[2]+", close "+r[1];line.append(title);}
 });
 node("path",{d:path,fill:"none",stroke:"#175a4a","stroke-width":2});
 // Visible points make isolated observations and gaps inspectable.
 rows.forEach((r,i)=>{if(r[1]!==null){const point=node("circle",{cx:x(times[i]),cy:y(r[1]),r:1.8,fill:"#175a4a"});const title=document.createElementNS(ns,"title");title.textContent=r[0]+": close "+r[1]+", volume "+r[2];point.append(title);}});
 node("text",{x:52,y:340},rows[0][0]);node("text",{x:658,y:340,"text-anchor":"end"},rows[rows.length-1][0]);node("text",{x:658,y:244,"text-anchor":"end"},"Peak "+fmt(vmax));container.append(svg);
}
$("query").addEventListener("input",filter);for(const id of ["category","status","from","to"])$(id).addEventListener("change",filter);
$("reset").addEventListener("click",()=>{for(const id of ["query","category","status","from","to"])$(id).value="";filter();});
$("prev").addEventListener("click",()=>{page--;renderResults();});$("next").addEventListener("click",()=>{page++;renderResults();});
window.addEventListener("hashchange",()=>{const id=new URLSearchParams(location.hash.slice(1)).get("contract");if(id)select(id);});
(async()=>{try{
 const [data,stats]=await Promise.all([getJSON("catalog.json"),getJSON("stats.json")]);catalog=data.sort((a,b)=>b.trade_rows-a.trade_rows||Number(a.contract_id)-Number(b.contract_id));
 for(const cat of [...new Set(catalog.map(c=>c.category))].sort()){const o=el("option",cat);o.value=cat;$("category").append(o);}
 $("stats").replaceChildren();for(const [value,label]of [[fmt(stats.metadata_contracts),"Contracts"],[stats.observed_start.slice(0,4)+"–"+stats.observed_end.slice(0,4),"Observed years"],[fmt(stats.counts.trade_rows),"Trade rows"],[fmt(stats.known_missing_trade_files),"Documented missing trade files"]]){const n=el("div",undefined,"stat");n.append(el("strong",value),el("span",label));$("stats").append(n);}
 filter();const id=new URLSearchParams(location.hash.slice(1)).get("contract");if(id&&catalog.some(c=>c.contract_id===id))select(id);
 }catch(e){$("stats").textContent="Archive data loading failed.";const p=el("p",e.message+". Serve this directory over HTTP to load the archive data.","error");$("results").append(p);}
})();
