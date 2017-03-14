package com.eje_c.playerservice.sample;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.eje_c.playerservice.BufferingListener;
import com.eje_c.playerservice.Player;
import com.eje_c.playerservice.PlayerService;

public class MainActivity extends Activity {
    private static final String TAG = "PlayerService_Sample";
    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: " + name);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: " + name);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent playerServiceIntent = new Intent(this, PlayerService.class)
                .putExtra(PlayerService.EXTRA_USE_EXO_PLAYER, true);

        bindService(playerServiceIntent, conn, BIND_AUTO_CREATE);

        // Set UI
        setContentView(R.layout.activity_main);

        final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Player player = PlayerService.getPlayer();
                if (player != null) {
                    player.prepareFor(Uri.parse("http://techslides.com/demos/sample-videos/small.mp4"));
                    player.start();

                    player.setSurface(surfaceView.getHolder().getSurface());

                    player.onEnd(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Ended", Toast.LENGTH_SHORT).show();
                        }
                    });

                    player.setBufferingListener(new BufferingListener() {
                        @Override
                        public void onStartBuffering() {
                            Log.d(TAG, "onStartBuffering: ");
                        }

                        @Override
                        public void onEndBuffering() {
                            Log.d(TAG, "onEndBuffering: ");
                        }
                    });
                }
            }
        });

        findViewById(R.id.play_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Player player = PlayerService.getPlayer();
                if (player != null) {
                    if (player.isPlaying()) {
                        player.pause();
                    } else {
                        player.start();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {

        // Unbinding from service releases internal player automatically.
        unbindService(conn);

        super.onDestroy();
    }
}
