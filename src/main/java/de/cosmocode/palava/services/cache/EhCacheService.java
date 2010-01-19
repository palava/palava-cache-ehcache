/**
 * palava - a java-php-bridge
 * Copyright (C) 2007  CosmoCode GmbH
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

import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.cosmocode.palava.ServiceInitializationException;
import de.cosmocode.palava.core.service.lifecycle.Initializable;
import de.cosmocode.palava.core.service.lifecycle.Startable;

/**
 * An implementation of the {@link CacheService} interface
 * which uses <a href="http://ehcache.org/">Ehcache</a>.
 *
 * @author Willi Schoenborn
 */
public class EhCacheService implements CacheService, Initializable, Startable {

    private static final Logger log = LoggerFactory.getLogger(EhCacheService.class);

    @Inject
    @Named("ehcache.maxElementsInMemory")
    private int maxElementsInMemory;

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

    @Inject
    @Named("ehcache.timeToLive")
    private long timeToLive;

    @Inject
    @Named("ehcache.timeToLiveUnit")
    private TimeUnit timeToLiveUnit;

    @Inject
    @Named("ehcache.timeToIdle")
    private long timeToIdle;

    @Inject
    @Named("ehcache.timeToIdleUnit")
    private TimeUnit timeToIdleUnit;

    @Inject
    @Named("ehcache.diskPersistent")
    private boolean diskPersistent;

    @Inject
    @Named("ehcache.diskExpiryThreadInterval")
    private long diskExpiryThreadInterval;

    @Inject
    @Named("ehcache.diskExpiryThreadIntervalUnit")
    private TimeUnit diskExpiryThreadIntervalUnit;

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
    public void initialize() throws ServiceInitializationException {
        log.info("Ehcache: [clearOnFlush={}, diskExpiryThreadInterval={}, diskExpiryThreadIntervalUnit={}, " +
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
    }
    
    @Override
    public void start() {
        final String name = getClass().getName();
        manager.addCache(new Cache(
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
        ));
    }
    
    @Override
    public void store(Serializable key, Object value) {
        final Element element = new Element(key, value);
        cache.putQuiet(element);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T read(Serializable key) {
        return (T) cache.get(key);
    }
    
    @Override
    public <T> T remove(Serializable key) {
        final T value = read(key);
        cache.remove(key);
        return value;
    }
    
    @Override
    public void clear() {
        cache.removeAll();
    }
    
    @Override
    public void stop() {
        manager.shutdown();
    }
    
}
