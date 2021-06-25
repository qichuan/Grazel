# Grazel

**Grazel** stands for `Gradle` to `Bazel`.

It is a Gradle plugin that enables you to migrate Android projects to [Bazel build](https://bazel.build) system in an incremental and automated fashion. 

# How it works

It works by automatically generating Bazel scripts for given Android project based on your Gradle configuration. For simple projects, it should be able to migrate, fully build and launch the app with `bazel mobile-install //<target-name>`. 

In advanced cases, where entire project might not be migratable, it migrates part of the graph and sets up hybrid build where part of the graph can be built with Bazel and rest with Gradle. 

# Components

* [Gradle plugin](https://github.com/grab/Grazel/tree/master/grazel-gradle-plugin)
* A Kotlin Starlark DSL to generate Starlark code in a type-safe way.
* [Grab Bazel Common](https://github.com/grab/grab-bazel-common) - Custom rules to bridge the gap between Gradle/Bazel.

# Goals

* Reduce the overall migration effort for migration via automation.
* Setup hybrid build to establish early feedback loop on CI during migration.
* Minimal source changes to not impact feature delivery - supported by [Grab Bazel Common](https://github.com/grab/grab-bazel-common).
* Gradle as source of truth until migration is complete.

For documentation, please visit [link](https://grab.github.io/Grazel/).

# License

```
Copyright 2021 Grabtaxi Holdings PTE LTE (GRAB)

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