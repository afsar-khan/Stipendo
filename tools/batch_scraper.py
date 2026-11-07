import json
import time
import random
import requests
import os
from ai_scraper import fetch_webpage_text, extract_scholarship_json

INPUT_FILE = "moe_universities.json"
OUTPUT_FILE = "all_scholarships.json"
PROGRESS_FILE = "scraped_progress.json" # This file will remember what we did

# Known admissions URLs for specific universities
KNOWN_URLS = {
    "中央民族大學": "https://cie.muc.edu.cn/info/1666/11449.htm",
    "中央音樂學院": "https://liuxue.ccom.edu.cn/",
    "中央美術學院": "https://www.cafa.edu.cn/st/2025/80633278.htm",
    "中央財經大學": "https://sice.cufe.edu.cn/admissions/info/1061/3450.htm",
    "中國人民大學": "http://sgl.ruc.edu.cn/",
    "中國石油大學(北京)": "https://admission.cup.edu.cn/",
    "中國礦業大學(北京)": "https://international.cumtb.edu.cn/",
    "北京大學": "https://admission.pku.edu.cn/",
    "北京化工大學": "https://study.buct.edu.cn/",
    "北京外國語大學": "https://liuxue.bfsu.edu.cn/",
    # Add more here as you find them
}

def find_admissions_url(uni_name, main_url):
    if uni_name in KNOWN_URLS:
        return KNOWN_URLS[uni_name]
    
    domain = main_url.replace("http://", "").replace("https://", "").split("/")[0]
    if domain.startswith("www."):
        domain = domain[4:]
    
    patterns = [
        f"https://international.{domain}/",
        f"https://iso.{domain}/",
        f"https://cie.{domain}/",
        f"https://liuxue.{domain}/",
        f"https://sice.{domain}/",
        f"https://admission.{domain}/",
        f"https://study.{domain}/",
    ]
    
    for url in patterns:
        try:
            response = requests.head(url, timeout=5, allow_redirects=True, verify=False)
            if response.status_code == 200:
                return url
        except:
            pass
    return None

def run_batch():
    # 1. Load Progress
    if os.path.exists(PROGRESS_FILE):
        with open(PROGRESS_FILE, "r", encoding="utf-8") as f:
            scraped_names = set(json.load(f))
    else:
        scraped_names = set()

    # 2. Delete old batch file to start fresh
    if os.path.exists(OUTPUT_FILE):
        os.remove(OUTPUT_FILE)

    print("Loading university list...")
    with open(INPUT_FILE, "r", encoding="utf-8") as f:
        universities = json.load(f)

    # 3. Filter out universities we already scraped
    remaining_universities = [u for u in universities if u['name'] not in scraped_names]
    
    if not remaining_universities:
        print("🎉 All universities have been scraped!")
        return

    # 4. Take the next 5
    batch = remaining_universities[:5]
    print(f"Found {len(remaining_universities)} remaining. Processing next {len(batch)}...\n")

    new_scholarships = []

    for i, uni in enumerate(batch):
        uni_name = uni['name']
        main_url = uni['website']
        
        print(f"[{i+1}/{len(batch)}] Processing: {uni_name}")
        
        target_url = find_admissions_url(uni_name, main_url)
        
        if not target_url:
            print(f"   ❌ Could not find admissions page. Skipping.")
            # We still add it to progress so we don't get stuck on it forever
            scraped_names.add(uni_name) 
            continue
        
        print(f"   🎯 Using URL: {target_url}")
        
        try:
            text = fetch_webpage_text(target_url)
            json_data = extract_scholarship_json(text, target_url)
            
            if json_data.get("lengths") is None:
                json_data["lengths"] = []
                
            new_scholarships.append(json_data)
            print(f"   ✅ Successfully extracted data for {uni_name}")
            
            # Save this university to progress
            scraped_names.add(uni_name)
                
        except Exception as e:
            print(f"   ❌ Extraction failed: {e}")
            
        wait_time = random.uniform(5, 8)
        print(f"   Sleeping for {wait_time:.1f} seconds...")
        time.sleep(wait_time)

    # 5. Save the new batch
    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        json.dump(new_scholarships, f, indent=2, ensure_ascii=False)

    # 6. Save the progress
    with open(PROGRESS_FILE, "w", encoding="utf-8") as f:
        json.dump(list(scraped_names), f, indent=2, ensure_ascii=False)

    print(f"\n🎉 DONE! Extracted {len(new_scholarships)} scholarships for this batch.")
    print(f"Progress saved. Run the script again for the next 5.")

if __name__ == "__main__":
    run_batch()