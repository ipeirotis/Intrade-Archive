"""Package original data, research tables and the static explorer."""
import json
import os
from pathlib import Path
from build_archive import ROOT, OUT, DATA, SITE, deterministic_zip, sha256
from release_links import rewrite_markdown_links
RELEASE=OUT/"release"
RELEASE.mkdir(parents=True,exist_ok=True)
VERSION="1.0.1"
metadata={"version":VERSION,"source_commit":"620380b2ade7504e270239ecf98b68995c384aa8","build_commit":os.environ.get("GITHUB_SHA","local-unrecorded"),"python_dependencies":(ROOT/"requirements.txt").read_text(),"timestamp_policy":"Trade CSV timestamps use UTC; daily observations use business dates."}
(DATA/"BUILD.json").write_text(json.dumps(metadata,indent=2)+"\n")
documents = ["README.md","RIGHTS.md","LICENSE","CITATION.cff","references.bib","CONTRIBUTING.md",
             "docs/DATA_DICTIONARY.md","docs/COVERAGE.md","docs/PRESERVATION.md",
             "docs/QUALITY_REPORT.md","docs/ZENODO_DEPOSIT.md"]
bundled_files = {name: Path(name).name for name in documents}
for source, destination in bundled_files.items():
    if source.endswith(".md"):
        text = rewrite_markdown_links((ROOT/source).read_text(encoding="utf-8"), source,
                                      bundled_files, ROOT, os.environ.get("GITHUB_SHA", "master"))
        (DATA/destination).write_text(text, encoding="utf-8")
    else:
        (DATA/destination).write_bytes((ROOT/source).read_bytes())
(DATA/"SHA256SUMS").write_text("".join(f"{sha256(p)}  {p.name}\n" for p in sorted(DATA.iterdir()) if p.is_file() and p.name!="SHA256SUMS"))
deterministic_zip(RELEASE/f"intrade-research-v{VERSION}.zip",((p.name,p.read_bytes()) for p in sorted(DATA.iterdir()) if p.is_file()))
deterministic_zip(RELEASE/f"intrade-explorer-v{VERSION}.zip",((str(p.relative_to(SITE)).replace(os.sep,"/"),p.read_bytes()) for p in sorted(SITE.rglob("*")) if p.is_file()))
manifest=json.loads((ROOT/"preservation/original-manifest.json").read_text())
deterministic_zip(RELEASE/"intrade-original-snapshot.zip",((item["path"],(ROOT/item["path"]).read_bytes()) for item in manifest["files"]))
for name in ["quality_report.json","contracts.csv","source_files.csv","original-manifest.json","ORIGINAL_SHA256SUMS","BUILD.json"]:
    (RELEASE/name).write_bytes((DATA/name).read_bytes())
(RELEASE/"SHA256SUMS").write_text("".join(f"{sha256(p)}  {p.name}\n" for p in sorted(RELEASE.iterdir()) if p.name!="SHA256SUMS"))
print(json.dumps({p.name:p.stat().st_size for p in RELEASE.iterdir()},indent=2))
