package com.grab.grazel.migrate.unittest

import com.grab.grazel.bazel.starlark.BazelDependency

data class UnitTestData(
    val name: String,
    val srcs: List<String>,
    val deps: List<BazelDependency>
)

internal fun UnitTestData.toUnitTestTarget(): UnitTestTarget {
    return UnitTestTarget(
        name = name,
        srcs = srcs,
        deps = deps
    )
}