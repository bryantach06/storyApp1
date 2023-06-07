package com.example.storyapp.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.viewmodels.SignupViewModel
import com.example.storyapp.models.UserPreference
import com.example.storyapp.viewmodels.ViewModelFactory
import com.example.storyapp.api.ApiConfig
import com.example.storyapp.api.ApiService
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.databinding.ActivitySignUpBinding
import com.example.storyapp.responses.RegisterResponse
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SignUpActivity : AppCompatActivity() {

    private lateinit var storyRepo: StoryRepository
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var viewModel: SignupViewModel
    private lateinit var apiService: ApiService
    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = ApiConfig.getApiService()
        context = this@SignUpActivity
        storyRepo = StoryRepository(apiService, context)

        setupView()
        setupViewModel()
        setupAction()
        setupAnimation()
    }

    private fun setupAnimation() {
        val title = ObjectAnimator.ofFloat(binding.tvSignupTitle, View.ALPHA, 1f).setDuration(300)
        val name = ObjectAnimator.ofFloat(binding.nameRegisterTil, View.ALPHA, 1f).setDuration(300)
        val email =
            ObjectAnimator.ofFloat(binding.emailRegisterTil, View.ALPHA, 1f).setDuration(300)
        val password =
            ObjectAnimator.ofFloat(binding.passwordRegisterTil, View.ALPHA, 1f).setDuration(300)
        val btnSignUp = ObjectAnimator.ofFloat(binding.btnRegister, View.ALPHA, 1f).setDuration(300)

        AnimatorSet().apply {
            playSequentially(title, name, email, password, btnSignUp)
            startDelay = 500
            start()
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

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore), storyRepo)
        )[SignupViewModel::class.java]
    }

    private fun setMyButtonEnable() {
        val name = binding.edRegisterName.text
        val email = binding.edRegisterEmail.text
        val password = binding.edRegisterPassword.text
        binding.btnRegister.isEnabled =
            name != null && email != null && password != null && name.toString()
                .isNotEmpty() && email.toString().isNotEmpty() && password.toString().isNotEmpty()
    }

    private fun setupAction() {
        binding.edRegisterName.addTextChangedListener {
            setMyButtonEnable()
        }

        binding.edRegisterEmail.addTextChangedListener {
            setMyButtonEnable()
        }

        binding.edRegisterPassword.addTextChangedListener {
            setMyButtonEnable()
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()
            val client = ApiConfig.getApiService().onRegister(name, email, password)
            client.enqueue(object : retrofit2.Callback<RegisterResponse> {
                override fun onResponse(
                    call: Call<RegisterResponse>,
                    response: Response<RegisterResponse>
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null && !responseBody.error) {
                            AlertDialog.Builder(this@SignUpActivity).apply {
                                setTitle("Yeah!")
                                setMessage("Kamu berhasil registrasi. Yuk Login!")
                                setPositiveButton("Login") { _, _ ->
                                    finish()
                                }
                                create()
                                show()
                            }
                        }
                    } else {
                        val jsonObj =
                            JSONObject(response.errorBody()!!.charStream().readText())
                        Toast.makeText(
                            this@SignUpActivity,
                            jsonObj.getString("message"),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    Log.d("SignUpActivity", "${t.message}")
                }
            })
        }
    }
}