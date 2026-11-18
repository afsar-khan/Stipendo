# Adds the rows from tools\new_rows.csv to tools\scholarships.csv (skipping ids already there)
# and updates tools\csc_universities.csv. Safe to run twice.
# Run from the project root:  python tools\apply_v29.py
import csv, os

here = os.path.dirname(os.path.abspath(__file__))
S = os.path.join(here, "scholarships.csv")
N = os.path.join(here, "new_rows.csv")
T = os.path.join(here, "csc_universities.csv")

# Hainan University has no 2027 notice yet, so it is left out for now.
# To include it later, delete the next line.
SKIP_IDS = {"hainan-cgs-b-2027"}

def read(p):
    with open(p, encoding="utf-8-sig", newline="") as f:
        return list(csv.DictReader(f))

def write(p, cols, rows):
    with open(p, "w", encoding="utf-8-sig", newline="") as f:
        w = csv.DictWriter(f, fieldnames=cols, lineterminator="\r\n")
        w.writeheader(); w.writerows(rows)

# ---- scholarships.csv
rows = read(S); cols = list(rows[0].keys())
have = {r["id"] for r in rows}
added = 0
for r in read(N):
    if r["id"] in SKIP_IDS:
        print("skipped (by choice):", r["id"]); continue
    if r["id"] in have:
        print("already there:", r["id"]); continue
    rows.append({c: r.get(c, "") for c in cols}); have.add(r["id"]); added += 1
    print("added:", r["id"])
write(S, cols, rows)
print(f"scholarships.csv now has {len(rows)} rows ({added} added)")

# ---- csc_universities.csv
t = read(T); tcols = list(t[0].keys())
def find(n): return next((x for x in t if x["name"] == n), None)

if not find("Northeastern University"):
    new = {c: "" for c in tcols}
    new.update({"name": "Northeastern University", "status": "in app, not yet checked",
                "appIds": "neu-cgs-b-2027;neu-icl-semester", "agencyNumber": "10145",
                "agencySource": "read on the university's official page",
                "noticeUrl": "http://studyinneu.neu.edu.cn/html/article_content/202610/71C6675B7C3749DA85379684D8299E44.shtml"})
    t.append(new); t.sort(key=lambda x: x["name"].lower()); print("tracker: added Northeastern University")
r = find("Nanjing Agricultural University")
if r:
    r.update({"status": "in app, not yet checked", "appIds": "nau-cgs-b-2027",
              "noticeUrl": "https://mp.weixin.qq.com/s/7fE-wC7KhuhYsRE76Q0opg",
              "note": "Brochure gives no deadline or agency number; check coieen.njau.edu.cn"})
    print("tracker: updated Nanjing Agricultural University")
r = find("Hainan University")
if r:
    r.update({"agencyNumber": "10589", "agencySource": "read on the university's official page",
              "noticeUrl": "https://en.hainanu.edu.cn/Admission/InternationalStudents/ScholarshipInformation/Chinese_Government_Scholarship_Programs.htm",
              "note": "General page, no year or deadline; need the 2027 notice"})
    print("tracker: updated Hainan University")
write(T, tcols, t)
