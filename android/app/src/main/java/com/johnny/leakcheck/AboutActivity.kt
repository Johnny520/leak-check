package com.johnny.leakcheck

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.johnny.leakcheck.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.txtVersion.text = getString(R.string.about_version_value, BuildConfig.VERSION_NAME)
        binding.txtAuthorName.text = getString(R.string.about_author_name)

        val authorUrl = getString(R.string.about_author_url)
        val projectUrl = getString(R.string.about_project_url)

        binding.txtAuthorUrl.text = authorUrl
        binding.txtAuthorUrl.setOnClickListener { openUrl(authorUrl) }

        binding.txtProjectUrl.text = projectUrl
        binding.txtProjectUrl.setOnClickListener { openUrl(projectUrl) }
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.about_open_failed), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.about_open_failed), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
