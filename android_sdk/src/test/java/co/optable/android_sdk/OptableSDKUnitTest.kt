/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.android_sdk

import org.junit.Test
import org.junit.Assert.*

/**
 * OptableSDK unit tests
 */
class OptableSDKUnitTest {
    @Test
    fun eid_isCorrect() {
        val expected = "e:a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3"

        assertEquals(expected, OptableSDK.eid("123"))
        assertEquals(expected, OptableSDK.eid(" 123"))
        assertEquals(expected, OptableSDK.eid("123 "))
        assertEquals(expected, OptableSDK.eid(" 123 "))
    }

    @Test
    fun eid_ignoresCase() {
        val var1 = "tEsT@FooBarBaz.CoM"
        val var2 = "test@foobarbaz.com"
        val var3 = "TEST@FOOBARBAZ.COM"
        val var4 = "TeSt@fOObARbAZ.cOm"
        val eid = OptableSDK.eid(var1)

        assertEquals(eid, OptableSDK.eid(var2))
        assertEquals(eid, OptableSDK.eid(var3))
        assertEquals(eid, OptableSDK.eid(var4))
    }

    @Test
    fun gaid_isCorrectAndIgnoresCase() {
        val expected = "g:38400000-8cf0-11bd-b23e-10b96e40000d"

        assertEquals(expected, OptableSDK.gaid("38400000-8cf0-11bd-b23e-10b96e40000d"))
        assertEquals(expected, OptableSDK.gaid("  38400000-8cf0-11bd-b23e-10b96e40000d"))
        assertEquals(expected, OptableSDK.gaid("38400000-8cf0-11bd-b23e-10b96e40000d  "))
        assertEquals(expected, OptableSDK.gaid("  38400000-8cf0-11bd-b23e-10b96e40000d  "))
        assertEquals(expected, OptableSDK.gaid("38400000-8CF0-11BD-B23E-10B96E40000D"))
    }

    @Test
    fun cid_isCorrect() {
        val expected = "c:FooBarBAZ-01234#98765.!!!"

        assertEquals(expected, OptableSDK.cid("FooBarBAZ-01234#98765.!!!"))
        assertEquals(expected, OptableSDK.cid(" FooBarBAZ-01234#98765.!!!"))
        assertEquals(expected, OptableSDK.cid("FooBarBAZ-01234#98765.!!!  "))
        assertEquals(expected, OptableSDK.cid("  FooBarBAZ-01234#98765.!!!  "))
    }

    @Test
    fun cid_isCaseSensitive() {
        val unexpected = "c:FooBarBAZ-01234#98765.!!!"

        assertNotEquals(unexpected, OptableSDK.cid("foobarBAZ-01234#98765.!!!"))
    }
}