package com.salihutimothy.myaudiojournalapp.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.salihutimothy.myaudiojournalapp.R
import com.salihutimothy.myaudiojournalapp.database.Constants
import com.salihutimothy.myaudiojournalapp.services.RecordingService
import com.salihutimothy.myaudiojournalapp.services.RecordingService.Companion.maxAmplitude
import com.salihutimothy.myaudiojournalapp.views.Typewriter
import com.salihutimothy.myaudiojournalapp.views.WaveformView
import java.util.*
import kotlin.math.roundToInt


class RecordFragment : Fragment() {

    private lateinit var chronometer: Chronometer
    private lateinit var recordingStatus: TextView
    private lateinit var timerText: TextView
//    private lateinit var recordButton: ImageButton
//    private lateinit var pauseButton: ImageButton
//    private lateinit var listButton: ImageButton
    private lateinit var recordButton: FloatingActionButton
    private lateinit var pauseButton: FloatingActionButton
    private lateinit var listButton: FloatingActionButton
    private lateinit var nextButton: ImageButton
    private lateinit var waveformView: WaveformView
    private lateinit var recordingIcon: ImageView
    private lateinit var journalPrompt: Typewriter
    private lateinit var timer: Timer
    private lateinit var progressBar: ProgressBar

    private lateinit var navController: NavController
//    private lateinit var timerr : Tim

    private var mStartRecording = true
    private var mPauseRecording = true
    private var timeWhenPaused = 0L
    private var mPromptList: ArrayList<String>? = null
    private var promptText : String? = null

    @RequiresApi(Build.VERSION_CODES.R)
    var permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.MANAGE_EXTERNAL_STORAGE
    )

    private val PERMISSION_CODE = 21

    companion object {
        fun newInstance() =
            RecordFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkrequestPermissions()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recorrd, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pauseButton = view.findViewById(R.id.btnPause) as FloatingActionButton
//        timerText = view.findViewById(R.id.tv_timer) as TextView
        listButton = view.findViewById(R.id.btnList) as FloatingActionButton
        recordButton = view.findViewById(R.id.btnRecord) as FloatingActionButton
//        nextButton = view.findViewById(R.id.next) as ImageButton
        waveformView = view.findViewById(R.id.waveformView) as WaveformView
        recordingIcon = view.findViewById(R.id.isRecording) as ImageView
        journalPrompt = view.findViewById(R.id.journalTxt) as Typewriter
//        progressBar = view.findViewById(R.id.recordProgressBar) as ProgressBar
        navController = Navigation.findNavController(view)

//        pauseButton.visibility = View.GONE
//        recordButton.colorPressed = resources.getColor(R.color.background_tab_pressed)

//        Has user opened app previously?
        if (restorePrefData()) {
//            journalPrompt.visibility = View.VISIBLE
//            journalPrompt.setCharacterDelay(100)
//            journalPrompt.animateText(
//                "Hiiiiiii (^O^)\n" +
//                        "Welcome to Audio Journal *\\(^o^)/*\n" +
//                        "Click the button below to record your thoughts (^-^)"
//            )

        } else {
            savePrefsData()
        }

        var prompt = true


        recordButton.setOnClickListener {
//            Log.d("TAG", "promptname 1 $promptText")
//
//            if (prompt) {
//                promptText = null
////                promptText = journalPrompt.text as String?
//            }
//            Log.d("TAG", "promptname 1.5 $promptText")
//
//            journalPrompt.text = ""
//            journalPrompt.visibility = View.GONE
//            nextButton.visibility = View.GONE
//            progressBar.isEnabled = false
            onRecord(mStartRecording)
        }

        listButton.setOnClickListener {
            navController.navigate(R.id.action_recordFragment_to_audioListFragment);
        }

        pauseButton.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }


