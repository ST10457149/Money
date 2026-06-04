package com.moneygoat.app.ui

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.moneygoat.app.R
import com.moneygoat.app.adapter.CategoryTotalAdapter
import com.moneygoat.app.data.entity.CategoryTotal
import com.moneygoat.app.viewmodel.ExpenseViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Fragment that displays aggregated spending data per category and account trends.
 * Includes PDF export functionality for financial reporting.
 */
class CategoryTotalsFragment : Fragment() {
    private lateinit var expenseVM: ExpenseViewModel
    private lateinit var adapter: CategoryTotalAdapter
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var lineChart: LineChart
    private lateinit var spinnerAccount: Spinner
    private var accounts = listOf<com.moneygoat.app.data.entity.Account>()
    private var selectedAccountId = -1L
    private var startDate = ""
    private var endDate = ""

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View? = 
        inflater.inflate(R.layout.fragment_category_totals, c, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = (requireActivity() as MainActivity).userId
        expenseVM = ViewModelProvider(this)[ExpenseViewModel::class.java]
        adapter = CategoryTotalAdapter()

        val btnStart = view.findViewById<Button>(R.id.btnCatFilterStart)
        val btnEnd = view.findViewById<Button>(R.id.btnCatFilterEnd)
        val btnApply = view.findViewById<Button>(R.id.btnCatApplyFilter)
        val rv = view.findViewById<RecyclerView>(R.id.rvCategoryTotals)
        val btnExport = view.findViewById<Button>(R.id.btnExportPdf)
        val btnExportCsv = view.findViewById<Button>(R.id.btnExportCsv)
        
        pieChart = view.findViewById(R.id.pieChart)
        barChart = view.findViewById(R.id.barChart)
        lineChart = view.findViewById(R.id.lineChart)
        spinnerAccount = view.findViewById(R.id.spinnerAccountFilter)
        
        setupPieChart()
        setupBarChart()
        setupLineChart()

        // Setup RecyclerView
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // Populate Account Spinner - Hidden for demo
        /*
        expenseVM.getAccounts(userId).observe(viewLifecycleOwner) { accs ->
            accounts = accs
            val names = mutableListOf("All Accounts")
            names.addAll(accs.map { "${it.name} (${it.type})" })
            val aa = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerAccount.adapter = aa
        }
        */

        spinnerAccount.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                // selectedAccountId = if (position == 0) -1L else accounts[position - 1].id
                selectedAccountId = -1L // Always use all accounts for demo
                loadData(userId)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        btnExport.setOnClickListener { exportToPdf() }
        btnExportCsv.setOnClickListener { exportToCsv() }

        // Set initial filter to current month
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        startDate = "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, 1)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        endDate = "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        
        btnStart.text = startDate
        btnEnd.text = endDate

        btnStart.setOnClickListener { 
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d -> 
                startDate = "%04d-%02d-%02d".format(y, m + 1, d)
                btnStart.text = startDate 
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() 
        }
        
        btnEnd.setOnClickListener { 
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d -> 
                endDate = "%04d-%02d-%02d".format(y, m + 1, d)
                btnEnd.text = endDate 
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() 
        }
        
        btnApply.setOnClickListener { loadData(userId) }
        loadData(userId)
    }

