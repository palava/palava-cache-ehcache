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
