package com.eje_c.playerservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * MediaPlayer as a Service (MPaaS).
 * It holds single {@link Player}. It can be retrieved from {@link #getPlayer()}.
 */
public class PlayerService extends Service {
    public static final String EXTRA_USE_EXO_PLAYER = "use_exo_player";
    private static final String TAG = "PlayerService";
    private static WeakReference<PlayerService> ref;
    private Player player;

    /**
     * Get {@link Player} instance or null if {@link PlayerService} was released or bound to activity yet.
     *
     * @return Player or null if {@link PlayerService} was released or bound to activity yet.
     */
    public static Player getPlayer() {

        if (ref != null) {
            PlayerService service = ref.get();
            if (service != null) {
                return service.player;
            }
        }

        return null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        boolean useExoPlayer = intent.getBooleanExtra(EXTRA_USE_EXO_PLAYER, true);

        if (useExoPlayer) {
            player = new ExoPlayerImpl(this);
        } else {
            player = new MediaPlayerImpl(this);
        }

        return new Binder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");

        // Keep reference as static
        ref = new WeakReference<>(this);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");

        // Release player
        if (player != null) {
            player.release();
            player = null;
        }

        if (ref != null) {
            ref.clear();
            ref = null;
        }

        super.onDestroy();
    }
}
