package com.renatsolocorp.dutyapp.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.renatsolocorp.dutyapp.R
import com.renatsolocorp.dutyapp.database.*
import com.renatsolocorp.dutyapp.extensions.showConnectionProblem
import kotlinx.android.synthetic.main.activity_login.*

lateinit var loginDb: FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    lateinit var imm: InputMethodManager

    lateinit var repository: LoginRepository

    lateinit var name: EditText
    lateinit var password: EditText
    lateinit var toSignupText: TextView
    lateinit var loginButton: Button
    lateinit var loginLoadingScreen: ConstraintLayout

    var email = ""
    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginDb = FirebaseDatabase.getInstance()

        name = findViewById(R.id.editTextNameLogin)
        password = findViewById(R.id.editTextPasswordLogin)
        toSignupText = findViewById(R.id.login_to_signup_text)
        loginButton = findViewById(R.id.login_button)
        loginLoadingScreen = login_loading_screen
        loginLoadingScreen.setOnClickListener{  }

        loginButton.setOnClickListener { loginUser() }

        toSignupText.setOnClickListener {
            finish()
            val intent = Intent(this, SignUpActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
        }

        imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        repository = LoginRepository(this, application)
        repository.clearAllClassesEntirely()

        if (mAuth.currentUser != null) {
            loginDb.getReference(USERS).get().addOnCompleteListener { utask ->
                if (utask.isSuccessful && utask.result != null){
                    val result = utask.result!!
                    loginLoadingScreen.visibility = View.VISIBLE
                    imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                    repository.updateUserDataAndFinish(result)
                } else {
                    showConnectionProblem(this)
                }
            }
        }
    }

    private fun loginUser(){
        if (checkForErrors()){
            loginLoadingScreen.visibility = View.VISIBLE
            if (Patterns.EMAIL_ADDRESS.matcher(name.text.toString()).matches() && name.text.isNotEmpty()){
                email = name.text.toString()
                name.error = null

                loginDb.getReference(USERS).get().addOnCompleteListener { utask ->
                    if (utask.isSuccessful && utask.result != null){
                        val result = utask.result!!
                        mAuth.signInWithEmailAndPassword(email, password.text.toString()).addOnCompleteListener {
                            if (it.isSuccessful){
                                Toast.makeText(this, getString(R.string.login_is_successful), Toast.LENGTH_SHORT).show()
                                imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                                repository.updateUserDataAndFinish(result)
                            } else {
                                Toast.makeText(this, getString(R.string.login_is_failed), Toast.LENGTH_LONG).show()
                                loginLoadingScreen.visibility = View.GONE
                            }
                        }
                    } else {
                        showConnectionProblem(this)
                    }
                }
            } else if (!Patterns.EMAIL_ADDRESS.matcher(name.text.toString()).matches() && name.text.isNotEmpty()){
                var taskDone = false
                loginDb.getReference(USERS).get().addOnCompleteListener { utask ->
                    if (utask.isSuccessful && utask.result != null){
                        val result = utask.result!!
                        result.children.forEach {
                            if (it.child(USER_INFO).child(USERNAME).value == name.text.toString()){
                                taskDone = true
                                email = it.child(USER_INFO).child(EMAIL).value as String
                                mAuth.signInWithEmailAndPassword(email, password.text.toString()).addOnCompleteListener { task ->
                                    if (task.isSuccessful){
                                        Toast.makeText(this, getString(R.string.login_is_successful), Toast.LENGTH_SHORT).show()
                                        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                                        repository.updateUserDataAndFinish(result)
                                    } else {
                                        Toast.makeText(this, getString(R.string.login_is_failed), Toast.LENGTH_LONG).show()
                                        loginLoadingScreen.visibility = View.GONE
                                    }
                                }
                            }
                        }
                        if (!taskDone) name.error = getString(R.string.username_is_not_registered)
                    } else {
                        loginLoadingScreen.visibility = View.GONE
                        showConnectionProblem(this)
                    }
                }
            }
        } else {
            loginLoadingScreen.visibility = View.GONE
        }
    }

    private fun checkForErrors(): Boolean{
        var noErrors = true

        if (name.text.isEmpty()){
            name.error = getString(R.string.field_not_be_empty)
            name.requestFocus()
            noErrors = false
        }

        if (password.text.isEmpty()){
            password.error = getString(R.string.password_empty)
        } else if (password.text.length < 8) {
            password.error = getString(R.string.password_is_short)
        } else password.error = null

        if (password.error != null) noErrors = false

        return noErrors
    }
}