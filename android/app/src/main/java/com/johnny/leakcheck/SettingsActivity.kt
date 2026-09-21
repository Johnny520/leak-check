package com.johnny.leakcheck

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.johnny.leakcheck.data.ApiResult
import com.johnny.leakcheck.data.LeakCheckApi
import com.johnny.leakcheck.data.Prefs
import com.johnny.leakcheck.databinding.ActivitySettingsBinding
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val api = LeakCheckApi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.inputUrl.setText(Prefs.getBaseUrl(this))

        binding.btnSave.setOnClickListener {
            Prefs.setBaseUrl(this, binding.inputUrl.text?.toString().orEmpty())
            Snackbar.make(binding.root, getString(R.string.settings_saved), Snackbar.LENGTH_SHORT)
                .show()
        }

        binding.btnTest.setOnClickListener {
            val url = binding.inputUrl.text?.toString().orEmpty()
            binding.btnTest.isEnabled = false
            binding.txtResult.text = getString(R.string.hint_querying)
            lifecycleScope.launch {
                val result = api.count(url)
                binding.btnTest.isEnabled = true
                binding.txtResult.text = when (result) {
                    is ApiResult.Success -> getString(R.string.settings_test_ok, result.data)
                    is ApiResult.Failure -> getString(R.string.settings_test_fail, result.message)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
