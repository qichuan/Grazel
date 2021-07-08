/*
 * Copyright 2021 Grabtaxi Holdings PTE LTE (GRAB)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.grab.grazel.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import javax.inject.Inject
import javax.inject.Singleton

internal enum class ConfigurationScope {
    BUILD, TEST, ANDROID_TEST
}

internal interface ConfigurationDataSource {
    /**
     * Return a sequence of the configurations which are filtered out by the ignore flavors & build variants
     * these configuration can be queried or resolved.
     */
    fun resolvedConfigurations(project: Project): Sequence<Configuration>

    /**
     * Return a sequence of the configurations which are filtered out by the ignore flavors & build variants
     */
    fun configurations(project: Project, scope: ConfigurationScope = ConfigurationScope.BUILD): Sequence<Configuration>
}

@Singleton
internal class DefaultConfigurationDataSource @Inject constructor(
    private val androidVariantDataSource: AndroidVariantDataSource
) : ConfigurationDataSource {

    override fun configurations(project: Project, scope: ConfigurationScope): Sequence<Configuration> {
        val ignoreFlavors = androidVariantDataSource.getIgnoredFlavors(project)
        val ignoreVariants = androidVariantDataSource.getIgnoredVariants(project)
        return project.configurations
            .asSequence()
            .filter { !it.name.contains("classpath", true) && !it.name.contains("lint") }
            .filter { !it.name.contains("coreLibraryDesugaring") }
            .filter { !it.name.contains("_internal_aapt2_binary") }
            .filter { !it.name.contains("archives") }
            .filter {
                when (scope) {
                    ConfigurationScope.TEST -> !it.isAndroidTest()
                    ConfigurationScope.ANDROID_TEST -> !it.isUnitTest()
                    else -> it.isNotTest()
                }
            }
            .filter { config ->
                !config.name.let { configurationName ->
                    ignoreFlavors.any { configurationName.contains(it.name, true) }
                            || ignoreVariants.any { configurationName.contains(it.name, true) }
                }
            }
    }

    override fun resolvedConfigurations(project: Project): Sequence<Configuration> {
        return configurations(project).filter { it.isCanBeResolved }
    }
}

internal fun Configuration.isUnitTest() = name.contains("UnitTest", true) || name.startsWith("test")
internal fun Configuration.isAndroidTest() = name.contains("androidTest", true)
internal fun Configuration.isNotTest() = !name.contains("test", true)