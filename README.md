# Intrade Historical Archive

[![DOI: 10.5281/zenodo.23740923](https://zenodo.org/badge/DOI/10.5281/zenodo.23740923.svg)](https://doi.org/10.5281/zenodo.23740923)
[![Build and validation](https://github.com/ipeirotis/Intrade-Archive/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/ipeirotis/Intrade-Archive/actions/workflows/build.yml)
[![Latest release](https://img.shields.io/github/v/release/ipeirotis/Intrade-Archive)](https://github.com/ipeirotis/Intrade-Archive/releases/latest)
[![Explorer: browse data](https://img.shields.io/badge/explorer-browse%20data-175a4a)](https://ipeirotis.github.io/Intrade-Archive/)
[![Cite both papers](https://img.shields.io/badge/cite-both%20papers-blue)](#cite)

Historical Intrade prediction-market data curated by **Panos Ipeirotis, NYU Stern**.

This archive contains contract metadata, daily market summaries and individual trade records, with observed dates spanning **7 November 2002 to 10 March 2013**. The original data were added on **2 April 2013**. Research tools convert the source files into CSV and Parquet tables, report coverage, and power a searchable explorer.

**Dataset DOI:** [10.5281/zenodo.23740923](https://doi.org/10.5281/zenodo.23740923) · **[Cite both papers and the dataset](#cite)** · **[BibTeX](references.bib)**

## Start here

- **[Explore the archive](https://ipeirotis.github.io/Intrade-Archive/)** — search contracts, view daily prices and download source observations.
- **[Preserved dataset on Zenodo](https://doi.org/10.5281/zenodo.23740923)** — version 1.0.1 source archives, research tables and provenance.
- **[Download research tables and the offline explorer](https://github.com/ipeirotis/Intrade-Archive/releases)** — versioned exports, checksums and coverage reports.
- **[Original source files](data/)** — the contract spreadsheet, two ZIP archives, historical notes and datastore snapshot.
- **[Data dictionary](docs/DATA_DICTIONARY.md)** — fields, units, time conventions and source formats.
- **[Quality report](docs/QUALITY_REPORT.md)** — inventory and diagnostics for 2,129,385 trade records and 2,035,482 daily records.
- **[Coverage](docs/COVERAGE.md)** — availability by contract and source.
- **[Reproduce the exports](#reproduce-the-exports)** — build the research tables and explorer locally.
- **[Citations](#cite)** and **[rights](RIGHTS.md)**.
- **[Preservation and releases](docs/PRESERVATION.md)** — source hashes, versioned downloads and website hosting.

## Cite

When using this archive in research, please cite **both papers** below and the **dataset version**.

1. Archak, Nikolay, and Panagiotis G. Ipeirotis. 2009. **[Modeling Volatility in Prediction Markets](https://doi.org/10.1145/1566374.1566414)**. In *Proceedings of the 10th ACM Conference on Electronic Commerce (EC '09)*, 275–284. ACM.
2. Archak, Nikolay, and Panagiotis G. Ipeirotis. 2010. **[Modeling Dependency in Prediction Markets](https://ssrn.com/abstract=1718942)**. NYU Center for Digital Economy Research Working Paper No. CeDER-10-05, December. [NYU record](https://archive.nyu.edu/handle/2451/29885).

[Download BibTeX for all three citations](references.bib). [Citation guide](CITATION.md) · [Machine-readable metadata](CITATION.cff).

For reproducibility, also cite the dataset version used:

> Ipeirotis, Panos. 2026. *Intrade Historical Archive* (Version 1.0.1) [Data set]. Zenodo. [https://doi.org/10.5281/zenodo.23740923](https://doi.org/10.5281/zenodo.23740923)

The [Zenodo deposit](https://doi.org/10.5281/zenodo.23740923) preserves the original data and research exports. Record the release tag or commit when using a later repository revision.

## Archive contents

The contract spreadsheet contains **17,295 rows** across **22 category labels**. The main ZIP contains **28,108 CSV files**. The supplemental ZIP supplies **12 daily closing-price histories**. Each contract's coverage record reports available files, observed row counts and dates.

| Original file | Contents |
| --- | --- |
| `data/Intrade-Contracts-NoFinancial.xlsx` | Contract metadata and reported summary/trade counts |
| `data/Intrade-Contracts-NoFinancial.zip` | Per-contract market summaries and trade records |
| `data/Intrade-Contracts-WithClosingPrices-MissingTrades.zip` | Supplemental daily closing-price histories for 12 contracts |
| `data/Notes.txt` | Historical list of 12 missing trade files |
| `docs/Intrade_XML_API.pdf` | Historical Intrade XML API reference |
| `data/legacy-datastore.bin` | Original Java-serialized datastore snapshot |

The `NoFinancial` filenames are inherited from the source collection. The spreadsheet's category labels identify the available subjects, including economic subjects and test markets.

## Working with the data

- Twelve contracts have missing trade files, including Obama and Romney's individual 2012 presidential contracts. Supplemental daily prices are available for these contracts.
- Main summaries and supplemental closing prices have distinct source formats. The research tables label each source, and the explorer displays separate chart series.
- Source values include zero prices, zero-volume days, repeated closes and repeated trade tuples. The exports retain these observations and provide quality flags.
- Trade timestamps use UTC. Daily observations retain their source business dates.
- The metadata supply contract end dates. The data dictionary describes each date field and its source.
- Coverage records compare observed counts with spreadsheet counts and identify the first and last available observation.

## Reproduce the exports

Use Python 3.12 and Node.js for the build and website syntax check.

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

Open `http://localhost:8000` for the explorer.

The build verifies the original data hashes and ZIP CRCs, parses the source records, and writes:

| Output in `build/research/` | Contents |
| --- | --- |
| `contracts.csv`, `contracts.parquet` | Metadata, observed coverage, counts and quality flags |
| `daily.csv.gz`, `daily.parquet` | Daily observations labeled by source |
| `trades.csv.gz`, `trades.parquet` | Individual trade records, with repeated tuples flagged |
| `source_files.csv` | Source-file IDs, archive/member paths, encoding, row counts and SHA-256 |
| `quality_report.json` | Aggregate inventory and diagnostics |
| `original-manifest.json`, `ORIGINAL_SHA256SUMS` | Inventory and checksums for the original data and API reference |
| `SHA256SUMS` | Checksums for the generated research files |

`build/site/` contains the searchable explorer, original contract-file downloads and daily chart series. Release packages add documentation, citations and build metadata.

## Use and contribute

Use the coverage catalog to define your sample before loading observations. [The worked example](examples/README.md) selects a contract whose observed row counts reconcile with the metadata, then demonstrates a filtered Parquet read.

Report corrections with the contract ID, archive name, source member, source row and supporting evidence. Add corrections as documented derived data while preserving original source bytes. See [CONTRIBUTING.md](CONTRIBUTING.md).

The original collector is available in the repository's [2013 commit](https://github.com/ipeirotis/Intrade-Archive/tree/620380b2ade7504e270239ecf98b68995c384aa8). The current repository contains the archived data and research tools.
