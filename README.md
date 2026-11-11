# Stipendo v24: find out what your free Gemini key can do

Copy tools\check_gemini.py and tools\find_notice_pages.py over your project (replace the second). Then in PowerShell:
    cd F:\Stipendo\tools
    python check_gemini.py
It tests your model (and three others) with one plain request and one Google Search request each, and tells you what to do.
Do not run it repeatedly: every request counts toward the daily quota.
