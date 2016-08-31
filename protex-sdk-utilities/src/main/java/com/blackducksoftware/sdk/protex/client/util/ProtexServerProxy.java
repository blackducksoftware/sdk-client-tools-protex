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

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.sdk.fault.ErrorCode;
import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.comparison.FileComparisonApi;
import com.blackducksoftware.sdk.protex.component.ComponentApi;
import com.blackducksoftware.sdk.protex.component.custom.CustomComponentManagementApi;
import com.blackducksoftware.sdk.protex.license.LicenseApi;
import com.blackducksoftware.sdk.protex.obligation.ObligationApi;
import com.blackducksoftware.sdk.protex.policy.PolicyApi;
import com.blackducksoftware.sdk.protex.policy.externalid.ExternalIdApi;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.bom.BomApi;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeApi;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.DiscoveryApi;
import com.blackducksoftware.sdk.protex.project.codetree.identification.IdentificationApi;
import com.blackducksoftware.sdk.protex.project.localcomponent.LocalComponentApi;
import com.blackducksoftware.sdk.protex.project.template.TemplateApi;
import com.blackducksoftware.sdk.protex.report.ReportApi;
import com.blackducksoftware.sdk.protex.role.RoleApi;
import com.blackducksoftware.sdk.protex.synchronization.SynchronizationApi;
import com.blackducksoftware.sdk.protex.user.UserApi;

/**
 * A Proxy class which provides the ability to retrieve references to the service API interfaces
 *
 * <p>
 * Provides a {@link #close()} method to allow for erasing of the stored server password used with the proxy instance
 * </p>
 */
public class ProtexServerProxy implements Closeable {

    /** Logger reference to output information to the application log files */
    private static final Logger logger = LoggerFactory.getLogger(ProtexServerProxy.class);

    /**
     * Property which controls output of "Proxy initialized" messages.
     * Valid values are "true" and "false".
     */
    public static final String PROTEX_SDK_INFO_PROPERTY = "protex.sdk.info";

    /** Timeout value which indicates no timeout should be used */
    public static final long INDEFINITE_TIMEOUT = 0L;

    /**
     * String constant which is a unique base WSDL version identifier for the Protex SDK APIs
     * referenced by this Proxy
     */
    private static final String PROTEX_SDK_VERSION = "7_0";

    /** Input properties for WSDL communication */
    protected Map<String, Object> inProps = null;

    /** Output properties for WSDL communication */
    protected Map<String, Object> outProps = null;

    /** The URL of the server this proxy will communicate with to get service API instances */
    private String serverUrl = null;

    private CallbackHandler callbackHandler = null;

    /** The default timeout when communicating with the server. Defaults to {@link #INDEFINITE_TIMEOUT} */
    private long defaultTimeout = INDEFINITE_TIMEOUT;

    /** The default limit for returned list size */
    private long maximumChildElements = 1000000;

    /** Any cookies to set on the HTTP request */
    private Map<String, List<String>> requestCookies = new HashMap<String, List<String>>();

    /** Any headers to set on the HTTP request */
    private Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();

    /** Internally cached reference to the license API on the server being interfaced with by this proxy */
    private LicenseApi licenseApi = null;

    /** Internally cached reference to the local component API on the server being interfaced with by this proxy */
    private LocalComponentApi localComponentApi = null;

    /** Internally cached reference to the component API on the server being interfaced with by this proxy */
    private ComponentApi componentApi = null;

    /**
     * Internally cached reference to the custom component management API on the server being interfaced with by this
     * proxy
     */
    private CustomComponentManagementApi customComponentManagementApi = null;

    /** Internally cached reference to the obligation API on the server being interfaced with by this proxy */
    private ObligationApi obligationApi = null;

    /** Internally cached reference to the policy API on the server being interfaced with by this proxy */
    private PolicyApi policyApi = null;

    /** Internally cached reference to the external ID API on the server being interfaced with by this proxy */
    private ExternalIdApi externalIdApi = null;

    /** Internally cached reference to the file comparison API on the server being interfaced with by this proxy */
    private FileComparisonApi fileComparisonApi = null;

    /** Internally cached reference to the project API on the server being interfaced with by this proxy */
    private ProjectApi projectApi = null;

    /** Internally cached reference to the project template API on the server being interfaced with by this proxy */
    private TemplateApi templateApi = null;

    /** Internally cached reference to the code tree API on the server being interfaced with by this proxy */
    private CodeTreeApi codeTreeApi = null;

    /** Internally cached reference to the BOM API on the server being interfaced with by this proxy */
    private BomApi bomApi = null;

    /** Internally cached reference to the discovery API on the server being interfaced with by this proxy */
    private DiscoveryApi discoveryApi = null;

    /** Internally cached reference to the identification API on the server being interfaced with by this proxy */
    private IdentificationApi identificationApi = null;

    /** Internally cached reference to the report API on the server being interfaced with by this proxy */
    private ReportApi reportApi = null;

    /** Internally cached reference to the role API on the server being interfaced with by this proxy */
    private RoleApi roleApi = null;

    /** Internally cached reference to the user API on the server being interfaced with by this proxy */
    private UserApi userApi = null;

