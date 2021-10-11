package com.renatsolocorp.dutyapp.login

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.renatsolocorp.dutyapp.R
import com.renatsolocorp.dutyapp.database.*
import com.renatsolocorp.dutyapp.extensions.showConnectionProblem
import com.renatsolocorp.dutyapp.main.MainActivity
import com.renatsolocorp.dutyapp.profile.ProfilePreferences
import com.renatsolocorp.dutyapp.profile.globalProfilePreferences
import kotlinx.android.synthetic.main.activity_sign_up.*

lateinit var signUpDb: FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    lateinit var name: EditText
    lateinit var email: EditText
    lateinit var password: EditText
    lateinit var passwordRepeated: EditText
    lateinit var toLoginText: TextView
    lateinit var signupButton: Button
    lateinit var signupLoadingScreen: ConstraintLayout

    val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        signUpDb = FirebaseDatabase.getInstance()

        name = editTextNameSignup
        email = editTextEmailSignup
        password = editTextPasswordSignup
        passwordRepeated = editTextPasswordSignupRepeat
        toLoginText = signup_to_login_text
        signupButton = signup_button
        signupLoadingScreen = sign_up_loading_screen
        signupLoadingScreen.setOnClickListener{  }


        password.addTextChangedListener {
            val text = it.toString()
            if (text.isEmpty()){
                passwordRepeated.isEnabled = false
                password.error = getString(R.string.password_empty)
            } else {
                password.error = null
                passwordRepeated.isEnabled = true
                if (text != password.text.toString()) {
                    passwordRepeated.error = getString(R.string.password_confirmed_badly)
                } else {
                    passwordRepeated.error = null
                }
            }
        }

        passwordRepeated.addTextChangedListener {
            val text = it.toString()
            if (text != password.text.toString()) {
                passwordRepeated.error = getString(R.string.password_confirmed_badly)
            } else {
                passwordRepeated.error = null
            }
        }

        signupButton.setOnClickListener { registerUser(this) }

        toLoginText.setOnClickListener {
            finish()
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
        }
    }

    private fun registerUser(context: Context){
        if (checkForErrors()){
            signUpDb.getReference(USERS).get().addOnCompleteListener { utask ->
                if (utask.isSuccessful && utask.result != null){
                    val result = utask.result!!
                    mAuth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString()).addOnCompleteListener {
                        if (it.isSuccessful){
                            signupLoadingScreen.visibility = View.VISIBLE
                            Toast.makeText(this, getString(R.string.registration_is_succesful), Toast.LENGTH_SHORT).show()
                            mAuth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                            result.child(mAuth.currentUser!!.uid).child(USER_INFO).child(EMAIL).ref.setValue(email.text.toString())
                            result.child(mAuth.currentUser!!.uid).child(USER_INFO).child(USERNAME).ref.setValue(name.text.toString())
                            result.child(mAuth.currentUser!!.uid).child(USER_INFO).child(OWN_CLASSES).ref.setValue("0")
                            result.child(mAuth.currentUser!!.uid).child(USER_INFO).child(FOLLOWING).ref.setValue("0")
                            result.child(mAuth.currentUser!!.uid).child(USER_INFO).child(FOLLOWERS).ref.setValue("0")

                            ProfilePreferences(context).saveProfileDataOnSignIn(
                                username = name.text.toString(),
                                email = email.text.toString()
                            )

                            val preferences = ProfilePreferences(context)
                            if (preferences.customGetData(APP_THEME) == "") preferences.customSaveData("1", APP_THEME)
                            if (preferences.customGetData(LANGUAGE) == "") preferences.customSaveData("en", LANGUAGE)

                            val currentUser = mAuth.currentUser!!
                            val profileCR = UserProfileChangeRequest.Builder().setDisplayName(name.text.toString()).build()
                            currentUser.updateProfile(profileCR)

                            finish()
                            val intent = Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, getString(R.string.failed_registration), Toast.LENGTH_LONG).show()
                            signupLoadingScreen.visibility = View.GONE
                        }
                    }
                } else {
                    signupLoadingScreen.visibility = View.GONE
                    showConnectionProblem(this)
                }
            }
        } else {
            signupLoadingScreen.visibility = View.GONE
        }
    }

    private fun checkForErrors(): Boolean{
        var noErrors = true

        if (!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()){
            email.error = getString(R.string.field_must_be_email)
            email.requestFocus()
            noErrors = false
        } else if (email.text.isEmpty()){
            email.error = getString(R.string.field_not_be_empty)
            email.requestFocus()
            noErrors = false
        } else {
            signUpDb.getReference(USERS).get().addOnCompleteListener { task1 ->
                if (task1.isSuccessful && task1.result!!.value != null){
                    task1.result!!.children.forEach {
                        if (email.text == it.child(USER_INFO).child(EMAIL).value){
                            email.error = getString(R.string.email_is_taken)
                            email.requestFocus()
                            noErrors = false
                        } else {
                            name.error = null
                            email.error = null
                            noErrors = true
                        }
                    }
                }
            }
        }

        if (name.text.isNotEmpty()){
            if (email.error == null) {
                signUpDb.getReference(USERS).get().addOnCompleteListener { task1 ->
                    if (task1.isSuccessful && task1.result!!.value != null){
                        task1.result!!.children.forEach {
                            if (name.text == it.child(USER_INFO).child(USERNAME).value){
                                name.error = getString(R.string.username_is_taken)
                                name.requestFocus()
                                noErrors = false
                            } else {
                                name.error = null
                                email.error = null
                                noErrors = true
                            }
                        }
                    }
                }
            }
        } else {
            name.error = getString(R.string.field_not_be_empty)
            name.requestFocus()
            noErrors = false
        }

        if (password.text.length < 8) {
            password.error = getString(R.string.password_is_short)
        } else password.error = null

        if (password.error != null) noErrors = false
        if (passwordRepeated.error != null) noErrors = false

        return noErrors
    }
}