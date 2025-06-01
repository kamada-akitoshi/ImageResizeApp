package com.example.imageresize

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.net.Uri
import android.widget.Button
import android.widget.Toast
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import androidx.activity.result.contract.ActivityResultContracts
import android.app.AlertDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // 画像リサイズ用
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            resizeAndSaveImage(it)
        }
    }

    // 拡張子変更用
    private val pickImageForExtensionLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            showExtensionSelectDialog(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.selectImageButton).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        findViewById<Button>(R.id.changeExtensionButton).setOnClickListener {
            pickImageForExtensionLauncher.launch("image/*")
        }
    }

    private fun resizeAndSaveImage(imageUri: Uri) {
        val inputStream = contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val resizedFile = createResizedFile(imageUri)

        val outputStream = FileOutputStream(resizedFile)
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        outputStream.flush()
        outputStream.close()

        Toast.makeText(this, "保存しました: ${resizedFile.absolutePath}", Toast.LENGTH_LONG).show()
    }

    private fun createResizedFile(imageUri: Uri): File {
        val fileName = getFileNameFromUri(imageUri)
        val extension = fileName.substringAfterLast('.', "jpg")
        val baseName = fileName.substringBeforeLast('.', "image")

        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val dateStr = dateFormat.format(Date())
        val resizedFileName = "${dateStr}_resized.$extension"

        val outputDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ResizedImages")
        if (!outputDir.exists()) outputDir.mkdirs()

        return File(outputDir, resizedFileName)
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var name = "image"
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    // --- ここから拡張子変更用 ---

    private fun showExtensionSelectDialog(imageUri: Uri) {
        val extensions = arrayOf("jpg", "jpeg", "png")

        AlertDialog.Builder(this)
            .setTitle("変換する拡張子を選択")
            .setItems(extensions) { _, which ->
                val selectedExt = extensions[which]
                convertAndSaveImage(imageUri, selectedExt)
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    private fun convertAndSaveImage(imageUri: Uri, targetExtension: String) {
        val inputStream = contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val fileName = getFileNameFromUri(imageUri)
        val baseName = fileName.substringBeforeLast('.', "image")

        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val dateStr = dateFormat.format(Date())

        val newFileName = "${dateStr}_converted.$targetExtension"

        val outputDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ResizedImages")
        if (!outputDir.exists()) outputDir.mkdirs()

        val outputFile = File(outputDir, newFileName)
        val outputStream = FileOutputStream(outputFile)

        val format = when (targetExtension.lowercase(Locale.getDefault())) {
            "jpg", "jpeg" -> Bitmap.CompressFormat.JPEG
            "png" -> Bitmap.CompressFormat.PNG
            else -> Bitmap.CompressFormat.JPEG // デフォルトはJPEG
        }

        bitmap.compress(format, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        Toast.makeText(this, "変換して保存しました: ${outputFile.absolutePath}", Toast.LENGTH_LONG).show()
    }
}
