package com.eje_c.playerservice;

public interface OnRenderStartListener {
    /**
     * Called when a frame is rendered for the first time since setting the surface, and when a frame is rendered for the first time since a video track was selected.
     */
    void onStartRendering();
}
