/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * In applying the License, ECMWF does not waive the privileges and immunities
 * granted to it by virtue of its status as an inter-governmental organization
 * nor does it submit to any jurisdiction.
 */

package ecmwf.ecpds.master.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A Storage Lifecycle Policy. A policy can only shorten the physical retention of a data file, never extend it beyond
 * the lifetime (expiry) of the related transfers.
 *
 */
public final class StoragePolicy {

    /** The name of the built-in policy applied when no other policy matches. */
    public static final String DEFAULT_NAME = "default";

    /** The default requeue window (7 days). */
    public static final long DEFAULT_REQUEUE_WINDOW_MS = 7L * 24 * 60 * 60 * 1000;

    /** The default delay before dropping replicas (1 hour). */
    public static final long DEFAULT_DROP_REPLICAS_AFTER_MS = 60L * 60 * 1000;

    /** The policy modes. */
    public enum Mode {
        /** The policy is ignored. */
        DISABLED,
        /** The policy is evaluated and reported but nothing is removed. */
        DRY_RUN,
        /** The policy is enforced. */
        ENFORCE
    }

    /** The name. */
    private final String name;

    /** The comment. */
    private final String comment;

    /** The active flag. */
    private final boolean active;

    /** The mode. */
    private final Mode mode;

    /** The order (lowest first). */
    private final int order;

    /** The destination names or wildcard patterns. */
    private final List<String> destinations;

    /** The destination types. */
    private final Set<Integer> destinationTypes;

    /** The transfer group names. */
    private final List<String> transferGroups;

    /** The file name regular expression. */
    private final String filePattern;

    /** Prefix marking a file pattern as a regular expression. */
    public static final String REGEX_PREFIX = "regex:";

    /** The compiled file pattern. */
    private final Pattern compiledFilePattern;

    /** The requeue window in milliseconds. */
    private final long requeueWindowMs;

    /** Restart the window on requeue. */
    private final boolean restartOnRequeue;

    /** Drop the replicas. */
    private final boolean dropReplicas;

    /** The delay before dropping the replicas in milliseconds. */
    private final long dropReplicasAfterMs;

    private final long minRetentionMs;
    private final long graceMs;
    private final long lazyReplicationMs;
    private final int highWatermark;
    private final int lowWatermark;

    /**
     * Instantiates a new storage policy.
     *
     * @param row
     *            the row (keys as returned by the database interface)
     *
     * @throws IllegalArgumentException
     *             if a value is invalid
     */
    public StoragePolicy(final Map<String, String> row) {
        name = value(row, "name");
        comment = value(row, "comment");
        active = flag(row, "active", true);
        mode = parseMode(value(row, "mode"));
        order = (int) number(row, "order", 0);
        destinations = splitList(value(row, "destinations"));
        destinationTypes = new LinkedHashSet<>();
        for (final var type : splitList(value(row, "types"))) {
            try {
                destinationTypes.add(Integer.parseInt(type));
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException("Invalid destination type: " + type);
            }
        }
        transferGroups = splitList(value(row, "groups"));
        filePattern = value(row, "pattern");
        compiledFilePattern = compile(filePattern);
        requeueWindowMs = number(row, "window", DEFAULT_REQUEUE_WINDOW_MS);
        restartOnRequeue = flag(row, "restart", false);
        dropReplicas = flag(row, "drop", false);
        dropReplicasAfterMs = number(row, "dropAfter", DEFAULT_DROP_REPLICAS_AFTER_MS);
        minRetentionMs = number(row, "minRetention", 0);
        graceMs = number(row, "grace", 0);
        lazyReplicationMs = number(row, "lazy", 0);
        final long high = number(row, "high", 0);
        final long low = number(row, "low", 0);
        if (high < 0 || high > 100 || low < 0 || low > 100 || high == 0 && low != 0
                || high != 0 && (low == 0 || low >= high)) {
            throw new IllegalArgumentException("Watermarks must both be zero (disabled), or 0 < low < high <= 100");
        }
        highWatermark = (int) high;
        lowWatermark = (int) low;
        if (requeueWindowMs < 0 || dropReplicasAfterMs < 0 || minRetentionMs < 0 || graceMs < 0
                || lazyReplicationMs < 0) {
            throw new IllegalArgumentException("Durations cannot be negative");
        }
    }

