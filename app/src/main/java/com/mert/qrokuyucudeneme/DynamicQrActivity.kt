package com.mert.qrokuyucudeneme

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.KeyListener
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

class DynamicQrActivity : AppCompatActivity() {

    private val firestore by lazy {
        FirebaseFirestore.getInstance().apply {
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        }
    }
    private val collectionName = "dynamic_qrs"
    private val prefixText     = "mertdynamicqr52-"
    private val prefixTable    = "mertdynamicqr(52)-"

    private lateinit var tableLayout: TableLayout
    private lateinit var tabLayout: TabLayout
    private var isTableAuthenticated = false
    private val originalKeyListeners = mutableMapOf<EditText, KeyListener?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dynamic_qr)

        tableLayout   = findViewById(R.id.tableLayout)
        tabLayout     = findViewById(R.id.tabLayout)
        val scrollText  = findViewById<ScrollView>(R.id.scrollText)
        val scrollTable = findViewById<ScrollView>(R.id.scrollTable)

        findViewById<Button>(R.id.btnBackAll).setOnClickListener { finish() }
        findViewById<TextView>(R.id.btnInfo).setOnClickListener {
            startActivity(Intent(this, InfoActivity::class.java))
        }

        val editKey     = intent.getStringExtra("edit_key")
        val isEditTable = editKey?.startsWith(prefixTable) == true
        val isEditText  = editKey?.startsWith(prefixText)  == true

        // Başlangıç sekmesi
        if (isEditTable) {
            tabLayout.getTabAt(1)?.select()
            scrollText.visibility  = ScrollView.GONE
            scrollTable.visibility = ScrollView.VISIBLE
        } else {
            tabLayout.getTabAt(0)?.select()
            scrollText.visibility  = ScrollView.VISIBLE
            scrollTable.visibility = ScrollView.GONE
        }

        setupTextSection(editKey, isEditText)
        setupTableSection(editKey, isEditTable)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                scrollText.visibility  = if (tab.position == 0) ScrollView.VISIBLE else ScrollView.GONE
                scrollTable.visibility = if (tab.position == 1) ScrollView.VISIBLE else ScrollView.GONE
                applyTableMode()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) { onTabSelected(tab) }
        })

        applyTableMode()
    }

    // ===== Dinamik Metin =====
    private fun setupTextSection(editKey: String?, isEditMode: Boolean) {
        val etId       = findViewById<EditText>(R.id.etDynamicId)
        val etContent  = findViewById<EditText>(R.id.etDynamicContent)
        val etPassword = findViewById<EditText>(R.id.etPasswordText)
        val btnSave    = findViewById<Button>(R.id.btnSaveDynamicText)
        val ivQr       = findViewById<ImageView>(R.id.ivDynamicQrText)

        val originalKeyListener = etContent.keyListener

        if (isEditMode && editKey != null) {
            etId.setText(editKey.removePrefix(prefixText))
            firestore.collection(collectionName).document(editKey).get()
                .addOnSuccessListener { doc ->
                    etContent.setText(doc.getString("content").orEmpty())
                    ivQr.setImageBitmap(generateQRCode(editKey))
                }
            etContent.keyListener       = null
            etContent.isCursorVisible   = false
            etContent.setTextIsSelectable(true)
            btnSave.isEnabled           = false

            etPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                override fun onTextChanged(s: CharSequence?, st: Int, b: Int, cnt: Int) {
                    if (s != null && s.length == 6) {
                        firestore.collection(collectionName).document(editKey).get()
                            .addOnSuccessListener { doc2 ->
                                val actualPw = doc2.getString("password").orEmpty()
                                if (s.toString() == actualPw) {
                                    etContent.keyListener     = originalKeyListener
                                    etContent.isCursorVisible = true
                                    btnSave.isEnabled         = true
                                    ivQr.setImageBitmap(generateQRCode(editKey))
                                } else {
                                    Toast.makeText(
                                        this@DynamicQrActivity,
                                        "Şifre yanlış!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        } else {
            etContent.keyListener     = originalKeyListener
            etContent.isCursorVisible = true
            btnSave.isEnabled         = true
        }

        btnSave.setOnClickListener {
            val idText  = etId.text.toString().trim()
            val content = etContent.text.toString().trim()
            if (isEditMode && editKey != null) {
                firestore.collection(collectionName).document(editKey)
                    .update("content", content)
                    .addOnSuccessListener {
                        ivQr.setImageBitmap(generateQRCode(editKey))
                        Toast.makeText(this, "Metin güncellendi", Toast.LENGTH_SHORT).show()
                    }
            } else {
                val pw  = etPassword.text.toString()
                val key = prefixText + idText
                firestore.collection(collectionName).document(key)
                    .set(mapOf("content" to content, "password" to pw))
                    .addOnSuccessListener {
                        ivQr.setImageBitmap(generateQRCode(key))
                        Toast.makeText(this, "Metin kaydedildi", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    // ===== Dinamik Tablo =====
    private fun setupTableSection(editKey: String?, isEditMode: Boolean) {
        val etId         = findViewById<EditText>(R.id.etTableId)
        val etPassword   = findViewById<EditText>(R.id.etPasswordTable)
        val btnSave      = findViewById<Button>(R.id.btnSaveDynamicTable)
        val ivQr         = findViewById<ImageView>(R.id.ivDynamicQrTable)
        val btnAddRow    = findViewById<ImageButton>(R.id.btnAddRow)
        val btnRemoveRow = findViewById<ImageButton>(R.id.btnRemoveRow)
        val btnAddCol    = findViewById<ImageButton>(R.id.btnAddCol)
        val btnRemoveCol = findViewById<ImageButton>(R.id.btnRemoveCol)

        if (isEditMode && editKey != null) {
            etId.setText(editKey.removePrefix(prefixTable))
            firestore.collection(collectionName).document(editKey).get()
                .addOnSuccessListener { doc ->
                    loadAndRenderTable(doc.getString("tableData").orEmpty())
                    ivQr.setImageBitmap(generateQRCode(editKey))
                }
            isTableAuthenticated = false
            applyTableMode()

            etPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                override fun onTextChanged(s: CharSequence?, st: Int, b: Int, cnt: Int) {
                    if (s != null && s.length == 6 && !isTableAuthenticated) {
                        firestore.collection(collectionName).document(editKey).get()
                            .addOnSuccessListener { doc2 ->
                                val actualPw = doc2.getString("password").orEmpty()
                                if (s.toString() == actualPw) {
                                    isTableAuthenticated = true
                                    applyTableMode()
                                    ivQr.setImageBitmap(generateQRCode(editKey))
                                } else {
                                    Toast.makeText(this@DynamicQrActivity, "Şifre yanlış!", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        } else {
            // Yeni oluşturma modu: 2x2 boş tablo, hemen düzenlenebilir
            renderTable(emptyTable())
            isTableAuthenticated = true
            makeAllCellsEditable()
            btnSave.isEnabled = true
            listOf(btnAddRow, btnRemoveRow, btnAddCol, btnRemoveCol).forEach { it.isEnabled = true }
        }

        btnAddRow   .setOnClickListener { changeTable { cur -> cur.add(MutableList(cur.first().size){ "" }) } }
        btnRemoveRow.setOnClickListener { changeTable { cur -> if (cur.size>1) cur.removeAt(cur.lastIndex) } }
        btnAddCol   .setOnClickListener { changeTable { cur -> cur.forEach { row -> row.add("") } } }
        btnRemoveCol.setOnClickListener { changeTable { cur -> cur.forEach { row -> if (row.size>1) row.removeAt(row.lastIndex) } } }

        btnSave.setOnClickListener {
            if (!isTableAuthenticated) return@setOnClickListener
            val idText = etId.text.toString().trim()
            val pw     = etPassword.text.toString()
            val key    = editKey ?: (prefixTable + idText)
            val data   = buildTableString()
            val op     = if (editKey != null)
                firestore.collection(collectionName).document(editKey).update("tableData", data)
            else
                firestore.collection(collectionName).document(key)
                    .set(mapOf("tableData" to data, "password" to pw))
            op.addOnSuccessListener {
                ivQr.setImageBitmap(generateQRCode(key))
                Toast.makeText(
                    this,
                    if (editKey != null) "Tablo güncellendi" else "Tablo kaydedildi",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun emptyTable() = List(2) { List(2) { "" } }

    private fun changeTable(modify: (MutableList<MutableList<String>>) -> Unit) {
        val cur = extractTableData()
        modify(cur)
        renderTable(cur)
        applyTableMode()
    }

    private fun applyTableMode() {
        if (isTableAuthenticated) makeAllCellsEditable() else makeAllCellsReadOnly()
        findViewById<Button>(R.id.btnSaveDynamicTable).isEnabled = isTableAuthenticated
        listOf(R.id.btnAddRow, R.id.btnRemoveRow, R.id.btnAddCol, R.id.btnRemoveCol).forEach { id ->
            findViewById<ImageButton>(id).isEnabled = isTableAuthenticated
        }
    }

    private fun loadAndRenderTable(raw: String) {
        val rows = raw.split("\n(---) ").map { it.split(" #|# ") }
        renderTable(rows)
        applyTableMode()
    }

    private fun makeAllCellsReadOnly() {
        tableLayout.children.filterIsInstance<TableRow>().forEach { tr ->
            tr.children.filterIsInstance<EditText>().forEach { et ->
                originalKeyListeners[et] = et.keyListener
                et.keyListener = null
                et.isEnabled = false
            }
        }
    }

    private fun makeAllCellsEditable() {
        tableLayout.children.filterIsInstance<TableRow>().forEach { tr ->
            tr.children.filterIsInstance<EditText>().forEach { et ->
                et.keyListener = originalKeyListeners[et]
                et.isEnabled = true
                et.isFocusable = true
                et.isFocusableInTouchMode = true
                et.isCursorVisible = true
            }
        }
    }

    private fun renderTable(data: List<List<String>>) {
        tableLayout.removeAllViews()
        data.forEachIndexed { r, rowVals ->
            val tr = TableRow(this)
            rowVals.forEachIndexed { c, txt ->
                val et = EditText(this).apply {
                    setText(txt)
                    hint = if (r == 0) "Sütun ${c+1}" else "Satır ${r+1},${c+1}"
                    setPadding(16, 8, 16, 8)
                    layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                }
                originalKeyListeners[et] = et.keyListener
                tr.addView(et)
            }
            tableLayout.addView(tr)
        }
    }

    private fun extractTableData(): MutableList<MutableList<String>> =
        MutableList(tableLayout.childCount) { r ->
            val tr = tableLayout.getChildAt(r) as TableRow
            MutableList(tr.childCount) { c ->
                (tr.getChildAt(c) as EditText).text.toString()
            }
        }

    private fun buildTableString(): String =
        extractTableData().joinToString("\n(---) ") { row ->
            row.joinToString(" #|# ")
        }

    private fun generateQRCode(text: String): Bitmap? = try {
        val matrix: BitMatrix = MultiFormatWriter().encode(
            text, BarcodeFormat.QR_CODE, 512, 512
        )
        Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565).also { bmp ->
            for (x in 0 until 512) for (y in 0 until 512)
                bmp.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
        }
    } catch (e: Exception) {
        Toast.makeText(this, "QR oluşturulamadı: ${e.message}", Toast.LENGTH_SHORT).show()
        null
    }
}
