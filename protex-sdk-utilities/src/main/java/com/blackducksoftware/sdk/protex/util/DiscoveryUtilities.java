package com.blackducksoftware.sdk.protex.util;

import com.blackducksoftware.sdk.protex.common.Snippet;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.CodeMatchDiscovery;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.CodeMatchLocation;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.DiscoveryType;

public final class DiscoveryUtilities {

    /**
     * Private constructor to prevent instantiation.
     */
    private DiscoveryUtilities() {
        throw new AssertionError("No " + DiscoveryUtilities.class.getCanonicalName() + " instances for you!");
    }

    /**
     * Determines if two {@linkplain CodeMatchDiscovery code match discoveries} conflict.
     *
     * <p>
     * Two code match discoveries are determined as conflicting if they apply to the same file path and their source
     * locations {@linkplain #overlaps(Snippet, Snippet) overlap}. A full-file source location overlaps with any source
     * location. It is assumed that the discoveries are from the same project, or they would trivially not conflict.
     * </p>
     *
     * @param disc1
     *            a code match discovery
     * @param disc2
     *            another code match discovery (not necessarily different)
     * @return {@code true} if the two code match discoveries conflict, {@code false} if not
     */
    public static boolean conflicts(CodeMatchDiscovery disc1, CodeMatchDiscovery disc2) {
        // different file paths => no conflict
        if (!disc1.getFilePath().equals(disc2.getFilePath())) {
            return false;
        }
        // either is a full-file match => conflict
        if ((disc1.getDiscoveryType() == DiscoveryType.FILE) || (disc2.getDiscoveryType() == DiscoveryType.FILE)) {
            return true;
        }
        // both are snippet matches => conflict == source file snippets overlap
        return overlaps(disc1.getSourceFileLocation().getSnippet(), disc2.getSourceFileLocation().getSnippet());
    }

    /**
     * Determines if two {@linkplain CodeMatchLocation code match locations} overlap.
     *
     * <p>
     * Two code match locations are determined as overlapping if they apply to the same file path and they
     * {@linkplain #overlaps(Snippet, Snippet) overlap}. A full-file source location overlaps with any source location.
     * </p>
     * <p>
     * If possible, use {@link #conflicts(CodeMatchDiscovery, CodeMatchDiscovery)} in preference to this method, as its
     * implementation is more robust.
     * </p>
     *
     * @param loc1
     *            a code match location
     * @param loc2
     *            another code match location (not necessarily different)
     * @return {@code true} if the two code match locations overlap, {@code false} if not
     */
    public static boolean overlaps(CodeMatchLocation loc1, CodeMatchLocation loc2) {
        // different file paths => no conflict
        if (!loc1.getFilePath().equals(loc2.getFilePath())) {
            return false;
        }
        // either is a full-file match => conflict
        Snippet snippet1 = loc1.getSnippet();
        Snippet snippet2 = loc2.getSnippet();
        if ((snippet1.getLastLine() == 0) || (snippet2.getLastLine() == 0)) { // 0 signals a full-file match
            return true;
        }
        // both are snippet matches => conflict == source file snippets overlap
        return overlaps(snippet1, snippet2);
    }

    /**
     * Determines if two {@linkplain Snippet snippets} overlap.
     *
     * <p>
     * Two snippets are determined as overlapping if their line number ranges overlap <b>by more than a single edge
     * line</b>. This means that, while two snippets with line number ranges of 10-30 and 25-45 overlap, two snippets
     * with line number ranges of 10-30 and 30-50 do not overlap.
     * </p>
     *
     * @param snippet1
     *            a snippet
     * @param snippet2
     *            another snippet (not necessarily different)
     * @return {@code true} if the two snippets overlap, {@code false} if not
     */
    public static boolean overlaps(Snippet snippet1, Snippet snippet2) {
        return (snippet1.getFirstLine() < snippet2.getLastLine()) && (snippet2.getFirstLine() < snippet1.getLastLine());
    }

}
