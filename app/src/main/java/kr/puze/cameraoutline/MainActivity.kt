package kr.puze.cameraoutline

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val REQUEST_PERMISSION_CODE = 111

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        id_camera.setOnClickListener {
            checkPermission()
        }
    }

    //카메라 권한 확인
    @TargetApi(Build.VERSION_CODES.M)
    private fun checkPermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            // 권한을 활성화 해주기 위한 설명
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to write the permission.
                Toast.makeText(this@MainActivity, "카메라로 찍어 업로드한 사진을 갤러리에 저장하기 위해 권한을 허용해주세요...", Toast.LENGTH_LONG).show()
            }
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to write the permission.
                Toast.makeText(this@MainActivity, "업로드할 사진을 갤러리에서 불러오기 위해 권한을 허용해주세요...", Toast.LENGTH_LONG).show()
            }
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                // Explain to the user why we need to write the permission.
                Toast.makeText(this@MainActivity, "업로드할 사진을 카메라에서 촬영하기 위해 권한을 허용해주세요...", Toast.LENGTH_LONG).show()
            }

            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA), REQUEST_PERMISSION_CODE)

            // MY_PERMISSION_REQUEST_STORAGE is an
            // app-defined int constant

        } else {
            // 모든 권한 항상 허용
            startActivity(Intent(this@MainActivity, CameraActivity::class.java))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION_CODE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this@MainActivity, "기능 사용을 위한 권한 동의가 필요합니다...", Toast.LENGTH_LONG).show()
            }
        }
    }
}
