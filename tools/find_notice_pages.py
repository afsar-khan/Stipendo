#!/usr/bin/env python3
"""
Finds the official 2027 scholarship notice page for each university in csc_universities.csv.

  python find_notice_pages.py --limit 3          # try 3 universities first (priority list order)
  python find_notice_pages.py --limit 20         # a bigger run (the free Gemini quota is limited: run again tomorrow if it stops)
  python find_notice_pages.py --only "Renmin University of China;Jilin University"
  python find_notice_pages.py --accept           # copy good suggestions (score 6 or more) into the noticeUrl column

Needs GEMINI_API_KEY in your .env file (see draft_rows.py). Uses Gemini WITH Google Search, which a free plan may not
include. If you get error 429 on the first request, run check_gemini.py.

How it stays safe
  - The AI only SUGGESTS addresses. The script then downloads each one itself and checks it:
      * the site must be a university site (domain ends in .edu.cn or .ac.cn)
      * the page must contain admission/scholarship words, the year 2027, and enough text to be a real notice
    Pages that fail are thrown away, however confident the AI sounded.
  - Suggestions go into NEW columns (suggestedUrl, suggestedScore, suggestedWhy). Your noticeUrl column is never
    overwritten, except by --accept, and only for rows where it was empty.
  - Score 6 or more = good, 4 or 5 = maybe (open it and look), below 4 = not saved.
Next steps after --accept:  python watch_pages.py --universities   (the radar now watches these pages)
                            python draft_rows.py --universities    (drafts rows from them, with evidence)
"""
import argparse, csv, datetime, json, os, re, shutil, sys, time, urllib.error, urllib.parse, urllib.request
from pathlib import Path
from pagetext import HEADERS, html_to_text

HERE = Path(__file__).resolve().parent
GEMINI_MODEL = os.environ.get("GEMINI_MODEL", "gemini-3.5-flash-lite")
OFFICIAL_SUFFIXES = (".edu.cn", ".ac.cn")
REDIRECT_HOSTS = ("vertexaisearch.cloud.google.com",)      # Gemini sometimes returns Google redirect links
RELEVANT = ["scholarship", "admission", "apply", "application", "deadline", "tuition", "stipend", "csc", "agency",
            "international students", "奖学金", "獎學金", "招生", "申请", "申請", "截止", "留学生"]
GENERIC = {"university", "of", "and", "the", "science", "technology", "institute", "china", "chinese", "normal", "for", "college"}
NEW_COLUMNS = ["suggestedUrl", "suggestedScore", "suggestedWhy", "suggestedOn"]
PRIORITY = ["Renmin University of China", "Central South University", "University of Electronic Science and Technology of China",
    "South China University of Technology", "Shandong University", "Jilin University", "Lanzhou University", "Chongqing University",
    "Northeastern University", "Zhengzhou University", "Hunan University", "Nanjing University of Aeronautics and Astronautics",
    "Nanjing University of Science and Technology", "Shanghai University", "Beijing University of Posts and Telecommunications",
    "Beijing University of Chemical Technology", "Beijing Foreign Studies University", "China Agricultural University",
    "Ocean University of China", "Huazhong Agricultural University", "Wuhan University of Technology", "Dalian Maritime University",
    "Soochow University", "Jinan University", "South China Normal University", "Central China Normal University",
    "Northeast Forestry University", "Harbin Engineering University", "Beijing Jiaotong University", "Beijing University of Technology",
    "Nanjing Normal University", "Nanjing Agricultural University", "China University of Geosciences (Wuhan)", "Tianjin Normal University",
    "Shaanxi Normal University", "Sun Yat-sen University", "Xidian University", "Northwest A&F University", "Yunnan University", "Xinjiang University"]

def load_env():
    for folder in (HERE, HERE.parent):
        f = folder / ".env"
        if f.exists():
            for line in f.read_text(encoding="utf-8-sig").splitlines():
                line = line.strip()
                if line and not line.startswith("#") and "=" in line:
                    k, v = line.split("=", 1); os.environ.setdefault(k.strip(), v.strip().strip('"').strip("'"))

def key_of(name): return re.sub(r"[^a-z0-9]+", " ", name.lower().replace("&", "and")).strip()

def host_of(url): return (urllib.parse.urlparse(url).hostname or "").lower()
def host_matches(host, domains): return any(host == d or host.endswith("." + d) or host.endswith(d) and d.startswith(".") for d in domains)

def urls_from_text(text):
    out = []
    for u in re.findall(r"https?://[^\s)>\]\"'|]+", text):
        u = u.rstrip(".,;:")
        if u not in out: out.append(u)
    return out