    /**
     * Gets the built-in default policy (the current behaviour with a 7 days requeue window).
     *
     * @return the default policy
     */
    public static StoragePolicy defaultPolicy() {
        return new StoragePolicy(Map.of("name", DEFAULT_NAME, "comment",
                "Built-in policy used when no other policy matches (current behaviour)", "mode", "DISABLED"));
    }

    /**
     * Checks if this is the built-in default policy.
     *
     * @return true, if it is the default policy
     */
    public boolean isDefault() {
        return DEFAULT_NAME.equals(name);
    }

    /**
     * Parses a list of rows.
     *
     * @param rows
     *            the rows
     *
     * @return the policies
     */
    public static List<StoragePolicy> fromRows(final List<Map<String, String>> rows) {
        final List<StoragePolicy> policies = new ArrayList<>();
        for (final var row : rows) {
            policies.add(new StoragePolicy(row));
        }
        return policies;
    }

    /**
     * Describes what is being evaluated.
     *
     * @param destination
     *            the destination name
     * @param destinationType
     *            the destination type
     * @param transferGroup
     *            the transfer group name (may be null)
     * @param fileName
     *            the file name (may be null)
     * @param explicitPolicy
     *            the policy named in the destination option (may be null)
     */
    public record Subject(String destination, int destinationType, String transferGroup, String fileName,
            String explicitPolicy) {
    }

    /**
     * The result of an evaluation.
     *
     * @param policy
     *            the selected policy
     * @param reason
     *            why this policy was selected
     */
    public record Match(StoragePolicy policy, String reason) {
    }

    /**
     * Selects the policy for the subject. A policy explicitly named by the destination option always wins. Otherwise
     * the active, non-disabled policies are examined in order and the first one whose criteria all match is selected.
     * If none matches the built-in default policy is returned.
     *
     * @param policies
     *            the policies (any order)
     * @param subject
     *            the subject
     *
     * @return the match
     */
    public static Match select(final List<StoragePolicy> policies, final Subject subject) {
        final var sorted = new ArrayList<>(policies);
        sorted.sort((a, b) -> a.order != b.order ? Integer.compare(a.order, b.order) : a.name.compareTo(b.name));
        final var explicit = subject.explicitPolicy();
        if (explicit != null && !explicit.isBlank()) {
            for (final var policy : sorted) {
                if (policy.name.equals(explicit.trim())) {
                    return new Match(policy,
                            "Selected by the destination option scheduler.storagePolicy = \"" + policy.name + "\"");
                }
            }
            if (DEFAULT_NAME.equals(explicit.trim())) {
                return new Match(defaultPolicy(), "Explicit expiry-only destination policy");
            }
            throw new IllegalArgumentException("Destination references missing storage policy: " + explicit);
        }
        for (final var policy : sorted) {
            if (policy.active && policy.mode != Mode.DISABLED && policy.matches(subject)) {
                return new Match(policy, "First active policy (by order " + policy.order + ") whose criteria match");
            }
        }
        return new Match(defaultPolicy(), "No policy matches: the built-in default policy applies");
    }

    /**
     * Checks that all the criteria of this policy match the subject (a policy without any criteria matches everything).
     *
     * @param subject
     *            the subject
     *
     * @return true, if all the criteria match
     */
    public boolean matches(final Subject subject) {
        if (!destinations.isEmpty() && destinations.stream().noneMatch(p -> wildcard(p, subject.destination()))) {
            return false;
        }
        if (!destinationTypes.isEmpty() && !destinationTypes.contains(subject.destinationType())) {
            return false;
        }
        if (!transferGroups.isEmpty()
                && (subject.transferGroup() == null || !transferGroups.contains(subject.transferGroup()))) {
            return false;
        }
        return compiledFilePattern == null
                || subject.fileName() != null && compiledFilePattern.matcher(subject.fileName()).matches();
    }

    /**
     * Gets the time until which the original file is guaranteed to be kept for a requeue. A policy can only shorten the
     * retention, never extend it beyond the lifetime.
     *
     * @param windowStart
     *            the start of the window (time of the last activity)
     * @param expiry
     *            the expiry time of the transfer (the lifetime)
     *
     * @return the time until which the requeue is guaranteed
     */
    public long guaranteedUntil(final long windowStart, final long expiry) {
        return Math.min(addDuration(windowStart, requeueWindowMs), expiry);
    }

