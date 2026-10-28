# Data dictionary

## Table conventions

IDs are strings. Empty CSV cells / Parquet nulls represent unavailable values; numeric zero remains zero. The exports retain source observations, original price units, observed dates and repeated trade tuples.

`source_file_id` joins to `source_files.csv`. `source_row` is the 1-based CSV **record number including the header** (first data record = 2). A record with embedded newlines spans multiple physical lines. Original raw bytes remain downloadable for exact inspection.

## Contract catalog: contracts.csv / contracts.parquet

One row per spreadsheet contract ID.

| Field | Meaning |
| --- | --- |
| contract_id | Spreadsheet `ID`; join key for observations |
| category, event_group, event | Original classification labels |
| event_id, symbol, name | Original event ID, market symbol and market name |
| end_date | Original spreadsheet end-date value |
| reported_summary_count, reported_trade_count | Spreadsheet counts, distinct from observed counts |
| summary_url, trades_url | Historical source URLs, retained as provenance |
| original_file_stem, metadata_row | Original spreadsheet path label and Excel row number |
| settlement_status, settlement_price | Unavailable settlement metadata; null price |
| trade_status | See status definitions below |
| known_missing_trade_file | Whether the ID appears in original Notes.txt |
| trade_rows, summary_rows, supplemental_daily_rows, daily_rows | Parsed record counts; daily_rows includes both labeled daily sources |
| first_observed_date, last_observed_date | Earliest/latest daily date or UTC trade date |
| first_trade_utc, last_trade_utc | First/last surviving trade timestamp |
| first_daily_date, last_daily_date | First/last date across surviving daily sources |
| trade_count_mismatch, summary_count_mismatch | Observed main-archive counts differ from metadata; supplemental rows are counted separately |
| duplicate_candidate_trade_rows | Number of rows participating in repeated timestamp/price/quantity tuples |
| duplicate_daily_keys | Excess rows sharing source type and date within the contract |
| overlapping_daily_source_dates | Dates present in both main summary and supplemental closing-price series |
| zero_volume_daily_rows | Daily rows reporting zero volume |
| price_outside_0_100_rows | Retained rows with at least one checked price outside 0-100 |
| source_file_ids | Semicolon-delimited list joining to source_files.csv |

Trade status precedence: `observed` for contracts with trade rows; `empty_file` for an existing file with zero rows; `known_missing` for a missing file listed in Notes.txt; `missing_expected` for a missing file whose metadata reports positive trades; otherwise `metadata_reports_zero` for a spreadsheet-reported count of zero.

## Trades: trades.csv.gz / trades.parquet

| Export field | Original field / interpretation |
| --- | --- |
| contract_id | market id (also validated against filename) |
| timestamp_utc | date/time [utc], parsed from English month/day/year and time into ISO 8601 with Z |
| price | trade price, in original price points |
| quantity | trade quantity, integer number of source-reported contracts |
| duplicate_candidate | True for every member of a repeated timestamp/price/quantity tuple within a contract |
| source_file_id, source_row | Exact provenance link |

The source records have second-resolution timestamp text. The `duplicate_candidate` flag identifies equal timestamp, price and quantity values. All flagged rows remain in the exports.

## Daily observations: daily.csv.gz / daily.parquet

| Export field | Main summary source | Supplemental source |
| --- | --- | --- |
| date | business day (YYYY-MM-DD) | Date (English month, day, year) |
| open | session open | Open |
| high | session high | High |
| low | session low | Low |
| close | closing price | Close |
| volume | session trade volume | Volume |
| open_interest | open interest | Unavailable |
| lifetime_high, lifetime_low | lifetime high, lifetime low | Unavailable |
| source_type | summary | closingPrice |
| contract_id | market id, checked against filename | Derived from filename |
| source_file_id, source_row | Provenance | Provenance |

Daily dates remain calendar dates in the exports. Charts display the two source types as separate series, including overlapping dates. Main summaries include closing-price changes on zero-volume days and zero closes alongside positive session prices. Interpret each series using its source fields.

Prices stay in source units. The 0-100 range check flags values outside the common binary-market scale for inspection. Contract-specific interpretation requires the market's original terms.

## Source-file manifest

`source_file_id` is deterministic for a particular source inventory: archive filenames and member names are sorted before assignment. `archive`, `member`, `contract_id`, `source_type`, `bytes`, `sha256`, `encoding`, and `rows` describe each CSV member. The parser decodes as UTF-8 (accepting a BOM), falling back to Windows-1252 on a UTF-8 decoding error; any remaining decoding error stops the build. The `encoding` field records the successful decoder. Existing question-mark substitutions remain as supplied in the source text.

## Build verification

The build verifies source hashes, schemas, dates, numeric parsing, IDs, ZIP integrity and output row totals. It flags repeated keys and count mismatches. The parser maps the fields in the archived CSV headers to the export fields documented above.

## Malformed source names and empty files

Some main-archive records contain unquoted commas in the market-name field. When a record has excess columns, the parser preserves the first two fields and the final nine (summary) or three (trade) fields, joining the intervening fields as the market name. It then validates IDs, dates and numeric fields. Every affected observation has `source_name_comma_repair=true`; counts appear in the file manifest and contract diagnostics. This repair applies exclusively to excess-column records in the main archive. Unexpected schemas or invalid typed values stop the build. Empty source files yield zero observations and remain in the manifest.
