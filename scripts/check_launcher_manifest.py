from pathlib import Path
import sys

manifest = Path("app/src/main/AndroidManifest.xml").read_text()
required = [
    'android:name=".LoginActivity"',
    'android:exported="true"',
    'android.intent.action.MAIN',
    'android.intent.category.LAUNCHER',
    'android:icon="@mipmap/ic_launcher"',
    'android:roundIcon="@mipmap/ic_launcher_round"',
]
missing = [item for item in required if item not in manifest]
if missing:
    print("Launcher manifest check FAILED:")
    for item in missing:
        print(" - missing", item)
    sys.exit(1)
print("Launcher manifest check PASS")
