// <proje-root>/app/src/main/java/com/mert/qrokuyucudeneme/MainActivity.kt
package com.mert.qrokuyucudeneme

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class MainActivity : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner
    private lateinit var btnCopy: Button
    private val handler = Handler(Looper.getMainLooper())
    private var isScanningAllowed = true
    private val scanDelay = 5000L

    companion object {
        private const val CAMERA_REQUEST_CODE = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge görünümler
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Firestore offline cache’i aktif et
        FirebaseFirestore.getInstance().apply {
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        }

        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
        btnCopy = findViewById(R.id.btnCopy)

        // CodeScanner kurulumu
        codeScanner = CodeScanner(this, scannerView).apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS
            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true
            isFlashEnabled = false

            decodeCallback = DecodeCallback { result ->
                if (!isScanningAllowed) return@DecodeCallback
                isScanningAllowed = false

                runOnUiThread {
                    val text = result.text
                    when {
                        text.startsWith("mertdynamicqr(52)-") ||
                                text.startsWith("mertdynamicqr52-") -> {
                            startActivity(
                                Intent(this@MainActivity, DynamicQrActivity::class.java)
                                    .putExtra("edit_key", text)
                            )
                        }
                        else -> {
                            Toast.makeText(this@MainActivity, text, Toast.LENGTH_LONG).show()
                            btnCopy.apply {
                                visibility = Button.VISIBLE
                                setOnClickListener { copyToClipboard(text) }
                            }
                        }
                    }
                }
                handler.postDelayed({
                    isScanningAllowed = true
                    btnCopy.visibility = Button.GONE
                }, scanDelay)
            }

            errorCallback = ErrorCallback { e ->
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Kamera hatası: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        // Kamera izni kontrolü
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            codeScanner.startPreview()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        }

        // Preview yeniden başlatma
        scannerView.setOnClickListener {
            if (hasCameraPermission()) codeScanner.startPreview()
        }

        // Oluştur ekranına geçiş
        findViewById<Button>(R.id.btnGenerateQr).setOnClickListener {
            startActivity(Intent(this, QrGeneratorActivity::class.java))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            codeScanner.startPreview()
        } else {
            Toast.makeText(this, "Kamera izni gerekli!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        isScanningAllowed = true
        if (hasCameraPermission()) codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun hasCameraPermission() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    private fun copyToClipboard(text: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("QR İçeriği", text))
        Toast.makeText(this, "Kopyalandı!", Toast.LENGTH_SHORT).show()
    }
}
