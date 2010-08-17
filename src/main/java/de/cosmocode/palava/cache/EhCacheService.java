/**
 * Copyright 2010 CosmoCode GmbH
 *
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
 */

package de.cosmocode.palava.cache;

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
final class EhCacheService implements CacheService, Initializable, Disposable {

    private static final Logger LOG = LoggerFactory.getLogger(EhCacheService.class);
    
    private static final String MAX_AGE_NEGATIVE = "Max age must not be negative, but was %s";

    private String name = "ehcache";
    
    /*
     * Start configuration parameters.
     */

    private boolean eternal;

    private boolean overflowToDisk;
    
    private MemoryStoreEvictionPolicy memoryStoreEvictionPolicy = MemoryStoreEvictionPolicy.LRU;
    
    private int maxElementsInMemory = 10000;
    
    private long timeToLive = 600L;
    
    private TimeUnit timeToLiveUnit = TimeUnit.SECONDS;

    private long timeToIdle = 600L;
    
    private TimeUnit timeToIdleUnit = TimeUnit.SECONDS;
    
    private boolean diskPersistent;

    private String diskStorePath;
    
    private int maxElementsOnDisk;

    private long diskExpiryThreadInterval;
    
    private TimeUnit diskExpiryThreadIntervalUnit = TimeUnit.SECONDS;

    private int diskSpoolBufferSizeMB;

    private boolean clearOnFlush = true;

    private boolean isTerracottaClustered;

    private String terracottaValueMode;

    private boolean terracottaCoherentReads = true;

    /*
     * End configuration parameters. 
     */
    
    private final CacheManager manager = CacheManager.create();
    
    private Ehcache cache;
    
    @Inject(optional = true)
    void setName(@Named(EhCacheServiceConfig.NAME) String name) {
        this.name = Preconditions.checkNotNull(name, "Name");
    }

    @Inject(optional = true)
    void setEternal(@Named(EhCacheServiceConfig.ETERNAL) boolean eternal) {
        this.eternal = eternal;
    }
    
    @Inject(optional = true)
    void setOverflowToDisk(@Named(EhCacheServiceConfig.OVERFLOW_TO_DISK) boolean overflowToDisk) {
        this.overflowToDisk = overflowToDisk;
    }
    
    @Inject(optional = true)
    void setMemoryStoreEvictionPolicy(@Named(EhCacheServiceConfig.CACHE_MODE) CacheMode cacheMode) {
        this.memoryStoreEvictionPolicy = of(cacheMode);
    }

    @Inject(optional = true)
    void setMaxElementsInMemory(@Named(EhCacheServiceConfig.MAX_ELEMENTS_IN_MEMORY) int maxElementsInMemory) {
        this.maxElementsInMemory = maxElementsInMemory;
    }

    @Inject(optional = true)
    void setTimeToLive(@Named(EhCacheServiceConfig.TIME_TO_LIVE) long timeToLive) {
        this.timeToLive = timeToLive;
    }

    @Inject(optional = true)
    void setTimeToLiveUnit(@Named(EhCacheServiceConfig.TIME_TO_LIVE_UNIT) TimeUnit timeToLiveUnit) {
        this.timeToLiveUnit = timeToLiveUnit;
    }

    @Inject(optional = true)
    void setTimeToIdle(@Named(EhCacheServiceConfig.TIME_TO_IDLE) long timeToIdle) {
        this.timeToIdle = timeToIdle;
    }

    @Inject(optional = true)
    void setTimeToIdleUnit(@Named(EhCacheServiceConfig.TIME_TO_IDLE_UNIT) TimeUnit timeToIdleUnit) {
        this.timeToIdleUnit = timeToIdleUnit;
    }

    @Inject(optional = true)
    void setDiskPersistent(@Named(EhCacheServiceConfig.DISK_PERSISTENT) boolean diskPersistent) {
        this.diskPersistent = diskPersistent;
    }
    
    @Inject(optional = true)
    void setDiskStorePath(@Named(EhCacheServiceConfig.DISK_STORE_PATH) String diskStorePath) {
        this.diskStorePath = diskStorePath;
    }
    
