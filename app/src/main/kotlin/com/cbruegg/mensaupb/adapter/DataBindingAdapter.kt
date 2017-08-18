package com.cbruegg.mensaupb.adapter

import android.databinding.ViewDataBinding
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.cbruegg.mensaupb.BR
import com.cbruegg.mensaupb.GlideRequests

/**
 * A RecyclerView adapter that auto-updates the RecyclerView on changes
 * of the [list]. Row views are created automatically. Data binding is used
 * to load data into the rows.
 *
 * @param [delegateFor]  A selector for the delegate to use for the specific instance.
 *                       For the same instance, this should always return the same delegate.
 * @param [imageUrlGetter] Optionally returns an image for preloading
 */
class DataBindingAdapter<DATA : Any>(
        private val glide: GlideRequests,
        private val imageUrlGetter: (DATA) -> String?,
        private val delegateFor: (DATA) -> DataBindingViewTypeDelegate<DATA>
) : ObservableListAdapter<DATA, BindingHolder<ViewDataBinding>>(),
        ListPreloader.PreloadModelProvider<DATA>,
        ListPreloader.PreloadSizeProvider<DATA> {

    private val stolenSizeByViewType = mutableMapOf<Int, IntArray>()

    override fun getPreloadRequestBuilder(item: DATA): RequestBuilder<*> = glide.load(imageUrlGetter(item))

    override fun getPreloadItems(position: Int): List<DATA> = listOf(list[position])

    override fun getPreloadSize(item: DATA, adapterPosition: Int, perItemPosition: Int): IntArray? =
            stolenSizeByViewType[getItemViewType(adapterPosition)]

    private val delegateByViewType = mutableMapOf<Int, DataBindingViewTypeDelegate<DATA>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BindingHolder<ViewDataBinding> {
        val delegate = delegateByViewType[viewType]!!
        return BindingHolder(inflater.inflate(delegate.layoutId, parent, false))
    }

    override fun onBindViewHolder(holder: BindingHolder<ViewDataBinding>, item: DATA, onClickListener: View.OnClickListener, viewType: Int) {
        val delegate = delegateByViewType[viewType]!!

        holder.binding.setVariable(delegate.modelVar, item)
        holder.binding.setVariable(delegate.onClickListenerVar, onClickListener)
        holder.binding.executePendingBindings()

        if (viewType !in stolenSizeByViewType && delegate.imageId != null) {
            val iv = holder.itemView.findViewById<ImageView>(delegate.imageId)
            stolenSizeByViewType[viewType] = intArrayOf(iv.width, iv.height)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val delegate = delegateFor(list[position])
        val hashCode = delegate.hashCode()
        delegateByViewType[hashCode] = delegate
        return hashCode
    }

}

/**
 * Factory method for [DataBindingAdapter]s supporting only
 * one viewType.
 */
inline fun <reified DATA : Any> DataBindingAdapter(
        /**
         * Layout to use for rows
         */
        @LayoutRes layoutId: Int,
        /**
         * Variable id from [BR] to assign the model [DATA] to.
         */
        modelVar: Int,
        /**
         * Variable id to set the onClickListener to.
         */
        onClickListenerVar: Int = BR.onClickListener,
        /**
         * Id of a view holding an image used for preloading. Needs to have a fixed size.
         */
        @IdRes imageId: Int?,
        glide: GlideRequests,
        /**
         * Optionally returns an image URL for preloading.
         */
        noinline imageUrlGetter: (DATA) -> String?
): DataBindingAdapter<DATA> {
    val delegate = DataBindingViewTypeDelegate<DATA>(layoutId, modelVar, onClickListenerVar, imageId)
    return DataBindingAdapter<DATA>(glide, imageUrlGetter) { delegate }
}

data class DataBindingViewTypeDelegate<DATA>(
        /**
         * Layout to use for rows
         */
        @LayoutRes val layoutId: Int,
        /**
         * Variable id from [BR] to assign the model [DATA] to.
         */
        val modelVar: Int,
        /**
         * Variable id to set the onClickListener to.
         */
        val onClickListenerVar: Int = BR.onClickListener,
        /**
         * Optional ID of a view holding an image used for preloading.
         */
        @IdRes val imageId: Int? = null
)