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

package eu.thesimplecloud.base.wrapper.process.serviceconfigurator.configurators

import eu.thesimplecloud.api.service.ICloudService
import eu.thesimplecloud.api.utils.FileEditor
import eu.thesimplecloud.base.wrapper.process.serviceconfigurator.IServiceConfigurator
import eu.thesimplecloud.launcher.utils.FileCopier
import java.io.File

class DefaultNukkitConfigurator : IServiceConfigurator {

    override fun configureService(cloudService: ICloudService, serviceTmpDirectory: File) {
        val propertiesFile = File(serviceTmpDirectory, "server.properties")
        val nukkitFile = File(serviceTmpDirectory, "nukkit.yml")
        if (!propertiesFile.exists())
            FileCopier.copyFileOutOfJar(propertiesFile, "/files/server.properties")
        if (!nukkitFile.exists())
            FileCopier.copyFileOutOfJar(nukkitFile, "/files/nukkit.yml")
        val fileEditor = FileEditor(propertiesFile)
        fileEditor["server-ip"] = cloudService.getHost()
        fileEditor["server-port"] = cloudService.getPort().toString()
        fileEditor["max-players"] = cloudService.getMaxPlayers().toString()
        fileEditor.save(propertiesFile)
    }
}