    @Inject(optional = true)
    void setMaxElementsOnDisk(@Named(EhCacheServiceConfig.MAX_ELEMENTS_ON_DISK) int maxElementsOnDisk) {
        this.maxElementsOnDisk = maxElementsOnDisk;
    }

    @Inject(optional = true)
    void setDiskExpiryThreadInterval(
        @Named(EhCacheServiceConfig.DISK_EXPIRY_THREAD_INTERVAL) long diskExpiryThreadInterval) {
        this.diskExpiryThreadInterval = diskExpiryThreadInterval;
    }

    @Inject(optional = true)
    void setDiskExpiryThreadIntervalUnit(
        @Named(EhCacheServiceConfig.DISK_EXPIRY_THREAD_INTERVAL_UNIT) TimeUnit diskExpiryThreadIntervalUnit) {
        this.diskExpiryThreadIntervalUnit = diskExpiryThreadIntervalUnit;
    }

    @Inject(optional = true)
    void setDiskSpoolBufferSizeMB(@Named(EhCacheServiceConfig.DISK_SPOOL_BUFFER_SIZE_MB) int diskSpoolBufferSizeMB) {
        this.diskSpoolBufferSizeMB = diskSpoolBufferSizeMB;
    }
    
    @Inject(optional = true)
    void setClearOnFlush(@Named(EhCacheServiceConfig.CLEAR_ON_FLUSH) boolean clearOnFlush) {
        this.clearOnFlush = clearOnFlush;
    }

    @Inject(optional = true)
    void setTerracottaClustered(@Named(EhCacheServiceConfig.IS_TERRACOTTA_CLUSTERED) boolean terracottaClustered) {
        this.isTerracottaClustered = terracottaClustered;
    }
    
    @Inject(optional = true)
    void setTerracottaValueMode(@Named(EhCacheServiceConfig.TERRACOTTA_VALUE_MODE) String terracottaValueMode) {
        this.terracottaValueMode = terracottaValueMode;
    }

    @Inject(optional = true)
    void setTerracottaCoherentReads(
        @Named(EhCacheServiceConfig.TERRACOTTA_COHERENT_READS) boolean terracottaCoherentReads) {
        this.terracottaCoherentReads = terracottaCoherentReads;
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
    public long getMaxAge() {
        return cache.getCacheConfiguration().getTimeToLiveSeconds();
    }
    
    @Override
    public long getMaxAge(TimeUnit unit) {
        return unit.convert(getMaxAge(), TimeUnit.SECONDS);
    }
    
    @Override
    public void setMaxAge(long maxAgeSeconds) {
        Preconditions.checkArgument(maxAgeSeconds >= 0, MAX_AGE_NEGATIVE, maxAgeSeconds);
        cache.getCacheConfiguration().setTimeToLiveSeconds(maxAgeSeconds);
        cache.getCacheConfiguration().setEternal(false);
    }
    
    @Override
    public void setMaxAge(long maxAge, TimeUnit maxAgeUnit) {
        Preconditions.checkArgument(maxAge >= 0, MAX_AGE_NEGATIVE, maxAge);
        Preconditions.checkNotNull(maxAgeUnit, "MaxAge TimeUnit");
        this.setMaxAge(maxAgeUnit.toSeconds(maxAge));
    }
    
    @Override
    public void store(Serializable key, Object value) {
        Preconditions.checkNotNull(key, "Key");
        final Element element = new Element(key, value);
        cache.putQuiet(element);
    }
    
    @Override
    public void store(Serializable key, Object value, long maxAge, TimeUnit maxAgeUnit) {
        Preconditions.checkNotNull(key, "Key");
        Preconditions.checkArgument(maxAge >= 0, MAX_AGE_NEGATIVE, maxAge);
        Preconditions.checkNotNull(maxAgeUnit, "MaxAge TimeUnit");

        final int maxAgeSeconds = (int) maxAgeUnit.toSeconds(maxAge);
        final Element element = new Element(key, value, false, null, maxAgeSeconds);
        cache.putQuiet(element);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T read(Serializable key) {
        Preconditions.checkNotNull(key, "Key");
        final Element element = cache.get(key);
        return element == null ? null : (T) element.getObjectValue();
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
        return String.format("%s [%s]", EhCacheService.class.getSimpleName(), name);
    }
    
    Ehcache getCache() {
        return cache;
    }
    
}
