# Optable Android SDK [![](https://jitci.com/gh/Optable/optable-android-sdk/svg)](https://jitci.com/gh/Optable/optable-android-sdk)

Kotlin SDK for integrating with an [Optable Data Connectivity Node (DCN)](https://docs.optable.co) from an Android
application.

## Contents

- [Installing](#installing)
- [Using](#using)
    - [Identify API](#identify-api)
    - [Profile API](#profile-api)
    - [Targeting API](#targeting-api)
    - [Witness API](#witness-api)
    - [Integrating GAM](#integrating-google-ad-manager)
    - [Identify from URL](#identify-from-url)
- [Consents](#consents)
- [Demo Applications](#demo-applications)
    - [Building](#building)

## Installing

This SDK is published using [JitPack](https://jitpack.io/), so you need to add the repository to your application build.
Add the following in your root `build.gradle` at the end of `repositories`:

```kotlin
allprojects {
    repositories {
        ...
        maven {
            url 'https://jitpack.io'
        }
    }
}
```

Then add the dependency to the SDK in your app `build.gradle`:

```kotlin
dependencies {
    implementation 'com.github.Optable:optable-android-sdk:VERSION_TAG'
}
```

Replace `VERSION_TAG` with the latest or desired [SDK release](https://github.com/Optable/optable-android-sdk/releases).

## Using

To create the instance of the `OptableSDK` you have to create `OptableConfig` with the required parameters. 
- `providedContext` Android Context (Application or Activity).
- `tenant` The tenant name associated with the configuration. E.g. `acmeco.optable.co` => `acmeco`.
- `originSlug` The DCN's Source Slug. E.g. `acmeco-sdk`.

### Creating instance

#### Kotlin

```kotlin
class TheApplication : Application() {

  companion object {
    lateinit var optable: OptableSDK
  }

  override fun onCreate() {
    super.onCreate()

    val config = OptableConfig(
      providedContext = this,
      tenant = "prebidtest",
      originSlug = "js-sdk",
    )
    optable = OptableSDK(config)
  }

}
```

#### Java

```java
public class TheApplication extends Application {

  public static OptableSDK optable;

  @Override
  public void onCreate() {
    super.onCreate();

    OptableConfig config = new OptableConfig(this, "prebidtest", "js-sdk");
    optable = new OptableSDK(config);
  }

}
```

Additionally, you can set optional parameters to `OptableConfig`:
- `host` The hostname of the Optable endpoint. Default value is "na.edge.optable.co".
- `path` The API path to be appended to the host. Default value is "v2".
- `insecure` Boolean flag that determines if insecure HTTP should be used instead of HTTPS. Default is false. Note 
that production DCNs only listen to TLS traffic. The `insecure: true` option is meant to be used by Optable 
developers running the DCN locally for testing.
- `apiKey` An optional API key for authentication. If the API Endpoint is enabled as private, a Service Account API key will be required.
- `customUserAgent` An optional custom user agent string for network requests.
- `skipAdvertisingIdDetection` Boolean flag to skip the detection of advertising IDs. Default is false.
- `consents` Optional `OptableConsents` object for providing custom consent information. If not provided, default values will be used.

```kotlin
val config = OptableConfig(
    providedContext = this,
    tenant = "prebidtest",
    originSlug = "js-sdk",
    host = "ca.edge.optable.co",
    path = "v2",
    insecure = false,
    apiKey = API_KEY,
    customUserAgent = "",
    skipAdvertisingIdDetection = false,
    consents = OptableConsents(),
)
```

You can then call various SDK APIs on the instance as shown in the examples below. It's also possible to configure
multiple instances of `OptableSDK` in order to connect to other (e.g., partner) DCNs and/or reference other configured
application slug IDs.

By default, the SDK detects the application user agent by sniffing `settings.userAgentString` from a `WebView`. The
resulting user agent string is sent to your DCN for analytics purposes. To disable this behavior, you can provide an
optional fifth string parameter `customUserAgent`, which allows you to set whatever user agent string you would like to send
instead. For example, in Kotlin:

```kotlin
val config = OptableConfig(
  ...
  customUserAgent = "Chromium WebView",
)
```

Finally, an optional sixth boolean parameter `skipAdvertisingIdDetection` can be used to skip any ID info detection from
`AdvertisingIdClient` which by default runs in a background co-routine. Disabling ad ID detection means that the SDK
will not be able to automatically obtain the Google Advertising ID. For example, to disable ad ID detection, in Kotlin:

```kotlin
val config = OptableConfig(
  ...
  skipAdvertisingIdDetection = true,
)
```

### OptableIdentifiers

There are many fields to identify the users. You can provide any user information using `OptableIdentifiers` class. 
Sensitive data as email and phone number encrypted using SHA256. 

**NOTE**: You don't need to provide all fields. Only the fields that you have are required.

> :warning: **Client-side email and phone hashing**: The SDK will compute the SHA-256 hash of the sensitive information on the client-side
> and send the hashed value to the DCN. The email address and phone number is **not** sent by the device in plain text.

#### Kotlin

```kotlin
val ids = listOf(
  OptableIdentifier.Email("john.doe+test@example.com"),
  OptableIdentifier.PhoneNumber("+1(555)1234567"),
  OptableIdentifier.PostalCode("12345"),
  OptableIdentifier.IPv4("192.168.0.1"),
  OptableIdentifier.IPv6("2001:db8::1"),
  OptableIdentifier.AppleIdfa("a1b2c3d4-e5f6-7890-abcd-ef0123456789"),
  OptableIdentifier.GoogleGaid("38400000-8cf0-11bd-b23e-10b96e40000d"),
  OptableIdentifier.RokuRida("roku-rida-123"),
  OptableIdentifier.SamsungTifa("tifaabc"),
  OptableIdentifier.AmazonFireAfai("afaixyz"),
  OptableIdentifier.NetId("netid123"),
  OptableIdentifier.ID5("id5token"),
  OptableIdentifier.Utiq("utiqvalue"),
  OptableIdentifier.Custom("v", "vidvalue"),
  OptableIdentifier.Custom("c1", "custom one"),
  OptableIdentifier.Raw("raw:alreadyEncoded"),
  OptableIdentifier.Raw("ultra:someRawValue")
)
```

#### Java

```java
ArrayList<OptableIdentifier> ids = Lists.newArrayList(
  new OptableIdentifier.Email("john.doe+test@example.com"),
  new OptableIdentifier.PhoneNumber("+1(555)1234567"),
  new OptableIdentifier.PostalCode("12345"),
  new OptableIdentifier.IPv4("192.168.0.1"),
  new OptableIdentifier.IPv6("2001:db8::1"),
  new OptableIdentifier.AppleIdfa("a1b2c3d4-e5f6-7890-abcd-ef0123456789"),
  new OptableIdentifier.GoogleGaid("38400000-8cf0-11bd-b23e-10b96e40000d"),
  new OptableIdentifier.RokuRida("roku-rida-123"),
  new OptableIdentifier.SamsungTifa("tifaabc"),
  new OptableIdentifier.AmazonFireAfai("afaixyz"),
  new OptableIdentifier.NetId("netid123"),
  new OptableIdentifier.ID5("id5token"),
  new OptableIdentifier.Utiq("utiqvalue"),
  new OptableIdentifier.Custom("v", "vidvalue"),
  new OptableIdentifier.Custom("c1", "custom one"),
  new OptableIdentifier.Raw("raw:alreadyEncoded"),
  new OptableIdentifier.Raw("ultra:someRawValue")
);
```

### Identify API

To associate a user device with an authenticated identifier such as an Email address, or with other known IDs such as
the Google Advertising ID, or even your own vendor or app level `PPID`, you can call the `identify` API as follows:

#### Kotlin

```kotlin
val ids = listOf(
  OptableIdentifier.Email("john.doe+test@example.com"),
  OptableIdentifier.PhoneNumber("+1(555)1234567")
)
optable.identify(ids) { result ->
  val msg = when (result) {
    is OptableResult.Success -> "Identify success"
    is OptableResult.Error -> "Identify error: ${result.message}"
  }
  Log.d(TAG, msg)
}
```

#### Java

```java
ArrayList<OptableIdentifier> ids = Lists.newArrayList(
    new OptableIdentifier.Email("john.doe+test@example.com"),
    new OptableIdentifier.PhoneNumber("+1(555)1234567")
);
optable.identify(ids, result -> {
    if (result instanceof OptableResult.Success) {
        Log.d(TAG, "Identify success");
    } else if (result instanceof OptableResult.Error<Unit> error) {
        Log.d(TAG, "Identify error: " + error.getMessage());
    }
});
```

The SDK `identify()` method will asynchronously connect to the configured DCN and send IDs for resolution. You can register an observer to
understand successful completion or errors. 

The frequency of invocation of `identify` is up to you, however for optimal identity resolution we recommended to call
the `identify()` method on your SDK instance every time you authenticate a user, as well as periodically, such as for
example once every 15 to 60 minutes while the application is being actively used and an internet connection is
available.


### Profile API

To associate key value traits with the user's device, for eventual audience assembly, you can call the profile API as
follows:

#### Kotlin

```kotlin
val traits = hashMapOf<String, Any>("gender" to "F", "age" to 38, "hasAccount" to true)
optable.profile(traits) { result ->
  when (result) {
    is OptableResult.Success<*> -> {
      Log.d(TAG, "Profile success")
    }

    is OptableResult.Error -> {
      Log.d(TAG, "Profile error: ${result.message}")
    }
  }
}
```

#### Java

```java
HashMap<String, Object> traits = new HashMap<>();
traits.put("gender", "F");
traits.put("age", 38);
traits.put("hasAccount", true);

optable.profile(traits, result -> {
    if (result instanceof OptableResult.Success) {
        Log.d(TAG, "Profile success");
    } else if (result instanceof OptableResult.Error<Unit> error) {
        Log.d(TAG, "Profile error: " + error.getMessage());
    }
});
```

The specified traits are associated with the user's device and can be used for matching during audience assembly. The
traits are of type `HashMap<String, Any>`, and should consist only of key-value pairs where the key is a string and the value is either a string, number, or
boolean.

Additionally you can provide `id` and `neighbors` to associate the profile with other identifiers. The `id` parameter takes precedence over the passport if provided.

```kotlin
optable.profile(traits, "c:12", setOf("c:id1", "c:id2")) { ... }
```

### Targeting API

To get the targeting key values associated by the configured DCN with the device in real-time, you can call the
`targeting` API as follows:

#### Kotlin

```kotlin
val ids = listOf(
  OptableIdentifier.Email("test@test.com"),
)
optable.targeting(ids) { result ->
    val requestBuilder = AdManagerAdRequest.Builder()

    when (result) {
        is OptableResult.Success<OptableTargeting> -> {
            val audiences = result.data.audiences
            if (audiences != null) {
                // Apply 
            }
        }

        is OptableResult.Error<OptableTargeting> -> {
            Log.d(TAG, "Targeting error: ${result.message}")
        }
    }
}
```

#### Java

```java
ArrayList<OptableIdentifier> ids = Lists.newArrayList(
        new OptableIdentifier.Email("test@test.com")
)
optable.targeting(ids, result -> {
    if (result instanceof OptableResult.Success<OptableTargeting> success) {
        Map<String, List<String>> audiences = success.getData().getAudiences();
        if (audiences != null) {
            // Apply
        }
    } else if (result instanceof OptableResult.Error<OptableTargeting> error) {
        Log.d(TAG, "Targeting error: " + error.getMessage());
    }
})    
```

On success, the resulting key values are typically sent as part of a subsequent ad call. Therefore we recommend that you
either call `targeting()` before each ad call, or in parallel periodically, caching the resulting key values which you
then provide in ad calls.

#### Resolver hints (ID5 Mobile In-App)

Some third-party identity resolvers, such as [ID5](https://id5.io/) Mobile In-App, require additional context to return
an identifier. The SDK automatically attaches the following parameters to every `targeting` call:

- `bundle` — the app's package name.
- `ver` — the app's version name.
- `ua` — the device user agent (the same value sent in the `User-Agent` header; overridable via `customUserAgent`).

You can supply additional hint identifiers (HIDs) through an overload of `targeting()` that accepts a `hids` list. Each
hint is encoded exactly like the primary identifiers — with its Optable type prefix (for example `e:`, `p:`, `g:`,
`i6:`, and custom `c1:`…`c9:` used for a publisher-provided user ID), and, as with the primary `ids` list, the
device's Google Advertising ID is added automatically when available. Any identifier type may be passed as a hint -
the SDK forwards hints as-is, and which of them a resolver consumes is determined server-side.

The ID5 signature is managed by the SDK: when a `targeting` response returns one, it is cached on the device and resent
automatically on subsequent `targeting` calls to increase ID5 resolve rates and accuracy. No application code is required.

##### Kotlin

```kotlin
val ids = listOf(OptableIdentifier.Email("test@test.com"))
val hids = listOf(
    OptableIdentifier.IPv6("2001:0db8:5b96:0000:0000:426f:8e17:642a"),
    OptableIdentifier.PhoneNumber("+1(555)1234567"),
    OptableIdentifier.Email("user@example.com"),
    OptableIdentifier.Custom("c1", "puid-value"),
)
optable.targeting(ids, hids) { result ->
    when (result) {
        is OptableResult.Success<OptableTargeting> -> {
            // Targeting key-values are available via result.data.
        }

        is OptableResult.Error<OptableTargeting> -> {
            Log.d(TAG, "Targeting error: ${result.message}")
        }
    }
}
```

##### Java

```java
ArrayList<OptableIdentifier> hids = Lists.newArrayList(
        new OptableIdentifier.IPv6("2001:0db8:5b96:0000:0000:426f:8e17:642a"),
        new OptableIdentifier.PhoneNumber("+1(555)1234567"),
        new OptableIdentifier.Email("user@example.com"),
        new OptableIdentifier.Custom("c1", "puid-value")
);
optable.targeting(ids, hids, result -> {
    if (result instanceof OptableResult.Success<OptableTargeting> success) {
        // success.getData() exposes the targeting key-values.
    } else if (result instanceof OptableResult.Error<OptableTargeting> error) {
        Log.d(TAG, "Targeting error: " + error.getMessage());
    }
});
```

#### Caching Targeting Data

The `targeting` API will automatically cache resulting key value data in client storage on success. You can subsequently
retrieve the cached key value data as follows:

##### Kotlin

```kotlin
val targeting = optable.targetingFromCache()
if (targeting != null) {
    // Apply
}
```

##### Java

```java
OptableTargeting optableTargeting = optable.targetingFromCache();
if (optableTargeting != null) {
    // Apply
}
```

You can also clear the locally cached targeting data:

```kotlin
optable.targetingClearCache()
```

Note that both `targetingFromCache()` and `targetingClearCache()` are synchronous.

### Witness API

To send real-time event data from the user's device to the DCN for eventual audience assembly, you can call the witness
API as follows:

#### Kotlin

```kotlin
optable.witness(
  "GAMBannerFragment.loadAdButtonClicked",
  hashMapOf("exampleKey" to "exampleValue", "anotherExample" to 123, "foo" to false)
) { result ->
  when (result) {
    is OptableResult.Success<*> -> {
      Log.d(TAG, "Witness success")
    }

    is OptableResult.Error -> {
      Log.d(TAG, "Witness error: ${result.message}")
    }
  }
}
```

#### Java

```java
HashMap<String, Object> eventProperties = new HashMap<>();
eventProperties.put("exampleKey", "exampleValue");
eventProperties.put("exampleKey2", 123);
eventProperties.put("exampleKey3", false);

optable.witness("GAMBannerFragment.loadAdButtonClicked", eventProperties, result -> {
    if (result instanceof OptableResult.Success) {
        Log.d(TAG, "Witness success");
    } else if (result instanceof OptableResult.Error<Unit> error) {
        Log.d(TAG, "Witness error: " + error.getMessage());
    }
});
```

The specified event type and properties are associated with the logged event and which can be used for matching during
audience assembly. The optional witness event properties are of type `HashMap<String, Any>`, and should consist only of key-value pairs where the key is
a string and the value is either a string, number, or boolean.

### Integrating Google Ad Manager

We can further extend the above `targeting` example to show an integration with
a [Google Ad Manager](https://admanager.google.com/home/) ad server account:

#### Kotlin

```kotlin
private lateinit var optable: OptableSDK

private fun onClickLoadAd() {
  val ids = OptableIdentifiers(email = "test@test.com")
  optable.targeting(ids) { result ->
    val requestBuilder = AdManagerAdRequest.Builder()

    when (result) {
      is OptableResult.Success<OptableTargeting> -> {
        applyOptableToGam(requestBuilder, result.data)
      }

      is OptableResult.Error<OptableTargeting> -> {
        Log.d(TAG, "Targeting error: ${result.message}")
      }
    }

    adView.loadAd(requestBuilder.build())
  }
}

private fun applyOptableToGam(builder: AdManagerAdRequest.Builder, targeting: OptableTargeting?) {
  if (targeting == null) return

  val audiences = targeting.gamTargetingKeywords
  if (audiences != null) {
    for (entry in audiences.entries) {
      builder.addCustomTargeting(entry.key, entry.value)
    }
  }
}
```

#### Java

```java
    private AdManagerAdView mAdView;

    private void onClickLoadAd() {
      OptableIdentifiers ids = new OptableIdentifiers.Builder().email("test@test.com").build();
      optable.targeting(ids, result -> {
        AdManagerAdRequest.Builder requestBuilder = new AdManagerAdRequest.Builder();
    
        if (result instanceof OptableResult.Success<OptableTargeting> success) {
          applyOptableToGam(requestBuilder, success.getData());
        } else if (result instanceof OptableResult.Error<OptableTargeting> error) {
          Log.d(TAG, "Targeting error: " + error.getMessage());
        }
        mAdView.loadAd(requestBuilder.build());
      });
    }

    private void applyOptableToGam(AdManagerAdRequest.Builder builder, @Nullable OptableTargeting targeting) {
      if (targeting == null) return;
    
      Map<String, List<String>> audiences = targeting.getGamTargetingKeywords();
      if (audiences != null) {
        for (Map.Entry<String, List<String>> entry : audiences.entrySet()) {
          builder.addCustomTargeting(entry.getKey(), entry.getValue());
        }
      }
    }
```

Working examples are available in the Kotlin and Java SDK demo applications.

## Identify from URL

You can identify visitors arriving from Email newsletters. If you send Email newsletters that contain links to your application (e.g., deep links), then you may want to
automatically _identify_ visitors that have clicked on any such links via their Email address.

### Insert oeid into your Email newsletter template

To enable automatic identification of visitors originating from your Email newsletter, you first need to include an *
*oeid** parameter in the query string of all links to your website in your Email newsletter template. The value of the *
*oeid** parameter should be set to the SHA256 hash of the lowercased Email address of the recipient. For example, if you
are using [Braze](https://www.braze.com/) to send your newsletters, you can easily encode the SHA256 hash value of the
recipient's Email address by setting the **oeid** parameter in the query string of any links to your application as
follows:

```
oeid={{${email_address} | downcase | sha2}}
```

The above example uses various personalization tags as documented
in [Braze's user guide](https://www.braze.com/docs/user_guide/personalization_and_dynamic_content/) to dynamically
insert the required data into an **oeid** parameter, all of which should make up a _part_ of the destination URL in your
template.

### Capture clicks on deep links in your application

In order for your application to open on devices where it is installed when a link to your domain is clicked, you need
to [configure and prepare your application to handle deep links](https://developer.android.com/training/app-links/deep-linking)
first.

When Android launches your app after a user clicks on a link, it will start your app activity with your configured
_intent filters_. You can then obtain the `Uri` of the link by calling `getData()`, and pass it to the SDK's
`tryIdentifyFromUrl()` API which will automatically look for `oeid` in the query parameters of the `String` and call
`identify` with its value if found.

#### Kotlin

```kotlin
optable.tryIdentifyFromUrl(url) { result ->
  val msg = when (result) {
    is OptableResult.Success -> "Identify success"
    is OptableResult.Error -> "Identify error: ${result.message}"
  }
  Log.d(TAG, msg)
}
```

#### Java

```java
optable.tryIdentifyFromUrl(url, result -> {
    if (result instanceof OptableResult.Success) {
        Log.d(TAG, "Identify success");
    } else if (result instanceof OptableResult.Error<Unit> error) {
        Log.d(TAG, "Identify error: " + error.getMessage());
    }
});
```

## Consents

The SDK supports GDPR, IAB TCF, IAB GPP. You can set the most common consent values like this:
```kotlin
val consents = OptableConsents(
  gdprSubject = true,               // User is in scope of GDPR
  gdprConsent = "consent-string",   // IAB TCF consent string
  gpp = "gpp-string",               // IAB GPP string
  gppSid = "1,2",                   // GPP section IDs (comma-separated)
  req = "gdpr",                     // Regulatory framework to apply (e.g., "gdpr")
)

optable.setConsents(consents)
```

Parameters: 
- `gdprSubject` A boolean indicating whether GDPR applies. This value should be present when gdpr_consent is supplied.
If not set, SDK will try to fetch data from SharedPreferences (key `IABTCF_gdprApplies`), as stated in
[standard](https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/TCFv2/IAB%20Tech%20Lab%20-%20CMP%20API%20v2.md#in-app-details)
- `gdprConsent` TCF EU v2 consent string. If not set, SDK will try to fetch data from SharedPreferences (key `IABTCF_TCString`), as stated in
[standard](https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/TCFv2/IAB%20Tech%20Lab%20-%20CMP%20API%20v2.md#in-app-details).
- `gpp`  GPP privacy string.
If not set, SDK will try to fetch data from SharedPreferences (key `IABGPP_2_TCString`), as stated in
[standard](https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/TCFv2/IAB%20Tech%20Lab%20-%20CMP%20API%20v2.md#in-app-details)
- `gppSid`  A comma-separated list of up to two sections applicable in a given GPP privacy string. This value is required when gpp is present.
- `req` Optable privacy regulation override, which can be one of: `gdpr`, `can`, `us`, or `null` and will override all other privacy regulations when present.

**NOTE**: The custom values provided to the `OptableConfig` will override all other privacy regulations when present.

## Demo Applications

The Kotlin and Java demo applications show a working example of the APIs, as well as
an integration with the [Google Ad Manager](https://admanager.google.com/home/) ad server, enabling the targeting of
ads served by GAM to audiences activated in the [Optable](https://optable.co/) DCN.

By default, the demo applications will connect to the [Optable](https://optable.co/) demo DCN at `sandbox.optable.co`and
reference application slug `android-sdk-demo`. The demo apps depend on
the [GAM Mobile Ads SDK for Android](https://developers.google.com/ad-manager/mobile-ads-sdk/android/quick-start) and
load ads from a GAM account operated by [Optable](https://optable.co/).

### Building

To build the Kotlin demo app, from [Android Studio](https://developer.android.com/studio), navigate to `File > Open` and
open the `DemoApp/DemoAppKotlin` directory. To build the Java demo app, open the `DemoApp/DemoAppJava` directory. In
both cases, you should be able to build and run the resulting project directly, since it will automatically download the
`co.optable.android_sdk` library from the [JitPack](https://jitpack.io/) Maven repository.

### Development

Test coverage is measured using the Kotlinx Kover plugin. To generate a report, run the following command:

```bash
./gradlew android_sdk:koverHtmlReportDebug
```

The HTML report will be available at `./android_sdk/build/reports/kover/htmlDebug/index.html`.
