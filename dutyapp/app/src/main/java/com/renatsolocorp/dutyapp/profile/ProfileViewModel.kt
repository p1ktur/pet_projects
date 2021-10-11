    package com.renatsolocorp.dutyapp.profile

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.renatsolocorp.dutyapp.R
import com.renatsolocorp.dutyapp.database.classdb.DutyClass
import com.renatsolocorp.dutyapp.database.*
import com.renatsolocorp.dutyapp.database.classdb.ClassRepository
import com.renatsolocorp.dutyapp.database.eventdb.DutyEvent
import com.renatsolocorp.dutyapp.database.eventdb.EventRepository
import com.renatsolocorp.dutyapp.database.pairdb.DutyPair
import com.renatsolocorp.dutyapp.database.pairdb.PairRepository
import com.renatsolocorp.dutyapp.extensions.calculateBitmap
import com.renatsolocorp.dutyapp.extensions.getCurrentTime
import com.renatsolocorp.dutyapp.extensions.showConnectionProblem
import com.renatsolocorp.dutyapp.extensions.unFormatTime
import com.renatsolocorp.dutyapp.login.loggingIn
import com.renatsolocorp.dutyapp.main.db
import com.renatsolocorp.dutyapp.main.headerImage
import com.renatsolocorp.dutyapp.main.mainRefreshLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

    class ProfileViewModel: ViewModel() {
    lateinit var repository: ProfileRepository
    lateinit var classRepository: ClassRepository
    lateinit var pairRepository: PairRepository
    lateinit var eventRepository: EventRepository

    lateinit var userInfoData: MutableLiveData<MutableList<String>>
    var userInfo =  mutableListOf<String>()

    lateinit var usersClassesList: MutableLiveData<MutableList<DutyClass>>
    var usersClasses = mutableListOf<DutyClass>()

    lateinit var pinnedClassesList: MutableLiveData<MutableList<DutyClass>>
    lateinit var localPinnedClassesList: LiveData<MutableList<DutyClass>>
    var pinnedClasses = mutableListOf<DutyClass>()

    val currentUser = FirebaseAuth.getInstance().currentUser!!

    fun init(viewedUserId: String, context: Context, loadingScreen: ConstraintLayout, application: Application, userImage: ImageView, activity: Activity){
        repository = ProfileRepository(viewedUserId, context, application)
        userInfoData = MutableLiveData()
        usersClassesList = MutableLiveData()

        getUserData(viewedUserId)

        val storage = FirebaseStorage.getInstance()
        val reference = storage.reference.child("$PROFILE_IMAGES/${viewedUserId}.jpg")
        reference.downloadUrl.addOnSuccessListener {
            Glide.with(context).load(it).centerCrop().transition(DrawableTransitionOptions.withCrossFade(200)).into(userImage)
        }

        if (!appIsOffline){
            db.getReference(USERS).get().addOnCompleteListener { utask ->
                if (utask.isSuccessful && utask.result != null) {
                    val result = utask.result!!
                    result.child(viewedUserId).child(CLASSES).children.forEach { ds ->
                        usersClasses.add(
                            DutyClass(
                                name = ds.child(CLASS_INFO).child(CLASS_NAME).value.toString(),
                                dutyAmount = ds.child(CLASS_INFO).child(CLASS_DUTY_AMOUNT).value.toString(),
                                creatorName = ds.child(CLASS_INFO).child(CLASS_CREATOR_NAME).value.toString(),
                                id = ds.child(CLASS_INFO).child(CLASS_ID).value.toString(),
                                creatorId = ds.child(CLASS_INFO).child(CLASS_CREATOR_ID).value.toString(),
                                grade = ds.child(CLASS_INFO).child(CLASS_GRADE).value.toString(),
                                show = ds.child(CLASS_INFO).child(CLASS_SHOW).value.toString().toBoolean(),
                                gradeShow = ds.child(CLASS_INFO).child(CLASS_GRADE_SHOW).value.toString().toBoolean(),
                                isPinnedByCurrentUser = ds.child(CLASS_INFO).child(CLASS_PINNED_LIST).child(currentUser.uid).exists()
                            )
                        )
                    }
                    usersClasses = usersClasses.filter{ it.show }.toMutableList()
                    usersClassesList.value = usersClasses
                    loadingScreen.visibility = View.GONE
                } else {
                    showConnectionProblem(context)
                    activity.onBackPressed()
                }
            }
        } else {
            showConnectionProblem(context)
            activity.onBackPressed()
            appIsOffline = false
        }
        usersClassesList.value = usersClasses
    }

    fun initForCurrentUser(loadingScreen: ConstraintLayout, application: Application, context: Context, following: TextView, followers: TextView){
        pinnedClassesList = MutableLiveData()
        classRepository = ClassRepository(application)
        pairRepository = PairRepository(application)
        eventRepository = EventRepository(application)
        localPinnedClassesList = classRepository.pinnedClasses
        pinnedClassesList.value = localPinnedClassesList.value ?: mutableListOf()

        if (loggingIn){
            //classRepository.clearAllClassesWhenLogin()
            getDataFromInternetOnLogin(context)
        } else {
            getPublicData(following, followers)
            classRepository.getPinnedClasses(context)

            loadingScreen.visibility = View.GONE
        }
    }

    fun clearUnpinnedClasses(list: MutableList<DutyClass>){
        pinnedClassesList.value = list.filter { it.isPinnedByCurrentUser }.toMutableList()
    }

    fun getPublicData(following: TextView, followers: TextView){
        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null) {
                val result = utask.result!!
                val infoSnapshot = result.child(currentUser.uid).child(USER_INFO)
                following.text = infoSnapshot.child(FOLLOWING).value.toString()
                followers.text = infoSnapshot.child(FOLLOWERS).value.toString()
            }
        }
    }

    private fun getUserData(viewedUserId: String){
        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null) {
                val result = utask.result!!
                val infoSnapshot = result.child(viewedUserId).child(USER_INFO)
                userInfo.add(infoSnapshot.child(USERNAME).value.toString())
                userInfo.add(infoSnapshot.child(BIRTHDAY).value.toString())
                userInfo.add(infoSnapshot.child(GRADE).value.toString())
                userInfo.add(infoSnapshot.child(BIO).value.toString())
                userInfo.add(infoSnapshot.child(EMAIL).value.toString())
                userInfo.add(infoSnapshot.child(MOBILE).value.toString())
                userInfo.add(infoSnapshot.child(OWN_CLASSES).value.toString())
                userInfo.add(infoSnapshot.child(FOLLOWING).value.toString())
                userInfo.add(infoSnapshot.child(FOLLOWERS).value.toString())

                userInfo.add(infoSnapshot.child(BIRTHDAY_SHOW).value.toString())
                userInfo.add(infoSnapshot.child(GRADE_SHOW).value.toString())
                userInfo.add(infoSnapshot.child(BIO_SHOW).value.toString())
                userInfo.add(infoSnapshot.child(EMAIL_SHOW).value.toString())
                userInfo.add(infoSnapshot.child(MOBILE_SHOW).value.toString())

                userInfoData.value = userInfo
            }
        }
    }

    fun updateData(viewedUserId: String, context: Context){
        getUserData(viewedUserId)

        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null) {
                val result = utask.result!!
                result.child(viewedUserId).child(CLASSES).children.forEach { ds ->
                    if (!usersClasses.map{ it.id }.contains(ds.child(CLASS_INFO).child(CLASS_ID).value.toString())) usersClasses.add(DutyClass(
                        name = ds.child(CLASS_INFO).child(CLASS_NAME).value.toString(),
                        dutyAmount = ds.child(CLASS_INFO).child(CLASS_DUTY_AMOUNT).value.toString(),
                        creatorName = ds.child(CLASS_INFO).child(CLASS_CREATOR_NAME).value.toString(),
                        id = ds.child(CLASS_INFO).child(CLASS_ID).value.toString(),
                        creatorId = ds.child(CLASS_INFO).child(CLASS_CREATOR_ID).value.toString(),
                        grade = ds.child(CLASS_INFO).child(CLASS_GRADE).value.toString(),
                        show = ds.child(CLASS_INFO).child(CLASS_SHOW).value.toString().toBoolean()
                    ))
                }
                usersClasses = usersClasses.filter{ it.show }.toMutableList()
                usersClassesList.value = usersClasses
            } else {
                showConnectionProblem(context)
            }
            mainRefreshLayout.isRefreshing = false
        }
    }

    fun updateDataForCurrentUser(context: Context, toCheckPairs: MutableList<DutyPair>? = null){
        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null) {
                val result = utask.result!!
                val toDelete = mutableListOf<Int>()
                pinnedClassesList.value!!.forEach { klass ->
                    if (klass.creatorId != currentUser.uid){
                        if (result.child(klass.creatorId).child(CLASSES).child(klass.id).exists()){
                            val path = result.child(klass.creatorId).child(CLASSES).child(klass.id).child(CLASS_INFO)
                            klass.apply {
                                name = path.child(CLASS_NAME).value.toString()
                                dutyAmount = path.child(CLASS_DUTY_AMOUNT).value.toString()
                                creatorName = path.child(CLASS_CREATOR_NAME).value.toString()
                                id = path.child(CLASS_ID).value.toString()
                                creatorId = path.child(CLASS_CREATOR_ID).value.toString()
                                grade = path.child(CLASS_GRADE).value.toString()
                                show = path.child(CLASS_SHOW).value.toString().toBoolean()
                                isPinnedByCurrentUser = true
                                pinnedTime = if (path.child(CLASS_PINNED_LIST).child(currentUser.uid).exists()) {
                                    path.child(CLASS_PINNED_LIST).child(currentUser.uid).value.toString().toLong()
                                } else {
                                    val currentTime = getCurrentTime().toLong()
                                    path.child(CLASS_PINNED_LIST).child(currentUser.uid).ref.setValue(currentTime.toString())
                                    currentTime
                                }
                            }

                            GlobalScope.launch {
                                result.child(klass.creatorId).child(CLASSES).child(klass.id).child(DUTY_LIST).children.forEach { pds ->
                                    val pair = DutyPair(
                                        name = pds.child(FULLNAME).value.toString(),
                                        debts = pds.child(DEBTS).value.toString().toInt(),
                                        id = pds.key.toString().toInt(),
                                        isCurrent = pds.child(IS_CURRENT).value.toString().toBoolean(),
                                        dutyTime = pds.child(DUTY_TIME).value.toString().toLong(),
                                        dutiesAmount = pds.child(DUTIES_AMOUNT).value.toString().toInt(),
                                        classId = path.child(CLASS_ID).value.toString()
                                    )

                                    if (toCheckPairs != null){
                                        if (!toCheckPairs.contains(pair)) {
                                            pairRepository.pairDao.addPair(pair)

                                            pds.child(EVENT_LIST).children.forEach { eds ->
                                                eventRepository.eventDao.addEvent(DutyEvent(
                                                    id = eds.key.toString().toInt(),
                                                    event = eds.child(EVENT_NAME).value.toString(),
                                                    date = eds.child(EVENT_TIME).value.toString(),
                                                    pairId = pds.key.toString().toInt(),
                                                    classId = path.child(CLASS_ID).value.toString()
                                                ))
                                            }
                                        }
                                    } else {
                                        val classes = classRepository.classDao.getPinnedClassesAsList()
                                        val pairs = mutableListOf<DutyPair>()

                                        classes.forEach {
                                            pairs.addAll(pairRepository.pairDao.getAllPairsAsList(it.id))
                                        }

                                        if (!pairs.contains(pair)){
                                            pairRepository.pairDao.addPair(pair)

                                            pds.child(EVENT_LIST).children.forEach { eds ->
                                                eventRepository.eventDao.addEvent(DutyEvent(
                                                    id = eds.key.toString().toInt(),
                                                    event = eds.child(EVENT_NAME).value.toString(),
                                                    date = eds.child(EVENT_TIME).value.toString(),
                                                    pairId = pds.key.toString().toInt(),
                                                    classId = path.child(CLASS_ID).value.toString()
                                                ))
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            toDelete.add(pinnedClassesList.value!!.indexOf(klass))
                        }
                    }
                }

                if (toDelete.isNotEmpty()){
                    toDelete.forEach {
                        classRepository.deleteClassEntirely(pinnedClassesList.value!![it].id)
                    }
                    
                    val newList = mutableListOf<DutyClass>()
                    pinnedClassesList.value!!.forEach {
                        if (!toDelete.contains(pinnedClassesList.value!!.indexOf(it))){
                            newList.add(it)
                        }
                    }

                    pinnedClassesList.value = newList.filter{ it.show || it.creatorId == currentUser.uid }.toMutableList()
                } else {
                    pinnedClassesList.value = pinnedClassesList.value!!.filter{ it.show || it.creatorId == currentUser.uid }.toMutableList()
                }
            } else {
                showConnectionProblem(context)
            }
            mainRefreshLayout.isRefreshing = false
        }
    }

    private fun getDataFromInternetOnLogin(context: Context){
        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null) {
                val result = utask.result!!

                val storage = FirebaseStorage.getInstance()
                val reference = storage.reference.child("$PROFILE_IMAGES/${currentUser.uid}.jpg")
                if (result.child(currentUser.uid).child(USER_INFO).child(PROFILE_IMAGE_LOCATION).exists()) reference.downloadUrl.addOnSuccessListener {
                    Glide.with(context).load(it).centerCrop().transition(DrawableTransitionOptions.withCrossFade()).addListener(object : RequestListener<Drawable>{
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            headerImage.setImageDrawable(resource)

                            val bitmap = resource!!.toBitmap()
                            val wrapper = ContextWrapper(context)
                            var file = wrapper.getDir("images", Context.MODE_PRIVATE)
                            file = File(file, "${currentUser.uid}.jpg")
                            val stream: OutputStream = FileOutputStream(file)
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                            stream.apply { flush(); close() }

                            globalProfilePreferences.customSaveData(file.absolutePath, PROFILE_IMAGE_LOCATION + currentUser.uid)

                            return false
                        }

                    }).into(userImage)
                }

                result.child(currentUser.uid).child(USER_INFO).child(PINNED_CLASSES_LIST).children.forEach { ds ->
                    val path = result.child(ds.value.toString()).child(CLASSES).child(ds.key.toString())
                    val classpath = path.child(CLASS_INFO)

                    val klass = DutyClass(
                        name = classpath.child(CLASS_NAME).value.toString(),
                        dutyAmount = classpath.child(CLASS_DUTY_AMOUNT).value.toString(),
                        creatorName = classpath.child(CLASS_CREATOR_NAME).value.toString(),
                        id = classpath.child(CLASS_ID).value.toString(),
                        creatorId = classpath.child(CLASS_CREATOR_ID).value.toString(),
                        grade = classpath.child(CLASS_GRADE).value.toString(),
                        show = classpath.child(CLASS_SHOW).value.toString().toBoolean(),
                        isPinnedByCurrentUser = true,
                        pinnedTime = getCurrentTime().toLong()
                    )
                    pinnedClasses.add(klass)
                    classRepository.addClass(klass)

                    path.child(DUTY_LIST).children.forEach { pds ->
                        pairRepository.addPair(DutyPair(
                            name = pds.child(FULLNAME).value.toString(),
                            debts = pds.child(DEBTS).value.toString().toInt(),
                            id = pds.key.toString().toInt(),
                            isCurrent = pds.child(IS_CURRENT).value.toString().toBoolean(),
                            dutyTime = pds.child(DUTY_TIME).value.toString().toLong(),
                            dutiesAmount = pds.child(DUTIES_AMOUNT).value.toString().toInt(),
                            classId = classpath.child(CLASS_ID).value.toString()
                        ))

                        pds.child(EVENT_LIST).children.forEach { eds ->
                            eventRepository.addEvent(DutyEvent(
                                id = eds.key.toString().toInt(),
                                event = eds.child(EVENT_NAME).value.toString(),
                                date = eds.child(EVENT_TIME).value.toString(),
                                pairId = pds.key.toString().toInt(),
                                classId = classpath.child(CLASS_ID).value.toString()
                            ))
                        }
                    }
                }

                pinnedClasses = pinnedClasses.filter{ it.show || it.creatorId == currentUser.uid }.toMutableList()
                pinnedClassesList.value = pinnedClasses

                GlobalScope.launch {
                    classRepository.downloadData(currentUser.uid, false)
                }
            }
        }
    }
}