    /** Internally cached reference to the sync server API on the server being interfaced with by this proxy */
    private SynchronizationApi synchronizationApi = null;

    /**
     * Creates a new proxy to interact with a Protex server via SDK entry points
     *
     * <p>
     * Use per server and per user
     * </p>
     *
     * @param serverUrl
     *            The base URI for the Protex server. Example: http://protex.example.com:80/
     * @param userName
     *            The user name to use to login to the Protex server. Example: test@example.com
     * @param password
     *            The password for the provided user
     * @param serverDefaultTimeout
     *            The default timeout for API connections if one is not specified upon creation
     */
    public ProtexServerProxy(String serverUrl, String userName, String password, long serverDefaultTimeout) {
        this(serverUrl, SimpleCallbackHandler.create(userName, password), serverDefaultTimeout);
    }

    /**
     * Creates a new proxy to interact with a Protex server via SDK entry points
     *
     * <p>
     * Use per server and per user
     * </p>
     *
     * @param serverUrl
     *            The base URI for the Protex server. Example: http://protex.example.com:80/
     * @param callbackHandler
     *            A callback handler which allows retrieval of authentication information. Must support
     *            {@link NameCallback} and {@link PasswordCallback}
     * @param serverDefaultTimeout
     *            The default timeout for API connections if one is not specified upon creation
     */
    public ProtexServerProxy(String serverUrl, CallbackHandler callbackHandler, long serverDefaultTimeout) {
        while ((serverUrl.length() > 0) && serverUrl.endsWith("/")) {
            serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
        }

        Boolean logInfo = Boolean.valueOf(System.getProperty(PROTEX_SDK_INFO_PROPERTY, "true"));

        logger.debug("Server URL: {}", serverUrl);
        logger.debug("WSDL Base Version: {}", PROTEX_SDK_VERSION);

        this.serverUrl = serverUrl;
        this.callbackHandler = callbackHandler;
        defaultTimeout = serverDefaultTimeout;

        if (logInfo) {
            // Support legacy behavior
            logger.info("Proxy initialized - SDK version 7.2");
        } else {
            logger.debug("Proxy initialized - SDK version 7.2");
        }
    }

    /**
     * Creates a new proxy to interact with a Protex server via SDK entry points
     *
     * <p>
     * Uses a default timeout of {@link #INDEFINITE_TIMEOUT}
     * </p>
     *
     * <p>
     * Use per server and per user
     * </p>
     *
     * @param serverUrl
     *            The base URI for the Protex server. Example: http://protex.example.com:80/
     * @param userName
     *            The user name to use to login to the Protex server. Example: test@example.com
     * @param password
     *            The password for the provided user
     */
    public ProtexServerProxy(String serverUrl, String userName, String password) {
        this(serverUrl, SimpleCallbackHandler.create(userName, password));
    }

    /**
     * Creates a new proxy to interact with a Protex server via SDK entry points
     *
     * <p>
     * Uses a default timeout of {@link #INDEFINITE_TIMEOUT}
     * </p>
     *
     * <p>
     * Use per server and per user
     * </p>
     *
     * @param serverUrl
     *            The base URI for the Protex server. Example: http://protex.example.com:80/
     * @param callbackHandler
     *            A callback handler which allows retrieval of authentication information. Must support
     *            {@link NameCallback} and {@link PasswordCallback}
     */
    public ProtexServerProxy(String serverUrl, CallbackHandler callbackHandler) {
        this(serverUrl, callbackHandler, INDEFINITE_TIMEOUT);
    }

    /**
     * Gets the default timeout for all API connections produced by get*Api calls that don't specify their own
     *
     * @return The timeout in milliseconds
     */
    public long getDefaultTimeout() {
        return defaultTimeout;
    }

