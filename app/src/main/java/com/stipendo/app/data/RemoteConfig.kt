package com.stipendo.app.data

object RemoteConfig {
    /**
     * Where the app looks for newer data. Upload site/data/scholarships.json (see tools/build_site.py)
     * to your website and put its address here. It must start with https://.
     * Set it to "" to switch downloads off; the app then only uses the data bundled inside it.
     */
    const val DATA_URL = "https://stipendo.app/data/scholarships.json"
}
