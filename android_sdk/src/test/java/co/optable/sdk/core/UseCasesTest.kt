package co.optable.sdk.core

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UseCasesTest {

    private lateinit var useCases: UseCases

    @Before
    fun setUp() {
        useCases = UseCases()
    }

    @Test
    fun `parse targeting success`() {
        val responseJson  = Gson().fromJson(RESPONSE, JsonObject::class.java)
        val result = useCases.parseTargetingResponse(responseJson)

        assertNotNull(result)
        assertEquals(EXPECTED_OPEN_RTB, result.openRtbJson)
        assertEquals(EXPECTED_TARGETING_DATA, result.targetingData.toString())
        assertEquals(mapOf("optable-test" to listOf("test1", "test2")), result.gamTargetingKeywords)
    }

    @Test
    fun `parse targeting without OpenRTB`() {
        val responseJson  = Gson().fromJson(RESPONSE_WITHOUT_OPENRTB, JsonObject::class.java)
        val result = useCases.parseTargetingResponse(responseJson)

        assertNotNull(result)
        assertEquals(null, result.openRtbJson)
        assertEquals("""{"user":[],"audience":[{"provider":"optable.co","ids":[{"id":"test1"},{"id":"test2"}],"keyspace":"optable-test","rtb_segtax":5001}],"resolved_ids":["e:f660ab912ec121d1b1e928a0bb4bc61b15f5ad44d5efdc4e1c92a25e99b8e44a"]}""", result.targetingData.toString())
        assertEquals(mapOf("optable-test" to listOf("test1", "test2")), result.gamTargetingKeywords)
    }

    @Test
    fun `parse targeting without keywords`() {
        val responseJson  = Gson().fromJson(RESPONSE_WITHOUT_AUDIENCES, JsonObject::class.java)
        val result = useCases.parseTargetingResponse(responseJson)

        assertNotNull(result)
        assertEquals(null, result.openRtbJson)
        assertEquals("""{"user":[],"resolved_ids":["e:f660ab912ec121d1b1e928a0bb4bc61b15f5ad44d5efdc4e1c92a25e99b8e44a"]}""", result.targetingData.toString())
        assertEquals(emptyMap<String, List<String>>(), result.gamTargetingKeywords)
    }

    @Test
    fun `parse id5 signature from response`() {
        val responseJson = Gson().fromJson(RESPONSE_WITH_ID5, JsonObject::class.java)
        assertEquals("id5-signature-value", useCases.parseId5Signature(responseJson))
    }

    @Test
    fun `parse id5 signature returns null when absent`() {
        val responseJson = Gson().fromJson(RESPONSE, JsonObject::class.java)
        assertEquals(null, useCases.parseId5Signature(responseJson))
    }

    @Test
    fun `parse targeting blank`() {
        val responseJson  = Gson().fromJson("{}", JsonObject::class.java)
        val result = useCases.parseTargetingResponse(responseJson)

        assertNotNull(result)
        assertEquals(null, result.openRtbJson)
        assertEquals("{}", result.targetingData.toString())
        assertEquals(emptyMap<String, List<String>>(), result.gamTargetingKeywords)
    }

    companion object {

        private const val EXPECTED_OPEN_RTB =
            """{"user":{"data":[{"id":"optable.co","segment":[{"id":"test1"},{"id":"test2"}]}],"eids":[{"inserter":"optable.co","matcher":"optable.co","mm":3,"source":"optable.co","uids":[{"id":"e:f660ab912ec121d1b1e928a0bb4bc61b15f5ad44d5efdc4e1c92a25e99b8e44a"},{"id":"v:17pZOV6BmGyUYifUNpYJsq"},{"id":"v:2UrRt9jeXPXAxxxsxTbGu9"},{"id":"v:4B4qgjdrBYtU9tn0RfjqJY"},{"id":"v:5WX5fAM6pyoEQXUDaJy0Um"},{"id":"v:5dJU7HMbmcHzut5uk0hJk4"},{"id":"v:6wZ8XnZ2FuO8EQSxGK3OnS"},{"id":"v:74gpCwdJknMdvLgQvfuKvu"}]},{"inserter":"optable.co","matcher":"optable.co","mm":3,"source":"criteo-hemapi.com","uids":[{"id":"QcOx619EVCUyQkl6d0xDZkJCWmIlMkYzUHBDZU0yN2sxbkpIMERtWEgydVJSNXUxbmVHYUZJNWJZYkpqN2JmeUZnRjlPMmJNWVRzUVhnNDhBRGFjS2dnM016ZWpPaVU4dG5uRXhmdTlFbmNPUm8xeWRsdFklM0Q","atype":3,"ext":{"stype":"cto_bundle_hem_api"}}]}]}}"""

        private const val EXPECTED_TARGETING_DATA =
            """{"user":[],"audience":[{"provider":"optable.co","ids":[{"id":"test1"},{"id":"test2"}],"keyspace":"optable-test","rtb_segtax":5001}],"ortb2":{"user":{"data":[{"id":"optable.co","segment":[{"id":"test1"},{"id":"test2"}]}],"eids":[{"inserter":"optable.co","matcher":"optable.co","mm":3,"source":"optable.co","uids":[{"id":"e:f660ab912ec121d1b1e928a0bb4bc61b15f5ad44d5efdc4e1c92a25e99b8e44a"},{"id":"v:17pZOV6BmGyUYifUNpYJsq"},{"id":"v:2UrRt9jeXPXAxxxsxTbGu9"},{"id":"v:4B4qgjdrBYtU9tn0RfjqJY"},{"id":"v:5WX5fAM6pyoEQXUDaJy0Um"},{"id":"v:5dJU7HMbmcHzut5uk0hJk4"},{"id":"v:6wZ8XnZ2FuO8EQSxGK3OnS"},{"id":"v:74gpCwdJknMdvLgQvfuKvu"}]},{"inserter":"optable.co","matcher":"optable.co","mm":3,"source":"criteo-hemapi.com","uids":[{"id":"QcOx619EVCUyQkl6d0xDZkJCWmIlMkYzUHBDZU0yN2sxbkpIMERtWEgydVJSNXUxbmVHYUZJNWJZYkpqN2JmeUZnRjlPMmJNWVRzUVhnNDhBRGFjS2dnM016ZWpPaVU4dG5uRXhmdTlFbmNPUm8xeWRsdFklM0Q","atype":3,"ext":{"stype":"cto_bundle_hem_api"}}]}]}},"resolved_ids":["e:f660ab912ec121d1b1e928a0bb4bc61b15f5ad44d5efdc4e1c92a25e99b8e44a"]}"""

        private const val RESPONSE_WITH_ID5 = """
{
  "ortb2": {
    "user": {
      "eids": [
        {
          "source": "id5-sync.com",
          "uids": [
            {
              "id": "ID5-abc",
              "atype": 1,
              "ext": {
                "linkType": 2,
                "signature": "id5-signature-value"
              }
            }
          ]
        }
      ]
    }
  }
}
        """

        private const val RESPONSE_WITHOUT_AUDIENCES = """
{
  "user": [],
  "resolved_ids": [
    "e:f660ab912ec121d1b1e928a0bb4bc61b15f5ad44d5efdc4e1c92a25e99b8e44a"
  ]
}
        """

        private const val RESPONSE_WITHOUT_OPENRTB = """
{
  "user": [],
  "audience": [
    {
      "provider": "optable.co",
      "ids": [
        {
          "id": "test1"
        },
        {
          "id": "test2"
        }
      ],
      "keyspace": "optable-test",
      "rtb_segtax": 5001
    }
  ],
  "resolved_ids": [
    "e:f660ab912ec121d1b1e928a0bb4bc61b15f5ad44d5efdc4e1c92a25e99b8e44a"
  ]
}
        """

        private const val RESPONSE = """
{
  "user": [],
  "audience": [
    {
      "provider": "optable.co",
      "ids": [
        {
          "id": "test1"
        },
        {
          "id": "test2"
        }
      ],
      "keyspace": "optable-test",
      "rtb_segtax": 5001
    }
  ],
  "ortb2": {
    "user": {
      "data": [
        {
          "id": "optable.co",
          "segment": [
            {
              "id": "test1"
            },
            {
              "id": "test2"
            }
          ]
        }
      ],
      "eids": [
        {
          "inserter": "optable.co",
          "matcher": "optable.co",
          "mm": 3,
          "source": "optable.co",
          "uids": [
            {
              "id": "e:f660ab912ec121d1b1e928a0bb4bc61b15f5ad44d5efdc4e1c92a25e99b8e44a"
            },
            {
              "id": "v:17pZOV6BmGyUYifUNpYJsq"
            },
            {
              "id": "v:2UrRt9jeXPXAxxxsxTbGu9"
            },
            {
              "id": "v:4B4qgjdrBYtU9tn0RfjqJY"
            },
            {
              "id": "v:5WX5fAM6pyoEQXUDaJy0Um"
            },
            {
              "id": "v:5dJU7HMbmcHzut5uk0hJk4"
            },
            {
              "id": "v:6wZ8XnZ2FuO8EQSxGK3OnS"
            },
            {
              "id": "v:74gpCwdJknMdvLgQvfuKvu"
            }
          ]
        },
        {
          "inserter": "optable.co",
          "matcher": "optable.co",
          "mm": 3,
          "source": "criteo-hemapi.com",
          "uids": [
            {
              "id": "QcOx619EVCUyQkl6d0xDZkJCWmIlMkYzUHBDZU0yN2sxbkpIMERtWEgydVJSNXUxbmVHYUZJNWJZYkpqN2JmeUZnRjlPMmJNWVRzUVhnNDhBRGFjS2dnM016ZWpPaVU4dG5uRXhmdTlFbmNPUm8xeWRsdFklM0Q",
              "atype": 3,
              "ext": {
                "stype": "cto_bundle_hem_api"
              }
            }
          ]
        }
      ]
    }
  },
  "resolved_ids": [
    "e:f660ab912ec121d1b1e928a0bb4bc61b15f5ad44d5efdc4e1c92a25e99b8e44a"
  ]
}
        """

    }
}
