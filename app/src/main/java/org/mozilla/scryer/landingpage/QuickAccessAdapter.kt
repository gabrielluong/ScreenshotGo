/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.scryer.landingpage

import android.annotation.SuppressLint
import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import org.mozilla.scryer.R
import org.mozilla.scryer.persistence.ScreenshotModel
import java.io.File

class QuickAccessAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val TYPE_ITEM = 0
        const val TYPE_MORE = 1
    }

    var list: List<ScreenshotModel> = emptyList()
    var clickListener: ItemClickListener? = null

    private val maxItemsToDisplay = HomeFragment.QUICK_ACCESS_ITEM_COUNT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_MORE -> createMoreHolder(parent)
            TYPE_ITEM -> createItemHolder(parent)
            else -> throw IllegalArgumentException("unrecognized view type")
        }
    }

    override fun getItemCount(): Int {
        return if (hasMoreItem()) {
            maxItemsToDisplay + 1
        } else {
            Math.min(list.size, maxItemsToDisplay)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (isPositionForMoreButton(position)) {
            return TYPE_MORE
        }
        return TYPE_ITEM
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        when (viewType) {
            TYPE_ITEM -> bindItemHolder(holder, position)
            TYPE_MORE -> bindMoreHolder(holder)
        }
    }

    private fun createItemHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quick_access, parent, false)
        val metrics = Resources.getSystem().displayMetrics
        val ratio = metrics.widthPixels / metrics.heightPixels.toFloat()
        val width = view.layoutParams.height * ratio
        view.layoutParams.width = width.toInt()
        view.layoutParams = view.layoutParams

        val holder = ScreenshotItemHolder(view)
        holder.image = view.findViewById(R.id.image_view)
        holder.itemView.setOnClickListener { _ ->
            holder.adapterPosition.takeIf { position ->
                position != RecyclerView.NO_POSITION

            }?.let { position: Int ->
                clickListener?.onItemClick(list[position], holder)
            }
        }
        return holder
    }

    private fun createMoreHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quick_access_more, parent, false)
        val holder = object : RecyclerView.ViewHolder(view) {
        }
        view.setOnClickListener {
            clickListener?.onMoreClick(holder)
        }
        return holder
    }

    private fun bindItemHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val path = list[position].absolutePath
        val fileName = path.substring(path.lastIndexOf(File.separator) + 1)
        (holder as? ScreenshotItemHolder)?.apply {
            holder.title?.text = fileName
            holder.image?.let {
                val options = RequestOptions()
                val corner = holder.itemView.resources.getDimensionPixelSize(R.dimen.quick_access_item_corner)
                options.transforms(CenterCrop(), RoundedCorners(corner))
                Glide.with(holder.itemView.context)
                        .load(File(list[position].absolutePath)).apply(options).into(it)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindMoreHolder(holder: RecyclerView.ViewHolder) {

    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        (holder as? ScreenshotItemHolder)?.image?.let {
            Glide.with(holder.itemView.context).clear(it)
        }
    }

    private fun isPositionForMoreButton(position: Int) = position >= maxItemsToDisplay

    private fun hasMoreItem() = list.size > maxItemsToDisplay

    interface ItemClickListener {
        fun onItemClick(screenshotModel: ScreenshotModel, holder: ScreenshotItemHolder)
        fun onMoreClick(holder: RecyclerView.ViewHolder)
    }
}