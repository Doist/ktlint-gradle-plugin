> **Warning** — Not actively maintained.

# Ktlint Gradle Plugin

This plugin is mainly designed to run [Ktlint](https://github.com/pinterest/ktlint) for changed `kt` files.

## Usage

Run any gradle task from this plugin on CI or locally.

### Tasks

- `ktlintCheck` - Checks code styles for all kotlin files in the project.
- `ktlintFormat` - Formats all kotlin files in the project.
- `ktlintCheckUncommittedChanges` - Checks code styles for all changed kotlin files.
- `ktlintFormatUncommittedChanges` - Formats code style for all changed kotlin files.
- `ktlintCheckChangesFrom*` - Checks code style for all changed `kt` files against branch, specified in plugin configuration.
- `ktlintFormatChangesFrom*` - Formats code styles for all changed `kt` files against branch, specified in plugin configuration.

### Limitations
  
- Plugin checks only `kt` files, not `kts`.
- Tasks `ktlint*UncommittedChanges` and `ktlint*ChangesFrom*` require git.

## Setup

```kotlin
plugins {
    id("com.doist.gradle.ktlint") version "1.0.0"
}

ktlint {
    // Option to enable Android Kotlin Style Guide compatibility. 
    // Default: false 
    android = true | false
    // Option to disable some ktlint rules.
    // Default: undefined.
    disabledRules = listOf("...")
    // Name of branch used in tasks `ktlint*ChangesFrom*` to compare with current branch. Should be specified without `origin`, though plugin will use remote branch.
    // Default: main.
    mainBranch = "..."
    // Ktlint version.
    // Required and doesn't have default value.
    version = "..."
}
```

Also see plugin page in [Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.doist.gradle.ktlint).

## Release

To release a new version, ensure `CHANGELOG.md` is up-to-date, and push the corresponding tag (e.g., `v1.2.3`). GitHub Actions handles the rest.

## Licence

Released under the [MIT License](https://opensource.org/licenses/MIT).
