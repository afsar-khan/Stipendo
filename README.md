# Stipendo v14: the app updates its data from your website

Apply on top of v13 (copy folders over your project, replace existing files).
The manifest is replaced too: it adds the INTERNET permission. No Gradle changes.
Put the tools/ and site/ folders next to app/ in the project root.

How it works
- The app starts with the data bundled inside it (works offline, always).
- In the background it asks your website for newer data. If the file is valid, not older than
  what the phone has, and not suddenly less than half the size, it replaces the phone's copy and
  the list refreshes. Anything wrong: ignored, nothing breaks.
- Checking is cheap: the app sends the last ETag, and the server answers "unchanged".
- When you release a new version of the app, its bundled data is used if it is newer than the download.

One-time setup (about 15 minutes)
1. Go to Netlify (netlify.com/drop) and drag the `site` folder onto it. You get an address like
   https://something.netlify.app
2. Open  https://something.netlify.app/data/scholarships.json  in a browser. You should see the data.
3. Put that address in app/src/main/java/com/stipendo/app/data/RemoteConfig.kt (DATA_URL).
   Later, add your domain stipendo.app in Netlify (Domain settings) and change DATA_URL to
   https://stipendo.app/data/scholarships.json  (an app update is needed for that one change).
4. Run the app on a phone. In Logcat (filter "Stipendo") you will see "Data update skipped"
   if something is wrong, and nothing at all when it works.

Test that updates really arrive
- Install the app. Then add a row to tools/scholarships.csv, run  python3 tools/csv_to_json.py
  and  python3 tools/build_site.py , upload the site folder again, force-stop the app and open it:
  the new entry appears (with a "New" mark) without reinstalling.

Weekly routine: edit the spreadsheet > csv_to_json.py > build_site.py > upload the site folder.
(Netlify can also deploy automatically from a GitHub repository if you prefer.)

Privacy policy: the app now contacts your website when it opens, so your host can see the phone's
IP address, like any website visit. Say so in the policy. No personal data is sent.

Safety notes
- Whoever controls the website controls what students see. Use a strong password and two-step
  login on Netlify and on your domain registrar.
- Keep your old data file: if a bad file ever goes out, republish the previous one.