def fetch_info(url):
    req = urllib.request.Request(url, headers=HEADERS)
    with urllib.request.urlopen(req, timeout=30) as r:
        final = r.geturl(); raw = r.read(400000); charset = r.headers.get_content_charset() or "utf-8"
    html = raw.decode(charset, errors="replace")
    m = re.search(r"<title[^>]*>(.*?)</title>", html, re.I | re.S)
    title = re.sub(r"\s+", " ", m.group(1)).strip() if m else ""
    return final, title, html_to_text(html, 60000)

def score_page(name, final, title, text, year):
    low, reasons, s = text.lower(), [], 0
    n = sum(w in low for w in RELEVANT)
    if n >= 3: s += 2; reasons.append(f"{n} admission words")
    elif n >= 1: s += 1; reasons.append(f"only {n} admission word(s)")
    else: reasons.append("no admission words")
    if str(year) in text: s += 2; reasons.append(f"mentions {year}")
    else: reasons.append(f"does NOT mention {year}")
    if re.search(r"chinese government scholarship|csc|type b|agency (no|number)|中国政府奖学金|国家留学基金", low): s += 2; reasons.append("names the CSC scholarship")
    host = host_of(final)
    toks = [t for t in key_of(name).split() if t not in GENERIC and len(t) > 2]
    if toks and any(t in low or t in host for t in toks): s += 1; reasons.append("university name found")
    if len(text) < 400: s -= 2; reasons.append("very little text (may load with scripts)")
    if urllib.parse.urlparse(final).path in ("", "/"): s -= 2; reasons.append("looks like a homepage")
    return s, reasons

def call_gemini(prompt, key):
    url = f"https://generativelanguage.googleapis.com/v1beta/models/{GEMINI_MODEL}:generateContent"
    body = json.dumps({"contents": [{"role": "user", "parts": [{"text": prompt}]}],
                       "tools": [{"google_search": {}}], "generationConfig": {"temperature": 0}}).encode("utf-8")
    for attempt in range(4):
        req = urllib.request.Request(url, data=body, method="POST", headers={"x-goog-api-key": key, "content-type": "application/json"})
        try:
            with urllib.request.urlopen(req, timeout=180) as r: reply = json.load(r)
            cand = reply["candidates"][0]
            text = "".join(p.get("text", "") for p in cand.get("content", {}).get("parts", []))
            chunks = [c.get("web", {}).get("uri") for c in cand.get("groundingMetadata", {}).get("groundingChunks", [])]
            return text, [c for c in chunks if c]
        except urllib.error.HTTPError as e:
            msg = e.read().decode("utf-8", "replace")[:300]
            if e.code in (429, 500, 503) and attempt < 3: time.sleep(2 ** (attempt + 2)); continue
            if e.code == 429: raise QuotaStop(msg)
            if e.code == 400 and "search" in msg.lower():
                sys.exit(f"The model {GEMINI_MODEL} did not accept Google Search. Set GEMINI_MODEL in .env to a model that supports it.\n{msg}")
            raise

class QuotaStop(Exception): pass

def find_for(name, ask, year, extra_domains):
    prompt = (f"Find the university's own web page that announces applications for the {year} Chinese Government Scholarship "
              f"(CSC Type B, University Program) or international graduate admission, for international students.\nUniversity: {name}\n"
              "Rules:\n- Use Google Search. Only return addresses you actually found in the search results. Never invent or guess an address.\n"
              "- The address must be on the university's own website (the domain ends in .edu.cn). No agencies, news sites or scholarship blogs.\n"
              "- Prefer the English page for international students; a Chinese page is acceptable.\n"
              "- Give at most 3 addresses, best first, one per line, in this form:\nURL | one short reason\n- If you cannot find one, write: NONE")
    text, chunks = ask(prompt)
    candidates = (urls_from_text(text) + chunks)[:6]
    best, notes = None, []
    for u in candidates:
        h = host_of(u)
        if not (host_matches(h, OFFICIAL_SUFFIXES) or host_matches(h, REDIRECT_HOSTS) or host_matches(h, extra_domains)):
            notes.append(f"skipped {h}: not a university site"); continue
        try: final, title, page = fetch_info(u)
        except Exception as e: notes.append(f"{u}: could not open ({e})"); continue
        fh = host_of(final)
        if not (host_matches(fh, OFFICIAL_SUFFIXES) or host_matches(fh, extra_domains)):
            notes.append(f"{u}: ends on {fh}, not a university site"); continue
        s, reasons = score_page(name, final, title, page, year)
        notes.append(f"{final} score {s}")
        if best is None or s > best[0]: best = (s, final, title, reasons)
    return best, notes

