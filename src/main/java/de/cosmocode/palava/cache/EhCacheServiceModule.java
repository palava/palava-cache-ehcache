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

import java.lang.annotation.Annotation;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import de.cosmocode.palava.core.inject.AbstractRebindModule;
import de.cosmocode.palava.core.inject.Config;
import de.cosmocode.palava.core.inject.RebindModule;

/**
 * <p> Binds the EhCacheService to the {@link CacheService}.
 * </p>
 * <p> Look at the constructor ({@link #EhCacheServiceModule()})
 * for all configuration parameters.
 * </p>
 * <p> If you want to use several CacheServices you can use binding annotations with 
 * the static method {@link #annotatedWith(Class, String)}
 * and annotate each and every use of CacheService with these annotations.
 * </p>
 * 
 * @author Oliver Lorenz
 */
public final class EhCacheServiceModule implements Module {
    
    /**
     * Binds the EhCacheService to the {@link CacheService}.
     * 
     * <p>The following parameters must be set for the EhCacheService.</p>
     * <ul>
     *   <li>cache.ehcache.overflowToDisk (boolean, if true, cache data flows from memory to disk)</li>
     *   <li>cache.ehcache.eternal (boolean, if true then cached data live eternal)</li>
     * </ul>
     * <p>Optional parameters are:</p>
     * <ul>
     *   <li>cache.ehcache.clearOnFlush (boolean)</li>
     *   <li>cache.ehcache.diskExpiryThreadInterval (long)</li>
     *   <li>cache.ehcache.diskExpiryThreadIntervalUnit (TimeUnit)</li>
     *   <li>cache.ehcache.diskPersistent (boolean)</li>
     *   <li>cache.ehcache.diskStorePath (file path)</li>
     *   <li>cache.ehcache.diskSpoolBufferSizeMB (int)</li>
     *   <li>cache.ehcache.maxElementsInMemory (int)</li>
     *   <li>cache.ehcache.maxElementsOnDisk (int)</li>
     *   <li>cache.ehcache.cacheMode (one of LRU, LFU, FIFO)</li>
     *   <li>cache.ehcache.isTerracottaClustered (boolean)</li>
     *   <li>cache.ehcache.terracottaValueMode (one of SERIALIZATION, IDENTITY)</li>
     *   <li>cache.ehcache.terracottaCoherentReads (boolean)</li>
     *   <li>cache.ehcache.timeToIdle (long)</li>
     *   <li>cache.ehcache.timeToIdleUnit (TimeUnit)</li>
     *   <li>cache.ehcache.timeToLive (long)</li>
     *   <li>cache.ehcache.timeToLiveUnit (TimeUnit)</li>
     * </ul>
     */
    public EhCacheServiceModule() {
        
    }
    
    @Override
    public void configure(final Binder binder) {
        binder.bind(CacheService.class).to(EhCacheService.class).in(Singleton.class);
    }
    
    /**
     * Creates a {@link RebindModule} which can be used to register one or more eh caches.
     * 
     * @since 2.0
     * @param annotation the binding annotation
     * @param prefix the configuration prefix/name
     * @return a new {@link RebindModule}
     */
    public static RebindModule annotatedWith(Class<? extends Annotation> annotation, String prefix) {
        Preconditions.checkNotNull(annotation, "Annotation");
        Preconditions.checkNotNull(prefix, "Prefix");
        return new AnnotatedModule(annotation, prefix);
    }
    
    /**
     * Internal {@link RebindModule} implementation.
     *
     * @since 2.0
     * @author Willi Schoenborn
     */
    private static final class AnnotatedModule extends AbstractRebindModule {
        
        private final Class<? extends Annotation> annotation;
        private final String name;
        private final Config config;
        
        public AnnotatedModule(Class<? extends Annotation> annotation, String prefix) {
            this.annotation = annotation;
            this.name = prefix;
            this.config = new Config(prefix);
        }
        
        @Override
        protected void configuration() {
            bind(String.class).annotatedWith(Names.named(EhCacheServiceConfig.NAME)).toInstance(name);
            
            bind(boolean.class).annotatedWith(Names.named(EhCacheServiceConfig.OVERFLOW_TO_DISK)).to(
                Key.get(boolean.class, Names.named(config.prefixed(EhCacheServiceConfig.OVERFLOW_TO_DISK))));
            
            bind(boolean.class).annotatedWith(Names.named(EhCacheServiceConfig.ETERNAL)).to(
                Key.get(boolean.class, Names.named(config.prefixed(EhCacheServiceConfig.ETERNAL))));
        }
        
