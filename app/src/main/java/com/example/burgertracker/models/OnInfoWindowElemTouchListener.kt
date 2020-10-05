package com.example.burgertracker.models

import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import com.google.android.gms.maps.model.Marker


abstract class OnInfoWindowElemTouchListener(
    private val view: View
) :
    OnTouchListener {
    private val handler = Handler()
    private var marker: Marker? = null
    fun setMarker(marker: Marker?) {
        this.marker = marker
    }

    override fun onTouch(vv: View, event: MotionEvent): Boolean {
        if (0 <= event.x && event.x <= view.width && 0 <= event.y && event.y <= view.height) {
            Log.d(
                "InfoWindow",
                "Like Button clicked from onTouch - ${marker?.title} action is ${event.actionMasked}"
            )
            when (event.actionMasked) {
                //MotionEvent.ACTION_DOWN -> startPress()
                MotionEvent.ACTION_UP -> handler.postDelayed(confirmClickRunnable, 150)
                //MotionEvent.ACTION_CANCEL -> endPress()
                else -> {
                }
            }
        }
        return false
    }

    private val confirmClickRunnable = Runnable {
        Log.d("MapActivity", "calling onClickConfirmed")
        onClickConfirmed(view, marker)
    }

    /**
     * This is called after a successful click
     */
    protected abstract fun onClickConfirmed(v: View?, marker: Marker?)
}