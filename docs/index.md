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


# Getting Started

## Requirements

* [Buildifier](https://github.com/bazelbuild/buildtools/tree/master/buildifier) is installed and avaialble in the path.

=== "Mac"
    Install via [homebrew](https://brew.sh/). 
    ```bash
    brew install buildifier
    ```
=== "Linux"
    Install via `apt`. 
    ```bash 
    sudo apt-get install nodejs npm
    npm i -g @bazel/buildifier
    ```

## Apply and run

> Grazel will be soon available on `mavenCentral`. Until then please clone the project and use Gradle composite builds to run locally.

In `settings.gradle`:

```groovy
includeBuild("<path to this repo>/grazel-gradle-plugin") {
    dependencySubstitution {
        substitute module("com.grab:grazel") with project(":")
    }
}
```

In `build.gradle`:

```groovy
buildscript {
    dependencies {
        classpath "com.grab:grazel:0.1.0"
    }
}
apply plugin: "com.grab.grazel"

// Grazel configuration
grazel {
    // DSL
}
```

Grazel registers `migrateToBazel` lifecycle task that can be used to generate Bazel build scripts. By default, it filters out modules based on a set of migration criteria and generates scripts only for support modules.

For more advanced configuration options, see [Configuration](configuration.md).

<!-- # Demo

[Topeka](https://github.com/android/topeka) project migrated with Grazel. (Note: dynamic feature modules are not supported yet)

<video width="100%" controls>
  <source src="video/grazel-demo.mp4" type="video/mp4">
  Your browser does not support the video tag.
</video>
<br> -->

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