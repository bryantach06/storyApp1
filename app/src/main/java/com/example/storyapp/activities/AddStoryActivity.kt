package com.example.storyapp.activities

import android.Manifest
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.storyapp.*
import com.example.storyapp.api.ApiConfig
import com.example.storyapp.api.ApiService
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.databinding.ActivityAddStoryBinding
import com.example.storyapp.viewmodels.AddStoryViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var getFile: File? = null
    private var location: Location? = null
    private lateinit var addStoryViewModel: AddStoryViewModel

    private lateinit var pref: StoryRepository
    private lateinit var apiService: ApiService

    companion object {
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    "Tidak mendapatkan permission.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = ApiConfig.getApiService()

        pref = StoryRepository(apiService, this@AddStoryActivity)

        addStoryViewModel = AddStoryViewModel(pref)

//        viewModelFactory = ViewModelFactory.getInstance(this@AddStoryActivity, pref)

        binding.ivAddStory.setImageResource(R.drawable.baseline_camera_alt_24)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        binding.btnCamera.setOnClickListener {
            startCamera()
        }

        binding.btnGallery.setOnClickListener {
            startGallery()
        }

        binding.buttonAdd.setOnClickListener {
            uploadStory()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupView()
    }

    private fun startCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.data?.getSerializableExtra("picture", File::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.data?.getSerializableExtra("picture")
            } as? File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean
            myFile?.let { file ->
                rotateFile(file, isBackCamera)
                getFile = file
                binding.ivAddStory.setImageBitmap(BitmapFactory.decodeFile(file.path))
            }
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data as Uri
            selectedImg.let { uri ->
                val myFile = uriToFile(uri, this@AddStoryActivity)
                getFile = myFile
                binding.ivAddStory.setImageURI(uri)
            }
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLocation() {
        if (
            checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    this.location = loc
                } else {
                    Toast.makeText(
                        this, getString(R.string.empty_location), Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener { e ->
                Log.e("FusedLocationClient :", e.message.toString())
                print(e)
            }
        } else {
            // Handle the case where permissions are not granted
            Toast.makeText(
                this, "Location permissions not granted.", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun uploadStory() {
        if (getFile != null) {
            val file = reduceFileImage(getFile as File)
            val description = binding.edAddDescription.text.toString()
            val requestImageFile = file.asRequestBody("image/jpeg".toMediaType())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )

            getMyLocation()
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    val lat = loc.latitude
                    val lon = loc.longitude
                    val loginSession = LoginSession(this)
                    uploadResponse(
                        "Bearer ${loginSession.passToken().toString()}",
                        imageMultipart,
                        description.toRequestBody("text/plain".toMediaType()),
                        lat,
                        lon
                    )
                } else {
                    Toast.makeText(
                        this, getString(R.string.empty_location), Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(
                    this, "Failed to retrieve location: ${e.message}", Toast.LENGTH_SHORT
                ).show()
                Log.e("FusedLocationClient :", e.message.toString())
                print(e)
            }
        } else {
            Toast.makeText(
                this@AddStoryActivity,
                "Please select an image file.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun uploadResponse(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
        lat: Double?,
        lon: Double?
    ) {
        addStoryViewModel.uploadStory(token, file, description, lat, lon)
        addStoryViewModel.fileUploadResponse.observe(this) { response ->
            response?.let {
                if (!it.error) {
                    print(it.lat)
                    print(it.lon)
                    AlertDialog.Builder(this@AddStoryActivity).apply {
                        setTitle("Yeah!")
                        setMessage("Upload story berhasil!")
                        setPositiveButton("Lihat Story") { _, _ ->
                            val intent = Intent(this@AddStoryActivity, MainActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                            finish()
                        }
                        create()
                        show()
                    }
                } else {
                    // Handle error case if needed
                    Log.e(
                        "UploadStoryResponse",
                        "Error: ${it.message}"
                    )
                }
            }
        }
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }
}

//val apiService = ApiConfig.getApiService()
//            val uploadImageRequest = apiService.uploadStory("Bearer ${loginSession.passToken().toString()}", imageMultipart, description, lat, lon)
//            uploadImageRequest.enqueue(object : Callback<UploadStoryResponse> {
//                override fun onResponse(
//                    call: Call<UploadStoryResponse>,
//                    response: Response<UploadStoryResponse>
//                ) {
//                    if (response.isSuccessful) {
//                        val responseBody = response.body()
//                        if (responseBody != null && !responseBody.error) {
//                            Toast.makeText(this@AddStoryActivity, responseBody.message, Toast.LENGTH_SHORT).show()
//                            AlertDialog.Builder(this@AddStoryActivity).apply {
//                                setTitle("Yeah!")
//                                setMessage("Upload story berhasil!")
//                                setPositiveButton("Lihat Story") {_, _ ->
//                                    val intent = Intent(this@AddStoryActivity, MainActivity::class.java)
//                                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
//                                    startActivity(intent)
//                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
//                                    finish()
//                                }
//                                create()
//                                show()
//                            }
//                            print(response.body())
//                        }
//                    } else {
//                        Toast.makeText(this@AddStoryActivity, response.message(), Toast.LENGTH_SHORT).show()
//                    }
//                }
//                override fun onFailure(call: Call<UploadStoryResponse>, t: Throwable) {
//                    Toast.makeText(this@AddStoryActivity, t.message, Toast.LENGTH_SHORT).show()
//                }
//            })