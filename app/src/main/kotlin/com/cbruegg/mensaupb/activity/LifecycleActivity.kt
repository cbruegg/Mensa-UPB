package com.cbruegg.mensaupb.activity

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.support.v7.app.AppCompatActivity

abstract class LifecycleActivity: AppCompatActivity(), LifecycleRegistryOwner {
    override fun getLifecycle() = LifecycleRegistry(this)
}