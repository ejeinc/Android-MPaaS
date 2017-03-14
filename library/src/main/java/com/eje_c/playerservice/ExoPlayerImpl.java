package com.eje_c.playerservice;

import android.content.Context;
import android.net.Uri;
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

class ExoPlayerImpl implements Player, ExoPlayer.EventListener, SimpleExoPlayer.VideoListener {
    private static final String TAG = "ExoPlayerImpl";
    private final SimpleExoPlayer exoPlayer;
    private final DataSource.Factory dataSourceFactory;
    private final ExtractorsFactory extractorsFactory;
    private boolean buffering;
    private BufferingListener bufferingListener;
    private Runnable onEndCallback;
    private OnExceptionListener onExceptionListener;
    private OnRenderStartListener onRenderStartListener;

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
        exoPlayer.addListener(this);
        exoPlayer.setVideoListener(this);
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
    public void onEnd(Runnable runnable) {
        this.onEndCallback = runnable;
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
        this.bufferingListener = bufferingListener;
    }

    @Override
    public void onException(OnExceptionListener listener) {
        this.onExceptionListener = listener;
    }

    @Override
    public void setOnRenderStartListener(OnRenderStartListener listener) {
        this.onRenderStartListener = listener;
    }

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
                if (bufferingListener != null) {
                    bufferingListener.onStartBuffering();
                }
            }

        } else {
            // In buffering

            // End now
            if (playbackState != ExoPlayer.STATE_BUFFERING) {
                buffering = false;
                if (bufferingListener != null) {
                    bufferingListener.onEndBuffering();
                }
            }
        }

        // Call onEnd callback
        if (onEndCallback != null && playbackState == ExoPlayer.STATE_ENDED) {
            onEndCallback.run();
            onEndCallback = null;
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        if (onExceptionListener != null) {
            onExceptionListener.onException(error);
        }
    }

    @Override
    public void onPositionDiscontinuity() {
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
    }

    @Override
    public void onRenderedFirstFrame() {
        if (onRenderStartListener != null) {
            onRenderStartListener.onStartRendering();
        }
    }
}