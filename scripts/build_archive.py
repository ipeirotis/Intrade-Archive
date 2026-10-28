"""Build traceable research tables and a static explorer from immutable source files."""
from __future__ import annotations
import collections
import csv
import gzip
import hashlib
import io
import json
import math
import re
import shutil
import zipfile
from datetime import date, datetime
from pathlib import Path
import openpyxl
import pyarrow as pa
import pyarrow.parquet as pq

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "build"
SITE = OUT / "site"
DATA = OUT / "research"
MONTHS = {m: i for i, m in enumerate("Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec".split(), 1)}
HEADERS = {
    "summary": ["market id","market symbol","market name","business day","session open","session high","session low","closing price","open interest","session trade volume","lifetime high","lifetime low"],
    "trades": ["market id","market symbol","market name","trade quantity","trade price","date/time [utc]"],
    "closingPrice": ["Date","Open","Low","High","Close","Volume"],
}
DAILY_FIELDS = ["contract_id","date","open","high","low","close","volume","open_interest","lifetime_high","lifetime_low","source_type","source_file_id","source_row"]
TRADE_FIELDS = ["contract_id","timestamp_utc","price","quantity","duplicate_candidate","source_file_id","source_row"]
F64 = {"open","high","low","close","price","lifetime_high","lifetime_low"}
I64 = {"volume","open_interest","quantity","source_file_id","source_row"}
SCHEMAS = {}
for name, cols in [("daily", DAILY_FIELDS), ("trades", TRADE_FIELDS)]:
    SCHEMAS[name] = pa.schema([(c, pa.float64() if c in F64 else pa.int64() if c in I64 else pa.bool_() if c=="duplicate_candidate" else pa.string()) for c in cols])

def json_write(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, ensure_ascii=False, separators=(",",":"), allow_nan=False)+"\n", encoding="utf-8")

def date_value(value, kind):
    if kind == "summary":
        return date.fromisoformat(value).isoformat()
    if kind == "closingPrice":
        match = re.fullmatch(r"([A-Za-z]{3}) (\d{1,2}), (\d{4})", value)
        if not match: raise ValueError("Invalid closing-price date: "+value)
        m,d,y = match.groups()
        return date(int(y), MONTHS[m], int(d)).isoformat()
    match = re.fullmatch(r"([A-Za-z]{3})/(\d{1,2})/(\d{4}) (\d{2}):(\d{2}):(\d{2})", value)
    if not match: raise ValueError("Invalid UTC trade timestamp: "+value)
    m,d,y,hh,mm,ss = match.groups()
    return datetime(int(y), MONTHS[m], int(d), int(hh), int(mm), int(ss)).isoformat()+"Z"

def number(value, integer=False):
    if value.strip() == "": return None
    x = float(value)
    if not math.isfinite(x): raise ValueError("Nonfinite numeric value")
    if integer:
        if not x.is_integer(): raise ValueError("Noninteger count: "+value)
        return int(x)
    return x

def decode_csv(raw):
    try: return raw.decode("utf-8-sig"), "utf-8"
    except UnicodeDecodeError:
        return raw.decode("cp1252"), "cp1252"

def parse_member(raw, kind, cid, source_id):
    text, encoding = decode_csv(raw)
    reader = csv.reader(io.StringIO(text, newline=""), strict=True)
    header = next(reader, None)
    if header != HEADERS[kind]: raise ValueError(f"Unexpected {kind} header: {header}")
    result=[]
    for rowno, r in enumerate(reader, 2):
        if len(r) != len(header): raise ValueError(f"Wrong column count at record {rowno}: {r}")
        row = dict(zip(header,r))
        if kind != "closingPrice" and str(int(row["market id"])) != cid:
            raise ValueError(f"Contract ID mismatch at record {rowno}")
        provenance = dict(contract_id=cid, source_file_id=source_id, source_row=rowno)
        if kind == "trades":
            result.append(dict(provenance, timestamp_utc=date_value(r[5],kind),price=number(r[4]),quantity=number(r[3],True),duplicate_candidate=False))
        else:
            if kind=="summary":
                values = [r[3],r[4],r[5],r[6],r[7],r[9],r[8],r[10],r[11]]
            else:
                values = [r[0],r[1],r[3],r[2],r[4],r[5],"","",""]
            result.append(dict(provenance,date=date_value(values[0],kind),open=number(values[1]),high=number(values[2]),low=number(values[3]),close=number(values[4]),volume=number(values[5],True),open_interest=number(values[6],True),lifetime_high=number(values[7]),lifetime_low=number(values[8]),source_type=kind))
    return result, encoding

