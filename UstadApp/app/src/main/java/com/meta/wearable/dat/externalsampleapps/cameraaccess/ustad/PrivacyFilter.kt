// PrivacyFilter - On-device bystander protection before any image leaves the phone
//
// Detects faces with the platform android.media.FaceDetector (no extra dependency, runs offline)
// and pixelates them, so only the machine — never a bystander's face — is sent to the AI.

package com.meta.wearable.dat.externalsampleapps.cameraaccess.ustad

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.Rect
import android.media.FaceDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PrivacyResult(val image: Bitmap, val facesBlurred: Int)

object PrivacyFilter {
  private const val MAX_FACES = 8
  private const val PIXEL_BLOCKS = 12

  suspend fun blurFaces(source: Bitmap): PrivacyResult =
      withContext(Dispatchers.Default) {
        // FaceDetector needs an RGB_565 bitmap with an even width.
        val evenWidth = source.width - (source.width % 2)
        val detectInput = Bitmap.createBitmap(source, 0, 0, evenWidth, source.height).copy(Bitmap.Config.RGB_565, false)
        val faces = arrayOfNulls<FaceDetector.Face>(MAX_FACES)
        val found = runCatching { FaceDetector(evenWidth, source.height, MAX_FACES).findFaces(detectInput, faces) }.getOrDefault(0)
        detectInput.recycle()
        if (found == 0) return@withContext PrivacyResult(source, 0)

        val out = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(out)
        val mid = PointF()
        faces.take(found).filterNotNull().forEach { face ->
          face.getMidPoint(mid)
          // Eye distance → generous box covering the whole head.
          val r = (face.eyesDistance() * 1.8f).toInt()
          val box =
              Rect(
                  (mid.x - r).toInt().coerceAtLeast(0),
                  (mid.y - r * 1.2f).toInt().coerceAtLeast(0),
                  (mid.x + r).toInt().coerceAtMost(out.width),
                  (mid.y + r * 1.4f).toInt().coerceAtMost(out.height),
              )
          if (box.width() > 1 && box.height() > 1) {
            val crop = Bitmap.createBitmap(out, box.left, box.top, box.width(), box.height())
            val tiny = Bitmap.createScaledBitmap(crop, PIXEL_BLOCKS, PIXEL_BLOCKS, false)
            val pixelated = Bitmap.createScaledBitmap(tiny, box.width(), box.height(), false)
            canvas.drawBitmap(pixelated, box.left.toFloat(), box.top.toFloat(), null)
            crop.recycle(); tiny.recycle(); pixelated.recycle()
          }
        }
        PrivacyResult(out, found)
      }
}
