import sys
import unittest
import tempfile
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parents[1]/"scripts"))
from build_archive import parse_member, date_value, number
from release_links import rewrite_markdown_links

class ParsingTests(unittest.TestCase):
    def test_trade_timestamp_and_quoted_name(self):
        raw=b'market id,market symbol,market name,trade quantity,trade price,date/time [utc]\r\n12,TEST,"A, B",2,45.5,Nov/03/2012 01:30:00\r\n'
        rows,encoding=parse_member(raw,"trades","12",7)
        self.assertEqual(rows[0]["timestamp_utc"],"2012-11-03T01:30:00Z")
        self.assertEqual(rows[0]["source_row"],2)
        self.assertEqual(rows[0]["quantity"],2)
        self.assertEqual(rows[0]["price"],45.5)
    def test_supplement_order(self):
        rows,_=parse_member(b'Date,Open,Low,High,Close,Volume\n"Dec 6, 2010",50,49,52,51,0\n',"closingPrice","12",2)
        self.assertEqual([rows[0][k] for k in ["date","open","high","low","close","volume"]],["2010-12-06",50,52,49,51,0])
        self.assertIsNone(rows[0]["open_interest"])
    def test_empty_source_file(self):
        rows,_=parse_member(b"","summary","12",1)
        self.assertEqual(rows,[])
    def test_zero_is_not_missing(self):
        self.assertEqual(number("0"),0)
        self.assertIsNone(number(""))
        with self.assertRaises(ValueError):number("nan")
        with self.assertRaises(ValueError):number("1.2",True)
    def test_wrong_id_fails(self):
        raw=b'market id,market symbol,market name,trade quantity,trade price,date/time [utc]\n13,T,T,1,50,Jan/01/2010 00:00:00\n'
        with self.assertRaises(ValueError):parse_member(raw,"trades","12",1)
    def test_unquoted_name_comma_is_flagged(self):
        raw=b'market id,market symbol,market name,trade quantity,trade price,date/time [utc]\n12,T,A, B,1,50,Jan/01/2010 00:00:00\n'
        rows,_=parse_member(raw,"trades","12",1)
        self.assertTrue(rows[0]["source_name_comma_repair"])
        self.assertEqual(rows[0]["quantity"],1)
    def test_invalid_date_fails(self):
        with self.assertRaises(ValueError):date_value("Feb/30/2012 00:00:00","trades")
    def test_duplicate_records_retained(self):
        header=b'market id,market symbol,market name,trade quantity,trade price,date/time [utc]\n'
        line=b'12,T,T,1,50,Jan/01/2010 00:00:00\n'
        rows,_=parse_member(header+line+line,"trades","12",1)
        self.assertEqual(len(rows),2)
        self.assertEqual([r["source_row"] for r in rows],[2,3])

class ReleaseLinksTests(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name)
        for directory in ["docs", "examples", "data"]:
            (self.root/directory).mkdir()
        for file in ["README.md", "docs/PRESERVATION.md", "docs/ZENODO_DEPOSIT.md",
                     "references.bib", "examples/README.md"]:
            (self.root/file).write_text("", encoding="utf-8")
        self.bundle = {"README.md": "README.md", "docs/PRESERVATION.md": "PRESERVATION.md",
                       "docs/ZENODO_DEPOSIT.md": "ZENODO_DEPOSIT.md", "references.bib": "references.bib"}
        self.revision = "a" * 40

    def rewrite(self, text, source="README.md"):
        return rewrite_markdown_links(text, source, self.bundle, self.root, self.revision)

    def test_bundled_links_resolve_from_original_document_directory(self):
        actual = self.rewrite('[cite](../README.md#cite) [deposit](ZENODO_DEPOSIT.md) '
                              '[bib](../references.bib)', "docs/PRESERVATION.md")
        self.assertEqual(actual, '[cite](README.md#cite) [deposit](ZENODO_DEPOSIT.md) '
                                 '[bib](references.bib)')

    def test_repository_links_use_exact_commit_and_preserve_external_links(self):
        unchanged = '[external](https://example.org/path?q=1#part) [cite](#cite) [email](mailto:a@example.org)'
        actual = self.rewrite('[data](data/) [example](examples/README.md#usage) ' + unchanged)
        prefix = "https://github.com/ipeirotis/Intrade-Archive"
        self.assertEqual(actual, f'[data]({prefix}/tree/{self.revision}/data) '
                                 f'[example]({prefix}/blob/{self.revision}/examples/README.md#usage) ' + unchanged)

    def test_titles_angle_brackets_and_query_are_preserved(self):
        actual = self.rewrite('[cite](<../README.md?raw=1#cite> "Citation")', "docs/PRESERVATION.md")
        self.assertEqual(actual, '[cite](<README.md?raw=1#cite> "Citation")')

    def test_missing_and_outside_repository_targets_fail_packaging(self):
        with self.assertRaisesRegex(ValueError, "Missing link target"):
            self.rewrite('[missing](docs/missing.md)')
        with self.assertRaisesRegex(ValueError, "Link escapes repository"):
            self.rewrite('[outside](../outside.md)')


if __name__=="__main__":unittest.main()
