package com.doist.gradle.tasks

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.OutputFile
import java.io.File

abstract class ExecWithResultTask : Exec() {
    @get:OutputFile
    abstract val result: Property<File>
}
