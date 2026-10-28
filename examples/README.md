# Worked example: from coverage to observations

After building the archive, run:

```bash
python examples/load_contract.py
```

The example deterministically selects the lowest numeric contract ID with more than ten surviving trades, main daily summaries, matching spreadsheet counts, and no repeated trade tuples or daily keys. It prints the selected market, coverage dates, row counts, first trades and number of positive-volume days.

It demonstrates a safe sequence:

1. Inspect contract-level coverage before selecting observations.
2. Read only that contract from Parquet.
3. Keep main summaries distinct from supplemental prices.
4. Reconcile observed counts to metadata.
5. Distinguish positive-volume days without deleting source rows.

This example verifies surviving-file consistency. It does not certify complete historical coverage or infer settlement outcomes.

For an individual market, replace the selected ID with an explicit string and keep the same filtered reads. Examine its coverage flags first; Obama/Romney 2012 are intentionally unsuitable as examples of complete trade-file coverage.
