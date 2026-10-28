# Zenodo preservation record

Version **1.0.1** is preserved on Zenodo at **[10.5281/zenodo.23740923](https://doi.org/10.5281/zenodo.23740923)**. The [record page](https://zenodo.org/records/23740923) provides the dataset metadata and downloads. The [two papers in the README](../README.md#cite) provide the recommended scholarly citations.

## Deposited files

The deposit contains six files from the version 1.0.1 collection:

| File | Contents |
| --- | --- |
| `intrade-original-snapshot.zip` | Original contract spreadsheet, two CSV archives, historical notes, API reference and datastore snapshot |
| `intrade-research-v1.0.1.zip` | CSV/Parquet tables, documentation, citations, provenance, quality report and checksums |
| `RIGHTS.md` | Rights and attribution statement for source material and archive tooling |
| `ORIGINAL_SHA256SUMS` | SHA-256 hashes of the original data and reference files |
| `quality_report.json` | Record counts, observed dates and quality diagnostics |
| `SHA256SUMS` | Release asset checksums |

The research ZIP includes `BUILD.json`, `original-manifest.json`, `contracts.csv`, `source_files.csv`, and the full CSV/Parquet research tables. `BUILD.json` identifies the build commit and dependency versions.

The [GitHub release](https://github.com/ipeirotis/Intrade-Archive/releases/tag/v1.0.1) also provides the standalone catalogs and manifests, plus `intrade-explorer-v1.0.1.zip` for offline browsing.

## Checksum verification

The deposited `SHA256SUMS` lists all GitHub release assets. Verify the entries whose filenames match the files downloaded from Zenodo. The research ZIP contains its own `SHA256SUMS` covering its packaged contents. After extracting the original snapshot, use `ORIGINAL_SHA256SUMS` to verify the original data and reference files.

## Metadata

- **Resource type:** Dataset
- **Title:** Intrade Historical Archive
- **Creator:** Ipeirotis, Panos
- **Affiliation:** New York University, Stern School of Business
- **Version:** 1.0.1
- **DOI:** https://doi.org/10.5281/zenodo.23740923
- **Publication date:** 2026-10-28
- **Keywords:** prediction markets; Intrade; historical data; forecasting; political forecasting
- **Related source:** https://github.com/ipeirotis/Intrade-Archive
- **Historical source commit:** 620380b2ade7504e270239ecf98b68995c384aa8

## Description

Historical Intrade contract metadata, daily market summaries and individual trade records. The archive contains 17,295 contracts across 22 category labels, 2,129,385 trade records and 2,035,482 daily records. Observed dates span 7 November 2002 to 10 March 2013.

The collection includes original archives, reproducible CSV/Parquet tables, source provenance and quality reports. Twelve contracts have missing trade files and supplemental daily closing-price histories. Exports retain source values and repeated trade tuples, and label each daily source separately. The original datastore snapshot and XML API reference accompany the data.

## Related papers

1. Archak, Nikolay, and Panagiotis G. Ipeirotis. 2009. **[Modeling Volatility in Prediction Markets](https://doi.org/10.1145/1566374.1566414)**. In *Proceedings of the 10th ACM Conference on Electronic Commerce (EC '09)*, 275–284. ACM.
2. Archak, Nikolay, and Panagiotis G. Ipeirotis. 2010. **[Modeling Dependency in Prediction Markets](https://ssrn.com/abstract=1718942)**. NYU Center for Digital Economy Research Working Paper No. CeDER-10-05, December. [NYU record](https://archive.nyu.edu/handle/2451/29885).

The deposit cites both papers and links to the repository documentation.

## Rights

The record includes the MIT license for archive tooling and the custom **Intrade archive rights statement**. [RIGHTS.md](../RIGHTS.md) describes the scope: reuse terms for the historical source data and derived tables are unspecified in the original archive; research tooling is licensed under MIT.

## Future versions

1. Validate and publish a new GitHub release when the archived files change.
2. Use **New version** on the existing Zenodo record and upload the matching release files.
3. Verify checksums, update the version and descriptive metadata, and retain the paper citations and applicable rights statements.
4. Publish the new version and record its assigned version DOI.
5. Update `CITATION.cff`, the README and the explorer with the new dataset DOI. Retain earlier releases and deposits as versioned snapshots.