    /**
     * Sets the default timeout for all API connections produced by get*Api calls that don't specify their own
     *
     * @param defaultTimeout
     *            The timeout in milliseconds
     */
    public void setDefaultTimeout(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    /**
     * Gets the cookies to apply to each HTTP request for all fresh API connections produced by get*Api() calls
     *
     * @return Cookies to apply to each HTTP request
     */
    public Map<String, List<String>> getRequestCookies() {
        return requestCookies;
    }

    /**
     * Sets the cookies to apply to each HTTP request for all fresh API connections produced by get*Api() calls
     *
     * @param requestCookies
     *            Cookies to apply to each HTTP request
     */
    public void setRequestCookies(Map<String, List<String>> requestCookies) {
        if (requestCookies != null) {
            this.requestCookies = requestCookies;
        } else {
            this.requestCookies.clear();
        }
    }

    /**
     * Gets the custom headers to apply to each HTTP request for all fresh API connections produced by get*Api() calls
     *
     * @return Custom headers to apply to each HTTP request
     */
    public Map<String, List<String>> getRequestHeaders() {
        return requestHeaders;
    }

    /**
     * Sets the custom headers to apply to each HTTP request for all fresh API connections produced by get*Api() calls
     *
     * @param requestHeaders
     *            Custom headers to apply to each HTTP request
     */
    public void setRequestHeaders(Map<String, List<String>> requestHeaders) {
        if (requestHeaders != null) {
            this.requestHeaders = requestHeaders;
        } else {
            this.requestHeaders.clear();
        }
    }

    /**
     * Gets the maximum number of elements allowed in returned lists for all fresh API connections produced by get*Api()
     * calls
     *
     * @return The maximum number of elements allowed in returned lists
     */
    public long getMaximumChildElements() {
        return maximumChildElements;
    }

    /**
     * Sets the maximum number of elements allowed in returned lists for all fresh API connections produced by get*Api()
     * calls
     *
     * @param maximumChildElements
     *            The maximum number of elements allowed in returned lists
     */
    public void setMaximumChildElements(long maximumChildElements) {
        this.maximumChildElements = maximumChildElements;
    }

    /**
     * Validates the configured User Credentials
     *
     * @throws ServerAuthenticationException
     *             If the credentials fail to authenticate with the server for any reason (invalid URL or user
     *             name/password)
     */
    public void validateCredentials() throws ServerAuthenticationException {
        String userName = null;

        try {
            NameCallback nameCallback = new NameCallback("Username: ");
            callbackHandler.handle(new Callback[] { nameCallback });

            userName = nameCallback.getName();
        } catch (Exception e) {
            logger.error("Error during credential validation", e);
        }

        try {
            // call the cheapest method possible on the server, as every call is sent with the credentials (according to
            // WS-I Basic profile compliance)
            getUserApi().getCurrentUserHasServerFileAccess();
        } catch (SdkFault e) {
            if (ErrorCode.INVALID_CREDENTIALS.equals(e.getFaultInfo().getErrorCode())) {
                throw new ServerAuthenticationException("Invalid credentials provided for '" + userName + "'", e);
            }
            // Eat these errors - we just want to know if the user exists and can talk to the server
        } catch (SOAPFaultException e) {
            throw new ServerAuthenticationException("Validating credentials for '" + userName + "' failed", e);
        }
    }

    /**
     * Resets all cached API connections. Any subsequent calls to get*Api() will create a fresh connection to the server
     *
     * <p>
     * This call may be used to attempt to handle {@code TimeoutException} occurrences - call this method and use
     * get*Api() to get a fresh connection
     * </p>
     */
    public void resetApiConnections() {
        licenseApi = null;
        localComponentApi = null;
        componentApi = null;
        customComponentManagementApi = null;
        obligationApi = null;
        policyApi = null;
        externalIdApi = null;
        fileComparisonApi = null;
        projectApi = null;
        templateApi = null;
        codeTreeApi = null;
        bomApi = null;
        discoveryApi = null;
        identificationApi = null;
        reportApi = null;
        roleApi = null;
        userApi = null;
        synchronizationApi = null;
    }

    @Override
    public void close() throws IOException {
        // Null out the password upon destruction
        callbackHandler = null;
    }

    /* === APIs === */

    /**
     * Gets a license API entry point
     *
     * <p>
     * Uses the configured proxy default timeout for server communication
     * </p>
     *
     * @return A license API entry point for SDK calls to the server
     */
    public LicenseApi getLicenseApi() {
        return getLicenseApi(defaultTimeout);
    }

    /**
     * Gets a license API entry point
     *
     * @param timeout
     *            The timeout to use when communicating with the server. Applies only to this instance (does not change
     *            the default timeout)
     * @return A license API entry point for SDK calls to the server
     */
    public LicenseApi getLicenseApi(long timeout) {
        licenseApi = getApiInstance(licenseApi, ProtexApi.LICENSE, timeout);
        return licenseApi;
    }

    /**
     * Gets a component API entry point
     *
     * <p>
     * Uses the configured proxy default timeout for server communication
     * </p>
     *
     * @return A component API entry point for SDK calls to the server
     */
    public ComponentApi getComponentApi() {
        return getComponentApi(defaultTimeout);
    }

    /**
     * Gets a component API entry point
     *
     * @param timeout
     *            The timeout to use when communicating with the server. Applies only to this instance (does not change
     *            the default timeout)
     * @return A component API entry point for SDK calls to the server
     */
    public ComponentApi getComponentApi(long timeout) {
        componentApi = getApiInstance(componentApi, ProtexApi.COMPONENT, timeout);
        return componentApi;
    }

    /**
     * Gets a custom component management API entry point
     *
     * <p>
     * Uses the configured proxy default timeout for server communication
     * </p>
     *
     * @return A custom component management API entry point for SDK calls to the server
     */
    public CustomComponentManagementApi getCustomComponentManagementApi() {
        return getCustomComponentManagementApi(defaultTimeout);
    }

    /**
     * Gets a custom component management API entry point
     *
     * @param timeout
     *            The timeout to use when communicating with the server. Applies only to this instance (does not change
     *            the default timeout)
     * @return A custom component management API entry point for SDK calls to the server
     */
    public CustomComponentManagementApi getCustomComponentManagementApi(long timeout) {
        customComponentManagementApi = getApiInstance(customComponentManagementApi, ProtexApi.CUSTOM_COMPONENT_MANAGEMENT, timeout);
        return customComponentManagementApi;
    }

    /**
     * Gets a local component API entry point
     *
     * <p>
     * Uses the configured proxy default timeout for server communication
     * </p>
     *
     * @return A local component API entry point for SDK calls to the server
     */
    public LocalComponentApi getLocalComponentApi() {
        return getLocalComponentApi(defaultTimeout);
    }

    /**
     * Gets a local component API entry point
     *
     * @param timeout
     *            The timeout to use when communicating with the server. Applies only to this instance (does not change
     *            the default timeout)
     * @return A local component API entry point for SDK calls to the server
     */
    public LocalComponentApi getLocalComponentApi(long timeout) {
        localComponentApi = getApiInstance(localComponentApi, ProtexApi.LOCAL_COMPONENT, timeout);
        return localComponentApi;
    }

    /**
     * Gets an obligation API entry point
     *
     * <p>
     * Uses the configured proxy default timeout for server communication
     * </p>
     *
     * @return An obligation API entry point for SDK calls to the server
     */
    public ObligationApi getObligationApi() {
        return getObligationApi(defaultTimeout);
    }

    /**
     * Gets an obligation API entry point
     *
     * @param timeout
     *            The timeout to use when communicating with the server. Applies only to this instance (does not change
     *            the default timeout)
     * @return An obligation API entry point for SDK calls to the server
     */
    public ObligationApi getObligationApi(long timeout) {
        obligationApi = getApiInstance(obligationApi, ProtexApi.OBLIGATION, timeout);
        return obligationApi;
    }

    /**
     * Gets a policy API entry point
     *
     * <p>
     * Uses the configured proxy default timeout for server communication
     * </p>
     *
     * @return A policy API entry point for SDK calls to the server
     */
    public PolicyApi getPolicyApi() {
        return getPolicyApi(defaultTimeout);
    }

    /**
     * Gets a policy API entry point
     *
     * @param timeout
     *            The timeout to use when communicating with the server. Applies only to this instance (does not change
     *            the default timeout)
     * @return A policy API entry point for SDK calls to the server
     */
    public PolicyApi getPolicyApi(long timeout) {
        policyApi = getApiInstance(policyApi, ProtexApi.POLICY, timeout);
        return policyApi;
    }

    /**
     * Gets an external ID API entry point
     *
     * <p>
     * Uses the configured proxy default timeout for server communication
     * </p>
     *
     * @return An external ID API entry point for SDK calls to the server
     */
    public ExternalIdApi getExternalIdApi() {
        return getExternalIdApi(defaultTimeout);
    }

    /**
     * Gets an external ID API entry point
     *
     * @param timeout
     *            The timeout to use when communicating with the server. Applies only to this instance (does not change
     *            the default timeout)
     * @return An external ID API entry point for SDK calls to the server
     */
    public ExternalIdApi getExternalIdApi(long timeout) {
        externalIdApi = getApiInstance(externalIdApi, ProtexApi.EXTERNAL_ID, timeout);
        return externalIdApi;
    }

    /**
     * Gets a file comparison API entry point
     *
     * <p>
     * Uses the configured proxy default timeout for server communication
     * </p>
     *
     * @return A file comparison API entry point for SDK calls to the server
     */
    public FileComparisonApi getFileComparisonApi() {
        return getFileComparisonApi(defaultTimeout);
    }

    /**
     * Gets a file comparison API entry point
     *
     * @param timeout
     *            The timeout to use when communicating with the server. Applies only to this instance (does not change
     *            the default timeout)
     * @return A file comparison API entry point for SDK calls to the server
     */
    public FileComparisonApi getFileComparisonApi(long timeout) {
        fileComparisonApi = getApiInstance(fileComparisonApi, ProtexApi.FILE_COMPARISON, timeout);
        return fileComparisonApi;
    }

    /**
     * Gets a project API entry point
     *
     * <p>
     * Uses the configured proxy default timeout for server communication
     * </p>
     *
     * @return A project API entry point for SDK calls to the server
     */
    public ProjectApi getProjectApi() {
        return getProjectApi(defaultTimeout);
    }

    /**
     * Gets a project API entry point
     *
     * @param timeout
     *            The timeout to use when communicating with the server. Applies only to this instance (does not change
     *            the default timeout)
     * @return A project API entry point for SDK calls to the server
     */
    public ProjectApi getProjectApi(long timeout) {
        projectApi = getApiInstance(projectApi, ProtexApi.PROJECT, timeout);
        return projectApi;
    }

    /**
     * Gets a template API entry point
     *
     * <p>
     * Uses the configured proxy default timeout for server communication
     * </p>
     *
     * @return A template API entry point for SDK calls to the server
     */
    public TemplateApi getTemplateApi() {
        return getTemplateApi(defaultTimeout);
    }

    /**
     * Gets a template API entry point
     *
     * @param timeout
     *            The timeout to use when communicating with the server. Applies only to this instance (does not change
     *            the default timeout)
     * @return A template API entry point for SDK calls to the server
     */
    public TemplateApi getTemplateApi(long timeout) {
        templateApi = getApiInstance(templateApi, ProtexApi.TEMPLATE, timeout);
        return templateApi;
    }

    /**
     * Gets a code tree API entry point
     *
     * <p>
     * Uses the configured proxy default timeout for server communication
     * </p>
     *
     * @return A code tree API entry point for SDK calls to the server
     */
    public CodeTreeApi getCodeTreeApi() {
        return getCodeTreeApi(defaultTimeout);
    }

    /**
     * Gets a code tree API entry point
     *
     * @param timeout
     *            The timeout to use when communicating with the server. Applies only to this instance (does not change
     *            the default timeout)
     * @return A code tree API entry point for SDK calls to the server
     */
    public CodeTreeApi getCodeTreeApi(long timeout) {
        codeTreeApi = getApiInstance(codeTreeApi, ProtexApi.CODETREE, timeout);
        return codeTreeApi;
    }

    /**
     * Gets a BOM API entry point
     *
     * <p>
     * Uses the configured proxy default timeout for server communication
     * </p>
     *
     * @return A BOM API entry point for SDK calls to the server
     */
    public BomApi getBomApi() {
        return getBomApi(defaultTimeout);
    }

    /**
     * Gets a BOM API entry point
     *
     * @param timeout
     *            The timeout to use when communicating with the server. Applies only to this instance (does not change
     *            the default timeout)
     * @return A BOM API entry point for SDK calls to the server
     */
    public BomApi getBomApi(long timeout) {
        bomApi = getApiInstance(bomApi, ProtexApi.BOM, timeout);
        return bomApi;
    }

    /**
     * Gets a discovery API entry point
     *
     * <p>
     * Uses the configured proxy default timeout for server communication
     * </p>
     *
     * @return A discovery API entry point for SDK calls to the server
     */
    public DiscoveryApi getDiscoveryApi() {
        return getDiscoveryApi(defaultTimeout);
    }

    /**
     * Gets a discovery API entry point
     *
     * @param timeout
     *            The timeout to use when communicating with the server. Applies only to this instance (does not change
     *            the default timeout)
     * @return A discovery API entry point for SDK calls to the server
     */
    public DiscoveryApi getDiscoveryApi(long timeout) {
        discoveryApi = getApiInstance(discoveryApi, ProtexApi.DISCOVERY, timeout);
        return discoveryApi;
    }

    /**
     * Gets an identification API entry point
     *
     * <p>
     * Uses the configured proxy default timeout for server communication
     * </p>
     *
     * @return An identification API entry point for SDK calls to the server
     */
    public IdentificationApi getIdentificationApi() {
        return getIdentificationApi(defaultTimeout);
    }

    /**
     * Gets an identification API entry point
     *
     * @param timeout
     *            The timeout to use when communicating with the server. Applies only to this instance (does not change
     *            the default timeout)
     * @return An identification API entry point for SDK calls to the server
     */
    public IdentificationApi getIdentificationApi(long timeout) {
        identificationApi = getApiInstance(identificationApi, ProtexApi.IDENTIFICATION, timeout);
        return identificationApi;
    }

    /**
     * Gets a report API entry point
     *
     * <p>
     * Uses the configured proxy default timeout for server communication
     * </p>
     *
     * @return A report API entry point for SDK calls to the server
     */
    public ReportApi getReportApi() {
        return getReportApi(defaultTimeout);
    }

    /**
     * Gets a report API entry point
     *
     * @param timeout
     *            The timeout to use when communicating with the server. Applies only to this instance (does not change
     *            the default timeout)
     * @return A report API entry point for SDK calls to the server
     */
    public ReportApi getReportApi(long timeout) {
        reportApi = getApiInstance(reportApi, ProtexApi.REPORT, timeout);
        return reportApi;
    }

    /**
     * Gets a role API entry point
     *
     * <p>
     * Uses the configured proxy default timeout for server communication
     * </p>
     *
     * @return A role API entry point for SDK calls to the server
     */
    public RoleApi getRoleApi() {
        return getRoleApi(defaultTimeout);
    }

    /**
     * Gets a role API entry point
     *
     * @param timeout
     *            The timeout to use when communicating with the server. Applies only to this instance (does not change
     *            the default timeout)
     * @return A role API entry point for SDK calls to the server
     */
    public RoleApi getRoleApi(long timeout) {
        roleApi = getApiInstance(roleApi, ProtexApi.ROLE, timeout);
        return roleApi;
    }

    /**
     * Gets an user API entry point
     *
     * <p>
     * Uses the configured proxy default timeout for server communication
     * </p>
     *
     * @return An user API entry point for SDK calls to the server
     */
    public UserApi getUserApi() {
        return getUserApi(defaultTimeout);
    }

    /**
     * Gets an user API entry point
     *
     * @param timeout
     *            The timeout to use when communicating with the server. Applies only to this instance (does not change
     *            the default timeout)
     * @return An user API entry point for SDK calls to the server
     */
    public UserApi getUserApi(long timeout) {
        userApi = getApiInstance(userApi, ProtexApi.USER, timeout);
        return userApi;
    }

    /**
     * Gets a synchronization API entry point
     *
     * <p>
     * Uses the configured proxy default timeout for server communication
     * </p>
     *
     * @return A synchronization API entry point for SDK calls to the server
     */
    public SynchronizationApi getSynchronizationApi() {
        return getSynchronizationApi(defaultTimeout);
    }

    /**
     * Gets a synchronization API entry point
     *
     * @param timeout
     *            The timeout to use when communicating with the server. Applies only to this instance (does not change
     *            the default timeout)
     * @return A synchronization API entry point for SDK calls to the server
     */
    public SynchronizationApi getSynchronizationApi(long timeout) {
        synchronizationApi = getApiInstance(synchronizationApi, ProtexApi.SYNCHRONIZATION, timeout);
        return synchronizationApi;
    }

    /**
     * Initialize the Authentication Properties, such as user name and password
     *
     * @param userName
     *            The user name to login to the Protex server, for example: test@example.com
     * @param password
     *            The user's password
     * @deprecated Use {@link #initAuthProps(CallbackHandler)} instead
     */
    @Deprecated
    protected void initAuthProps(String userName, char[] password) {
        initAuthProps(SimpleCallbackHandler.create(userName, password));
    }

    /**
     * Initialize the Authentication Properties, such as user name and password
     *
     * @param callbackHandler
     *            Callback which allows retrieval of authentication information
     */
    protected void initAuthProps(CallbackHandler callbackHandler) {
        String userName = null;

        try {
            NameCallback nameCallback = new NameCallback("Username: ");
            callbackHandler.handle(new Callback[] { nameCallback });

            userName = nameCallback.getName();
        } catch (Exception e) {
            logger.error("Error reading username from callback", e);
        }

        if (inProps == null) {
            inProps = new HashMap<String, Object>();
            // instrument service with authentication credentials
            inProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.NO_SECURITY);
        }

        if (outProps == null) {
            outProps = new HashMap<String, Object>();
            outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
            // Specify our username
            outProps.put(WSHandlerConstants.USER, userName);
            // Password type : plain text
            outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
            // Callback used to retrieve password for given user.
            outProps.put(WSHandlerConstants.PW_CALLBACK_REF, new ProgrammedPasswordCallback(callbackHandler));
            outProps.put(WSHandlerConstants.MUST_UNDERSTAND, "false");
        }

        logger.debug("Authenticate with: {}", userName);
    }

