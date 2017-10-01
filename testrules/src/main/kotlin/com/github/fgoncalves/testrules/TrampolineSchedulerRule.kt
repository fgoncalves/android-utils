package com.github.fgoncalves.testrules

import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * A rule that ensures the test case runs always on the trampoline scheduler.
 *
 * In other words, it makes sure that every rx stream runs in the JUnit thread.
 *
 * This is essential for testing since we need to guarantee the test doesn't finish
 * before the actual subscriptions run
 *
 * The rule makes sure to reset everything to the proper schedulers
 */
class TrampolineSchedulerRule : TestRule {
    private val scheduler by lazy { Schedulers.trampoline() }

    override fun apply(base: Statement?, description: Description?): Statement =
            object : Statement() {
                override fun evaluate() {
                    try {
                        RxJavaPlugins.setComputationSchedulerHandler { scheduler }
                        RxJavaPlugins.setIoSchedulerHandler { scheduler }
                        RxJavaPlugins.setNewThreadSchedulerHandler { scheduler }
                        RxJavaPlugins.setSingleSchedulerHandler { scheduler }
                        RxAndroidPlugins.setInitMainThreadSchedulerHandler { scheduler }
                        base?.evaluate()
                    } finally {
                        RxJavaPlugins.reset()
                        RxAndroidPlugins.reset()
                    }
                }
            }
}
