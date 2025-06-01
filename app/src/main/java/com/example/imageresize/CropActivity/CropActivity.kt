package com.example.imageresize

import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class CropActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var cropOverlay: CropOverlayView
    private lateinit var cropButton: Button
    private lateinit var originalUri: Uri
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)

        imageView = findViewById(R.id.imageView)
        cropOverlay = findViewById(R.id.cropOverlay)
        cropButton = findViewById(R.id.cropButton)

        val uriStr = intent.getStringExtra("imageUri")
        if (uriStr == null) {
            Toast.makeText(this, "画像が見つかりません", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        originalUri = Uri.parse(uriStr)

        contentResolver.openInputStream(originalUri)?.use { inputStream ->
            bitmap = BitmapFactory.decodeStream(inputStream)
        }

        imageView.setImageBitmap(bitmap)

        cropButton.setOnClickListener {
            val cropRectInView = cropOverlay.getCropRect()

            // ImageViewの画像表示範囲を取得
            val drawable = imageView.drawable ?: return@setOnClickListener
            val matrix = imageView.imageMatrix
            val values = FloatArray(9)
            matrix.getValues(values)

            val scaleX = values[Matrix.MSCALE_X]
            val scaleY = values[Matrix.MSCALE_Y]
            val transX = values[Matrix.MTRANS_X]
            val transY = values[Matrix.MTRANS_Y]

            // ImageView内のBitmapの表示サイズ
            val drawableWidth = drawable.intrinsicWidth
            val drawableHeight = drawable.intrinsicHeight

            // Bitmap上のトリミング開始座標・サイズを計算
            val left = ((cropRectInView.left - transX) / scaleX).toInt().coerceIn(0, drawableWidth - 1)
            val top = ((cropRectInView.top - transY) / scaleY).toInt().coerceIn(0, drawableHeight - 1)
            val right = ((cropRectInView.right - transX) / scaleX).toInt().coerceIn(left + 1, drawableWidth)
            val bottom = ((cropRectInView.bottom - transY) / scaleY).toInt().coerceIn(top + 1, drawableHeight)

            val cropWidth = right - left
            val cropHeight = bottom - top

            if (cropWidth <= 0 || cropHeight <= 0) {
                Toast.makeText(this, "正しい範囲を選択してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val croppedBitmap = Bitmap.createBitmap(bitmap, left, top, cropWidth, cropHeight)
            saveCroppedImage(croppedBitmap)
        }
    }

    private fun saveCroppedImage(bitmap: Bitmap) {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
        val fileName = "${dateFormat.format(Date())}_cropped.jpg"
        val outputDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ResizedImages")
        if (!outputDir.exists()) outputDir.mkdirs()

        val file = File(outputDir, fileName)
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        }

        Toast.makeText(this, "トリミングして保存しました: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        finish()
    }
}