    /**
     * Configures properties on the CXF end point like maximum child elements. Clients may override this method to add
     * custom configuration
     *
     * @param cxfEndpoint
     *            The end point to configure properties on
     */
    protected void configureCxfEndpoint(org.apache.cxf.endpoint.Endpoint cxfEndpoint) {
        cxfEndpoint.put("org.apache.cxf.stax.maxChildElements", maximumChildElements);
    }

    /**
     * Instrument the service port object with authentication information and the appropriate handlers
     *
     * @param serviceApi
     *            The service port Object
     * @param userName
     *            The user name to login to the Protex server, for example: test@example.com
     * @param password
     *            The user's password
     * @param timeout
     *            Optional HTTP timeout in milliseconds, if {@link #INDEFINITE_TIMEOUT}, there is no timeout
     * @deprecated use {@link #instrumentService(Object, CallbackHandler, long)} instead
     */
    @Deprecated
    protected void instrumentService(Object serviceApi, String userName, char[] password, long timeout) {
        instrumentService(serviceApi, SimpleCallbackHandler.create(userName, password), timeout);
    }

    /**
     * Instrument the service port object with authentication information and the appropriate handlers
     *
     * @param serviceApi
     *            The service port Object
     * @param callbackHandler
     *            Callback which allows retrieval of authentication information
     * @param timeout
     *            Optional HTTP timeout in milliseconds, if {@link #INDEFINITE_TIMEOUT}, there is no timeout
     */
    protected void instrumentService(Object serviceApi, CallbackHandler callbackHandler, long timeout) throws ServerConnectException {
        logger.debug("Instrument service: {}", serviceApi.toString());

        try {
            initAuthProps(callbackHandler);

            org.apache.cxf.endpoint.Client client = org.apache.cxf.frontend.ClientProxy.getClient(serviceApi);

            client = applyRequestHeaders(client);

            org.apache.cxf.endpoint.Endpoint cxfEndpoint = client.getEndpoint();

            WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
            cxfEndpoint.getOutInterceptors().add(wssOut);

            configureCxfEndpoint(cxfEndpoint);

            // Set timeout
            setTimeout(serviceApi, timeout);

            if (client.getConduit() instanceof HTTPConduit) {
                HTTPConduit http = (HTTPConduit) client.getConduit();
                http = applyCertificates(http);

                HTTPClientPolicy httpClientPolicy = http.getClient();
                httpClientPolicy = applyRequestCookies(httpClientPolicy);
                httpClientPolicy.setAutoRedirect(true);
            }
        } catch (Exception e) {
            throw new ServerConnectException("Connection to server \"" + serverUrl + "\" failed: " + e.getMessage(), e);
        }
    }

