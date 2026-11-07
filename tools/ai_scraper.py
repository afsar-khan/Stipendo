import sys
import json
import datetime
import time
import random
import os
import requests
from bs4 import BeautifulSoup
from google import genai
from dotenv import load_dotenv

# Load the API key from the .env file securely
load_dotenv()
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")

if not GEMINI_API_KEY:
    print("ERROR: GEMINI_API_KEY not found in .env file!")
    sys.exit(1)

def fetch_webpage_text(url):
    """Downloads the webpage and extracts only the readable text."""
    print(f"Fetching data from: {url} ...")
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
    }
    try:
        response = requests.get(url, headers=headers, timeout=15, verify=False)
        response.raise_for_status()
    except Exception as e:
        print(f"Error fetching URL: {e}")
        

    soup = BeautifulSoup(response.text, 'html.parser')
    
    # Remove scripts and styles to save tokens
    for script in soup(["script", "style", "nav", "footer", "header"]):
        script.extract()
        
    text = soup.get_text(separator=' ', strip=True)
    # Limit text to ~30,000 characters to stay within free API limits
    return text[:30000]

def extract_scholarship_json(text, url):
    """Sends the text to Gemini and asks for the specific JSON format (with retry)."""
    
    today = datetime.date.today().isoformat()
    
    prompt = f"""
    You are an AI assistant for the 'Stipendo' app. Your job is to read the following webpage text and extract information about a scholarship for international students in China.
    
    Here is the exact JSON schema you must follow. DO NOT output any markdown (like ```json), just output the raw JSON object. If a field is not mentioned in the text, use null. 
    For enums (degrees, languages, lengths, funding), you MUST ONLY use the allowed values listed below.

    SCHEMA:
    {{
      "id": "generate-a-unique-slug-like-university-name-year",
      "university": "University Name in English",
      "universityZh": "University Name in Chinese (or null)",
      "city": "City Name",
      "name": "Full official name of the scholarship",
      "provider": "Who provides it (e.g., China Scholarship Council, University Name)",
      "degrees": ["BACHELOR", "MASTER", "PHD", "LANGUAGE"],
      "languages": ["ENGLISH", "CHINESE"],
      "lengths": ["SIX_MONTHS", "ONE_YEAR"],
      "funding": "FULL, PARTIAL, VARIES, or SELF_FUNDED",
      "coversTuition": true,
      "coversHousing": true,
      "tuitionRmbPerYear": integer or null,
      "stipendRmbByDegree": {{"MASTER": 3000, "PHD": 3500}},
      "languageRequirement": "Text describing IELTS/HSK requirements",
      "deadline": "YYYY-MM-DD or null",
      "cscDeadline": "YYYY-MM-DD or null",
      "cscAgencyNumber": "Number or null",
      "officialUrl": "{url}",
      "sourceUrl": "{url}",
      "verification": "REPORTED",
      "lastChecked": null,
      "addedOn": "{today}",
      "updatedOn": "{today}"
    }}

    WEBPAGE TEXT:
    {text}
    """

    print("Asking AI to extract data (this takes 3-5 seconds)...")
    
    client = genai.Client(api_key=GEMINI_API_KEY)
    max_retries = 5  # Increased retries for better stability
    
    for attempt in range(max_retries):
        try:
            response = client.models.generate_content(
                model='gemini-3.5-flash-lite',
                contents=prompt,
            )
            
            raw_json = response.text.strip()
            if raw_json.startswith("```json"):
                raw_json = raw_json.replace("```json\n", "").replace("```", "").strip()
            elif raw_json.startswith("```"):
                 raw_json = raw_json.replace("```\n", "").replace("```", "").strip()
            
            # Parse to ensure it's valid JSON
            parsed_json = json.loads(raw_json)
            return parsed_json

        except Exception as e:
            error_message = str(e)
            # If it's a 503 error (high demand) and we still have retries left
            if "503" in error_message and attempt < max_retries - 1:
                # Exponential backoff: wait 2s, 4s, 8s, 16s
                wait_time = (2 ** attempt) + random.random() 
                print(f"Model busy (503). Retrying in {wait_time:.1f} seconds... (Attempt {attempt + 1}/{max_retries})")
                time.sleep(wait_time)
            else:
                # If it's a different error or we've run out of retries, exit
                print(f"Error during AI extraction: {e}")
                sys.exit(1)

if __name__ == "__main__":
    if GEMINI_API_KEY == "YOUR_API_KEY_HERE":
        print("ERROR: Please paste your Gemini API key inside the script first!")
        sys.exit(1)

    url_to_scrape = input("Paste the URL of the scholarship page: ")
    
    # 1. Get text from website
    page_text = fetch_webpage_text(url_to_scrape)
    
    # 2. Ask AI to format it
    scholarship_json = extract_scholarship_json(page_text, url_to_scrape)
    
    # 3. Print the result
    print("\n" + "="*50)
    print("✅ SUCCESS! COPY THE JSON BELOW:")
    print("="*50 + "\n")
    
    # Print with indentation so it's easy to read and copy
    print(json.dumps(scholarship_json, indent=2, ensure_ascii=False))
    
    print("\n" + "="*50)
    print("Note: Remember to verify the data on the official page before setting 'verification' to OFFICIAL_CHECKED!")