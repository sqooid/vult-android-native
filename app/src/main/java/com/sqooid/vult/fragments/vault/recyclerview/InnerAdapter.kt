package com.sqooid.vult.fragments.vault.recyclerview

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sqooid.vult.database.CredentialField
import com.sqooid.vult.databinding.FieldBinding
import com.sqooid.vult.databinding.TagBinding

class AdapterClickOverride(private val clickParent: View) : RecyclerView.OnItemTouchListener {
    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
//        val child = rv.findChildViewUnder(e.x, e.y)
//        if (child == null) {
//            clickParent.dispatchTouchEvent(e)
//            return false
//        }
//        return true
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        val child = rv.findChildViewUnder(e.x, e.y)
        if (child == null) {
            clickParent.dispatchTouchEvent(e)
        }
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
}

class FieldAdapter(var fields: List<CredentialField>, private val clickParent: View) : RecyclerView.Adapter<FieldAdapter.FieldViewHolder>() {
    class FieldViewHolder(val binding: FieldBinding) : RecyclerView.ViewHolder(binding.root) {}

//    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
//        super.onAttachedToRecyclerView(recyclerView)
//        recyclerView.addOnItemTouchListener(AdapterClickOverride(clickParent))
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FieldViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FieldBinding.inflate(inflater, parent, false)
        return FieldViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FieldViewHolder, position: Int) {
        holder.binding.textFieldName.text = fields[position].name
        holder.binding.textValue.text = fields[position].value
    }

    override fun getItemCount(): Int {
        return fields.size
    }
}

class TagAdapter(var tags: List<String>, private val clickParent: View) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {
    class TagViewHolder(val binding: TagBinding) : RecyclerView.ViewHolder(binding.root) {}

//    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
//        super.onAttachedToRecyclerView(recyclerView)
//        recyclerView.addOnItemTouchListener(AdapterClickOverride(clickParent))
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TagBinding.inflate(inflater, parent, false)
        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.binding.text.text = tags[position]
    }

    override fun getItemCount(): Int {
        return tags.size
    }
}