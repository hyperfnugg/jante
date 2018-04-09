package jante.model;

import static com.google.common.base.Strings.isNullOrEmpty;

public interface PropertyProvider {

    /**
     * Retreives property for given string or null if missing or empty
     */
    String get(String key);


    void failIfNotPresent(String... keys);

    void failIfNotPresent(Iterable<String> keys);

    /**
     * Retreives property for given string, returning fallback on missing or empty. Fallback may be null.
     */
    String getWithFallback(String key, String fallback);

    /**
     * Retreives property for given string, returning fallback on missing or empty.
     *
     * Trows RuntimeException if property for key null or empty AND fallbak null or empty
     */
    default String requireWithFallback(String key, String fallback) {
        String prop = getWithFallback(key, fallback);
        if (!isNullOrEmpty(prop)) {
            return prop;
        } else {
            throw new RuntimeException("missing property: " + key);
        }
    }

    /**
     * Retreives property for given string, returning fallback on missing or empty. Fallback may be null.
     */
    default Integer getWithFallback(String key, Integer fallback) {
        return Integer.valueOf(getWithFallback(key, String.valueOf(fallback)));
    }

    /**
     * Retreives property for given string, returning fallback on missing or empty.
     *
     * Throws RuntimeException if property for key null or empty AND fallbak null or empty
     */
    default Integer requireWithFallback(String key, Integer fallback) {
        return Integer.valueOf(requireWithFallback(key, String.valueOf(fallback)));
    }
}