    /**
     * Masks the provided string to avoid showing sensitive credential information in logged messages
     *
     * @param password
     *            The string to mask with '*' characters
     * @return A masked string which does not reveal the password used by this proxy
     */
    protected String getPasswordMask(char[] password) {
        char[] mask = new char[password.length];
        Arrays.fill(mask, '*');
        return new String(mask);
    }

    /**
     * Creates a connection the Protex SDK API specified on the server this proxy is configured with
     *
     * @param targetApi
     *            The cached API value for the API to retrieve. May be null
     * @param api
     *            The SDK API type being retrieved
     * @param timeout
     *            The timeout to configure the connection with
     * @return An instance of the API requested
     */
    @SuppressWarnings("unchecked")
    private <T> T getApiInstance(T targetApi, ProtexApi api, long timeout) {
        if (targetApi == null) {
            targetApi = (T) getPortFromUrl(api.getApiClass(), serverUrl + api.getServiceStub());

            instrumentService(targetApi, callbackHandler, timeout);
        } else {
            Long serviceTimeout = getTimeout(targetApi);

            if (timeout != serviceTimeout) {
                setTimeout(targetApi, timeout);
            }
        }

        return targetApi;
    }

    /**
     * Get the current HTTP timeout value for a service port
     *
     * @param serviceApi
     *            The object representing the service API port
     * @return The timeout in milliseconds for the provided API port
     */
    protected long getTimeout(Object serviceApi) {
        org.apache.cxf.endpoint.Client client = org.apache.cxf.frontend.ClientProxy.getClient(serviceApi);
        /* get timeout */
        HTTPConduit http = (HTTPConduit) client.getConduit();

        HTTPClientPolicy httpClientPolicy = http.getClient();
        return httpClientPolicy.getConnectionTimeout();
    }

