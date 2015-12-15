/*
 * Copyright (C) 2011 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.sdk.protex.client.examples.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.examples.BDProtexSample;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.project.ProjectApi;

/**
 * This assigns a list of userIDs to a list of projects (and removes them first
 * so that it can be run multiple times to measure the runtime)
 * 
 * It demonstrates:
 * - How to create SDK client programs that run multi threaded with a configurable thread pool
 * - How to assign a user to a project
 * - How to remove a user from a project - How to handle errors in the multi threaded
 * environment
 * - How to speed up tasks that would take quite long if executed sequentially (See measurements below and discussion)
 * 
 *         Results for the parameters: http://protex.example.com
 *         superuser@example.com password
 *         c_test-1,c_test-2,c_test-3,c_test-4,c_test
 *         -5,c_test-6,c_test-7,c_test-8,c_test-9,c_test-10
 *         test-1@example.com,test-2@example.com,test-3@example.com,test-4@example.com,test-5@example.com,test-6@example
 *         .com,test-7@example.com,test-8@example.com,test-9@example.com,test-10@example.com:
 * 
 *         Clear time (1 threads): 9279 (100 calls :: 0)
 *         Assign time (1 threads): 4531 (100 calls :: 0)
 * 
 *         Clear time (5 threads): 3275 (100 calls :: 0)
 *         Assign time (5 threads): 1943 (100 calls :: 0)
 * 
 *         Clear time (10 threads): 3987 (100 calls :: 0)
 *         Assign time (10 threads): 2410 (100 calls :: 0)
 * 
 *         Clear time (20 threads): 19124 (100 calls :: 0)
 *         Assign time (20 threads): 6006 (100 calls :: 0)
 * 
 *         As we see an increasing thread pool size does only improve run time
 *         within reason (this is run against a Protex instance which is below
 *         our recommended minimum hardware, take the numbers with a grain of
 *         salt)
 */
public class SampleAssignProjectUsersNToM extends BDProtexSample {
    private static final int POOL_SIZE = 5;

    private static String serverUri = null;

    private static String username = null;

    private static String password = null;

    private long assignUsers(String[] projectIds, String[] userIds) {
        ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE);
        List<Future<SdkFault>> list = new ArrayList<Future<SdkFault>>();
        for (String projectId : projectIds) {
            for (String userId : userIds) {
                Callable<SdkFault> worker = new AssignProjectUser(projectId,
                        userId);
                System.out.println("Assigning: " + userId + " ==> " + projectId);
                Future<SdkFault> submit = executor.submit(worker);
                list.add(submit);
            }
        }
        // Now retrieve the result
        long errCount = 0;
        for (Future<SdkFault> future : list) {
            try {
                SdkFault fault = future.get();
                if (fault != null) {
                    errCount++;
                    System.err.println(fault);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
        return errCount;
    }

    private long clearProjects(String[] projectIds, String[] userIds) {
        ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE);
        List<Future<SdkFault>> list = new ArrayList<Future<SdkFault>>();
        for (String projectId : projectIds) {
            for (String userId : userIds) {
                Callable<SdkFault> worker = new RemoveProjectUser(projectId,
                        userId);
                System.out.println("removing: " + userId + " ==> " + projectId);
                Future<SdkFault> submit = executor.submit(worker);
                list.add(submit);
            }
        }
        // Now retrieve the result
        long errCount = 0;
        for (Future<SdkFault> future : list) {
            try {
                SdkFault fault = future.get();
                if (fault != null) {
                    errCount++;
                    System.err.println(fault);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
        return errCount;
    }

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleAssignProjectUsersNToM.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<projectID-1>,<projectID-2>,...");
        parameters.add("<userID-1>,<userID-2>,...");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("<projectID-1>,<projectID-2>,...", "A comma separated list of project IDs (no white space)"));
        paramDescriptions.add(formatUsageDetail("<userID-1>,<userID-2>,...", "A comma separated list of User IDs (no white space)"));

        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 5) {
            System.err.println("Not enough parameters!");
            usage();
            System.exit(-1);
        }
        serverUri = args[0];
        username = args[1];
        password = args[2];
        String projectIdsArg = args[3];
        String userIdsArg = args[4];

        String[] projectIds = projectIdsArg.split(",");
        String[] userIds = userIdsArg.split(",");

        SampleAssignProjectUsersNToM myInstance = new SampleAssignProjectUsersNToM();

        long startClear = System.currentTimeMillis();
        System.out.println("===== Start of Clear");
        long errCountClear = myInstance.clearProjects(projectIds, userIds);
        System.out.println("===== End of Clear");
        long start = System.currentTimeMillis();
        System.out.println("===== Start of Assignment");
        long errCount = myInstance.assignUsers(projectIds, userIds);
        System.out.println("===== End of Assignment");

        System.out.println("Clear time (" + POOL_SIZE + " threads): "
                + (System.currentTimeMillis() - startClear) + " ("
                + (projectIds.length * userIds.length) + " calls :: "
                + errCountClear + ")");
        System.out.println("Assign time (" + POOL_SIZE + " threads): "
                + (System.currentTimeMillis() - start) + " ("
                + (projectIds.length * userIds.length) + " calls :: "
                + errCount + ")");

    }

    /*
     * --------------------- Inner classes just to make the sample contained in
     * one file. No need to do this in the real world ;-)
     */
    public final ThreadLocal<ProtexServerProxy> protexServerProxy = new ThreadLocal<ProtexServerProxy>();

    public class AssignProjectUser implements Callable<SdkFault> {

        private String projectId = null;

        private String userId = null;

        public AssignProjectUser(String projectId, String userId) {
            this.projectId = projectId;
            this.userId = userId;
        }

        @Override
        public SdkFault call() throws Exception {
            try {
                ProtexServerProxy proxy = protexServerProxy.get();
                if (protexServerProxy.get() == null) {
                    proxy = new ProtexServerProxy(serverUri, username, password);
                    protexServerProxy.set(proxy);
                }
                ProjectApi projectApi = proxy.getProjectApi();
                projectApi.addProjectUser(projectId, userId);
            } catch (SdkFault e) {
                return e;
            }
            return null;
        }

    }

    public class RemoveProjectUser implements Callable<SdkFault> {

        private String projectId = null;

        private String userId = null;

        public RemoveProjectUser(String projectId, String userId) {
            this.projectId = projectId;
            this.userId = userId;
        }

        @Override
        public SdkFault call() throws Exception {
            try {
                ProtexServerProxy proxy = protexServerProxy.get();
                if (protexServerProxy.get() == null) {
                    proxy = new ProtexServerProxy(serverUri, username,
                            password);
                    protexServerProxy.set(proxy);
                }
                ProjectApi projectApi = proxy.getProjectApi();
                projectApi.removeProjectUser(projectId, userId);
            } catch (SdkFault e) {
                return e;
            }
            return null;
        }

    }

}
