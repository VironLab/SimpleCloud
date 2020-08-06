/*
 * MIT License
 *
 * Copyright (C) 2020 The SimpleCloud authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package eu.thesimplecloud.launcher.dependency

import eu.thesimplecloud.api.depedency.Dependency
import org.eclipse.aether.artifact.DefaultArtifact
import java.io.File

/**
 * Created by IntelliJ IDEA.
 * Date: 17.07.2020
 * Time: 09:26
 * @author Frederick Baier
 */

fun main() {
    val repo = "https://repo.maven.apache.org/maven2/"
    val dependencies = listOf(
            DefaultArtifact("org.eclipse.aether:aether-impl:1.1.0"),
            DefaultArtifact("org.eclipse.aether:aether-connector-basic:1.1.0"),
            DefaultArtifact("org.eclipse.aether:aether-transport-file:1.1.0"),
            DefaultArtifact("org.apache.maven:maven-aether-provider:3.3.9")
    )
    val results = DependencyLoader.instance.loadDependencies(listOf(repo), dependencies)
    val resultDependencies = results.map { it.artifact }.map { Dependency(it.groupId, it.artifactId, it.version) }
    val file = File("dependencies.txt")
    file.writeText(resultDependencies.joinToString("\n") { "${it.groupId}:${it.artifactId}:${it.version}" })
}