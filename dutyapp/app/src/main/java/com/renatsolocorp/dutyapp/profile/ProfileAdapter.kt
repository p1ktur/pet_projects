package com.renatsolocorp.dutyapp.profile

import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.renatsolocorp.dutyapp.R
import com.renatsolocorp.dutyapp.classes.editableclass.*
import com.renatsolocorp.dutyapp.database.classdb.DutyClass
import com.renatsolocorp.dutyapp.classes.viewedclass.ViewedClassFragment
import com.renatsolocorp.dutyapp.database.*
import com.renatsolocorp.dutyapp.database.classdb.ClassRepository
import com.renatsolocorp.dutyapp.database.eventdb.DutyEvent
import com.renatsolocorp.dutyapp.database.eventdb.EventRepository
import com.renatsolocorp.dutyapp.database.pairdb.DutyPair
import com.renatsolocorp.dutyapp.database.pairdb.PairRepository
import com.renatsolocorp.dutyapp.extensions.*
import com.renatsolocorp.dutyapp.main.*
import kotlinx.android.synthetic.main.class_element_layout.view.*

class ProfileAdapter(val list: MutableList<DutyClass>, val application: Application, val fragmentManager: FragmentManager, val viewedUserId: String, val context: Context): RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {

    class ViewHolder(view: View, application: Application): RecyclerView.ViewHolder(view) {
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val classRepository = ClassRepository(application)
        val pairRepository = PairRepository(application)
        val eventRepository = EventRepository(application)

        val className = view.class_name_text!!
        val onDutyAmount = view.on_duty_amount_text!!
        val classCreator = view.class_creator_text!!
        val grade = view.class_grade_text!!
        val gradeField = view.class_grade_field!!
        val divider = view.elem_divider!!

        val pinnedImage = view.elem_pinned_image_view!!
        private val clickable = view.clickable_layout!!

