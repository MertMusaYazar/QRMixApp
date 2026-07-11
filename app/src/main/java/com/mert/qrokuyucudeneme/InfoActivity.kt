package com.mert.qrokuyucudeneme

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        // TextView referansı
        val tvVersion = findViewById<TextView>(R.id.tvVersion)

        // PackageManager ile versionName al
        val versionName = try {
            packageManager
                .getPackageInfo(packageName, 0)
                .versionName
        } catch (e: Exception) {
            "Bilinmiyor"
        }

        tvVersion.text = "Uygulama Sürümü: v5.1"
    }

    // Kapat butonuna basıldığında aktiviteyi bitir
    fun onCloseInfoClicked(v: View) {
        finish()
    }
}
