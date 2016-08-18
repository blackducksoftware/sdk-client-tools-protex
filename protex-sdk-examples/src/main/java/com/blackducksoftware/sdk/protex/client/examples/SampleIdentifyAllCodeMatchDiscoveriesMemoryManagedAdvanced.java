package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.common.BomRefreshMode;
import com.blackducksoftware.sdk.protex.common.ComponentKey;
import com.blackducksoftware.sdk.protex.common.Snippet;
import com.blackducksoftware.sdk.protex.common.UsageLevel;
import com.blackducksoftware.sdk.protex.license.LicenseInfo;
import com.blackducksoftware.sdk.protex.project.bom.BomApi;
import com.blackducksoftware.sdk.protex.project.bom.BomProgressStatus;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeApi;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNode;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeRequest;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeType;
import com.blackducksoftware.sdk.protex.project.codetree.NodeCount;
import com.blackducksoftware.sdk.protex.project.codetree.NodeCountType;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.CodeMatchDiscovery;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.CodeMatchLocation;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.CodeMatchType;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.DiscoveryApi;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.DiscoveryType;
import com.blackducksoftware.sdk.protex.project.codetree.identification.CodeMatchIdentificationDirective;
import com.blackducksoftware.sdk.protex.project.codetree.identification.CodeMatchIdentificationRequest;
import com.blackducksoftware.sdk.protex.project.codetree.identification.IdentificationApi;
import com.blackducksoftware.sdk.protex.util.CodeTreeUtilities;
import com.blackducksoftware.sdk.protex.util.DiscoveryUtilities;

/**
 * This sample demonstrates how to gradually identify all remaining code match discoveries
 *
 * It demonstrates:
 * - How to generate a generate a full-project code tree containing multiple node counts
 * - How to gather code match discoveries pending identification in batches in a multi-threaded manner
 * - How to strip down and cache discovery information to minimize retained and redundant data
 * - How to identify a discovery to its discovered code match
 * - How to make non-conflicting identifications from a collection of discoveries, following a purposeful pattern such
 *   as minimizing the number of identified components
 * - How to refresh the BOM and retrieve status information while waiting
 */
public class SampleIdentifyAllCodeMatchDiscoveriesMemoryManagedAdvanced extends BDProtexSample {

    private static DiscoveryApi discoveryApi;

    private static CodeTreeApi codeTreeApi;

    private static IdentificationApi identificationApi;

    private static BomApi bomApi;

    private static String projectId;

    private static final List<SlimCodeMatchDiscovery> discoveries = new ArrayList<SlimCodeMatchDiscovery>();

    // The value the thread pool is created with controls the number of discovery fetching threads
    private static final ExecutorService discoveryExecService = Executors.newFixedThreadPool(4);

    private static int countTotal;

    private static AtomicInteger countCurrent;

    // The maximum number of discoveries fetched in one SDK call
    private static final int maximumChildElements = 50000;

    // The maximum number of identification-making threads
    private static final int maxIdentifierThreads = 8;

    private static Stopwatch stopwatch;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleIdentifyAllCodeMatchDiscoveriesMemoryManagedAdvanced.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID", "The ID for the project, i.e. c_test-project"));

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
        projectId = args[3];

        // Maximize rather than minimize the number of identified components. Meant for generating a large BOM for
        // testing purposes.
        final boolean maximizeBom = false;
        // Maximize rather than minimize the number of individual IDs made. If true or if maximizing the BOM, IDs
        // will be made at the file level. Otherwise, a single folder-level ID will be made to identify all matches to
        // a component.
        final boolean maximizeIds = false;
        // If true or if maximizing IDs, make IDs at the deepest/most specific level that contains all matches to
        // identify. Otherwise, make IDs at the root level.
        final boolean maximizeIdSpecificity = true;

        Long connectionTimeout = 240 * 1000L;

        ProtexServerProxy myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

