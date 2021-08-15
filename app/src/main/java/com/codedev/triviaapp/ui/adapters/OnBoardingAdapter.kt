package com.codedev.triviaapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codedev.triviaapp.R
import com.codedev.triviaapp.databinding.LayoutOnBoardingItemBinding


class OnBoardingAdapter (private val list: List<ViewPagerItem>) : RecyclerView.Adapter<OnBoardingAdapter.OnBoardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnBoardingViewHolder {
        return OnBoardingViewHolder(
            LayoutOnBoardingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: OnBoardingViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class OnBoardingViewHolder(
        private val binding: LayoutOnBoardingItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(viewPagerItem: ViewPagerItem) {
            binding.imageViewPager.setImageResource(viewPagerItem.imageResource)
            binding.textTitle.text = viewPagerItem.title
        }
    }
}

data class ViewPagerItem(
    val title: String,
    val imageResource: Int
)

val viewPagerItems = listOf<ViewPagerItem>(
    ViewPagerItem(
        "A tech-based team for vast specializations", R.drawable.ic_meeting
    ),
    ViewPagerItem(
        "Getting mentors in this interactive community", R.drawable.ic_code_inspection
    ),
    ViewPagerItem(
        "Sharing and solving code related problems", R.drawable.ic_workspace
    )
)
