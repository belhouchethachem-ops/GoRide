package com.example.goride.ui.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.goride.R
import com.example.goride.databinding.FragmentCommunityBinding

/** Sezione community (RF12, RF14). */
class CommunityFragment : Fragment() {

    private val viewModel: CommunityViewModel by viewModels()

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ReviewAdapter()
        binding.listaRecensioni.adapter = adapter
        binding.listaRecensioni.layoutManager = LinearLayoutManager(requireContext())

        // La lista arriva dal listener realtime: si aggiorna da sola
        // quando un altro utente pubblica
        viewModel.recensioni.observe(viewLifecycleOwner) { recensioni ->
            adapter.submitList(recensioni)
            binding.statoVuoto.visibility =
                if (recensioni.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabNuova.setOnClickListener {
            findNavController().navigate(R.id.action_community_to_newReview)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.listaRecensioni.adapter = null
        _binding = null
    }
}