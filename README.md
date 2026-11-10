# Stipendo v23: find the notice page for every university

## Install
Copy over your project (replace): tools\find_notice_pages.py, tools\watch_pages.py, tools\pagetext.py, tools\.gitignore and
.github\workflows\page-radar.yml. (pagetext.py is needed by the finder, the radar and the drafting tool: it is the
version from the radar tune-up, with the browser header.) Commit and push.
You need a NEW Gemini key in .env:  GEMINI_API_KEY=...   (never commit .env)

## Use (PowerShell)
    cd F:\Stipendo\tools
    python find_notice_pages.py --limit 3        # try 3 universities first
    # open tools\notice_suggestions_<time>.md and look at each address in your browser
    python find_notice_pages.py --accept         # copies the GOOD ones (score 6+) into the noticeUrl column
    python watch_pages.py --universities         # the radar now also watches those pages
    python find_notice_pages.py --limit 20       # more, on another day (the free quota is limited)

When the radar reports a changed page:
    python draft_rows.py --changed               # drafts rows with evidence (Gemini)
    read tools\drafts_<time>.evidence.md, check the official page, copy good rows into scholarships.csv,
    python csv_to_json.py, python build_site.py, commit, push.

## How the finder stays safe
- The AI only suggests. The script opens every address itself and drops anything that is not a university site
  (.edu.cn or .ac.cn), cannot be opened, is a homepage, has little text, lacks admission words or does not mention 2027.
- It never overwrites noticeUrl by itself. Suggestions go to new columns (suggestedUrl, suggestedScore, suggestedWhy).
  --accept copies score 6+ into noticeUrl for rows that were empty. A backup (csc_universities.backup.csv) is made first.
- Score 6+ good, 4-5 maybe (open it and look), below 4 not saved. Your radar list and the weekly GitHub run now also watch
  every noticeUrl (the workflow passes --universities; commit csc_universities.csv after accepting).

## What I could not test
I tested everything offline (a fake AI answer, a local test website). I could not call Gemini with Google Search from here.
If the model you set does not support Google Search the script says so; set GEMINI_MODEL in .env to one that does.
Expect to find roughly half the pages: the rest are Chinese-only, loaded by scripts, or PDFs. Run it in December or January,
when most 2027 notices are out.