    private fun loadData(userId: Long) {
        expenseVM.setDateFilter(userId, startDate, endDate, selectedAccountId)
        
        expenseVM.categoryTotals.observe(viewLifecycleOwner) { totals ->
            adapter.submitList(totals)
            updatePieChart(totals)
            updateBarChart(totals)
            view?.findViewById<TextView>(R.id.tvEmptyCat)?.visibility = if (totals.isEmpty()) View.VISIBLE else View.GONE
        }
        
        val trendSource = if (selectedAccountId == -1L) {
            expenseVM.getAllExpenses(userId)
        } else {
            expenseVM.getExpensesByAccount(userId, selectedAccountId)
        }

        trendSource.observe(viewLifecycleOwner) { expenses ->
            val filtered = expenses.filter { it.date >= startDate && it.date <= endDate }
                .sortedBy { it.date }
            updateLineChart(filtered)
            generateInsights(filtered)
        }
        
        expenseVM.totalSpent.observe(viewLifecycleOwner) { total ->
            val totalVal = total ?: 0.0
            view?.findViewById<TextView>(R.id.tvGrandTotal)?.text = "Total: R %.2f".format(totalVal)
        }

        expenseVM.filteredExpensesWithCategory.observe(viewLifecycleOwner) { items ->
            if (items.isNotEmpty()) {
                val amounts = items.map { it.expense.amount }
                val min = amounts.minOrNull() ?: 0.0
                val max = amounts.maxOrNull() ?: 0.0
                view?.findViewById<TextView>(R.id.tvMinMaxDisplay)?.text = 
                    "Min Transaction: R %.2f | Max Transaction: R %.2f".format(min, max)
            } else {
                view?.findViewById<TextView>(R.id.tvMinMaxDisplay)?.text = "Min: R 0.00 | Max: R 0.00"
            }
        }
    }