    /**
     * Set the HTTP timeout value for a service port
     *
     * @param serviceApi
     *            The object representing the service API port
     * @param timeout
     *            The timeout in milliseconds to configure the server API port with
     *
     */
    protected void setTimeout(Object serviceApi, long timeout) {
        logger.debug("Set timeout for service: {} ({}ms)", serviceApi.toString(), timeout);

        org.apache.cxf.endpoint.Client client = org.apache.cxf.frontend.ClientProxy.getClient(serviceApi);
        /* set timeout */
        HTTPConduit http = (HTTPConduit) client.getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout(timeout);
        httpClientPolicy.setReceiveTimeout(timeout);

        http.setClient(httpClientPolicy);
    }

    /**
     * Applies configured cookies values to a client
     *
     * @param httpClientPolicy
     *            HTTP conduit to be used by CXF
     * @return the updated client
     */
    protected HTTPClientPolicy applyRequestCookies(HTTPClientPolicy httpClientPolicy) {
        if (!requestCookies.isEmpty()) {
            StringBuilder cookieBuilder = new StringBuilder();

            for (Entry<String, List<String>> cookie : requestCookies.entrySet()) {
                if (cookie.getKey() != null && cookie.getValue() != null) {
                    for (String cookieValue : cookie.getValue()) {
                        if (cookieBuilder.length() > 0) {
                            cookieBuilder.append("; ");
                        }

                        cookieBuilder.append(cookie.getKey()).append('=').append(cookieValue);
                    }
                }
            }

            httpClientPolicy.setCookie(cookieBuilder.toString());

            logger.debug("Applying custom cookies ({})", requestCookies);
        }

        return httpClientPolicy;
    }

