package com.blackducksoftware.sdk.protex.client.examples.test.type;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.apache.cxf.helpers.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.sdk.fault.ErrorCode;
import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.AnalysisStatus;
import com.blackducksoftware.sdk.protex.license.LicenseCategory;
import com.blackducksoftware.sdk.protex.policy.SourceCodeUploadRequest;
import com.blackducksoftware.sdk.protex.project.AnalysisSourceLocation;
import com.blackducksoftware.sdk.protex.project.AnalysisSourceRepository;
import com.blackducksoftware.sdk.protex.project.ProjectRequest;

/**
 * General utilities for use with example tests which require scanned source
 */
public final class TestSources {

    /** Logger reference to output information to the application log files */
    private static final Logger logger = LoggerFactory.getLogger(TestSources.class);

    private static final String SOURCES_PROPERTY = "test.sources";

    private static final String UPLOAD_SOURCES_PROPERTY = "test.sources.allowupload";

    private static final String HIBERNATE_SOURCE_NAME = "com.blackducksoftware.protex.sdk.examples-hibernate-test-source";

    private static final String HIBERNATE_SOURCE_LOCATION = "test-source" + File.separator + "hibernate-test-sources.zip";

    private static AnalysisSourceLocation sourceLocationCached;

    private static boolean sourceLookupAttempted = false;

    /**
     * Prevent instantiation of utility class
     */
    private TestSources() throws InstantiationException {
        throw new InstantiationException("Cannot instantiate instance of utility class '" + getClass().getName() + "'");
    }

    /**
     * Performs a scan of project source files, blocking until the scan is complete
     * 
     * @param server
     *            The server to operate on
     * @param projectId
     *            The ID of the project to scan
     * @param pollDelayMs
     *            The delay in milliseconds between polls of the server to check on scan status
     * @throws SdkFault
     *             If there is an issue while performing the scan
     */
    public static void synchronousSourceScan(ProtexServerProxy server, String projectId, long pollDelayMs) throws SdkFault {
        server.getProjectApi().startAnalysis(projectId, true);

        boolean done = false;

        while (!done) {
            try {
                Thread.sleep(pollDelayMs);
                AnalysisStatus status = server.getProjectApi().getAnalysisStatus(projectId);
                done = status.isFinished();
            } catch (Exception e) {
                done = true;
            }
        }
    }

    /**
     * Generates a source location from test settings for use by SDK example tests
     *
     * @param server
     *            The server to operate on
     * @return The analysis source location of the configured settings
     * @throws SdkFault
     *             If there is an issue communicating with the server
     * @throws UnsupportedOperationException
     *             If test configuration does not allow for scanned-source tests
     */
    public static AnalysisSourceLocation getAnalysisSourceLocation(ProtexServerProxy server) throws SdkFault {
        if (sourceLocationCached == null && !sourceLookupAttempted) {
            sourceLocationCached = getConfiguredSourceLocation(server);

            if (sourceLocationCached == null && getUploadSourcesAllowed()) {
                sourceLocationCached = getHibernateCoreSource(server);
            }

            sourceLookupAttempted = true;
        }

        if (sourceLocationCached == null) {
            throw new UnsupportedOperationException("No sources configured - a source location must be set, or default sources must be allowed");
        }

        return sourceLocationCached;
    }

    /**
     * @return Configured pre-existing source location for tests on the server
     */
    private static String getTestSources() {
        String sourcesLocation = System.getProperty(SOURCES_PROPERTY);

        sourcesLocation = (sourcesLocation != null ? sourcesLocation.trim() : null);

        return sourcesLocation;
    }

    /**
     * @return True if uploading sources for tests is allowed, false otherwise
     */
    private static Boolean getUploadSourcesAllowed() {
        return Boolean.getBoolean(UPLOAD_SOURCES_PROPERTY);
    }

