package co.optable.sdk

import co.optable.sdk.core.GoogleAdIdManager
import co.optable.sdk.core.IdentifiersEncoder
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.security.MessageDigest
import java.util.*

class OptableIdentifiersTest {

    private lateinit var identifiersEncoder: IdentifiersEncoder
    private lateinit var mockGoogleAdIdManager: GoogleAdIdManager

    @Before
    fun setUp() {
        mockGoogleAdIdManager = mockk<GoogleAdIdManager>()
        every { mockGoogleAdIdManager.getId() } returns null

        identifiersEncoder = IdentifiersEncoder(mockGoogleAdIdManager)
    }

    @Test
    fun `encode empty list`() {
        val ids = emptyList<OptableIdentifier>()
        val actual = identifiersEncoder.encode(ids)
        assertEquals(emptyList<String>(), actual)
    }

    @Test
    fun `encode email`() {
        val email = "  John.DOE+test@example.COM  "
        val ids = listOf(OptableIdentifier.Email(email))
        val actual = identifiersEncoder.encode(ids)
        val expected = listOf("e:${sha256(normalize(email))}")
        assertEquals(expected, actual)
    }

    @Test
    fun `encode phoneNumber`() {
        val phone = " +1  (555)  123  45 67 "
        val ids = listOf(OptableIdentifier.PhoneNumber(phone))
        val actual = identifiersEncoder.encode(ids)
        val expected = listOf("p:${sha256(normalize(phone))}")
        assertEquals(expected, actual)
    }

    @Test
    fun `encode postalCode`() {
        val postal = " 12 3 45 "
        val ids = listOf(OptableIdentifier.PostalCode(postal))
        val actual = identifiersEncoder.encode(ids)
        val expected = listOf("z:${normalize(postal)}")
        assertEquals(expected, actual)
    }

    @Test
    fun `encode ipv4Address`() {
        val ipv4 = " 192.168. 0. 1 "
        val ids = listOf(OptableIdentifier.IPv4(ipv4))
        val actual = identifiersEncoder.encode(ids)
        val expected = listOf("i4:${removeWhitespaces(ipv4)}")
        assertEquals(expected, actual)
    }

    @Test
    fun `encode ipv6Address`() {
        val ipv6 = " 2001:DB8:: 1 "
        val ids = listOf(OptableIdentifier.IPv6(ipv6))
        val actual = identifiersEncoder.encode(ids)
        val expected = listOf("i6:${normalize(ipv6)}")
        assertEquals(expected, actual)
    }

    @Test
    fun `encode appleIdfa`() {
        val idfa = " A1B2C3D4-E5F6-7890-ABCD-EF0123456789 "
        val ids = listOf(OptableIdentifier.AppleIdfa(idfa))
        val actual = identifiersEncoder.encode(ids)
        val expected = listOf("a:${normalize(idfa)}")
        assertEquals(expected, actual)
    }

    @Test
    fun `encode googleGaid`() {
        val gaid = " 38400000-8cf0-11bd-b23e-10b96e40000d "
        val ids = listOf(OptableIdentifier.GoogleGaid(gaid))
        val actual = identifiersEncoder.encode(ids)
        val expected = listOf("g:${normalize(gaid)}")
        assertEquals(expected, actual)
    }

    @Test
    fun `encode rokuRida`() {
        val rida = " ROKU-RIDA- 123 "
        val ids = listOf(OptableIdentifier.RokuRida(rida))
        val actual = identifiersEncoder.encode(ids)
        val expected = listOf("r:${normalize(rida)}")
        assertEquals(expected, actual)
    }

    @Test
    fun `encode samsungTifa`() {
        val tifa = " TIFA  ABC "
        val ids = listOf(OptableIdentifier.SamsungTifa(tifa))
        val actual = identifiersEncoder.encode(ids)
        val expected = listOf("s:${normalize(tifa)}")
        assertEquals(expected, actual)
    }

    @Test
    fun `encode amazonFireAfai`() {
        val afai = " AFAI  XYZ "
        val ids = listOf(OptableIdentifier.AmazonFireAfai(afai))
        val actual = identifiersEncoder.encode(ids)
        val expected = listOf("f:${normalize(afai)}")
        assertEquals(expected, actual)
    }

    @Test
    fun `encode netId`() {
        val netId = " net id  123 "
        val ids = listOf(OptableIdentifier.NetId(netId))
        val actual = identifiersEncoder.encode(ids)
        val expected = listOf("n:${removeWhitespaces(netId)}")
        assertEquals(expected, actual)
    }

    @Test
    fun `encode id5`() {
        val id5 = " id5   token "
        val ids = listOf(OptableIdentifier.ID5(id5))
        val actual = identifiersEncoder.encode(ids)
        val expected = listOf("id5:${removeWhitespaces(id5)}")
        assertEquals(expected, actual)
    }

