# Data dictionary

## Table conventions

IDs are strings. Empty CSV cells / Parquet nulls represent unavailable values; numeric zero remains zero. All source observations are retained. No prices are converted to probabilities, no missing days are filled, no outcomes are inferred, and no trade tuples are deduplicated.

`source_file_id` joins to `source_files.csv`. `source_row` is the 1-based CSV **record number including the header** (first data record = 2), not necessarily a physical line number when a quoted field contains newlines. Original raw bytes remain downloadable for exact inspection.

## Contract catalog: contracts.csv / contracts.parquet

One row per spreadsheet contract ID.

| Field | Meaning |
| --- | --- |
| contract_id | Spreadsheet `ID`; join key for observations |
| category, event_group, event | Original classification labels; not merged or reclassified |
| event_id, symbol, name | Original event ID, market symbol and market name |
| end_date | Spreadsheet end date as supplied; time-zone and settlement semantics unverified |
| reported_summary_count, reported_trade_count | Spreadsheet counts, distinct from observed counts |
| summary_url, trades_url | Historical source URLs, retained as provenance; not queried by the build |
| original_file_stem, metadata_row | Original spreadsheet path label and Excel row number |
| settlement_status, settlement_price | Explicitly unavailable in this metadata source; null price |
| trade_status | See status definitions below |
| known_missing_trade_file | Whether the ID appears in original Notes.txt |
| trade_rows, summary_rows, supplemental_daily_rows, daily_rows | Parsed record counts; daily_rows includes both labeled daily sources |
| first_observed_date, last_observed_date | Extrema of daily dates and UTC trade dates, not proof of continuous coverage |
| first_trade_utc, last_trade_utc | First/last surviving trade timestamp |
| first_daily_date, last_daily_date | First/last date across surviving daily sources |
| trade_count_mismatch, summary_count_mismatch | Observed main-archive counts differ from metadata; supplemental rows do not replace main counts |
| duplicate_candidate_trade_rows | Number of rows participating in repeated timestamp/price/quantity tuples |
| duplicate_daily_keys | Excess rows sharing source type and date within the contract |
| overlapping_daily_source_dates | Dates present in both main summary and supplemental closing-price series |
| zero_volume_daily_rows | Daily rows reporting zero volume |
| price_outside_0_100_rows | Rows with at least one checked price outside 0–100; diagnostic, not automatic rejection |
| source_file_ids | Semicolon-delimited list joining to source_files.csv |

Trade status precedence: `observed` if trade rows survive; `empty_file` if a trade file exists with no rows; `known_missing` if listed in Notes.txt and no trade file survives; `missing_expected` if metadata reports positive trades but no file survives; otherwise `metadata_reports_zero`. The last state does not independently prove that no trades occurred.

## Trades: trades.csv.gz / trades.parquet

| Export field | Original field / interpretation |
| --- | --- |
| contract_id | market id (also validated against filename) |
| timestamp_utc | date/time [utc], parsed from English month/day/year and time into ISO 8601 with Z |
| price | trade price, in original price points |
| quantity | trade quantity, integer number of source-reported contracts |
| duplicate_candidate | True for every member of a repeated timestamp/price/quantity tuple within a contract |
| source_file_id, source_row | Exact provenance link |

The source records have second-resolution timestamp text. Identical tuples may be distinct legitimate executions. The flag is descriptive; it does not authorize deletion.

## Daily observations: daily.csv.gz / daily.parquet

| Export field | Main summary source | Supplemental source |
| --- | --- | --- |
| date | business day (YYYY-MM-DD) | Date (English month, day, year) |
| open | session open | Open |
| high | session high | High |
| low | session low | Low |
| close | closing price | Close |
| volume | session trade volume | Volume |
| open_interest | open interest | Not supplied |
| lifetime_high, lifetime_low | lifetime high, lifetime low | Not supplied |
| source_type | summary | closingPrice |
| contract_id | market id, checked against filename | Derived from filename |
| source_file_id, source_row | Provenance | Provenance |

Daily dates have no assigned UTC offset. A chart combines neither the two source types nor their overlapping dates. The main source's close is not necessarily an executed last trade: closes can change on zero-volume days, and zero closes can coexist with positive session prices. The two formats must not be assumed semantically equivalent.

Prices stay in source units. The 0–100 range check reflects the common binary-market scale, but does not establish payout rules, tick value, currency or a valid probability interpretation for every contract.

## Source-file manifest

`source_file_id` is deterministic for a particular source inventory: archive filenames and member names are sorted before assignment. `archive`, `member`, `contract_id`, `source_type`, `bytes`, `sha256`, `encoding`, and `rows` describe each CSV member. Decode as UTF-8 (accepting a BOM), falling back to Windows-1252 only on a UTF-8 decoding error; any remaining decoding error stops the build. This is an operational decoding rule, not proof of the historical source encoding. Existing question-mark substitutions in source text cannot be restored by decoding.

## Verification boundaries

The build verifies source hashes, schemas, dates, numeric parsing, IDs, ZIP integrity and output row totals. It flags repeated keys and count mismatches without claiming to reconstruct all missing data. The legacy Java collector's data model is not treated as a definitive specification of the later warehouse CSVs.
