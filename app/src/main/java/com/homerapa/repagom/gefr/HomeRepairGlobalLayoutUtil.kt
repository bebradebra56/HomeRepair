package com.homerapa.repagom.gefr

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout
import com.homerapa.repagom.gefr.presentation.app.HomeRepairApplication

class HomeRepairGlobalLayoutUtil {

    private var homeRepairMChildOfContent: View? = null
    private var homeRepairUsableHeightPrevious = 0

    fun homeRepairAssistActivity(activity: Activity) {
        val content = activity.findViewById<FrameLayout>(android.R.id.content)
        homeRepairMChildOfContent = content.getChildAt(0)

        homeRepairMChildOfContent?.viewTreeObserver?.addOnGlobalLayoutListener {
            possiblyResizeChildOfContent(activity)
        }
    }

    private fun possiblyResizeChildOfContent(activity: Activity) {
        val homeRepairUsableHeightNow = homeRepairComputeUsableHeight()
        if (homeRepairUsableHeightNow != homeRepairUsableHeightPrevious) {
            val homeRepairUsableHeightSansKeyboard = homeRepairMChildOfContent?.rootView?.height ?: 0
            val homeRepairHeightDifference = homeRepairUsableHeightSansKeyboard - homeRepairUsableHeightNow

            if (homeRepairHeightDifference > (homeRepairUsableHeightSansKeyboard / 4)) {
                activity.window.setSoftInputMode(HomeRepairApplication.homeRepairInputMode)
            } else {
                activity.window.setSoftInputMode(HomeRepairApplication.homeRepairInputMode)
            }
//            mChildOfContent?.requestLayout()
            homeRepairUsableHeightPrevious = homeRepairUsableHeightNow
        }
    }

    private fun homeRepairComputeUsableHeight(): Int {
        val r = Rect()
        homeRepairMChildOfContent?.getWindowVisibleDisplayFrame(r)
        return r.bottom - r.top  // Visible height без status bar
    }
}