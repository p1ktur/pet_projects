package com.renatsolocorp.dutyapp.profile

import android.app.Application
import android.app.DatePickerDialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.renatsolocorp.dutyapp.R
import com.renatsolocorp.dutyapp.database.*
import com.renatsolocorp.dutyapp.database.classdb.ClassRepository
import com.renatsolocorp.dutyapp.extensions.calculateBitmap
import com.renatsolocorp.dutyapp.extensions.rotateBitmap
import com.renatsolocorp.dutyapp.extensions.translateMonth
import com.renatsolocorp.dutyapp.login.LoginActivity
import com.renatsolocorp.dutyapp.login.loggingIn
import com.renatsolocorp.dutyapp.main.*
import kotlinx.android.synthetic.main.fragment_profile_settings.*
import kotlinx.android.synthetic.main.grade_picker_layout.view.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

var language = ""
var appTheme = 0

class ProfileSettingsFragment(val application: Application) : Fragment() {
    val currentUser = FirebaseAuth.getInstance().currentUser!!
    val storage = FirebaseStorage.getInstance()
    val classRepository = ClassRepository(application)

    var image: Uri? = null
    var bitmap: Bitmap? = null
    lateinit var userProfileImage: ImageView

    lateinit var chooseProfileImageButton: ImageButton
    lateinit var userUsernameEditText: EditText
    lateinit var userBirthdayText: TextView
    lateinit var userGradeText: TextView
    lateinit var userBioEditText: EditText
    lateinit var userEmailEditText: TextView
    lateinit var userMobileEditText: EditText
    lateinit var saveChangesButton: Button

    lateinit var birthdayCheckBox: CheckBox
    lateinit var gradeCheckBox: CheckBox
    lateinit var bioCheckBox: CheckBox
    lateinit var emailCheckBox: CheckBox
    lateinit var mobileCheckBox: CheckBox

    lateinit var languageSpinner: Spinner
    lateinit var themeSpinner: Spinner

    var languageInitFinished = false
    var themeInitFinished = false

    lateinit var profileSettingsLoadingScreen: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainTextField.text = getString(R.string.editing)
        mainRefreshLayout.isEnabled = false

        userProfileImage = user_profile_image
        userProfileImage.clipToOutline = true

        chooseProfileImageButton = choose_profile_image_button
        userUsernameEditText = user_username_edit_text
        userBirthdayText = user_settings_birthday_text
        userGradeText = user_settings_grade_text
        userBioEditText = user_bio_edit_text
        userEmailEditText = user_email_edit_text
        userMobileEditText = user_mobile_edit_text

        saveChangesButton = save_changes_button

        birthdayCheckBox = birthday_checkbox
        gradeCheckBox = grade_checkbox
        bioCheckBox = bio_checkbox
        emailCheckBox = email_checkbox
        mobileCheckBox = mobile_checkbox

        languageSpinner = language_spinner
        themeSpinner = theme_spinner

        profileSettingsLoadingScreen = profile_settings_loading_screen
        profileSettingsLoadingScreen.setOnClickListener {  }

        initViews()
        addListeners()

