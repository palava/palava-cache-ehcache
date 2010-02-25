/**
 * palava - a java-php-bridge
 * Copyright (C) 2007-2010  CosmoCode GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.cosmocode.palava.services.cache;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.cosmocode.palava.core.lifecycle.Disposable;
import de.cosmocode.palava.core.lifecycle.Initializable;

/**
 * An implementation of the {@link CacheService} interface
 * which uses <a href="http://ehcache.org/">Ehcache</a>.
 *
 * TODO
 * - package private
 * - @Inject (ggf optional = true
 * - @Named("...") am Parameters
 * 
 * @author Willi Schoenborn
 */
public class EhCacheService implements CacheService, Initializable, Disposable {

    private static final Logger LOG = LoggerFactory.getLogger(EhCacheService.class);

    private int maxElementsInMemory = 10000;

    private MemoryStoreEvictionPolicy memoryStoreEvictionPolicy;

    @Inject
    @Named("ehcache.overflowToDisk")
    private boolean overflowToDisk;

    @Inject
    @Named("ehcache.diskStorePath")
    private String diskStorePath;

    @Inject
    @Named("ehcache.eternal")
    private boolean eternal;

    private long timeToLive = 600L;

    @Inject
    @Named("ehcache.timeToLiveUnit")
    private TimeUnit timeToLiveUnit = TimeUnit.SECONDS;

    @Inject
    @Named("ehcache.timeToIdle")
    private long timeToIdle;

    @Inject
    @Named("ehcache.timeToIdleUnit")
    private TimeUnit timeToIdleUnit = TimeUnit.SECONDS;

    @Inject
    @Named("ehcache.diskPersistent")
    private boolean diskPersistent;

    @Inject
    @Named("ehcache.diskExpiryThreadInterval")
    private long diskExpiryThreadInterval;

    @Inject
    @Named("ehcache.diskExpiryThreadIntervalUnit")
    private TimeUnit diskExpiryThreadIntervalUnit = TimeUnit.SECONDS;

    @Inject
    @Named("ehcache.maxElementsOnDisk")
    private int maxElementsOnDisk;

    @Inject
    @Named("ehcache.diskSpoolBufferSizeMB")
    private int diskSpoolBufferSizeMB;

    @Inject
    @Named("ehcache.clearOnFlush")
    private boolean clearOnFlush;

    @Inject
    @Named("ehcache.isTerracottaClustered")
    private boolean isTerracottaClustered;

    @Inject
    @Named("ehcache.terracottaValueMode")
    private String terracottaValueMode;

    @Inject
    @Named("ehcache.terracottaCoherentReads")
    private boolean terracottaCoherentReads;

    private CacheManager manager;
    
    private Ehcache cache;
    
    @Inject
    void setMemoryStoreEvictionPolicy(@Named("ehcache.cacheMode") CacheMode cacheMode) {
        this.memoryStoreEvictionPolicy = of(cacheMode);
    }
    
    @Inject(optional = true)
    void setMaxElementsInMemory(@Named("ehcache.maxElementsInMemory") int maxElementsInMemory) {
        this.maxElementsInMemory = maxElementsInMemory;
    }
    
    @Inject(optional = true)
    void setTimeToLive(@Named("ehcache.timeToLive") long timeToLive) {
        this.timeToLive = timeToLive;
    }
    
    private MemoryStoreEvictionPolicy of(CacheMode mode) {
        switch (mode) {
            case LRU: {
                return MemoryStoreEvictionPolicy.LFU;
            }
            case LFU: {
                return MemoryStoreEvictionPolicy.LFU;
            }
            case FIFO: {
                return MemoryStoreEvictionPolicy.FIFO;
            }
            default: {
                throw new UnsupportedOperationException(mode.name());
            }
        }
    }
    
    @Override
    public void initialize() {
        LOG.info("Ehcache: [clearOnFlush={}, diskExpiryThreadInterval={}, diskExpiryThreadIntervalUnit={}, " +
            "diskPersistent={}, diskSpoolBufferSizeMB={}, diskStorePath={}, eternal={}, isTerracottaClustered={}, " +
            "maxElementsInMemory={}, maxElementsOnDisk={}, memoryStoreEvictionPolicy={}, overflowToDisk={}, " +
            "terracottaCoherentReads={}, terracottaValueMode={}, timeToIdle={}, timeToIdleUnit={}, timeToLive={}, " +
            "timeToLiveUnit={}]", new Object[] {
                clearOnFlush, diskExpiryThreadInterval, diskExpiryThreadIntervalUnit, diskPersistent,
                diskSpoolBufferSizeMB, diskStorePath, eternal, isTerracottaClustered, maxElementsInMemory,
                maxElementsOnDisk, memoryStoreEvictionPolicy, overflowToDisk, terracottaCoherentReads,
                terracottaValueMode, timeToIdle, timeToIdleUnit, timeToLive, timeToLiveUnit
            }
        );
        manager = CacheManager.create();
        final String name = getClass().getName();
        cache = new Cache(
                name,
                maxElementsInMemory,
                memoryStoreEvictionPolicy,
                overflowToDisk,
                diskStorePath,
                eternal, 
                timeToLiveUnit.toSeconds(timeToLive),
                timeToIdleUnit.toSeconds(timeToIdle),
                diskPersistent,
                diskExpiryThreadIntervalUnit.toSeconds(diskExpiryThreadInterval),
                null,
                null, 
                maxElementsOnDisk,
                diskSpoolBufferSizeMB,
                clearOnFlush,
                isTerracottaClustered,
                terracottaValueMode,
                terracottaCoherentReads
        );
        if (manager.cacheExists(name)) {
            manager.removeCache(name);
        }
        manager.addCache(cache);
    }
    
    @Override
    public void store(Serializable key, Object value) {
        Preconditions.checkNotNull(key, "Key");
        final Element element = new Element(key, value);
        cache.putQuiet(element);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T read(Serializable key) {
        Preconditions.checkNotNull(key, "Key");
        final Element element = cache.get(key);
        return element == null ? null : (T) element.getValue();
    }
    
    @Override
    public <T> T remove(Serializable key) {
        Preconditions.checkNotNull(key, "Key");
        final T value = this.<T>read(key);
        cache.remove(key);
        return value;
    }
    
    @Override
    public void clear() {
        cache.removeAll();
    }
    
    @Override
    public void dispose() {
        manager.shutdown();
    }
    
    @Override
    public String toString() {
        return cache.toString();
    }
    
    Ehcache getCache() {
        return cache;
    }
    
}

