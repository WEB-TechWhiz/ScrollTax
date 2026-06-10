package com.scrolltax.intervention

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.scrolltax.app.R
import com.scrolltax.data.model.DismissalType
import com.scrolltax.data.model.InterventionType
import com.scrolltax.data.model.MonkeyTone
import com.scrolltax.data.model.SessionInternalState
import com.scrolltax.data.repository.InterventionRepository
import com.scrolltax.data.repository.SettingsRepository
import com.scrolltax.data.repository.TrackingRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class OverlayService : Service() {

    @Inject
    lateinit var interventionRepository: InterventionRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var trackingRepository: TrackingRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val handler = Handler(Looper.getMainLooper())

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var currentInterventionId: Long? = null
    private var currentSessionId: String? = null

    companion object {
        const val ACTION_SHOW_INTERVENTION = "com.scrolltax.intervention.SHOW"
        const val ACTION_DISMISS = "com.scrolltax.intervention.DISMISS"
        const val EXTRA_SESSION_ID = "session_id"
        const val EXTRA_PACKAGE_NAME = "package_name"
        const val EXTRA_INTERVENTION_TYPE = "intervention_type"
        const val EXTRA_SCORE = "score"

        private const val AUTO_DISMISS_MS = 8000L
        private const val MONKEY_AUTO_DISMISS_MS = 12000L
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_INTERVENTION -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: return START_NOT_STICKY
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: return START_NOT_STICKY
                val typeName = intent.getStringExtra(EXTRA_INTERVENTION_TYPE) ?: return START_NOT_STICKY
                val score = intent.getIntExtra(EXTRA_SCORE, 0)

                val type = try {
                    InterventionType.valueOf(typeName)
                } catch (e: IllegalArgumentException) {
                    return START_NOT_STICKY
                }

                showIntervention(sessionId, packageName, type, score)
            }
            ACTION_DISMISS -> dismissOverlay()
        }
        return START_NOT_STICKY
    }

    private fun showIntervention(sessionId: String, packageName: String, type: InterventionType, score: Int) {
        // Check if we can draw overlays
        if (!Settings.canDrawOverlays(this)) {
            return
        }

        // Dismiss existing overlay
        dismissOverlay()

        currentSessionId = sessionId

        serviceScope.launch {
            val settings = settingsRepository.getSettingsSync()
            val tone = settings.monkeyTone
            val reducedMotion = settings.reducedMotion

            // Record intervention
            currentInterventionId = interventionRepository.recordIntervention(sessionId, packageName, type)

            // Update session state
            when (type) {
                InterventionType.SMALL_CHIP -> {
                    trackingRepository.updateSessionState(sessionId, SessionInternalState.WARNING_SHOWN)
                }
                InterventionType.STRONG_CHIP -> {
                    trackingRepository.updateSessionState(sessionId, SessionInternalState.WARNING_SHOWN)
                }
                InterventionType.INTENT_CARD -> {
                    trackingRepository.updateSessionState(sessionId, SessionInternalState.WARNING_SHOWN)
                }
                InterventionType.MONKEY_BUBBLE, InterventionType.MONKEY_ANGRY -> {
                    trackingRepository.updateSessionState(sessionId, SessionInternalState.MONKEY_TRIGGERED)
                }
            }

            // Create overlay view
            val overlayView = createOverlayView(type, score, tone, reducedMotion)
            this@OverlayService.overlayView = overlayView

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                when (type) {
                    InterventionType.SMALL_CHIP, InterventionType.STRONG_CHIP -> 
                        WindowManager.LayoutParams.WRAP_CONTENT
                    else -> WindowManager.LayoutParams.MATCH_PARENT
                },
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
                else 
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = when (type) {
                    InterventionType.SMALL_CHIP -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
                    InterventionType.STRONG_CHIP -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
                    InterventionType.INTENT_CARD -> Gravity.CENTER
                    InterventionType.MONKEY_BUBBLE, InterventionType.MONKEY_ANGRY -> Gravity.BOTTOM or Gravity.START
                }
                y = when (type) {
                    InterventionType.SMALL_CHIP -> 100
                    InterventionType.STRONG_CHIP -> 100
                    else -> 0
                }
            }

            windowManager?.addView(overlayView, params)

            // Auto dismiss
            val dismissTime = if (type == InterventionType.MONKEY_BUBBLE || type == InterventionType.MONKEY_ANGRY) 
                MONKEY_AUTO_DISMISS_MS else AUTO_DISMISS_MS

            handler.postDelayed({ dismissOverlay() }, dismissTime)
        }
    }

    private fun createOverlayView(type: InterventionType, score: Int, tone: MonkeyTone, reducedMotion: Boolean): View {
        return when (type) {
            InterventionType.SMALL_CHIP -> createSmallChip(score)
            InterventionType.STRONG_CHIP -> createStrongChip(score)
            InterventionType.INTENT_CARD -> createIntentCard(score)
            InterventionType.MONKEY_BUBBLE -> createMonkeyBubble(tone, reducedMotion)
            InterventionType.MONKEY_ANGRY -> createAngryMonkey(tone, reducedMotion)
        }
    }

    private fun createSmallChip(score: Int): View {
        val view = LayoutInflater.from(this).inflate(R.layout.overlay_small_chip, null)

        val chipText = view.findViewById<TextView>(R.id.chip_text)
        chipText.text = getString(R.string.chip_small)

        val dismissBtn = view.findViewById<View>(R.id.chip_dismiss)
        dismissBtn.setOnClickListener { 
            dismissOverlay(DismissalType.IGNORE)
        }

        return view
    }

    private fun createStrongChip(score: Int): View {
        val view = LayoutInflater.from(this).inflate(R.layout.overlay_strong_chip, null)

        val chipText = view.findViewById<TextView>(R.id.chip_text)
        chipText.text = getString(R.string.chip_strong)

        val dismissBtn = view.findViewById<View>(R.id.chip_dismiss)
        dismissBtn.setOnClickListener { 
            dismissOverlay(DismissalType.IGNORE)
        }

        return view
    }

    private fun createIntentCard(score: Int): View {
        val view = LayoutInflater.from(this).inflate(R.layout.overlay_intent_card, null)

        val titleText = view.findViewById<TextView>(R.id.card_title)
        val descText = view.findViewById<TextView>(R.id.card_description)
        val continueBtn = view.findViewById<Button>(R.id.btn_continue)
        val exitBtn = view.findViewById<Button>(R.id.btn_exit)

        titleText.text = getString(R.string.intent_card_title)
        descText.text = getString(R.string.intent_card_desc, score)

        continueBtn.setOnClickListener {
            dismissOverlay(DismissalType.CONTINUE)
        }

        exitBtn.setOnClickListener {
            dismissOverlay(DismissalType.EXIT_APP)
            // Launch home
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)
        }

        return view
    }

    private fun createMonkeyBubble(tone: MonkeyTone, reducedMotion: Boolean): View {
        val view = LayoutInflater.from(this).inflate(R.layout.overlay_monkey_bubble, null)

        val bubbleText = view.findViewById<TextView>(R.id.monkey_text)
        val monkeyAnimation = view.findViewById<LottieAnimationView>(R.id.monkey_animation)
        val dismissBtn = view.findViewById<View>(R.id.monkey_dismiss)

        bubbleText.text = when (tone) {
            MonkeyTone.FUNNY -> getString(R.string.monkey_bubble_funny)
            MonkeyTone.BALANCED -> getString(R.string.monkey_bubble_balanced, 3)
            MonkeyTone.SAVAGE -> getString(R.string.monkey_bubble_savage)
        }

        if (!reducedMotion) {
            monkeyAnimation.setAnimation("monkey_idle.json")
            monkeyAnimation.playAnimation()
        } else {
            monkeyAnimation.setImageResource(R.drawable.monkey_static)
        }

        dismissBtn.setOnClickListener {
            dismissOverlay(DismissalType.IGNORE)
        }

        return view
    }

    private fun createAngryMonkey(tone: MonkeyTone, reducedMotion: Boolean): View {
        val view = LayoutInflater.from(this).inflate(R.layout.overlay_monkey_angry, null)

        val monkeyAnimation = view.findViewById<LottieAnimationView>(R.id.monkey_angry_animation)
        val dismissBtn = view.findViewById<View>(R.id.angry_dismiss)

        if (!reducedMotion) {
            monkeyAnimation.setAnimation("monkey_angry.json")
            monkeyAnimation.playAnimation()
        } else {
            monkeyAnimation.setImageResource(R.drawable.monkey_angry_static)
        }

        dismissBtn.setOnClickListener {
            dismissOverlay(DismissalType.IGNORE)
        }

        return view
    }

    private fun dismissOverlay(dismissalType: DismissalType = DismissalType.TIMEOUT) {
        overlayView?.let { view ->
            windowManager?.removeView(view)
            overlayView = null
        }

        serviceScope.launch {
            currentInterventionId?.let { id ->
                val wasEffective = dismissalType == DismissalType.EXIT_APP
                interventionRepository.dismissIntervention(id, dismissalType, wasEffective)

                if (wasEffective) {
                    showExitSuccessToast()
                }
            }

            currentSessionId?.let { sessionId ->
                if (dismissalType == DismissalType.EXIT_APP) {
                    trackingRepository.updateSessionState(sessionId, SessionInternalState.SESSION_EXITED)
                } else if (dismissalType == DismissalType.IGNORE) {
                    trackingRepository.updateSessionState(sessionId, SessionInternalState.WARNING_IGNORED)
                }
            }
        }

        currentInterventionId = null
        currentSessionId = null
    }

    private fun showExitSuccessToast() {
        val toast = Toast.makeText(this, R.string.exit_success, Toast.LENGTH_LONG)
        toast.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissOverlay()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