def main():
    ap = argparse.ArgumentParser(description="Find official scholarship notice pages.")
    ap.add_argument("--limit", type=int, default=10); ap.add_argument("--only", default="")
    ap.add_argument("--accept", nargs="?", const=6, type=int, help="copy suggestions with this score or more (default 6) into noticeUrl")
    ap.add_argument("--csv", default=str(HERE / "csc_universities.csv"))
    ap.add_argument("--extra-domain", action="append", default=[], help="testing only")
    ap.add_argument("--response-file", help="testing only: use this text as the AI's answer")
    a = ap.parse_args()
    path = Path(a.csv)
    rows = list(csv.DictReader(open(path, encoding="utf-8-sig")))
    fields = list(rows[0].keys()) + [c for c in NEW_COLUMNS if c not in rows[0]]
    for r in rows:
        for c in NEW_COLUMNS: r.setdefault(c, "")

    if a.accept is not None:
        shutil.copyfile(path, path.with_suffix(".backup.csv")); n = 0
        for r in rows:
            if not r["noticeUrl"].strip() and r["suggestedUrl"].strip() and (r["suggestedScore"] or "0").lstrip("-").isdigit() and int(r["suggestedScore"]) >= a.accept:
                r["noticeUrl"] = r["suggestedUrl"]; n += 1; print(f"accepted  {r['name']}  ({r['suggestedScore']})  {r['noticeUrl']}")
        with open(path, "w", newline="", encoding="utf-8-sig") as f:
            w = csv.DictWriter(f, fieldnames=fields); w.writeheader(); w.writerows(rows)
        sys.exit(f"{n} notice address(es) accepted. Backup: {path.with_suffix('.backup.csv').name}")

    load_env(); key = os.environ.get("GEMINI_API_KEY", "")
    if a.response_file: ask = lambda p: (Path(a.response_file).read_text(encoding="utf-8"), [])
    elif key: ask = lambda p: call_gemini(p, key)
    else: sys.exit("No GEMINI_API_KEY found. Put it in a .env file (see draft_rows.py).")

    today = datetime.date.today(); year = today.year + (1 if today.month >= 7 else 0)
    todo = [r for r in rows if not r["noticeUrl"].strip()]
    if a.only:
        wanted = {key_of(x) for x in a.only.split(";") if x.strip()}; todo = [r for r in todo if key_of(r["name"]) in wanted]
    else:
        order = {key_of(n): i for i, n in enumerate(PRIORITY)}
        todo.sort(key=lambda r: (order.get(key_of(r["name"]), 999), r["name"]))
    todo = todo[:a.limit]
    if not todo: sys.exit("Nothing to do: every university already has a noticeUrl, or the names did not match.")

    shutil.copyfile(path, path.with_suffix(".backup.csv"))
    report = [f"# Notice page suggestions, {today.isoformat()}", "", "Open each address and look before you accept it.", ""]
    found = 0
    try:
        for r in todo:
            try: best, notes = find_for(r["name"], ask, year, a.extra_domain)
            except QuotaStop as e:
                print("Google refused the request (error 429, quota).")
                print("If this happens on the FIRST request of the day, your free plan has no allowance for Google Search,")
                print("the tool this script needs. Run  python check_gemini.py  to find out, and see its advice.")
                print("If it only happened after many requests, the daily limit is used up: run again tomorrow.")
                print(str(e)[:160]); break
            except Exception as e:
                print(f"ERROR  {r['name']}: {e}"); report += [f"## {r['name']}", f"Error: {e}", ""]; continue
            if best and best[0] >= 4:
                s, url, title, reasons = best; found += 1
                r.update(suggestedUrl=url, suggestedScore=str(s), suggestedWhy="; ".join(reasons) + f" | {title[:80]}", suggestedOn=today.isoformat())
                label = "GOOD " if s >= 6 else "MAYBE"
                print(f"{label}  {r['name']}  (score {s})  {url}")
                report += [f"## {r['name']} ({label.strip().lower()}, score {s})", url, f"Title: {title}", "Why: " + "; ".join(reasons), ""]
            else:
                print(f"none   {r['name']}")
                report += [f"## {r['name']} (nothing good found)"] + [f"- {n}" for n in notes] + [""]
            time.sleep(4)
    finally:
        with open(path, "w", newline="", encoding="utf-8-sig") as f:
            w = csv.DictWriter(f, fieldnames=fields); w.writeheader(); w.writerows(rows)
        stamp = datetime.datetime.now().strftime("%Y%m%d_%H%M")
        (HERE / f"notice_suggestions_{stamp}.md").write_text("\n".join(report), encoding="utf-8")
    print(f"\n{found} of {len(todo)} suggested. Report: tools/notice_suggestions_{stamp}.md")
    print("Look at the GOOD/MAYBE addresses, then run:  python find_notice_pages.py --accept")

if __name__ == "__main__":
    main()
