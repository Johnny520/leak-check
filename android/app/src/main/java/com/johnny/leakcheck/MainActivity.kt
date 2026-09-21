package com.johnny.leakcheck

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.johnny.leakcheck.data.AggregatedResult
import com.johnny.leakcheck.data.ApiResult
import com.johnny.leakcheck.data.LeakCheckApi
import com.johnny.leakcheck.data.LocalDataSource
import com.johnny.leakcheck.data.Prefs
import com.johnny.leakcheck.databinding.ActivityMainBinding
import com.johnny.leakcheck.ui.ResultAdapter
import com.johnny.leakcheck.ui.ResultRow
import com.johnny.leakcheck.util.TypeDetector
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val api = LeakCheckApi()
    private val adapter = ResultAdapter()
    private var loading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.resultList.layoutManager = LinearLayoutManager(this)
        binding.resultList.adapter = adapter

        initModeSwitcher()

        binding.btnQuery.setOnClickListener { performQuery() }
        binding.btnClear.setOnClickListener {
            binding.inputQuery.setText("")
            showPlaceholder(getString(R.string.hint_input))
        }
        binding.inputQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performQuery()
                true
            } else {
                false
            }
        }
        // 点击状态栏可快速进入设置（切换服务器 / 导入数据库）
        binding.txtStatus.setOnClickListener { openSettings() }
    }

    private fun initModeSwitcher() {
        binding.btnModeOnline.isChecked = !isLocalMode()
        binding.btnModeLocal.isChecked = isLocalMode()
        binding.modeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                Prefs.setMode(
                    this,
                    if (checkedId == R.id.btnModeLocal) Prefs.MODE_LOCAL else Prefs.MODE_ONLINE
                )
                adapter.submit(emptyList())
                showPlaceholder(getString(R.string.hint_input))
                refreshCount()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_switch_server -> {
                showServerPicker()
                true
            }
            R.id.action_settings -> {
                openSettings()
                true
            }
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            R.id.action_refresh -> {
                refreshCount()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshCount()
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun isLocalMode(): Boolean = Prefs.getMode(this) == Prefs.MODE_LOCAL

    /** 快速切换服务器地址（默认 + 自定义） */
    private fun showServerPicker() {
        val servers = Prefs.getAllServers(this)
        if (servers.isEmpty()) return
        val current = Prefs.getBaseUrl(this)
        val checked = servers.indexOf(current).coerceAtLeast(0)
        AlertDialog.Builder(this)
            .setTitle(R.string.action_switch_server)
            .setSingleChoiceItems(servers.toTypedArray(), checked) { dialog, which ->
                Prefs.setBaseUrl(this, servers[which])
                dialog.dismiss()
                adapter.submit(emptyList())
                showPlaceholder(getString(R.string.hint_input))
                refreshCount()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setNeutralButton(R.string.action_settings) { _, _ -> openSettings() }
            .show()
    }

    private fun refreshCount() {
        if (isLocalMode()) {
            lifecycleScope.launch {
                binding.txtStatus.text = getString(R.string.status_local_loading)
                when (val r = LocalDataSource.count(this@MainActivity)) {
                    is ApiResult.Success -> binding.txtStatus.text =
                        getString(R.string.status_local_count, r.data)
                    is ApiResult.Failure -> binding.txtStatus.text =
                        getString(R.string.status_local_error, r.message)
                }
            }
            return
        }

        val base = Prefs.getBaseUrl(this)
        if (base.isBlank()) {
            binding.txtStatus.text = getString(R.string.status_no_server)
            return
        }
        lifecycleScope.launch {
            binding.txtStatus.text = getString(R.string.status_server, base)
            when (val r = api.count(base)) {
                is ApiResult.Success -> binding.txtStatus.text =
                    getString(R.string.status_count, base, r.data)
                is ApiResult.Failure -> binding.txtStatus.text =
                    getString(R.string.status_server_error, r.message)
            }
        }
    }

    private fun performQuery() {
        if (loading) return

        val raw = binding.inputQuery.text?.toString().orEmpty()
        val type = TypeDetector.detect(raw)
        if (type == null) {
            Snackbar.make(binding.root, getString(R.string.error_invalid_input), Snackbar.LENGTH_LONG)
                .show()
            return
        }
        val q = TypeDetector.clean(raw)

        setLoading(true)
        showPlaceholder(getString(R.string.hint_querying))

        if (isLocalMode()) {
            lifecycleScope.launch {
                val result = LocalDataSource.query(this@MainActivity, type, q)
                setLoading(false)
                handleResult(result)
            }
        } else {
            val base = Prefs.getBaseUrl(this)
            if (base.isBlank()) {
                setLoading(false)
                Snackbar.make(binding.root, getString(R.string.error_no_server), Snackbar.LENGTH_LONG)
                    .setAction(R.string.action_settings) { openSettings() }
                    .show()
                return
            }
            lifecycleScope.launch {
                val result = api.query(base, q)
                setLoading(false)
                handleResult(result)
            }
        }
    }

    private fun handleResult(result: ApiResult<AggregatedResult>) {
        when (result) {
            is ApiResult.Success -> renderResult(result.data)
            is ApiResult.Failure -> showPlaceholder(getString(R.string.hint_error, result.message))
        }
    }

    private fun renderResult(data: AggregatedResult) {
        val rows = ArrayList<ResultRow>()
        addRow(rows, R.string.field_id, data.id)
        addRow(rows, R.string.field_name, data.name)
        addRow(rows, R.string.field_receiver, data.receiver)
        addRow(rows, R.string.field_nickname, data.nickname)
        addRow(rows, R.string.field_phone, data.phone)
        addRow(rows, R.string.field_email, data.email)
        addRow(rows, R.string.field_qq, data.qq)
        addRow(rows, R.string.field_weibo, data.weibo)
        addRow(rows, R.string.field_address, data.address)
        addRow(rows, R.string.field_car, data.car)
        addRow(rows, R.string.field_contact, data.contact)
        addRow(rows, R.string.field_company, data.company)
        addRow(rows, R.string.field_source, data.source)

        if (rows.isEmpty()) {
            showPlaceholder(getString(R.string.hint_empty))
            return
        }
        binding.emptyView.visibility = View.GONE
        binding.resultList.visibility = View.VISIBLE
        adapter.submit(rows)
    }

    private fun addRow(rows: ArrayList<ResultRow>, labelRes: Int, values: List<String>) {
        if (values.isNotEmpty()) {
            rows.add(ResultRow(getString(labelRes), values))
        }
    }

    private fun setLoading(value: Boolean) {
        loading = value
        binding.progress.visibility = if (value) View.VISIBLE else View.GONE
        binding.btnQuery.isEnabled = !value
        binding.btnClear.isEnabled = !value
    }

    private fun showPlaceholder(message: String) {
        adapter.submit(emptyList())
        binding.resultList.visibility = View.GONE
        binding.emptyView.visibility = View.VISIBLE
        binding.txtEmpty.text = message
    }
}
