package com.johnny.leakcheck

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.johnny.leakcheck.data.ApiResult
import com.johnny.leakcheck.data.LeakCheckApi
import com.johnny.leakcheck.data.LocalDataSource
import com.johnny.leakcheck.data.LocalDb
import com.johnny.leakcheck.data.Prefs
import com.johnny.leakcheck.databinding.ActivitySettingsBinding
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val api = LeakCheckApi()

    private val pickDb = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) {
            Snackbar.make(binding.root, getString(R.string.settings_import_none), Snackbar.LENGTH_SHORT)
                .show()
            return@registerForActivityResult
        }
        val err = LocalDb.importFrom(this, uri)
        if (err == null) {
            Snackbar.make(binding.root, getString(R.string.settings_import_ok), Snackbar.LENGTH_SHORT)
                .show()
        } else {
            Snackbar.make(binding.root, err, Snackbar.LENGTH_LONG).show()
        }
        refreshLocalCount()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.inputUrl.setText(Prefs.getBaseUrl(this))

        binding.btnSave.setOnClickListener {
            Prefs.setBaseUrl(this, binding.inputUrl.text?.toString().orEmpty())
            val saved = Prefs.getBaseUrl(this)
            binding.inputUrl.setText(saved)
            refreshServers()
            Snackbar.make(binding.root, getString(R.string.settings_saved, saved), Snackbar.LENGTH_SHORT)
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

        binding.btnResetDefault.setOnClickListener {
            Prefs.resetToDefault(this)
            binding.inputUrl.setText(Prefs.getBaseUrl(this))
            refreshServers()
            Snackbar.make(binding.root, getString(R.string.settings_reset_done), Snackbar.LENGTH_SHORT)
                .show()
        }

        binding.btnImportDb.setOnClickListener {
            pickDb.launch(arrayOf("*/*"))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshServers()
        refreshLocalCount()
    }

    /** 重建服务器地址 Chip 列表：点击选用，× 删除自定义地址 */
    private fun refreshServers() {
        val current = Prefs.getBaseUrl(this)
        binding.chipGroup.removeAllViews()
        for (url in Prefs.getAllServers(this)) {
            val chip = Chip(this)
            chip.id = View.generateViewId()
            chip.text = url
            chip.isCheckable = true
            chip.isChecked = url == current
            chip.isCloseIconVisible = url != Prefs.DEFAULT_BASE_URL
            chip.setOnClickListener {
                Prefs.setBaseUrl(this, url)
                binding.inputUrl.setText(url)
                refreshServers()
            }
            if (url != Prefs.DEFAULT_BASE_URL) {
                chip.setOnCloseIconClickListener {
                    Prefs.removeServer(this, url)
                    binding.inputUrl.setText(Prefs.getBaseUrl(this))
                    refreshServers()
                }
            }
            binding.chipGroup.addView(chip)
        }
    }

    private fun refreshLocalCount() {
        lifecycleScope.launch {
            when (val r = LocalDataSource.count(this@SettingsActivity)) {
                is ApiResult.Success -> binding.txtLocalCount.text =
                    getString(R.string.settings_local_count, r.data)
                is ApiResult.Failure -> binding.txtLocalCount.text =
                    getString(R.string.settings_local_count_error, r.message)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
