# Coverage

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

The supplemental ZIP provides daily closing-price histories for these 12 contracts. Their trade files are missing from the archive.

## Population and selection

The source filenames use the label `NoFinancial`. The surviving categories include Fine Wine, Real Estate and Test: Company Valuation. Test events are also present in the metadata. The catalog retains the original category labels for researchers to define their sample.

Coverage dates identify the first and last observed records. The catalog records each contract's spreadsheet end date separately. Trade coverage distinguishes missing files, empty files and metadata-reported zero trades; daily volume is recorded for each daily observation.

## Price interpretation

Daily records include repeated closes, price changes on zero-volume days, and zero closes alongside positive session prices. Zero-volume OHLC fields differ between the main summary and supplemental closing-price formats. The exports retain these values and identify each source. Document the missingness and staleness rules used in your analysis.

The exports flag and retain every row with an identical timestamp, price and quantity within a contract. Source timestamps have second precision.

## Legacy datastore

The 136,297,819-byte `data/legacy-datastore.bin` is the original serialized Java datastore snapshot. Its checksum is recorded alongside the CSV sources. The datastore inspection report in the preservation documentation describes the stream and its incomplete top-level object.

## How to use the quality report

Compare the per-contract observed counts with the spreadsheet-reported counts. Inspect repeated trade tuples and overlapping daily sources before aggregation. Use source IDs and record numbers to trace each observation to its archived CSV.
