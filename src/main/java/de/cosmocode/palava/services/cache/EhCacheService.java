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
 * @author Willi Schoenborn
 * @author Oliver Lorenz
 */
class EhCacheService implements CacheService, Initializable, Disposable {

    private static final Logger LOG = LoggerFactory.getLogger(EhCacheService.class);

    private int maxElementsInMemory = 10000;

    private MemoryStoreEvictionPolicy memoryStoreEvictionPolicy = MemoryStoreEvictionPolicy.LRU;

    private final boolean overflowToDisk;

    private String diskStorePath;

    private final boolean eternal;

    private long timeToLive = 600L;

    private TimeUnit timeToLiveUnit = TimeUnit.SECONDS;

    private long timeToIdle = 600L;

    private TimeUnit timeToIdleUnit = TimeUnit.SECONDS;

    private boolean diskPersistent;

    private long diskExpiryThreadInterval;

    private TimeUnit diskExpiryThreadIntervalUnit = TimeUnit.SECONDS;

    private int maxElementsOnDisk;

    private int diskSpoolBufferSizeMB;

    private boolean clearOnFlush = true;

    private boolean isTerracottaClustered;

    private String terracottaValueMode;

    private boolean terracottaCoherentReads = true;

    private CacheManager manager;
    
    private Ehcache cache;
    
    
    /**
     * Injected constructor. Sets overflowToDisk.
     * @param overflowToDisk if true, then the cache puts elements onto the disk if the memory is full
     * @param eternal if true, the cached elements never expire
     */
    @Inject
    public EhCacheService(
        @Named("ehcache.overflowToDisk") final boolean overflowToDisk,
        @Named("ehcache.eternal") final boolean eternal) {
        
        this.overflowToDisk = overflowToDisk;
        this.eternal = eternal;
    }
    
    
    @Inject(optional = true)
    void setClearOnFlush(@Named("ehcache.clearOnFlush") boolean clearOnFlush) {
        this.clearOnFlush = clearOnFlush;
    }

    @Inject(optional = true)
    public void setDiskExpiryThreadInterval(
            @Named("ehcache.diskExpiryThreadInterval") long diskExpiryThreadInterval) {
        this.diskExpiryThreadInterval = diskExpiryThreadInterval;
    }
    
    @Inject(optional = true)
    public void setDiskExpiryThreadIntervalUnit(
            @Named("ehcache.diskExpiryThreadIntervalUnit") TimeUnit diskExpiryThreadIntervalUnit) {
        this.diskExpiryThreadIntervalUnit = diskExpiryThreadIntervalUnit;
    }
    
    @Inject(optional = true)
    public void setDiskPersistent(@Named("ehcache.diskPersistent") boolean diskPersistent) {
        this.diskPersistent = diskPersistent;
    }
    
    @Inject(optional = true)
    public void setDiskStorePath(@Named("ehcache.diskStorePath") String diskStorePath) {
        this.diskStorePath = diskStorePath;
    }
    
    @Inject(optional = true)
    public void setDiskSpoolBufferSizeMB(@Named("ehcache.diskSpoolBufferSizeMB") int diskSpoolBufferSizeMB) {
        this.diskSpoolBufferSizeMB = diskSpoolBufferSizeMB;
    }
    
    @Inject(optional = true)
    void setMaxElementsInMemory(@Named("ehcache.maxElementsInMemory") int maxElementsInMemory) {
        this.maxElementsInMemory = maxElementsInMemory;
    }

    @Inject(optional = true)
    public void setMaxElementsOnDisk(@Named("ehcache.maxElementsOnDisk") int maxElementsOnDisk) {
        this.maxElementsOnDisk = maxElementsOnDisk;
    }

    @Inject(optional = true)
    void setMemoryStoreEvictionPolicy(@Named("ehcache.cacheMode") CacheMode cacheMode) {
        this.memoryStoreEvictionPolicy = of(cacheMode);
    }

    @Inject(optional = true)
    void setTerracottaClustered(@Named("ehcache.isTerracottaClustered") boolean terracottaClustered) {
        this.isTerracottaClustered = terracottaClustered;
    }
    
    @Inject(optional = true)
    void setTerracottaValueMode(@Named("ehcache.terracottaValueMode") String terracottaValueMode) {
        this.terracottaValueMode = terracottaValueMode;
    }

    @Inject(optional = true)
    void setTerracottaCoherentReads(@Named("ehcache.terracottaCoherentReads") boolean terracottaCoherentReads) {
        this.terracottaCoherentReads = terracottaCoherentReads;
    }

    @Inject(optional = true)
    public void setTimeToIdle(@Named("ehcache.timeToIdle") long timeToIdle) {
        this.timeToIdle = timeToIdle;
    }

    @Inject(optional = true)
    public void setTimeToIdleUnit(@Named("ehcache.timeToIdleUnit") TimeUnit timeToIdleUnit) {
        this.timeToIdleUnit = timeToIdleUnit;
    }
    
    @Inject(optional = true)
    void setTimeToLive(@Named("ehcache.timeToLive") long timeToLive) {
        this.timeToLive = timeToLive;
    }
    
    @Inject(optional = true)
    public void setTimeToLiveUnit(@Named("ehcache.timeToLiveUnit") TimeUnit timeToLiveUnit) {
        this.timeToLiveUnit = timeToLiveUnit;
    }
    
    
    private MemoryStoreEvictionPolicy of(CacheMode mode) {
        switch (mode) {
            case LRU: {
                return MemoryStoreEvictionPolicy.LRU;
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

