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
}