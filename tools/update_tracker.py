# Updates tools/csc_universities.csv: adds Northeastern University,
# marks Nanjing Agricultural as in the app, records Hainan University's page.
# Safe to run more than once. Run from the project root:  python tools\update_tracker.py
import csv, os

p = os.path.join(os.path.dirname(os.path.abspath(__file__)), "csc_universities.csv")
rows = list(csv.DictReader(open(p, encoding="utf-8-sig", newline="")))
cols = list(rows[0].keys())

def find(name):
    return next((r for r in rows if r["name"] == name), None)

# 1) Northeastern University (missing from the tracker)
if not find("Northeastern University"):
    new = {c: "" for c in cols}
    new.update({
        "name": "Northeastern University",
        "status": "in app, not yet checked",
        "appIds": "neu-cgs-b-2027",
        "agencyNumber": "10145",
        "agencySource": "read on the university's official page",
        "noticeUrl": "http://studyinneu.neu.edu.cn/html/article_content/202610/71C6675B7C3749DA85379684D8299E44.shtml",
    })
    if "nameZh" in new:
        new["nameZh"] = "东北大学"
    rows.append(new)
    rows.sort(key=lambda r: r["name"].lower())
    print("added: Northeastern University")
else:
    print("already there: Northeastern University")

# 2) Nanjing Agricultural University
r = find("Nanjing Agricultural University")
if r:
    r["status"] = "in app, not yet checked"
    r["appIds"] = "njau-cgs-b-2027"
    r["noticeUrl"] = "https://mp.weixin.qq.com/s/7fE-wC7KhuhYsRE76Q0opg"
    r["note"] = "Brochure gives no deadline or agency number; check coieen.njau.edu.cn"
    print("updated: Nanjing Agricultural University")

# 3) Hainan University (stays todo)
r = find("Hainan University")
if r:
    r["agencyNumber"] = "10589"
    r["agencySource"] = "read on the university's official page"
    r["noticeUrl"] = "https://en.hainanu.edu.cn/Admission/InternationalStudents/ScholarshipInformation/Chinese_Government_Scholarship_Programs.htm"
    r["note"] = "General page, no year or deadline; need the 2027 notice"
    print("updated: Hainan University")

with open(p, "w", encoding="utf-8-sig", newline="") as f:
    w = csv.DictWriter(f, fieldnames=cols, lineterminator="\r\n")
    w.writeheader()
    w.writerows(rows)
