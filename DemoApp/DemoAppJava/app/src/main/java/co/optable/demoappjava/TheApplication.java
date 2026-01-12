package co.optable.demoappjava;

import android.app.Application;
import co.optable.android_sdk.OptableConfig;
import co.optable.android_sdk.OptableSDK;

public class TheApplication extends Application {

    public static OptableSDK optable;

    @Override
    public void onCreate() {
        super.onCreate();

        OptableConfig config = new OptableConfig(this, "prebidtest", "js-sdk");
        optable = new OptableSDK(config);
    }

}
