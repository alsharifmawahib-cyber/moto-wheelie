MOTO WHEELIE CO-OP (Android)

Build the APK
1. Open this folder in Android Studio and wait for Gradle sync (needs internet once;
   it also downloads three.js into app/src/main/assets automatically).
2. Build > Build Bundle(s) / APK(s) > Build APK(s).
3. APK: app/build/outputs/apk/debug/app-debug.apk. Install it on both phones.

Play without internet
1. Phone A: turn on the mobile hotspot. Phone B: connect to it.
2. Phone A: open the app, tap "Host a game". The top of the screen shows the address.
3. Phone B: open the app, type that address, tap "Join a game".
4. Phone A holds the gas, phone B holds the brake.

Build the APK from a phone (no computer)
In Termux:
  pkg install git gh -y
  git config --global user.name "me" && git config --global user.email "me@example.com"
  gh auth login
  git init -b main && git add . && git commit -m init
  gh repo create moto-wheelie --public --source=. --push
Then open github.com > your repo > Actions > latest run > Artifacts > MotoWheelie-apk.
