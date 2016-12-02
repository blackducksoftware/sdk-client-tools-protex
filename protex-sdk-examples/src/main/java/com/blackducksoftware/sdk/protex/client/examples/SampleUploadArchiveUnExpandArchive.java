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
package com.blackducksoftware.sdk.protex.client.examples;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.client.util.UnExpandedFileDataSource;
import com.blackducksoftware.sdk.protex.policy.PolicyApi;
import com.blackducksoftware.sdk.protex.policy.SourceCodeUploadRequest;
import com.blackducksoftware.sdk.protex.project.AnalysisSourceLocation;

public class SampleUploadArchiveUnExpandArchive extends BDProtexSample {

    static PolicyApi policyApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleUploadArchiveUnExpandArchive.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<source path>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("source path",
                "The sourceArchive absolute path, i.e. /Users/Documents/test-general.tar. Must end with a file extension"));

        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 4) {
            System.err.println("Not enough parameters!");
            usage();
            System.exit(-1);
        }
        String serverUri = args[0];
        String username = args[1];
        String password = args[2];
        String sourcePath = args[3];

        Long connectionTimeout = 120 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                policyApi = myProtexServer.getPolicyApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            AnalysisSourceLocation uploadedSourceLocation = null;

            try {
                // UnExpandedFileDataSource is used if the uploaded source archive need not to be expanded.
                UnExpandedFileDataSource unexpandedfileDS = new UnExpandedFileDataSource(new File(sourcePath));
                SourceCodeUploadRequest sourceCodeUploadRequest = new SourceCodeUploadRequest();
                sourceCodeUploadRequest.setSourceName(unexpandedfileDS.getZipArchive().getName());
                sourceCodeUploadRequest.setSourceContent(new DataHandler(unexpandedfileDS));
                uploadedSourceLocation = policyApi.uploadSourceArchive(sourceCodeUploadRequest);
            } catch (SdkFault e) {
                System.err.println("uploadSourceArchive() failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (uploadedSourceLocation != null) {
                System.out.println("Server location of the uploaded - unexpanded Source Archive: " +
                        uploadedSourceLocation.getSourcePath());
            }
        } catch (Exception e) {
            System.err.println("SampleUploadArchiveUnExpandArchive failed");
            e.printStackTrace(System.err);
            System.exit(-1);
        } finally {
            // This is optional - it causes the proxy to overwrite the stored password with null characters, increasing
            // security
            if (myProtexServer != null) {
                myProtexServer.close();
            }
        }
    }
}
