package com.sqooid.vult.fragments.vault.recyclerview

import android.animation.LayoutTransition
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.constraintlayout.helper.widget.Carousel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sqooid.vult.R
import com.sqooid.vult.database.Credential
import com.sqooid.vult.databinding.CredentialTileBinding
import com.sqooid.vult.databinding.FieldBinding
import com.sqooid.vult.databinding.TagBinding

class MainAdapter(var data: List<Credential>): RecyclerView.Adapter<MainAdapter.ViewHolder>() {
    class ViewHolder(val binding: CredentialTileBinding) : RecyclerView.ViewHolder(binding.root){
        var expanded = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CredentialTileBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        val layout = binding.linearLayout
        layout.layoutTransition = LayoutTransition().apply {
            enableTransitionType(LayoutTransition.CHANGING)
        }
        binding.tagContainer.layoutManager = LinearLayoutManager(parent.context, LinearLayoutManager.HORIZONTAL, false)
        binding.tagContainer.adapter = TagAdapter(listOf())
        binding.fieldContainer.layoutManager = LinearLayoutManager(parent.context)
        binding.fieldContainer.adapter = FieldAdapter(listOf())
        binding.card.animation = AnimationUtils.loadAnimation(parent.context, R.anim.fade_in)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.expanded = false
        val binding = holder.binding
        val credential = data[position]
        binding.textViewName.text = credential.name

        // Tags
        if (credential.tags.isNotEmpty()) {
            binding.tagScrollView.visibility = View.VISIBLE
            (binding.tagContainer.adapter as TagAdapter).tags = credential.tags
        } else {
            binding.tagScrollView.visibility = View.GONE
        }

        // First field
        if (credential.fields.isNotEmpty()) {
            binding.fieldContainer.visibility = View.VISIBLE
            (binding.fieldContainer.adapter as FieldAdapter).fields = credential.getVisibleFields()
            Log.d("app", "bound ${credential.getVisibleFields()}")
            binding.fieldContainer.adapter!!.notifyDataSetChanged()
            // Expand when clicked
            binding.root.setOnClickListener {
                Log.d("app","clicked tile $position")
                credential.expanded = !credential.expanded
                (binding.fieldContainer.adapter as FieldAdapter).fields = credential.getVisibleFields()
                binding.fieldContainer.adapter!!.notifyDataSetChanged()
                this.notifyItemChanged(position)
                this.notifyDataSetChanged()
            }
        } else {
            binding.fieldContainer.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}