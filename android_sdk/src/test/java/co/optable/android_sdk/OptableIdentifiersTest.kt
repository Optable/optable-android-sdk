package co.optable.android_sdk

import co.optable.android_sdk.core.GoogleAdIdManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import java.security.MessageDigest
import java.util.*

class OptableIdentifiersTest {

    @After
    fun tearDown() {
        OptableIdentifiers.receiveGaidAutomatically = true
    }

    @Test
    fun `generateEIDs empty`() {
        val ids = OptableIdentifiers()
        val actual = ids.generateEIDs()
        assertEquals(emptyList<String>(), actual)
    }

    @Test
    fun `generateEIDs email`() {
        val ids = OptableIdentifiers(email = "  John.DOE+test@example.COM  ")
        val actual = ids.generateEIDs()
        val expected = listOf("e:${sha256(normalize("  John.DOE+test@example.COM  "))}")
        assertEquals(expected, actual)
    }

    @Test
    fun `generateEIDs phoneNumber`() {
        val ids = OptableIdentifiers(phoneNumber = " +1  (555)  123  45 67 ")
        val actual = ids.generateEIDs()
        val expected = listOf("p:${sha256(normalize(" +1  (555)  123  45 67 "))}")
        assertEquals(expected, actual)
    }

    @Test
    fun `generateEIDs postalCode`() {
        val ids = OptableIdentifiers(postalCode = " 12 3 45 ")
        val actual = ids.generateEIDs()
        val expected = listOf("z:${normalize(" 12 3 45 ")}")
        assertEquals(expected, actual)
    }

    @Test
    fun `generateEIDs ipv4Address`() {
        val ids = OptableIdentifiers(ipv4Address = " 192.168. 0. 1 ")
        val actual = ids.generateEIDs()
        val expected = listOf("i4:${removeWhitespaces(" 192.168. 0. 1 ")}")
        assertEquals(expected, actual)
    }

    @Test
    fun `generateEIDs ipv6Address`() {
        val ids = OptableIdentifiers(ipv6Address = " 2001:DB8:: 1 ")
        val actual = ids.generateEIDs()
        val expected = listOf("i6:${normalize(" 2001:DB8:: 1 ")}")
        assertEquals(expected, actual)
    }

    @Test
    fun `generateEIDs appleIdfa`() {
        val ids = OptableIdentifiers(appleIdfa = " A1B2C3D4-E5F6-7890-ABCD-EF0123456789 ")
        val actual = ids.generateEIDs()
        val expected = listOf("a:${normalize(" A1B2C3D4-E5F6-7890-ABCD-EF0123456789 ")}")
        assertEquals(expected, actual)
    }

    @Test
    fun `generateEIDs googleGaid`() {
        val ids = OptableIdentifiers(googleGaid = " 38400000-8cf0-11bd-b23e-10b96e40000d ")
        val actual = ids.generateEIDs()
        val expected = listOf("g:${normalize(" 38400000-8cf0-11bd-b23e-10b96e40000d ")}")
        assertEquals(expected, actual)
    }

    @Test
    fun `generateEIDs rokuRida`() {
        val ids = OptableIdentifiers(rokuRida = " ROKU-RIDA- 123 ")
        val actual = ids.generateEIDs()
        val expected = listOf("r:${normalize(" ROKU-RIDA- 123 ")}")
        assertEquals(expected, actual)
    }

    @Test
    fun `generateEIDs samsungTifa`() {
        val ids = OptableIdentifiers(samsungTifa = " TIFA  ABC ")
        val actual = ids.generateEIDs()
        val expected = listOf("s:${normalize(" TIFA  ABC ")}")
        assertEquals(expected, actual)
    }

    @Test
    fun `generateEIDs amazonFireAfai`() {
        val ids = OptableIdentifiers(amazonFireAfai = " AFAI  XYZ ")
        val actual = ids.generateEIDs()
        val expected = listOf("f:${normalize(" AFAI  XYZ ")}")
        assertEquals(expected, actual)
    }

    @Test
    fun `generateEIDs netId`() {
        val ids = OptableIdentifiers(netId = " net id  123 ")
        val actual = ids.generateEIDs()
        val expected = listOf("n:${removeWhitespaces(" net id  123 ")}")
        assertEquals(expected, actual)
    }

    @Test
    fun `generateEIDs id5`() {
        val ids = OptableIdentifiers(id5 = " id5   token ")
        val actual = ids.generateEIDs()
        val expected = listOf("id5:${removeWhitespaces(" id5   token ")}")
        assertEquals(expected, actual)
    }

    @Test
    fun `generateEIDs utiq`() {
        val ids = OptableIdentifiers(utiq = " UTIQ  VALUE ")
        val actual = ids.generateEIDs()
        val expected = listOf("utiq:${normalize(" UTIQ  VALUE ")}")
        assertEquals(expected, actual)
    }

