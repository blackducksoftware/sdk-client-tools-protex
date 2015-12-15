/*
 * Black Duck Software Suite SDK
 * Copyright (C) 2015  Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.blackducksoftware.sdk.protex.client.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This DateSource wraps up the source archive inside a zip archive.
 */
import javax.activation.DataSource;

public class UnExpandedFileDataSource implements DataSource {

    private final File sourceArchive;

    private File zipArchive;

    public UnExpandedFileDataSource(File sourceArchive) {
        this.sourceArchive = sourceArchive;
        createZipArchive();
    }

    @Override
    public String getContentType() {
        return "application/zip";
    }

    @Override
    public InputStream getInputStream() throws IOException {

        byte[] buffer = new byte[1024];
        int len;

        FileOutputStream fos = new FileOutputStream(zipArchive);
        ZipOutputStream zos = new ZipOutputStream(fos);
        FileInputStream fis = new FileInputStream(sourceArchive);
        zos.putNextEntry(new ZipEntry(sourceArchive.getName()));

        while ((len = fis.read(buffer)) > 0) {
            zos.write(buffer, 0, len);
        }

        zos.closeEntry();
        fis.close();
        zos.close();

        return new FileInputStream(zipArchive);
    }

    @Override
    public String getName() {
        return sourceArchive.getName();
    }

    public File getZipArchive() {
        return zipArchive;
    }

    private void createZipArchive() {
        String sourceName = sourceArchive.getName();
        String zipArchiveName = sourceName.substring(0, sourceName.lastIndexOf(".")) + ".zip";
        zipArchive = new File(zipArchiveName);
        zipArchive.deleteOnExit();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new IOException("Read Only DataSource");
    }
}