    private fun generateInsights(expenses: List<com.moneygoat.app.data.entity.Expense>) {
        if (expenses.isEmpty()) {
            view?.findViewById<TextView>(R.id.tvTrendInsights)?.text = "Start logging expenses to see AI-powered insights."
            return
        }
        val tvInsights = view?.findViewById<TextView>(R.id.tvTrendInsights) ?: return
        
        val dailyTotals = expenses.groupBy { it.date }
            .mapValues { it.value.sumOf { exp -> exp.amount } }
            .toSortedMap()
            
        val values = dailyTotals.values.toList()
        val totalSpent = values.sum()
        val avgDaily = totalSpent / values.size.coerceAtLeast(1)
        
        // Predictive Analytics: Project next month's spending
        val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
        val forecast = avgDaily * daysInMonth

        val trendText = if (values.size >= 2) {
            val firstHalf = values.take(values.size / 2).average()
            val secondHalf = values.takeLast(values.size / 2).average()
            if (secondHalf > firstHalf) {
                "⚠️ Spending is trending UP (↑${"%.1f".format(((secondHalf-firstHalf)/firstHalf)*100)}%)."
            } else {
                "✅ Spending is trending DOWN (↓${"%.1f".format(((firstHalf-secondHalf)/firstHalf)*100)}%)."
            }
        } else ""

        tvInsights.text = "Insight: $trendText\nForecast: R ${"%.2f".format(forecast)} projected for next month based on current velocity."
    }

    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            legend.isEnabled = false
        }
    }

    private fun updatePieChart(totals: List<CategoryTotal>) {
        val entries = totals.map { PieEntry(it.totalAmount.toFloat(), it.categoryName) }
        val dataSet = PieDataSet(entries, "Spending by Category").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 14f
            valueTextColor = Color.BLACK
        }
        pieChart.data = PieData(dataSet)
        pieChart.invalidate()
    }

    private fun setupBarChart() {
        barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = Color.BLACK
            }
            axisLeft.textColor = Color.BLACK
            axisRight.isEnabled = false
            legend.isEnabled = false
        }
    }

    private fun updateBarChart(totals: List<CategoryTotal>) {
        val entries = totals.mapIndexed { index, total -> BarEntry(index.toFloat(), total.totalAmount.toFloat()) }
        val labels = totals.map { it.categoryName }
        val dataSet = BarDataSet(entries, "Spending").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 12f
            valueTextColor = Color.BLACK
        }
        
        barChart.apply {
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            data = BarData(dataSet)
            
            // Enterprise Feature: Highlight Min/Max categories visually
            val maxVal = totals.maxByOrNull { it.totalAmount }?.totalAmount?.toFloat() ?: 0f
            axisLeft.removeAllLimitLines()
            if (maxVal > 0) {
                val line = LimitLine(maxVal, "Highest Category").apply {
                    lineWidth = 2f
                    enableDashedLine(10f, 10f, 0f)
                    labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                    textSize = 10f
                    lineColor = Color.RED
                }
                axisLeft.addLimitLine(line)
            }
            invalidate()
        }
    }

    private fun setupLineChart() {
        lineChart.apply {
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            axisRight.isEnabled = false
            legend.isEnabled = true
        }
    }

    private fun updateLineChart(expenses: List<com.moneygoat.app.data.entity.Expense>) {
        val dailyTotals = expenses.groupBy { it.date }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toSortedMap()

        val entries = dailyTotals.values.mapIndexed { index, total -> Entry(index.toFloat(), total.toFloat()) }
        val dataSet = LineDataSet(entries, "Daily Spending").apply {
            color = Color.BLUE
            setCircleColor(Color.BLUE)
            lineWidth = 2f
            setDrawFilled(true)
            fillColor = Color.CYAN
        }
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(dailyTotals.keys.toList())
        lineChart.data = LineData(dataSet)
        lineChart.invalidate()
    }

    private fun exportToPdf() {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // Title
        paint.textSize = 28f
        paint.isFakeBoldText = true
        paint.color = Color.BLACK
        canvas.drawText("MoneyGoat Financial Report", 50f, 60f, paint)

        // Subtitle / Date
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = Color.GRAY
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Generated on: $timestamp", 50f, 85f, paint)

        // Period
        paint.textSize = 14f
        paint.isFakeBoldText = true
        paint.color = Color.BLACK
        canvas.drawText("Report Period: $startDate to $endDate", 50f, 115f, paint)

        // Divider
        paint.strokeWidth = 2f
        canvas.drawLine(50f, 130f, 545f, 130f, paint)

        var y = 165f
        paint.textSize = 18f
        canvas.drawText("Category Breakdown", 50f, y, paint)
        y += 35f

        paint.textSize = 14f
        paint.isFakeBoldText = false
        
        // Table Headers
        paint.isFakeBoldText = true
        canvas.drawText("Category", 70f, y, paint)
        canvas.drawText("Amount", 400f, y, paint)
        y += 10f
        canvas.drawLine(70f, y, 500f, y, paint)
        y += 25f
        paint.isFakeBoldText = false

        adapter.currentList.forEach { 
            canvas.drawText(it.categoryName, 70f, y, paint)
            canvas.drawText("R ${"%.2f".format(it.totalAmount)}", 400f, y, paint)
            y += 25f
            
            if (y > 750f) { // Basic overflow protection
                return@forEach 
            }
        }

        // Grand Total
        y += 20f
        paint.isFakeBoldText = true
        paint.textSize = 16f
        val grandTotal = expenseVM.totalSpent.value ?: 0.0
        canvas.drawText("Total Spending: R ${"%.2f".format(grandTotal)}", 50f, y, paint)

        // Footer
        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.color = Color.LTGRAY
        canvas.drawText("End of Report - MoneyGoat Intelligence Engine", 50f, 800f, paint)

        pdfDocument.finishPage(page)

        val file = File(requireContext().getExternalFilesDir(null), "MoneyGoat_Report_${System.currentTimeMillis()}.pdf")
        try {
            pdfDocument.writeTo(file.outputStream())
            Toast.makeText(requireContext(), "PDF Saved: ${file.name}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun exportToCsv() {
        val file = File(requireContext().getExternalFilesDir(null), "Transactions_${System.currentTimeMillis()}.csv")
        try {
            file.printWriter().use { out ->
                out.println("Category,Total Amount")
                adapter.currentList.forEach { 
                    out.println("${it.categoryName},${it.totalAmount}")
                }
            }
            Toast.makeText(requireContext(), "CSV Exported: ${file.name}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "CSV Export failed", Toast.LENGTH_SHORT).show()
        }
    }
}
