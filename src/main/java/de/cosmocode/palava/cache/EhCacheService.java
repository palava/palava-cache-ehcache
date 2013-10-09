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
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.TerracottaConfiguration;
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
    
    private long timeToLive = 600L;
    
    private TimeUnit timeToLiveUnit = TimeUnit.SECONDS;

    private long timeToIdle = 600L;
    
    private TimeUnit timeToIdleUnit = TimeUnit.SECONDS;

    private long diskExpiryThreadInterval;
    
    private TimeUnit diskExpiryThreadIntervalUnit = TimeUnit.SECONDS;

    /*
     * End configuration parameters. 
     */
    
    private final CacheManager manager = CacheManager.create();
    
    private Ehcache cache;

    private CacheConfiguration config;

    @Inject
    EhCacheService(@Named(EhCacheServiceConfig.NAME) String name) {
        this.name = name;
        if (manager.cacheExists(name)) {
            this.config = manager.getCache(name).getCacheConfiguration();
            timeToLive = this.config.getTimeToLiveSeconds();
            timeToLiveUnit = TimeUnit.SECONDS;
            timeToIdle = this.config.getTimeToIdleSeconds();
            timeToIdleUnit = TimeUnit.SECONDS;
            diskExpiryThreadInterval = this.config.getDiskExpiryThreadIntervalSeconds();
            diskExpiryThreadIntervalUnit = TimeUnit.SECONDS;
        } else {
            this.config = new CacheConfiguration(name, 0);
            this.config.setName(name);
        }
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
    void setDiskExpiryThreadInterval(
        @Named(EhCacheServiceConfig.DISK_EXPIRY_THREAD_INTERVAL) long diskExpiryThreadInterval) {
        this.diskExpiryThreadInterval = diskExpiryThreadInterval;
    }

    @Inject(optional = true)
    void setDiskExpiryThreadIntervalUnit(
        @Named(EhCacheServiceConfig.DISK_EXPIRY_THREAD_INTERVAL_UNIT) TimeUnit diskExpiryThreadIntervalUnit) {
        this.diskExpiryThreadIntervalUnit = diskExpiryThreadIntervalUnit;
    }

    /**
     * Sets eternal to the given value.
     * @param eternal the new eternal value
     */
    @Inject(optional = true)
    void setEternal(@Named(EhCacheServiceConfig.ETERNAL) boolean eternal) {
        config.setEternal(eternal);
    }

    /**
     * Sets overflow to disk.
     * @param overflowToDisk whether or not to overflow to disk
     */
    @Inject(optional = true)
    void setOverflowToDisk(@Named(EhCacheServiceConfig.OVERFLOW_TO_DISK) boolean overflowToDisk) {
        config.setOverflowToDisk(overflowToDisk);
    }

    /**
     * The eviction policy that applies when elements have to be evicted from the cache.
     * @param cacheMode the new cache mode (or eviction policy for EhCache)
     */
    @Inject(optional = true)
    void setMemoryStoreEvictionPolicy(@Named(EhCacheServiceConfig.CACHE_MODE) CacheMode cacheMode) {
        config.setMemoryStoreEvictionPolicyFromObject(of(cacheMode));
    }

    /**
     * Sets whether the disk store persists between CacheManager instances.
     * Note that this operates independently of overflowToDisk.
     * @param diskPersistent new value for disk persistent
     */
    @Inject(optional = true)
    void setDiskPersistent(@Named(EhCacheServiceConfig.DISK_PERSISTENT) boolean diskPersistent) {
        config.setDiskPersistent(diskPersistent);
    }

    /**
     * Sets the path that will be used for the disk store.
     * @param diskStorePath the path for the disk store (if used)
     */
    @Inject(optional = true)
    void setDiskStorePath(@Named(EhCacheServiceConfig.DISK_STORE_PATH) String diskStorePath) {
        config.setDiskStorePath(diskStorePath);
    }

    /**
     * Sets the maximum number of elements in memory. 0 means unlimited.
     * @param maxElementsInMemory maximum number of elements in memory
     */
    @Inject(optional = true)
    void setMaxElementsInMemory(@Named(EhCacheServiceConfig.MAX_ELEMENTS_IN_MEMORY) int maxElementsInMemory) {
        config.setMaxElementsInMemory(maxElementsInMemory);
    }

    /**
     * Sets the maximum number elements on Disk. 0 means unlimited.
     * @param maxElementsOnDisk maximum number of elements on disk
     */
    @Inject(optional = true)
    void setMaxElementsOnDisk(@Named(EhCacheServiceConfig.MAX_ELEMENTS_ON_DISK) int maxElementsOnDisk) {
        config.setMaxElementsOnDisk(maxElementsOnDisk);
    }

    /**
     * Sets the disk spool size, which is used to buffer writes to the DiskStore.
     * @param diskSpoolBufferSizeMB buffer size in MB, positive number
     */
    @Inject(optional = true)
    void setDiskSpoolBufferSizeMB(@Named(EhCacheServiceConfig.DISK_SPOOL_BUFFER_SIZE_MB) int diskSpoolBufferSizeMB) {
        config.setDiskSpoolBufferSizeMB(diskSpoolBufferSizeMB);
    }

    /**
     * Sets whether the MemoryStore should be cleared when flush() is called on the cache - true by default.
     * @param clearOnFlush true to clear on flush
     */
    @Inject(optional = true)
    void setClearOnFlush(@Named(EhCacheServiceConfig.CLEAR_ON_FLUSH) boolean clearOnFlush) {
        config.setClearOnFlush(clearOnFlush);
    }

    /**
     * Indicates whether to cluster this cache with Terracotta.
     * If terracotta is used this defaults to true.
     * @param terracottaClustered true to cluster with terracotta, false otherwise
     */
    @Inject(optional = true)
    void setTerracottaClustered(@Named(EhCacheServiceConfig.IS_TERRACOTTA_CLUSTERED) boolean terracottaClustered) {
        if (config.getTerracottaConfiguration() == null) {
            config.addTerracotta(new TerracottaConfiguration());
        }
        config.getTerracottaConfiguration().setClustered(terracottaClustered);
    }

    /**
     * Represents whether values are stored with serialization in the clustered store
     * or through Terracotta clustered identity.
     * @param terracottaValueMode the value mode, as in {@link net.sf.ehcache.config.TerracottaConfiguration.ValueMode}
     */
    @Inject(optional = true)
    void setTerracottaValueMode(@Named(EhCacheServiceConfig.TERRACOTTA_VALUE_MODE) String terracottaValueMode) {
        if (config.getTerracottaConfiguration() == null) {
            config.addTerracotta(new TerracottaConfiguration());
        }
        config.getTerracottaConfiguration().setValueMode(terracottaValueMode);
    }

    /**
     * Sets coherent reads in the terracotta config and activates terracotta if not already done.
     * @param terracottaCoherentReads true for coherent reads
     */
    @Inject(optional = true)
    void setTerracottaCoherentReads(
        @Named(EhCacheServiceConfig.TERRACOTTA_COHERENT_READS) boolean terracottaCoherentReads) {
        if (config.getTerracottaConfiguration() == null) {
            config.addTerracotta(new TerracottaConfiguration());
        }
        config.getTerracottaConfiguration().setCoherent(terracottaCoherentReads);
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
        config.setTimeToLiveSeconds(timeToLiveUnit.toSeconds(timeToLive));
        config.setTimeToIdleSeconds(timeToIdleUnit.toSeconds(timeToIdle));
        config.setDiskExpiryThreadIntervalSeconds(diskExpiryThreadIntervalUnit.toSeconds(diskExpiryThreadInterval));
        config.validateCompleteConfiguration();

        logConfiguredValues();

        if (!manager.cacheExists(name)) {
            cache = new Cache(config);
            cache.initialise();
            manager.addCache(name);
        } else {
            cache = manager.getCache(name);
        }
    }

    private void logConfiguredValues() {
        final TerracottaConfiguration terracottaConfiguration = config.getTerracottaConfiguration();
        final boolean terracottaCoherent;
        final TerracottaConfiguration.ValueMode terracottaValueMode;
        if (terracottaConfiguration == null) {
            terracottaCoherent = false;
            terracottaValueMode = null;
        } else {
            terracottaCoherent = terracottaConfiguration.isCoherent();
            terracottaValueMode = terracottaConfiguration.getValueMode();
        }

        LOG.info("Ehcache: [clearOnFlush={}, diskExpiryThreadInterval={}, diskExpiryThreadIntervalUnit={}, " +
            "diskPersistent={}, diskSpoolBufferSizeMB={}, diskStorePath={}, eternal={}, isTerracottaClustered={}, " +
            "maxElementsInMemory={}, maxElementsOnDisk={}, memoryStoreEvictionPolicy={}, overflowToDisk={}, " +
            "terracottaCoherentReads={}, terracottaValueMode={}, timeToIdle={}, timeToIdleUnit={}, timeToLive={}, " +
            "timeToLiveUnit={}]", new Object[] {
                config.isClearOnFlush(), diskExpiryThreadInterval, diskExpiryThreadIntervalUnit,
                config.isDiskPersistent(), config.getDiskSpoolBufferSizeMB(), config.getDiskStorePath(),
                config.isEternal(), config.isTerracottaClustered(), config.getMaxElementsInMemory(),
                config.getMaxElementsOnDisk(), config.getMemoryStoreEvictionPolicy(), config.isOverflowToDisk(),
                terracottaCoherent, terracottaValueMode, timeToIdle, timeToIdleUnit, timeToLive, timeToLiveUnit
            }
        );
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
    public void store(final Serializable key, final Object value, final long maxAge, final TimeUnit maxAgeUnit) {
        Preconditions.checkNotNull(key, "Key");
        Preconditions.checkNotNull(maxAgeUnit, "MaxAgeUnit");

        final Element element = new Element(key, value);
        element.setEternal(false);
        element.setTimeToLive((int) maxAgeUnit.toSeconds(maxAge));
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
