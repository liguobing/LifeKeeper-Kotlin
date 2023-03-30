package com.lixyz.lifekeeperforkotlin.view.activity

import android.content.Context
import android.graphics.Point
import com.lixyz.lifekeeperforkotlin.R
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.filter.Filter
import com.zhihu.matisse.internal.entity.IncapableCause
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.utils.PhotoMetadataUtils


/**
 * @author LGB
 */
class GifSizeFilter(
    private val mMinWidth: Int,
    private val mMinHeight: Int,
    private val mMaxSize: Int
) : Filter() {

    override fun constraintTypes(): MutableSet<MimeType> {
        val hashSet: HashSet<MimeType> = HashSet()
        hashSet.add(MimeType.GIF)
        return hashSet
    }

    override fun filter(context: Context, item: Item): IncapableCause? {
        if (!needFiltering(context, item)) {
            return null
        }
        val size: Point =
            PhotoMetadataUtils.getBitmapBound(context.getContentResolver(), item.getContentUri())
        return if (size.x < mMinWidth || size.y < mMinHeight || item.size > mMaxSize) {
            IncapableCause(
                IncapableCause.DIALOG,
                context.getString(
                    R.string.error_gif,
                    mMinWidth,
                    PhotoMetadataUtils.getSizeInMB(mMaxSize.toLong()).toString()
                )
            )
        } else null
    }
}