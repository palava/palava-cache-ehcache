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
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import de.cosmocode.palava.AbstractService;
import de.cosmocode.palava.Server;
import de.cosmocode.palava.ServiceInitializationException;

/**
 * An implementation of the {@link CacheService} interface
 * which uses <a href="http://ehcache.org/">Ehcache</a>.
 *
 * @author Willi Schoenborn
 */
public class EhCacheService extends AbstractService implements CacheService {

    private static final Logger log = LoggerFactory.getLogger(EhCacheService.class);

    private int maxElementsInMemory;
    
    private MemoryStoreEvictionPolicy memoryStoreEvictionPolicy;
    
    private boolean overflowToDisk;
    
    private String diskStorePath;
    
    private boolean eternal;
    
    private long timeToLive;
    
    private TimeUnit timeToLiveUnit;
    
    private long timeToIdle;
    
    private TimeUnit timeToIdleUnit;
    
    private boolean diskPersistent;
    
    private long diskExpiryThreadInterval;
    
    private TimeUnit diskExpiryThreadIntervalUnit;
    
    private int maxElementsOnDisk;
    
    private int diskSpoolBufferSizeMB;
    
    private boolean clearOnFlush;
    
    private boolean isTerracottaClustered;
    
    private String terracottaValueMode;
    
    private boolean terracottaCoherentReads;
    
    private CacheManager manager;
    
    private Ehcache cache;
    
    @Override
    public void configure(Element root, Server neverUsed) {
        this.maxElementsInMemory = Integer.parseInt(Preconditions.checkNotNull(
            root.getChildText("maxElementsInMemory"), "MaxElementsInMemory"));
        final CacheMode cacheMode = CacheMode.valueOf(Preconditions.checkNotNull(
            root.getChildText("cacheMode"), "CacheMode").toUpperCase());
        this.memoryStoreEvictionPolicy = of(cacheMode);
        this.overflowToDisk = Boolean.parseBoolean(Preconditions.checkNotNull(
            root.getChildText("overflowToDisk"), "OverflowToDistk"));
        this.diskStorePath = Preconditions.checkNotNull(root.getChildText("diskStorePath"), "DiskStorePath");
        this.eternal = Boolean.parseBoolean(Preconditions.checkNotNull(
            root.getChildText("eternal"), "Eternal"));
        this.timeToLive = Long.parseLong(Preconditions.checkNotNull(
            root.getChildText("timeToLive"), "TimeToLive"));
        this.timeToLiveUnit = TimeUnit.valueOf(Preconditions.checkNotNull(
            root.getChildText("timeToLiveUnit"), "TimeToLiveUnit").toUpperCase());
        this.timeToIdle = Long.parseLong(Preconditions.checkNotNull(
            root.getChildText("timeToIdle"), "TimeToIdle"));
        this.timeToIdleUnit = TimeUnit.valueOf(Preconditions.checkNotNull(
            root.getChildText("timeToIdleUnit"), "TimeToIdleUnit").toUpperCase());
        this.diskPersistent = Boolean.parseBoolean(Preconditions.checkNotNull(
            root.getChildText("diskPersistent"), "DiskPersistent"));
        this.diskExpiryThreadInterval = Long.parseLong(Preconditions.checkNotNull(
            root.getChildText("diskExpiryThreadInterval"), "DiskExpiryThreadInterval"));
        this.diskExpiryThreadIntervalUnit = TimeUnit.valueOf(Preconditions.checkNotNull(
            root.getChildText("diskExpiryThreadIntervalUnit"), "DiskExpiryThreadIntervalUnit").toUpperCase());
        this.maxElementsOnDisk = Integer.parseInt(Preconditions.checkNotNull(
            root.getChildText("maxElementsOnDisk"), "MaxElementsOnDisk"));
        this.diskSpoolBufferSizeMB = Integer.parseInt(Preconditions.checkNotNull(
            root.getChildText("diskSpoolBufferSize"), "DiskSpoolBufferSize"));
        this.clearOnFlush = Boolean.parseBoolean(Preconditions.checkNotNull(
            root.getChildText("clearOnFlush"), "ClearOnFlush"));
        this.isTerracottaClustered = Boolean.parseBoolean(Preconditions.checkNotNull(
            root.getChildText("isTerracottaClustered"), "IsTerracottaClustered"));
        this.terracottaValueMode = Preconditions.checkNotNull(
            root.getChildText("terracottaValueMode"), "TerracottaValueMode");
        this.terracottaCoherentReads = Boolean.parseBoolean(Preconditions.checkNotNull(
            root.getChildText("terracottaCoherentReads"), "TerracottaCoherentReads"));

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
        manager = CacheManager.create();
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
        final net.sf.ehcache.Element element = new net.sf.ehcache.Element(key, value);
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
    public void shutdown() {
        manager.shutdown();
    }
    
}
