package com.example.goride.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.goride.R
import com.example.goride.databinding.FragmentRegisterBinding
import com.google.android.material.snackbar.Snackbar

/** Registrazione nuovo utente (RF8). */
class RegisterFragment : Fragment() {

    private val viewModel: RegisterViewModel by viewModels()

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.errore.observe(viewLifecycleOwner) { messaggio ->
            messaggio?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.erroreMostrato()
            }
        }

        viewModel.registrazioneRiuscita.observe(viewLifecycleOwner) { riuscita ->
            if (riuscita) {
                findNavController().navigate(R.id.action_register_to_map)
                viewModel.navigazioneCompletata()
            }
        }

        binding.linkAccedi.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}