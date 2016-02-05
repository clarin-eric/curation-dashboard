package eu.clarin.cmdi.curation.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dostojic
 *
 */

public abstract class AbstractCache<K, V> {

    protected Map<K, V> cache = new HashMap<>();

    public abstract V lookup(final K key) throws Exception;

    public boolean contains(K key) {
	return cache.containsKey(key);
    }

    public void put(K key, V value) {
	cache.put(key, value);
    }

    synchronized public void clear() {
	cache.clear();
    }

}
