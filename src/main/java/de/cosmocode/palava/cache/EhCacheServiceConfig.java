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

/**
 * Config class for EhCache.
 * TODO: make configurable for "ehcache.(name).*"
 *
 * @author Oliver Lorenz
 */
final class EhCacheServiceConfig {

    public static final String PREFIX = "ehcache.";
    
    public static final String NAME = PREFIX + "name";
    
    public static final String ETERNAL = PREFIX + "eternal";
    
    public static final String OVERFLOW_TO_DISK = PREFIX + "overflowToDisk";
    
    public static final String CLEAR_ON_FLUSH = PREFIX + "clearOnFlush";
    
    public static final String DISK_EXPIRY_THREAD_INTERVAL = PREFIX + "diskExpiryThreadInterval";
    
    public static final String DISK_EXPIRY_THREAD_INTERVAL_UNIT = PREFIX + "diskExpiryThreadIntervalUnit";
    
    public static final String DISK_PERSISTENT = PREFIX + "diskPersistent";
    
    public static final String DISK_STORE_PATH = PREFIX + "diskStorePath";
    
    public static final String DISK_SPOOL_BUFFER_SIZE_MB = PREFIX + "diskSpoolBufferSizeMB";
    
    public static final String MAX_ELEMENTS_IN_MEMORY = PREFIX + "maxElementsInMemory";
    
    public static final String MAX_ELEMENTS_ON_DISK = PREFIX + "maxElementsOnDisk";
    
    public static final String CACHE_MODE = PREFIX + "cacheMode";
    
    public static final String IS_TERRACOTTA_CLUSTERED = PREFIX + "isTerracottaClustered";
    
    public static final String TERRACOTTA_VALUE_MODE = PREFIX + "terracottaValueMode";
    
    public static final String TERRACOTTA_COHERENT_READS = PREFIX + "terracottaCoherentReads";
    
    public static final String TIME_TO_IDLE = PREFIX + "timeToIdle";
    
    public static final String TIME_TO_IDLE_UNIT = PREFIX + "timeToIdleUnit";
    
    public static final String TIME_TO_LIVE = PREFIX + "timeToLive";
    
    public static final String TIME_TO_LIVE_UNIT = PREFIX + "timeToLiveUnit";
    
    private EhCacheServiceConfig() {
        
    }

}
