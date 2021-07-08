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

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.TestVariant
import com.android.build.gradle.api.UnitTestVariant
import com.android.builder.model.ProductFlavor
import com.grab.grazel.configuration.DefaultVariantFilter
import com.grab.grazel.configuration.VariantFilter
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the
import javax.inject.Inject
import javax.inject.Singleton

internal interface AndroidVariantDataSource {
    /**
     * Variant filter instance to filter out unsupported variants
     */
    val variantFilter: Action<VariantFilter>?

    /**
     * This method will return the flavors which are ignored after evaluate the ignore variants
     * determined by [variantFilter]
     */
    fun getIgnoredFlavors(project: Project): List<ProductFlavor>

    /**
     * This method will return the variants which are ignored by the configuration determined by [variantFilter]
     */
    fun getIgnoredVariants(project: Project): List<BaseVariant>

    /**
     * @return The list of variants that can be migrated.
     */
    fun getMigratableVariants(project: Project): List<BaseVariant>
}

internal class DefaultAndroidVariantDataSource(
    private val androidVariantsExtractor: AndroidVariantsExtractor = DefaultAndroidVariantsExtractor(),
    override val variantFilter: Action<VariantFilter>? = null
) : AndroidVariantDataSource {

    override fun getIgnoredFlavors(project: Project): List<ProductFlavor> {
        val supportFlavors = getMigratableVariants(project).flatMap(BaseVariant::getProductFlavors)
        return androidVariantsExtractor.getFlavors(project)
            .filter { flavor -> !supportFlavors.any { it.name == flavor.name } }
    }

    private fun Project.androidVariants() =
        androidVariantsExtractor.getVariants(this) + androidVariantsExtractor.getUnitTestVariants(this)

    override fun getIgnoredVariants(project: Project): List<BaseVariant> {
        return project.androidVariants().filter(::ignoredVariantFilter)
    }

    override fun getMigratableVariants(project: Project): List<BaseVariant> {
        return project.androidVariants().filterNot(::ignoredVariantFilter)
    }

    private fun ignoredVariantFilter(
        variant: BaseVariant
    ): Boolean = DefaultVariantFilter(variant)
        .apply { variantFilter?.execute(this) }
        .ignored
}

internal interface AndroidVariantsExtractor {
    fun getUnitTestVariants(project: Project): Set<BaseVariant>
    fun getTestVariants(project: Project): Set<BaseVariant>
    fun getVariants(project: Project): Set<BaseVariant>
    fun getFlavors(project: Project): Set<ProductFlavor>
}


@Singleton
internal class DefaultAndroidVariantsExtractor @Inject constructor() : AndroidVariantsExtractor {

    override fun getVariants(project: Project): Set<BaseVariant> {
        return when {
            project.isAndroidApplication -> project.the<AppExtension>().applicationVariants
            project.isAndroidLibrary -> project.the<LibraryExtension>().libraryVariants
            else -> emptySet()
        }
    }

    override fun getTestVariants(project: Project): Set<BaseVariant> {
        return when {
            project.isAndroidApplication -> project.the<AppExtension>().testVariants
            project.isAndroidLibrary -> project.the<LibraryExtension>().testVariants
            else -> emptySet()
        }
    }

    override fun getUnitTestVariants(project: Project): Set<UnitTestVariant> {
        return when {
            project.isAndroidApplication -> project.the<AppExtension>().unitTestVariants
            project.isAndroidLibrary -> project.the<LibraryExtension>().unitTestVariants
            else -> emptySet()
        }
    }

    override fun getFlavors(project: Project): Set<ProductFlavor> {
        return when {
            project.isAndroidApplication -> project.the<AppExtension>().productFlavors
            project.isAndroidLibrary -> project.the<LibraryExtension>().productFlavors
            else -> emptySet()
        }
    }
}

internal fun AndroidVariantDataSource.getMigratableBuildVariants(project: Project): List<BaseVariant> =
    getMigratableVariants(project)
        .filter { it !is UnitTestVariant && it !is TestVariant }

internal fun AndroidVariantDataSource.getMigratableUnitTestVariants(project: Project): List<BaseVariant> =
    getMigratableVariants(project)
        .filterIsInstance<UnitTestVariant>()