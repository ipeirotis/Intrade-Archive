"""Load a contract with reconciled source counts; run after build_archive.py."""
import json
from pathlib import Path
import pyarrow.parquet as pq
ROOT=Path(__file__).resolve().parents[1]
DATA=ROOT/"build/research"
report=json.loads((DATA/"quality_report.json").read_text())
cid=report["example_contract"]
if not cid: raise RuntimeError("No reconciled example was found")
contracts=pq.read_table(DATA/"contracts.parquet",filters=[("contract_id","=",cid)]).to_pylist()
trades=pq.read_table(DATA/"trades.parquet",filters=[("contract_id","=",cid)])
daily=pq.read_table(DATA/"daily.parquet",filters=[("contract_id","=",cid),("source_type","=","summary")])
c=contracts[0]
assert trades.num_rows==c["reported_trade_count"]==c["trade_rows"]
assert daily.num_rows==c["reported_summary_count"]==c["summary_rows"]
print(json.dumps({"contract_id":cid,"name":c["name"],"trade_rows":trades.num_rows,"daily_rows":daily.num_rows,"observed_start":c["first_observed_date"],"observed_end":c["last_observed_date"]},indent=2))
print("First source trade records:",trades.slice(0,3).to_pylist())
print("Positive-volume daily rows:",sum((r["volume"] or 0)>0 for r in daily.to_pylist()))
print("Count reconciliation is not a guarantee of complete historical coverage.")
