package com.google.android.youtube.player

import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.youtube.player.internal.ab

open class YouTubePlayerSupportFragmentXKt : Fragment(), YouTubePlayer.Provider {

    private val callbackHandler: CallbackHandler = CallbackHandler()
    private var viewState: Bundle? = null
    private var view: YouTubePlayerView? = null
    private var developerKey: String? = null
    private var initializationListener: YouTubePlayer.OnInitializedListener? = null
    private val isInitialized = false

    private fun initializeIfReady() {
        if (view != null && initializationListener != null) {
            view?.a(isInitialized)
            view?.a(this.activity, this, developerKey, initializationListener, viewState)
            viewState = null
            initializationListener = null
        }
    }

    override fun initialize(key: String?, listener: YouTubePlayer.OnInitializedListener?) {
        this.developerKey = ab.a(key, "Developer key cannot be null or empty")
        this.initializationListener = listener

        this.initializeIfReady()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewState =
            savedInstanceState?.getBundle("YouTubePlayerSupportFragment.KEY_PLAYER_VIEW_STATE")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = YouTubePlayerView(this.activity, null as AttributeSet?, 0, callbackHandler)
        this.initializeIfReady()
        return view
    }

    override fun onStart() {
        super.onStart()

        view?.a()
    }

    override fun onResume() {
        super.onResume()

        view?.b()
    }

    override fun onPause() {
        view?.c()

        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val var2 = if (view != null) {
            (view as YouTubePlayerView).e()
        } else viewState

        outState.putBundle("YouTubePlayerSupportFragment.KEY_PLAYER_VIEW_STATE", var2)
    }

    override fun onStop() {
        view?.d()
        super.onStop()
    }

    override fun onDestroyView() {
        this.activity?.let {
            view?.c(it.isFinishing)
            view = null
        }

        super.onDestroyView()
    }

    override fun onDestroy() {
        if (view != null) {
            val var1 = this.activity
            (view as YouTubePlayerView).b(var1 == null || var1.isFinishing)
        }

        super.onDestroy()
    }

    companion object {
        fun newInstance(): YouTubePlayerSupportFragmentXKt {
            return YouTubePlayerSupportFragmentXKt()
        }
    }

    private class CallbackHandler : YouTubePlayerView.b {

        override fun a(
            view: YouTubePlayerView?,
            developerKey: String?,
            listener: YouTubePlayer.OnInitializedListener?
        ) {
            val fragment = newInstance()
            fragment.initialize(developerKey, fragment.initializationListener)
        }

        override fun a(p0: YouTubePlayerView?) {}
    }

}