package kr.puze.cameraoutline

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_camera_add.*
import java.io.*
import android.os.*
import android.provider.MediaStore
import android.widget.Toast

class CameraAddActivity : AppCompatActivity() {

    companion object {
        lateinit var data: ByteArray
        lateinit var uri: Uri
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_add)

        Log.d("CameraAddActivity", "onCreate")

        val type = intent.getIntExtra("type", 0)

        when (type) {
            //카메라로 직접 찍어 올리는 경우
            1 -> {
                CameraActivity.finishActivity.finish()
                val width = intent.getIntExtra("width", 0)
                val height = intent.getIntExtra("height", 0)
                val orientation = intent.getIntExtra("orientation", 0)
                val camera = intent.getIntExtra("camera", 0)

                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, options)

                //이미지를 디바이스 방향으로 회전
                val matrix = Matrix()

                //셀카모드면 좌우 반전
                if (camera == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    matrix.setScale(-1f, 1f)
                }

                matrix.postRotate(orientation.toFloat())
                val result = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)

                image_camera_result.setImageBitmap(result)

                button_camera_back.setOnClickListener {
                    startActivity(Intent(this@CameraAddActivity, CameraActivity::class.java))
                    finish()
                }
                button_camera_done.setOnClickListener {
                    //bitmap 을  byte array 로 변환
                    val stream = ByteArrayOutputStream()
                    result.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    val currentData = stream.toByteArray()

                    //파일로 저장
                    makeFile(currentData)
                }
            }
            //갤러리에서 선택해서 올리는 경우
            2 -> {
                val imagePath = getRealPathFromURI(uri) // path 경로
                var imgFile = File(imagePath)
                var exif: ExifInterface? = null
                try {
                    exif = ExifInterface(imagePath)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                val exifOrientation = exif!!.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                val exifDegree = exifOrientationToDegrees(exifOrientation)

                val bitmap = BitmapFactory.decodeFile(imagePath)//경로를 통해 비트맵으로 전환
                image_camera_result.setImageBitmap(rotate(bitmap, exifDegree.toFloat()))//이미지 뷰에 비트맵 넣기

                button_camera_back.setOnClickListener {
                    startActivity(Intent(this@CameraAddActivity, CameraActivity::class.java))
                    finish()
                }
                button_camera_done.setOnClickListener {
                    Toast.makeText(this@CameraAddActivity, "완료입니다.", Toast.LENGTH_LONG).show()
                }
            }
            else -> {
                Toast.makeText(this@CameraAddActivity, "오류입니다.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun makeFile(vararg data: ByteArray) {
        val outStream: FileOutputStream?

        try {
            val path = File(Environment.getExternalStorageDirectory().absolutePath + "/everywear")
            if (!path.exists()) {
                path.mkdirs()
            }

            @SuppressLint("DefaultLocale") val fileName = String.format("%d.jpg", System.currentTimeMillis())
            val outputFile = File(path, fileName)

            outStream = FileOutputStream(outputFile)
            outStream.write(data[0])
            outStream.flush()
            outStream.close()

            Log.d("CUSTOMCAMERA", "onPictureTaken - wrote bytes: " + data.size + " to " + outputFile.absolutePath)
            Log.d("CAMERATAG", "Camera Upload Complete")
            // 갤러리에 반영
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = Uri.fromFile(outputFile)
            application.sendBroadcast(mediaScanIntent)
            Toast.makeText(this@CameraAddActivity, "${outputFile}로 저장되었습니다.", Toast.LENGTH_LONG).show()
            finish()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //사진이 저장된 절대 경로 구하기
    private fun getRealPathFromURI(contentUri: Uri): String {
        var column_index = 0
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(contentUri, proj, null, null, null)
        if (cursor!!.moveToFirst()) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        }
        return cursor.getString(column_index)
    }

    //사진의 회전값 구하기
    private fun exifOrientationToDegrees(exifOrientation: Int): Int {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270
        }
        return 0
    }

    //사진을 정방향대로 회전
    private fun rotate(src: Bitmap, degree: Float): Bitmap {

        // Matrix 객체 생성
        val matrix = Matrix()
        // 회전 각도 셋팅
        matrix.postRotate(degree)
        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.width,
            src.height, matrix, true)
    }


}