package com.renatsolocorp.dutyapp.profile

import android.content.Context
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import com.renatsolocorp.dutyapp.database.*

class ProfilePreferences(context: Context) {
    companion object {
        const val ENGLISH = "en"
        const val UKRAINIAN = "ua"
        const val RUSSIAN = "ru"
    }

    private val PREFERENCES = "preferences"
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun clear(){
        preferences.edit().clear().apply()
    }

    fun saveProfileDataOnSignIn(
        username: String = "",
        birthday: String = "",
        grade: String = "",
        bio: String = "",
        email: String = "",
        mobile: String = "",
        ownClasses: String = "0",
        following: String = "0",
        followers: String = "0",
        birthdayShow: Boolean = true,
        gradeShow: Boolean = true,
        bioShow: Boolean = true,
        emailShow: Boolean = true,
        mobileShow: Boolean = true
    ){
        val editedPreferences = preferences.edit()

        editedPreferences.putString(USERNAME, username)
        editedPreferences.putString(BIRTHDAY, birthday)
        editedPreferences.putString(GRADE, grade)
        editedPreferences.putString(BIO, bio)
        editedPreferences.putString(EMAIL, email)
        editedPreferences.putString(MOBILE, mobile)
        editedPreferences.putString(OWN_CLASSES, ownClasses)
        editedPreferences.putString(FOLLOWING, following)
        editedPreferences.putString(FOLLOWERS, followers)

        editedPreferences.putBoolean(BIRTHDAY_SHOW, birthdayShow)
        editedPreferences.putBoolean(GRADE_SHOW, gradeShow)
        editedPreferences.putBoolean(BIO_SHOW, bioShow)
        editedPreferences.putBoolean(EMAIL_SHOW, emailShow)
        editedPreferences.putBoolean(MOBILE_SHOW, mobileShow)

        editedPreferences.apply()
    }

    fun saveProfileData(
        birthdayCheckBox: CheckBox,
        gradeCheckBox: CheckBox,
        bioCheckBox: CheckBox,
        emailCheckBox: CheckBox,
        mobileCheckBox: CheckBox,
        username: String,
        userBirthdayText: TextView,
        userGradeText: TextView,
        userBioText: EditText,
        userEmailText: TextView,
        userMobileText: EditText,
    ){
        val editedPreferences = preferences.edit()
        editedPreferences.putBoolean(BIRTHDAY_SHOW, birthdayCheckBox.isChecked)
        editedPreferences.putBoolean(GRADE_SHOW, gradeCheckBox.isChecked)
        editedPreferences.putBoolean(BIO_SHOW, bioCheckBox.isChecked)
        editedPreferences.putBoolean(EMAIL_SHOW, emailCheckBox.isChecked)
        editedPreferences.putBoolean(MOBILE_SHOW, mobileCheckBox.isChecked)

        editedPreferences.putString(USERNAME, username)
        editedPreferences.putString(BIRTHDAY, userBirthdayText.text.toString())
        editedPreferences.putString(GRADE, userGradeText.text.toString())
        editedPreferences.putString(BIO, userBioText.text.toString())
        editedPreferences.putString(EMAIL, userEmailText.text.toString())
        editedPreferences.putString(MOBILE, userMobileText.text.toString())

        editedPreferences.apply()
    }

    fun getProfileSettingsData(
        birthdayCheckBox: CheckBox,
        gradeCheckBox: CheckBox,
        bioCheckBox: CheckBox,
        emailCheckBox: CheckBox,
        mobileCheckBox: CheckBox,
        userUsernameEditText: EditText,
        userBirthdayText: TextView,
        userGradeText: TextView,
        userBioEditText: EditText,
        userEmailEditText: TextView,
        userMobileEditText: EditText
    ){
        birthdayCheckBox.isChecked = preferences.getBoolean(BIRTHDAY_SHOW, true)
        gradeCheckBox.isChecked = preferences.getBoolean(GRADE_SHOW, true)
        bioCheckBox.isChecked = preferences.getBoolean(BIO_SHOW, true)
        emailCheckBox.isChecked = preferences.getBoolean(EMAIL_SHOW, true)
        mobileCheckBox.isChecked = preferences.getBoolean(MOBILE_SHOW, true)

        userUsernameEditText.setText(preferences.getString(USERNAME, ""))
        userBirthdayText.text = preferences.getString(BIRTHDAY, "")
        userGradeText.text = preferences.getString(GRADE, "1-А")
        userBioEditText.setText(preferences.getString(BIO, ""))
        userEmailEditText.text = preferences.getString(EMAIL, "")
        userMobileEditText.setText(preferences.getString(MOBILE, ""))

        if (userBirthdayText.text == "null") userBirthdayText.text = ""
        if (userBioEditText.text.toString() == "null") userBioEditText.setText("")
        if (userEmailEditText.text == "null") userEmailEditText.text = ""
        if (userMobileEditText.text.toString() == "null") userMobileEditText.setText("")
    }

    fun getProfileData(
        userUsernameText: TextView,
        userBirthdayText: TextView,
        userGradeText: TextView,
        userBioText: TextView,
        userEmailText: TextView,
        userMobileText: TextView,
        userOwnClassesText: TextView,
        userFollowingText: TextView,
        userFollowersText: TextView
    ){
        userBirthdayText.visibility = if (preferences.getBoolean(BIRTHDAY_SHOW, true)) View.VISIBLE else View.GONE
        userGradeText.visibility = if (preferences.getBoolean(GRADE_SHOW, true)) View.VISIBLE else View.GONE
        userBioText.visibility = if (preferences.getBoolean(BIO_SHOW, true)) View.VISIBLE else View.GONE
        userEmailText.visibility = if (preferences.getBoolean(EMAIL_SHOW, true)) View.VISIBLE else View.GONE
        userMobileText.visibility = if (preferences.getBoolean(MOBILE_SHOW, true)) View.VISIBLE else View.GONE

        userUsernameText.text = preferences.getString(USERNAME, "Your Username")
        userBirthdayText.text = preferences.getString(BIRTHDAY, "")
        if (userBirthdayText.text == "null") userBirthdayText.text = ""
        userGradeText.text = preferences.getString(GRADE, "")
        if (userGradeText.text == "null") userGradeText.text = ""
        userBioText.text = preferences.getString(BIO, "")
        if (userBioText.text == "null") userBioText.text = ""
        userEmailText.text = preferences.getString(EMAIL, "")
        userMobileText.text = preferences.getString(MOBILE, "")
        if (userMobileText.text == "null") userMobileText.text = ""
        userOwnClassesText.text = preferences.getString(OWN_CLASSES, "0")
        userFollowingText.text = preferences.getString(FOLLOWING, "0")
        userFollowersText.text = preferences.getString(FOLLOWERS, "0")
    }

    fun customSaveData(value: String, code: String){
        val editedPreferences = preferences.edit()
        editedPreferences.putString(code, value)
        editedPreferences.apply()
    }

    fun customGetData(code: String): String {
        return preferences.getString(code, "")!!
    }

    fun customGetBooleanData(code: String): Boolean{
        return preferences.getBoolean(code, false)
    }
}