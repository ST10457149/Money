package com.moneygoat.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.moneygoat.app.R
import com.moneygoat.app.data.entity.Category
import com.moneygoat.app.viewmodel.CategoryViewModel

/**
 * Fragment for managing user-defined categories.
 * Users can create new categories and delete existing ones.
 */
class CategoryManagerFragment : Fragment() {
    private lateinit var categoryVM: CategoryViewModel

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View? = 
        inflater.inflate(R.layout.fragment_category_manager, c, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = (requireActivity() as MainActivity).userId
        categoryVM = ViewModelProvider(this)[CategoryViewModel::class.java]

        val etName = view.findViewById<EditText>(R.id.etCategoryName)
        val btnAdd = view.findViewById<Button>(R.id.btnAddCategory)
        val rv = view.findViewById<RecyclerView>(R.id.rvCategories)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyCategories)
        
        rv.layoutManager = LinearLayoutManager(requireContext())

        // Add category button listener
        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) { 
                Toast.makeText(requireContext(), "Enter a category name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener 
            }
            categoryVM.addCategory(name, userId)
            etName.text.clear()
            Toast.makeText(requireContext(), "Category added!", Toast.LENGTH_SHORT).show()
        }

        // Observe category list changes with usage counts
        categoryVM.getCategoriesWithCounts(userId).observe(viewLifecycleOwner) { cats ->
            tvEmpty.visibility = if (cats.isEmpty()) View.VISIBLE else View.GONE
            rv.adapter = CatAdapter(cats) { categoryVM.deleteCategory(it) }
        }
    }

    /**
     * Inner adapter class for displaying categories with counts.
     */
    inner class CatAdapter(private val items: List<com.moneygoat.app.data.entity.CategoryWithCount>, private val onDel: (Category) -> Unit) : RecyclerView.Adapter<CatAdapter.VH>() {
        
        inner class VH(v: View) : RecyclerView.ViewHolder(v) { 
            val tvName: TextView = v.findViewById(R.id.tvCatItemName)
            val btnDel: Button = v.findViewById(R.id.btnDeleteCategory) 
        }

        override fun onCreateViewHolder(p: ViewGroup, vt: Int) = 
            VH(LayoutInflater.from(p.context).inflate(R.layout.item_category, p, false))

        override fun onBindViewHolder(h: VH, pos: Int) { 
            val item = items[pos]
            h.tvName.text = "${item.category.name} (${item.expenseCount} items)"
            
            // Disable delete if category is in use
            h.btnDel.isEnabled = item.expenseCount == 0
            h.btnDel.alpha = if (item.expenseCount == 0) 1.0f else 0.5f

            h.btnDel.setOnClickListener { 
                if (item.expenseCount > 0) {
                    Toast.makeText(requireContext(), "Cannot delete category in use", Toast.LENGTH_SHORT).show()
                } else {
                    onDel(item.category)
                }
            }
        }

        override fun getItemCount() = items.size
    }
}
