#!/usr/bin/env python3
"""
Convert your spreadsheet (saved as CSV) into the app's data file.

  python3 csv_to_json.py                      # tools/scholarships.csv -> ../app/src/main/assets/scholarships.json
  python3 csv_to_json.py my.csv out.json

In Excel / Google Sheets: File > Save as / Download > CSV (UTF-8).

Cell rules
  degrees      BACHELOR;MASTER;PHD;LANGUAGE        (separate with ;)
  languages    ENGLISH;CHINESE                     (leave blank if not stated)
  lengths      SIX_MONTHS;ONE_YEAR  (language programs only, blank for degrees)
  funding      FULL, PARTIAL, VARIES (full or partial depending on the award) or SELF_FUNDED (you pay)
  tuitionRmbPerYear   number, for self-funded programs (blank if not stated)
  coversTuition / coversHousing   yes, no, or blank (= not stated)
  stipends     MASTER:3000;PHD:3500                (RMB per month, blank if not stated)
  deadline / cscDeadline / lastChecked   YYYY-MM-DD, or blank
  verification OFFICIAL_CHECKED or REPORTED
               OFFICIAL_CHECKED needs officialUrl and lastChecked, and means you read EVERY
               field on the university's own page. Anything else is REPORTED.
"""
import csv, json, os, re, sys, datetime
from pathlib import Path

HERE = Path(__file__).resolve().parent
src = Path(sys.argv[1]) if len(sys.argv) > 1 else HERE / "scholarships.csv"
dst = Path(sys.argv[2]) if len(sys.argv) > 2 else HERE.parent / "app/src/main/assets/scholarships.json"

DEGREES = {"BACHELOR", "MASTER", "PHD", "LANGUAGE"}
LANGS = {"ENGLISH", "CHINESE"}
LENGTHS = {"SIX_MONTHS", "ONE_YEAR"}
FUNDING = {"FULL", "PARTIAL", "VARIES", "SELF_FUNDED"}
VERIF = {"OFFICIAL_CHECKED", "REPORTED"}
REQUIRED = ["id", "university", "city", "name", "provider", "degrees", "funding", "sourceUrl", "verification"]

errors, ids, out = [], set(), []

def blank(v): return v is None or v.strip() == ""
def opt(v): return None if blank(v) else v.strip()
def parts(v): return [p.strip().upper() for p in (v or "").split(";") if p.strip()]
def date_ok(v):
    try: datetime.date.fromisoformat(v); return True
    except ValueError: return False

with open(src, newline="", encoding="utf-8-sig") as f:
    for n, row in enumerate(csv.DictReader(f), start=2):   # row 1 is the header
        def err(msg): errors.append(f"row {n} ({row.get('id') or '?'}): {msg}")
        for k in REQUIRED:
            if blank(row.get(k)): err(f"'{k}' is required")
        rid = (row.get("id") or "").strip()
        if rid in ids: err("duplicate id")
        ids.add(rid)

        degrees, langs = parts(row.get("degrees")), parts(row.get("languages"))
        lengths = parts(row.get("lengths"))
        for x in lengths:
            if x not in LENGTHS: err(f"unknown length '{x}'")
        for d in degrees:
            if d not in DEGREES: err(f"unknown degree '{d}'")
        for l in langs:
            if l not in LANGS: err(f"unknown language '{l}'")
        funding = (row.get("funding") or "").strip().upper()
        if funding and funding not in FUNDING: err(f"unknown funding '{funding}'")
        verification = (row.get("verification") or "").strip().upper()
        if verification and verification not in VERIF: err(f"unknown verification '{verification}'")

        def yn(key):
            v = (row.get(key) or "").strip().lower()
            if v == "": return None
            if v in ("yes", "true"): return True
            if v in ("no", "false"): return False
            err(f"'{key}' must be yes, no or blank"); return None

        tuition = None
        tv = (row.get("tuitionRmbPerYear") or "").strip().replace(",", "")
        if tv:
            if tv.isdigit(): tuition = int(tv)
            else: err("'tuitionRmbPerYear' must be a whole number")

        stipends = {}
        for item in parts(row.get("stipends")):
            m = re.fullmatch(r"([A-Z]+):(\d+)", item)
            if not m or m.group(1) not in DEGREES: err(f"bad stipend '{item}' (use MASTER:3000)")
            else: stipends[m.group(1)] = int(m.group(2))

        for key in ("deadline", "cscDeadline", "lastChecked"):
            v = opt(row.get(key))
            if v and not date_ok(v): err(f"'{key}' must be YYYY-MM-DD")
        for key in ("officialUrl", "sourceUrl"):
            v = opt(row.get(key))
            if v and not v.startswith(("https://", "http://")): err(f"'{key}' must start with https:// or http://")

        if verification == "OFFICIAL_CHECKED":
            if blank(row.get("officialUrl")): err("OFFICIAL_CHECKED needs officialUrl")
            if blank(row.get("lastChecked")): err("OFFICIAL_CHECKED needs lastChecked")

        out.append({
            "id": rid,
            "university": opt(row.get("university")),
            "universityZh": opt(row.get("universityZh")),
            "city": opt(row.get("city")),
            "name": opt(row.get("name")),
            "provider": opt(row.get("provider")),
            "degrees": degrees,
            "languages": langs,
            "lengths": lengths,
            "funding": funding,
            "coversTuition": yn("coversTuition"),
            "coversHousing": yn("coversHousing"),
            "tuitionRmbPerYear": tuition,
            "stipendRmbByDegree": stipends,
            "languageRequirement": opt(row.get("languageRequirement")),
            "deadline": opt(row.get("deadline")),
            "cscDeadline": opt(row.get("cscDeadline")),
            "cscAgencyNumber": opt(row.get("cscAgencyNumber")),
            "officialUrl": opt(row.get("officialUrl")),
            "sourceUrl": opt(row.get("sourceUrl")),
            "verification": verification,
            "lastChecked": opt(row.get("lastChecked")),
        })

if errors:
    print("Not written. Fix these first:\n  " + "\n  ".join(errors))
    sys.exit(1)

# Dates the app uses for its "New" and "Updated" marks. You never type them:
#   row not in the previous file   -> addedOn = today
#   row text changed since last run -> updatedOn = today
#   otherwise                       -> kept as they were
# The very first run only records the rows (no dates), so nothing is wrongly marked New.
today = os.environ.get("STIPENDO_TODAY") or datetime.date.today().isoformat()
old = {}
if dst.exists():
    try: old = {e["id"]: e for e in json.loads(dst.read_text(encoding="utf-8")).get("scholarships", [])}
    except Exception: old = {}
DATES = ("addedOn", "updatedOn")
strip = lambda d: {k: v for k, v in d.items() if k not in DATES}
n_new = n_changed = 0
for e in out:
    prev = old.get(e["id"])
    if prev is None:
        e["addedOn"], e["updatedOn"] = (today, None) if old else (None, None)
        n_new += 1 if old else 0
    elif strip(prev) != strip(e):
        e["addedOn"], e["updatedOn"] = prev.get("addedOn"), today
        n_changed += 1
    else:
        e["addedOn"], e["updatedOn"] = prev.get("addedOn"), prev.get("updatedOn")

dst.parent.mkdir(parents=True, exist_ok=True)
data = {"version": 1, "updated": datetime.date.today().isoformat(), "scholarships": out}
dst.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
checked = sum(1 for s in out if s["verification"] == "OFFICIAL_CHECKED")
print(f"OK: {len(out)} entries ({checked} checked on official page, {len(out) - checked} reported) -> {dst}")
print(f"    {n_new} new, {n_changed} changed since the last run")
