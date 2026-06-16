package com.candlevision.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Image helpers: capture-file creation, downscaling, EXIF rotation,
 * base64 (JPEG) encoding for the API, and thumbnail decoding for history.
 */
object ImageUtils {

    /** Keep the longest edge around this size to stay fast and memory-friendly. */
    private const val MAX_EDGE = 1568
    private const val JPEG_QUALITY = 85

    private fun chartsDir(context: Context): File =
        File(context.filesDir, "charts").apply { if (!exists()) mkdirs() }

    /** Creates an empty file (in our storage) and a shareable URI for the camera. */
    fun newCaptureFile(context: Context): Pair<File, Uri> {
        val file = File(chartsDir(context), "chart_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        return file to uri
    }

    /** Imports a gallery picture into our storage as a downscaled JPEG. Returns the path. */
    fun importUri(context: Context, uri: Uri): String {
        val bitmap = decodeFromUri(context, uri) ?: error("Image illisible")
        val rotated = applyExif(context, uri, bitmap)
        val out = File(chartsDir(context), "chart_${System.currentTimeMillis()}.jpg")
        writeJpeg(rotated, out)
        return out.absolutePath
    }

    /** Re-processes a freshly captured photo in place (downscale + rotate). */
    fun processCaptured(path: String) {
        val file = File(path)
        val bitmap = decodeFromFile(path) ?: return
        val rotated = applyExifFromPath(path, bitmap)
        writeJpeg(rotated, file)
    }

    /** Small bitmap for the history list (cheap to decode). */
    fun decodeThumb(path: String, targetPx: Int = 160): Bitmap? {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, opts)
        opts.inSampleSize = sampleSize(opts.outWidth, opts.outHeight, targetPx)
        opts.inJustDecodeBounds = false
        val bmp = BitmapFactory.decodeFile(path, opts) ?: return null
        return applyExifFromPath(path, bmp)
    }

    // --- internals ---

    private fun decodeFromUri(context: Context, uri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri).use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, MAX_EDGE)
        }
        val decoded = context.contentResolver.openInputStream(uri).use {
            BitmapFactory.decodeStream(it, null, opts)
        } ?: return null
        return scaleDown(decoded)
    }

    private fun decodeFromFile(path: String): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, MAX_EDGE)
        }
        val decoded = BitmapFactory.decodeFile(path, opts) ?: return null
        return scaleDown(decoded)
    }

    private fun scaleDown(bmp: Bitmap): Bitmap {
        val longest = max(bmp.width, bmp.height)
        if (longest <= MAX_EDGE) return bmp
        val ratio = MAX_EDGE.toFloat() / longest
        val w = (bmp.width * ratio).roundToInt()
        val h = (bmp.height * ratio).roundToInt()
        val scaled = Bitmap.createScaledBitmap(bmp, w, h, true)
        if (scaled != bmp) bmp.recycle()
        return scaled
    }

    private fun applyExif(context: Context, uri: Uri, bitmap: Bitmap): Bitmap =
        context.contentResolver.openInputStream(uri).use { stream ->
            if (stream == null) bitmap else rotate(bitmap, ExifInterface(stream).orientationDegrees())
        }

    private fun applyExifFromPath(path: String, bitmap: Bitmap): Bitmap =
        rotate(bitmap, ExifInterface(path).orientationDegrees())

    private fun ExifInterface.orientationDegrees(): Int =
        when (getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

    private fun rotate(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        val m = Matrix().apply { postRotate(degrees.toFloat()) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
        if (rotated != bitmap) bitmap.recycle()
        return rotated
    }

    private fun writeJpeg(bitmap: Bitmap, file: File) {
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
    }

    private fun sampleSize(width: Int, height: Int, target: Int): Int {
        var sample = 1
        var w = width
        var h = height
        while (w / 2 >= target && h / 2 >= target) {
            w /= 2
            h /= 2
            sample *= 2
        }
        return sample
    }
}
