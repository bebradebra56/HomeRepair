package com.homerapa.repagom.gefr.presentation.ui.load

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.homerapa.repagom.gefr.data.shar.HomeRepairSharedPreference
import com.homerapa.repagom.MainActivity
import com.homerapa.repagom.R
import com.homerapa.repagom.databinding.FragmentLoadHomeRepairBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomeRepairLoadFragment : Fragment(R.layout.fragment_load_home_repair) {
    private lateinit var homeRepairLoadBinding: FragmentLoadHomeRepairBinding

    private val homeRepairLoadViewModel by viewModel<HomeRepairLoadViewModel>()

    private val homeRepairSharedPreference by inject<HomeRepairSharedPreference>()

    private var homeRepairUrl = ""

    private val homeRepairRequestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        homeRepairSharedPreference.homeRepairNotificationState = 2
        homeRepairNavigateToSuccess(homeRepairUrl)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeRepairLoadBinding = FragmentLoadHomeRepairBinding.bind(view)

        homeRepairLoadBinding.homeRepairGrandButton.setOnClickListener {
            val homeRepairPermission = Manifest.permission.POST_NOTIFICATIONS
            homeRepairRequestNotificationPermission.launch(homeRepairPermission)
        }

        homeRepairLoadBinding.homeRepairSkipButton.setOnClickListener {
            homeRepairSharedPreference.homeRepairNotificationState = 1
            homeRepairSharedPreference.homeRepairNotificationRequest =
                (System.currentTimeMillis() / 1000) + 259200
            homeRepairNavigateToSuccess(homeRepairUrl)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeRepairLoadViewModel.homeRepairHomeScreenState.collect {
                    when (it) {
                        is HomeRepairLoadViewModel.HomeRepairHomeScreenState.HomeRepairLoading -> {

                        }

                        is HomeRepairLoadViewModel.HomeRepairHomeScreenState.HomeRepairError -> {
                            requireActivity().startActivity(
                                Intent(
                                    requireContext(),
                                    MainActivity::class.java
                                )
                            )
                            requireActivity().finish()
                        }

                        is HomeRepairLoadViewModel.HomeRepairHomeScreenState.HomeRepairSuccess -> {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                                val homeRepairNotificationState = homeRepairSharedPreference.homeRepairNotificationState
                                when (homeRepairNotificationState) {
                                    0 -> {
                                        homeRepairLoadBinding.homeRepairNotiGroup.visibility = View.VISIBLE
                                        homeRepairLoadBinding.homeRepairLoadingGroup.visibility = View.GONE
                                        homeRepairUrl = it.data
                                    }
                                    1 -> {
                                        if (System.currentTimeMillis() / 1000 > homeRepairSharedPreference.homeRepairNotificationRequest) {
                                            homeRepairLoadBinding.homeRepairNotiGroup.visibility = View.VISIBLE
                                            homeRepairLoadBinding.homeRepairLoadingGroup.visibility = View.GONE
                                            homeRepairUrl = it.data
                                        } else {
                                            homeRepairNavigateToSuccess(it.data)
                                        }
                                    }
                                    2 -> {
                                        homeRepairNavigateToSuccess(it.data)
                                    }
                                }
                            } else {
                                homeRepairNavigateToSuccess(it.data)
                            }
                        }

                        HomeRepairLoadViewModel.HomeRepairHomeScreenState.HomeRepairNotInternet -> {
                            homeRepairLoadBinding.homeRepairStateGroup.visibility = View.VISIBLE
                            homeRepairLoadBinding.homeRepairLoadingGroup.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }


    private fun homeRepairNavigateToSuccess(data: String) {
        findNavController().navigate(
            R.id.action_homeRepairLoadFragment_to_homeRepairV,
            bundleOf(HOME_REPAIR_D to data)
        )
    }

    companion object {
        const val HOME_REPAIR_D = "homeRepairData"
    }
}