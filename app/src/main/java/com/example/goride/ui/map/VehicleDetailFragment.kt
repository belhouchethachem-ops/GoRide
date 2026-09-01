package com.example.goride.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.goride.R
import com.example.goride.databinding.FragmentVehicleDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Dettaglio del mezzo in bottom sheet (RF2.1, RF3, RF4).
 *
 * I dati arrivano via Safe Args: il Vehicle e' Parcelable, quindi
 * viaggia intero senza dover ricaricare nulla (Lez. 2.4).
 */
class VehicleDetailFragment : BottomSheetDialogFragment() {

    private val args: VehicleDetailFragmentArgs by navArgs()

    private var _binding: FragmentVehicleDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVehicleDetailBinding.inflate(inflater, container, false)
        binding.vehicle = args.vehicle
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mezzo = args.vehicle

        if (mezzo.isEbike) {
            binding.valoreBatteria.text = getString(
                R.string.formato_percentuale, mezzo.batteriaPercentuale ?: 0
            )
            binding.valoreAutonomia.text = getString(
                R.string.formato_km, mezzo.autonomiaKm ?: 0.0
            )

            // Accessibilita': il colore da solo non basta, ma aiuta
            val coloreBatteria = when {
                (mezzo.batteriaPercentuale ?: 0) >= 60 -> R.color.batteria_alta
                (mezzo.batteriaPercentuale ?: 0) >= 25 -> R.color.batteria_media
                else -> R.color.batteria_bassa
            }
            binding.valoreBatteria.setTextColor(
                resources.getColor(coloreBatteria, null)
            )
        }

        binding.valoreSblocco.text =
            getString(R.string.formato_euro, mezzo.tariffaSbloccoEuro)
        binding.valoreMinuto.text =
            getString(R.string.formato_euro_minuto, mezzo.tariffaAlMinutoEuro)

        binding.bottoneSblocca.setOnClickListener {
            val azione = VehicleDetailFragmentDirections
                .actionVehicleDetailToRide(mezzo)
            findNavController().navigate(azione)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}