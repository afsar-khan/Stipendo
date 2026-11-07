import json
import time
import random
import requests
import os
from ai_scraper import fetch_webpage_text, extract_scholarship_json

# --- Configuration ---
INPUT_FILE = "moe_universities.json"
OUTPUT_FILE = "all_scholarships.json"
PROGRESS_FILE = "scraped_progress.json"
MAIN_APP_FILE = "../site/data/scholarships.json"

# Add URLs here as you find them
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
    "北京交通大學": "https://international.bjtu.edu.cn/",
    "北京林業大學": "https://international.bjfu.edu.cn/",
    "北京科技大學": "https://en.ustb.edu.cn/",
    "北京理工大學": "https://isc.bit.edu.cn/",
    "北京郵電大學": "https://international.bupt.edu.cn/",
    "北京體育大學": "https://international.bsu.edu.cn/",
    "清華大學": "https://www.tsinghua.edu.cn/en/Admissions.htm",
    "華北電力大學(北京)": "https://icd.ncepu.edu.cn/",
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

    # 2. Clear old batch file
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
            print(f"   ❌ Could not find admissions page. Skipping (Will retry next run).")
            # Do NOT add to progress so it retries next time
            continue
        
        print(f"   🎯 Using URL: {target_url}")
        
        try:
            text = fetch_webpage_text(target_url)
            json_data = extract_scholarship_json(text, target_url)
            
            if json_data.get("lengths") is None:
                json_data["lengths"] = []
                
            new_scholarships.append(json_data)
            print(f"   ✅ Successfully extracted data for {uni_name}")
            
            # Only mark as scraped if it succeeded
            scraped_names.add(uni_name)
                
        except Exception as e:
            print(f"   ❌ Extraction failed: {e}")
            
        wait_time = random.uniform(5, 8)
        print(f"   Sleeping for {wait_time:.1f} seconds...")
        time.sleep(wait_time)

    # 5. --- AUTOMATIC CLEANUP & MERGE ---
    print("\n🧹 Cleaning data and merging into app...")
    
    # Save raw batch (optional)
    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        json.dump(new_scholarships, f, indent=2, ensure_ascii=False)

    # Load main app file
    if os.path.exists(MAIN_APP_FILE):
        with open(MAIN_APP_FILE, "r", encoding="utf-8") as f:
            main_json = json.load(f)
    else:
        main_json = {"version": 1, "updated": "2026-11-07", "scholarships": []}

    existing_ids = {item['id'] for item in main_json['scholarships']}
    added_count = 0

    for item in new_scholarships:
        # AUTO-DELETE BAD DATA: If no deadline and no funding, skip it
        if item.get("deadline") is None and item.get("funding") == "VARIES":
            print(f"   🗑️ Deleted low-quality entry: {item['university']}")
            continue

        # Fix nulls
        if item.get("stipendRmbByDegree") is None: item["stipendRmbByDegree"] = {}
        if item.get("funding") is None: item["funding"] = "VARIES"
        if item.get("lengths") is None: item["lengths"] = []

        if item['id'] not in existing_ids:
            main_json['scholarships'].append(item)
            existing_ids.add(item['id'])
            added_count += 1
            print(f"   ✅ Added to app: {item['university']}")

    # Save main file
    with open(MAIN_APP_FILE, "w", encoding="utf-8") as f:
        json.dump(main_json, f, indent=2, ensure_ascii=False)

    # 6. Save the progress
    with open(PROGRESS_FILE, "w", encoding="utf-8") as f:
        json.dump(list(scraped_names), f, indent=2, ensure_ascii=False)

    print(f"\n🎉 DONE! Added {added_count} new scholarships to your app.")
    print(f"Run the script again for the next 5 universities.")

if __name__ == "__main__":
    run_batch()