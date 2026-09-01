package com.example.goride.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.goride.databinding.FragmentForgotPasswordBinding
import com.google.android.material.snackbar.Snackbar

class ForgotPasswordFragment : Fragment() {

    private val viewModel: ForgotPasswordViewModel by viewModels()

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.messaggio.observe(viewLifecycleOwner) { testo ->
            testo?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.messaggioMostrato()
            }
        }

        viewModel.emailInviata.observe(viewLifecycleOwner) { inviata ->
            if (inviata) {
                binding.root.postDelayed({
                    if (isAdded) findNavController().navigateUp()
                }, 2500)
            }
        }

        binding.bottoneIndietro.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}