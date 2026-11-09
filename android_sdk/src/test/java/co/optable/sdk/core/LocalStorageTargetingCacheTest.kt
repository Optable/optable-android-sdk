package co.optable.sdk.core

import co.optable.sdk.OptableConfig
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Caches targeting in real SharedPreferences and reads it back, so the Gson serialization of
 * [co.optable.sdk.OptableTargeting], including its `targetingData` JSONObject, is exercised
 * end to end. LocalStorageTest mocks SharedPreferences and the JSONObject.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LocalStorageTargetingCacheTest {

    private companion object {
        private const val RESPONSE = """{"user":[{"provider":"optable.co","ids":[{"id":"e:f660ab912ec121d1b1e928a0bb4bc61b15f5ad44d5efdc4e1c92a25e99b8e44a"}]}],"audience":[{"provider":"optable.co","ids":[{"id":"test1"},{"id":"test2"}],"keyspace":"optable-test","rtb_segtax":5001}],"ortb2":{"user":{"data":[{"id":"optable.co","segment":[{"id":"test1"},{"id":"test2"}],"ext":{"segtax":5001}}],"eids":[{"source":"optable.co","uids":[{"id":"abc","atype":3}]}]}},"resolved_ids":["e:f660ab912ec121d1b1e928a0bb4bc61b15f5ad44d5efdc4e1c92a25e99b8e44a"],"flag":true,"score":0.5}"""
    }

    private fun newStorage() = LocalStorage(
        OptableConfig(RuntimeEnvironment.getApplication(), "testtenant", "testslug")
    )

    @Test
    fun `cached targeting should read back with the same values`() {
        val targeting = UseCases().parseTargetingResponse(Gson().fromJson(RESPONSE, JsonObject::class.java))

        newStorage().setTargeting(targeting)
        // A new instance reads from SharedPreferences only, as after an app restart.
        val cached = newStorage().getTargeting()

        assertNotNull(cached)
        assertEquals(targeting.gamTargetingKeywords, cached!!.gamTargetingKeywords)
        assertEquals(targeting.openRtbJson, cached.openRtbJson)
        assertEquals(targeting.targetingData.toString(), cached.targetingData.toString())
    }

    @Test
    fun `cached targetingData should keep nested objects and arrays usable`() {
        val targeting = UseCases().parseTargetingResponse(Gson().fromJson(RESPONSE, JsonObject::class.java))

        newStorage().setTargeting(targeting)
        val data = newStorage().getTargeting()!!.targetingData

        assertEquals("optable-test", data.getJSONArray("audience").getJSONObject(0).getString("keyspace"))
        assertEquals(5001, data.getJSONArray("audience").getJSONObject(0).getInt("rtb_segtax"))
        assertEquals("abc", data.getJSONObject("ortb2").getJSONObject("user").getJSONArray("eids")
            .getJSONObject(0).getJSONArray("uids").getJSONObject(0).getString("id"))
        assertEquals(true, data.getBoolean("flag"))
        assertEquals(0.5, data.getDouble("score"), 0.0)
    }
}
