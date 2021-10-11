package com.renatsolocorp.dutyapp.classes.myclasses

import android.app.Application
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.renatsolocorp.dutyapp.R
import com.renatsolocorp.dutyapp.database.classdb.DutyClass
import com.renatsolocorp.dutyapp.classes.editableclass.EditableClassFragment
import com.renatsolocorp.dutyapp.classes.editableclass.editing
import com.renatsolocorp.dutyapp.database.*
import com.renatsolocorp.dutyapp.database.classdb.ClassRepository
import com.renatsolocorp.dutyapp.extensions.*
import com.renatsolocorp.dutyapp.main.*
import kotlinx.android.synthetic.main.class_element_editable_layout.view.*

class MyClassesAdapter(val list: MutableList<DutyClass>, val application: Application, val fragmentManager: FragmentManager, val context: Context): RecyclerView.Adapter<MyClassesAdapter.ViewHolder>() {

    class ViewHolder(view: View, application: Application): RecyclerView.ViewHolder(view){
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val classRepository = ClassRepository(application)

        val className = view.editable_class_name_text!!
        val onDutyAmount = view.editable_on_duty_amount_text!!
        val classCreator = view.editable_class_creator_text!!
        val grade = view.edtable_class_grade_text!!
        val gradeField = view.editable_class_grade_field!!

        val pinnedImage = view.elem_editable_pinned_image_view!!
        private val clickable = view.editable_clickable_layout!!

        fun initListeners(viewedClass: DutyClass, application: Application, fragmentManager: FragmentManager, context: Context){
            clickable.setOnLongClickListener {
                pinnedImage.visibility = if (!viewedClass.isPinnedByCurrentUser) View.VISIBLE else View.GONE

                db.getReference(USERS).get().addOnCompleteListener { utask ->
                    if (utask.isSuccessful && utask.result != null){
                        val result = utask.result!!
                        viewedClass.isPinnedByCurrentUser = !viewedClass.isPinnedByCurrentUser
                        viewedClass.pinnedTime = getCurrentTime().toLong()
                        if (!viewedClass.isPinnedByCurrentUser){
                            pinnedImage.visibility = View.GONE
                            result.child(viewedClass.creatorId).child(CLASSES).child(viewedClass.id)
                                .child(CLASS_INFO).child(CLASS_PINNED_LIST).child(currentUser.uid).ref.removeValue()
                            result.child(currentUser.uid).child(USER_INFO).child(PINNED_CLASSES_LIST).child(viewedClass.id).ref.removeValue()

                            classRepository.updateClass(viewedClass)
                        } else {
                            pinnedImage.visibility = View.VISIBLE
                            result.child(viewedClass.creatorId).child(CLASSES).child(viewedClass.id)
                                .child(CLASS_INFO).child(CLASS_PINNED_LIST).child(currentUser.uid).ref.setValue(getCurrentTime())
                            result.child(currentUser.uid).child(USER_INFO).child(PINNED_CLASSES_LIST).child(viewedClass.id).ref.setValue(viewedClass.creatorId)

                            classRepository.updateClass(viewedClass)
                        }
                    } else {
                        showConnectionProblem(context)
                    }
                }
                true
            }

            clickable.setOnClickListener {
                selectedClass = viewedClass
                editing = true
                createNewClassButton.visibility = View.GONE
                classMenuButton.visibility = View.VISIBLE
                currentFragment = NEW_CLASS_FRAGMENT
                fragmentManager.beginTransaction().replace(R.id.main_fragment_container, EditableClassFragment(application)).addToBackStack(null).commit()
                mainDrawer.closeDrawer(GravityCompat.START)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.class_element_editable_layout, parent, false), application)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.className.text = list[position].name
        holder.onDutyAmount.text = list[position].dutyAmount
        holder.classCreator.text = list[position].creatorName
        holder.grade.text = list[position].grade
        holder.pinnedImage.visibility = if (list[position].isPinnedByCurrentUser) View.VISIBLE else View.GONE

        if (list[position].grade == "null" || list[position].grade == "") {
            holder.grade.visibility = View.GONE
            holder.gradeField.visibility = View.GONE
        }
        holder.grade.visibility = View.GONE
        holder.gradeField.visibility = View.GONE

        holder.initListeners(list[position], application, fragmentManager, context)
    }

    override fun getItemCount() = list.size
}