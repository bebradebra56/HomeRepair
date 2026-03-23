package com.homerapa.repagom.gefr.presentation.ui.view

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.homerapa.repagom.gefr.presentation.app.HomeRepairApplication
import com.homerapa.repagom.gefr.presentation.ui.load.HomeRepairLoadFragment
import org.koin.android.ext.android.inject

class HomeRepairV : Fragment(){

    private lateinit var homeRepairPhoto: Uri
    private var homeRepairFilePathFromChrome: ValueCallback<Array<Uri>>? = null

    private val homeRepairTakeFile: ActivityResultLauncher<PickVisualMediaRequest> = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        homeRepairFilePathFromChrome?.onReceiveValue(arrayOf(it ?: Uri.EMPTY))
        homeRepairFilePathFromChrome = null
    }

    private val homeRepairTakePhoto: ActivityResultLauncher<Uri> = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            homeRepairFilePathFromChrome?.onReceiveValue(arrayOf(homeRepairPhoto))
            homeRepairFilePathFromChrome = null
        } else {
            homeRepairFilePathFromChrome?.onReceiveValue(null)
            homeRepairFilePathFromChrome = null
        }
    }

    private val homeRepairDataStore by activityViewModels<HomeRepairDataStore>()


    private val homeRepairViFun by inject<HomeRepairViFun>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "Fragment onCreate")
        CookieManager.getInstance().setAcceptCookie(true)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (homeRepairDataStore.homeRepairView.canGoBack()) {
                        homeRepairDataStore.homeRepairView.goBack()
                        Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "WebView can go back")
                    } else if (homeRepairDataStore.homeRepairViList.size > 1) {
                        Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "WebView can`t go back")
                        homeRepairDataStore.homeRepairViList.removeAt(homeRepairDataStore.homeRepairViList.lastIndex)
                        Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "WebView list size ${homeRepairDataStore.homeRepairViList.size}")
                        homeRepairDataStore.homeRepairView.destroy()
                        val previousWebView = homeRepairDataStore.homeRepairViList.last()
                        homeRepairAttachWebViewToContainer(previousWebView)
                        homeRepairDataStore.homeRepairView = previousWebView
                    }
                }

            })
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (homeRepairDataStore.homeRepairIsFirstCreate) {
            homeRepairDataStore.homeRepairIsFirstCreate = false
            homeRepairDataStore.homeRepairContainerView = FrameLayout(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                id = View.generateViewId()
            }
            return homeRepairDataStore.homeRepairContainerView
        } else {
            return homeRepairDataStore.homeRepairContainerView
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "onViewCreated")
        if (homeRepairDataStore.homeRepairViList.isEmpty()) {
            homeRepairDataStore.homeRepairView = HomeRepairVi(requireContext(), object :
                HomeRepairCallBack {
                override fun homeRepairHandleCreateWebWindowRequest(homeRepairVi: HomeRepairVi) {
                    homeRepairDataStore.homeRepairViList.add(homeRepairVi)
                    Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "WebView list size = ${homeRepairDataStore.homeRepairViList.size}")
                    Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "CreateWebWindowRequest")
                    homeRepairDataStore.homeRepairView = homeRepairVi
                    homeRepairVi.homeRepairSetFileChooserHandler { callback ->
                        homeRepairHandleFileChooser(callback)
                    }
                    homeRepairAttachWebViewToContainer(homeRepairVi)
                }

            }, homeRepairWindow = requireActivity().window).apply {
                homeRepairSetFileChooserHandler { callback ->
                    homeRepairHandleFileChooser(callback)
                }
            }
            homeRepairDataStore.homeRepairView.homeRepairFLoad(arguments?.getString(
                HomeRepairLoadFragment.HOME_REPAIR_D) ?: "")
//            ejvview.fLoad("www.google.com")
            homeRepairDataStore.homeRepairViList.add(homeRepairDataStore.homeRepairView)
            homeRepairAttachWebViewToContainer(homeRepairDataStore.homeRepairView)
        } else {
            homeRepairDataStore.homeRepairViList.forEach { webView ->
                webView.homeRepairSetFileChooserHandler { callback ->
                    homeRepairHandleFileChooser(callback)
                }
            }
            homeRepairDataStore.homeRepairView = homeRepairDataStore.homeRepairViList.last()

            homeRepairAttachWebViewToContainer(homeRepairDataStore.homeRepairView)
        }
        Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "WebView list size = ${homeRepairDataStore.homeRepairViList.size}")
    }

    private fun homeRepairHandleFileChooser(callback: ValueCallback<Array<Uri>>?) {
        Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "handleFileChooser called, callback: ${callback != null}")

        homeRepairFilePathFromChrome = callback

        val listItems: Array<out String> = arrayOf("Select from file", "To make a photo")
        val listener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                0 -> {
                    Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "Launching file picker")
                    homeRepairTakeFile.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                1 -> {
                    Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "Launching camera")
                    homeRepairPhoto = homeRepairViFun.homeRepairSavePhoto()
                    homeRepairTakePhoto.launch(homeRepairPhoto)
                }
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Choose a method")
            .setItems(listItems, listener)
            .setCancelable(true)
            .setOnCancelListener {
                Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "File chooser canceled")
                callback?.onReceiveValue(null)
                homeRepairFilePathFromChrome = null
            }
            .create()
            .show()
    }

    private fun homeRepairAttachWebViewToContainer(w: HomeRepairVi) {
        homeRepairDataStore.homeRepairContainerView.post {
            (w.parent as? ViewGroup)?.removeView(w)
            homeRepairDataStore.homeRepairContainerView.removeAllViews()
            homeRepairDataStore.homeRepairContainerView.addView(w)
        }
    }


}