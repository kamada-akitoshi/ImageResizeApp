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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.registerForActivityResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            resizeAndSaveImage(it)
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.selectImageButton).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun resizeAndSaveImage(imageUri: Uri) {
        val inputStream = contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // 50%に圧縮（ここでは品質50%として保存）
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

// タイムスタンプを yyyymmdd に変換

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
}