        @Override
        protected void optionals() {
            bind(boolean.class).annotatedWith(Names.named(EhCacheServiceConfig.CLEAR_ON_FLUSH)).to(
                Key.get(boolean.class, Names.named(config.prefixed(EhCacheServiceConfig.CLEAR_ON_FLUSH))));

            bind(long.class).annotatedWith(Names.named(EhCacheServiceConfig.DISK_EXPIRY_THREAD_INTERVAL)).to(
                Key.get(long.class, Names.named(config.prefixed(EhCacheServiceConfig.DISK_EXPIRY_THREAD_INTERVAL))));

            bind(TimeUnit.class).annotatedWith(Names.named(EhCacheServiceConfig.DISK_EXPIRY_THREAD_INTERVAL_UNIT)).to(
                Key.get(TimeUnit.class, Names.named(config.prefixed(
                    EhCacheServiceConfig.DISK_EXPIRY_THREAD_INTERVAL_UNIT))));

            bind(boolean.class).annotatedWith(Names.named(EhCacheServiceConfig.DISK_PERSISTENT)).to(
                Key.get(boolean.class, Names.named(config.prefixed(EhCacheServiceConfig.DISK_PERSISTENT))));

            bind(String.class).annotatedWith(Names.named(EhCacheServiceConfig.DISK_STORE_PATH)).to(
                Key.get(String.class, Names.named(config.prefixed(EhCacheServiceConfig.DISK_STORE_PATH))));

            bind(int.class).annotatedWith(Names.named(EhCacheServiceConfig.DISK_SPOOL_BUFFER_SIZE_MB)).to(
                Key.get(int.class, Names.named(config.prefixed(EhCacheServiceConfig.DISK_SPOOL_BUFFER_SIZE_MB))));

            bind(int.class).annotatedWith(Names.named(EhCacheServiceConfig.MAX_ELEMENTS_IN_MEMORY)).to(
                Key.get(int.class, Names.named(config.prefixed(EhCacheServiceConfig.MAX_ELEMENTS_IN_MEMORY))));

            bind(int.class).annotatedWith(Names.named(EhCacheServiceConfig.MAX_ELEMENTS_ON_DISK)).to(
                Key.get(int.class, Names.named(config.prefixed(EhCacheServiceConfig.MAX_ELEMENTS_ON_DISK))));

            bind(CacheMode.class).annotatedWith(Names.named(EhCacheServiceConfig.CACHE_MODE)).to(
                Key.get(CacheMode.class, Names.named(config.prefixed(EhCacheServiceConfig.CACHE_MODE))));

            bind(boolean.class).annotatedWith(Names.named(EhCacheServiceConfig.IS_TERRACOTTA_CLUSTERED)).to(
                Key.get(boolean.class, Names.named(config.prefixed(EhCacheServiceConfig.IS_TERRACOTTA_CLUSTERED))));

            bind(String.class).annotatedWith(Names.named(EhCacheServiceConfig.TERRACOTTA_VALUE_MODE)).to(
                Key.get(String.class, Names.named(config.prefixed(EhCacheServiceConfig.TERRACOTTA_VALUE_MODE))));

            bind(boolean.class).annotatedWith(Names.named(EhCacheServiceConfig.TERRACOTTA_COHERENT_READS)).to(
                Key.get(boolean.class, Names.named(config.prefixed(EhCacheServiceConfig.TERRACOTTA_COHERENT_READS))));

            bind(long.class).annotatedWith(Names.named(EhCacheServiceConfig.TIME_TO_IDLE)).to(
                Key.get(long.class, Names.named(config.prefixed(EhCacheServiceConfig.TIME_TO_IDLE))));

            bind(TimeUnit.class).annotatedWith(Names.named(EhCacheServiceConfig.TIME_TO_IDLE_UNIT)).to(
                Key.get(TimeUnit.class, Names.named(config.prefixed(EhCacheServiceConfig.TIME_TO_IDLE_UNIT))));

            bind(long.class).annotatedWith(Names.named(EhCacheServiceConfig.TIME_TO_LIVE)).to(
                Key.get(long.class, Names.named(config.prefixed(EhCacheServiceConfig.TIME_TO_LIVE))));

            bind(TimeUnit.class).annotatedWith(Names.named(EhCacheServiceConfig.TIME_TO_LIVE_UNIT)).to(
                Key.get(TimeUnit.class, Names.named(config.prefixed(EhCacheServiceConfig.TIME_TO_LIVE_UNIT))));
        }
        
        @Override
        protected void bindings() {
            bind(CacheService.class).annotatedWith(annotation).to(EhCacheService.class).in(Singleton.class);
        }

        @Override
        protected void expose() {
            expose(CacheService.class).annotatedWith(annotation);
        }
        
    }

}
