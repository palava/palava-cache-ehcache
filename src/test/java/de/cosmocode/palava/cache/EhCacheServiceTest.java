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

import java.net.URL;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Status;

import org.junit.Test;

/**
 * Tests the {@link EhCacheService}.
 *
 * @author Markus Baumann
 * @author Oliver Lorenz
 */
public class EhCacheServiceTest extends CacheServiceTest {

    @Override
    public CacheService unit() {
        final EhCacheService service = new EhCacheService("testcache");
        service.setTimeToIdle(1);
        service.setTimeToIdleUnit(TimeUnit.SECONDS);
        service.setTimeToLive(1);
        service.setTimeToLiveUnit(TimeUnit.SECONDS);
        service.initialize();

        return service;
    }

    @Test
    public void fromFile() {
        // create one service configured from xml
        final EhCacheService fromXml = new EhCacheService("testFromXml");
        fromXml.initialize();
        fromXml.store("bla", "blubb");
        final String actual = fromXml.read("bla");
        Assert.assertEquals("blubb", actual);
    }

    /**
     * Tests every optional setter. Should throw no exception afterwards.
     */
    @Test
    public void setOptionals() {
        final EhCacheService service = new EhCacheService("configuredtestcache");
        service.setClearOnFlush(true);
        service.setDiskExpiryThreadInterval(10);
        service.setDiskExpiryThreadIntervalUnit(TimeUnit.MINUTES);
        service.setDiskPersistent(false);
        service.setDiskSpoolBufferSizeMB(50);
        service.setDiskStorePath("/tmp");
        service.setEternal(false);
        service.setMaxElementsOnDisk(2000);
        service.setMemoryStoreEvictionPolicy(CacheMode.FIFO);
        service.setOverflowToDisk(false);
        service.setTerracottaClustered(false);
        service.setTerracottaCoherentReads(false);
        service.setTerracottaValueMode("IDENTITY");
        service.setTimeToIdle(1);
        service.setTimeToIdleUnit(TimeUnit.SECONDS);
        service.setTimeToLive(1);
        service.setTimeToLiveUnit(TimeUnit.SECONDS);
        service.initialize();
    }
}