//        progressBar.setOnClickListener {
//            journalPrompt.setTextColor(ContextCompat.getColor(requireContext(), (R.color.white)))
//
//            if (prompt) {
//                journalPrompt.visibility = View.VISIBLE
//                nextButton.visibility = View.VISIBLE
//
////                journalPrompt.setCharacterDelay(70)
////                journalPrompt.animateText("What are 3 things you are grateful for today? ^-^")
//                prompt = false
//            } else {
//                journalPrompt.visibility = View.GONE
//                nextButton.visibility = View.GONE
//
//                prompt = true
//            }
//
//        }

        mPromptList = Constants.getPrompts()


//        nextButton.setOnClickListener {
//            val rand = (0 until mPromptList!!.size).random()
//
//            journalPrompt.setCharacterDelay(65)
//            journalPrompt.animateText(mPromptList!![rand])
////            journalPrompt.animateText("What can you do today now that will improve your circumstances? p(^-^)q")
//
//            promptText = mPromptList!![rand]
//            Log.d("TAG", "promptname 0 $promptText")
//
//        }


    }

    private fun onRecord(start: Boolean) {
        val intent = Intent(context, RecordingService::class.java)
        recordButton = requireView().findViewById(R.id.btnRecord) as FloatingActionButton
        listButton = requireView().findViewById(R.id.btnList) as FloatingActionButton
        pauseButton = requireView().findViewById(R.id.btnPause) as FloatingActionButton
        recordingStatus = requireView().findViewById(R.id.recording_status_txt) as TextView
        chronometer = requireView().findViewById(R.id.chronometer) as Chronometer
        waveformView = requireView().findViewById(R.id.waveformView) as WaveformView
        recordingIcon = requireView().findViewById(R.id.isRecording) as ImageView
//        progressBar = requireView().findViewById(R.id.recordProgressBar) as ProgressBar
        navController = Navigation.findNavController(requireView())
//        timerText = requireView().findViewById(R.id.tv_timer) as TextView

        if (start) {
            // check permission to record audio
            if (
                checkPermissions()
            ) {
                Log.d("RecordFragment", "onREcord - start record")
                mStartRecording = !mStartRecording
                recordingIcon.visibility = View.VISIBLE
//                recordButton.setImageResource(R.drawable.ic_stop)
//                recordButton.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_stop)
//                recordButton.setBackgroundColor(ContextCompat.getColor(requireContext(), (R.color.red)))
//                recordButton.background =
//                    ContextCompat.getDrawable(requireContext(), R.drawable.button_bg_red2)
//                recordButton.setImageResource(R.drawable.ic_stop)
//                recordButton.setPadding(dpToPx(25))
//                Log.d("RecordFragment", "padding: ${dpToPx(25)}")
//                recordButton.scaleType = ImageView.ScaleType.FIT_CENTER

//                progressBar.progressDrawable =
//                    ContextCompat.getDrawable(requireContext(), R.drawable.record_progress_bar_stop)
//                Toast.makeText(context, "Recording started", Toast.LENGTH_SHORT).show()

                listButton.isEnabled = false
                pauseButton.isEnabled = false
                recordButton.setImageResource(R.drawable.ic_stop)
                chronometer.base = SystemClock.elapsedRealtime()
//                    chronometer.format = "00:%s"
                chronometer.start()

//                var timerSeconds = 0L
//
//                chronometer.setOnChronometerTickListener {
//                    val timerInMinutes = timerSeconds / 60
//                    val timerInSeconds = timerSeconds % 60
//                    val secondsStr = timerInSeconds.toString()
//
//                    timerText.text = "$timerInMinutes:${if (secondsStr.length == 2) secondsStr else "0" + secondsStr}"
//
//                    timerSeconds++
//                    Log.d("RecordFragment", "timer text $timerSeconds")
//
//
//                }

                timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        waveformView.addAmplitude(maxAmplitude)

//                        Log.d("RecordFragment", "timer still running $maxAmplitude")

                    }
                }, 0, 80)

                val bundle = Bundle()
                Log.d("TAG", "promptname 2 $promptText")

                bundle.putString("prompt", promptText)
                intent.putExtras(bundle)

                activity?.startService(intent)

                activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

                recordingStatus.text = "Recording..."
            } else {
                Log.d("RecordFragment", "onRecord - request permissions")
                requestPermissions()
            }

        } else {

            mStartRecording = !mStartRecording
            recordButton.setImageResource(R.drawable.ic_mic)

//            recordButton.setImageResource(R.drawable.ic_placeholder)
//            recordButton.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_start)
//            recordButton.setBackgroundColor(ContextCompat.getColor(requireContext(), (R.color.green)))


//            progressBar.progressDrawable =
//                ContextCompat.getDrawable(requireContext(), R.drawable.record_progress_bar)

//            recordButton.background =
//                ContextCompat.getDrawable(requireContext(), R.drawable.button_bg_orange)
//            recordButton.setImageResource(R.drawable.ic_mic)
//            recordButton.setPadding(dpToPx(25))
//            recordButton.scaleType = ImageView.ScaleType.FIT_CENTER

            listButton.isEnabled = true
            pauseButton.isEnabled = true
            recordingIcon.visibility = View.GONE
            chronometer.stop()
            timer.cancel()
            waveformView.clear()
            chronometer.base = SystemClock.elapsedRealtime()
            timeWhenPaused = 0
            recordingStatus.text = "Tap the Button to start recording"

            activity?.stopService(intent)

            Log.d("RecordFragment", "onRecord - stop record")

            navController.navigate(R.id.action_recordFragment_to_audioListFragment);

        }
    }

    private fun dpToPx(dp: Int): Int {
        val density: Float = requireContext().resources
            .displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }

    var listPermissionsNeeded: MutableList<String> = ArrayList()

    private fun checkrequestPermissions() {
        var result: Int
        for (p in permissions) {
            result = ActivityCompat.checkSelfPermission(requireContext(), p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                (listPermissionsNeeded as List<String>).toTypedArray(),
                PERMISSION_CODE
            )

            Log.d("RecordFragment", "permissions - $listPermissionsNeeded[0]")

        }

        // reset the permission array
        listPermissionsNeeded = ArrayList()
    }

    private fun checkPermissions(): Boolean {
        //Check permission
        Log.d("RecordFragment", "permissions - checking permission")

        var result: Int
        for (p in permissions) {
            result = ActivityCompat.checkSelfPermission(requireContext(), p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            Log.d(
                "RecordFragment",
                "permissions - some permission not granted: ${listPermissionsNeeded.toString()}"
            )
            return false
        }

        return true
    }

    private fun requestPermissions() {
        Log.d(
            "RecordFragment",
            "permissions - requesting permissions: ${listPermissionsNeeded.toString()}"
        )

        checkrequestPermissions()

        checkPermissions()

        for (p in listPermissionsNeeded) {
            mPermissionResult.launch(p)
        }
    }

    private val mPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->

        when {
            result -> {
                Log.e(ContentValues.TAG, "onActivityResult: PERMISSION GRANTED")
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {

            }
            else -> {
                Log.e(ContentValues.TAG, "onActivityResult: PERMISSION DENIED")
                AlertDialog.Builder(requireContext())
                    .setTitle("Permissions Required")
                    .setMessage("This app may not work correctly without the requested permission. Open the app settings screen to modify app permissions.")
                    .setPositiveButton("Settings") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", requireContext().packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel") { dialog, which -> }
                    .create()
                    .show()
            }
        }

        listPermissionsNeeded = ArrayList()

    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(context, RecordingService::class.java)
        activity?.stopService(intent)
    }

    private fun restorePrefData(): Boolean {
        val pref: SharedPreferences =
            requireContext().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        return pref.getBoolean("isAppOpened", false)
    }

    private fun savePrefsData() {
        val pref: SharedPreferences =
            requireContext().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean("isAppOpened", true)
        editor.apply()
    }


}