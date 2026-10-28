# Verified source inventory

Generated from the immutable 2013 source snapshot by the modernization build. The machine-readable report and per-contract flags are included with each release.

| Measure | Observed value |
| --- | ---: |
| Metadata contracts | 17,295 |
| Distinct original category labels | 22 |
| CSV members across both ZIPs | 28,120 |
| Completely empty CSV files | 12 (6 summary, 6 trade) |
| Surviving trade records | 2,129,385 |
| Main summary records | 2,021,656 |
| Supplemental daily records | 13,826 |
| Total daily records, both source types | 2,035,482 |
| Earliest observed date | 2002-11-07 |
| Latest observed date | 2013-03-10 |
| Contracts with observed trade rows | 10,815 |
| Contracts whose metadata reports zero trades and no trade file survives | 6,462 |
| Contracts with empty trade files | 6 |
| Documented missing trade files | 12 |
| Contracts with trade-count discrepancies | 18 |
| Contracts with main-summary-count discrepancies | 7 |

## Diagnostics retained in the exports

| Diagnostic | Count | Interpretation |
| --- | ---: | --- |
| Trade rows participating in repeated timestamp/price/quantity tuples | 180,382 | Potential repeated executions; not automatically erroneous duplicates |
| Excess daily rows sharing contract, source type and date | 9,202 | Retained; inspect before day-level aggregation |
| Contract/date combinations appearing in both daily source types | 13,766 | Compare separately; do not concatenate and treat as independent days |
| Daily records with zero reported volume | 1,792,290 | These include both source types and repeated dates |
| Records requiring the unquoted-market-name comma repair | 17,444 | Flagged; raw CSV bytes remain intact |
| Rows with a checked price outside 0–100 | 1,283 | A diagnostic of unusual scale/values, not a claim of invalid records |

The observed dates are extrema, not continuous coverage guarantees. In particular, 17,295 contracts does not mean 17,295 complete histories.

## Worked example verified

Contract **27824**, `EURO.SWEDEN.YES` (“Sweden to vote YES to the Euro in next referendum”), has 13 observed trades and 34 main summary records, matching the spreadsheet counts. Its observed dates run from 2003-08-11 through 2003-09-13. Twenty-seven summary rows have zero volume. This is an example of surviving-file reconciliation, not an independent completeness certification.

## Datastore result

The historical Java stream ends before its top-level object is complete. Inspection completed 2,131,476 byte arrays containing 29,198,752 bytes before EOF. These are serialization counts, not trade or contract counts. No records from this stream are incorporated into the research tables.
