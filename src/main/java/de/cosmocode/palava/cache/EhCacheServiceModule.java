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

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;

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
public final class EhCacheServiceModule implements Module {
    
    @Override
    public void configure(final Binder binder) {
        binder.bind(CacheService.class).to(EhCacheService.class).in(Singleton.class);
    }

}
