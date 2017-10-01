package com.github.fgoncalves.testrules

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runners.model.Statement

@Suppress("IllegalIdentifier")
class TrampolineSchedulerRuleTest {
    private val rule = TrampolineSchedulerRule()

    @Test
    fun `applying the rule should change all schedulers to trampoline during test`() {
        rule.apply(object : Statement() {
            override fun evaluate() {
                Schedulers.computation() shouldBe Schedulers.trampoline()
                Schedulers.io() shouldBe Schedulers.trampoline()
                Schedulers.newThread() shouldBe Schedulers.trampoline()
                Schedulers.single() shouldBe Schedulers.trampoline()
            }
        }, null).evaluate()

        Schedulers.computation() shouldNotBe Schedulers.trampoline()
        Schedulers.io() shouldNotBe Schedulers.trampoline()
        Schedulers.newThread() shouldNotBe Schedulers.trampoline()
        Schedulers.single() shouldNotBe Schedulers.trampoline()
    }

    private infix fun Scheduler.shouldBe(expected: Scheduler) {
        assertThat(this)
                .overridingErrorMessage("Schedulers differ. Got $this expected $expected")
                .isEqualTo(expected)
    }

    private infix fun Scheduler.shouldNotBe(expected: Scheduler) {
        assertThat(this)
                .overridingErrorMessage("Schedulers are equal. Got $this")
                .isNotEqualTo(expected)
    }
}
