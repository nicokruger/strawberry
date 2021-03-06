/**
 * Strawberry Library
 * Copyright (C) 2011 - 2012
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package com.github.strawberry.guice;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import static com.github.strawberry.util.JedisUtil.destroyOnShutdown;

/**
 *
 * @author Wiehann Matthysen
 */
public class LongInjectionTest extends AbstractModule {

    private final JedisPool pool = destroyOnShutdown(new JedisPool("localhost", 6379));

    private Injector injector;
    private Jedis jedis;

    @Override
    protected void configure() {
        install(new RedisModule(this.pool));
    }

    @Before
    public void setup() {
        this.injector = Guice.createInjector(this);
        this.jedis = this.pool.getResource();
    }

    @After
    public void teardown() {
        for (String key : this.jedis.keys("test:*")) {
            this.jedis.del(key);
        }
        this.pool.returnResource(this.jedis);
    }



    public static class PrimitiveLongContainer {

        @Redis(value = "test:long", allowNull = false)
        private long injectedLong;

        public long getInjectedLong() {
            return this.injectedLong;
        }
    }

    public static class PrimitiveLongAllowNullContainer {

        @Redis(value = "test:long", forceUpdate = true)
        private long injectedLong;

        public long getInjectedLong() {
            return this.injectedLong;
        }
    }
    
    public static class PrimitiveLongDefaultValueContainer {

        @Redis("test:long")
        private long injectedLong = 123L;

        public long getInjectedLong() {
            return this.injectedLong;
        }
    }

    @Test
    public void test_that_string_is_converted_into_primitive_long() {
        this.jedis.set("test:long", "123");
        PrimitiveLongContainer dummy = this.injector.getInstance(PrimitiveLongContainer.class);
        assertThat(dummy.getInjectedLong(), is(123L));
        this.jedis.set("test:long", "-123");
        dummy = this.injector.getInstance(PrimitiveLongContainer.class);
        assertThat(dummy.getInjectedLong(), is(-123L));
    }

    @Test(expected = RuntimeException.class)
    public void test_that_missing_value_causes_exception_when_setting_primitive_long_to_null() {
        this.injector.getInstance(PrimitiveLongAllowNullContainer.class);
    }

    @Test
    public void test_that_missing_value_is_injected_as_zero_into_primitive_long() {
        PrimitiveLongContainer dummy = this.injector.getInstance(
            PrimitiveLongContainer.class);
        assertThat(dummy.getInjectedLong(), is(0L));
    }
    
    @Test
    public void test_that_missing_value_causes_default_value_to_be_set_for_primitive_long() {
        // Test for case where no value is present in redis database.
        PrimitiveLongDefaultValueContainer dummy = this.injector.getInstance(
            PrimitiveLongDefaultValueContainer.class);
        assertThat(dummy.getInjectedLong(), is(123L));
        
        // Test for case where value is present in redis database.
        // Default value should be overwritten.
        this.jedis.set("test:long", "456");
        dummy = this.injector.getInstance(PrimitiveLongDefaultValueContainer.class);
        assertThat(dummy.getInjectedLong(), is(456L));
    }

    @Test(expected = RuntimeException.class)
    public void test_that_invalid_string_throws_exception_when_converting_to_primitive_long() {
        this.jedis.set("test:long", "invalid");
        this.injector.getInstance(PrimitiveLongContainer.class);
    }

    @Test(expected = RuntimeException.class)
    public void test_that_too_small_value_throws_exception_when_converting_to_primitive_long() {
        this.jedis.set("test:long", "-9223372036854775809");
        this.injector.getInstance(PrimitiveLongContainer.class);
    }

    @Test(expected = RuntimeException.class)
    public void test_that_too_large_value_throws_exception_when_converting_to_primitive_long() {
        this.jedis.set("test:long", "9223372036854775808");
        this.injector.getInstance(PrimitiveLongContainer.class);
    }



    public static class LongContainer {

        @Redis(value = "test:long", allowNull = false)
        private Long injectedLong;

        public Long getInjectedLong() {
            return this.injectedLong;
        }
    }

    public static class LongAllowNullContainer {

        @Redis("test:long")
        private Long injectedLong;

        public Long getInjectedLong() {
            return this.injectedLong;
        }
    }
    
    public static class LongDefaultValueContainer {

        @Redis("test:long")
        private Long injectedLong = 123L;

        public Long getInjectedLong() {
            return this.injectedLong;
        }
    }

    @Test
    public void test_that_string_is_converted_into_long() {
        this.jedis.set("test:long", "123");
        LongContainer dummy = this.injector.getInstance(LongContainer.class);
        assertThat(dummy.getInjectedLong(), is(123L));
        this.jedis.set("test:long", "-123");
        dummy = this.injector.getInstance(LongContainer.class);
        assertThat(dummy.getInjectedLong(), is(-123L));
    }

    @Test
    public void test_that_missing_value_is_injected_as_null_into_long() {
        LongAllowNullContainer dummy = this.injector.getInstance(
            LongAllowNullContainer.class);
        assertThat(dummy.getInjectedLong(), is(nullValue()));
    }

    @Test
    public void test_that_missing_value_is_injected_as_zero_into_long() {
        LongContainer dummy = this.injector.getInstance(LongContainer.class);
        assertThat(dummy.getInjectedLong(), is(0L));
    }
    
    @Test
    public void test_that_missing_value_causes_default_value_to_be_set_for_long() {
        // Test for case where no value is present in redis database.
        LongDefaultValueContainer dummy = this.injector.getInstance(
            LongDefaultValueContainer.class);
        assertThat(dummy.getInjectedLong(), is(123L));
        
        // Test for case where value is present in redis database.
        // Default value should be overwritten.
        this.jedis.set("test:long", "456");
        dummy = this.injector.getInstance(LongDefaultValueContainer.class);
        assertThat(dummy.getInjectedLong(), is(456L));
    }

    @Test(expected = RuntimeException.class)
    public void test_that_invalid_string_throws_exception_when_converting_to_long() {
        this.jedis.set("test:long", "invalid");
        this.injector.getInstance(LongContainer.class);
    }

    @Test(expected = RuntimeException.class)
    public void test_that_too_small_value_throws_exception_when_converting_to_long() {
        this.jedis.set("test:long", "-9223372036854775809");
        this.injector.getInstance(LongContainer.class);
    }

    @Test(expected = RuntimeException.class)
    public void test_that_too_large_value_throws_exception_when_converting_to_long() {
        this.jedis.set("test:long", "9223372036854775808");
        this.injector.getInstance(LongContainer.class);
    }
}
