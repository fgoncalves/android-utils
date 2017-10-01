package com.github.fgoncalves.pathmanager

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import org.mockito.Mockito

class ScreenNavigatorTest : StringSpec() {
  init {
    val transaction: FragmentTransaction = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)
    val fragmentManager = mock<FragmentManager> {
      on { beginTransaction() } doReturn transaction
      on { backStackEntryCount } doReturn 123
    }
    val container = 123

    "go should add the fragment to the back stack" {
      val navigator = ScreenNavigatorImpl(fragmentManager, container)

      val to = Fragment()
      navigator.go(to)

      verify(transaction).replace(container, to, Fragment::class.java.canonicalName)
    }

    "single should pop the entire back stack and then add the passed in fragment" {
      val navigator = ScreenNavigatorImpl(fragmentManager, container)

      val to = Fragment()
      navigator.single(to)

      verify(fragmentManager, times(123)).popBackStackImmediate()
      verify(transaction).replace(container, to, to::class.java.canonicalName)
    }

    "go should invoke callbacks" {
      val navigator = ScreenNavigatorImpl(fragmentManager, container)
      val callback: (fragment: Fragment) -> Any = mock { }

      val to = Fragment()
      navigator.onScreenAdded(callback)
      navigator.go(to)

      verify(callback).invoke(to)
    }

    "Back should return true and pop back stack when there's more than a screen to pop" {
      val navigator = ScreenNavigatorImpl(fragmentManager, container)

      val result = navigator.back()

      verify(fragmentManager).popBackStackImmediate()
      result shouldBe true
    }

    "Back should return false and should not pop back stack if there's only one screen to be popped or less" {
      val localFragmentManager = mock<FragmentManager> {
        on { backStackEntryCount } doReturn 1
      }
      val navigator = ScreenNavigatorImpl(localFragmentManager, container)

      val result = navigator.back()

      verify(fragmentManager, never()).popBackStackImmediate()
      result shouldBe false
    }
  }
}
