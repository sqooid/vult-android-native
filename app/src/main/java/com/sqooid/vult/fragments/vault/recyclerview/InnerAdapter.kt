package com.sqooid.vult.fragments.vault.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sqooid.vult.database.CredentialField
import com.sqooid.vult.databinding.FieldBinding
import com.sqooid.vult.databinding.TagBinding

class FieldAdapter(
    var fields: List<CredentialField>,
    private val onClickCopy: (String, Boolean) -> Unit,
) : RecyclerView.Adapter<FieldAdapter.FieldViewHolder>() {
    class FieldViewHolder(val binding: FieldBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FieldViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FieldBinding.inflate(inflater, parent, false)
        return FieldViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FieldViewHolder, position: Int) {
        holder.binding.textFieldName.text = fields[position].name
        holder.binding.textValue.text = fields[position].value
        holder.binding.textValue.setOnClickListener {
            onClickCopy(fields[position].value, false)
        }
    }

    override fun getItemCount(): Int {
        return fields.size
    }
}

class TagAdapter(var tags: List<String>, private val onClick: ((view: View) -> Unit)? = null) :
    RecyclerView.Adapter<TagAdapter.TagViewHolder>() {
    class TagViewHolder(val binding: TagBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TagBinding.inflate(inflater, parent, false)
        binding.root.setOnClickListener {
            onClick?.let { func -> func(it) }
        }
        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.binding.text.text = tags[position]
    }

    override fun getItemCount(): Int {
        return tags.size
    }
}