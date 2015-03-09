# sonar-android-gradle-plugin
SonarQube Plugin that imports the Android Lint Report into Sonar database.

This currently only imports issues that are detected in .java and .xml files.

The lint-rules-gen project gets the current rules from Android Lint nad creates Sonar rules.xml and profile files that are required for the plugin

License
-------

    Copyright 2015 Jordan Hansen

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
