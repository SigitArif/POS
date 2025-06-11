package com.example.pos.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.pos.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DateRangeDialog : DialogFragment() {
    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var btnApply: Button
    private lateinit var btnCancel: Button

    private var startDate: Date? = null
    private var endDate: Date? = null
    private var onDateRangeSelected: ((Date, Date) -> Unit)? = null

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_date_range)
            setupViews()
        }
    }

    private fun Dialog.setupViews() {
        tvStartDate = findViewById(R.id.tvStartDate)
        tvEndDate = findViewById(R.id.tvEndDate)
        btnApply = findViewById(R.id.btnApply)
        btnCancel = findViewById(R.id.btnCancel)

        tvStartDate.setOnClickListener { showDatePicker(true) }
        tvEndDate.setOnClickListener { showDatePicker(false) }

        btnApply.setOnClickListener {
            if (startDate != null && endDate != null) {
                onDateRangeSelected?.invoke(startDate!!, endDate!!)
                dismiss()
            }
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                if (isStartDate) {
                    startDate = calendar.time
                    tvStartDate.text = dateFormat.format(calendar.time)
                } else {
                    endDate = calendar.time
                    tvEndDate.text = dateFormat.format(calendar.time)
                }
            },
            year,
            month,
            day
        ).show()
    }

    companion object {
        fun newInstance(
            initialStartDate: Date? = null,
            initialEndDate: Date? = null,
            onDateRangeSelected: (Date, Date) -> Unit
        ): DateRangeDialog {
            return DateRangeDialog().apply {
                this.startDate = initialStartDate
                this.endDate = initialEndDate
                this.onDateRangeSelected = onDateRangeSelected
            }
        }
    }
} 