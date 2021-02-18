package com.wilsofts.utilities.dialogs

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wilsofts.utilities.LibUtils
import com.wilsofts.utilities.databinding.DialogMultiSelectOptionsBinding
import com.wilsofts.utilities.databinding.ListMultiSelectOptionsBinding
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.util.*

class MultiSelectOptions : BottomSheetDialogFragment() {
    private val original_items = arrayListOf<MultiSelect>()
    private val filter_items = arrayListOf<MultiSelect>()
    private lateinit var binding: DialogMultiSelectOptionsBinding

    var single_select: SingleSelect? = null
    var multi_receiver: MultiReceiver? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        this.binding = DialogMultiSelectOptionsBinding.inflate(inflater, container, false)

        this.original_items.addAll(this.requireArguments().getParcelableArrayList("select_items")!!)
        this.filter_items.addAll(this.original_items)

        this.single_select = this.requireArguments().getSerializable("single_select") as SingleSelect?
        this.multi_receiver = this.requireArguments().getSerializable("multi_receiver") as MultiReceiver?

        val adapter = this.SelectAdapter(items = this.filter_items)
        this.binding.recyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        this.binding.recyclerView.adapter = adapter

        this.binding.filterText.doAfterTextChanged {
            adapter.filter(it.toString())
        }

        if (this.single_select != null) {
            this.binding.multiSelectLayout.visibility = View.GONE

        } else {
            this.is_all_selected()
            this.binding.checkAll.setOnCheckedChangeListener { _, isChecked ->
                for (index in 0 until this.original_items.size) {
                    this.original_items[index].item_checked = isChecked
                }
                this.filter_items.clear()
                this.filter_items.addAll(this.original_items)
                adapter.notifyDataSetChanged()
            }

            this.binding.cancelSelection.setOnClickListener {
                this.dismiss()
            }

            this.binding.useSelection.setOnClickListener {
                this.multi_receiver?.receiver(this.original_items)
                this.dismiss()
            }
        }
        return this.binding.root
    }

    fun is_all_selected() {
        var all_selected = true
        for (index in 0 until this.original_items.size) {
            if (!this.original_items[index].item_checked) {
                all_selected = false
                break
            }
        }
        this.binding.checkAll.isChecked = all_selected
    }

    companion object {
        fun newInstance(select_items: ArrayList<MultiSelect>, manager: FragmentManager,
                        multi_receiver: MultiReceiver? = null, single_select: SingleSelect? = null) {
            val dialog = MultiSelectOptions()
            Bundle().apply {
                this.putParcelableArrayList("select_items", select_items)
                this.putSerializable("multi_receiver", multi_receiver)
                this.putSerializable("single_select", single_select)
                dialog.arguments = this
            }
            dialog.show(manager, "option_dialog")
        }
    }

    private inner class SelectAdapter(val items: ArrayList<MultiSelect>) : RecyclerView.Adapter<SelectAdapter.MyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return this.MyViewHolder(ListMultiSelectOptionsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val item = this.items[position]
            holder.binding.checkbox.text = item.item_text
            holder.binding.checkbox.isChecked = item.item_checked

            /*disabling click event for selected item*/
            if (this@MultiSelectOptions.single_select != null) {
                holder.binding.checkbox.isClickable = !item.item_checked
            }

            holder.binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                item.item_checked = isChecked
                if (this@MultiSelectOptions.single_select != null) {
                    this@MultiSelectOptions.original_items.forEachIndexed { index, _ ->
                        this@MultiSelectOptions.original_items[index].item_checked = false
                    }

                    val _position = this.get_position(item.item_id)
                    this@MultiSelectOptions.original_items[_position].item_checked = isChecked
                    this@MultiSelectOptions.single_select?.receiver(position = _position, select_items = this@MultiSelectOptions.original_items)
                    this@MultiSelectOptions.dismiss()
                } else {
                    this.items[position] = item
                    this@MultiSelectOptions.original_items[this.get_position(item.item_id)] = item
                    this@MultiSelectOptions.is_all_selected()
                }
            }
        }

        fun get_position(item_id: String): Int {
            for (index in 0 until this@MultiSelectOptions.original_items.size) {
                if (this@MultiSelectOptions.original_items[index].item_id == item_id) {
                    return index
                }
            }
            return 0
        }

        fun filter(search: String) {
            LibUtils.logE("Original Size ${items.size}")
            items.clear()
            LibUtils.logE("Clear Size ${items.size}")

            original_items.forEach {
               if(it.item_id.toLowerCase(Locale.getDefault()).contains(search.toLowerCase(Locale.getDefault())) ||
                       it.item_text.toLowerCase(Locale.getDefault()).contains(search.toLowerCase(Locale.getDefault()))){
                   items.add(it)
               }
            }
            LibUtils.logE("Filter Size ${items.size}")
            this.notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return this.items.size
        }

        inner class MyViewHolder(internal val binding: ListMultiSelectOptionsBinding) : RecyclerView.ViewHolder(binding.root)
    }

    @Parcelize
    class MultiSelect(val item_text: String, val item_id: String, var item_checked: Boolean) : Parcelable

    interface MultiReceiver : Serializable {
        fun receiver(select_items: ArrayList<MultiSelect>)
    }

    interface SingleSelect : Serializable {
        fun receiver(position: Int, select_items: ArrayList<MultiSelect>)
    }
}