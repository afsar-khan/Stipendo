#!/usr/bin/env python3
"""
Checks what your free Gemini key can do, in about 20 seconds. Prints no secrets.

  python check_gemini.py                         # tests your model and a few others
  python check_gemini.py --models gemini-2.5-flash-lite,gemini-3.5-flash-lite

For each model it sends two tiny requests:
  1. plain  (no tools)         -> this is what draft_rows.py needs
  2. with Google Search        -> this is what find_notice_pages.py needs
Then it tells you what to do. Each request counts toward your daily quota, so do not run it in a loop.
"""
import argparse, json, os, sys, urllib.error, urllib.request
from pathlib import Path

HERE = Path(__file__).resolve().parent
BASE = os.environ.get("GEMINI_BASE", "https://generativelanguage.googleapis.com")

def load_env():
    for folder in (HERE, HERE.parent):
        f = folder / ".env"
        if f.exists():
            for line in f.read_text(encoding="utf-8-sig").splitlines():
                line = line.strip()
                if line and not line.startswith("#") and "=" in line:
                    k, v = line.split("=", 1); os.environ.setdefault(k.strip(), v.strip().strip('"').strip("'"))

def ask(model, key, prompt, search):
    body = {"contents": [{"role": "user", "parts": [{"text": prompt}]}], "generationConfig": {"temperature": 0}}
    if search: body["tools"] = [{"google_search": {}}]
    req = urllib.request.Request(f"{BASE}/v1beta/models/{model}:generateContent", data=json.dumps(body).encode("utf-8"), method="POST",
                                 headers={"x-goog-api-key": key, "content-type": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=60) as r:
            reply = json.load(r)
        text = "".join(p.get("text", "") for p in reply["candidates"][0].get("content", {}).get("parts", []))
        return 200, text.strip()[:60]
    except urllib.error.HTTPError as e:
        try: msg = json.loads(e.read().decode("utf-8", "replace"))["error"]["message"]
        except Exception: msg = ""
        return e.code, " ".join(msg.split())[:150]
    except Exception as e:
        return 0, str(e)[:150]

def main():
    ap = argparse.ArgumentParser(); ap.add_argument("--models", default="")
    a = ap.parse_args(); load_env(); key = os.environ.get("GEMINI_API_KEY", "")
    if not key: sys.exit("No GEMINI_API_KEY found in .env")
    default = os.environ.get("GEMINI_MODEL", "gemini-3.5-flash-lite")
    models = [m.strip() for m in a.models.split(",") if m.strip()] or [default, "gemini-3.1-flash-lite", "gemini-2.5-flash-lite", "gemini-2.5-flash"]
    models = list(dict.fromkeys(models)); good_plain, good_search = [], []
    for m in models:
        c1, t1 = ask(m, key, "Reply with the single word OK.", False)
        c2, t2 = ask(m, key, "Use Google Search: what is the capital of France? Answer in one word.", True)
        print(f"{m}\n   plain  : {'works' if c1 == 200 else f'FAILED {c1}'}  {t1 if c1 != 200 else ''}")
        print(f"   search : {'works' if c2 == 200 else f'FAILED {c2}'}  {t2 if c2 != 200 else ''}")
        if c1 == 200: good_plain.append(m)
        if c2 == 200: good_search.append(m)
    print()
    if good_search:
        print(f"Google Search works with: {', '.join(good_search)}\n-> put GEMINI_MODEL={good_search[0]} in .env and find_notice_pages.py will work.")
    elif good_plain:
        print(f"Plain requests work with: {', '.join(good_plain)}, but Google Search does not on your free plan.\n"
              f"-> Use Gemini only for drafting (draft_rows.py): put GEMINI_MODEL={good_plain[0]} in .env.\n"
              "-> Do not use find_notice_pages.py. Find the notice pages another way (ask Claude to search, in batches of 10).")
    else:
        print("No model answered. Check the key in .env, your region, and your quota in Google AI Studio (aistudio.google.com).")

if __name__ == "__main__":
    main()
