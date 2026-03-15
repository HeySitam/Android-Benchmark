package dev.heysitam.benchmark

import android.os.Build
import androidx.annotation.IntRange
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.Metric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Wrapper around [MacrobenchmarkRule] that skips the WRITE_EXTERNAL_STORAGE grant
 * on Android 13+ (API 33+) where it can no longer be granted for apps with targetSdk >= 33.
 *
 * Root cause: [MacrobenchmarkRule.apply] unconditionally wraps the test with
 * GrantPermissionRule.grant(WRITE_EXTERNAL_STORAGE) without any Android version check.
 * On Android 13+, this grant always fails, aborting the test before it even runs.
 *
 * This wrapper bypasses the broken GrantPermissionRule on Android 13+ by directly
 * invoking the internal applyInternal method via reflection.
 */
class CompatMacrobenchmarkRule : TestRule {

    private val delegate = MacrobenchmarkRule()

    @JvmOverloads
    fun measureRepeated(
        packageName: String,
        metrics: List<Metric>,
        compilationMode: CompilationMode = CompilationMode.DEFAULT,
        startupMode: StartupMode? = null,
        @IntRange(from = 1) iterations: Int,
        setupBlock: MacrobenchmarkScope.() -> Unit = {},
        measureBlock: MacrobenchmarkScope.() -> Unit
    ) = delegate.measureRepeated(
        packageName = packageName,
        metrics = metrics,
        compilationMode = compilationMode,
        startupMode = startupMode,
        iterations = iterations,
        setupBlock = setupBlock,
        measureBlock = measureBlock
    )

    override fun apply(base: Statement, description: Description): Statement {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // On Android 13+, WRITE_EXTERNAL_STORAGE is deprecated and cannot be granted
            // via pm grant or UiAutomation.grantRuntimePermission() for apps with targetSdk >= 33.
            // Bypass MacrobenchmarkRule's GrantPermissionRule by directly invoking applyInternal,
            // which sets currentDescription and checks enabledRules — everything we actually need.
            val applyInternal = MacrobenchmarkRule::class.java
                .getDeclaredMethod(
                    "applyInternal",
                    Statement::class.java,
                    Description::class.java
                )
                .also { it.isAccessible = true }

            applyInternal.invoke(delegate, base, description) as Statement
        } else {
            delegate.apply(base, description)
        }
    }
}
