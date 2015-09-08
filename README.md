# Mensa-UPB

<p align="center">
<img src="/screenshot_main.png"></img>
</p>

There already exist a few Android apps for the restaurants of the University of Paderborn, yet I've found no one functional enough to be a daily driver. Some have a great design, yet lack image previews; others are outdated and don't work anymore as they rely on parsing the website of the Studierendenwerk.

# Advantages of this project
Mensa-UPB works with the official API of the Studierendenwerk, so it's unlikely to break anytime soon.
I've also tried to make it use modern technologies, like Google's support libraries, RxJava and many others. It's also written almost entirely in Kotlin, JetBrains relatively new language that's a more innovative alternative to Java, yet preserves Java-Interop as much as possible.
In addition to that, the app uses the new data binding feature and targets the Marshmallow SDK.

# Contributions
Contributions are of course welcome, but unfortunately an API key is required for communicating with the official API. It has to be put into the api_id.properties file in the root directory of the project.
I'm planning to revoke this restriction by creating an own API service that wraps the official API and doesn't require an API key. There's no ETA on that though.

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