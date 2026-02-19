package co.optable.sdk.core

import co.optable.sdk.OptableIdentifier
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class IdentifiersEncoderTest {

    private lateinit var identifiersEncoder: IdentifiersEncoder
    private lateinit var mockGoogleAdIdManager: GoogleAdIdManager

    @Before
    fun setUp() {
        mockGoogleAdIdManager = mock<GoogleAdIdManager>()
        identifiersEncoder = IdentifiersEncoder(mockGoogleAdIdManager)
    }

    @Test
    fun eid_isCorrect() {
        val expected = "e:a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3"

        assertEquals(expected, encode(OptableIdentifier.Email("123")))
        assertEquals(expected, encode(OptableIdentifier.Email(" 123")))
        assertEquals(expected, encode(OptableIdentifier.Email("123 ")))
        assertEquals(expected, encode(OptableIdentifier.Email(" 123 ")))
    }

    @Test
    fun `phoneNumber is encrypted`() {
        val expected = "e:97ed57b6d666c803e1d0a74b0e3ecc5157f23c02800d007f2e4b8b2fabea8dbb"

        assertEquals(expected, encode(OptableIdentifier.Email("+123467890")))
        assertEquals(expected, encode(OptableIdentifier.Email(" +123467890")))
        assertEquals(expected, encode(OptableIdentifier.Email("+123467890 ")))
        assertEquals(expected, encode(OptableIdentifier.Email(" +123467890 ")))
    }

    @Test
    fun eid_ignoresCase() {
        val var1 = "tEsT@FooBarBaz.CoM"
        val var2 = "test@foobarbaz.com"
        val var3 = "TEST@FOOBARBAZ.COM"
        val var4 = "TeSt@fOObARbAZ.cOm"
        val eid = encode(OptableIdentifier.Email(var1))

        assertEquals(eid, encode(OptableIdentifier.Email(var2)))
        assertEquals(eid, encode(OptableIdentifier.Email(var3)))
        assertEquals(eid, encode(OptableIdentifier.Email(var4)))
    }

    @Test
    fun gaid_isCorrectAndIgnoresCase() {
        val expected = "g:38400000-8cf0-11bd-b23e-10b96e40000d"

        assertEquals(expected, encode(OptableIdentifier.GoogleGaid("38400000-8cf0-11bd-b23e-10b96e40000d")))
        assertEquals(expected, encode(OptableIdentifier.GoogleGaid("  38400000-8cf0-11bd-b23e-10b96e40000d")))
        assertEquals(expected, encode(OptableIdentifier.GoogleGaid("38400000-8cf0-11bd-b23e-10b96e40000d  ")))
        assertEquals(expected, encode(OptableIdentifier.GoogleGaid("  38400000-8cf0-11bd-b23e-10b96e40000d  ")))
        assertEquals(expected, encode(OptableIdentifier.GoogleGaid("38400000-8CF0-11BD-B23E-10B96E40000D")))
    }

    @Test
    fun cid_isCorrect() {
        val expected = "c:FooBarBAZ-01234#98765.!!!"


        assertEquals(expected, encode(OptableIdentifier.Custom("c", "FooBarBAZ-01234#98765.!!!")))
        assertEquals(expected, encode(OptableIdentifier.Custom("c", " FooBarBAZ-01234#98765.!!!")))
        assertEquals(expected, encode(OptableIdentifier.Custom("c", "FooBarBAZ-01234#98765.!!!  ")))
        assertEquals(expected, encode(OptableIdentifier.Custom("c", "  FooBarBAZ-01234#98765.!!!  ")))
    }

    @Test
    fun cid_isCaseSensitive() {
        val unexpected = "c:FooBarBAZ-01234#98765.!!!"

        val actual = encode(OptableIdentifier.Custom("c", "foobarBAZ-01234#98765.!!!"))
        assertNotEquals(unexpected, actual)
    }

    @Test
    fun prefixedIdFromUrl_isCorrect() {
        val url =
            "http://some.domain.com/some/path?some=query&something=else&oeid=a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3&foo=bar&baz"
        val expected = "e:a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3"

        assertEquals(expected, identifiersEncoder.prefixedIdFromUrl(url))
    }

    @Test
    fun prefixedIdFromUrl_returnsEmptyWhenOeidAbsentFromQuerystr() {
        val url = "http://some.domain.com/some/path?some=query&something=else"

        assertNull(identifiersEncoder.prefixedIdFromUrl(url))
    }

    @Test
    fun eidFromURI_returnsEmptyWhenQuerystrAbsent() {
        val url = "http://some.domain.com/some/path"

        assertNull(identifiersEncoder.prefixedIdFromUrl(url))
    }

    @Test
    fun eidFromURI_returnsEmptyWhenInputEmptyString() {
        val url = ""

        assertNull(identifiersEncoder.prefixedIdFromUrl(url))
    }

    @Test
    fun eidFromURI_expectsSHA256() {
        val url =
            "http://some.domain.com/some/path?some=query&something=else&oeid=AAAAAAAa665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3&foo=bar&baz"

        assertNull(identifiersEncoder.prefixedIdFromUrl(url))
    }

    @Test
    fun eidFromURI_ignoresCase() {
        val url =
            "http://some.domain.com/some/path?some=query&something=else&oEId=A665A45920422F9D417E4867EFDC4FB8A04A1F3FFF1FA07E998E86f7f7A27AE3&foo=bar&baz"
        val expected = "e:a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3"

        val actual = identifiersEncoder.prefixedIdFromUrl(url)
        assertEquals(expected, actual)
    }

    private fun encode(ids: OptableIdentifier): String {
        val generatedIds = identifiersEncoder.encode(listOf(ids))
        assertEquals("This method checks only 1 string", 1, generatedIds.size)
        return generatedIds.first()
    }

}
