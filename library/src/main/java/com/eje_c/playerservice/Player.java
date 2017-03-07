package com.eje_c.playerservice;


import android.net.Uri;
import android.view.Surface;

/**
 * Abstract interface for internal MediaPlayer or ExoPlayer.
 */
public interface Player {

    /**
     * Starts or resumes playback. If playback had previously been paused,
     * playback will continue from where it was paused. If playback had
     * been stopped, or never started before, playback will start at the
     * beginning.
     */
    void start();

    /**
     * Pauses playback. Call start() to resume.
     */
    void pause();

    /**
     * Stops playback after playback has been stopped or paused.
     */
    void stop();

    /**
     * Releases resources associated with this Player object.
     */
    void release();

    /**
     * Sets the volume on this player.
     *
     * @param volume Volume
     */
    void setVolume(float volume);

    /**
     * Sets the data source as a content Uri.
     *
     * @param uri the Content URI of the data you want to play
     */
    void prepareFor(Uri uri);

    /**
     * Sets the Surface to be used as the sink for the video portion of the media.
     *
     * @param surface The Surface to be used for the video portion of the media.
     */
    void setSurface(Surface surface);

    /**
     * Register a callback to be invoked when the end of a media source has been reached during playback.
     *
     * @param runnable the callback that will be run
     */
    void onEnd(Runnable runnable);

    /**
     * Checks whether the MediaPlayer is playing.
     *
     * @return true if currently playing, false otherwise
     */
    boolean isPlaying();

    /**
     * Returns the width of the video.
     *
     * @return the width of the video, or 0 if there is no video, no display surface was set, or the width has not been determined yet.
     */
    int getVideoWidth();

    /**
     * Returns the height of the video.
     *
     * @return the height of the video, or 0 if there is no video, no display surface was set, or the height has not been determined yet.
     */
    int getVideoHeight();

    /**
     * Seeks to specified time position.
     *
     * @param msec the offset in milliseconds from the start to seek to
     */
    void seekTo(long msec);

    /**
     * Gets the current playback position.
     *
     * @return the current position in milliseconds
     */
    long getCurrentPosition();

    /**
     * Gets the duration of the file.
     *
     * @return the duration in milliseconds, if no duration is available (for example, if streaming live content), -1 is returned.
     */
    long getDuration();

    /**
     * Register a callback to be invoked when the status of a network stream's buffer has changed.
     *
     * @param bufferingListener the callback that will be run.
     */
    void setBufferingListener(BufferingListener bufferingListener);
}