    /**
     * Attempts to find the configured test source location on the target server
     *
     * @param server
     *            The server to operate on
     * @return The analysis source location of the configured location, or null if the configured location is not valid
     * @throws SdkFault
     *             If there is an issue checking for the source location
     */
    private static AnalysisSourceLocation getConfiguredSourceLocation(ProtexServerProxy server) throws SdkFault {
        String configuredLocation = getTestSources();
        AnalysisSourceLocation testLocation = null;

        if (configuredLocation != null && !configuredLocation.trim().isEmpty()) {
            // First, check that the source aren't already there - lets not duplicate the upload
            testLocation = new AnalysisSourceLocation();
            testLocation.setHostname("localhost");
            testLocation.setRepository(AnalysisSourceRepository.REMOTE_SERVER);
            testLocation.setSourcePath(configuredLocation);

            // Create a fake project to test if the sources exist
            if (!isSourceUploaded(server, testLocation)) {
                testLocation = null;
            }
        }

        return testLocation;
    }

    /**
     * Attempts to find or upload the default SDK examples test source
     *
     * @param server
     *            Server to operate on
     * @return The analysis source location of the test source on the server
     * @throws SdkFault
     *             If there is an issue uploading/finding the source
     */
    private static AnalysisSourceLocation getHibernateCoreSource(ProtexServerProxy server) throws SdkFault {
        // First, check that the source aren't already there - lets not duplicate the upload
        AnalysisSourceLocation testLocation = new AnalysisSourceLocation();
        testLocation.setHostname("localhost");
        testLocation.setRepository(AnalysisSourceRepository.REMOTE_SERVER);
        testLocation.setSourcePath(HIBERNATE_SOURCE_NAME);

        if (!isSourceUploaded(server, testLocation)) {
            logger.info("Test sources not yet present - uploading to server");
            File temporaryUnpackLocation = unpackClasspathSource(HIBERNATE_SOURCE_LOCATION, ".zip");

            DataSource unexpandedfileDS = new FileDataSource(temporaryUnpackLocation);

            SourceCodeUploadRequest sourceCodeUploadRequest = new SourceCodeUploadRequest();
            sourceCodeUploadRequest.setSourceName(HIBERNATE_SOURCE_NAME + ".zip");
            sourceCodeUploadRequest.setSourceContent(new DataHandler(unexpandedfileDS));
            testLocation = server.getPolicyApi().uploadSourceArchive(sourceCodeUploadRequest);
        }

        return testLocation;
    }

    /**
     * Checks if the default test source has already been uploaded
     *
     * @param server
     *            The server to check against
     * @param testLocation
     *            The location to check
     * @return True if the source is there, false otherwise
     * @throws SdkFault
     *             If there is an error communicating with the server
     */
    private static boolean isSourceUploaded(ProtexServerProxy server, AnalysisSourceLocation testLocation) throws SdkFault {
        // Create a temporary project to test if the sources exist
        String projectId = null;

        try {
            ProjectRequest projectRequest = new ProjectRequest();
            projectRequest.setAnalysisSourceLocation(testLocation);
            projectRequest.setName("Test Sources Project");

            projectId = server.getProjectApi().createProject(projectRequest, LicenseCategory.OPEN_SOURCE);

            server.getProjectApi().startAnalysis(projectId, true);

            Thread.sleep(1000);
        } catch (SdkFault e) {
            if (e.getFaultInfo().getErrorCode().equals(ErrorCode.INVALID_SOURCE_LOCATION_FOR_SCAN)) {
                return false;
            } else {
                throw e;
            }
        } catch (Exception e) {
            logger.debug("Exception testing source location", e);
            throw new RuntimeException(e);
        } finally {
            if (projectId != null) {
                server.getProjectApi().deleteProject(projectId);
            }
        }

        return true;
    }

    /**
     * Extracts sources from the test jar class path to a temporary location for upload
     *
     * @param classpathLocation
     *            The location relative to the class path root of the file to upload
     * @param extension
     *            The extension of the file being uploaded (used with temporary file)
     * @return File representation of the file extracted from the jar
     */
    private static File unpackClasspathSource(String classpathLocation, String extension) {
        final InputStream stream = TestSources.class.getClassLoader().getResourceAsStream(classpathLocation);

        OutputStream outputStream = null;

        try {
            File outputFile = File.createTempFile("temp", extension);

            try {
                if (!outputFile.exists() && !outputFile.createNewFile()) {
                    throw new IOException("Cannot create temporary file " + outputFile.getName());
                }

                outputStream = new FileOutputStream(outputFile);
                IOUtils.copy(stream, outputStream);

                return outputFile;
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } finally {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error copying test source file", e);
        }
    }
}