        fun initListeners(viewedClass: DutyClass, application: Application, fragmentManager: FragmentManager, viewedUserId: String, list: MutableList<DutyClass>, context: Context) {
            clickable.setOnClickListener {
                selectedClass = viewedClass
                mainBackButton.visibility = View.VISIBLE
                profileSettingsButton.visibility = View.GONE
                mainDrawerButton.visibility = View.GONE
                if (viewedClass.creatorId == currentUser.uid){
                    currentFragment = NEW_CLASS_FRAGMENT
                    fragmentManager.beginTransaction().replace(R.id.main_fragment_container, EditableClassFragment(application)).addToBackStack(null).commit()
                } else {
                    currentFragment = VIEWED_CLASS_FRAGMENT
                    fragmentManager.beginTransaction().replace(R.id.main_fragment_container, ViewedClassFragment(viewedClass, viewedUserId, application)).addToBackStack(null).commit()
                }
                mainDrawer.closeDrawer(GravityCompat.START)
                mainDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }

            clickable.setOnLongClickListener {
                if (viewedUserId != currentUser.uid){
                    pinnedImage.visibility = if (!viewedClass.isPinnedByCurrentUser) View.VISIBLE else View.GONE

                    db.getReference(USERS).get().addOnCompleteListener { utask ->
                        if (utask.isSuccessful && utask.result != null){
                            val result = utask.result!!
                            viewedClass.isPinnedByCurrentUser = !viewedClass.isPinnedByCurrentUser
                            if (!viewedClass.isPinnedByCurrentUser){
                                pinnedImage.visibility = View.GONE
                                result.child(viewedClass.creatorId).child(CLASSES).child(viewedClass.id)
                                    .child(CLASS_INFO).child(CLASS_PINNED_LIST).child(currentUser.uid).ref.removeValue()
                                result.child(currentUser.uid).child(USER_INFO).child(PINNED_CLASSES_LIST).child(viewedClass.id).ref.removeValue()

                                classRepository.deleteClassEntirely(viewedClass.id)

                                Toast.makeText(context, context.getString(R.string.class_is_unpinned), Toast.LENGTH_SHORT).show()
                            } else {
                                val currentTime = getCurrentTime()
                                pinnedImage.visibility = View.VISIBLE
                                result.child(viewedClass.creatorId).child(CLASSES).child(viewedClass.id)
                                    .child(CLASS_INFO).child(CLASS_PINNED_LIST).child(currentUser.uid).ref.setValue(currentTime)
                                result.child(currentUser.uid).child(USER_INFO).child(PINNED_CLASSES_LIST).child(viewedClass.id).ref.setValue(viewedClass.creatorId)

                                val infoPath = result.child(viewedClass.creatorId).child(CLASSES).child(viewedClass.id).child(CLASS_INFO)
                                val dutyList = result.child(viewedClass.creatorId).child(CLASSES).child(viewedClass.id).child(DUTY_LIST)

                                classRepository.addClass(DutyClass(
                                    name = infoPath.child(CLASS_NAME).value.toString(),
                                    id = infoPath.child(CLASS_ID).value.toString(),
                                    creatorName = infoPath.child(CLASS_CREATOR_NAME).value.toString(),
                                    creatorId = infoPath.child(CLASS_CREATOR_ID).value.toString(),
                                    dutyAmount = infoPath.child(CLASS_DUTY_AMOUNT).value.toString(),
                                    grade = infoPath.child(CLASS_GRADE).value.toString(),
                                    gradeShow = infoPath.child(CLASS_GRADE_SHOW).value.toString().toBoolean(),
                                    show = true,
                                    isPinnedByCurrentUser = true,
                                    pinnedTime = currentTime.toLong(),
                                    createdTime = if (infoPath.child(CLASS_CREATOR_ID).value.toString() == currentUser.uid) infoPath.child(CLASS_CREATED_TIME).value.toString().toLong() else 0L
                                ))

                                dutyList.children.forEach { ds ->
                                    pairRepository.addPair(DutyPair(
                                        name = ds.child(FULLNAME).value.toString(),
                                        debts = ds.child(DEBTS).value.toString().toInt(),
                                        id = ds.key.toString().toInt(),
                                        isCurrent = ds.child(IS_CURRENT).value.toString().toBoolean(),
                                        dutyTime = ds.child(DUTY_TIME).value.toString().toLong(),
                                        dutiesAmount = ds.child(DUTIES_AMOUNT).value.toString().toInt(),
                                        classId = viewedClass.id
                                    ))

                                    ds.child(EVENT_LIST).children.forEach { eds ->
                                        eventRepository.addEvent(DutyEvent(
                                            id = eds.key.toString().toInt(),
                                            event = eds.child(EVENT_NAME).value.toString(),
                                            date = eds.child(EVENT_TIME).value.toString(),
                                            pairId = ds.key.toString().toInt(),
                                            classId = viewedClass.id
                                        ))
                                    }
                                }

                                Toast.makeText(context, context.getString(R.string.class_is_pinned), Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            showConnectionProblem(context)
                        }
                    }
                } else {
                    val builder = AlertDialog.Builder(context)
                    val message = SpannableString(context.getString(R.string.class_can_be_pinned))
                    message.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.textColor)), 0, message.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    builder.setMessage(message)
                    builder.setTitle(context.getString(R.string.unpin_this_class))
                    builder.setPositiveButton(context.getString(R.string.ok)) { _: DialogInterface, _: Int ->
                        db.getReference(USERS).get().addOnCompleteListener { task ->
                            if (task.isSuccessful && task.result != null){
                                val result = task.result!!
                                list[list.indexOf(viewedClass)].isPinnedByCurrentUser = false
                                pinnedImage.visibility = View.GONE
                                result.child(viewedClass.creatorId).child(CLASSES).child(viewedClass.id).child(CLASS_INFO).child(CLASS_PINNED_LIST).child(currentUser.uid).ref.removeValue()
                                result.child(currentUser.uid).child(USER_INFO).child(PINNED_CLASSES_LIST).child(viewedClass.id).ref.removeValue()
                                if (viewedClass.creatorId != currentUser.uid) {
                                    classRepository.deleteClassEntirely(viewedClass.id)
                                } else {
                                    viewedClass.isPinnedByCurrentUser = false
                                    classRepository.updateClass(viewedClass)
                                }
                                profileViewModel.clearUnpinnedClasses(list)

                                Toast.makeText(context, context.getString(R.string.class_is_unpinned), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, context.getString(R.string.unpin_failed), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    builder.setNegativeButton(context.getString(R.string.cancel)) { _: DialogInterface, _: Int -> }
                    builder.setCancelable(true)
                    val dialog = builder.create()
                    dialog.window!!.setBackgroundDrawable(AppCompatResources.getDrawable(context, R.drawable.action_bg))
                    dialog.show()
                }

                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.class_element_layout, parent, false), application)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentUser = FirebaseAuth.getInstance().currentUser!!

        holder.className.text = list[position].name
        holder.onDutyAmount.text = list[position].dutyAmount
        holder.classCreator.text = list[position].creatorName
        holder.grade.text = list[position].grade
        holder.pinnedImage.visibility = if (list[position].isPinnedByCurrentUser) View.VISIBLE else View.GONE

        if (list[position].grade == "null" || list[position].grade == "" || (!list[position].gradeShow && list[position].creatorId != currentUser.uid)) {
            holder.grade.visibility = View.GONE
            holder.gradeField.visibility = View.GONE
        }
        holder.grade.visibility = View.GONE
        holder.gradeField.visibility = View.GONE

        //holder.divider.visibility = if (position == list.size-1) View.GONE else View.VISIBLE

        holder.initListeners(list[position], application, fragmentManager, viewedUserId, list, context)
    }

    override fun getItemCount() = list.size
}