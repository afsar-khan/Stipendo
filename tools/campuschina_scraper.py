import requests
from bs4 import BeautifulSoup
import json
import time
import random

# The regions page contains the detail cards with Website URLs
BASE_URL = "https://campuschina.org/universities/regions.html"

def scrape_campuschina():
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
    }

    print(f"Fetching: {BASE_URL}")
    try:
        response = requests.get(BASE_URL, headers=headers, timeout=20)
        response.raise_for_status()
    except Exception as e:
        print(f"Error fetching page: {e}")
        return

    soup = BeautifulSoup(response.text, 'html.parser')
    universities = []

    # The page structure typically wraps each university in a block.
    # We look for common containers. The exact class might vary, so we search for blocks
    # that contain a "Website:" label.
    
    # Strategy: Find all text blocks that contain "Website:" and work backwards
    # This is a robust fallback if the specific CSS class isn't known.
    
    # A more structured approach if the HTML has clear cards:
    # Look for headers (h2 or h3) followed by details.
    # Based on the page structure, university names often appear as headings.
    
    # Let's try to find all potential containers. 
    # We'll look for elements that contain the "Website:" text.
    
    # Find all elements that might be a university card/block
    # This selector is a guess based on typical campuschina structure; 
    # you may need to inspect the page and adjust the class name.
    # For now, let's grab all headings and then look for the website link nearby.
    
    # Better approach: Iterate through all elements, find the one with "Website:"
    # and then look at its parent container to get the name.
    
    # Since the page is complex, let's just find all links that look like external websites
    # and try to associate them with the preceding text.
    
    # Let's use a simpler method: find all elements with the text "Website:" and extract the URL
    # Then find the nearest heading before it.
    
    # However, the most reliable way is to inspect the HTML structure.
    # Assuming each university is in a <div> with a specific class, let's try to find them.
    
    # Fallback: Just grab all text and parse.
    # But we need structure.
    
    # Let's try to find all <h2> or <h3> tags which likely contain the names.
    # And then find the "Website:" link that follows.
    
    # NOTE: Without inspecting the live DOM, this is a best-effort approach.
    # You might need to right-click the page, inspect element, and find the specific class.
    
    # Let's try to find blocks by looking for the "Website:" text.
    # We can use a regex or just string matching on the parent.
    
    # Let's try a different strategy: Find all links that are NOT campuschina internal links.
    # The website URLs are usually external links (http://...).
    
    # We'll iterate through all elements, look for "Website:" text, then find the <a> tag next to it.
    
    # Find all text nodes or elements containing "Website:"
    # In BeautifulSoup, we can find tags by string.
    
    # Let's find all tags that contain "Website:" as text
    website_labels = soup.find_all(string=lambda text: text and "Website:" in text)
    
    for label in website_labels:
        # The label is likely a text node inside a <p> or <div>.
        # The URL is usually in the next <a> tag or the same parent.
        parent = label.parent
        
        # Try to find the URL in the parent or next sibling
        link_tag = parent.find_next('a')
        
        if link_tag and link_tag.get('href'):
            url = link_tag.get('href')
            
            # Filter out internal campuschina links (they might have relative links)
            if url.startswith('http') and 'campuschina' not in url:
                # Now find the university name. It's usually the heading before this block.
                # We can look for the previous <h2>, <h3>, or <strong> tag.
                # Or look at the grandparent container.
                
                # Let's try to find the nearest heading
                heading = parent.find_previous(['h2', 'h3', 'h4', 'strong'])
                
                name = "Unknown"
                if heading:
                    name = heading.get_text(strip=True)
                
                # If name is still Unknown, try to get text from the parent container
                if name == "Unknown" or len(name) > 100:
                    # Maybe the heading is far away, try to get the first text block in the container
                    container = parent.find_parent('div')
                    if container:
                        # Get the first line of text, which might be the name
                        text_lines = container.get_text(separator='\n', strip=True).split('\n')
                        if text_lines:
                            name = text_lines[0]
                
                # Clean up the name (remove extra info like "211", "985")
                if name:
                    # Remove common suffixes/prefixes if they exist
                    name = name.replace('211', '').replace('985', '').strip()
                
                universities.append({
                    "name": name,
                    "website": url
                })
                
                print(f"Found: {name} -> {url}")

    # Remove duplicates (if any)
    seen = set()
    unique_universities = []
    for uni in universities:
        if uni['name'] not in seen:
            seen.add(uni['name'])
            unique_universities.append(uni)

    # Save to file
    output_file = "campuschina_universities.json"
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(unique_universities, f, indent=2, ensure_ascii=False)
    
    print(f"\n✅ Scraped {len(unique_universities)} universities.")
    print(f"✅ Saved to {output_file}")

if __name__ == "__main__":
    scrape_campuschina()