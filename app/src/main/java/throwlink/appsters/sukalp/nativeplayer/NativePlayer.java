package throwlink.appsters.sukalp.nativeplayer;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class NativePlayer extends Application {
    private static Context context;


    public NativePlayer(){

    }

    @Override
    public void onCreate() {
        super.onCreate();

        // getting application context
        NativePlayer.context = getApplicationContext();

        //Firebase offline capability
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //Picasso offline Capability
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
    }

    public static Context getAppContext() {
        return NativePlayer.context;
    }
}
