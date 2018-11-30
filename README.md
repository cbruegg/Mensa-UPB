# Mensa-UPB

There already exist a few Android apps for the restaurants of the University of Paderborn, yet I've found no one functional enough to be a daily driver. Some have a great design, yet lack image previews; others are outdated and don't work anymore as they rely on parsing the website of the Studierendenwerk.

Please find screenshots and more information about the app's features in the [Play Store](https://play.google.com/store/apps/details?id=com.cbruegg.mensaupb).

# Advantages of this project
Mensa-UPB works with a proxy API that calls the official API of the Studierendenwerk, so it's unlikely to break anytime soon. On top of this, the proxy API allows for deploying hotfixes.
I've also tried to make it use modern technologies, like Google's support libraries, Kotlin coroutines and many others. It's also written entirely in Kotlin, including the Gradle buildscript.
Another goal of this app is to always target the latest version of Android.

# Contributions
Contributions are of course welcome, but unfortunately an API key provided by the Studierendenwerk is required for communicating with the official API. It has to be put into the `api_id.properties` file in the root directory of the project. Additionally, this app relies on Firebase Crashlytics for crash reporting and requires a `google-services.json`.

# License

```
Copyright 2015 Christian Brüggemann

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
