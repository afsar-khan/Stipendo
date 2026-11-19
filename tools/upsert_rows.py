# Adds new rows and replaces existing rows (matched by id) in tools\scholarships.csv.
# The batch file MUST have a header line (the same columns as scholarships.csv).
# Run from the project root:   python tools\upsert_rows.py path\to\batch.csv
import csv, os, shutil, sys

here = os.path.dirname(os.path.abspath(__file__))
S = os.path.join(here, "scholarships.csv")
if len(sys.argv) < 2:
    sys.exit("Usage: python tools\\upsert_rows.py path\\to\\batch.csv")
B = sys.argv[1]

def read(p):
    with open(p, encoding="utf-8-sig", newline="") as f:
        r = csv.DictReader(f); return r.fieldnames, list(r)

cols, rows = read(S)
bcols, batch = read(B)
if bcols != cols:
    sys.exit("The batch file's columns do not match scholarships.csv.\n"
             f"Missing: {[c for c in cols if c not in (bcols or [])]}  Extra: {[c for c in (bcols or []) if c not in cols]}")

shutil.copy(S, os.path.join(here, "scholarships.backup.csv"))   # ignored by git (*.backup.csv)
pos = {r["id"]: i for i, r in enumerate(rows)}
added = replaced = 0
for b in batch:
    i = b["id"]
    if i in pos:
        old = rows[pos[i]]
        diff = [c for c in cols if old[c] != b[c]]
        rows[pos[i]] = b; replaced += 1
        print(f"replaced {i}: " + (", ".join(f"{c}: {old[c][:30]!r} -> {b[c][:30]!r}" for c in diff) or "no change"))
    else:
        rows.append(b); pos[i] = len(rows) - 1; added += 1
        print(f"added    {i}")

with open(S, "w", encoding="utf-8-sig", newline="") as f:
    w = csv.DictWriter(f, fieldnames=cols, lineterminator="\r\n")
    w.writeheader(); w.writerows(rows)
print(f"Done: {added} added, {replaced} replaced, {len(rows)} rows in total. Backup: tools\\scholarships.backup.csv")