class TableWriter:
    def __init__(self, name):
        self.name = name
        self.schema = SCHEMAS[name]
        self.raw = open(DATA / (name+".csv.gz"),"wb")
        self.gz = gzip.GzipFile(filename="", mode="wb", fileobj=self.raw,mtime=0)
        self.text = io.TextIOWrapper(self.gz, encoding="utf-8",newline="")
        self.csv = csv.DictWriter(self.text,fieldnames=self.schema.names,lineterminator="\n")
        self.csv.writeheader()
        self.pq = pq.ParquetWriter(DATA / (name+".parquet"),self.schema,compression="zstd")
        self.buffer=[]
        self.count=0
    def add(self, rows):
        self.csv.writerows(rows)
        self.buffer.extend(rows)
        self.count+=len(rows)
        if len(self.buffer)>=100000: self.flush()
    def flush(self):
        if self.buffer:
            self.pq.write_table(pa.Table.from_pylist(self.buffer,schema=self.schema))
            self.buffer=[]
    def close(self):
        self.flush()
        self.pq.close()
        self.text.close()
        self.raw.close()

def small_table(path, rows):
    if not rows: return
    with path.open("w",encoding="utf-8",newline="") as f:
        w=csv.DictWriter(f,fieldnames=list(rows[0]),lineterminator="\n")
        w.writeheader()
        w.writerows(rows)

def sha256(path):
    with path.open("rb") as f:
        return hashlib.file_digest(f,"sha256").hexdigest()

def source_inventory():
    baseline=json.loads((ROOT/"preservation/original-manifest.json").read_text())
    manifest=[]
    for item in baseline["files"]:
        p=ROOT/item["path"]
        raw=p.read_bytes()
        gitsha=hashlib.sha1(b"blob "+str(len(raw)).encode()+b"\0"+raw).hexdigest()
        if gitsha!=item["git_blob_sha1"]: raise ValueError("Original source changed: "+item["path"])
        manifest.append(dict(item,sha256=hashlib.sha256(raw).hexdigest()))
    json_write(DATA/"original-manifest.json",dict(source_commit=baseline["source_commit"],files=manifest))
    (DATA/"ORIGINAL_SHA256SUMS").write_text("".join(f"{r['sha256']}  {r['path']}\n" for r in manifest),encoding="utf-8")
    return baseline["source_commit"]

def metadata():
    workbook=openpyxl.load_workbook(ROOT/"data/Intrade-Contracts-NoFinancial.xlsx",read_only=True,data_only=True)
    ws=workbook["Intrade_AllContracts_ExceptFin"]
    rows=ws.iter_rows(values_only=True)
    headers=list(next(rows))
    expected=["Category","Event Group","Event","Event ID","Market Symbol","ID","End Date","Market Name","Summary Count","Summary URL","Trades Count","Trades URL","File name"]
    if headers!=expected: raise ValueError("Spreadsheet schema changed")
    result={}
    for i,r in enumerate(rows,2):
        if not any(v is not None for v in r): continue
        cid=str(int(r[5]))
        if cid in result: raise ValueError("Duplicate metadata contract "+cid)
        result[cid]=dict(contract_id=cid,category=r[0],event_group=r[1],event=r[2],event_id=str(r[3]),symbol=r[4],end_date=r[6].isoformat() if isinstance(r[6],(date,datetime)) else r[6],name=r[7],reported_summary_count=r[8],summary_url=r[9],reported_trade_count=r[10],trades_url=r[11],original_file_stem=r[12],metadata_row=i,settlement_status="not provided in spreadsheet",settlement_price=None)
    workbook.close()
    return result

