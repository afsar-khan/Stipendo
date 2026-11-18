# Removes repeated rows (same id) from tools/scholarships.csv, keeping the FIRST one.
# Run from the project root:  python tools\fix_duplicates.py
import csv, os

p = os.path.join(os.path.dirname(os.path.abspath(__file__)), "scholarships.csv")
rows = list(csv.DictReader(open(p, encoding="utf-8-sig", newline="")))
cols = list(rows[0].keys())
seen, keep = {}, []
for n, r in enumerate(rows, start=2):          # line 1 is the header
    i = r["id"]
    if i in seen:
        same = (r == rows[seen[i] - 2])
        print(f"line {n}: duplicate of line {seen[i]} -> {i} ({'identical' if same else 'DIFFERENT content, check!'}) removed")
        continue
    seen[i] = n
    keep.append(r)
if len(keep) == len(rows):
    print("No duplicates found. Nothing changed.")
else:
    with open(p, "w", encoding="utf-8-sig", newline="") as f:
        w = csv.DictWriter(f, fieldnames=cols, lineterminator="\r\n")
        w.writeheader(); w.writerows(keep)
    print(f"Done: {len(rows)} -> {len(keep)} rows")
