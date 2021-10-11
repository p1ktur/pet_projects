package com.renatsolocorp.dutyapp.database.classdb

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.renatsolocorp.dutyapp.classes.editableclass.pairsList
import com.renatsolocorp.dutyapp.classes.myclasses.firstLaunch
import com.renatsolocorp.dutyapp.classes.myclasses.myClassesViewModel
import com.renatsolocorp.dutyapp.database.*
import com.renatsolocorp.dutyapp.database.eventdb.DutyEvent
import com.renatsolocorp.dutyapp.database.eventdb.EventDatabase
import com.renatsolocorp.dutyapp.database.pairdb.DutyPair
import com.renatsolocorp.dutyapp.database.pairdb.PairDatabase
import com.renatsolocorp.dutyapp.login.loggingIn
import com.renatsolocorp.dutyapp.main.db
import com.renatsolocorp.dutyapp.main.deletingClass
import com.renatsolocorp.dutyapp.main.headerImage
import com.renatsolocorp.dutyapp.profile.globalProfilePreferences
import com.renatsolocorp.dutyapp.profile.profileViewModel
import com.renatsolocorp.dutyapp.profile.userImage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ClassRepository(application: Application) {
    private val currentUser = FirebaseAuth.getInstance().currentUser!!
    val classDao = ClassDatabase.get(application).getDao()
    val myClasses = classDao.getAllClasses(currentUser.uid)
    val pinnedClasses = classDao.getPinnedClasses()

    private val pairDao = PairDatabase.get(application).getDao()
    private val eventDao = EventDatabase.get(application).getDao()

    fun addClass(input: DutyClass){
        GlobalScope.launch {
            classDao.addClass(input)
        }
    }

    fun updateClass(input: DutyClass){
        GlobalScope.launch {
            classDao.updateClass(input)
        }
    }

    fun updateAllClasses(name: String, grade: String, gradeShow: Boolean){
        GlobalScope.launch {
            classDao.getAllClassesAsList(currentUser.uid).forEach{
                classDao.updateClass(it.apply { creatorName = name; this.grade = grade; this.gradeShow = gradeShow })
            }
        }
    }

    fun updateDataInTheInternet(snapshot: DataSnapshot){
        GlobalScope.launch {
            val classes = classDao.getAllClassesAsList(currentUser.uid)
            val updatedClassesIndexes = mutableListOf<Int>()
            snapshot.child(currentUser.uid).child(CLASSES).children.forEach { ds ->
                if (classes.map{ it.id }.contains(ds.key.toString())){
                    val klass = classes[classes.map{ it.id }.indexOf(ds.key.toString())]
                    updatedClassesIndexes.add(classes.indexOf(klass))
                    ds.child(CLASS_INFO).child(CLASS_NAME).ref.setValue(klass.name)
                    ds.child(CLASS_INFO).child(CLASS_ID).ref.setValue(klass.id)
                    ds.child(CLASS_INFO).child(CLASS_DUTY_AMOUNT).ref.setValue(klass.dutyAmount)
                    ds.child(CLASS_INFO).child(CLASS_CREATOR_ID).ref.setValue(klass.creatorId)
                    ds.child(CLASS_INFO).child(CLASS_CREATOR_NAME).ref.setValue(klass.creatorName)
                    ds.child(CLASS_INFO).child(CLASS_GRADE).ref.setValue(klass.grade)
                    ds.child(CLASS_INFO).child(CLASS_GRADE_SHOW).ref.setValue(klass.gradeShow)
                    ds.child(CLASS_INFO).child(CLASS_CREATED_TIME).ref.setValue(klass.createdTime)
                    ds.child(CLASS_INFO).child(CLASS_SHOW).ref.setValue(klass.show)
                } else {
                    ds.ref.removeValue()
                    if (snapshot.child(currentUser.uid).child(USER_INFO).child(PINNED_CLASSES_LIST).child(ds.key.toString()).exists()){
                        snapshot.child(currentUser.uid).child(USER_INFO).child(PINNED_CLASSES_LIST).child(ds.key.toString()).ref.removeValue()
                    }
                }
            }

            if (updatedClassesIndexes.size != classes.size){
                classes.forEach{
                    if (!updatedClassesIndexes.contains(classes.indexOf(it))){
                        val klass = it
                        val classSnapshot = snapshot.child(currentUser.uid).child(CLASSES).child(klass.id)

                        classSnapshot.child(CLASS_INFO).child(CLASS_NAME).ref.setValue(klass.name)
                        classSnapshot.child(CLASS_INFO).child(CLASS_ID).ref.setValue(klass.id)
                        classSnapshot.child(CLASS_INFO).child(CLASS_DUTY_AMOUNT).ref.setValue(klass.dutyAmount)
                        classSnapshot.child(CLASS_INFO).child(CLASS_CREATOR_ID).ref.setValue(klass.creatorId)
                        classSnapshot.child(CLASS_INFO).child(CLASS_CREATOR_NAME).ref.setValue(klass.creatorName)
                        classSnapshot.child(CLASS_INFO).child(CLASS_GRADE).ref.setValue(klass.grade)
                        classSnapshot.child(CLASS_INFO).child(CLASS_SHOW).ref.setValue(klass.show)
                        classSnapshot.child(CLASS_INFO).child(CLASS_GRADE_SHOW).ref.setValue(klass.gradeShow)
                        classSnapshot.child(CLASS_INFO).child(CLASS_CREATED_TIME).ref.setValue(klass.createdTime.toString())

                        pairDao.getAllPairsAsList(klass.id).forEach { pair ->
                            classSnapshot.child(DUTY_LIST).child(pair.id.toString()).child(FULLNAME).ref.setValue(pair.name)
                            classSnapshot.child(DUTY_LIST).child(pair.id.toString()).child(DEBTS).ref.setValue(pair.debts)
                            classSnapshot.child(DUTY_LIST).child(pair.id.toString()).child(DUTIES_AMOUNT).ref.setValue(pair.dutiesAmount)
                            classSnapshot.child(DUTY_LIST).child(pair.id.toString()).child(IS_CURRENT).ref.setValue(pair.isCurrent)
                            classSnapshot.child(DUTY_LIST).child(pair.id.toString()).child(DUTY_TIME).ref.setValue(pair.dutyTime)

                            GlobalScope.launch {
                                eventDao.getPairEventsAsList(pair.id, pair.classId).forEach{ event ->
                                    classSnapshot.child(DUTY_LIST).child(pair.id.toString()).child(EVENT_LIST).child(event.id.toString()).child(EVENT_NAME).ref.setValue(event.event)
                                    classSnapshot.child(DUTY_LIST).child(pair.id.toString()).child(EVENT_LIST).child(event.id.toString()).child(EVENT_TIME).ref.setValue(event.date)
                                }
                            }
                        }

                        classSnapshot.child(DUTY_LIST).children.forEach { ds ->
                            val value = ds.key.toString().toInt()
                            if (!pairsList.map{ pair -> pair.id }.contains(value)) ds.ref.removeValue()
                        }
                    }
                }
            }
        }
    }

    fun getAllClasses(userUid: String) {
        GlobalScope.launch {
            myClassesViewModel.myClassesList = classDao.getAllClasses(userUid)

            downloadData(userUid)
        }
    }

    fun downloadData(userUid: String, changeLoggingIn: Boolean = true){
        val myClasses = classDao.getAllClassesAsList(userUid)
        if (myClasses.isEmpty() || loggingIn) {
            val classes = mutableListOf<DutyClass>()
            val pairs = mutableListOf<DutyPair>()
            val events = mutableListOf<DutyEvent>()

            db.getReference(USERS).get().addOnCompleteListener { utask ->
                if (utask.isSuccessful && utask.result != null) {
                    val result = utask.result!!

                    if (result.child(currentUser.uid).child(CLASSES).exists()) result.child(currentUser.uid).child(CLASSES).children.forEach { klass ->
                        val infoPath = klass.child(CLASS_INFO)
                        val dutyList = klass.child(DUTY_LIST)

                        if (myClasses.isNotEmpty() && !myClasses.map{ it.id }.contains(infoPath.child(CLASS_ID).value.toString()) || myClasses.isEmpty()){
                            classes.add(DutyClass(
                                name = infoPath.child(CLASS_NAME).value.toString(),
                                id = infoPath.child(CLASS_ID).value.toString(),
                                creatorName = infoPath.child(CLASS_CREATOR_NAME).value.toString(),
                                creatorId = infoPath.child(CLASS_CREATOR_ID).value.toString(),
                                dutyAmount = infoPath.child(CLASS_DUTY_AMOUNT).value.toString(),
                                grade = infoPath.child(CLASS_GRADE).value.toString(),
                                gradeShow = infoPath.child(CLASS_GRADE_SHOW).value.toString().toBoolean(),
                                show = infoPath.child(CLASS_SHOW).value.toString().toBoolean(),
                                isPinnedByCurrentUser = infoPath.child(CLASS_PINNED_LIST).child(currentUser.uid).exists(),
                                createdTime = infoPath.child(CLASS_CREATED_TIME).value.toString().toLong()
                            ))

                            dutyList.children.forEach { pair ->
                                pairs.add(DutyPair(
                                    name = pair.child(FULLNAME).value.toString(),
                                    debts = pair.child(DEBTS).value.toString().toInt(),
                                    id = pair.key.toString().toInt(),
                                    isCurrent = pair.child(IS_CURRENT).value.toString().toBoolean(),
                                    dutyTime = pair.child(DUTY_TIME).value.toString().toLong(),
                                    dutiesAmount = pair.child(DUTIES_AMOUNT).value.toString().toInt(),
                                    classId = klass.key.toString()
                                ))

                                pair.child(EVENT_LIST).children.forEach { event ->
                                    events.add(DutyEvent(
                                        id = event.key.toString().toInt(),
                                        event = event.child(EVENT_NAME).value.toString(),
                                        date = event.child(EVENT_TIME).value.toString(),
                                        pairId = pair.key.toString().toInt(),
                                        classId = klass.key.toString()
                                    ))
                                }
                            }
                        }
                    }
                    GlobalScope.launch {
                        classDao.addClasses(classes)
                        pairDao.addPairs(pairs)
                        eventDao.addEvents(events)
                    }
                }
            }

            if (changeLoggingIn) loggingIn = false
        } else if (!loggingIn && myClasses.isNotEmpty()){
            myClassesViewModel.updateDataInTheInternet()
        }
    }

    fun getPinnedClasses(context: Context){
        GlobalScope.launch {
            val localPinnedClasses = classDao.getPinnedClasses()
            profileViewModel.localPinnedClassesList = localPinnedClasses

            val classes = classDao.getPinnedClassesAsList()
            val pairs = mutableListOf<DutyPair>()

            classes.forEach {
                pairs.addAll(pairDao.getAllPairsAsList(it.id))
            }

            val ownClasses = classDao.getAllClassesAsList(currentUser.uid)
            globalProfilePreferences.customSaveData(ownClasses.size.toString(), OWN_CLASSES)

            profileViewModel.updateDataForCurrentUser(context, pairs)
        }
    }

    fun deleteClass(input: DutyClass){
        GlobalScope.launch {
            classDao.deleteClass(input)
        }
    }

    fun deleteClassWithId(classId: String){
        GlobalScope.launch {
            val klass = classDao.getClassesWithId(classId).last()
            classDao.deleteClass(klass)
        }
    }

    fun deleteClassEntirely(classId: String){
        GlobalScope.launch {
            val klassList = classDao.getClassesWithId(classId)
            if (klassList.isNotEmpty()){
                val klass = klassList.last()
                classDao.deleteClass(klass)

                pairDao.getAllPairsAsList(classId).forEach { pair ->
                    pairDao.deletePair(pair)

                    eventDao.getPairEventsAsList(pair.id, classId).forEach{ event ->
                        eventDao.deleteSinglePairEvent(event)
                    }
                }
            }
        }
    }

    fun clearAllClassesWhenLogin(){
        GlobalScope.launch {
            if (classDao.getAllClassesAsList(currentUser.uid).isNotEmpty()){
                classDao.clearAllClasses()
                pairDao.clearAllPairs()
                eventDao.clearAllEvents()
            }
        }
    }

    fun getDataFromInternetOnLogin(context: Context){
        GlobalScope.launch {
            if (classDao.getAllClassesAsList(currentUser.uid).isNotEmpty()){
                classDao.clearAllClasses()
                pairDao.clearAllPairs()
                eventDao.clearAllEvents()
            }

            db.getReference(USERS).get().addOnCompleteListener { utask ->
                if (utask.isSuccessful && utask.result != null) {
                    val result = utask.result!!
                    GlobalScope.launch {

                    }
                    val storage = FirebaseStorage.getInstance()
                    val reference = storage.reference.child("$PROFILE_IMAGES/${currentUser.uid}.jpg")
                    if (result.child(currentUser.uid).child(USER_INFO).child(PROFILE_IMAGE_LOCATION).exists()) reference.downloadUrl.addOnSuccessListener {
                        Glide.with(context).load(it).centerCrop().transition(DrawableTransitionOptions.withCrossFade()).addListener(object : RequestListener<Drawable> {
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

//                    result.child(currentUser.uid).child(USER_INFO).child(PINNED_CLASSES_LIST).children.forEach { ds ->
//                        val path = result.child(ds.value.toString()).child(CLASSES).child(ds.key.toString())
//                        val classpath = path.child(CLASS_INFO)
//                        pinnedClasses.add(DutyClass(
//                            name = classpath.child(CLASS_NAME).value.toString(),
//                            dutyAmount = classpath.child(CLASS_DUTY_AMOUNT).value.toString(),
//                            creatorName = classpath.child(CLASS_CREATOR_NAME).value.toString(),
//                            id = classpath.child(CLASS_ID).value.toString(),
//                            creatorId = classpath.child(CLASS_CREATOR_ID).value.toString(),
//                            grade = classpath.child(CLASS_GRADE).value.toString(),
//                            show = classpath.child(CLASS_SHOW).value.toString().toBoolean(),
//                            isPinnedByCurrentUser = true,
//                            pinnedTime = getCurrentTime().toLong()
//                        ))
//
//                        path.child(DUTY_LIST).children.forEach { pds ->
//                            pairRepository.addPair(DutyPair(
//                                name = pds.child(FULLNAME).value.toString(),
//                                debts = pds.child(DEBTS).value.toString().toInt(),
//                                id = pds.key.toString().toInt(),
//                                isCurrent = pds.child(IS_CURRENT).value.toString().toBoolean(),
//                                dutyTime = pds.child(DUTY_TIME).value.toString().toLong(),
//                                dutiesAmount = pds.child(DUTIES_AMOUNT).value.toString().toInt(),
//                                classId = classpath.child(CLASS_ID).value.toString()
//                            ))
//
//                            pds.child(EVENT_LIST).children.forEach { eds ->
//                                eventRepository.addEvent(DutyEvent(
//                                    id = eds.key.toString().toInt(),
//                                    event = eds.child(EVENT_NAME).value.toString(),
//                                    date = eds.child(EVENT_TIME).value.toString(),
//                                    pairId = pds.key.toString().toInt(),
//                                    classId = classpath.child(CLASS_ID).value.toString()
//                                ))
//                            }
//                        }
//                    }
//
//
//                    pinnedClasses = pinnedClasses.filter{ it.show || it.creatorId == currentUser.uid }.toMutableList()
//                    pinnedClassesList.value = pinnedClasses
                }
            }
        }
    }

    fun clearAllClasses(){
        GlobalScope.launch {
            classDao.clearAllClasses()
            pairDao.clearAllPairs()
            eventDao.clearAllEvents()
        }
    }
}