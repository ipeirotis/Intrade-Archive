"""Package complete reproducible deliverables; no network or credential handling."""
import json
import os
from pathlib import Path
from build_archive import ROOT, OUT, DATA, SITE, deterministic_zip, sha256
RELEASE=OUT/"release"
RELEASE.mkdir(parents=True,exist_ok=True)
metadata={"version":"1.0.0","source_commit":"620380b2ade7504e270239ecf98b68995c384aa8","build_commit":os.environ.get("GITHUB_SHA","local-unrecorded"),"python_dependencies":(ROOT/"requirements.txt").read_text(),"timestamp_policy":"Trade CSV header explicitly UTC; daily dates have no assigned time zone."}
(DATA/"BUILD.json").write_text(json.dumps(metadata,indent=2)+"\n")
for name in ["README.md","RIGHTS.md","LICENSE","CITATION.cff"]:
    (DATA/name).write_bytes((ROOT/name).read_bytes())
for name in ["DATA_DICTIONARY.md","COVERAGE.md","PRESERVATION.md"]:
    (DATA/name).write_bytes((ROOT/"docs"/name).read_bytes())
(DATA/"SHA256SUMS").write_text("".join(f"{sha256(p)}  {p.name}\n" for p in sorted(DATA.iterdir()) if p.is_file() and p.name!="SHA256SUMS"))
deterministic_zip(RELEASE/"intrade-research-v1.0.0.zip",((p.name,p.read_bytes()) for p in sorted(DATA.iterdir()) if p.is_file()))
deterministic_zip(RELEASE/"intrade-explorer-v1.0.0.zip",((str(p.relative_to(SITE)).replace(os.sep,"/"),p.read_bytes()) for p in sorted(SITE.rglob("*")) if p.is_file()))
manifest=json.loads((ROOT/"preservation/original-manifest.json").read_text())
deterministic_zip(RELEASE/"intrade-original-snapshot.zip",((item["path"],(ROOT/item["path"]).read_bytes()) for item in manifest["files"]))
for name in ["quality_report.json","contracts.csv","source_files.csv","original-manifest.json","ORIGINAL_SHA256SUMS","BUILD.json"]:
    (RELEASE/name).write_bytes((DATA/name).read_bytes())
(RELEASE/"SHA256SUMS").write_text("".join(f"{sha256(p)}  {p.name}\n" for p in sorted(RELEASE.iterdir()) if p.name!="SHA256SUMS"))
print(json.dumps({p.name:p.stat().st_size for p in RELEASE.iterdir()},indent=2))
