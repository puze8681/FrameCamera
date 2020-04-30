package kr.puze.cameraoutline

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import  android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Build
import android.util.Log
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_camera.*

class CameraActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 111
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    //카메라를 실행시켰을 때 전면 카메라인지 후면카메라인지 설정
    private var CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK
    private var customCamera: CustomCamera? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var finishActivity: AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_camera)
        finishActivity = this
        //셔터를 누르면 사진을 찍음
        button_camera_shutter.setOnClickListener { customCamera?.takePicture() }
        //회전하는 이미지를 누르면 카메라 방향이 바뀜
        button_camera_new.setOnClickListener { transformCamera() }

        //카메라 권한 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionCheckCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            val permissionCheckStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permissionCheckCamera ==
                PackageManager.PERMISSION_GRANTED && permissionCheckStorage == PackageManager.PERMISSION_GRANTED) {
                Log.d("CAMERAACTIVITY", "권한 이미 있음")
                //이미 권한이 있다면 바로 카메라 실행
                startCamera()
            } else {
                Log.d("CAMERAACTIVITY", "권한 없음")
                //권한이 없다면 권한 요구
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
            }
        } else {
            Log.d("CAMERAACTIVITY", "마시멜로 버전 이하로 권한 이미 있음")
            //마시멜로 버전 이하일 때 권한이 이미 있다면 카메라 실행
            startCamera()
        }
    }

    private fun startCamera() {
        Log.e("CAMERAACTIVITY", "startCamera")
        //CAMERA_FACING 의 설정대로의 카메라를 frame_camera 라는 뷰에 입힘
        customCamera = CustomCamera(this, CAMERA_FACING)
        frame_camera.addView(customCamera)
    }

    private fun transformCamera() {
        //CAMERA_FACING 방향을 바꾼 후 다시 카메라를 실행시킴
        if(CAMERA_FACING == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK
            frame_camera.removeAllViews()
            startCamera()
        } else {
            CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_FRONT
            frame_camera.removeAllViews()
            startCamera()
        }
    }

    //카메라에 대한 권한 요구
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("CAMERAACTIVITY", "requestCode : $requestCode, grantResults size : ${grantResults.size}")
        if(requestCode == PERMISSIONS_REQUEST_CODE) {
            var checkResult = true
            for(result in grantResults) {
                if(result != PackageManager.PERMISSION_GRANTED) {
                    checkResult = false
                    break
                }
            }
            if(checkResult) {
                startCamera()
            } else {
                Log.e("CAMERAACTIVITY", "권한 거부")
            }
        }
    }
}