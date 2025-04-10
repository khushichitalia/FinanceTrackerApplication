package com.example.financetrackerapplication.ui.home

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.example.financetrackerapplication.R
import java.util.*

class MonthYearPickerDialog : DialogFragment() {

    private var listener: DatePickerDialog.OnDateSetListener? = null

    fun setListener(listener: DatePickerDialog.OnDateSetListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView: View = inflater.inflate(R.layout.date_picker_dialog, null)

        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.picker_month)
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.picker_year)

        val cal = Calendar.getInstance()

        monthPicker.minValue = 0
        monthPicker.maxValue = 11
        monthPicker.displayedValues = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        monthPicker.value = cal.get(Calendar.MONTH)

        val year = cal.get(Calendar.YEAR)
        yearPicker.minValue = 1980
        yearPicker.maxValue = 2099
        yearPicker.value = year

        builder.setView(dialogView)
            .setPositiveButton("OK") { _: DialogInterface, _: Int ->
                listener?.onDateSet(null, yearPicker.value, monthPicker.value, 1)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        return builder.create()
    }
}
