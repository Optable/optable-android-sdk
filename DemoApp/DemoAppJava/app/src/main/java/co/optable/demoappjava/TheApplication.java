package co.optable.demoappjava;

import android.app.Application;
import co.optable.sdk.OptableConfig;
import co.optable.sdk.OptableSDK;

public class TheApplication extends Application {

    public static OptableSDK optable;

    @Override
    public void onCreate() {
        super.onCreate();

        OptableConfig config = new OptableConfig(this, "prebidtest", "android-sdk");
        config.setOrigin("https://www.optable.co");
        optable = new OptableSDK(config);
    }

}
