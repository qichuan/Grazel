package com.grab.grazel.migrate.android

import com.android.build.gradle.api.AndroidSourceSet
import com.google.common.graph.ImmutableValueGraph
import com.grab.grazel.bazel.starlark.BazelDependency
import com.grab.grazel.gradle.AndroidVariantDataSource
import com.grab.grazel.gradle.ConfigurationScope
import com.grab.grazel.gradle.dependencies.DependenciesDataSource
import com.grab.grazel.gradle.getMigratableUnitTestVariants
import com.grab.grazel.migrate.kotlin.kotlinParcelizeDeps
import com.grab.grazel.migrate.unittest.UnitTestData
import dagger.Lazy
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

internal const val FORMAT_UNIT_TEST_NAME = "%s-test"

internal interface AndroidUnitTestDataExtractor {
    fun extract(project: Project): UnitTestData
}

@Singleton
internal class DefaultAndroidUnitTestDataExtractor @Inject constructor(
    private val dependenciesDataSource: DependenciesDataSource,
    private val variantDataSource: AndroidVariantDataSource,
    private val dependencyGraphProvider: Lazy<ImmutableValueGraph<Project, Configuration>>
) : AndroidUnitTestDataExtractor {
    private val projectDependencyGraph by lazy { dependencyGraphProvider.get() }
    override fun extract(project: Project): UnitTestData {
        val migratableSourceSets = variantDataSource
            .getMigratableUnitTestVariants(project)
            .asSequence()
            .flatMap { it.sourceSets.asSequence() }
            .filterIsInstance<AndroidSourceSet>()

        val srcs = project.unitTestSources(migratableSourceSets).toList()

        val deps = project.getTestBazelModuleTargets(projectDependencyGraph) +
                dependenciesDataSource.collectMavenDeps(project, ConfigurationScope.TEST) +
                project.kotlinParcelizeDeps() +
                BazelDependency.ProjectDependency(project)

        return UnitTestData(
            name = FORMAT_UNIT_TEST_NAME.format(project.name),
            srcs = srcs,
            deps = deps
        )
    }

    private fun Project.unitTestSources(
        sourceSets: Sequence<AndroidSourceSet>,
        sourceSetType: SourceSetType = SourceSetType.JAVA_KOTLIN
    ): Sequence<String> {
        val dirs = sourceSets.flatMap { it.java.srcDirs.asSequence() }
        val dirsKotlin = dirs.map { File(it.path.replace("/java", "/kotlin")) }
        return filterValidPaths(dirs + dirsKotlin, sourceSetType.patterns)
    }
}


internal fun Project.getTestBazelModuleTargets(
    projectDependencyGraph: ImmutableValueGraph<Project, Configuration>
) = projectDependencyGraph.successors(this)
    .map { BazelDependency.ProjectDependency(it) }
    .toList()

