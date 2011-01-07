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

import net.sf.ehcache.Status;

import java.util.concurrent.TimeUnit;

/**
 * Tests the {@link EhCacheService}.
 *
 * @author Markus Baumann
 * @author Oliver Lorenz
 */
public class EhCacheServiceTest extends CacheServiceTest {

    @Override
    protected long lifeTime() {
        return 2;
    }

    @Override
    protected long idleTime() {
        return 2;
    }

    @Override
    protected long sleepBeforeIdleTime() {
        return 1;
    }

    @Override
    protected long sleepTimeExpires() {
        return 3;
    }

    @Override
    protected TimeUnit timeUnit() {
        return TimeUnit.SECONDS;
    }

    @Override
    public CacheService unit() {
        final EhCacheService service = new EhCacheService();
        service.setTimeToIdle(1);
        service.setTimeToIdleUnit(TimeUnit.SECONDS);
        service.setTimeToLive(1);
        service.setTimeToLiveUnit(TimeUnit.SECONDS);
        service.initialize();
        
        if (service.getCache().getStatus().equals(Status.STATUS_UNINITIALISED)) {
            service.getCache().initialise();
        }
        return service;
    }
}
