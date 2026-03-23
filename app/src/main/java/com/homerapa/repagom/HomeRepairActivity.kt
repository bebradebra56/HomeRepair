package com.homerapa.repagom

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.homerapa.repagom.gefr.HomeRepairGlobalLayoutUtil
import com.homerapa.repagom.gefr.presentation.app.HomeRepairApplication
import com.homerapa.repagom.gefr.presentation.pushhandler.HomeRepairPushHandler
import com.homerapa.repagom.gefr.homeRepairSetupSystemBars
import org.koin.android.ext.android.inject

class HomeRepairActivity : AppCompatActivity() {

    private val homeRepairPushHandler by inject<HomeRepairPushHandler>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homeRepairSetupSystemBars()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_home_repair)

        val homeRepairRootView = findViewById<View>(android.R.id.content)
        HomeRepairGlobalLayoutUtil().homeRepairAssistActivity(this)
        ViewCompat.setOnApplyWindowInsetsListener(homeRepairRootView) { homeRepairView, homeRepairInsets ->
            val homeRepairSystemBars = homeRepairInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val homeRepairDisplayCutout = homeRepairInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val homeRepairIme = homeRepairInsets.getInsets(WindowInsetsCompat.Type.ime())


            val homeRepairTopPadding = maxOf(homeRepairSystemBars.top, homeRepairDisplayCutout.top)
            val homeRepairLeftPadding = maxOf(homeRepairSystemBars.left, homeRepairDisplayCutout.left)
            val homeRepairRightPadding = maxOf(homeRepairSystemBars.right, homeRepairDisplayCutout.right)
            window.setSoftInputMode(HomeRepairApplication.homeRepairInputMode)

            if (window.attributes.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) {
                Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "ADJUST PUN")
                val homeRepairBottomInset = maxOf(homeRepairSystemBars.bottom, homeRepairDisplayCutout.bottom)

                homeRepairView.setPadding(homeRepairLeftPadding, homeRepairTopPadding, homeRepairRightPadding, 0)

                homeRepairView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = homeRepairBottomInset
                }
            } else {
                Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "ADJUST RESIZE")

                val homeRepairBottomInset = maxOf(homeRepairSystemBars.bottom, homeRepairDisplayCutout.bottom, homeRepairIme.bottom)

                homeRepairView.setPadding(homeRepairLeftPadding, homeRepairTopPadding, homeRepairRightPadding, 0)

                homeRepairView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = homeRepairBottomInset
                }
            }



            WindowInsetsCompat.CONSUMED
        }
        Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "Activity onCreate()")
        homeRepairPushHandler.homeRepairHandlePush(intent.extras)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            homeRepairSetupSystemBars()
        }
    }

    override fun onResume() {
        super.onResume()
        homeRepairSetupSystemBars()
    }
}