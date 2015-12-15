package com.blackducksoftware.sdk.protex.client.examples.test.type;

import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.role.UserRoleInfo;

/**
 * Base test class for testing Protex SDK samples
 */
public abstract class AbstractSdkSampleTest {

    /** Proxy which can be used by tests to setup required environment for samples */
    private static ProtexServerProxy serverProxy;

    /** The security manager originally used by the system */
    private SecurityManager existingSecurityManager;

    @BeforeSuite
    protected void setupProxy() {
        serverProxy = Tests.createProxy();

        // Validate permissions set as required
        try {
            List<UserRoleInfo> userRoles = serverProxy.getRoleApi().getUserRoles(Tests.getServerUsername());
            List<String> assignedRoles = new ArrayList<String>();

            for (UserRoleInfo userRole : userRoles) {
                assignedRoles.add(userRole.getRoleId());
            }

            for (String role : Tests.getRequiredTestRoles()) {
                Assert.assertTrue(assignedRoles.contains(role), "Test user does not have required role (" + role + ")");
            }

            for (String role : Tests.getDisallowedTestRoles()) {
                Assert.assertFalse(assignedRoles.contains(role), "Test user has role which will alter example output (" + role + ")");
            }
        } catch (SdkFault e) {
            Assert.fail("Error verifying correct role settings for test user", e);
        }
    }

    @AfterSuite(alwaysRun = true)
    protected void cleanupProxy() throws Exception {
        if (serverProxy != null) {
            serverProxy.close();
        }
    }

    @BeforeClass
    protected void setUpSecurityManager() throws Exception {
        existingSecurityManager = System.getSecurityManager();
        System.setSecurityManager(new NoExitSecurityManager());
    }

    @AfterClass
    protected void tearDownSecurityManager() throws Exception {
        System.setSecurityManager(existingSecurityManager);
    }

    public ProtexServerProxy getProxy() {
        return serverProxy;
    }

    /**
     * @return The URL provided by the test arguments for the Protex server
     * @deprecated Use {@link Tests#getServerUrl()} instead
     */
    @Deprecated
    protected String getServerUrl() {
        return Tests.getServerUrl();
    }

    /**
     * @return The user name provided by the test arguments for the Protex server
     * @deprecated Use {@link Tests#getServerUsername()} instead
     */
    @Deprecated
    protected String getServerUsername() {
        return Tests.getServerUsername();
    }

    /**
     * @return The password provided by the test arguments for the Protex server
     * @deprecated Use {@link Tests#getServerPassword()} instead
     */
    @Deprecated
    protected String getServerPassword() {
        return Tests.getServerPassword();
    }

    /**
     * Security manager which allows reaction to failed executions
     */
    private static class NoExitSecurityManager extends SecurityManager {

        @Override
        public void checkPermission(Permission perm) {
            // allow anything.
        }

        @Override
        public void checkPermission(Permission perm, Object context) {
            // allow anything.
        }

        @Override
        public void checkExit(int status) {
            super.checkExit(status);
            if (status != 0) {
                throw new RuntimeException("Exited with status code: " + status);
            }
        }
    }

}
