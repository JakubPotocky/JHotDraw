# CILab - Continuous Integration For Copy/Paste

## Lab Goal

The CI lab asks students to understand continuous integration and set up a simple CI pipeline. The course lab specifically mentions GitHub Actions, Maven, building on pull requests, and running tests automatically.

## CI Evidence In This Repository

The repository contains a GitHub Actions workflow:

```text
.github/workflows/maven.yml
```

The workflow is named:

```text
Java CI with Maven
```

It runs on:

- Pushes to `develop`.
- Pull requests targeting `develop`.

## Pipeline Steps

| Step | What It Does | Why It Matters For Copy/Paste Maintenance |
| --- | --- | --- |
| `actions/checkout@v4` | Checks out the repository. | CI needs the same source code that developers changed. |
| `actions/setup-java@v4` | Installs Temurin JDK 23 and enables Maven cache. | Ensures the project builds in a clean standard environment. |
| Configure GitHub Packages credentials | Writes Maven settings with `GITHUB_ACTOR` and `GITHUB_TOKEN`. | Allows Maven to resolve packages if needed. |
| `mvn -B clean install --file pom.xml` | Builds the whole multi-module project. | Confirms copy/paste-related modules still compile together. |
| `mvn test --file pom.xml` | Runs Maven tests. | Gives automated feedback before changes enter `develop`. |

## Maven Project Structure

The root `pom.xml` is a multi-module Maven project. The modules include:

- `jhotdraw-core`
- `jhotdraw-samples`
- `jhotdraw-xml`
- `jhotdraw-api`
- `jhotdraw-utils`
- `jhotdraw-gui`
- `jhotdraw-app`
- `jhotdraw-datatransfer`
- `jhotdraw-actions`

This matters because copy/paste is not local to one module:

- Actions live in `jhotdraw-actions`.
- Clipboard utilities live in `jhotdraw-datatransfer`.
- Drawing view and input/output formats live in `jhotdraw-core`.
- Menus and application action maps live in `jhotdraw-app`.
- Toolbars/action button collections live in `jhotdraw-gui`.
- The Draw sample lives in `jhotdraw-samples`.

## CI Relevance For The Copy/Paste Feature

Copy/paste crosses module boundaries. A small change in one module can break another module:

- Changing `CopyAction` or `PasteAction` can break action registration or UI command behavior.
- Changing `DefaultDrawingViewTransferHandler` can break copy, paste, cut, drag-and-drop, file import, or undo behavior.
- Changing input/output formats can break clipboard interoperability.
- Changing `CompositeTransferable` or clipboard proxy classes can break data flavor handling.

CI helps because it builds all modules and runs tests automatically after each push or pull request.

## CI And Baseline Thinking

In the course terminology, CI helps protect the baseline. Before a change becomes part of the shared branch, the build server checks whether the project still compiles and whether tests still pass. This reduces the risk that a copy/paste refactoring creates a broken baseline.

## Current Coverage And Limitation

The existing workflow builds and tests the project. The workspace now includes focused Copy/Paste tests in the sample module:

- `DOMStorableInputOutputFormatTest`
- `DefaultDrawingViewTransferHandlerTest`
- `CopyPasteBddTest`

These tests run under the normal Maven test command and cover native transferable round-trip behavior, drawing-view transfer handling, selection after paste, unsupported data, undo/redo, and executable BDD-style scenarios.

The remaining limitation is that this is still not a manual GUI test. Copy/paste involves Swing focus, operating-system clipboard behavior, menus, toolbar actions, and platform-specific data flavor ordering. Those can still vary outside the automated JUnit tests.

## CILab Conclusion

The project already has a Maven-based GitHub Actions pipeline. For Bob's copy/paste maintenance work, CI is important because the feature crosses several Maven modules. The build and test steps provide safety before merging changes into `develop`.
