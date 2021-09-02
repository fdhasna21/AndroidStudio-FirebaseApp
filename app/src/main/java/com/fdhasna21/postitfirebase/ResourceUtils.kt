package com.fdhasna21.postitfirebase

import android.content.ContentResolver
import android.content.Context
import android.net.Uri

import androidx.annotation.AnyRes

class ResourceUtils {
    fun getUriToDrawable(
        context: Context,
        @AnyRes drawableId: Int
    ): Uri {
        return Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + context.resources.getResourcePackageName(drawableId)
                    + '/' + context.resources.getResourceTypeName(drawableId)
                    + '/' + context.resources.getResourceEntryName(drawableId)
        )
    }
}