    public static long addDuration(final long start, final long duration) {
        return duration > Long.MAX_VALUE - start ? Long.MAX_VALUE : start + duration;
    }

    public long getMinRetentionMs() {
        return minRetentionMs;
    }

    public long getGraceMs() {
        return graceMs;
    }

    public long getLazyReplicationMs() {
        return lazyReplicationMs;
    }

    public int getHighWatermark() {
        return highWatermark;
    }

    public int getLowWatermark() {
        return lowWatermark;
    }

    // Wildcards ("*", "?") by default, or a regular expression when prefixed with "regex:".
    private static Pattern compile(final String value) {
        if (value.isEmpty()) {
            return null;
        }
        if (!value.startsWith(REGEX_PREFIX)) {
            return Pattern.compile(wildcardToRegex(value));
        }
        try {
            return Pattern.compile(value.substring(REGEX_PREFIX.length()));
        } catch (final PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid file pattern: " + e.getDescription());
        }
    }

    private static String wildcardToRegex(final String pattern) {
        final var regex = new StringBuilder();
        for (final var c : pattern.toCharArray()) {
            switch (c) {
            case '*' -> regex.append(".*");
            case '?' -> regex.append('.');
            default -> regex.append(Pattern.quote(String.valueOf(c)));
            }
        }
        return regex.toString();
    }

    // Simple wildcard match ("*" any sequence, "?" one character).
    private static boolean wildcard(final String pattern, final String text) {
        if (text == null) {
            return false;
        }
        final var regex = new StringBuilder(wildcardToRegex(pattern));
        return Pattern.matches(regex.toString(), text);
    }

    /**
     * Splits a list separated by commas, semicolons or whitespace.
     *
     * @param value
     *            the value
     *
     * @return the items
     */
    public static List<String> splitList(final String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        final List<String> items = new ArrayList<>();
        for (final var item : value.trim().split("[,;\\s]+")) {
            if (!item.isEmpty()) {
                items.add(item);
            }
        }
        return items;
    }

    private static Mode parseMode(final String value) {
        if (value.isEmpty()) {
            return Mode.DRY_RUN;
        }
        try {
            return Mode.valueOf(value.toUpperCase());
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid mode: " + value);
        }
    }

    private static String value(final Map<String, String> row, final String key) {
        final var value = row.get(key);
        return value == null ? "" : value.trim();
    }

    private static boolean flag(final Map<String, String> row, final String key, final boolean defaultValue) {
        final var value = value(row, key);
        if (value.isEmpty()) {
            return defaultValue;
        }
        return switch (value.toLowerCase(java.util.Locale.ROOT)) {
        case "1", "true", "on", "yes" -> true;
        case "0", "false", "off", "no" -> false;
        default -> throw new IllegalArgumentException("Invalid flag " + key + ": " + value);
        };
    }

    private static long number(final Map<String, String> row, final String key, final long defaultValue) {
        final var value = value(row, key);
        if (value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number for " + key + ": " + value);
        }
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Checks if is active.
     *
     * @return true, if active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Gets the mode.
     *
     * @return the mode
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Gets the order.
     *
     * @return the order
     */
    public int getOrder() {
        return order;
    }

    /**
     * Gets the requeue window in milliseconds.
     *
     * @return the requeue window
     */
    public long getRequeueWindowMs() {
        return requeueWindowMs;
    }

    /**
     * Checks if the window restarts on requeue.
     *
     * @return true, if it restarts
     */
    public boolean isRestartOnRequeue() {
        return restartOnRequeue;
    }

    /**
     * Checks if the replicas are dropped.
     *
     * @return true, if they are dropped
     */
    public boolean isDropReplicas() {
        return dropReplicas;
    }

    /**
     * Gets the delay before dropping replicas in milliseconds.
     *
     * @return the delay
     */
    public long getDropReplicasAfterMs() {
        return dropReplicasAfterMs;
    }

    /**
     * Gets the file pattern.
     *
     * @return the file pattern
     */
    public String getFilePattern() {
        return filePattern;
    }
}
