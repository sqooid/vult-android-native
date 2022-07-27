package com.sqooid.vult.fragments.credential

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sqooid.vult.database.CredentialField
import com.sqooid.vult.databinding.FieldEditBinding
import com.sqooid.vult.fragments.vault.recyclerview.FieldAdapter

class FieldEditAdapter(var fields: MutableList<CredentialField>) : RecyclerView.Adapter<FieldEditAdapter.FieldEditViewHolder>() {
    class FieldEditViewHolder(val binding: FieldEditBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FieldEditViewHolder {
        val binding = FieldEditBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FieldEditViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FieldEditViewHolder, position: Int) {
        val field = fields[position]
        holder.binding.textWrapper.hint = field.name
        holder.binding.textInput.text = holder.binding.textInput.text?.apply {
            clear()
            append(field.value)
        }
    }

    override fun getItemCount(): Int {
        return fields.size
    }

}