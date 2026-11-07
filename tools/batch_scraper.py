import json
import time
import random
import requests
from ai_scraper import fetch_webpage_text, extract_scholarship_json

INPUT_FILE = "moe_universities.json"
OUTPUT_FILE = "all_scholarships.json"

# Known admissions URLs for specific universities (from search results)
KNOWN_URLS = {
    "中央民族大學": "https://cie.muc.edu.cn/info/1666/11449.htm",
    "中央音樂學院": "https://liuxue.ccom.edu.cn/",
    "中央美術學院": "https://www.cafa.edu.cn/st/2025/80633278.htm",
    "中央財經大學": "https://sice.cufe.edu.cn/admissions/info/1061/3450.htm",
    "中國人民大學": "http://sgl.ruc.edu.cn/",
    # Add more as you discover them
}

def find_admissions_url(uni_name, main_url):
    """Try to find the admissions URL."""
    # 1. Check if we have a known URL
    if uni_name in KNOWN_URLS:
        return KNOWN_URLS[uni_name]
    
    # 2. Try common subdomain patterns
    # Extract domain from main_url (e.g., muc.edu.cn from http://www.muc.edu.cn/)
    domain = main_url.replace("http://", "").replace("https://", "").split("/")[0]
    if domain.startswith("www."):
        domain = domain[4:]  # Remove www.
    
    # Common patterns for international student offices
    patterns = [
        f"https://cie.{domain}/",
        f"https://iso.{domain}/",
        f"https://liuxue.{domain}/",
        f"https://sice.{domain}/",
        f"https://international.{domain}/",
        f"https://admission.{domain}/",
        f"https://study.{domain}/",
    ]
    
    for url in patterns:
        try:
            # Quick check if the URL exists (HEAD request)
            response = requests.head(url, timeout=5, allow_redirects=True)
            if response.status_code == 200:
                print(f"   🔍 Found potential admissions page: {url}")
                return url
        except:
            pass
    
    return None

def run_batch():
    print("Loading university list...")
    with open(INPUT_FILE, "r", encoding="utf-8") as f:
        universities = json.load(f)

    # Test with the first 5 universities
    universities_to_scrape = universities[10:15]

    all_scholarships = []
    
    # Load existing progress if any
    try:
        with open(OUTPUT_FILE, "r", encoding="utf-8") as f:
            all_scholarships = json.load(f)
    except:
        pass

    for i, uni in enumerate(universities_to_scrape):
        uni_name = uni['name']
        main_url = uni['website']
        
        print(f"\n[{i+1}/{len(universities_to_scrape)}] Processing: {uni_name}")
        
        # Find the admissions URL
        target_url = find_admissions_url(uni_name, main_url)
        
        if not target_url:
            print(f"   ❌ Could not find admissions page. Skipping.")
            continue
        
        print(f"   🎯 Using URL: {target_url}")
        
        # Fetch and extract
        try:
            text = fetch_webpage_text(target_url)
            json_data = extract_scholarship_json(text, target_url)
            
            if json_data.get("lengths") is None:
                json_data["lengths"] = []
                
            all_scholarships.append(json_data)
            print(f"   ✅ Successfully extracted data for {uni_name}")
            
            # Save progress
            with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
                json.dump(all_scholarships, f, indent=2, ensure_ascii=False)
                
        except Exception as e:
            print(f"   ❌ Extraction failed: {e}")
            
        # Be polite to servers
        wait_time = random.uniform(5, 10)
        print(f"   Sleeping for {wait_time:.1f} seconds...")
        time.sleep(wait_time)

    print(f"\n🎉 DONE! Successfully extracted {len(all_scholarships)} scholarships.")
    print(f"Check the {OUTPUT_FILE} file in your tools folder.")

if __name__ == "__main__":
    run_batch()