    @Test
    fun `encode utiq`() {
        val utiq = " UTIQ  VALUE "
        val ids = listOf(OptableIdentifier.Utiq(utiq))
        val actual = identifiersEncoder.encode(ids)
        val expected = listOf("utiq:${normalize(utiq)}")
        assertEquals(expected, actual)
    }

    @Test
    fun `encode custom vid`() {
        val vid = "  vid value  "
        val ids = listOf(OptableIdentifier.Custom("v", vid))
        val actual = identifiersEncoder.encode(ids)
        val expected = listOf("v:${removeWhitespaces(vid)}")
        assertEquals(expected, actual)
    }

    @Test
    fun `encode custom nonVid`() {
        val c1 = "  custom one  "
        val ids = listOf(OptableIdentifier.Custom("c1", c1))
        val actual = identifiersEncoder.encode(ids)
        val expected = listOf("c1:${c1.trim()}")
        assertEquals(expected, actual)
    }

    @Test
    fun `encode raw`() {
        val ids = listOf(
            OptableIdentifier.Raw("raw:alreadyEncoded"),
            OptableIdentifier.Raw("custom:any")
        )
        val actual = identifiersEncoder.encode(ids)
        val expected = listOf("raw:alreadyEncoded", "custom:any")
        assertEquals(expected, actual)
    }

    @Test
    fun `custom gaid`() {
        every { mockGoogleAdIdManager.getId() } returns null

        val actual = identifiersEncoder.encode(listOf(OptableIdentifier.GoogleGaid("customId")))
        val expected = listOf("g:customid")
        assertEquals(expected, actual)
    }

    @Test
    fun `both gaids, manager win`() {
        every { mockGoogleAdIdManager.getId() } returns "managerId"

        val actual = identifiersEncoder.encode(listOf(OptableIdentifier.GoogleGaid("customId")))
        val expected = listOf("g:managerId")
        assertEquals(expected, actual)
    }

    @Test
    fun `gaid from manager`() {
        every { mockGoogleAdIdManager.getId() } returns "managerId"

        val actual = identifiersEncoder.encode(emptyList())
        val expected = listOf("g:managerId")
        assertEquals(expected, actual)
    }

    @Test
    fun `gaid from manager null`() {
        every { mockGoogleAdIdManager.getId() } returns null

        val actual = identifiersEncoder.encode(emptyList())
        assertEquals(emptyList<String>(), actual)
    }


    @Test
    fun `encode all fields, same values`() {
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

        val ids = listOf(
            OptableIdentifier.Email(email),
            OptableIdentifier.PhoneNumber(phone),
            OptableIdentifier.PostalCode(postal),
            OptableIdentifier.IPv4(ipv4),
            OptableIdentifier.IPv6(ipv6),
            OptableIdentifier.AppleIdfa(idfa),
            OptableIdentifier.GoogleGaid(gaid),
            OptableIdentifier.RokuRida(rida),
            OptableIdentifier.SamsungTifa(tifa),
            OptableIdentifier.AmazonFireAfai(afai),
            OptableIdentifier.NetId(netId),
            OptableIdentifier.ID5(id5),
            OptableIdentifier.Utiq(utiq),
            OptableIdentifier.Custom("v", vid),
            OptableIdentifier.Custom("c1", c1),
            OptableIdentifier.Raw(raw1),
            OptableIdentifier.Raw(raw2)
        )

        val actual = identifiersEncoder.encode(ids)

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

        assertEquals(expected.sorted(), actual.sorted())
    }

    @Test
    fun `encode all fields, different case and whitespaces`() {
        val ids = listOf(
            OptableIdentifier.Email("  John.DOE+test@example.COM  "),
            OptableIdentifier.PhoneNumber(" +1  (555)  123  45 67 "),
            OptableIdentifier.PostalCode(" 12 3 45 "),
            OptableIdentifier.IPv4(" 192.168. 0. 1 "),
            OptableIdentifier.IPv6(" 2001:DB8:: 1 "),
            OptableIdentifier.AppleIdfa(" A1B2C3D4-E5F6-7890-ABCD-EF0123456789 "),
            OptableIdentifier.GoogleGaid(" 38400000-8cf0-11bd-b23e-10b96e40000d "),
            OptableIdentifier.RokuRida(" ROKU-RIDA- 123 "),
            OptableIdentifier.SamsungTifa(" TIFA  ABC "),
            OptableIdentifier.AmazonFireAfai(" AFAI  XYZ "),
            OptableIdentifier.NetId(" net id  123 "),
            OptableIdentifier.ID5(" id5   token "),
            OptableIdentifier.Utiq(" UTIQ  VALUE "),
            OptableIdentifier.Custom("v", "  vid value  "),
            OptableIdentifier.Custom("c1", "  custom one  "),
            OptableIdentifier.Raw("raw:alreadyEncoded"),
            OptableIdentifier.Raw("x:someRawValue")
        )

        val actual = identifiersEncoder.encode(ids)

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

        assertEquals(expected.sorted(), actual.sorted())
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
