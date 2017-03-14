package com.eje_c.playerservice;

public interface BufferingListener {
    /**
     * Called to update status in buffering a media stream received through progressive HTTP download.
     */
    void onStartBuffering();

    /**
     * Called to update status in buffering a media stream received through progressive HTTP download.
     */
    void onEndBuffering();
}
