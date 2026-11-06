#!/usr/bin/env python3
"""
Builds the folder you upload to your website: site/
  site/data/scholarships.json   the data the app downloads
  site/_headers                 tells Netlify to let phones re-check every 5 minutes
  site/index.html               a plain placeholder page (only created if you have none yet)

Weekly routine:
  1. edit tools/scholarships.csv
  2. python3 tools/csv_to_json.py        (checks the spreadsheet, writes the data file)
  3. python3 tools/build_site.py         (checks the data file again, builds site/)
  4. upload the site folder to Netlify (drag and drop, or connect it to GitHub)
Phones pick the new data up the next time the app is opened.
"""
import json, shutil, sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
SRC = HERE.parent / "app/src/main/assets/scholarships.json"
OUT = HERE.parent / "site"

try:
    data = json.loads(SRC.read_text(encoding="utf-8"))
except Exception as e:
    sys.exit(f"Cannot read {SRC}: {e}\nRun tools/csv_to_json.py first.")

items = data.get("scholarships")
if not isinstance(items, list) or not items:
    sys.exit("The data file has no scholarships. Not building the site.")
ids = [e.get("id") for e in items]
if len(set(ids)) != len(ids) or not all(ids):
    sys.exit("Duplicate or empty ids in the data file. Not building the site.")
if not data.get("updated"):
    sys.exit("The data file has no 'updated' date. Run tools/csv_to_json.py again.")

(OUT / "data").mkdir(parents=True, exist_ok=True)
shutil.copyfile(SRC, OUT / "data/scholarships.json")
(OUT / "_headers").write_text("/data/*\n  Cache-Control: public, max-age=300\n  Access-Control-Allow-Origin: *\n", encoding="utf-8")
index = OUT / "index.html"
if not index.exists():
    index.write_text('<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">'
                     '<title>Stipendo</title><body style="font-family:system-ui;max-width:32rem;margin:15vh auto;padding:0 1rem">'
                     '<h1>Stipendo</h1><p>Scholarships at Chinese universities. Free, with links to official pages only.</p></body></html>\n', encoding="utf-8")

checked = sum(1 for e in items if e.get("verification") == "OFFICIAL_CHECKED")
size_kb = (OUT / "data/scholarships.json").stat().st_size / 1024
print(f"Site ready in {OUT}")
print(f"  {len(items)} entries ({checked} checked on official page), data dated {data['updated']}, {size_kb:.1f} KB")
print("  Upload the whole 'site' folder. The app reads /data/scholarships.json.")
