package com.berdjoealan.agentattendance
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.Camera
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.GeolocationPermissions
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var loadingSpinner: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var cameraUploadCallback: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        webView = findViewById(R.id.webView)
        loadingSpinner = findViewById(R.id.loadingSpinner)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccess = true

        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                // Show loading spinner when page starts loading
                swipeRefreshLayout.isRefreshing = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Hide loading spinner when page finishes loading
                swipeRefreshLayout.isRefreshing = false
            }

            // Intercept file input element clicks
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString()
                if (url != null && url.startsWith("file://")) {
                    openCamera()
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                // Request camera permission if not granted
                if (ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity, // Replace with your activity reference
                        arrayOf(Manifest.permission.CAMERA),
                        REQUEST_CAMERA
                    )
                } else {
                    // Camera permission already granted, proceed to open camera
                    cameraUploadCallback = filePathCallback
                    openCamera()
                }
                return true
            }

            override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
                super.onGeolocationPermissionsShowPrompt(origin, callback)
                // Request location permission if not granted
                if (ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity, // Replace with your activity reference
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                } else {
                    // Location permission already granted, invoke callback
                    callback?.invoke(origin, true, false)
                }
            }

        }

        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            // Refresh WebView content
            webView.reload()
        }

        webView.loadUrl("https://hristaspen-494847.ingress-earth.ewp.live/my-account/?login=true&page=1&redirect_to=https%3A%2F%2Fhristaspen-494847.ingress-earth.ewp.live%2F")

        // Request location permission
        requestLocationPermission()

    }

    @SuppressLint("MissingPermission")
    private fun requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                // Location permission already granted
                webView.settings.setGeolocationEnabled(true)
            }
        } else {
            // Runtime permission not needed before Marshmallow
            webView.settings.setGeolocationEnabled(true)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                cameraUploadCallback?.onReceiveValue(arrayOf(data?.data ?: Uri.EMPTY))
            } else {
                cameraUploadCallback?.onReceiveValue(null)
            }
            cameraUploadCallback = null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Camera permission granted, handle it if needed
                } else {
                    // Camera permission denied, handle it if needed
                }
            }
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Location permission granted, handle it if needed
                } else {
                    // Location permission denied, handle it if needed
                }
            }
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        // Specify the front-facing camera
        cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", Camera.CameraInfo.CAMERA_FACING_FRONT)
        startActivityForResult(cameraIntent, REQUEST_CAMERA)
    }

    companion object {
        private const val REQUEST_CAMERA = 101
        private const val LOCATION_PERMISSION_REQUEST_CODE = 102
    }

}
