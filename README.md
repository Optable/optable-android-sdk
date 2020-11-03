# Optable Android SDK [![](https://jitci.com/gh/Optable/optable-android-sdk/svg)](https://jitci.com/gh/Optable/optable-android-sdk)

Kotlin SDK for integrating with optable-sandbox from an Android application.

## Contents

- [Installing](#installing)
- [Using](#using)
  - [Identify API](#identify-api)
  - [Targeting API](#targeting-api)
  - [Witness API](#witness-api)
  - [Integrating GAM360](#integrating-gam360)
- [Identifying visitors arriving from Email newsletters](#identifying-visitors-arriving-from-email-newsletters)
  - [Insert oeid into your Email newsletter template](#insert-oeid-into-your-email-newsletter-template)
  - [Capture clicks on deep links in your application](#capture-clicks-on-deep-links-in-your-application)
  - [Call tryIdentifyFromURI SDK API](#call-tryidentifyfromuri-sdk-api)
- [Demo Applications](#demo-applications)
  - [Building](#building)

## Installing

This SDK is published using [JitPack](https://jitpack.io/), so to add it to your application build, follow the [JitPack How To](https://jitpack.io/).

If you're using gradle, add the following in your root `build.gradle` at the end of `repositories`:

```kotlin
allprojects {
    repositories {
        ...
        maven {
            url 'https://jitpack.io'
            credentials { username authToken }
        }
    }
}
```

In order to allow [JitPack](https://jitpack.io/) to access this private GitHub repository, add the following authToken to your `gradle.properties`:

```
authToken=jp_usu041v753rg6asheri00bjihl
```

Finally, add the dependency to the SDK in your app `build.gradle`:

```kotlin
dependencies {
    implementation 'com.github.Optable:optable-android-sdk:VERSION_TAG'
}
```

Remember to replace `VERSION_TAG` with the latest or desired [SDK release](https://github.com/Optable/optable-android-sdk/releases).

## Using

To configure an instance of the SDK integrating with an [Optable](https://optable.co/) sandbox running at hostname `sandbox.customer.com`, from a configured application origin identified by slug `my-app`, you can instantiate the SDK from an Activity or Application `Context`, such as for example the following application `MainActivity`:

#### Kotlin

```kotlin
import co.optable.android_sdk.OptableSDK
...

class MainActivity : AppCompatActivity() {
    companion object {
        var OPTABLE: OptableSDK? = null
    }
    ...
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ...
        MainActivity.OPTABLE = OptableSDK(this, "sandbox.customer.com", "my-app")
        ...
    }
}
```

#### Java

```java
import co.optable.android_sdk.OptableSDK;
...

public class MainActivity extends AppCompatActivity {
    public static OptableSDK OPTABLE;
    ...
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ...
        MainActivity.OPTABLE = new OptableSDK(this.getApplicationContext(),
                                              "sandbox.customer.com", "my-app");
        ...
    }
}
```

You can then call various SDK APIs on the instance as shown in the examples below. It's also possible to configure multiple instances of `OptableSDK` in order to connect to other (e.g., partner) sandboxes and/or reference other configured application slug IDs.

Note that all SDK communication with Optable sandboxes is done over TLS. The only exception to this is if you instantiate the `OptableSDK` class with a fourth optional boolean parameter, `insecure`, set to `true`. For example, in Kotlin:

```kotlin
MainActivity.OPTABLE = OptableSDK(this, "sandbox.customer.com", "my-app", true)
```

However, since production sandboxes only listen to TLS traffic, the above is really only useful for developers of `optable-sandbox` running the sandbox locally for testing.

### Identify API

To associate a user device with an authenticated identifier such as an Email address, or with other known IDs such as the Google Advertising ID, or even your own vendor or app level `PPID`, you can call the `identify` API as follows:

#### Kotlin

```kotlin
import co.optable.android_sdk.OptableSDK
import my.org.app.MainActivity
import android.util.Log
...

val emailString = "some.email@address.com"
val sendGoogleAdIDBoolean = true
val ppid = "my-id-123"

MainActivity.OPTABLE!!
    .identify(emailString, sendGoogleAdIDBoolean, ppid)
    .observe(viewLifecycleOwner, Observer
    { result ->
        if (result.status == OptableSDK.Status.SUCCESS) {
            Log.i("Identify API Success")
        } else {
            // result.status is OptableSDK.Status.ERROR
            // result.message is the error message
            Log.e("Identify API Error: ${result.message}")
        }
    })
```

#### Java

```java
import co.optable.android_sdk.OptableSDK;
import co.optable.demoappjava.MainActivity;
import android.util.Log;
...

String emailString = "some.email@address.com";
Boolean sendGoogleAdIDBoolean = true;
String ppid = "my-id-123";

MainActivity.OPTABLE
    .identify(emailString, sendGoogleAdIDBoolean, ppid)
    .observe(getViewLifecycleOwner(), result -> {
        if (result.getStatus() == OptableSDK.Status.SUCCESS) {
            Log.i(null, "Identify API Success");
        } else {
            // result.getStatus() is OptableSDK.Status.ERROR
            // result.getMessage() is the error message
            Log.e(null, "Identify API Error: " + result.getMessage());
        }
    });
```

The SDK `identify()` method will asynchronously connect to the configured sandbox and send IDs for resolution. The second (`sendGoogleAdIDBoolean`) and third (`ppid`) arguments to `identify()` are optional. You can register an observer to understand successful completion or errors.

> :warning: **Client-Side Email Hashing**: The SDK will compute the SHA-256 hash of the Email address on the client-side and send the hashed value to the sandbox. The Email address is **not** sent by the device in plain text.

Since the `sendGoogleAdIDBoolean` value provided to `identify()` is `true`, the SDK will fetch and send the Google Advertising ID in the call to `identify` too, unless the user has turned on "Limit ad tracking" in their Google device advertising settings.

The frequency of invocation of `identify` is up to you, however for optimal identity resolution we recommended to call the `identify()` method on your SDK instance every time you authenticate a user, as well as periodically, such as for example once every 15 to 60 minutes while the application is being actively used and an internet connection is available.

### Targeting API

To get the targeting key values associated by the configured sandbox with the device in real-time, you can call the `targeting` API as follows:

#### Kotlin

```kotlin
import co.optable.android_sdk.OptableSDK
import my.org.app.MainActivity
import android.util.Log
...
MainActivity.OPTABLE!!
    .targeting()
    .observe(viewLifecycleOwner, Observer { result ->
        if (result.status == OptableSDK.Status.SUCCESS) {
            Log.i("Targeting API Success... ")

            // result.data!! can be iterated to get targeting key values:
            result.data!!.forEach { (key, values) ->
                Log.i("Targeting KV: ${key} = ${values}")
            }
        } else {
            // result.status is OptableSDK.Status.ERROR
            // result.message is the error message
            Log.e("Targeting API Error: ${result.message}")
        }
    })
```

#### Java

```java
import co.optable.android_sdk.OptableSDK;
import co.optable.demoappjava.MainActivity;
import android.util.Log;
...
MainActivity.OPTABLE
    .targeting()
    .observe(getViewLifecycleOwner(), result -> {
        if (result.getStatus() == OptableSDK.Status.SUCCESS) {
            Log.i(null, "Targeting API Success... ");

            result.getData().forEach((key, values) -> {
                Log.i(null, "Targeting KV: " + key.toString() + " = " + values.toString());
            });
        } else {
            // result.getStatus() is OptableSDK.Status.ERROR
            // result.getMessage() is the error message
            Log.e(null, "Targeting API Error: " + result.getMessage());
        }
    });
```

On success, the resulting key values are typically sent as part of a subsequent ad call. Therefore we recommend that you either call `targeting()` before each ad call, or in parallel periodically, caching the resulting key values which you then provide in ad calls.

### Witness API

To send real-time event data from the user's device to the sandbox for eventual audience assembly, you can call the witness API as follows:

#### Kotlin

```kotlin
import co.optable.android_sdk.OptableSDK
import my.org.app.MainActivity
import android.util.Log
...
MainActivity.OPTABLE!!
    .witness("example.event.type", hashMapOf("exampleKey" to "exampleValue"))
    .observe(viewLifecycleOwner, Observer { result ->
        if (result.status == OptableSDK.Status.SUCCESS) {
            Log.i("Witness API Success... ")
        } else {
            // result.status is OptableSDK.Status.ERROR
            // result.message is the error message
            Log.e("Witness API Error: ${result.message}")
        }
    })
```

#### Java

```java
import co.optable.android_sdk.OptableSDK;
import co.optable.demoappjava.MainActivity;
import android.util.Log;
import java.util.HashMap;
...
HashMap<String, String> eventProperties = new HashMap<String, String>();
eventProperties.put("exampleKey", "exampleValue");

MainActivity.OPTABLE
    .witness("example.event.type", eventProperties)
    .observe(getViewLifecycleOwner(), result -> {
        if (result.getStatus() == OptableSDK.Status.SUCCESS) {
            Log.i(null, "Witness API Success... ");
        } else {
            // result.getStatus() is OptableSDK.Status.ERROR
            // result.getMessage() is the error message
            Log.e(null, "Witness API Error: " + result.getMessage());
        }
    });
```

The specified event type and properties are associated with the logged event and which can be used for matching during audience assembly. The optional witness event properties are of type `OptableWitnessProperties` which is an alias for `HashMap<String,String>`, and should consist only of string key-value pairs.

### Integrating GAM360

We can further extend the above `targeting` example to show an integration with a [Google Ad Manager 360](https://admanager.google.com/home/) ad server account:

#### Kotlin

```kotlin
import co.optable.android_sdk.OptableSDK
import my.org.app.MainActivity
import android.util.Log
import com.google.android.gms.ads.doubleclick.PublisherAdRequest
import com.google.android.gms.ads.doubleclick.PublisherAdView
...
MainActivity.OPTABLE!!
    .targeting()
    .observe(viewLifecycleOwner, Observer { result ->
        // Build GAM360 ad request:
        var adRequest = PublisherAdRequest.Builder()

        if (result.status == OptableSDK.Status.SUCCESS) {
            Log.i("Targeting API Success... ")

            // result.data!! can be iterated to get targeting key values:
            result.data!!.forEach { (key, values) ->
                // Add key values to GAM360 ad request:
                adRequest.addCustomTargeting(key, values)
                Log.i("Targeting KV: ${key} = ${values}")
            }
        } else {
            // result.status is OptableSDK.Status.ERROR
            // result.message is the error message
            Log.e("Targeting API Error: ${result.message}")
        }

        // Assuming pubAdView refers to a pre-configured instance of
        // com.google.android.gms.ads.doubleclick.PublisherAdView:
        pubAdView.loadAd(adRequest.build())
    })
```

#### Java

```java
import co.optable.android_sdk.OptableSDK;
import co.optable.demoappjava.MainActivity;
import android.util.Log;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
...
MainActivity.OPTABLE.targeting().observe(getViewLifecycleOwner(), result -> {
    // Build GAM360 ad request:
    PublisherAdRequest.Builder adRequest = new PublisherAdRequest.Builder();

    if (result.getStatus() == OptableSDK.Status.SUCCESS) {
        Log.i(null, "Targeting API Success... ");

        // result.getData() can be iterated to get targeting key values:
        result.getData().forEach((key, values) -> {
            // Add key values to GAM360 ad request:
            adRequest.addCustomTargeting(key, values);
            Log.i(null, "Targeting KV: " + key.toString() + " = " + values.toString());
        });
    } else {
        // result.getStatus() is OptableSDK.Status.ERROR
        // result.getMessage() is the error message
        Log.e(null, "Targeting API Error: " + result.getMessage());
    }

    // Assuming pubAdView refers to a pre-configured instance of
    // com.google.android.gms.ads.doubleclick.PublisherAdView:
    pubAdView.loadAd(adRequest.build());
});
```

Working examples are available in the Kotlin and Java SDK demo applications.

## Identifying visitors arriving from Email newsletters

If you send Email newsletters that contain links to your application (e.g., deep links), then you may want to automatically _identify_ visitors that have clicked on any such links via their Email address. Incoming application traffic which is originating from a subscriber click on a link in a newsletter is considered to be implicitly authenticated by the recipient of the Email, therefore serving as an excellent source of linking of online user identities.

### Insert oeid into your Email newsletter template

To enable automatic identification of visitors originating from your Email newsletter, you first need to include an **oeid** parameter in the query string of all links to your website in your Email newsletter template. The value of the **oeid** parameter should be set to the SHA256 hash of the lowercased Email address of the recipient. For example, if you are using [Braze](https://www.braze.com/) to send your newsletters, you can easily encode the SHA256 hash value of the recipient's Email address by setting the **oeid** parameter in the query string of any links to your application as follows:

```
oeid={{${email_address} | downcase | sha2}}
```

The above example uses various personalization tags as documented in [Braze's user guide](https://www.braze.com/docs/user_guide/personalization_and_dynamic_content/) to dynamically insert the required data into an **oeid** parameter, all of which should make up a _part_ of the destination URL in your template.

### Capture clicks on deep links in your application

In order for your application to open on devices where it is installed when a link to your domain is clicked, you need to [configure and prepare your application to handle deep links](https://developer.android.com/training/app-links/deep-linking) first.

### Call tryIdentifyFromURI SDK API

When Android launches your app after a user clicks on a link, it will start your app activity with your configured _intent filters_. You can then obtain the `Uri` of the link by calling `getData()`, and pass it to the SDK's `tryIdentifyFromURI()` API which will automatically look for `oeid` in the query parameters of the `Uri` and call `identify` with its value if found.

For example, you can call `getData()` on the incoming `Intent` from your `onCreate()` activity lifecycle callback as follows:

#### Kotlin

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContentView(R.layout.main)
  ...
  val data: Uri? = intent?.data
  if (data != null) {
    MainActivity.OPTABLE!!.tryIdentifyFromURI(data)
  }
  ...
}
```

 #### Java

```java
@Override
public void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.main);
  ...
  Intent intent = getIntent();
  Uri data = intent.getData();
  if (data != null) {
    MainActivity.OPTABLE.tryIdentifyFromURI(data);
  }
  ...
}
```

## Demo Applications

The Kotlin and Java demo applications show a working example of `identify`, `targeting`, and `witness` APIs, as well as an integration with the [Google Ad Manager 360](https://admanager.google.com/home/) ad server, enabling the targeting of ads served by GAM360 to audiences activated in the [Optable](https://optable.co/) sandbox.

By default, the demo applications will connect to the [Optable](https://optable.co/) demo sandbox at `sandbox.optable.co` and reference application slug `android-sdk-demo`. The demo apps depend on the [GAM Mobile Ads SDK for Android](https://developers.google.com/ad-manager/mobile-ads-sdk/android/quick-start) and load ads from a GAM360 account operated by [Optable](https://optable.co/).

### Building

To build the Kotlin demo app, from [Android Studio](https://developer.android.com/studio), navigate to `File > Open` and open the `DemoApp/DemoAppKotlin` directory. To build the Java demo app, open the `DemoApp/DemoAppJava` directory. In both cases, you should be able to build and run the resulting project directly, since it will automatically download the `co.optable.android_sdk` library from the [JitPack](https://jitpack.io/) Maven repository.
