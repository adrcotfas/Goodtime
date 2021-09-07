/*
 * Copyright 2016-2021 Adrian Cotfas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.apps.adrcotfas.goodtime.util

import java.io.*
import kotlin.Throws

object FileUtils {
    @Throws(IOException::class)
    fun copyFile(sourceFile: File, destFile: File) {
        if (!destFile.parentFile.exists()) {
            destFile.parentFile.mkdirs()
        }
        if (!destFile.exists()) {
            destFile.createNewFile()
        }
        FileInputStream(sourceFile).channel.use { source ->
            FileOutputStream(destFile).channel.use { destination ->
                destination.transferFrom(
                    source,
                    0,
                    source.size()
                )
            }
        }
    }

    @Throws(IOException::class)
    fun copy(inStream: InputStream, dst: File) {
        val outStream = FileOutputStream(dst)
        copy(inStream, outStream)
    }

    @Throws(IOException::class)
    private fun copy(stream: InputStream, out: OutputStream) {
        var numBytes: Int
        val buffer = ByteArray(1024)
        while (stream.read(buffer).also { numBytes = it } != -1) {
            out.write(buffer, 0, numBytes)
        }
        out.close()
    }

    @Throws(IOException::class)
    fun isSQLite3File(file: File): Boolean {
        val fis = FileInputStream(file)
        val header = "SQLite format 3".toByteArray()
        val buffer = ByteArray(header.size)
        val count = fis.read(buffer)
        return if (count < header.size) false else buffer.contentEquals(header)
    }
}