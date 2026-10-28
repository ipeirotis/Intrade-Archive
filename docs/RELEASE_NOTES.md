# Intrade Historical Archive v1.0.1

Data-focused packaging and citation update for the historical snapshot added on 2 April 2013. Research observations match v1.0.0.

The repository now contains the archived data, research tools, documentation and explorer. The retired crawler, compiled classes, bundled libraries and Eclipse/Google App Engine configuration have been removed from the current tree.

Six original source files are retained with their original byte hashes: the four original files in `data/`, the historical API manual, and the Java datastore relocated to `data/legacy-datastore.bin`. The source snapshot download packages these data and documentation files. The v1.0.0 release and original Git commit preserve the earlier repository snapshot.

The README, citation metadata and explorer reference *Modeling Volatility in Prediction Markets* (ACM EC 2009) and *Modeling Dependency in Prediction Markets* (NYU working paper, 2010). `references.bib` supplies both BibTeX entries. Documentation uses direct descriptions of the data, coverage and quality checks.

Main summaries and supplemental closing-price histories remain distinct. Identical trade tuples and zero values are retained. Unquoted commas in source market names are repaired by the documented fixed-schema rule and flagged. Settlement fields are marked unavailable. The release preserves the legacy datastore and documents its incomplete serialization stream.

Use COVERAGE.md and quality_report.json to select a research sample. The repository includes metadata and instructions for a Zenodo preservation deposit.
