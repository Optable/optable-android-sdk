/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.android_sdk

import android.net.Uri
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

    @Test
    fun eidFromURI_isCorrect() {
        val url = "http://some.domain.com/some/path?some=query&something=else&oeid=a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3&foo=bar&baz"
        val expected = "e:a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3"

        assertEquals(expected, OptableSDK.eidFromURI(Uri.parse(url)))
    }

    @Test
    fun eidFromURI_returnsEmptyWhenOeidAbsentFromQuerystr() {
        val url = "http://some.domain.com/some/path?some=query&something=else"
        val expected = ""

        assertEquals(expected, OptableSDK.eidFromURI(Uri.parse(url)))
    }

    @Test
    fun eidFromURI_returnsEmptyWhenQuerystrAbsent() {
        val url = "http://some.domain.com/some/path"
        val expected = ""

        assertEquals(expected, OptableSDK.eidFromURI(Uri.parse(url)))
    }

    @Test
    fun eidFromURI_returnsEmptyWhenInputEmptyString() {
        val url = ""
        val expected = ""

        assertEquals(expected, OptableSDK.eidFromURI(Uri.parse(url)))
    }

    @Test
    fun eidFromURI_expectsSHA256() {
        val url = "http://some.domain.com/some/path?some=query&something=else&oeid=AAAAAAAa665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3&foo=bar&baz"
        val expected = ""

        assertEquals(expected, OptableSDK.eidFromURI(Uri.parse(url)))
    }

    @Test
    fun eidFromURI_ignoresCase() {
        val url = "http://some.domain.com/some/path?some=query&something=else&oEId=A665A45920422F9D417E4867EFDC4FB8A04A1F3FFF1FA07E998E86f7f7A27AE3&foo=bar&baz"
        val expected = "e:a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3"

        assertEquals(expected, OptableSDK.eidFromURI(Uri.parse(url)))
    }
}