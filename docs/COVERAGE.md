# Coverage and limitations

The original spreadsheet has 17,295 contract rows and 22 distinct category labels. The main ZIP has 28,108 CSV members; the supplemental ZIP has 12. Observed counts and date ranges are computed from source records during every build and published in `quality_report.json` and the contract catalog.

## Known missing trade files

These IDs come directly from `data/Notes.txt`.

| Contract symbol | ID |
| --- | --- |
| 2012.PRES.OBAMA | 743474 |
| 2012.PRES.PAUL(RON) | 743483 |
| 2012.PRES.ROMNEY | 743475 |
| 2012.REP.NOM.DANIELS | 695192 |
| 2012.REP.NOM.GINGRICH | 654836 |
| 2012.REP.NOM.HUNTSMAN | 658927 |
| 2012.REP.NOM.PALIN | 652756 |
| 2012.REP.NOM.PAUL(RON) | 669534 |
| 2012.REP.NOM.PERRY | 656777 |
| 2012.REP.NOM.ROMNEY | 652757 |
| 2012.REP.NOM.SANTORUM | 690905 |
| PRESIDENT.DEM.2012 | 639648 |

The supplemental ZIP provides daily closing-price histories. It does not fill missing executions or imply that these are the only missing observations.

## Population and selection

The source names describe the collection as excluding financial contracts, but no precise selection script or exclusion specification accompanies the exports. The surviving categories include Fine Wine, Real Estate and Test: Company Valuation. Test events are also visible in the metadata. Keep the original category labels and define any research-specific exclusions explicitly.

Coverage dates are observation extrema, not continuous coverage guarantees. A contract's spreadsheet end date is not an independently verified resolution date. Absence of a file, an empty file, zero reported trades, and a no-volume daily record are different states.

## Price interpretation

Daily closes may be carried forward, revised without same-day volume, or zero despite nonzero session prices. Zero-volume OHLC fields also differ between the main summary and supplemental closing-price formats. Preserve raw values; document your own missingness/staleness rules.

Identical trade timestamps, prices and quantities are flagged but retained. Source timestamps have second precision, so repeated tuples can represent real separate trades.

## Legacy datastore

The 136,297,819-byte `local_db.bin` is an original serialized Java datastore snapshot. Preserve it and its checksum independently of the CSV exports. See the datastore inspection report in the preservation documentation for the exact recovery status. Its existence alone does not establish that it contains the missing 2012 trades.

## How to use the quality report

Compare the per-contract observed counts with the spreadsheet-reported counts. Inspect duplicate candidates and source overlap before aggregation. Use source IDs and record numbers to reproduce anomalies. Do not interpret a clean parser run as certification of market completeness, price validity, or settlement outcomes.
