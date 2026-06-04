package com.moneygoat.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.moneygoat.app.R
import com.moneygoat.app.data.entity.Category
import com.moneygoat.app.data.entity.Expense
import com.moneygoat.app.viewmodel.CategoryViewModel
import com.moneygoat.app.viewmodel.ExpenseViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment for adding a new expense record.
 * Includes form fields for description, amount, date, time range, and category selection.
 * Also supports taking a photo of the receipt.
 */
class AddExpenseFragment : Fragment() {
    private val TAG = "MoneyGoat_AddExpenseUI"
    private lateinit var expenseVM: ExpenseViewModel
    private lateinit var categoryVM: CategoryViewModel
    private var selectedDate = ""
    private var selectedStartTime = ""
    private var selectedEndTime = ""
    private var photoUri: Uri? = null
    private var photoPath: String? = null
    private var categories = listOf<Category>()

    // Register activity result for camera permission request
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Log.d(TAG, "Camera permission granted")
            launchCamera()
        } else {
            Log.w(TAG, "Camera permission denied")
            Toast.makeText(requireContext(), "Camera permission required", Toast.LENGTH_SHORT)
                .show()
        }
    }

    // Register activity result for taking a picture
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) {
                Log.d(TAG, "Photo captured successfully: $photoPath")
                view?.findViewById<ImageView>(R.id.ivPhoto)
                    ?.let { it.setImageURI(photoUri); it.visibility = View.VISIBLE }
                Toast.makeText(requireContext(), "Photo captured!", Toast.LENGTH_SHORT).show()
                scanReceiptForAmount()
            } else {
                Log.w(TAG, "Photo capture failed or cancelled")
            }
        }

    /**
     * Uses ML Kit to scan the receipt for a "Total" amount and keywords.
     */
    private fun scanReceiptForAmount() {
        val uri = photoUri ?: return
        Toast.makeText(requireContext(), "Scanning receipt...", Toast.LENGTH_SHORT).show()
        
        try {
            val image = InputImage.fromFilePath(requireContext(), uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val fullText = visionText.text.uppercase()
                    
                    // Regex for currency amounts (supports R, $, etc. if followed by numbers)
                    val amountRegex = Regex("""(?:R|\$)?\s?(\d+[.,]\d{2})""")
                    val matches = amountRegex.findAll(visionText.text)
                    val amounts = matches.map { 
                        it.groupValues[1].replace(",", ".").toDoubleOrNull() ?: 0.0 
                    }.toList()
                    
                    if (amounts.isNotEmpty()) {
                        // In many receipts, the Total is the largest number
                        // But we can also look for lines containing "TOTAL", "AMOUNT", "DUE"
                        var finalAmount = amounts.maxOrNull() ?: 0.0
                        
                        // Refinement: if we find "TOTAL" followed by a number, that's likely it
                        visionText.textBlocks.forEach { block ->
                            val blockText = block.text.uppercase()
                            if (blockText.contains("TOTAL") || blockText.contains("DUE") || blockText.contains("BALANCE")) {
                                val lineMatch = amountRegex.find(block.text)
                                if (lineMatch != null) {
                                    finalAmount = lineMatch.groupValues[1].replace(",", ".").toDoubleOrNull() ?: finalAmount
                                }
                            }
                        }

                        if (finalAmount > 0) {
                            view?.findViewById<EditText>(R.id.etAmount)?.setText("%.2f".format(finalAmount))
                            Toast.makeText(requireContext(), "Detected Total: R%.2f".format(finalAmount), Toast.LENGTH_LONG).show()
                        }
                    }
                    
                    // Intelligent Category & Description Prediction
                    val etDesc = view?.findViewById<EditText>(R.id.etDescription)
                    val spinner = view?.findViewById<Spinner>(R.id.spinnerCategory)
                    
                    val keywordMap = mapOf(
                        "FOOD" to listOf("MC DONALD", "KFC", "BURGER", "STEERS", "DEBONAIRS", "PIZZA", "RESTAURANT"),
                        "GROCERIES" to listOf("PICK N PAY", "CHECKERS", "WOOLWORTHS", "SPAR", "SHOPRITE"),
                        "TRANSPORT" to listOf("UBER", "BOLT", "SHELL", "ENGEN", "TOTAL", "BP", "GARAGE"),
                        "HEALTH" to listOf("CLICKS", "DIS-CHEM", "PHARMACY", "DOCTOR", "MEDICINE"),
                        "ENTERTAINMENT" to listOf("NETFLIX", "SPOTIFY", "CINEMA", "STEAM", "PLAYSTATION")
                    )
                    
                    var matchedCategoryName: String? = null
                    
                    for ((cat, keywords) in keywordMap) {
                        if (keywords.any { fullText.contains(it) }) {
                            etDesc?.setText(cat.lowercase().replaceFirstChar { it.uppercase() })
                            matchedCategoryName = cat
                            break
                        }
                    }
                    
                    // If matched a category, try to select it in the spinner
                    matchedCategoryName?.let { name ->
                        val index = categories.indexOfFirst { it.name.equals(name, ignoreCase = true) }
                        if (index != -1) {
                            spinner?.setSelection(index)
                        }
                    }
                    
                    if (etDesc?.text.isNullOrEmpty()) {
                        // Extract first line as a fallback description
                        val firstLine = visionText.textBlocks.firstOrNull()?.text?.lines()?.firstOrNull()
                        if (firstLine != null && firstLine.length < 30) {
                            etDesc?.setText(firstLine)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "OCR failed", e)
                    Toast.makeText(requireContext(), "Could not read receipt. Please enter manually.", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process receipt image", e)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View? =
        inflater.inflate(R.layout.fragment_add_expense, c, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "AddExpenseFragment created")
        val userId = (requireActivity() as MainActivity).userId
        expenseVM = ViewModelProvider(this)[ExpenseViewModel::class.java]
        categoryVM = ViewModelProvider(this)[CategoryViewModel::class.java]

        val etDesc = view.findViewById<EditText>(R.id.etDescription)
        val etAmt = view.findViewById<EditText>(R.id.etAmount)
        val btnDate = view.findViewById<Button>(R.id.btnSelectDate)
        val btnStart = view.findViewById<Button>(R.id.btnStartTime)
        val btnEnd = view.findViewById<Button>(R.id.btnEndTime)
        val spinner = view.findViewById<Spinner>(R.id.spinnerCategory)
        val btnPhoto = view.findViewById<Button>(R.id.btnTakePhoto)
        val btnSave = view.findViewById<Button>(R.id.btnSaveExpense)
        val spinnerAccount = view.findViewById<Spinner>(R.id.spinnerAccount)
        spinnerAccount.visibility = View.GONE // Hide account selection

        // Populate the category spinner from the database
        categoryVM.getCategories(userId).observe(viewLifecycleOwner) { cats ->
            categories = cats
            val names =
                if (cats.isEmpty()) listOf("No categories - create one first") else cats.map { it.name }
            spinner.adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names).also {
                    it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
        }

        // Date Picker listener
        btnDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    selectedDate = "%04d-%02d-%02d".format(y, m + 1, d)
                    btnDate.text = selectedDate
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        
        // Start Time Picker listener
        btnStart.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                { _, h, m ->
                    selectedStartTime = "%02d:%02d".format(h, m)
                    btnStart.text = selectedStartTime
                },
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
            ).show()
        }
        
        // End Time Picker listener
        btnEnd.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                { _, h, m ->
                    selectedEndTime = "%02d:%02d".format(h, m)
                    btnEnd.text = selectedEndTime
                },
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
            ).show()
        }
        
        // Photo button listener with permission check
        btnPhoto.setOnClickListener {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.CAMERA
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                launchCamera()
            } else {
                requestCameraPermission.launch(android.Manifest.permission.CAMERA)
            }
        }
        
        // Save expense button listener with validation
        btnSave.setOnClickListener {
            val desc = etDesc.text.toString().trim()
            val amtStr = etAmt.text.toString().trim()
            Log.d(TAG, "Save expense button clicked. Desc: $desc, Amount: $amtStr")
            
            if (desc.isEmpty() || amtStr.isEmpty() || selectedDate.isEmpty() || selectedStartTime.isEmpty() || selectedEndTime.isEmpty()) {
                Log.w(TAG, "Expense validation failed: empty fields")
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            if (categories.isEmpty()) {
                Log.w(TAG, "Expense validation failed: no categories")
                Toast.makeText(requireContext(), "Create a category first", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            val amt = amtStr.toDoubleOrNull()
            if (amt == null || amt <= 0) {
                Log.w(TAG, "Expense validation failed: invalid amount $amtStr")
                Toast.makeText(requireContext(), "Enter a valid amount", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            
            Log.d(TAG, "Validated expense. Adding via ViewModel.")
            expenseVM.addExpense(
                Expense(
                    date = selectedDate,
                    startTime = selectedStartTime,
                    endTime = selectedEndTime,
                    description = desc,
                    amount = amt,
                    categoryId = categories[spinner.selectedItemPosition].id,
                    userId = userId,
                    photoPath = photoPath
                )
            )
            Toast.makeText(requireContext(), "Expense saved successfully!", Toast.LENGTH_SHORT).show()
            
            // Reset fields after saving
            etDesc.text.clear()
            etAmt.text.clear()
            selectedDate = ""
            selectedStartTime = ""
            selectedEndTime = ""
            photoPath = null
            photoUri = null
            btnDate.text = "Select Date"
            btnStart.text = "Start Time"
            btnEnd.text = "End Time"
            view.findViewById<ImageView>(R.id.ivPhoto).visibility = View.GONE
        }
    }

    /**
     * Creates a temporary file and launches the camera app to capture a photo.
     */
    private fun launchCamera() {
        val file = java.io.File.createTempFile(
            "EXPENSE_${
                java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                    .format(java.util.Date())
            }_",
            ".jpg", requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        )
        photoPath = file.absolutePath
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
        takePicture.launch(photoUri!!)
    }
}
