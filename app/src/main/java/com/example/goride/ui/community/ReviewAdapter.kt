package com.example.goride.ui.community

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.goride.BR
import com.example.goride.R
import com.example.goride.data.model.Review
import com.example.goride.data.model.ReviewType
import com.example.goride.databinding.ItemReviewConsiglioBinding
import com.example.goride.databinding.ItemReviewGuastoBinding
import com.example.goride.databinding.ItemReviewOpinioneBinding
import com.example.goride.util.Formatters

/**
 * Adapter delle recensioni (RF14).
 *
 * Lez. 2.5 - getItemViewType: tre layout diversi nella stessa lista,
 * scelti in base al tipo di recensione.
 */
class ReviewAdapter : ListAdapter<Review, ReviewAdapter.ReviewViewHolder>(DiffCallback) {

    /**
     * Il ViewHolder e' generico sui tre binding: usiamo ViewDataBinding
     * come tipo comune e setVariable per assegnare la variabile.
     */
    class ReviewViewHolder(
        private val binding: ViewDataBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            binding.setVariable(BR.review, review)

            // L'id autore_data esiste in tutti e tre i layout
            binding.root.findViewById<android.widget.TextView>(R.id.autore_data)?.text =
                "${review.autoreNome} · ${Formatters.dataBreve(review.timestamp)}"

            binding.executePendingBindings()
        }
    }

    /** Lez. 2.5 - il tipo determina il layout */
    override fun getItemViewType(position: Int): Int =
        when (getItem(position).tipo) {
            ReviewType.GUASTO -> TIPO_GUASTO
            ReviewType.OPINIONE -> TIPO_OPINIONE
            ReviewType.CONSIGLIO -> TIPO_CONSIGLIO
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val binding: ViewDataBinding = when (viewType) {
            TIPO_GUASTO -> ItemReviewGuastoBinding.inflate(inflater, parent, false)
            TIPO_CONSIGLIO -> ItemReviewConsiglioBinding.inflate(inflater, parent, false)
            else -> ItemReviewOpinioneBinding.inflate(inflater, parent, false)
        }

        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Review>() {

        private const val TIPO_GUASTO = 0
        private const val TIPO_OPINIONE = 1
        private const val TIPO_CONSIGLIO = 2

        override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean =
            oldItem == newItem
    }
}