package com.example.esp32home

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import java.util.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val themeSpinner: AutoCompleteTextView = view.findViewById(R.id.themeSpinner)
        val languageSpinner: AutoCompleteTextView = view.findViewById(R.id.languageSpinner)

        val themes = listOf("Світла тема", "Темна тема")
        val languages = listOf("Українська", "English")

        themeSpinner.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, themes))
        languageSpinner.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, languages))

        themeSpinner.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }

        languageSpinner.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> setLocale("uk")
                1 -> setLocale("en")
            }
        }

        return view
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        requireActivity().resources.updateConfiguration(config, requireActivity().resources.displayMetrics)
        requireActivity().recreate()
    }
}
