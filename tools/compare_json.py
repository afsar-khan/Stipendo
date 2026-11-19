# Compares the app's scholarships.json with the last committed version.
# Run from the project root:  python tools\compare_json.py
import json, subprocess, os

root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
path = "app/src/main/assets/scholarships.json"
old = json.loads(subprocess.run(["git", "show", "HEAD:" + path], cwd=root, capture_output=True,
                                text=True, encoding="utf-8", check=True).stdout)["scholarships"]
new = json.load(open(os.path.join(root, path), encoding="utf-8"))["scholarships"]
o = {i["id"]: i for i in old}; n = {i["id"]: i for i in new}
print(f"last commit: {len(o)} entries | now: {len(n)} entries")
print("ADDED  :", sorted(set(n) - set(o)) or "none")
print("REMOVED:", sorted(set(o) - set(n)) or "none")
for i in sorted(set(o) & set(n)):
    diff = [k for k in n[i] if n[i].get(k) != o[i].get(k)]
    if diff:
        print("CHANGED:", i)
        for k in diff:
            print(f"   {k}: {str(o[i].get(k))[:70]!r} -> {str(n[i].get(k))[:70]!r}")
