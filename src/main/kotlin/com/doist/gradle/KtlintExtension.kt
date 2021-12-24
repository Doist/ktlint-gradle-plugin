package com.doist.gradle

open class KtlintExtension {
    var android: Boolean = false
    var disabledRules: List<String>? = null
    var mainBranch: String = "main"
    var version: String? = null
}
