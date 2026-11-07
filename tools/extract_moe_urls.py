import pdfplumber
import json
import re

# The name of the PDF you downloaded
PDF_PATH = "moe_universities.pdf"
OUTPUT_FILE = "moe_universities.json"

def extract_urls_from_pdf():
    universities = []
    
    print(f"Opening {PDF_PATH}...")
    with pdfplumber.open(PDF_PATH) as pdf:
        for page_num, page in enumerate(pdf.pages):
            # Extract tables from the page
            tables = page.extract_tables()
            
            for table in tables:
                for row in table:
                    # The table usually has columns: Serial Number, School Name, Website, Location
                    # We need to find the URL in the row. It usually starts with http:// or https://
                    if not row or len(row) < 3:
                        continue
                        
                    # Clean up the cells (remove newlines)
                    cleaned_row = [str(cell).replace('\n', ' ').strip() if cell else "" for cell in row]
                    
                    # Look for the website (usually column index 2, but we'll search all cells)
                    url = None
                    name = None
                    
                    for cell in cleaned_row:
                        # Check if this cell looks like a URL
                        if "http" in cell:
                            # Extract just the URL part using regex
                            match = re.search(r'(https?://[^\s]+)', cell)
                            if match:
                                url = match.group(1)
                        # The name is usually the longest text that isn't a number or URL
                        elif cell and len(cell) > 3 and not cell.isdigit() and "http" not in cell:
                            # Ignore "Location" column headers or short city names if possible
                            if not name:
                                name = cell
                    
                    if name and url:
                        universities.append({
                            "name": name,
                            "website": url
                        })
                        print(f"Found: {name} -> {url}")

    # Remove duplicates based on URL
    seen_urls = set()
    unique_unis = []
    for uni in universities:
        if uni['website'] not in seen_urls:
            seen_urls.add(uni['website'])
            unique_unis.append(uni)

    # Save to JSON
    with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
        json.dump(unique_unis, f, indent=2, ensure_ascii=False)
        
    print(f"\n✅ Extracted {len(unique_unis)} universities.")
    print(f"✅ Saved to {OUTPUT_FILE}")

if __name__ == "__main__":
    extract_urls_from_pdf()