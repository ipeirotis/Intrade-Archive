"""Independently compare all generated CSV values to Parquet batches."""
import csv
import gzip
import json
from pathlib import Path
import pyarrow.parquet as pq
ROOT=Path(__file__).resolve().parents[1]
DATA=ROOT/"build/research"
def compare(name,compressed):
    csv_path=DATA/(name+".csv.gz" if compressed else name+".csv")
    opener=gzip.open if compressed else open
    count=0
    with opener(csv_path,"rt",encoding="utf-8",newline="") as stream:
        rows=csv.DictReader(stream)
        parquet=pq.ParquetFile(DATA/(name+".parquet"))
        assert rows.fieldnames==parquet.schema_arrow.names
        for batch in parquet.iter_batches(batch_size=10000):
            for record in batch.to_pylist():
                actual=next(rows)
                expected={k:"" if v is None else str(v) for k,v in record.items()}
                assert actual==expected,(name,count,actual,expected)
                count+=1
        assert next(rows,None) is None
        assert count==parquet.metadata.num_rows
    print(f"Verified {name}: {count:,} CSV/Parquet rows with identical values")
for name in ["daily","trades"]:compare(name,True)
compare("contracts",False)
report=json.loads((DATA/"quality_report.json").read_text())
assert report["metadata_contracts"]==17295
assert report["archive_files"]==28120
assert report["counts"]["trade_rows"]==2129385
assert report["counts"]["daily_rows"]==2035482
print("Historical snapshot row totals verified")
