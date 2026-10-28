# Preservation, releases and hosting

## Historical baseline

Original commit: `620380b2ade7504e270239ecf98b68995c384aa8` (2 April 2013).

`preservation/original-manifest.json` records every original tracked file's path, byte length and Git blob SHA-1. The build verifies that baseline before conversion and produces SHA-256 checksums as an independent integrity record. No original files, generated classes, JARs or datastore bytes have been removed.

The release contains:

- `intrade-original-snapshot.zip`: every file in the historical baseline.
- `intrade-research-v1.0.0.zip`: documented CSV/Parquet tables, provenance, diagnostics and checksums.
- `intrade-explorer-v1.0.0.zip`: a self-contained static explorer with contract-specific original CSV downloads.
- Separately downloadable coverage catalog, source-file manifest, quality report and checksum files.

An original contract download contains unchanged CSV member bytes; filenames are prefixed with the source-file ID to avoid collisions. The source-file manifest records their original ZIP paths.

Generated ZIP member timestamps and gzip timestamps are fixed for reproducibility. `BUILD.json` records the build commit. Exact Parquet bytes depend on the pinned writer/dependency version; row-level semantic equality matters across toolchain upgrades.

## Datastore inspection

The original datastore is a Java serialization stream, version 1, with SHA-256:

`ae9e86112f9953a70bdce91c427ff3cf58e688e81d0e5611dd261bf3d3ffb9e7`

A filtered read of the stream reaches EOF before a complete top-level object can be read. The inspection tool reports completed object/byte-array counts and an explicit incomplete-stream status. It does not modify the source or claim a complete recovery.

No missing trades are filled from this snapshot. The failed complete read does not imply that every embedded record is unusable; partial recovery is a separate research task. Preserve the file because it may contain unique material. Its relationship to the later CSV exports remains unverified.

Run the inspection with Java 17 or later:

```bash
java scripts/InspectDatastore.java war/WEB-INF/appengine-generated/local_db.bin
```

The tool permits only standard Java utility/language classes and arrays during deserialization and does not launch the legacy application.

## Release procedure

The build workflow validates sources, tests parsers, converts all files, runs the loading example and checks the explorer. Only successful builds may publish a versioned release. Existing release assets must not be silently replaced. Change the version and describe any changes to derived data when making another release.

Keep known source anomalies as explicit flags; do not rewrite the original snapshot.

## Static website

The explorer is buildless HTML/CSS/JavaScript. It makes no external analytics requests and requires no backend or live Intrade connection.

Serve the downloaded explorer folder with:

```bash
python -m http.server 8000 --directory intrade-explorer
```

For GitHub Pages, set **Settings → Pages → Build and deployment → Source → GitHub Actions**. The repository includes a deployment job for the generated site. A deployment is not considered published until GitHub returns a successful Pages deployment and URL.

## DOI deposit

See [ZENODO_DEPOSIT.md](ZENODO_DEPOSIT.md). A GitHub release alone does not create a DOI. Publish the actual deposit under an authenticated preservation account, record its assigned DOI, then update `CITATION.cff` and the README. Do not use a placeholder DOI.

The inspection completed 2,131,476 byte arrays (29,198,752 array-content bytes) before EOF. Those counts describe serialization objects, not recovered contracts or trades; the arrays have not been semantically reconciled to the CSV exports.
