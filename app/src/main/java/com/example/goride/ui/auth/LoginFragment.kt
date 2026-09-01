package com.example.goride.ui.auth

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.goride.R
import com.example.goride.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar

/**
 * Schermata di login (RF7).
 *
 * Il Fragment fa solo tre cose: collega il binding, osserva il
 * ViewModel e naviga. Nessuna logica, nessun listener sui campi:
 * ci pensa il two-way binding (Lez. 2.8).
 */
class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModels()

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        osservaViewModel()

        // Richiede il focus sul campo email e forza l'apertura della tastiera
        binding.inputEmail.requestFocus()
        binding.inputEmail.postDelayed({
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.inputEmail, InputMethodManager.SHOW_IMPLICIT)
        }, 200)

        binding.linkRegistrati.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        binding.linkPasswordDimenticata.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgotPassword)
        }
    }

    private fun osservaViewModel() {
        viewModel.errore.observe(viewLifecycleOwner) { messaggio ->
            messaggio?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.erroreMostrato()
            }
        }

        viewModel.loginRiuscito.observe(viewLifecycleOwner) { riuscito ->
            if (riuscito) {
                // Lez. 2.4 - popUpTo con inclusive: svuota il back stack
                // dell'autenticazione, cosi' il tasto Back non riporta al login
                findNavController().navigate(R.id.action_login_to_map)
                viewModel.navigazioneCompletata()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}