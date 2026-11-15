# Stipendo v26: draft in safe batches

Replace tools\draft_rows.py. Then (PowerShell, inside F:\Stipendo\tools):
    python draft_rows.py <one official page address>                       # first test, on a page whose answer you know
    python draft_rows.py --universities --todo-only --limit 5               # next 5 universities that are not in the app yet
Run again the next day for the next 5. It waits 5 seconds between pages to stay under the free per-minute limit.
Put GEMINI_API_KEY (and optionally GEMINI_MODEL=gemini-3.5-flash-lite) in .env. Never commit .env.
