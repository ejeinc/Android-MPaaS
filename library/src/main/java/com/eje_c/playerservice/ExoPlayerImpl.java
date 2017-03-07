package com.eje_c.playerservice;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

class ExoPlayerImpl implements Player {
    private static final String TAG = "ExoPlayerImpl";
    private final SimpleExoPlayer exoPlayer;
    private final DataSource.Factory dataSourceFactory;
    private final ExtractorsFactory extractorsFactory;
    private ExoPlayer.EventListener bufferingEventListener;

    ExoPlayerImpl(Context context) {

        dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, context.getPackageName()));
        extractorsFactory = new DefaultExtractorsFactory();

        // 1. Create a default TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create a default LoadControl
        LoadControl loadControl = new DefaultLoadControl();

        // 3. Create the player
        exoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl);
        exoPlayer.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
                Log.d(TAG, "onTimelineChanged: " + timeline + " " + manifest);
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                Log.d(TAG, "onTracksChanged: " + trackGroups + " " + trackSelections);
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                Log.d(TAG, "onLoadingChanged: " + isLoading);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                //            Log.d(TAG, "onPlayerStateChanged: " + playWhenReady + " " + playbackState);
                switch (playbackState) {
                    case ExoPlayer.STATE_IDLE:
                        Log.d(TAG, "onPlayerStateChanged: STATE_IDLE");
                        break;

                    case ExoPlayer.STATE_BUFFERING:
                        Log.d(TAG, "onPlayerStateChanged: STATE_BUFFERING");
                        break;

                    case ExoPlayer.STATE_READY:
                        Log.d(TAG, "onPlayerStateChanged: STATE_READY");
                        break;

                    case ExoPlayer.STATE_ENDED:
                        Log.d(TAG, "onPlayerStateChanged: STATE_ENDED");
                        break;
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.d(TAG, "onPlayerError: " + error);
            }

            @Override
            public void onPositionDiscontinuity() {
                Log.d(TAG, "onPositionDiscontinuity: ");
            }
        });
    }

    @Override
    public void start() {
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void pause() {
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void stop() {
        exoPlayer.stop();
    }

    @Override
    public void release() {
        exoPlayer.release();
    }

    @Override
    public void setVolume(float volume) {
        exoPlayer.setVolume(volume);
    }

    @Override
    public void prepareFor(Uri uri) {
        MediaSource videoSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
        exoPlayer.prepare(videoSource);
    }

    @Override
    public void setSurface(Surface surface) {
        exoPlayer.setVideoSurface(surface);
    }

    @Override
    public void onEnd(final Runnable runnable) {
        exoPlayer.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {
                    case ExoPlayer.STATE_ENDED:
                        exoPlayer.removeListener(this);
                        runnable.run();
                        break;
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
            }

            @Override
            public void onPositionDiscontinuity() {
            }
        });
    }

    @Override
    public boolean isPlaying() {
        return exoPlayer.getPlaybackState() > ExoPlayer.STATE_IDLE &&
                exoPlayer.getPlaybackState() < ExoPlayer.STATE_ENDED &&
                exoPlayer.getPlayWhenReady();
    }

    @Override
    public int getVideoWidth() {
        return exoPlayer.getVideoFormat().width;
    }

    @Override
    public int getVideoHeight() {
        return exoPlayer.getVideoFormat().height;
    }

    @Override
    public void seekTo(long msec) {
        exoPlayer.seekTo(msec);
    }

    @Override
    public long getCurrentPosition() {
        return exoPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return exoPlayer.getDuration();
    }

    @Override
    public void setBufferingListener(BufferingListener bufferingListener) {

        // Remove previous listener
        if (bufferingEventListener != null) {
            exoPlayer.removeListener(bufferingEventListener);
            bufferingListener = null;
        }

        // Register listener if it is not null
        if (bufferingListener != null) {

            final BufferingListener finalBufferingListener = bufferingListener;
            bufferingEventListener = new ExoPlayer.EventListener() {
                boolean buffering;

                @Override
                public void onTimelineChanged(Timeline timeline, Object manifest) {
                }

                @Override
                public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                }

                @Override
                public void onLoadingChanged(boolean isLoading) {
                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                    // Not in buffering
                    if (!buffering) {

                        // Start now
                        if (playbackState == ExoPlayer.STATE_BUFFERING) {
                            buffering = true;
                            finalBufferingListener.onStartBuffering();
                        }

                    } else {
                        // In buffering

                        // End now
                        if (playbackState != ExoPlayer.STATE_BUFFERING) {
                            buffering = false;
                            finalBufferingListener.onEndBuffering();
                        }
                    }
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
                }

                @Override
                public void onPositionDiscontinuity() {
                }
            };
            exoPlayer.addListener(bufferingEventListener);
        }
    }
}