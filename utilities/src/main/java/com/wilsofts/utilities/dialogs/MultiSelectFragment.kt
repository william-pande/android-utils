package com.wilsofts.utilities.dialogs

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wilsofts.utilities.R
import com.wilsofts.utilities.databinding.DialogMultiSelectBinding
import com.wilsofts.utilities.databinding.DialogMultiSelectItemsBinding
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

class MultiSelectFragment : BottomSheetDialogFragment() {
    private lateinit var binding: DialogMultiSelectBinding
    private lateinit var adapter: MultiSelectAdapter
    private lateinit var multi_select_items: MutableList<MultiSelectItem>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.dialog_multi_select, container, false)
        val options_receiver = this.requireArguments().getSerializable("options_receiver")!! as OptionsReceiver
        this.binding.dialogHeader.text = this.requireArguments().getString("title")

        this.multi_select_items = this.requireArguments()
                .getParcelableArrayList<MultiSelectItem>("array_items")!!.toMutableList()

        this.adapter = this.MultiSelectAdapter()
        this.binding.recyclerView.adapter = this.adapter

        this.binding.cancel.setOnClickListener {
            options_receiver.response(used = false, array_items = this.multi_select_items, items = "")
            this.dismiss()
        }

        this.binding.proceed.setOnClickListener {
            val selected = mutableListOf<MultiSelectItem>()
            for (item in this.multi_select_items) {
                if (item.selected) {
                    selected.add(item)
                }
            }

            var name = ""
            for ((index, item) in selected.withIndex()) {
                name = "$name${item.item_name}"
                if (index < selected.size - 2) {
                    name = "$name, "
                } else if (index == selected.size - 2) {
                    name = "$name and "
                }
            }

            if (selected.size == this.multi_select_items.size) {
                name = "All Selected"
            }

            options_receiver.response(used = true, array_items = this.multi_select_items, items = name)
            this.dismiss()
        }

        this.binding.toggleData.setOnCheckedChangeListener { _, isChecked ->
            for ((index, item) in this.multi_select_items.withIndex()) {
                item.selected = isChecked
                this.multi_select_items[index] = item
            }
            this.adapter.notifyDataSetChanged()
        }

        return this.binding.root
    }

    private inner class MultiSelectAdapter : RecyclerView.Adapter<MultiSelectAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = DataBindingUtil.inflate<DialogMultiSelectItemsBinding>(
                    LayoutInflater.from(parent.context), R.layout.dialog_multi_select_items, parent, false)
            return this.ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = this@MultiSelectFragment.multi_select_items[position]
            holder.binding.useItem.isChecked = item.selected
            holder.binding.itemName.text = item.item_name

            holder.binding.useItem.setOnCheckedChangeListener { _, isChecked ->
                this@MultiSelectFragment.multi_select_items[position].selected = isChecked
            }
        }

        override fun getItemCount(): Int {
            return this@MultiSelectFragment.multi_select_items.size
        }

        inner class ViewHolder(val binding: DialogMultiSelectItemsBinding) : RecyclerView.ViewHolder(binding.root)
    }

    @Parcelize
    class MultiSelectItem(var selected: Boolean, val item_name: String, val item_id: Long) : Parcelable

    interface OptionsReceiver : Serializable {
        fun response(used: Boolean, array_items: MutableList<MultiSelectItem>, items: String)
    }

    companion object {
        fun newInstance(array_items: ArrayList<MultiSelectItem>, title: String, activity: FragmentActivity,
                        options_receiver: OptionsReceiver) {
            val bundle = Bundle()
            bundle.putParcelableArrayList("array_items", array_items)
            bundle.putString("title", title)
            bundle.putSerializable("options_receiver", options_receiver)

            val dialog = MultiSelectFragment()
            dialog.arguments = bundle
            dialog.show(activity.supportFragmentManager, "option_dialog")
        }
    }
}