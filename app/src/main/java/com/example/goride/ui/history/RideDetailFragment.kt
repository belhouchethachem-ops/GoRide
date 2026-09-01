package com.example.goride.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.goride.R
import com.example.goride.data.model.VehicleType
import com.example.goride.databinding.FragmentRideDetailBinding
import com.example.goride.util.Formatters

/** Dettaglio di una corsa (RF11). */
class RideDetailFragment : Fragment() {

    private val args: RideDetailFragmentArgs by navArgs()

    private var _binding: FragmentRideDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRideDetailBinding.inflate(inflater, container, false)
        binding.ride = args.ride
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val corsa = args.ride

        binding.costoTotale.text = Formatters.euro(corsa.costoTotaleEuro)
        binding.durata.text = Formatters.durata(corsa.durataSecondi)
        binding.dataOra.text = Formatters.dataOra(corsa.startTimestamp)

        val isEbike = corsa.vehicleTipo == VehicleType.EBIKE
        binding.tipoMezzo.text = getString(
            if (isEbike) R.string.cd_ebike else R.string.cd_bici_classica
        )
        binding.tipoMezzo.setTextColor(
            requireContext().getColor(
                if (isEbike) R.color.arancio_ebike else R.color.verde_primario
            )
        )

        binding.rigaSblocco.text = Formatters.euro(corsa.tariffaSbloccoEuro)
        binding.rigaMinuto.text = getString(
            R.string.formato_euro_minuto, corsa.tariffaAlMinutoEuro
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}