package com.eje_c.playerservice;

public interface OnExceptionListener {
    /**
     * Called when an error occurs.
     *
     * @param e The error.
     */
    void onException(Exception e);
}
