package com.example.goride.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.goride.R
import com.example.goride.databinding.FragmentProfileBinding
import com.example.goride.util.Formatters
import com.google.android.material.snackbar.Snackbar

/** Profilo utente (RF8.1). */
class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.utente.observe(viewLifecycleOwner) { utente ->
            utente?.let {
                binding.email.text = it.email
                binding.dataRegistrazione.text = getString(
                    R.string.profilo_iscritto_dal,
                    Formatters.dataBreve(it.dataRegistrazione)
                )
            }
        }

        viewModel.messaggio.observe(viewLifecycleOwner) { testo ->
            testo?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                viewModel.messaggioMostrato()
            }
        }

        viewModel.logoutEseguito.observe(viewLifecycleOwner) { fatto ->
            if (fatto) {
                findNavController().navigate(R.id.action_profile_to_login)
                viewModel.navigazioneCompletata()
            }
        }

        binding.bottoneLogout.setOnClickListener { confermaLogout() }
    }

    private fun confermaLogout() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.profilo_logout_titolo)
            .setMessage(R.string.profilo_logout_messaggio)
            .setPositiveButton(R.string.profilo_logout) { _, _ -> viewModel.logout() }
            .setNegativeButton(R.string.annulla, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}