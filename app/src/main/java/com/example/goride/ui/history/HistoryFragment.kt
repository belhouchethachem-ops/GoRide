package com.example.goride.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.goride.databinding.FragmentHistoryBinding
import com.google.android.material.snackbar.Snackbar

/** Storico delle corse (RF10). */
class HistoryFragment : Fragment() {

    private val viewModel: HistoryViewModel by viewModels()

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RideAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RF11 - tocco su una corsa apre il dettaglio
        adapter = RideAdapter { corsa ->
            val azione = HistoryFragmentDirections.actionHistoryToRideDetail(corsa)
            findNavController().navigate(azione)
        }

        binding.listaCorse.adapter = adapter
        binding.listaCorse.layoutManager = LinearLayoutManager(requireContext())

        osservaViewModel()
    }

    private fun osservaViewModel() {
        viewModel.corse.observe(viewLifecycleOwner) { corse ->
            // submitList: DiffUtil calcola le differenze da solo,
            // niente notifyDataSetChanged (Lez. 2.5)
            adapter.submitList(corse)

            binding.statoVuoto.visibility =
                if (corse.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.errore.observe(viewLifecycleOwner) { messaggio ->
            messaggio?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                    .setAction("Riprova") { viewModel.sincronizza() }
                    .show()
                viewModel.erroreMostrato()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Sganciare l'adapter evita che la RecyclerView trattenga la View
        binding.listaCorse.adapter = null
        _binding = null
    }
}