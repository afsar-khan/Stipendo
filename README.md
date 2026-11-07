# Stipendo v16: clean-up after the review

## What is in this zip
- site/data/scholarships.json and app/src/main/assets/scholarships.json   the same clean file, 17 entries
  (made from your scholarships.csv, so it is the single source of truth again)
- tools/drafts_from_scraper.csv   the 13 distinct entries your scraper added (3 exact duplicates removed),
  as draft rows in the spreadsheet format. Nothing is lost; they are just not live.
- tools/draft_rows.py (v2)   drafts rows with Gemini (your free key) or Claude, with quote checks

## Why the 16 scraped entries and the 2 test entries came out of the live file
- 12 of 16 had no usable fact (no deadline, fee, language rule, stipend or agency number)
- 4 had deadlines that already passed; 3 were duplicates
- Most pointed to a university homepage, not a notice page
- The file the app reads is built from scholarships.csv. The next time you run csv_to_json.py and build_site.py,
  anything that exists only in the live file disappears. Keep ONE source: the CSV.
- test-github-scholarship and test-number-two were still live.

## Do these now
1. Replace the two scholarships.json files with the ones here, commit, push.
2. Rotate your Gemini key (Google AI Studio: create a new key, delete the old one) and update .env.
   The old one was shared in a chat. .env is correctly listed in .gitignore; keep it that way.
3. Retire ai_scraper.py and batch_scraper.py (move them to a folder called old/). Use draft_rows.py instead.
4. In RemoteConfig.kt set DATA_URL to https://stipendo.netlify.app/data/scholarships.json
5. Weekly: watch_pages.py > draft_rows.py --changed > read the evidence file > check the official page >
   copy good rows into scholarships.csv > csv_to_json.py > build_site.py > push.

## draft_rows.py v2
- Reads GEMINI_API_KEY (free tier) or ANTHROPIC_API_KEY from .env, without any extra package.
- Skips pages with almost no admission words (homepages) and tells you to find the real notice page.
- Keeps every quote check: a fact with no matching quote on the page is dropped.
- Gemini part not tested against the real service from here (offline tests only). Try it on the Nanjing page first.
