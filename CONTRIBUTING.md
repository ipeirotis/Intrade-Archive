# Contributing

Please include a contract ID, source archive/member and source record number in data reports. State the expected behavior and supporting evidence.

Keep all original files byte-for-byte unchanged. Add corrections or recovered material as new sources with a provenance note, acquisition date and checksum. Do not rewrite history to remove apparent duplicates or generated legacy files.

For code changes, install `requirements.txt`, run `python -m unittest discover -s tests -v`, run `python scripts/build_archive.py`, and inspect the resulting quality report. New parser formats require fixtures and explicit schema mappings. Changes to source selection, duplicate handling, time zones or interpretation require a documented method change and release note.

Keep the static explorer usable by keyboard and on narrow screens. Avoid external trackers and dependencies for basic browsing. Never insert source text as HTML.

Release versions identify derived datasets and documentation; the original source snapshot stays fixed at commit `620380b2ade7504e270239ecf98b68995c384aa8`.
