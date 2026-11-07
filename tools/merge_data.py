import json
import os

# File paths (adjust if your folder structure is different)
BATCH_FILE = "all_scholarships.json"
MAIN_FILE = "../site/data/scholarships.json" 

def clean_and_merge():
    print(f"Loading {BATCH_FILE}...")
    with open(BATCH_FILE, "r", encoding="utf-8") as f:
        batch_data = json.load(f)

    print(f"Loading {MAIN_FILE}...")
    with open(MAIN_FILE, "r", encoding="utf-8") as f:
        main_json = json.load(f)

    main_scholarships = main_json.get("scholarships", [])
    
    # Track IDs we already have to prevent duplicates
    existing_ids = {item['id'] for item in main_scholarships}
    
    added_count = 0

    for item in batch_data:
        # Fix the null bugs so the Android app doesn't crash
        if item.get("stipendRmbByDegree") is None:
            item["stipendRmbByDegree"] = {}
            
        if item.get("funding") is None:
            item["funding"] = "VARIES" # Default fallback
            
        if item.get("lengths") is None:
            item["lengths"] = []

        # Only add if it's a new ID
        if item['id'] not in existing_ids:
            main_scholarships.append(item)
            existing_ids.add(item['id'])
            added_count += 1
            print(f"✅ Added: {item['university']}")
        else:
            print(f"⏭️ Skipped (Duplicate): {item['university']}")

    # Update the root 'updated' date
    main_json['scholarships'] = main_scholarships
    main_json['updated'] = "2026-11-07" # Or use datetime.date.today().isoformat()

    # Save back to the main file
    with open(MAIN_FILE, "w", encoding="utf-8") as f:
        json.dump(main_json, f, indent=2, ensure_ascii=False)

    print(f"\n🎉 Done! Added {added_count} new scholarships to {MAIN_FILE}.")
    print("You can now commit this file to GitHub.")

if __name__ == "__main__":
    clean_and_merge()