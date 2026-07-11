package com.mert.qrokuyucudeneme

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

class QrGeneratorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_generator)

        val etQrText = findViewById<EditText>(R.id.etQrText)
        val btnGenerate = findViewById<Button>(R.id.btnGenerate)
        val btnDynamicQr = findViewById<Button>(R.id.btnDynamicQr)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val ivQrCode = findViewById<ImageView>(R.id.ivQrCode)

        btnBack.setOnClickListener { finish() }

        btnGenerate.setOnClickListener {
            val text = etQrText.text.toString()
            if (text.isNotEmpty()) {
                generateQRCode(text)?.let { ivQrCode.setImageBitmap(it) }
            } else {
                Toast.makeText(this, "Metin giriniz!", Toast.LENGTH_SHORT).show()
            }
        }

        btnDynamicQr.setOnClickListener {
            startActivity(Intent(this, DynamicQrActivity::class.java))
        }
    }

    private fun generateQRCode(text: String): Bitmap? {
        return try {
            val matrix: BitMatrix = MultiFormatWriter().encode(
                text, BarcodeFormat.QR_CODE, 512, 512
            )
            val bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
            for (x in 0 until 512) {
                for (y in 0 until 512) {
                    bmp.setPixel(
                        x, y,
                        if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                    )
                }
            }
            bmp
        } catch (e: Exception) {
            Toast.makeText(this, "QR oluşturulamadı: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }
}
