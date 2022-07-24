package com.sqooid.vult.fragments.vault.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sqooid.vult.database.CredentialField
import com.sqooid.vult.databinding.FieldBinding
import com.sqooid.vult.databinding.TagBinding

class FieldAdapter(var fields: List<CredentialField>) : BaseAdapter() {
    override fun getCount(): Int {
        return fields.size
    }

    override fun getItem(p0: Int): Any {
        return fields[p0]
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val inflater = LayoutInflater.from(p2?.context)
        val binding = FieldBinding.inflate(inflater, p2, false)
        binding.textFieldName.text = fields[p0].name
        binding.textValue.text = fields[p0].value
        return binding.root
    }
}

class TagAdapter(var tags: List<String>) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {
    class TagViewHolder(val binding: TagBinding) : RecyclerView.ViewHolder(binding.root) {}

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