    @Test
    fun `generateEIDs custom vid`() {
        val ids = OptableIdentifiers(custom = mapOf("v" to "  vid value  "))
        val actual = ids.generateEIDs()
        val expected = listOf("v:${removeWhitespaces("  vid value  ")}")
        assertEquals(expected, actual)
    }

    @Test
    fun `generateEIDs custom nonVid`() {
        val ids = OptableIdentifiers(custom = mapOf("c1" to "  custom one  "))
        val actual = ids.generateEIDs()
        val expected = listOf("c1:${"  custom one  ".trim()}")
        assertEquals(expected, actual)
    }

    @Test
    fun `generateEIDs raw`() {
        val ids = OptableIdentifiers(raw = listOf("raw:alreadyEncoded", "custom:any"))
        val actual = ids.generateEIDs()
        val expected = listOf("raw:alreadyEncoded", "custom:any")
        assertEquals(expected, actual)
    }

    @Test
    fun `generateEIDs adds GAID from GoogleAdIdManager when googleGaid is null and receiveGaidAutomatically is true`() {
        val previousAdId = GoogleAdIdManager.adId
        try {
            GoogleAdIdManager.adId = " 38400000-8CF0-11BD-b23e-10B96e40000D "

            OptableIdentifiers.receiveGaidAutomatically = true
            val ids = OptableIdentifiers(
                googleGaid = null,
            )

            val actual = ids.generateEIDs()
            val expected = listOf("g:${normalize(" 38400000-8CF0-11BD-b23e-10B96e40000D ")}")
            assertEquals(expected, actual)
        } finally {
            GoogleAdIdManager.adId = previousAdId
        }
    }

    @Test
    fun `generateEIDs does not add GAID from GoogleAdIdManager when googleGaid is set even if receiveGaidAutomatically is true`() {
        val previousAdId = GoogleAdIdManager.adId
        try {
            GoogleAdIdManager.adId = " 38400000-8cf0-11bd-b23e-10b96e40000d "

            OptableIdentifiers.receiveGaidAutomatically = true
            val ids = OptableIdentifiers(
                googleGaid = " USER-PROVIDED-GAID ",
            )

            val actual = ids.generateEIDs()
            val expected = listOf("g:${normalize(" USER-PROVIDED-GAID ")}")
            assertEquals(expected, actual)
        } finally {
            GoogleAdIdManager.adId = previousAdId
        }
    }

    @Test
    fun `generateEIDs does not add GAID from GoogleAdIdManager when receiveGaidAutomatically is false`() {
        val previousAdId = GoogleAdIdManager.adId
        try {
            GoogleAdIdManager.adId = " 38400000-8cf0-11bd-b23e-10b96e40000d "

            OptableIdentifiers.receiveGaidAutomatically = false
            val ids = OptableIdentifiers(
                googleGaid = null,
            )

            val actual = ids.generateEIDs()
            assertEquals(emptyList<String>(), actual)
        } finally {
            GoogleAdIdManager.adId = previousAdId
        }
    }


