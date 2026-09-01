package com.example.goride.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.goride.R
import com.example.goride.data.model.Ride
import com.example.goride.data.model.VehicleType
import com.example.goride.databinding.ItemRideBinding
import com.example.goride.util.Formatters

/**
 * Adapter dello storico (Lez. 2.5).
 *
 * ListAdapter invece di RecyclerView.Adapter: gestisce da solo la
 * lista e calcola le differenze in background con DiffUtil, animando
 * solo gli elementi cambiati invece di ridisegnare tutto.
 */
class RideAdapter(
    private val onClick: (Ride) -> Unit
) : ListAdapter<Ride, RideAdapter.RideViewHolder>(DiffCallback) {

    /**
     * Lez. 2.5 - il ViewHolder si costruisce con il pattern
     * companion object { fun from(parent) }, cosi' la logica di
     * inflate resta dentro il ViewHolder.
     */
    class RideViewHolder(
        private val binding: ItemRideBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ride: Ride, onClick: (Ride) -> Unit) {
            binding.ride = ride

            val contesto = binding.root.context

            // RF11 - durata, mezzo, costo
            binding.costo.text = Formatters.euro(ride.costoTotaleEuro)
            binding.data.text = Formatters.dataOra(ride.startTimestamp)
            binding.durata.text = Formatters.durata(ride.durataSecondi)

            val isEbike = ride.vehicleTipo == VehicleType.EBIKE
            binding.tipoMezzo.text = contesto.getString(
                if (isEbike) R.string.cd_ebike else R.string.cd_bici_classica
            )
            binding.tipoMezzo.setTextColor(
                contesto.getColor(
                    if (isEbike) R.color.arancio_ebike else R.color.verde_primario
                )
            )

            binding.root.setOnClickListener { onClick(ride) }

            // executePendingBindings: nelle liste il binding va forzato
            // subito, altrimenti l'aggiornamento slitta di un frame
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RideViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ItemRideBinding.inflate(inflater, parent, false)
                return RideViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder =
        RideViewHolder.from(parent)

    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    /**
     * Lez. 2.5 - DiffUtil.
     * areItemsTheSame: e' lo stesso oggetto? (confronto per id)
     * areContentsTheSame: e' cambiato qualcosa? (confronto per valore)
     */
    companion object DiffCallback : DiffUtil.ItemCallback<Ride>() {

        override fun areItemsTheSame(oldItem: Ride, newItem: Ride): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Ride, newItem: Ride): Boolean =
            oldItem == newItem   // data class: equals confronta tutti i campi
    }
}