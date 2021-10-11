package com.renatsolocorp.dutyapp.users

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.renatsolocorp.dutyapp.extensions.calculateBitmap

class User(
    var id: String,
    var name: String,
    var grade: String,
    var gradeShow: Boolean,
    var isFollowing: Boolean,
    var userImageLocation: String
    ) {

    override fun toString(): String {
        return name
    }
}