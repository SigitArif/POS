package com.example.pos.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.pos.R
import com.example.pos.data.local.AppDatabase
import com.example.pos.data.repository.CategoryRepositoryImpl
import com.example.pos.data.repository.ProductRepositoryImpl
import com.example.pos.databinding.DialogSettingsBinding
import com.example.pos.viewmodel.CategoryViewModel
import com.example.pos.viewmodel.ProductViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream

class SettingsDialog : DialogFragment() {
    private var _binding: DialogSettingsBinding? = null
    private val binding get() = _binding!!

    private val productViewModel: ProductViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val productRepository = ProductRepositoryImpl(database.productDao())
        val categoryRepository = CategoryRepositoryImpl(database.categoryDao())
        ProductViewModel.Factory(productRepository, categoryRepository)
    }

    private val categoryViewModel: CategoryViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = CategoryRepositoryImpl(database.categoryDao())
        CategoryViewModel.Factory(repository)
    }

    private val exportDbLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                exportDatabase(uri)
            }
        }
    }

    private val importDbLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                importDatabase(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_MaterialComponents_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
        observeCategories()
    }

    private fun observeCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                categoryViewModel.categories.collectLatest { categories ->
                    // Update UI if needed
                }
            }
        }
    }

    private fun setupButtons() {
        binding.btnExportDb.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/x-sqlite3"
                putExtra(Intent.EXTRA_TITLE, "pos_database_backup.db")
            }
            exportDbLauncher.launch(intent)
        }

        binding.btnImportDb.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/x-sqlite3"
            }
            importDbLauncher.launch(intent)
        }

        binding.btnDeleteCategory.setOnClickListener {
            showDeleteCategoryDialog()
        }
    }

    private fun exportDatabase(uri: Uri) {
        try {
            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                val dbFile = requireContext().getDatabasePath("pos_database")
                FileInputStream(dbFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Toast.makeText(context, "Database exported successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to export database: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun importDatabase(uri: Uri) {
        try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                val dbFile = requireContext().getDatabasePath("pos_database")
                FileOutputStream(dbFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Toast.makeText(context, "Database imported successfully. Please restart the app.", Toast.LENGTH_LONG).show()
            dismiss()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to import database: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteCategoryDialog() {
        val categories = categoryViewModel.categories.value.filter { it.name != "ALL" }
        if (categories.isEmpty()) {
            Toast.makeText(context, "No categories to delete", Toast.LENGTH_SHORT).show()
            return
        }

        val categoryNames = categories.map { it.name }
        val items = categoryNames.toTypedArray()

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Category")
            .setItems(items) { _, which ->
                val categoryToDelete = items[which]
                checkAndDeleteCategory(categoryToDelete)
            }
            .show()
    }

    private fun checkAndDeleteCategory(category: String) {
        val productsInCategory = productViewModel.products.value.filter { it.category.equals(category, ignoreCase = true) }
        
        if (productsInCategory.isNotEmpty()) {
            Toast.makeText(
                context,
                "Cannot delete category. Please delete all products in this category first.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                categoryViewModel.deleteCategory(category)
                Toast.makeText(context, "Category deleted successfully", Toast.LENGTH_SHORT).show()
                dismiss() // Close the dialog after successful deletion
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to delete category: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = SettingsDialog()
    }
} 