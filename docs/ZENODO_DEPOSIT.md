# Zenodo deposit guide

Zenodo can preserve a frozen release of the Intrade data and assign it a dataset DOI. The [two papers in the README](../README.md#cite) provide the recommended scholarly citations.

## Files to deposit

Use files from the same [GitHub release](https://github.com/ipeirotis/Intrade-Archive/releases). For version 1.0.1, upload:

| File | Contents |
| --- | --- |
| `intrade-original-snapshot.zip` | Original contract spreadsheet, two CSV archives, historical notes, API reference and datastore snapshot |
| `intrade-research-v1.0.1.zip` | CSV/Parquet tables, documentation, citations, provenance, quality report and checksums |
| `original-manifest.json` and `ORIGINAL_SHA256SUMS` | Original-file inventory and hashes |
| `quality_report.json` | Record counts, observed dates and quality diagnostics |
| `BUILD.json` | Build commit and dependency versions |
| `SHA256SUMS` | Release asset checksums |

The optional `intrade-explorer-v1.0.1.zip` adds the searchable explorer and contract downloads. The optional `contracts.csv` and `source_files.csv` offer direct access to the catalogs included in the research ZIP.

`SHA256SUMS` covers all GitHub release assets. When depositing a subset, verify the entries corresponding to the uploaded files.

## Metadata

- **Resource type:** Dataset
- **Title:** Intrade Historical Archive
- **Creator:** Ipeirotis, Panos
- **Affiliation:** New York University, Stern School of Business
- **Version:** 1.0.1
- **Publication date:** Use the deposit's publication date.
- **Keywords:** prediction markets; Intrade; historical data; forecasting
- **Related source:** https://github.com/ipeirotis/Intrade-Archive
- **Historical source commit:** 620380b2ade7504e270239ecf98b68995c384aa8

## Description

Historical Intrade contract metadata, daily market summaries and individual trade records. The archive contains 17,295 contracts across 22 category labels, 2,129,385 trade records and 2,035,482 daily records. Observed dates span 7 November 2002 to 10 March 2013.

The collection includes original archives, reproducible CSV/Parquet tables, source provenance and quality reports. Twelve contracts have missing trade files and supplemental daily closing-price histories. Exports retain source values and repeated trade tuples, and label each daily source separately. The original datastore snapshot and XML API reference accompany the data.

## Related papers

1. Archak, Nikolay, and Panagiotis G. Ipeirotis. 2009. **[Modeling Volatility in Prediction Markets](https://doi.org/10.1145/1566374.1566414)**. In *Proceedings of the 10th ACM Conference on Electronic Commerce (EC '09)*, 275–284. ACM.
2. Archak, Nikolay, and Panagiotis G. Ipeirotis. 2010. **[Modeling Dependency in Prediction Markets](https://ssrn.com/abstract=1718942)**. NYU Center for Digital Economy Research Working Paper No. CeDER-10-05, December. [NYU record](https://archive.nyu.edu/handle/2451/29885).

Add the papers as related publications in the deposit metadata.

## Rights

Use [RIGHTS.md](../RIGHTS.md) to distinguish the original data from the MIT-licensed research tools. Select deposit rights and access terms supported by the depositor's permissions.

## Publish

1. Create a Dataset deposit in the intended Zenodo account.
2. Upload the release files and verify their checksums.
3. Add the metadata, related papers and applicable rights/access terms.
4. Publish the deposit and record its assigned version DOI.
5. Add the dataset DOI to the archive metadata in `CITATION.cff` and link the deposit in the README.
6. Publish subsequent releases as new deposit versions.
