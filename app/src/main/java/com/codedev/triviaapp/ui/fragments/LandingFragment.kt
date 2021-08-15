package com.codedev.triviaapp.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.codedev.triviaapp.R
import com.codedev.triviaapp.databinding.FragmentLandingBinding
import com.codedev.triviaapp.ui.adapters.OnBoardingAdapter
import com.codedev.triviaapp.ui.adapters.viewPagerItems

class LandingFragment : Fragment(R.layout.fragment_landing) {

    private var _binding : FragmentLandingBinding? = null
    private val binding get() = _binding!!
    private lateinit var indicators: Array<ImageView>
    private lateinit var adapter: OnBoardingAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentLandingBinding.bind(view)

        adapter = OnBoardingAdapter(viewPagerItems)

        binding.viewPager.adapter = adapter
        indicators = arrayOf(
            binding.firstIndicator, binding.secondIndicator, binding.thirdIndicator
        )

        binding.buttonJoinTeam.setOnClickListener {

        }

        for(i in indicators) {
            i.setOnClickListener {
                binding.viewPager.setCurrentItem(indicators.indexOf(i), true)
            }
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                when(position) {
                    0 -> {
                        indicatorListener(0)
                    }
                    1 -> {
                        indicatorListener(1)
                    }
                    2 -> {
                        indicatorListener(2)
                    }
                }
            }
        })
    }

    private fun indicatorListener(index: Int) {
        for (i in indicators.indices) {
            if(i == index) {
                indicators[i].setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.background_on_boarding_indicator_active)
                )
            } else {
                indicators[i].setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.background_on_boarding_indicator_inactive)
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}