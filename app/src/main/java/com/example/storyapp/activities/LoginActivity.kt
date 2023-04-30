package com.example.storyapp.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.*
import com.example.storyapp.databinding.ActivityLoginPageBinding
import com.example.storyapp.models.UserModel
import com.example.storyapp.models.UserPreference
import com.example.storyapp.viewmodels.LoginViewModel
import com.example.storyapp.viewmodels.ViewModelFactory

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginPageBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var user: UserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvSignUp.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        setupView()
        setupViewModel()
        setupAction()
        setupAnimation()
    }

    private fun setupAnimation() {
        val title = ObjectAnimator.ofFloat(binding.tvLoginTitle, View.ALPHA, 1f).setDuration(300)
        val email = ObjectAnimator.ofFloat(binding.tilLoginEmail, View.ALPHA, 1f).setDuration(300)
        val password = ObjectAnimator.ofFloat(binding.tilLoginPassword, View.ALPHA, 1f).setDuration(300)
        val btnLogin = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(300)
        val signUp = ObjectAnimator.ofFloat(binding.tvSignUp, View.ALPHA, 1f).setDuration(300)

        AnimatorSet().apply {
            playSequentially(title, email, password, btnLogin, signUp)
            startDelay = 500
            start()
        }
    }

    private fun setupView() {
        binding.progressBar.visibility = View.GONE
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
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[LoginViewModel::class.java]

        viewModel.getUser().observe(this) { user ->
            this.user = user
        }
    }

    private fun setupAction() {
        binding.btnLogin.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.myEditText.text.toString()
            when {
                email.isEmpty() -> {
                    binding.edLoginEmail.error = "Masukkan email"
                }
                password.isEmpty() -> {
                    binding.myEditText.error = "Masukkan password"
                }
                email != user.email -> {
                    binding.edLoginEmail.error = "Email tidak sesuai"
                }
                password != user.password -> {
                    binding.myEditText.error = "Password tidak sesuai"
                }
                else -> {
                    viewModel.doLogin(email, password)
                    viewModel.login()
                    viewModel.userLogin.observe(this){
                        val loginSession = LoginSession(this)
                        loginSession.saveToken(it.loginResult.token)
                        Log.d("LoginActivity", "token : ${loginSession.passToken().toString()}")
                        AlertDialog.Builder(this).apply {
                            setTitle("Yeah!")
                            setMessage("Anda berhasil login. Sudah siap membagikan cerita anda?")
                            setPositiveButton("Siap!") { _, _ ->
                                val intent = Intent(context, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            }
                            create()
                            show()
                        }
                    }
                    viewModel.isLoading.observe(this) { isLoading ->
                        showLoading(isLoading)
                    }

                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}