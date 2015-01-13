package core.graphics;

// Copyright 2007 Christian d'Heureuse, Inventec Informatik AG, Zurich,
// Switzerland
// www.source-code.biz, www.inventec.ch/chdh
//
// This module is multi-licensed and may be used under the terms
// of any of the following licenses:
//
// EPL, Eclipse Public License, V1.0 or later, http://www.eclipse.org/legal
// LGPL, GNU Lesser General Public License, V2 or later,
// http://www.gnu.org/licenses/lgpl.html
// GPL, GNU General Public License, V2 or later,
// http://www.gnu.org/licenses/gpl.html
// AL, Apache License, V2.0 or later, http://www.apache.org/licenses
// BSD, BSD License, http://www.opensource.org/licenses/bsd-license.php
//
// Please contact the author if you need another license.
// This module is provided "as is", without warranties of any kind.

import com.jme3.math.Vector2f;
import com.jme3.scene.control.UpdateControl;
import com.jme3.terrain.geomipmap.TerrainQuad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * An LRU terrain cache, based on <code>LinkedHashMap</code>.
 */
public class TerrainCache {

	private static final float hashTableLoadFactor = 0.75f;
	private LinkedHashMap<Vector2f, TerrainQuad> map;
	private int cacheSize;
	private TerrainPager terrainPager;

	/**
	 * Creates a new LRU cache.
	 *
	 * @param cacheSize
	 *            the maximum number of entries that will be kept in this cache.
	 * @param terrainPager
	 */
	public TerrainCache(int cacheSize, TerrainPager terrainPager) {
		this.cacheSize = cacheSize;
		this.terrainPager = terrainPager;
		int hashTableCapacity = (int) Math.ceil(cacheSize / TerrainCache.hashTableLoadFactor) + 1;
		this.map = new LinkedHashMap<Vector2f, TerrainQuad>(hashTableCapacity, TerrainCache.hashTableLoadFactor, true) {
			// (an anonymous inner class)

			private static final long serialVersionUID = 1;
			
			@Override
			protected boolean removeEldestEntry(final Map.Entry<Vector2f, TerrainQuad> eldest) {
				boolean rem = this.size() > TerrainCache.this.cacheSize;

				if (rem)
					TerrainCache.this.terrainPager.getControl(UpdateControl.class).enqueue(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							eldest.getValue().removeFromParent();
							return null;
						}
					});

				return this.size() > TerrainCache.this.cacheSize;
			}
		};
	}

	public synchronized void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	public int getCacheSize() {
		return cacheSize;
	}

	/**
	 * Retrieves an entry from the cache.<br>
	 * The retrieved entry becomes the MRU (most recently used) entry.
	 *
	 * @param key
	 *            the key whose associated value is to be returned.
	 * @return the value associated to this key, or null if no value with this
	 *         key exists in the cache.
	 */
	public synchronized TerrainQuad get(Vector2f key) {
		return this.map.get(key);
	}

	/**
	 * Adds an entry to this cache.
	 * The new entry becomes the MRU (most recently used) entry.
	 * If an entry with the specified key already exists in the cache, it is
	 * replaced by the new entry.
	 * If the cache is full, the LRU (least recently used) entry is removed from
	 * the cache.
	 *
	 * @param key
	 *            the key with which the specified value is to be associated.
	 * @param value
	 *            a value to be associated with the specified key.
	 */
	public synchronized void put(Vector2f key, TerrainQuad value) {
		this.map.put(key, value);
	}

	/**
	 * Clears the cache.
	 */
	public synchronized void clear() {
		this.map.clear();
	}

	/**
	 * Returns the number of used entries in the cache.
	 *
	 * @return the number of entries currently in the cache.
	 */
	public synchronized int usedEntries() {
		return this.map.size();
	}

	/**
	 * Returns a <code>Collection</code> that contains a copy of all cache
	 * entries.
	 *
	 * @return a <code>Collection</code> with a copy of the cache content.
	 */
	public synchronized Collection<Map.Entry<Vector2f, TerrainQuad>> getAll() {
		return new ArrayList<Map.Entry<Vector2f, TerrainQuad>>(this.map.entrySet());
	}
} // end class LRUCache
