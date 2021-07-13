# Migration Capabilities

Grazel uses a combination of automation (script generation) and [custom Bazel rules](https://github.com/grab/grab-bazel-common) to successfully migrate a project and to ensure both Gradle and Bazel can function together.


## Android configuration

Grazel automatically infers Android configuration via Gradle extensions and generates corresponding rules in `WORKSPACE` and in generated `andorid_library` or `android_binary` targets. These include (but not limited to):

* Variant aware Kotlin/Java source sets
* Variant aware assets/resources source sets
* Manifest placeholders
* Manifest files
* Custom keystore files specified via `signingConfigs.storeFile`
* Multidex configs
* Compile, Min, Target SDK versions
* Build tools version
* Jetified artifacts
* Build config fields - via [build_config](https://github.com/grab/grab-bazel-common#build-config-fields)
* String resource values - via [res_value](https://github.com/grab/grab-bazel-common#res-values)
* Google Play Services 
    * Crashlytics - via [tools_android](https://github.com/bazelbuild/tools_android/tree/master/tools/crashlytics) 
* Dagger - via [dagger_rules](https://github.com/google/dagger/blob/master/workspace_defs.bzl)
* Kotlin Parcelize support - via [parcelize_rules](https://github.com/grab/grab-bazel-common/tree/master/tools/parcelize)
* Databinding - Docs TBA

Known unsupported features

* Annotation processors - Planned
* Test targets - Planned
* Local aar files - Planned

## Dependencies

For dependencies, Grazel uses [rules_jvm_external](https://github.com/bazelbuild/rules_jvm_external) to resove dependencies.

### Versions

During migration, Grazel performs Gradle's dependency resolution instead of simply using what's declared on `build.gradle` before generating final artifact coordinates in Bazel scripts (i.e `maven_install`). This is done to ensure Bazel uses the same dependency version that is being used by the app at the runtime. The has the following advantages:

* Gradle `Configuration`'s resolution strategy, forced modules and substitutions are supported.
* Actual resolved version of the dependency is generated for `maven_install` rule.
* Detect supported artifacts for migration. See [repositories](repositories).

### Dependency Graph aware

Grazel captures module dependency graph and generates `deps` field in generated rules. 

For example, when `app` depends on `project(":quiz")`, Grazel would generate `quiz/BUILD.bazel` if `quiz` can be successfully migrated and then add `//quiz` in `app`'s `android_library/android_binary` target.

In any case, quiz is not [migrateable](migration_criteria.md), then `app` or any other module that depends on `quiz` won't be migrated.

### Maven artifact repositories

While Grazel is capable of generating correct `@maven://artifact_coordinate` targets which are Bazel's version of `configuration 'artifact_coordinate'`, Bazel's official `rules_jvm_external` does not support all types of Maven repositories. Currently supported repository types are

* Public Maven repositories
* Private Maven repositories authenticated by `basic` auth

Other types of Maven repositories such as `AWS` or private Maven with auth headers are not supported.

!!! warning
    Grazel automatically detects dependencies from unsupported repositories and any module that uses these dependencies will be excluded from migration.

In any case, if the project has unsupported or unresolvable dependencies, combination of [exclude artifacts](grazel_extension.md#exclude-artifacts) and [ignore artifacts](grazel_extension.md#ignore-artifacts) can be used to get a partial successful build.

### Java/Koltin modules

In addition to Android modules, pure Kotlin or Java modules are supported. Grazel infers `srcs` and `deps` from Gradle source sets/extensions and generates corresponding `kt_jvm_library` or `java_library`.

#### Kotlin Toolchain

Grazel generates [rules_kotlin](https://github.com/bazelbuild/rules_kotlin) scripts and provides [configuration](grazel_extension.md#kotlin) to generate [define_kt_toolchain](https://bazelbuild.github.io/rules_kotlin/kotlin#define_kt_toolchain) in root `BUILD.bazel`. The toolchain adds ability to toggle specific `rules_kotlin` features like multiplex workers, langugage version and ABI jars. ABI jars in particular brings considerable incremental build performance due to compile avoidance on non ABI changes.

Currently Kotlin toolchain values are not auto inferred and needs to be manually specified although this may change in the future.