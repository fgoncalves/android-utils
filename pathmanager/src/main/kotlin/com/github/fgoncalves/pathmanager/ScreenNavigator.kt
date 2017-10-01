package com.github.fgoncalves.pathmanager

import android.os.Build
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.transition.Transition
import android.view.View

typealias onScreenAdded = (screen: Fragment) -> Any

/**
 * Responsible for navigating between screens keeping the history and managing the stack
 */
interface ScreenNavigator {
  /**
   * Go to the given screen applying the given transitions. The screen will be added to the history
   */
  fun go(
      to: Fragment,
      from: Fragment? = null,
      enterTransition: Transition? = null,
      enterSharedTransition: Transition? = null,
      exitTransition: Transition? = null,
      exitSharedTransition: Transition? = null,
      sharedElement: View? = null,
      sharedElementTransactionName: String? = null)

  /**
   * Clear the history and add the given screen to the history
   */
  fun single(screen: Fragment)

  /**
   * Remove the last screen from the history.
   *
   * @return False if there's no more screens to go back. True otherwise
   */
  fun back(): Boolean

  /**
   * Set the callback used for when the screen is added
   */
  fun onScreenAdded(callback: onScreenAdded?)
}

class ScreenNavigatorImpl(
    val fragmentManager: FragmentManager,
    @IdRes val container: Int) : ScreenNavigator {

  var onScreenAddedCallback: onScreenAdded? = null

  override fun go(to: Fragment, from: Fragment?, enterTransition: Transition?,
      enterSharedTransition: Transition?, exitTransition: Transition?,
      exitSharedTransition: Transition?, sharedElement: View?,
      sharedElementTransactionName: String?) {
    val fragmentTransaction = fragmentManager.beginTransaction()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      if (exitSharedTransition != null) from?.sharedElementReturnTransition = exitSharedTransition
      if (enterSharedTransition != null) to.sharedElementEnterTransition = enterSharedTransition
      if (exitTransition != null) from?.exitTransition = exitTransition
      if (enterTransition != null) to.enterTransition = enterTransition
      if (sharedElement != null && sharedElementTransactionName != null) {
        fragmentTransaction.addSharedElement(sharedElement, sharedElementTransactionName)
      }
    }

    val canonicalName = to.javaClass.canonicalName
    fragmentTransaction.replace(container, to, canonicalName)
        .addToBackStack(canonicalName)
        .commit()

    onScreenAddedCallback?.invoke(to)
  }

  override fun single(screen: Fragment) {
    fragmentManager.clear()
    go(screen)
  }

  override fun back(): Boolean {
    if (fragmentManager.backStackEntryCount <= 1) return false
    fragmentManager.popBackStackImmediate()
    return true
  }

  override fun onScreenAdded(callback: onScreenAdded?) {
    onScreenAddedCallback = callback
  }

  /**
   * Clear the stack immediately
   */
  private fun FragmentManager.clear() {
    for (i in 1..backStackEntryCount)
      popBackStackImmediate()
  }
}
