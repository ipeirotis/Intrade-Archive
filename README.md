# Intrade Historical Archive

Historical Intrade prediction-market data curated by **Panos Ipeirotis, NYU Stern**.

This repository preserves the original archival files added on **2 April 2013**, the legacy Java/Google App Engine collector, and modern tools for inspecting and reusing the surviving observations. It is a historical research collection; it does not collect current markets or promise complete coverage of Intrade.

## Start here

- **[Download research tables and the offline explorer](https://github.com/ipeirotis/Intrade-Archive/releases)** — versioned CSV/Parquet exports, checksums, and coverage reports.
- **[Original source files](data/)** — the spreadsheet and ZIPs remain byte-for-byte unchanged.
- **[Data dictionary](docs/DATA_DICTIONARY.md)** — fields, units, time conventions and source differences.
- **[Coverage and limitations](docs/COVERAGE.md)** — what missing files and suspicious observations mean.
- **[Reproduce the exports](#reproduce-the-exports)** — one local build, no Intrade service required.
- **[Citation](CITATION.cff)** and **[rights](RIGHTS.md)**.
- **[Preservation and release process](docs/PRESERVATION.md)** — immutable sources, checksums, DOI deposit and website hosting.

## What survives

The original spreadsheet contains **17,295 contract rows** across **22 category labels**. The main ZIP contains **28,108 CSV files**. A separate ZIP supplies **12 supplemental daily closing-price histories** for contracts whose trade files are listed as missing. File counts are not trade counts, and the spreadsheet's reported counts are not proof that those observations survived.

| Original file | Role |
| --- | --- |
| `data/Intrade-Contracts-NoFinancial.xlsx` | Contract metadata and reported summary/trade counts |
| `data/Intrade-Contracts-NoFinancial.zip` | Per-contract market summaries and trade records |
| `data/Intrade-Contracts-WithClosingPrices-MissingTrades.zip` | Supplemental daily closing-price histories; not recovered trades |
| `data/Notes.txt` | Historical list of 12 missing trade files |
| `docs/Intrade_XML_API.pdf` | Historical API reference, not a complete specification of the later data-warehouse exports |
| `war/WEB-INF/appengine-generated/local_db.bin` | Preserved legacy datastore snapshot; treated separately from the research exports |

The `NoFinancial` label is inherited from the source files. The precise historical exclusion rule is not recorded. Some surviving categories concern economic subjects, and test markets are present. Select your research population explicitly.

## Important limitations

- Missing trades include Obama and Romney's individual 2012 presidential contracts. Supplemental daily prices do **not** reconstruct their trade histories.
- Main summaries and supplemental closing prices use different source formats. They stay separate in the normalized tables and charts, including overlapping dates.
- Zero prices, zero-volume days, stale closes and repeated trade tuples are retained. They must not be silently interpreted as probabilities, missing values, or duplicate errors.
- Trade timestamps are explicitly labeled UTC in their CSV header. Daily business dates have no verified time-zone definition and remain dates.
- Contract end dates are not verified settlement dates. The spreadsheet does not provide settlement prices or outcomes.
- Reported count discrepancies and coverage endpoints describe the available files; they do not certify historical completeness.

## Reproduce the exports

Use Python 3.12 and Node.js (Node is used only for the website syntax check).

```bash
git clone https://github.com/ipeirotis/Intrade-Archive.git
cd Intrade-Archive
python -m venv .venv
# Activate .venv using the command appropriate to your shell.
python -m pip install -r requirements.txt
python -m unittest discover -s tests -v
python scripts/build_archive.py
python examples/load_contract.py
python -m http.server 8000 --directory build/site
```

Open `http://localhost:8000` for the explorer. Opening its HTML as a `file://` URL does not support the data fetches.

The build verifies every original file against the historical Git blob hashes, validates ZIP CRCs, parses all source records, and writes:

| Output in `build/research/` | Contents |
| --- | --- |
| `contracts.csv`, `contracts.parquet` | Metadata, observed coverage, counts and quality flags |
| `daily.csv.gz`, `daily.parquet` | Both daily sources, labeled separately |
| `trades.csv.gz`, `trades.parquet` | Individual source trade records; repeated tuples retained and flagged |
| `source_files.csv` | Source-file IDs, archive/member paths, encoding, row counts and SHA-256 |
| `quality_report.json` | Aggregate inventory and diagnostics |
| `original-manifest.json`, `ORIGINAL_SHA256SUMS` | Checksums for all original files |
| `SHA256SUMS` | Checksums for the generated research deliverables |

`build/site/` contains the searchable static explorer, contract-specific original-file ZIPs and daily chart series. Build products are generated, not committed into Git history.

## Use and contribute

Use the coverage catalog to define your sample before loading observations. [The worked example](examples/README.md) selects a contract whose observed row counts reconcile with the metadata, then demonstrates a filtered Parquet read.

Please report corrections with the contract ID, archive name, source member, source row and evidence. Preserve original files and add corrections as separately documented derived data. See [CONTRIBUTING.md](CONTRIBUTING.md).

The old collector in `src/` and `war/` is retained as historical evidence. The modern build does not run its servlets, contact old Intrade endpoints or require Google App Engine.

## Cite

Use GitHub's **Cite this repository** button, or cite:

> Ipeirotis, Panos. *Intrade Historical Archive*. Version 1.0.0. https://github.com/ipeirotis/Intrade-Archive

Also record the release tag or commit used. A DOI must be added only after an actual preservation deposit has been published.
