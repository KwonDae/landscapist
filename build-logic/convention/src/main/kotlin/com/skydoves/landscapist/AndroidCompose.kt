/*
 * Designed and developed by 2020-2022 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("UnstableApiUsage")

package com.skydoves.landscapist

import com.android.build.api.dsl.CommonExtension
import java.io.File
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * Configure Compose-specific options
 */
internal fun Project.configureAndroidCompose(
  commonExtension: CommonExtension<*, *, *, *, *>,
) {
  val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

  commonExtension.apply {
    buildFeatures {
      compose = true
    }

    composeOptions {
      kotlinCompilerExtensionVersion = libs.findVersion("androidxComposeCompiler").get().toString()
    }

    kotlinOptions {
      freeCompilerArgs += buildComposeMetricsParameters() + configureComposeStabilityPath()
    }

    packaging {
      resources {
        excludes.add("/META-INF/{AL2.0,LGPL2.1}")
      }
    }

    dependencies {
      val bom = libs.findLibrary("androidx-compose-bom").get()
      add("implementation", platform(bom))
      add("androidTestImplementation", platform(bom))
    }
  }
}

private fun Project.buildComposeMetricsParameters(): List<String> {
  val metricParameters = mutableListOf<String>()
  val enableMetricsProvider = project.providers.gradleProperty("enableComposeCompilerMetrics")
  val relativePath = projectDir.relativeTo(rootDir)
  val buildDir = layout.buildDirectory.get().asFile
  val enableMetrics = (enableMetricsProvider.orNull == "true")
  if (enableMetrics) {
    val metricsFolder = buildDir.resolve("compose-metrics").resolve(relativePath)
    metricParameters.add("-P")
    metricParameters.add(
      "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" + metricsFolder.absolutePath
    )
  }

  val enableReportsProvider = project.providers.gradleProperty("enableComposeCompilerReports")
  val enableReports = (enableReportsProvider.orNull == "true")
  if (enableReports) {
    val reportsFolder = buildDir.resolve("compose-reports").resolve(relativePath)
    metricParameters.add("-P")
    metricParameters.add(
      "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" + reportsFolder.absolutePath
    )
  }
  return metricParameters.toList()
}

private fun Project.configureComposeStabilityPath(): List<String> {
  val metricParameters = mutableListOf<String>()
  val stabilityConfigurationFile = rootDir.resolve("compose_compiler_config.conf")
  metricParameters.add("-P")
  metricParameters.add(
    "plugin:androidx.compose.compiler.plugins.kotlin:stabilityConfigurationPath=" + stabilityConfigurationFile.absolutePath
  )
  return metricParameters.toList()
}
