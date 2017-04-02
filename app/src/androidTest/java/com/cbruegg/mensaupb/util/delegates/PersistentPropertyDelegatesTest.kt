package com.cbruegg.mensaupb.util.delegates

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.model.RunnerBuilder
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(PersistentPropertyDelegatesTest::class)
@Suite.SuiteClasses(PersistentPropertyDelegatesTest.Nullable::class, PersistentPropertyDelegatesTest.NonNull::class)
class PersistentPropertyDelegatesTest(clazz: Class<*>, builder: RunnerBuilder) : Suite(clazz, builder) {

    @RunWith(AndroidJUnit4::class)
    class Nullable {
        private val context = InstrumentationRegistry.getContext()
        private val sharedPrefs = context.getSharedPreferences("testNullableString", Context.MODE_PRIVATE)
        private var delegated by StringSharedPreferencesPropertyDelegate<String?>(sharedPrefs, "testKey", null)

        @Test fun testNullableString() {
            assertNull(delegated, "Value should be null-initialized")
            delegated = "abc"
            assertEquals("abc", delegated, "Should be changed")
        }
    }

    @RunWith(AndroidJUnit4::class)
    class NonNull {
        private val context = InstrumentationRegistry.getContext()
        private val sharedPrefs = context.getSharedPreferences("testNullableString", Context.MODE_PRIVATE)
        private var delegated by StringSharedPreferencesPropertyDelegate<String>(sharedPrefs, "testKey", "nonnull")

        @Test fun testNonNullString() {
            assertEquals("nonnull", delegated, "Should be non-null")
            delegated = "abc"
            assertEquals("abc", delegated, "Should be changed")
        }
    }


}