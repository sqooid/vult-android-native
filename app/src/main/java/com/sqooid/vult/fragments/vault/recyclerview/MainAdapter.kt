package com.sqooid.vult.fragments.vault.recyclerview

import android.animation.ValueAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sqooid.vult.database.Credential
import com.sqooid.vult.databinding.CredentialTileBinding

class MainAdapter(
    var data: List<Credential>,
    private val recyclerView: RecyclerView,
    private val onClickEdit: (Int) -> Unit
) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {
    class ViewHolder(val binding: CredentialTileBinding) : RecyclerView.ViewHolder(binding.root) {
        var collapsedHeight = 0
        var expandedHeight = 0
        var animating = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CredentialTileBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        binding.tagContainer.layoutManager =
            LinearLayoutManager(parent.context, LinearLayoutManager.HORIZONTAL, false)
        binding.tagContainer.adapter = TagAdapter(listOf())

        binding.fieldContainer.layoutManager = LinearLayoutManager(parent.context)
        binding.fieldContainer.adapter = FieldAdapter(listOf())
        binding.fieldContainer.suppressLayout(true)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val credential = data[position]
        binding.textViewName.text = credential.name

        // Password
        binding.password.text = credential.password

        // Tags
        if (credential.tags.isNotEmpty()) {
            binding.tagContainer.isVisible = true
            (binding.tagContainer.adapter as TagAdapter).tags = credential.tags.toList()
            binding.tagContainer.adapter!!.notifyDataSetChanged()
        } else {
            binding.tagContainer.isVisible = false
        }

        // First field
        if (credential.fields.isNotEmpty()) {
            binding.firstField.fieldLayout.visibility = View.VISIBLE
            binding.firstField.textFieldName.text = credential.fields[0].name
            binding.firstField.textValue.text = credential.fields[0].value

            if (credential.fields.size > 1) {
                binding.fieldContainer.suppressLayout(false)
                (binding.fieldContainer.adapter as FieldAdapter).fields =
                    credential.fields.slice(1 until credential.fields.size)
                binding.fieldContainer.suppressLayout(true)
            }
            binding.fieldContainer.adapter!!.notifyDataSetChanged()

            setExpansionVisibility(credential.expanded, binding)

        } else {
            binding.fieldContainer.visibility = View.GONE
            binding.firstField.fieldLayout.visibility = View.GONE
            binding.password.visibility = View.GONE
        }

        setExpansionVisibility(false, binding)
        holder.binding.card.measure(
            View.MeasureSpec.makeMeasureSpec(
                recyclerView.width,
                View.MeasureSpec.EXACTLY
            ), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        holder.collapsedHeight = holder.binding.card.measuredHeight
        setExpansionVisibility(true, binding)
        holder.binding.card.measure(
            View.MeasureSpec.makeMeasureSpec(
                recyclerView.width,
                View.MeasureSpec.EXACTLY
            ), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        holder.expandedHeight = holder.binding.card.measuredHeight

        setExpansionVisibility(credential.expanded, binding)
        holder.binding.card.layoutParams.height =
            if (credential.expanded) holder.expandedHeight else holder.collapsedHeight
        holder.binding.card.requestLayout()

        binding.root.setOnClickListener {
            if (!holder.animating) {
                credential.expanded = !credential.expanded
                expandItem(holder, credential.expanded)
            }
        }
        binding.editButton.setOnClickListener {
            onClickEdit(position)
        }
    }

    private fun setExpansionVisibility(
        expand: Boolean,
        binding: CredentialTileBinding
    ) {
        if (expand) {
            binding.editButton.visibility = View.VISIBLE
            binding.fieldContainer.isVisible = true
            binding.password.isVisible = true
            binding.passwordTitle.isVisible = true
        } else {
            binding.editButton.visibility = View.INVISIBLE
            binding.fieldContainer.isVisible = false
            binding.password.isVisible = false
            binding.passwordTitle.isVisible = false
        }
    }

    private fun expandItem(holder: ViewHolder, expand: Boolean) {
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 250
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                holder.binding.card.layoutParams.height =
                    (holder.collapsedHeight + (holder.expandedHeight - holder.collapsedHeight) * it.animatedValue as Float).toInt()
                holder.binding.card.requestLayout()
            }
            doOnStart { holder.animating = true }
            doOnEnd { holder.animating = false }
        }
        if (expand) {
            animator.doOnStart { setExpansionVisibility(true, holder.binding) }
            animator.start()
        } else {
            animator.doOnEnd { setExpansionVisibility(false, holder.binding) }
            animator.reverse()
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}