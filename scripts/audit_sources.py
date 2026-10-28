"""Read-only inventory of original archive sources; never extracts or edits them."""
import collections
import hashlib
import json
from pathlib import Path
import zipfile
import openpyxl

ROOT = Path(__file__).resolve().parents[1]

def main():
    for p in sorted((ROOT / "data").iterdir()):
        print("SOURCE", p.name, p.stat().st_size, hashlib.sha256(p.read_bytes()).hexdigest())
        if p.suffix == ".xlsx":
            wb = openpyxl.load_workbook(p, read_only=True, data_only=True)
            for ws in wb:
                print("SHEET", ws.title, ws.max_row, ws.max_column)
                for row in list(ws.iter_rows(values_only=True))[:5]:
                    print(json.dumps(row, default=str))
            wb.close()
        if p.suffix == ".zip":
            with zipfile.ZipFile(p) as z:
                members = [i for i in z.infolist() if not i.is_dir()]
                print("ZIP", len(members), sum(i.file_size for i in members))
                print("EXTENSIONS", collections.Counter(Path(i.filename).suffix for i in members))
                print("NAMES", json.dumps([i.filename for i in members[:25]]))
                representatives = {}
                for i in members:
                    key = (Path(i.filename).suffix, i.filename.split("/")[0])
                    representatives.setdefault(key, i)
                for i in list(representatives.values())[:15]:
                    with z.open(i) as stream:
                        print("SAMPLE", i.filename, repr(stream.read(1600)))
    p = ROOT / "war/WEB-INF/appengine-generated/local_db.bin"
    with p.open("rb") as f:
        print("DATASTORE_HEADER", repr(f.read(128)))
    print("DATASTORE_SHA256", hashlib.sha256(p.read_bytes()).hexdigest())
    from pypdf import PdfReader
    for i, page in enumerate(PdfReader(ROOT / "docs/Intrade_XML_API.pdf").pages):
        text = page.extract_text() or ""
        if any(term.lower() in text.lower() for term in ["timestamp", "expiryPrice", "tickValue", "TimeAndSales", "ClosingPrice"]):
            print("API_PAGE", i + 1, text[:14000])

if __name__ == "__main__":
    main()