def deterministic_zip(path, entries):
    with zipfile.ZipFile(path,"w",compression=zipfile.ZIP_DEFLATED,compresslevel=9) as z:
        for name,raw in entries:
            info=zipfile.ZipInfo(name,date_time=(1980,1,1,0,0,0))
            info.compress_type=zipfile.ZIP_DEFLATED
            info.external_attr=0o644<<16
            z.writestr(info,raw)

def main():
    DATA.mkdir(parents=True,exist_ok=True)
    SITE.mkdir(parents=True,exist_ok=True)
    for d in ["data","downloads"]: (SITE/d).mkdir(exist_ok=True)
    source_commit=source_inventory()
    contracts=metadata()
    missing={line.rsplit(" ",1)[1] for line in (ROOT/"data/Notes.txt").read_text().splitlines() if re.search(r" \d+$",line)}
    members=collections.defaultdict(list)
    archives=[]
    file_manifest=[]
    for p in sorted((ROOT/"data").glob("*.zip")):
        z=zipfile.ZipFile(p)
        archives.append(z)
        if z.testzip() is not None: raise ValueError("ZIP CRC validation failed")
        for info in sorted(z.infolist(),key=lambda i:i.filename):
            if info.is_dir(): continue
            match=re.search(r"\.(\d+)-(summary|trades|closingPrice)\.csv$",info.filename)
            if not match: raise ValueError("Unrecognized archive member: "+info.filename)
            cid,kind=match.groups()
            if cid not in contracts: raise ValueError("Missing metadata for "+cid)
            sid=len(file_manifest)+1
            file_manifest.append(dict(source_file_id=sid,archive=p.name,member=info.filename,contract_id=cid,source_type=kind,bytes=info.file_size,sha256=None,encoding=None,rows=None))
            members[cid].append((z,info,kind,sid))
    writers={n:TableWriter(n) for n in ["daily","trades"]}
    diagnostics=collections.Counter()
    coverage=[]
    examples=[]
    for cid,c in sorted(contracts.items(),key=lambda item:int(item[0])):
        daily=[]; trades=[]; original_entries=[]
        for z,info,kind,sid in members[cid]:
            raw=z.read(info)
            parsed,encoding=parse_member(raw,kind,cid,sid)
            fm=file_manifest[sid-1]
            fm.update(sha256=hashlib.sha256(raw).hexdigest(),encoding=encoding,rows=len(parsed))
            (trades if kind=="trades" else daily).extend(parsed)
            original_entries.append((f"{sid}-{Path(info.filename).name}",raw))
        seen=collections.Counter((r["timestamp_utc"],r["price"],r["quantity"]) for r in trades)
        for r in trades: r["duplicate_candidate"]=seen[(r["timestamp_utc"],r["price"],r["quantity"])]>1
        summary=[r for r in daily if r["source_type"]=="summary"]
        supplement=[r for r in daily if r["source_type"]=="closingPrice"]
        kinds=[m[2] for m in members[cid]]
        trade_status="observed" if trades else "empty_file" if "trades" in kinds else "known_missing" if cid in missing else "missing_expected" if (c["reported_trade_count"] or 0)>0 else "metadata_reports_zero"
        days=[r["date"] for r in daily]; times=[r["timestamp_utc"] for r in trades]
        observed_dates=days+[v[:10] for v in times]
        count_mismatch=len(trades)!=(c["reported_trade_count"] or 0)
        summary_mismatch=len(summary)!=(c["reported_summary_count"] or 0)
        suspect=sum(r["duplicate_candidate"] for r in trades)
        duplicate_dates=len(daily)-len(set((r["source_type"],r["date"]) for r in daily))
        overlap=len({r["date"] for r in summary}&{r["date"] for r in supplement})
        zero_volume=sum(r["volume"]==0 for r in daily)
        bad_price=sum(any(r[k] is not None and not 0<=r[k]<=100 for k in ["open","high","low","close"]) for r in daily)
        bad_price+=sum(r["price"] is not None and not 0<=r["price"]<=100 for r in trades)
        row=dict(c,trade_status=trade_status,known_missing_trade_file=cid in missing,trade_rows=len(trades),summary_rows=len(summary),supplemental_daily_rows=len(supplement),daily_rows=len(daily),first_observed_date=min(observed_dates) if observed_dates else None,last_observed_date=max(observed_dates) if observed_dates else None,first_trade_utc=min(times) if times else None,last_trade_utc=max(times) if times else None,first_daily_date=min(days) if days else None,last_daily_date=max(days) if days else None,trade_count_mismatch=count_mismatch,summary_count_mismatch=summary_mismatch,duplicate_candidate_trade_rows=suspect,duplicate_daily_keys=duplicate_dates,overlapping_daily_source_dates=overlap,zero_volume_daily_rows=zero_volume,price_outside_0_100_rows=bad_price,source_file_ids=";".join(str(m[3]) for m in members[cid]))
        coverage.append(row)
        diagnostics.update(trade_rows=len(trades),daily_rows=len(daily),summary_rows=len(summary),supplemental_daily_rows=len(supplement),duplicate_candidate_trade_rows=suspect,duplicate_daily_keys=duplicate_dates,overlapping_daily_source_dates=overlap,zero_volume_daily_rows=zero_volume,price_outside_0_100_rows=bad_price,contracts_with_trade_count_mismatch=int(count_mismatch),contracts_with_summary_count_mismatch=int(summary_mismatch))
        writers["daily"].add(daily);writers["trades"].add(trades)
        # Two explicit source series; never blend overlapping dates or infer outcomes.
        series={kind:[[r["date"],r["close"],r["volume"]] for r in sorted(daily,key=lambda x:(x["date"],x["source_file_id"],x["source_row"])) if r["source_type"]==kind] for kind in ["summary","closingPrice"]}
        json_write(SITE/"data"/(cid+".json"),series)
        if original_entries:
            deterministic_zip(SITE/"downloads"/(cid+".zip"),original_entries)
        if len(trades)>10 and summary and not count_mismatch and not summary_mismatch and not suspect and not duplicate_dates:
            examples.append(cid)
    for w in writers.values():w.close()
    for z in archives:z.close()
    small_table(DATA/"contracts.csv",coverage)
    pq.write_table(pa.Table.from_pylist(coverage),DATA/"contracts.parquet",compression="zstd")
    small_table(DATA/"source_files.csv",file_manifest)
    stats=dict(source_commit=source_commit,metadata_contracts=len(contracts),archive_files=len(file_manifest),categories=dict(collections.Counter(c["category"] for c in coverage)),trade_status=dict(collections.Counter(c["trade_status"] for c in coverage)),known_missing_trade_files=len(missing),counts=dict(diagnostics),observed_start=min(c["first_observed_date"] for c in coverage if c["first_observed_date"]),observed_end=max(c["last_observed_date"] for c in coverage if c["last_observed_date"]),example_contract=examples[0] if examples else None)
    json_write(DATA/"quality_report.json",stats)
    json_write(SITE/"catalog.json",coverage)
    json_write(SITE/"stats.json",stats)
    small_table(SITE/"coverage.csv",coverage)
    small_table(SITE/"source_files.csv",file_manifest)
    for p in (ROOT/"site").iterdir():
        if p.is_file():shutil.copy2(p,SITE/p.name)
    (SITE/".nojekyll").touch()
    print(json.dumps(stats,indent=2))
    print("EXAMPLE",json.dumps(next((c for c in coverage if c["contract_id"]==stats["example_contract"]),{})))
    assert sum(c["trade_rows"] for c in coverage)==pq.ParquetFile(DATA/"trades.parquet").metadata.num_rows
    assert sum(c["daily_rows"] for c in coverage)==pq.ParquetFile(DATA/"daily.parquet").metadata.num_rows
    assert len(coverage)==len(contracts)
    (DATA/"SHA256SUMS").write_text("".join(f"{sha256(p)}  {p.name}\n" for p in sorted(DATA.iterdir()) if p.is_file() and p.name!="SHA256SUMS"),encoding="utf-8")

if __name__=="__main__":
    main()
