"""Shared by watch_pages.py and draft_rows.py: download a web page and return its visible text."""
import re, urllib.error, urllib.request
from html.parser import HTMLParser

class _Visible(HTMLParser):
    def __init__(self):
        super().__init__(); self.parts = []; self.skip = 0
    def handle_starttag(self, tag, attrs):
        if tag in ("script", "style", "noscript"): self.skip += 1
    def handle_endtag(self, tag):
        if tag in ("script", "style", "noscript") and self.skip: self.skip -= 1
    def handle_data(self, data):
        if not self.skip and data.strip(): self.parts.append(data.strip())

def html_to_text(html, limit=40000):
    parser = _Visible(); parser.feed(html)
    return re.sub(r"\s+", " ", " ".join(parser.parts)).strip()[:limit]

# Some university sites drop requests that do not look like a normal browser, so we send a browser-like header.
HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36 StipendoPageRadar",
    "Accept": "text/html,application/xhtml+xml",
    "Accept-Language": "en-US,en;q=0.9,zh-CN;q=0.8",
}

def page_text(url, limit=40000):
    last = None
    for attempt in range(2):                     # one retry: some sites close the first connection
        try:
            req = urllib.request.Request(url, headers=HEADERS)
            with urllib.request.urlopen(req, timeout=30) as r:
                raw = r.read(); charset = r.headers.get_content_charset() or "utf-8"
            return html_to_text(raw.decode(charset, errors="replace"), limit)
        except urllib.error.HTTPError:
            raise                                # a real answer such as 404 or 412: retrying will not help
        except Exception as e:
            last = e
    raise last

def sentences(text):
    return [s.strip() for s in re.split(r"(?<=[.!?。])\s+", text) if len(s.strip()) > 12]
