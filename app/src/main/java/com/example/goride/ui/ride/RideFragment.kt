package com.example.goride.ui.ride

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.goride.R
import com.example.goride.databinding.FragmentRideBinding
import kotlinx.coroutines.launch

/**
 * Schermata della corsa attiva (RF5, RF6, RF6.1, RF6.2).
 *
 * activityViewModels() e non viewModels(): il RideViewModel deve
 * sopravvivere alla navigazione, non solo alla rotazione.
 */
class RideFragment : Fragment() {

    private val viewModel: RideViewModel by activityViewModels()
    private val args: RideFragmentArgs by navArgs()

    private var _binding: FragmentRideBinding? = null
    private val binding get() = _binding!!

    private var dialogScadenza: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRideBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nomeMezzo.text = args.vehicle.nome

        osservaViewModel()

        // Il controllo su corsaAttiva evita che una rotazione riapra
        // il dialog o faccia ripartire il timer.
        if (viewModel.corsaAttiva.value != true) {
            viewLifecycleOwner.lifecycleScope.launch {
                // Prima verifichiamo se c'e' una corsa interrotta da
                // riprendere (process death), poi eventualmente
                // chiediamo la durata.
                val ripristinata = viewModel.ripristinaCorsaAttiva()
                if (!ripristinata && isAdded) {
                    mostraSceltaDurata()
                }
            }
        }

        binding.bottoneEstendi.setOnClickListener { mostraSceltaEstensione() }
        binding.bottoneTermina.setOnClickListener { confermaTermine() }

        // Il tasto Back non deve far uscire dalla corsa per sbaglio
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    confermaTermine()
                }
            }
        )
    }

    private fun osservaViewModel() {
        viewModel.costoAttuale.observe(viewLifecycleOwner) { costo ->
            binding.costo.text = getString(R.string.formato_euro, costo)
        }

        // RF6 - alert alla scadenza
        viewModel.tempoScaduto.observe(viewLifecycleOwner) { scaduto ->
            if (scaduto) mostraAlertScadenza()
        }

        viewModel.corsaTerminata.observe(viewLifecycleOwner) { terminata ->
            if (terminata) {
                findNavController().navigate(R.id.action_ride_to_map)
                viewModel.navigazioneCompletata()
            }
        }
    }

    /** Scelta del tempo prepagato prima di partire */
    private fun mostraSceltaDurata() {
        val opzioni = RideViewModel.DURATE_DISPONIBILI
        val etichette = opzioni.map { minuti ->
            val costo = args.vehicle.tariffaSbloccoEuro +
                    minuti * args.vehicle.tariffaAlMinutoEuro
            getString(R.string.corsa_opzione_durata, minuti, costo)
        }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.corsa_scegli_durata)
            .setItems(etichette) { _, indice ->
                viewModel.avviaCorsa(args.vehicle, opzioni[indice])
            }
            .setCancelable(false)
            .show()
    }

    /**
     * RF6 - Lez. 2.4, slide 78-80.
     * setCancelable(false): l'alert non si puo' ignorare, l'utente
     * deve scegliere se estendere o terminare.
     */
    private fun mostraAlertScadenza() {
        if (dialogScadenza?.isShowing == true) return

        dialogScadenza = AlertDialog.Builder(requireContext())
            .setTitle(R.string.corsa_scaduta_titolo)
            .setMessage(R.string.corsa_scaduta_messaggio)
            .setPositiveButton(R.string.corsa_estendi) { _, _ ->
                viewModel.alertMostrato()
                mostraSceltaEstensione()
            }
            .setNegativeButton(R.string.corsa_termina) { _, _ ->
                viewModel.terminaCorsa()
            }
            .setCancelable(false)
            .show()
    }

    /** RF6.1 - acquisto di tempo aggiuntivo */
    private fun mostraSceltaEstensione() {
        val opzioni = RideViewModel.ESTENSIONI_DISPONIBILI
        val vehicle = viewModel.mezzoCorrente ?: args.vehicle

        val etichette = opzioni.map { minuti ->
            val costo = minuti * vehicle.tariffaAlMinutoEuro
            getString(R.string.corsa_opzione_estensione, minuti, costo)
        }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.corsa_estendi_titolo)
            .setItems(etichette) { _, indice ->
                viewModel.estendiCorsa(opzioni[indice])
            }
            .setNegativeButton(R.string.annulla, null)
            .show()
    }

    /** RF6.2 - conferma prima di chiudere */
    private fun confermaTermine() {
        val costo = viewModel.costoAttuale.value ?: 0.0

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.corsa_termina_titolo)
            .setMessage(getString(R.string.corsa_termina_messaggio, costo))
            .setPositiveButton(R.string.corsa_termina) { _, _ ->
                viewModel.terminaCorsa()
            }
            .setNegativeButton(R.string.annulla, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Un dialog non chiuso lascia una window orfana: leak garantito
        dialogScadenza?.dismiss()
        dialogScadenza = null
        _binding = null
    }
}