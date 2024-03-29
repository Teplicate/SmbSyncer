package ru.teplicate.datasyncersmb.fragment.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.teplicate.datasyncersmb.R

abstract class AbstractMasterDetailFragment(
) : Fragment() {
    protected abstract val layoutId: Int
    private var isTablet: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isTablet = resources.getBoolean(R.bool.is_tablet)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val r = bindViews(inflater)
        setupViews()
        return r
    }

    abstract fun bindViews(layoutInflater: LayoutInflater): View

    abstract fun setupViews()
}