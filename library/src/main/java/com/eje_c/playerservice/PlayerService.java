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
 * It holds single or multiple {@link Player}. It can be retrieved from {@link #getPlayer()} or {@link #getPlayer(int)}.
 * Number of {@link Player} can be specified with {@link #EXTRA_PLAYER_COUNT} on binding to service.
 * ExoPlayer based {@link Player} will be used by default.
 * If you wish to use MediaPlayer based {@link Player}, specify {@link #EXTRA_USE_EXO_PLAYER} with {@code false} on binding to service.
 */
public class PlayerService extends Service {
    public static final String EXTRA_USE_EXO_PLAYER = "use_exo_player";
    public static final String EXTRA_PLAYER_COUNT = "player_count";
    private static final String TAG = "PlayerService";
    private static WeakReference<PlayerService> ref;
    private Player[] players;

    /**
     * Get {@link Player} instance or null if {@link PlayerService} was released or bound to activity yet.
     *
     * @param index must be 0 to count-1
     * @return Player or null if {@link PlayerService} was released or bound to activity yet.
     */
    public static Player getPlayer(int index) {

        if (ref != null) {
            PlayerService service = ref.get();
            if (service != null) {
                return service.players[index];
            }
        }

        return null;
    }

    /**
     * Get {@link Player} instance or null if {@link PlayerService} was released or bound to activity yet.
     *
     * @return Player or null if {@link PlayerService} was released or bound to activity yet.
     */
    public static Player getPlayer() {
        return getPlayer(0);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        boolean useExoPlayer = intent.getBooleanExtra(EXTRA_USE_EXO_PLAYER, true);
        int count = intent.getIntExtra(EXTRA_PLAYER_COUNT, 1);
        players = new Player[count];

        for (int i = 0; i < count; ++i) {
            if (useExoPlayer) {
                players[i] = new ExoPlayerImpl(this);
            } else {
                players[i] = new MediaPlayerImpl(this);
            }
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
        if (players != null) {
            for (Player p : players) {
                p.release();
            }
            players = null;
        }

        if (ref != null) {
            ref.clear();
            ref = null;
        }

        super.onDestroy();
    }
}
