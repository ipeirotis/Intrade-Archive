# Worked example: from coverage to observations

After building the archive, run:

```bash
python examples/load_contract.py
```

The example deterministically selects the lowest numeric contract ID with more than ten surviving trades, main daily summaries, matching spreadsheet counts, unique trade tuples and unique daily keys. It prints the selected market, coverage dates, row counts, first trades and number of positive-volume days.

It demonstrates the following sequence:

1. Inspect contract-level coverage before selecting observations.
2. Read only that contract from Parquet.
3. Keep main summaries distinct from supplemental prices.
4. Reconcile observed counts to metadata.
5. Count positive-volume days while retaining all source rows.

This example reconciles the surviving records with the spreadsheet counts.

For an individual market, replace the selected ID with an explicit string and keep the same filtered reads. Use its coverage flags to select the available observation types. The Obama and Romney 2012 contracts have supplemental daily closing prices and missing trade files.

For the preserved source snapshot, this selects **27824 / EURO.SWEDEN.YES**, with **13 trade records and 34 daily summaries** spanning **2003-08-11 to 2003-09-13**. Both row counts reconcile with the spreadsheet; 27 daily rows report zero volume.
