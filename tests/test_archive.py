import sys
import unittest
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parents[1]/"scripts"))
from build_archive import parse_member, date_value, number

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

if __name__=="__main__":unittest.main()
