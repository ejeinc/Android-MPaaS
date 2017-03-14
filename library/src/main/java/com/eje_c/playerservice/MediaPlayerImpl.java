package com.eje_c.playerservice;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Surface;

import java.io.IOException;

class MediaPlayerImpl implements Player, MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener {
    private final MediaPlayer mediaPlayer;
    private final Context context;
    private boolean playWhenReady;
    private boolean prepared;
    private OnRenderStartListener onVideoRenderingStartListener;
    private BufferingListener bufferingListener;

    MediaPlayerImpl(Context context) {
        this.mediaPlayer = new MediaPlayer();
        this.context = context;

        mediaPlayer.setOnInfoListener(this);
    }

    @Override
    public void start() {
        playWhenReady = true;

        if (prepared) {
            mediaPlayer.start();
        }
    }

    @Override
    public void pause() {
        playWhenReady = false;

        if (prepared) {
            mediaPlayer.pause();
        }
    }

    @Override
    public void stop() {
        playWhenReady = false;

        if (prepared) {
            mediaPlayer.stop();
        }
    }

    @Override
    public void release() {
        playWhenReady = false;
        mediaPlayer.release();
    }

    @Override
    public void setVolume(float volume) {
        mediaPlayer.setVolume(volume, volume);
    }

    @Override
    public void prepareFor(Uri uri) {
        mediaPlayer.reset();
        prepared = false;
        playWhenReady = false;

        try {
            mediaPlayer.setDataSource(context, uri);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(this);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void setSurface(Surface surface) {
        mediaPlayer.setSurface(surface);
    }

    @Override
    public void onEnd(final Runnable runnable) {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.setOnCompletionListener(null);
                runnable.run();
            }
        });
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public int getVideoWidth() {
        return mediaPlayer.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        return mediaPlayer.getVideoHeight();
    }

    @Override
    public void seekTo(long msec) throws IllegalStateException {
        mediaPlayer.seekTo((int) msec);
    }

    @Override
    public long getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public void setBufferingListener(final BufferingListener bufferingListener) {
        this.bufferingListener = bufferingListener;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        prepared = true;

        if (playWhenReady) {
            start();
        }
    }

    @Override
    public void setOnExceptionListener(final OnExceptionListener listener) {

        if (listener == null) {
            mediaPlayer.setOnErrorListener(null);
            return;
        }

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                listener.onException(new MediaPlayerException(what, extra));
                return false;
            }
        });
    }

    @Override
    public void setOnRenderStartListener(OnRenderStartListener listener) {
        this.onVideoRenderingStartListener = listener;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (bufferingListener != null) {
                    bufferingListener.onStartBuffering();
                }
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                if (bufferingListener != null) {
                    bufferingListener.onEndBuffering();
                }
                break;

            case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                if (onVideoRenderingStartListener != null) {
                    onVideoRenderingStartListener.onStartRendering();
                }
                break;
        }
        return false;
    }

    public static class MediaPlayerException extends Exception {
        public final int what;
        public final int extra;

        MediaPlayerException(int what, int extra) {
            this.what = what;
            this.extra = extra;
        }
    }
}