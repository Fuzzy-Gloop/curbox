package neth.iecal.curbox.ui.fragments.installation.onboarding.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import neth.iecal.curbox.R
import neth.iecal.curbox.data.sync.SyncGateway
import neth.iecal.curbox.ui.fragments.installation.onboarding.OnboardingFragment
import neth.iecal.curbox.ui.fragments.main.reducers.sync.AccountController

/**
 * Optional onboarding step (Play Store only) where someone can turn on sync.
 * They can also skip and set it up later from Reducers.
 */
class AccountOnboardingFragment : Fragment() {

    private lateinit var controller: AccountController

    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let { controller.pairWith(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_account_onboarding, container, false)

        controller = AccountController(v, this) {
            scanLauncher.launch(ScanOptions().setOrientationLocked(true).setPrompt("Point at the pairing code"))
        }
        controller.bind()

        val continueBtn = v.findViewById<MaterialButton>(R.id.btn_continue)
        continueBtn.setOnClickListener {
            (parentFragment as? OnboardingFragment)?.goToNextPage()
        }

        // Once sync is fully on, nudge the button from "Skip" to "Continue".
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                SyncGateway.provider.status.collect { s ->
                    continueBtn.text = if (s.unlocked) "Continue" else "Skip for now"
                }
            }
        }
        return v
    }
}