        try {
            // It's possible that a single node contains more than maximumChildElements discoveries which have to be
            // fetched together, and we also have to do other things like fetch the code tree, so let's be generous
            myProtexServer.setMaximumChildElements(Integer.MAX_VALUE);

            discoveryApi = myProtexServer.getDiscoveryApi();
            codeTreeApi = myProtexServer.getCodeTreeApi();
            identificationApi = myProtexServer.getIdentificationApi();
            bomApi = myProtexServer.getBomApi();

            // Fetch every file in the project, along with its discovery and pending code match ID counts

            stopwatch = new Stopwatch("Fetching files");

            CodeTreeNodeRequest codeTreeParameters = new CodeTreeNodeRequest();
            codeTreeParameters.getIncludedNodeTypes().add(CodeTreeNodeType.FILE);
            codeTreeParameters.setDepth(CodeTreeUtilities.INFINITE_DEPTH);
            codeTreeParameters.setIncludeParentNode(false);
            codeTreeParameters.getCounts().addAll(Arrays.asList(NodeCountType.DISCOVERIES, NodeCountType.PENDING_ID_CODE_MATCH));
            List<CodeTreeNode> nodes = codeTreeApi.getCodeTreeNodes(projectId, "/", codeTreeParameters);

            stopwatch.stop();
            println(nodes.size() + " files in project");

            // Sort the files for deterministic processing order

            stopwatch = new Stopwatch("Sorting files");

            Collections.sort(nodes, new Comparator<CodeTreeNode>() {
                @Override
                public int compare(CodeTreeNode n1, CodeTreeNode n2) {
                    return n1.getName().compareTo(n2.getName());
                }
            });

            stopwatch.stop();

            // Fetch every code match discovery for the files pending code match ID

            int discoveryCountTotal = 0;
            for (CodeTreeNode node : nodes) {
                discoveryCountTotal += getNodeCount(node, NodeCountType.DISCOVERIES);
            }
            int[] discoveryCounts = getCodeMatchDiscoveries(nodes, Arrays.asList(CodeMatchType.PRECISION));
            countTotal = discoveryCounts[0];
            int pendingCount = discoveryCounts[1];
            countCurrent = new AtomicInteger();

            stopwatch = new Stopwatch(
                    "Fetching " + countTotal + "/" + discoveryCountTotal + " discoveries for " + pendingCount + "/" + nodes.size() + " files");

            awaitTermination(discoveryExecService, "Fetching discoveries");

            stopwatch.stop();

            if (countTotal > 0) {

                // Sort the discoveries for deterministic processing order

                stopwatch = new Stopwatch("Processing " + discoveries.size() + " code match discoveries");

                Collections.sort(discoveries, new Comparator<SlimCodeMatchDiscovery>() {
                    @Override
                    public int compare(SlimCodeMatchDiscovery d1, SlimCodeMatchDiscovery d2) {
                        int compareComponentIds =
                                d1.getDiscoveredComponentKey().getComponentId().compareToIgnoreCase(d2.getDiscoveredComponentKey().getComponentId());
                        if (compareComponentIds != 0) {
                            return compareComponentIds;
                        }
                        int compareVersionIds =
                                d1.getDiscoveredComponentKey().getVersionId().compareToIgnoreCase(d2.getDiscoveredComponentKey().getVersionId());
                        if (compareVersionIds != 0) {
                            return compareVersionIds;
                        }
                        int compareFilePaths = d1.getSourceFileLocation().getFilePath().compareToIgnoreCase(d2.getSourceFileLocation().getFilePath());
                        if (compareFilePaths != 0) {
                            return compareFilePaths;
                        }
                        int compareFirstLines =
                                d1.getSourceFileLocation().getSnippet().getFirstLine().compareTo(d2.getSourceFileLocation().getSnippet().getFirstLine());
                        if (compareFirstLines != 0) {
                            return compareFirstLines;
                        }
                        throw new AssertionError("Overlapping code match discoveries to the same component version");
                    }
                });

                // Populate various associative maps for processing

                Map<String, Set<SlimCodeMatchDiscovery>> discoveriesByFile = new HashMap<String, Set<SlimCodeMatchDiscovery>>();
                Map<ComponentKey, Set<SlimCodeMatchDiscovery>> discoveriesByKey = new HashMap<ComponentKey, Set<SlimCodeMatchDiscovery>>();
                Map<ComponentKey, Integer> countsByKey = new HashMap<ComponentKey, Integer>();
                TreeMap<Integer, Set<ComponentKey>> keysByCount = new TreeMap<Integer, Set<ComponentKey>>();

                for (SlimCodeMatchDiscovery discovery : discoveries) {
                    put(discoveriesByFile, discovery.getFilePath(), discovery);
                    put(discoveriesByKey, discovery.getDiscoveredComponentKey(), discovery);
                }

                for (Map.Entry<ComponentKey, Set<SlimCodeMatchDiscovery>> discoveriesForKey : discoveriesByKey.entrySet()) {
                    ComponentKey key = discoveriesForKey.getKey();
                    Integer count = discoveriesForKey.getValue().size();
                    countsByKey.put(key, count);
                    put(keysByCount, count, discoveriesForKey.getKey());
                }

                stopwatch.stop();

                // Submit the code match identifications

                // Can only identify in parallel (and thus with no order guarantee) if not making folder-level IDs
                ExecutorService identificationExecService =
                        maximizeBom || maximizeIds ? Executors.newFixedThreadPool(maxIdentifierThreads) : Executors.newSingleThreadExecutor();
                countCurrent = new AtomicInteger();
                countTotal = 0;

                stopwatch = new Stopwatch("Queueing identifications for " + pendingCount + " files");

                while (!keysByCount.isEmpty()) {
                    Map.Entry<Integer, Set<ComponentKey>> keysByPendingCountNextEntry = maximizeBom ? keysByCount.firstEntry() : keysByCount.lastEntry();
                    Set<ComponentKey> keysByPendingCountNext = keysByPendingCountNextEntry.getValue();

                    if (keysByPendingCountNext.isEmpty()) {
                        keysByCount.remove(keysByPendingCountNextEntry.getKey());

                    } else {
                        ComponentKey keyToIdentify = keysByPendingCountNext.iterator().next();

                        Set<SlimCodeMatchDiscovery> discoveriesToIdentify = discoveriesByKey.get(keyToIdentify);
                        int discoveryCount = discoveriesToIdentify.size();
                        Iterator<SlimCodeMatchDiscovery> discoveriesToIdentifyIterator = discoveriesToIdentify.iterator();
                        SlimCodeMatchDiscovery discoveryToIdentify, exactDiscoveryToIdentify = null;
                        String idPath;

                        if (maximizeIdSpecificity) {
                            String[] paths = new String[discoveryCount];
                            int i = 0;
                            for (SlimCodeMatchDiscovery discovery : discoveriesToIdentify) {
                                paths[i++] = discovery.getFilePath();
                            }
                            idPath = CodeTreeUtilities.longestCommonPath(paths);
                        } else {
                            idPath = "";
                        }

                        do {
                            discoveryToIdentify = discoveriesToIdentifyIterator.next();
                            discoveriesToIdentifyIterator.remove();

                            if (!discoveriesToIdentifyIterator.hasNext()) {
                                discoveriesByKey.remove(keyToIdentify);
                                countsByKey.remove(keyToIdentify);
                                keysByPendingCountNext.remove(keyToIdentify);
                            }

                            if (discoveryToIdentify.isExact()) {
                                exactDiscoveryToIdentify = discoveryToIdentify;
                            }

                            String file = discoveryToIdentify.getFilePath();
                            Iterator<SlimCodeMatchDiscovery> discoveriesForFileIterator = discoveriesByFile.get(file).iterator();

                            while (discoveriesForFileIterator.hasNext()) {
                                SlimCodeMatchDiscovery discovery = discoveriesForFileIterator.next();
                                ComponentKey key = discovery.getDiscoveredComponentKey();

                                if (discovery == discoveryToIdentify) {
                                    discoveriesForFileIterator.remove();

                                    if (maximizeBom || maximizeIds) {
                                        CodeMatchIdentificationRequest identificationRequest = acceptDiscoveryAsIdentification(discoveryToIdentify);
                                        identificationExecService.submit(new CodeMatchIdentificationAdder(file, identificationRequest, 1));
                                        countTotal++;
                                    }

                                } else if (DiscoveryUtilities.overlaps(discovery.getSourceFileLocation(), discoveryToIdentify.getSourceFileLocation())) {
                                    discoveriesForFileIterator.remove();

                                    int oldCountForKey;
                                    Set<SlimCodeMatchDiscovery> discoveriesForKey = discoveriesByKey.get(key);
                                    discoveriesForKey.remove(discovery);

                                    if (discoveriesForKey.isEmpty()) {
                                        discoveriesByKey.remove(key);
                                        oldCountForKey = countsByKey.remove(key);

                                    } else {
                                        oldCountForKey = countsByKey.get(key);
                                        int newCountForKey = oldCountForKey - 1;
                                        countsByKey.put(key, newCountForKey);
                                        put(keysByCount, newCountForKey, key);
                                    }

                                    keysByCount.get(oldCountForKey).remove(key);
                                }
                            }

                            discoveriesToIdentifyIterator = discoveriesByKey.containsKey(keyToIdentify) ? discoveriesByKey.get(keyToIdentify).iterator() : null;
                        } while (!maximizeBom && (discoveriesToIdentifyIterator != null) && discoveriesToIdentifyIterator.hasNext());

                        if (!maximizeBom && !maximizeIds) {
                            CodeMatchIdentificationRequest identificationRequest =
                                    acceptDiscoveryAsIdentification(exactDiscoveryToIdentify != null ? exactDiscoveryToIdentify : discoveryToIdentify);
                            identificationExecService.submit(new CodeMatchIdentificationAdder(idPath, identificationRequest, discoveryCount));
                            countTotal++;
                        }
                    }
                }

                stopwatch.stop();

                // Wait for all of the submitted identifications to complete

                stopwatch = new Stopwatch("Adding " + countTotal + " identifications");

                awaitTermination(identificationExecService, "Identifying");

                stopwatch.stop();

                // Refresh the project's BOM

                stopwatch = new Stopwatch("Refreshing BOM");

                bomApi.refreshBom(projectId, true, true);

                BomProgressStatus status;
                do {
                    Thread.sleep(2000);
                    status = bomApi.getRefreshBomProgress(projectId);
                    printStatus(status.getRefreshStage(), status.getPercentComplete(), status.getCurrentPath(), true);
                } while (!(((Integer) 100).equals(status.getPercentComplete()) && "idle".equalsIgnoreCase(status.getRefreshStage())));

                clearStatus();

                stopwatch.stop();
            }

            println("Complete.");

        } finally {
            myProtexServer.close();
        }
    }

    // ### LOGGING UTILITY METHODS ###

    private static final int maxStatusLength = 80;

    private static final boolean useBackspace = false;

    private static String status = "";

    private static String statusBackspace = "";

    private static final char[] backspaceChars;

    static {
        if (useBackspace) {
            backspaceChars = new char[maxStatusLength];
            Arrays.fill(backspaceChars, '\b');
        } else {
            backspaceChars = null;
        }
    }

    private static void clearStatus() {
        synchronized (System.out) {
            System.out.print(statusBackspace);
            status = "";
            statusBackspace = "";
        }
    }

    private static void printStatus(String stage, int percent) {
        printStatus(stage, percent, null, false);
    }

    private static void printStatus(String stage, int percent, String message, boolean truncateMessageLeft) {
        StringBuilder statusBuilder = new StringBuilder();
        statusBuilder.append(String.format("%3d", percent));
        statusBuilder.append("% ");
        statusBuilder.append(stage);
        if (message != null && !message.isEmpty()) {
            statusBuilder.append(" - ");
            if (statusBuilder.length() <= maxStatusLength) {
                statusBuilder.append(message);
            } else {
                if (truncateMessageLeft) {
                    statusBuilder.append("...");
                    statusBuilder.append(message, 0, maxStatusLength - statusBuilder.length());
                } else {
                    statusBuilder.append(message, 0, maxStatusLength - statusBuilder.length());
                    statusBuilder.append("...");
                }
            }
        }

        String newStatus = statusBuilder.toString();
        String newStatusBackspace = useBackspace ? new String(backspaceChars, 0, newStatus.length()) : "\n";

        synchronized (System.out) {
            System.out.printf("%s%s", statusBackspace, newStatus);
            status = newStatus;
            statusBackspace = newStatusBackspace;
        }
    }

    private static void println(String x) {
        synchronized (System.out) {
            System.out.printf("%s%s\n%s", statusBackspace, x, status);
        }
    }

    private static void printf(String format, Object... args) {
        Object[] args2 = new Object[args.length + 2];
        args2[0] = statusBackspace;
        System.arraycopy(args, 0, args2, 1, args.length);
        args2[args2.length - 1] = status;

        synchronized (System.out) {
            System.out.printf("%s" + format + "%s", args2);
        }
    }

    // ### LOGGING UTILITY CLASSES ###

    private static class Stopwatch {

        private final String action;

        private final long nanoTime;

        public Stopwatch(String action) {
            println(action + "...");
            this.action = action;
            nanoTime = System.nanoTime();
        }

        public void stop() {
            final double elapsed = (System.nanoTime() - nanoTime) / 1e9;
            printf("%s completed in %.2fs.\n", action, elapsed);
        }

    }

    // ### OPERATIONAL UTILITY METHODS ###

    private static void awaitTermination(ExecutorService execService, String stage) {
        execService.shutdown();

        boolean done = false;
        do {
            try {
                done = execService.awaitTermination(useBackspace ? 200 : 2000, TimeUnit.MILLISECONDS);
            } catch (@SuppressWarnings("unused") InterruptedException e) {
            }
            printStatus(stage, countTotal != 0 ? countCurrent.get() * 100 / countTotal : 0);
        } while (!done);

        clearStatus();
    }

    private static int[] getCodeMatchDiscoveries(List<CodeTreeNode> partialCodeTree, List<CodeMatchType> filterByCodeMatchType) {
        int totalDiscoveryCount = 0;
        int totalFileCount = 0;
        List<CodeTreeNode> getDiscoveriesFor = new ArrayList<CodeTreeNode>();
        int partialDiscoveryCount = 0;

        for (CodeTreeNode node : partialCodeTree) {
            if (getNodeCount(node, NodeCountType.PENDING_ID_CODE_MATCH) != 0) {
                int nodeDiscoveryCount = getNodeCount(node, NodeCountType.DISCOVERIES).intValue();

                if ((partialDiscoveryCount + nodeDiscoveryCount > maximumChildElements) && (partialDiscoveryCount != 0)) {
                    discoveryExecService.submit(new SlimCodeMatchDiscoveryGetter(discoveries, getDiscoveriesFor, filterByCodeMatchType));
                    getDiscoveriesFor = new ArrayList<CodeTreeNode>();
                    partialDiscoveryCount = 0;
                }

                getDiscoveriesFor.add(node);
                partialDiscoveryCount += nodeDiscoveryCount;
                totalDiscoveryCount += nodeDiscoveryCount;
                totalFileCount++;
            }
        }

        if (!getDiscoveriesFor.isEmpty()) {
            discoveryExecService.submit(new SlimCodeMatchDiscoveryGetter(discoveries, getDiscoveriesFor, filterByCodeMatchType));
        }

        return new int[] { totalDiscoveryCount, totalFileCount };
    }

    private static Long getNodeCount(CodeTreeNode node, NodeCountType type) {
        for (NodeCount count : node.getNodeCounts()) {
            if (count.getCountType() == type) {
                return count.getCount();
            }
        }
        throw new IllegalArgumentException("Node \"" + node.getName() + "\" doesn't have a " + type.toString() + " count");
    }

    private static CodeMatchIdentificationRequest acceptDiscoveryAsIdentification(SlimCodeMatchDiscovery discovery) {
        CodeMatchIdentificationRequest identificationRequest = new CodeMatchIdentificationRequest();
        identificationRequest.setDiscoveredComponentKey(discovery.getDiscoveredComponentKey());
        identificationRequest.setIdentifiedComponentKey(discovery.getDiscoveredComponentKey());
        identificationRequest.setCodeMatchIdentificationDirective(CodeMatchIdentificationDirective.SNIPPET_AND_FILE);
        identificationRequest.setIdentifiedUsageLevel(discovery.isExact() ? UsageLevel.COMPONENT : UsageLevel.SNIPPET);
        identificationRequest.setIdentifiedLicenseInfo(discovery.getMatchingLicenseInfo());
        return identificationRequest;
    }

    private static <K, V> void put(Map<K, Set<V>> map, K key, V value) {
        Set<V> set = map.get(key);
        if (set != null) {
            set.add(value);
        } else {
            set = new LinkedHashSet<V>();
            set.add(value);
            map.put(key, set);
        }
    }

    // ### OPERATIONAL UTILITY CLASSES ###

    private static class SlimCodeMatchDiscoveryGetter implements Runnable {

        private final Collection<SlimCodeMatchDiscovery> accumulator;

        private final List<CodeTreeNode> partialCodeTree;

        private final List<CodeMatchType> filterByCodeMatchType;

        public SlimCodeMatchDiscoveryGetter(Collection<SlimCodeMatchDiscovery> accumulator, List<CodeTreeNode> partialCodeTree,
                List<CodeMatchType> filterByCodeMatchType) {
            this.accumulator = accumulator;
            this.partialCodeTree = partialCodeTree;
            this.filterByCodeMatchType = filterByCodeMatchType;
        }

        @Override
        public void run() {
            final Iterable<CodeMatchDiscovery> discoveries;

            try {
                discoveries = discoveryApi.getCodeMatchDiscoveries(projectId, partialCodeTree, filterByCodeMatchType);
            } catch (SdkFault e) {
                throw new RuntimeException(e);
            }

            synchronized (accumulator) {
                for (CodeMatchDiscovery discovery : discoveries) {
                    accumulator.add(new SlimCodeMatchDiscovery(discovery));
                    countCurrent.incrementAndGet();
                }
            }

        }

    }

    private static class CodeMatchIdentificationAdder implements Runnable {

        private final String file;

        private final CodeMatchIdentificationRequest identificationRequest;

        public CodeMatchIdentificationAdder(String file, CodeMatchIdentificationRequest identificationRequest, int discoveryCount) {
            this.file = file;
            this.identificationRequest = identificationRequest;
            println("Queued an identification for " + discoveryCount + " discover" + (discoveryCount != 1 ? "ies" : "y") + " to "
                    + identificationRequest.getIdentifiedComponentKey().getComponentId() + "#"
                    + identificationRequest.getIdentifiedComponentKey().getVersionId() + " on path \"" + file + "\"");
        }

        @Override
        public void run() {
            try {
                identificationApi.addCodeMatchIdentification(projectId, file, identificationRequest, BomRefreshMode.SKIP);
            } catch (SdkFault e) {
                throw new RuntimeException(e);
            }

            countCurrent.incrementAndGet();
        }

    }

    // ### MEMORY-CONSCIOUS CLASSES ###

    public static class SlimCodeMatchDiscovery {

        private CodeMatchLocation sourceFileLocation;

        private ComponentKey discoveredComponentKey;

        private LicenseInfo matchingLicenseInfo;

        private boolean isExact;

        public SlimCodeMatchDiscovery(CodeMatchDiscovery discovery) {
            setSourceFileLocation(discovery.getSourceFileLocation());
            setDiscoveredComponentKey(discovery.getDiscoveredComponentKey());
            setMatchingLicenseInfo(discovery.getMatchingLicenseInfo());
            setExact(discovery.getDiscoveryType() == DiscoveryType.FILE);
        }

        public String getFilePath() {
            return getSourceFileLocation().getFilePath();
        }

        public void setFilePath(String filePath) {
            CodeMatchLocation sourceFileLocation = new CodeMatchLocation();
            sourceFileLocation.setFilePath(filePath);
            sourceFileLocation.setSnippet(sourceFileLocation.getSnippet());
            setSourceFileLocation(sourceFileLocation);
        }

        public CodeMatchLocation getSourceFileLocation() {
            return sourceFileLocation;
        }

        public void setSourceFileLocation(CodeMatchLocation sourceFileLocation) {
            this.sourceFileLocation = InternedCodeMatchLocation.valueOf(sourceFileLocation);
        }

        public ComponentKey getDiscoveredComponentKey() {
            return discoveredComponentKey;
        }

        public void setDiscoveredComponentKey(ComponentKey discoveredComponentKey) {
            this.discoveredComponentKey = InternedComponentKey.valueOf(discoveredComponentKey);
        }

        public LicenseInfo getMatchingLicenseInfo() {
            return matchingLicenseInfo;
        }

        public void setMatchingLicenseInfo(LicenseInfo matchingLicenseInfo) {
            this.matchingLicenseInfo = InternedLicenseInfo.valueOf(matchingLicenseInfo);
        }

        public boolean isExact() {
            return isExact;
        }

        public void setExact(boolean isExact) {
            this.isExact = isExact;
        }

        public DiscoveryType getDiscoveryType() {
            return isExact ? DiscoveryType.FILE : DiscoveryType.SNIPPET;
        }

    }

    public static class InternCache<T> {

        public static final InternCache<Integer> INTEGERS = new InternCache<Integer>();

        private final WeakHashMap<T, T> cache = new WeakHashMap<T, T>();

        public synchronized T intern(T obj) {
            T cachedObj = cache.get(obj);
            if (cachedObj != null) {
                return cachedObj;
            } else {
                cache.put(obj, obj);
                return obj;
            }
        }

    }

    public static class InternedSnippet extends Snippet {

        protected static final InternCache<InternedSnippet> SNIPPETS = new InternCache<InternedSnippet>();

        public static InternedSnippet valueOf(Snippet snippet) {
            if (snippet instanceof InternedSnippet) {
                return (InternedSnippet) snippet;
            }
            final InternedSnippet internedSnippet = new InternedSnippet(snippet);
            return SNIPPETS.intern(internedSnippet);
        }

        private InternedSnippet(Snippet snippet) {
            super.setFirstLine(InternCache.INTEGERS.intern(snippet.getFirstLine()));
            super.setLastLine(InternCache.INTEGERS.intern(snippet.getLastLine()));
        }

        @Override
        public final void setFirstLine(Integer value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public final void setLastLine(Integer value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((firstLine == null) ? 0 : firstLine.hashCode());
            result = prime * result + ((lastLine == null) ? 0 : lastLine.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof InternedSnippet)) {
                return false;
            }
            InternedSnippet other = (InternedSnippet) obj;
            if (firstLine == null) {
                if (other.firstLine != null) {
                    return false;
                }
            } else if (!firstLine.equals(other.firstLine)) {
                return false;
            }
            if (lastLine == null) {
                if (other.lastLine != null) {
                    return false;
                }
            } else if (!lastLine.equals(other.lastLine)) {
                return false;
            }
            return true;
        }

    }

    public static class InternedCodeMatchLocation extends CodeMatchLocation {

        protected static final InternCache<InternedCodeMatchLocation> CODE_MATCH_LOCATIONS = new InternCache<InternedCodeMatchLocation>();

        public static InternedCodeMatchLocation valueOf(CodeMatchLocation codeMatchLocation) {
            if (codeMatchLocation instanceof InternedCodeMatchLocation) {
                return (InternedCodeMatchLocation) codeMatchLocation;
            }
            final InternedCodeMatchLocation internedCodeMatchLocation = new InternedCodeMatchLocation(codeMatchLocation);
            return CODE_MATCH_LOCATIONS.intern(internedCodeMatchLocation);
        }

        private InternedCodeMatchLocation(CodeMatchLocation codeMatchLocation) {
            super.setFilePath(codeMatchLocation.getFilePath().intern());
            super.setSnippet(InternedSnippet.valueOf(codeMatchLocation.getSnippet()));
        }

        @Override
        public final void setFilePath(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public final void setSnippet(Snippet value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
            result = prime * result + ((snippet == null) ? 0 : snippet.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof InternedCodeMatchLocation)) {
                return false;
            }
            InternedCodeMatchLocation other = (InternedCodeMatchLocation) obj;
            if (filePath == null) {
                if (other.filePath != null) {
                    return false;
                }
            } else if (!filePath.equals(other.filePath)) {
                return false;
            }
            if (snippet == null) {
                if (other.snippet != null) {
                    return false;
                }
            } else if (!snippet.equals(other.snippet)) {
                return false;
            }
            return true;
        }

    }

    public static class InternedComponentKey extends ComponentKey {

        protected static final InternCache<InternedComponentKey> COMPONENT_KEYS = new InternCache<InternedComponentKey>();

        public static InternedComponentKey valueOf(ComponentKey componentKey) {
            if (componentKey instanceof InternedComponentKey) {
                return (InternedComponentKey) componentKey;
            }
            final InternedComponentKey internedComponentKey = new InternedComponentKey(componentKey);
            return COMPONENT_KEYS.intern(internedComponentKey);
        }

        private InternedComponentKey(ComponentKey key) {
            super.setComponentId(key.getComponentId().intern());
            super.setVersionId(key.getVersionId().intern());
        }

        @Override
        public final void setComponentId(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public final void setVersionId(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((componentId == null) ? 0 : componentId.hashCode());
            result = prime * result + ((versionId == null) ? 0 : versionId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof InternedComponentKey)) {
                return false;
            }
            InternedComponentKey other = (InternedComponentKey) obj;
            if (componentId == null) {
                if (other.componentId != null) {
                    return false;
                }
            } else if (!componentId.equals(other.componentId)) {
                return false;
            }
            if (versionId == null) {
                if (other.versionId != null) {
                    return false;
                }
            } else if (!versionId.equals(other.versionId)) {
                return false;
            }
            return true;
        }

    }

    public static class InternedLicenseInfo extends LicenseInfo {

        protected static final InternCache<InternedLicenseInfo> LICENSE_INFOS = new InternCache<InternedLicenseInfo>();

        public static InternedLicenseInfo valueOf(LicenseInfo licenseInfo) {
            if (licenseInfo instanceof InternedLicenseInfo) {
                return (InternedLicenseInfo) licenseInfo;
            }
            final InternedLicenseInfo internedLicenseInfo = new InternedLicenseInfo(licenseInfo);
            return LICENSE_INFOS.intern(internedLicenseInfo);
        }

        private InternedLicenseInfo(LicenseInfo licenseInfo) {
            super.setLicenseId(licenseInfo.getLicenseId().intern());
            super.setName(licenseInfo.getName().intern());
        }

        @Override
        public final void setLicenseId(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public final void setName(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((licenseId == null) ? 0 : licenseId.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof InternedLicenseInfo)) {
                return false;
            }
            InternedLicenseInfo other = (InternedLicenseInfo) obj;
            if (licenseId == null) {
                if (other.licenseId != null) {
                    return false;
                }
            } else if (!licenseId.equals(other.licenseId)) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            return true;
        }

    }

}
