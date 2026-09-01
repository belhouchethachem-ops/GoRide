package com.example.goride.ui.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.goride.R
import com.example.goride.data.model.ReviewType
import com.example.goride.databinding.FragmentNewReviewBinding
import com.google.android.material.snackbar.Snackbar

/** Scrittura di una recensione (RF13). */
class NewReviewFragment : Fragment() {

    private val viewModel: NewReviewViewModel by viewModels()

    private var _binding: FragmentNewReviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewReviewBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gruppoTipi.setOnCheckedStateChangeListener { _, idSelezionati ->
            val tipo = when (idSelezionati.firstOrNull()) {
                R.id.chip_guasto -> ReviewType.GUASTO
                R.id.chip_consiglio -> ReviewType.CONSIGLIO
                else -> ReviewType.OPINIONE
            }
            viewModel.selezionaTipo(tipo)
        }

        viewModel.errore.observe(viewLifecycleOwner) { messaggio ->
            messaggio?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.erroreMostrato()
            }
        }

        viewModel.pubblicata.observe(viewLifecycleOwner) { fatto ->
            if (fatto) {
                findNavController().navigateUp()
                viewModel.navigazioneCompletata()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}