        profileSettingsLoadingScreen.visibility = View.GONE
    }

    private fun initViews() {
        language = globalProfilePreferences.customGetData(LANGUAGE)
        appTheme = globalProfilePreferences.customGetData(APP_THEME).toInt()

        val path = globalProfilePreferences.customGetData(PROFILE_IMAGE_LOCATION + currentUser.uid)
        if (path != "") {
            val uri = Uri.parse(path)
            userProfileImage.setImageURI(uri)
        }

        userUsernameEditText.setText(currentUser.displayName)

        globalProfilePreferences.getProfileSettingsData(
            birthdayCheckBox,
            gradeCheckBox,
            bioCheckBox,
            emailCheckBox,
            mobileCheckBox,
            userUsernameEditText,
            userBirthdayText,
            userGradeText,
            userBioEditText,
            userEmailEditText,
            userMobileEditText
        )

        if (userGradeText.text.toString() == "null") userGradeText.text = ""
        //TODO animations
        //TODO pairs duty text make expand
        //TODO make app icon

        languageSpinner.adapter = SettingsSpinnerAdapter(context!!, resources.getStringArray(R.array.languages).toMutableList())
        languageSpinner.setSelection(
            when (language) {
                ProfilePreferences.ENGLISH -> 0
                ProfilePreferences.UKRAINIAN -> 1
                ProfilePreferences.RUSSIAN -> 2
                else -> 0
            }
        )
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> language = ProfilePreferences.ENGLISH
                    1 -> language = ProfilePreferences.UKRAINIAN
                    2 -> language = ProfilePreferences.RUSSIAN
                }
                if (languageInitFinished) Toast.makeText(context!!, getString(R.string.changes_applied_end_editing), Toast.LENGTH_SHORT).show()
                languageInitFinished = true
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        themeSpinner.adapter = SettingsSpinnerAdapter(context!!, resources.getStringArray(R.array.themes).toMutableList())
        themeSpinner.setSelection(
            when (appTheme) {
                AppCompatDelegate.MODE_NIGHT_YES -> 0
                AppCompatDelegate.MODE_NIGHT_NO -> 1
                else -> 1
            }
        )

        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> appTheme = AppCompatDelegate.MODE_NIGHT_YES
                    1 -> appTheme = AppCompatDelegate.MODE_NIGHT_NO
                }
                if (themeInitFinished) Toast.makeText(context!!, getString(R.string.changes_applied_end_editing), Toast.LENGTH_SHORT).show()
                themeInitFinished = true
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun addListeners() {
        chooseProfileImageButton.setOnClickListener { selectImage() }

        userBirthdayText.isFocusable = false
        userBirthdayText.isClickable = true
        userBirthdayText.inputType = InputType.TYPE_NULL
        userBirthdayText.setOnClickListener { chooseBirthdayDate() }

        userGradeText.isFocusable = false
        userGradeText.isClickable = true
        userGradeText.inputType = InputType.TYPE_NULL
        userGradeText.setOnClickListener { chooseGrade() }

        userBioEditText.addTextChangedListener {
            if (userBioEditText.lineCount > 6 && it.toString().last().isWhitespace()) {
                userBioEditText.setText(it.toString().dropLast(1))
                userBioEditText.setSelection(userBioEditText.text.toString().length)
            }
        }

        saveChangesButton.setOnClickListener {
            profileSettingsLoadingScreen.visibility = View.VISIBLE

            db.getReference(USERS).get().addOnCompleteListener { utask ->
                if (utask.isSuccessful && utask.result != null){
                    val result = utask.result!!

                    val username = userUsernameEditText.text.toString()
                    val profileCR = UserProfileChangeRequest.Builder().setDisplayName(username)
                    if (image != null && bitmap != null) {
                        profileCR.photoUri = image
                        headerImage.setImageBitmap(bitmap)

                        val wrapper = ContextWrapper(context)
                        var file = wrapper.getDir("images", Context.MODE_PRIVATE)
                        file = File(file, "${currentUser.uid}.jpg")
                        val stream: OutputStream = FileOutputStream(file)
                        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                        stream.apply { flush(); close() }

                        val reference = storage.getReference("$PROFILE_IMAGES/${currentUser.uid}.jpg")
                        reference.putFile(image!!)

                        globalProfilePreferences.customSaveData(file.absolutePath, PROFILE_IMAGE_LOCATION + currentUser.uid)

                        db.getReference(USERS).child(currentUser.uid).child(USER_INFO).child(PROFILE_IMAGE_LOCATION).setValue(reference.path)
                    }

                    currentUser.updateProfile(profileCR.build())

                    globalProfilePreferences.saveProfileData(
                        birthdayCheckBox,
                        gradeCheckBox,
                        bioCheckBox,
                        emailCheckBox,
                        mobileCheckBox,
                        userUsernameEditText.text.toString(),
                        userBirthdayText,
                        userGradeText,
                        userBioEditText,
                        userEmailEditText,
                        userMobileEditText,
                    )

                    classRepository.updateAllClasses(
                        userUsernameEditText.text.toString(),
                        userGradeText.text.toString(),
                        gradeCheckBox.isChecked
                    )

                    result.child(currentUser.uid).child(USER_INFO).child(BIRTHDAY_SHOW).ref.setValue(
                        birthdayCheckBox.isChecked
                    )
                    result.child(currentUser.uid).child(USER_INFO).child(GRADE_SHOW).ref.setValue(
                        gradeCheckBox.isChecked
                    )
                    result.child(currentUser.uid).child(USER_INFO).child(BIO_SHOW).ref.setValue(
                        bioCheckBox.isChecked
                    )
                    result.child(currentUser.uid).child(USER_INFO).child(EMAIL_SHOW).ref.setValue(
                        emailCheckBox.isChecked
                    )
                    result.child(currentUser.uid).child(USER_INFO).child(MOBILE_SHOW).ref.setValue(
                        mobileCheckBox.isChecked
                    )

                    result.child(currentUser.uid).child(USER_INFO).child(BIRTHDAY).ref.setValue(
                        userBirthdayText.text.toString()
                    )
                    result.child(currentUser.uid).child(USER_INFO).child(GRADE).ref.setValue(
                        userGradeText.text.toString()
                    )
                    result.child(currentUser.uid).child(USER_INFO).child(BIO).ref.setValue(
                        userBioEditText.text.toString()
                    )
                    result.child(currentUser.uid).child(USER_INFO).child(EMAIL).ref.setValue(
                        userEmailEditText.text.toString()
                    )
                    result.child(currentUser.uid).child(USER_INFO).child(MOBILE).ref.setValue(
                        userMobileEditText.text.toString()
                    )

                    result.child(currentUser.uid).child(USER_INFO).child(USERNAME).ref.setValue(
                        userUsernameEditText.text.toString()
                    )
                    result.child(currentUser.uid).child(CLASSES).children.forEach { ds ->
                        if (ds.child(CLASS_INFO).child(CLASS_CREATOR_ID).value.toString() == currentUser.uid){
                            ds.child(CLASS_INFO).child(CLASS_CREATOR_NAME).ref.setValue(userUsernameEditText.text.toString())
                            ds.child(CLASS_INFO).child(CLASS_GRADE).ref.setValue(userGradeText.text.toString())
                            ds.child(CLASS_INFO).child(CLASS_GRADE_SHOW).ref.setValue(gradeCheckBox.isChecked.toString())
                        }
                    }

                    profileSettingsButton.visibility = View.VISIBLE
                    profileSettingsMenuButton.visibility = View.GONE
                    mainDrawerButton.visibility = View.VISIBLE
                    mainBackButton.visibility = View.GONE
                    mainDrawer.closeDrawer(GravityCompat.START)

                    if (appTheme != globalProfilePreferences.customGetData(APP_THEME).toInt()){
                        if (language != globalProfilePreferences.customGetData(LANGUAGE)){
                            globalProfilePreferences.customSaveData(language, LANGUAGE)
                        }
                        activity!!.finish()
                        globalProfilePreferences.customSaveData(appTheme.toString(), APP_THEME)
                        AppCompatDelegate.setDefaultNightMode(appTheme)
                        activity!!.startActivity(activity!!.intent)
                    } else if (language != globalProfilePreferences.customGetData(LANGUAGE)){
                        globalProfilePreferences.customSaveData(language, LANGUAGE)
                        activity!!.finish()
                        activity!!.startActivity(activity!!.intent)
                    } else fragmentManager!!.beginTransaction().replace(R.id.main_fragment_container, ProfileFragment(currentUser.uid, application)).commit()
                } else {
                    profileSettingsLoadingScreen.visibility = View.GONE
                    Toast.makeText(context!!, getString(R.string.failed_save_on_cloud_connection), Toast.LENGTH_SHORT).show()
                }
            }
        }

        profileSettingsMenuButton.setOnClickListener {
            val context = context!!
            val popupMenu = PopupMenu(context, profileSettingsMenuButton)
            popupMenu.menuInflater.inflate(R.menu.profile_settings_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId){
                    R.id.log_out_item -> {
                        loggingIn = true
                        FirebaseAuth.getInstance().signOut()
                        activity?.finish()
                        startActivity(Intent(context, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        })
                    }
                    R.id.delete_account_item -> {
                        val builder = AlertDialog.Builder(context)
                        val message = SpannableString(getString(R.string.sure_delete_account))
                        message.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.textColor)), 0, message.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        builder.setMessage(message)
                        builder.setTitle(getString(R.string.delete_account))
                        builder.setPositiveButton(getString(R.string.ok)) { _: DialogInterface, _: Int ->
                            val userUid = currentUser.uid
                            db.getReference(USERS).get().addOnCompleteListener { utask ->
                                if (utask.isSuccessful && utask.result != null) {
                                    val result = utask.result!!
                                    result.child(userUid).child(CLASSES).children.forEach { ds ->
                                        if (ds.child(CLASS_INFO).child(CLASS_PINNED_LIST).childrenCount != 0L) {
                                            ds.child(CLASS_INFO).child(CLASS_PINNED_LIST).children.forEach { child ->
                                                if (child.key.toString() != userUid) {
                                                    result.child(child.value.toString()).child(USER_INFO).child(PINNED_CLASSES_LIST).child(ds.key.toString()).ref.removeValue()
                                                }
                                            }
                                        }
                                    }
                                    if (result.child(userUid).child(USER_INFO).child(FOLLOWING_LIST).childrenCount != 0L) {
                                        result.child(userUid).child(USER_INFO).child(FOLLOWING_LIST).children.forEach { child ->
                                            result.child(child.value.toString()).child(USER_INFO).child(FOLLOWERS_LIST).child(userUid).ref.removeValue()
                                            result.child(child.value.toString()).child(USER_INFO).child(FOLLOWERS).ref.setValue(
                                                (result.child(child.value.toString()).child(USER_INFO).child(FOLLOWERS).value.toString().toInt() - 1).toString()
                                            )
                                        }
                                    }
                                    if (result.child(userUid).child(FOLLOWERS_LIST).childrenCount != 0L) {
                                        result.child(userUid).child(USER_INFO).child(FOLLOWERS_LIST).children.forEach { child ->
                                            result.child(child.value.toString()).child(USER_INFO).child(FOLLOWING_LIST).child(userUid).ref.removeValue()
                                            result.child(child.value.toString()).child(USER_INFO).child(FOLLOWING).ref.setValue(
                                                (result.child(child.value.toString()).child(USER_INFO).child(FOLLOWING).value.toString().toInt() - 1).toString()
                                            )
                                        }
                                    }

                                    result.child(userUid).ref.removeValue()
                                    currentUser.delete().addOnSuccessListener {
                                        activity?.finish()
                                        startActivity(Intent(context, LoginActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP })
                                    }
                                } else {
                                    Toast.makeText(context, getString(R.string.failed_to_delete_account), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        builder.setNegativeButton(getString(R.string.cancel)) { _: DialogInterface, _: Int -> }
                        builder.setCancelable(true)
                        val dialog = builder.create()
                        dialog.window!!.setBackgroundDrawable(
                            AppCompatResources.getDrawable(
                                context,
                                R.drawable.action_bg
                            )
                        )
                        dialog.show()
                    }
                }
                true
            }
            popupMenu.show()
        }
    }

    private fun chooseBirthdayDate(){
        val clearedText = userBirthdayText.text.toString().replace(",", "")
        var year = 2000
        var month = 0
        var day = 1

        if (clearedText != "Your Birthday" && clearedText.isNotEmpty()) {
            day = clearedText.split(" ")[0].toInt()
            month = unFormatMonth(clearedText.split(" ")[1])
            year = clearedText.split(" ")[2].toInt()
        }

        val datePickerDialog = DatePickerDialog(activity!!, R.style.DialogTheme, { view, year, month, dayOfMonth ->
            userBirthdayText.text = "$dayOfMonth ${translateMonth(formatMonth(month), context!!)}, $year"
        }, year, month, day)

        //TODO fix the months bug
        //datePickerDialog.setTitle(getString(R.string.pick_birthday))
        datePickerDialog.show()
    }

    private fun chooseGrade(){
        val builder = AlertDialog.Builder(context!!)

        val dView = LayoutInflater.from(context!!).inflate(R.layout.grade_picker_layout, null)
        builder.setView(dView)
        builder.setCancelable(true)
        val dialog = builder.create()
        dialog.show()

        val numberPicker = dView.grade_number_picker
        val letterPicker = dView.grade_letter_picker
        val confirmButton = dView.grade_confirm_button
        val cancelButton = dView.grade_cancel_button

        initGradePickers(numberPicker, letterPicker)
        if (userGradeText.text != ""){
            textToGrade(userGradeText.text.toString())
        }

        confirmButton.setOnClickListener {
            userGradeText.text = gradeToText(numberPicker, letterPicker)
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun selectImage(){
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_profile_image)), PROFILE_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PROFILE_IMAGE_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK && data != null && data.data != null){
            image = data.data!!
            bitmap = MediaStore.Images.Media.getBitmap(context!!.contentResolver, image!!)
            bitmap = rotateBitmap(bitmap!!, 90f)
            bitmap = calculateBitmap(bitmap!!, bitmap!!.width, bitmap!!.height)
            Glide.with(context!!).load(image).centerCrop().transition(DrawableTransitionOptions.withCrossFade()).addListener(object : RequestListener<Drawable>{
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    bitmap = resource?.toBitmap()
                    return false
                }

            }).into(userProfileImage)
        }
    }

    private fun formatMonth(month: Int): String{
        return when (month) {
            0 -> "Jan"
            1 -> "Feb"
            2 -> "Mar"
            3 -> "Apr"
            4 -> "May"
            5 -> "Jun"
            6 -> "Jul"
            7 -> "Aug"
            8 -> "Sep"
            9 -> "Oct"
            10 -> "Nov"
            11 -> "Dec"
            else -> "Jan"
        }
    }

    private fun unFormatMonth(month: String): Int{
        return when (month) {
            "Jan" -> 0
            "Feb" -> 1
            "Mar" -> 2
            "Apr" -> 3
            "May" -> 4
            "Jun" -> 5
            "Jul" -> 6
            "Aug" -> 7
            "Sep" -> 8
            "Oct" -> 9
            "Nov" -> 10
            "Dec" -> 11
            else -> 0
        }
    }

}