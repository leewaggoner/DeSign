package com.wreckingball.design.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.wreckingball.design.R
import kotlinx.android.synthetic.main.fragment_confirm_password.*
import org.koin.android.ext.android.inject

class ConfirmPasswordFragment : Fragment(R.layout.fragment_confirm_password) {
    private val model: LoginViewModel by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        confirm_password.setOnClickListener {
            if (model.handleConfirmPasswordClick(confirmation_code.text.toString(), password.text.toString())) {
                progress_confirm_password.visibility = View.VISIBLE
            }
        }

        //TODO: Handle errors
        model.passwordConfirmed.observe(viewLifecycleOwner, Observer { passwordConfirmed->
            progress_confirm_password.visibility = View.GONE
            if (passwordConfirmed) {
                val action = ConfirmPasswordFragmentDirections.actionConfirmPasswordFragmentToSignInFragment()
                requireView().findNavController().navigate(action)
            }
        })
    }
}