    /**
     * Applies any configured headers for HTTP requests
     *
     * @param client
     *            Client which will handle HTTP communications
     * @return The provided client
     */
    protected org.apache.cxf.endpoint.Client applyRequestHeaders(org.apache.cxf.endpoint.Client client) {
        if (!requestHeaders.isEmpty() && client != null && client.getRequestContext() != null) {
            client.getRequestContext().put(Message.PROTOCOL_HEADERS, requestHeaders);

            logger.debug("Applying custom headers ({})", requestHeaders);
        }

        return client;
    }

    /**
     * If a custom key or trust store is specified, perform required additional setup to CXF HTTP handling to use it
     *
     * @param http
     *            The HTTP conduit being used for communication
     * @return The provided conduit
     * @throws Exception
     *             If there is an error finding or applying key/trust store data
     */
    protected HTTPConduit applyCertificates(HTTPConduit http) throws Exception {
        StoreParameters trustStoreParams = StoreParameters.getTrustStoreParameters();
        StoreParameters keyStoreParams = StoreParameters.getKeyStoreParameters();

        // Only enable for debugging if you are working with a non exact match on the hostname in the SSL Certificate
        // bad security practice for this to be true for production use
        boolean disableCnCheck = false;

        if (disableCnCheck || trustStoreParams.isCustomLocation() || keyStoreParams.isCustomLocation()) {
            TLSClientParameters tlsClientParameters = new TLSClientParameters();

            if (disableCnCheck) {
                tlsClientParameters.setDisableCNCheck(true);

                logger.warn("Disabling CN Host checking - not recommended for production operations!");
            }

            // Trust store setup
            if (trustStoreParams.isCustomLocation()) {
                TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustFactory.init(trustStoreParams.asKeyStore());
                TrustManager[] tm = trustFactory.getTrustManagers();

                tlsClientParameters.setTrustManagers(tm);

                logger.info("Configuring CXF with explicit trust store");
            }

            // Key store setup
            if (keyStoreParams.isCustomLocation()) {
                KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyFactory.init(keyStoreParams.asKeyStore(), keyStoreParams.getStorePassword());
                KeyManager[] km = keyFactory.getKeyManagers();

                tlsClientParameters.setKeyManagers(km);

                logger.info("Configuring CXF with explicit key store");
            }

            http.setTlsClientParameters(tlsClientParameters);
        }

        return http;
    }

