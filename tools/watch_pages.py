#!/usr/bin/env python3
"""
Page radar: tells you which official pages changed since the last time you ran it.

  python watch_pages.py                  # uses watch_list.csv next to this file
  python watch_pages.py my_list.csv
  python watch_pages.py --universities   # also watch the noticeUrl pages in csc_universities.csv
  python watch_pages.py --report         # also writes radar_report.md when pages changed (for the weekly automatic run)
  python watch_pages.py --state some/file.json   # keep the page memory somewhere else

Run it once a week (it takes a minute). The first run just records every page.
Later runs print NEW / CHANGED / UNREACHABLE for each page, plus the sentences that were added,
so you know exactly which rows of scholarships.csv to re-check. Nothing is edited for you:
a change on a page is a reason to look, not a verified fact.

watch_list.csv columns: id, name, url   (add a row for every official page you rely on)
"""
import csv, hashlib, json, re, sys, datetime
from pathlib import Path
from pagetext import page_text, sentences

HERE = Path(__file__).resolve().parent
args = [a for a in sys.argv[1:] if not a.startswith("--")]
LIST = Path(args[0]) if args else HERE / "watch_list.csv"
STATE = LIST.with_suffix(".state.json")
if "--state" in sys.argv:                       # --state path/to/file.json  (used by the automatic weekly run)
    STATE = Path(sys.argv[sys.argv.index("--state") + 1])
    args = [a for a in args if a != str(STATE)]
    LIST = Path(args[0]) if args else HERE / "watch_list.csv"
REPORT = HERE / "radar_report.md"               # only written with --report, and only when something changed
make_report = "--report" in sys.argv
report = []
CHANGES = LIST.with_suffix(".changes.json")   # read by draft_rows.py --changed

state = json.loads(STATE.read_text(encoding="utf-8")) if STATE.exists() else {}
today = datetime.date.today().isoformat()
counts = {"NEW": 0, "CHANGED": 0, "same": 0, "UNREACHABLE": 0}
changed_pages = []

def watched_rows():
    with open(LIST, newline="", encoding="utf-8-sig") as f:
        for row in csv.DictReader(f):
            yield row
    if "--universities" in sys.argv:              # also watch every noticeUrl in csc_universities.csv
        listed = HERE / "csc_universities.csv"
        if listed.exists():
            with open(listed, newline="", encoding="utf-8-sig") as f:
                for r in csv.DictReader(f):
                    if (r.get("noticeUrl") or "").strip():
                        slug = re.sub(r"[^a-z0-9]+", "-", r["name"].lower()).strip("-")
                        yield {"id": "uni-" + slug, "name": r["name"] + ": notice page", "url": r["noticeUrl"].strip()}

seen_urls = set()
if True:
    for row in watched_rows():
        pid, name, url = row["id"].strip(), row["name"].strip(), row["url"].strip()
        if url in seen_urls:                      # the same page listed twice: look once
            continue
        seen_urls.add(url)
        try:
            text = page_text(url)
        except Exception as e:
            counts["UNREACHABLE"] += 1
            print(f"UNREACHABLE  {name}\n             {url}\n             ({e})")
            continue
        digest = hashlib.sha256(text.encode("utf-8")).hexdigest()
        old = state.get(pid)
        if old is None:
            counts["NEW"] += 1
            print(f"NEW          {name} (recorded)")
        elif old["hash"] != digest:
            counts["CHANGED"] += 1
            changed_pages.append({"id": pid, "name": name, "url": url})
            print(f"CHANGED      {name}\n             {url}\n             last seen {old['seen']}")
            added = [s for s in sentences(text) if s not in set(sentences(old["text"]))][:6]
            for s in added: print("             + " + s[:220])
            report.append(f"### {name}\n{url}\n(last seen {old['seen']})\n" + "".join(f"- {s[:220]}\n" for s in added))
        else:
            counts["same"] += 1
        if old is None or old["hash"] != digest:
            state[pid] = {"hash": digest, "seen": today, "text": text}
        else:
            state[pid]["checked"] = today

STATE.parent.mkdir(parents=True, exist_ok=True)
STATE.write_text(json.dumps(state, ensure_ascii=False, indent=1), encoding="utf-8")
CHANGES.write_text(json.dumps(changed_pages, ensure_ascii=False, indent=1), encoding="utf-8")
if make_report:
    if report:
        REPORT.write_text("# Pages that changed\n\nOpen each page, see what changed, then update the spreadsheet.\n"
                          "The lines marked with - are sentences that are new on the page.\n\n" + "\n".join(report), encoding="utf-8")
    elif REPORT.exists():
        REPORT.unlink()
print("\nSummary: " + ", ".join(f"{k} {v}" for k, v in counts.items()))
