# Preservation, releases and hosting

## Original data

The historical baseline is commit [`620380b2ade7504e270239ecf98b68995c384aa8`](https://github.com/ipeirotis/Intrade-Archive/tree/620380b2ade7504e270239ecf98b68995c384aa8), dated 2 April 2013.

`preservation/original-manifest.json` records the six retained original files: the contract spreadsheet, two ZIP archives, historical notes, API reference and datastore snapshot. Each entry records its byte length and original Git blob SHA-1. The datastore entry also records its historical path.

The build verifies these hashes and produces SHA-256 checksums. Version 1.0.1 moves the datastore to `data/legacy-datastore.bin` and removes the retired crawler, compiled classes, dependency JARs and deployment configuration from the current tree. The historical commit and v1.0.0 release preserve the earlier repository layout.

## Release contents

- `intrade-original-snapshot.zip`: the six retained original data and reference files.
- `intrade-research-v1.0.1.zip`: CSV/Parquet tables, documentation, citations, provenance, quality reports and checksums.
- `intrade-explorer-v1.0.1.zip`: the static explorer, chart series and original contract-file downloads.
- Separate coverage and source-file catalogs, quality report, source manifests, build metadata and checksum files.

Contract downloads contain the original CSV member bytes. Filenames carry a source-file ID prefix, and the source-file manifest maps each file to its original ZIP path.

Generated ZIP member timestamps and gzip timestamps are fixed. `BUILD.json` records the build commit and pinned Python dependencies. Validation compares CSV and Parquet values row by row.

## Datastore snapshot

The original datastore uses Java serialization. Its SHA-256 is:

`ae9e86112f9953a70bdce91c427ff3cf58e688e81d0e5611dd261bf3d3ffb9e7`

Inspection reaches EOF while reading the top-level object. Before EOF, it completes 2,131,476 byte arrays containing 29,198,752 bytes. These counts describe serialized objects. The research tables are built from the spreadsheet and CSV archives.

Run the inspection with Java 17 or later:

```bash
java scripts/InspectDatastore.java data/legacy-datastore.bin
```

The inspection tool permits standard Java utility/language classes and arrays during deserialization.

## Release procedure

1. Update the release version in the citation metadata, packaging script, workflow and documentation.
2. Run source verification, parser tests, conversion, table validation, the loading example and explorer checks.
3. Publish the versioned release with its build metadata and checksums.
4. Retain existing release assets as immutable snapshots.

Record source anomalies as quality flags. Publish corrections and transformations as documented derived data.

## Static website

The explorer uses HTML, CSS and JavaScript, with locally packaged data files. GitHub Pages hosts the public version at https://ipeirotis.github.io/Intrade-Archive/.

Serve the downloaded explorer folder with:

```bash
python -m http.server 8000 --directory intrade-explorer
```

For GitHub Pages, use **Settings → Pages → Build and deployment → Source → GitHub Actions**. The build workflow deploys the generated site after validation.

## Citations and preservation deposit

The [README](../README.md#cite) and [BibTeX file](../references.bib) provide the two recommended paper citations. Record the archive release or commit alongside those references.

Version **1.0.1** is preserved at **[10.5281/zenodo.23740923](https://doi.org/10.5281/zenodo.23740923)**. The Zenodo record contains the original snapshot ZIP, research ZIP, rights statement, quality report and checksum files.

The research ZIP includes the build metadata, original-file manifest and catalogs. The GitHub release also provides standalone copies and the offline explorer. [ZENODO_DEPOSIT.md](ZENODO_DEPOSIT.md) lists the six deposited files, explains checksum verification and describes the procedure for future versions.
