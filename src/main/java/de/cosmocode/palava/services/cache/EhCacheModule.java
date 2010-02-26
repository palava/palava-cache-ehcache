package de.cosmocode.palava.services.cache;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * <p> Binds the EhCacheService to the {@link CacheService}.
 * </p>
 * <p> The following parameters must be set for the EhCacheService.
 * </p>
 * <ul>
 *   <li>ehcache.overflowToDisk (boolean, if true, cache data flows from memory to disk)</li>
 *   <li>ehcache.eternal (boolean, if true then cached data live eternal)</li>
 * </ul>
 * <p> Optional parameters are:
 * </p>
 * <ul>
 *   <li>ehcache.clearOnFlush (boolean)</li>
 *   <li>ehcache.diskExpiryThreadInterval (long)</li>
 *   <li>ehcache.diskExpiryThreadIntervalUnit (TimeUnit)</li>
 *   <li>ehcache.diskPersistent (boolean)</li>
 *   <li>ehcache.diskStorePath (file path)</li>
 *   <li>ehcache.diskSpoolBufferSizeMB (int)</li>
 *   <li>ehcache.maxElementsInMemory (int)</li>
 *   <li>ehcache.maxElementsOnDisk (int)</li>
 *   <li>ehcache.cacheMode (one of LRU, LFU, FIFO)</li>
 *   <li>ehcache.isTerracottaClustered (boolean)</li>
 *   <li>ehcache.terracottaValueMode (one of SERIALIZATION, IDENTITY)</li>
 *   <li>ehcache.terracottaCoherentReads (boolean)</li>
 *   <li>ehcache.timeToIdle (long)</li>
 *   <li>ehcache.timeToIdleUnit (TimeUnit)</li>
 *   <li>ehcache.timeToLive (long)</li>
 *   <li>ehcache.timeToLiveUnit (TimeUnit)</li>
 * </ul>
 * 
 * @author Oliver Lorenz
 */
public class EhCacheModule implements Module {
    
    @Override
    public void configure(final Binder binder) {
        binder.bind(CacheService.class).to(EhCacheService.class);
    }

}