    @Test
    fun `generateEIDs, all fields, same values`() {
        val email = "john.doe+test@example.com"
        val phone = "+1(555)1234567"
        val postal = "12345"
        val ipv4 = "192.168.0.1"
        val ipv6 = "2001:db8::1"
        val idfa = "a1b2c3d4-e5f6-7890-abcd-ef0123456789"
        val gaid = "38400000-8cf0-11bd-b23e-10b96e40000d"
        val rida = "roku-rida-123"
        val tifa = "tifaabc"
        val afai = "afaixyz"
        val netId = "netid123"
        val id5 = "id5token"
        val utiq = "utiqvalue"
        val vid = "vidvalue"
        val c1 = "custom one"
        val raw1 = "raw:alreadyEncoded"
        val raw2 = "x:someRawValue"

        val ids = OptableIdentifiers(
            email = email,
            phoneNumber = phone,
            postalCode = postal,
            ipv4Address = ipv4,
            ipv6Address = ipv6,
            appleIdfa = idfa,
            googleGaid = gaid,
            rokuRida = rida,
            samsungTifa = tifa,
            amazonFireAfai = afai,
            netId = netId,
            id5 = id5,
            utiq = utiq,
            custom = mapOf(
                "v" to vid,
                "c1" to c1
            ),
            raw = listOf(raw1, raw2)
        )

        val actual = ids.generateEIDs()

        val expected = listOf(
            "e:${sha256(email)}",
            "p:${sha256(phone)}",
            "z:$postal",
            "i4:$ipv4",
            "i6:$ipv6",
            "a:$idfa",
            "g:$gaid",
            "r:$rida",
            "s:$tifa",
            "f:$afai",
            "n:$netId",
            "id5:$id5",
            "utiq:$utiq",
            "v:$vid",
            "c1:$c1",
            raw1,
            raw2
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `generateEIDs, all fields, different case and whitespaces`() {
        val ids = OptableIdentifiers(
            email = "  John.DOE+test@example.COM  ",
            phoneNumber = " +1  (555)  123  45 67 ",
            postalCode = " 12 3 45 ",
            ipv4Address = " 192.168. 0. 1 ",
            ipv6Address = " 2001:DB8:: 1 ",
            appleIdfa = " A1B2C3D4-E5F6-7890-ABCD-EF0123456789 ",
            googleGaid = " 38400000-8cf0-11bd-b23e-10b96e40000d ",
            rokuRida = " ROKU-RIDA- 123 ",
            samsungTifa = " TIFA  ABC ",
            amazonFireAfai = " AFAI  XYZ ",
            netId = " net id  123 ",
            id5 = " id5   token ",
            utiq = " UTIQ  VALUE ",
            custom = mapOf(
                "v" to "  vid value  ",
                "c1" to "  custom one  "
            ),
            raw = listOf(
                "raw:alreadyEncoded",
                "x:someRawValue"
            ),
        )

        val actual = ids.generateEIDs()

        val expected = listOf(
            "e:${sha256(normalize("  John.DOE+test@Example.COM  "))}",
            "p:${sha256(normalize(" +1  (555)  123  45 67 "))}",
            "z:${normalize(" 12 3 45 ")}",
            "i4:${removeWhitespaces(" 192.168. 0. 1 ")}",
            "i6:${normalize(" 2001:DB8:: 1 ")}",
            "a:${normalize(" A1B2C3D4-E5F6-7890-ABCD-EF0123456789 ")}",
            "g:${normalize(" 38400000-8cf0-11bd-b23e-10b96e40000d ")}",
            "r:${normalize(" ROKU-RIDA- 123 ")}",
            "s:${normalize(" TIFA  ABC ")}",
            "f:${normalize(" AFAI  XYZ ")}",
            "n:${removeWhitespaces(" net id  123 ")}",
            "id5:${removeWhitespaces(" id5   token ")}",
            "utiq:${normalize(" UTIQ  VALUE ")}",
            "v:${removeWhitespaces("  vid value  ")}",
            "c1:${"  custom one  ".trim()}",
            "raw:alreadyEncoded",
            "x:someRawValue"
        )

        assertEquals(expected, actual)
    }


    @Test
    fun `generateEIDs, all fields, builder`() {
        val built = OptableIdentifiers.Builder()
            .email("  John.DOE+test@example.COM  ")
            .phoneNumber(" +1  (555)  123  45 67 ")
            .postalCode(" 12 3 45 ")
            .ipv4Address(" 192.168. 0. 1 ")
            .ipv6Address(" 2001:DB8:: 1 ")
            .appleIdfa(" A1B2C3D4-E5F6-7890-ABCD-EF0123456789 ")
            .googleGaid(" 38400000-8cf0-11bd-b23e-10b96e40000d ")
            .rokuRida(" ROKU-RIDA- 123 ")
            .samsungTifa(" TIFA  ABC ")
            .amazonFireAfai(" AFAI  XYZ ")
            .netId(" net id  123 ")
            .id5(" id5   token ")
            .utiq(" UTIQ  VALUE ")
            .custom(
                mapOf(
                    "v" to "  vid value  ",
                    "c1" to "  custom one  "
                )
            )
            .raw(
                listOf(
                    "raw:alreadyEncoded",
                    "x:someRawValue"
                )
            )
            .build()

        val direct = OptableIdentifiers(
            email = "  John.DOE+test@example.COM  ",
            phoneNumber = " +1  (555)  123  45 67 ",
            postalCode = " 12 3 45 ",
            ipv4Address = " 192.168. 0. 1 ",
            ipv6Address = " 2001:DB8:: 1 ",
            appleIdfa = " A1B2C3D4-E5F6-7890-ABCD-EF0123456789 ",
            googleGaid = " 38400000-8cf0-11bd-b23e-10b96e40000d ",
            rokuRida = " ROKU-RIDA- 123 ",
            samsungTifa = " TIFA  ABC ",
            amazonFireAfai = " AFAI  XYZ ",
            netId = " net id  123 ",
            id5 = " id5   token ",
            utiq = " UTIQ  VALUE ",
            custom = mapOf(
                "v" to "  vid value  ",
                "c1" to "  custom one  "
            ),
            raw = listOf(
                "raw:alreadyEncoded",
                "x:someRawValue"
            ),
        )

        assertEquals(direct, built)
        assertEquals(direct.generateEIDs(), built.generateEIDs())
    }

    private fun sha256(value: String): String {
        val bytes = value.toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun normalize(value: String): String =
        value.replace("\\s+".toRegex(), "").lowercase(Locale.ROOT)

    private fun removeWhitespaces(value: String): String =
        value.replace("\\s+".toRegex(), "")
}