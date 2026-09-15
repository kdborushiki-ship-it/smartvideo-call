package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TalkText AI", appName)
  }

  @Test
  fun `email login updates repository user state`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.data.DemoRepository.getInstance(context)
    repo.loginWithEmail("kdborushiki@gmail.com", name = "Borushiki")
    val currentUser = repo.currentUser.value
    assertEquals("kdborushiki@gmail.com", currentUser?.email)
    assertEquals("Borushiki", currentUser?.name)
  }
}