    /**
     * Creates a service API port to the given URL
     *
     * @param serviceClass
     *            The class which represents the port object being retrieved
     * @param serviceUrl
     *            The URL which corresponds to the communication point for the port object
     * @return An instance of the port object mapped to the specified class
     */
    @SuppressWarnings("unchecked")
    private static <T> T getPortFromUrl(Class<T> serviceClass, String serviceUrl) {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(serviceClass);
        factory.setAddress(serviceUrl);

        logger.debug("getPortFromUrl: Service Url={}", serviceUrl);

        return (T) factory.create();
    }

    /**
     * Encodes information about the SDK APIs which are offered by a Protex server
     */
    private static enum ProtexApi {

        LICENSE("license", LicenseApi.class),
        COMPONENT("component", ComponentApi.class),
        CUSTOM_COMPONENT_MANAGEMENT("customcomponentmanagement", CustomComponentManagementApi.class),
        LOCAL_COMPONENT("localcomponent", LocalComponentApi.class),
        OBLIGATION("obligation", ObligationApi.class),
        POLICY("policy", PolicyApi.class),
        EXTERNAL_ID("externalid", ExternalIdApi.class),
        FILE_COMPARISON("filecomparison", FileComparisonApi.class),
        PROJECT("project", ProjectApi.class),
        TEMPLATE("template", TemplateApi.class),
        CODETREE("codetree", CodeTreeApi.class),
        BOM("bom", BomApi.class),
        DISCOVERY("discovery", DiscoveryApi.class),
        IDENTIFICATION("identification", IdentificationApi.class),
        REPORT("report", ReportApi.class),
        ROLE("role", RoleApi.class),
        USER("user", UserApi.class),
        SYNCHRONIZATION("synchronization", SynchronizationApi.class), ;

        /** The URL which references the WSDL file on a Protex server for the API */
        private final String serviceStub;

        /** The class which the target URL maps to for the API */
        private final Class<?> apiClass;

        /**
         * @param apiStub
         *            The URL which references the WSDL file on a Protex server for the API
         * @param apiClass
         *            The class which the target URL maps to for the API
         */
        private ProtexApi(String apiStub, Class<?> apiClass) {
            serviceStub = "/protex-sdk/v" + PROTEX_SDK_VERSION + "/" + apiStub;
            this.apiClass = apiClass;
        }

        /**
         * @return The URL which references the WSDL file on a Protex server for the API
         */
        public String getServiceStub() {
            return serviceStub;
        }

        /**
         * @return The class which the target URL maps to for the API
         */
        public Class<?> getApiClass() {
            return apiClass;
        }
    }

    /**
     * Represents a set of store parameters (location, password, and type)
     */
    private static final class StoreParameters {

        private final String storeLocation;

        private final char[] storePassword;

        private final String storeType;

        private StoreParameters(String storeLocation, char[] storePassword, String storeType) {
            this.storeLocation = storeLocation;
            this.storePassword = (storePassword != null ? storePassword : new char[0]);
            this.storeType = (storeType != null ? storeType : "JKS");
        }

        /**
         * @return True if the parameters provided indicate a customized store location, false otherwise
         */
        public boolean isCustomLocation() {
            return storeLocation != null && !storeLocation.trim().isEmpty();
        }

        /**
         * @return The configured password for the store file
         */
        public char[] getStorePassword() {
            return storePassword;
        }

        /**
         * @return The contained store parameters as a KeyStore object. NUll if no custom store location is configured
         * @throws Exception
         *             If there is an error loading the store file
         */
        public KeyStore asKeyStore() throws Exception {
            KeyStore store = null;
            if (isCustomLocation()) {
                store = KeyStore.getInstance(storeType);
                FileInputStream storeFileStream = new FileInputStream(storeLocation);
                store.load(storeFileStream, storePassword);
                storeFileStream.close();
            }

            return store;
        }

        /**
         * @return Java key store parameters read from standard VM arguments
         */
        public static StoreParameters getKeyStoreParameters() {
            String storeLocation = System.getProperty("javax.net.ssl.keyStore");
            String storePassword = System.getProperty("javax.net.ssl.keyStorePassword");
            String storeType = System.getProperty("javax.net.ssl.keyStoreType");

            return new StoreParameters(storeLocation, (storePassword != null ? storePassword.toCharArray() : null), storeType);
        }

        /**
         * @return Java trust store parameters read from standard VM arguments
         */
        public static StoreParameters getTrustStoreParameters() {
            String storeLocation = System.getProperty("javax.net.ssl.trustStore");
            String storePassword = System.getProperty("javax.net.ssl.trustStorePassword");
            String storeType = System.getProperty("javax.net.ssl.trustStoreType");

            return new StoreParameters(storeLocation, (storePassword != null ? storePassword.toCharArray() : null), storeType);
        }
    }

}
