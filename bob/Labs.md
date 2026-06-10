# JHotDraw Copy/Paste Maintenance Portfolio

## Course-Style Folder Layout

I also mirrored the course-style `docs` layout under this folder:

- [docs/exercises](docs/exercises/README.md)
- [docs/portfolio](docs/portfolio/README.md)

Those files use the same structure as the sibling workspace `docs` example, but with my Copy/Paste contribution.

## Selected Feature

Basic Editing - Copy/Paste.

This portfolio analyses the existing JHotDraw Copy/Paste feature. The implementation is already present in the repository and is centered around `CopyAction`, `PasteAction`, `DefaultDrawingViewTransferHandler`, clipboard utilities, drawing input/output formats, and the `Drawing`/`Figure` model.

## Lab Documents

1. [01 Intro Lab](docs/exercises/01-intro-lab.md)
2. [02 Change Request Lab](docs/exercises/02-change-request-and-user-stories.md)
3. [03 Concept Location Lab](docs/exercises/03-concept-location.md)
4. [04 Impact Analysis Lab](docs/exercises/04-impact-analysis.md)
5. [05 CI Lab](docs/exercises/05-continuous-integration.md)
6. [06 Refactoring Lab](docs/exercises/06-refactoring.md)
7. [07 Actualization Lab](docs/exercises/07-actualization-clean-architecture-solid.md)
8. [08 Testing Lab](docs/exercises/08-testing.md)
9. [09 BDD Lab](docs/exercises/09-bdd-user-stories-to-scenarios.md)

## Overall Findings

Copy/Paste is implemented through a clean delegation chain:

- UI actions (`CopyAction`, `PasteAction`) trigger Swing transfer operations.
- `DefaultDrawingView` installs `DefaultDrawingViewTransferHandler`.
- `DefaultDrawingViewTransferHandler` coordinates selected figures, transferables, data flavors, import/export formats, selection updates, and undo edits.
- `InputFormat` and `OutputFormat` provide the strategy extension points.
- `Drawing` and `Figure` remain the domain model.
- `ClipboardUtil` and transferable classes isolate clipboard/data-transfer infrastructure.

The most important design point is that Copy/Paste is format-driven. The same action and transfer handler can support native JHotDraw figure paste, image transfer, text paste, and sample-specific formats depending on what the drawing registers.

## Changes Made

- Verified the existing GitHub Actions Maven CI workflow at `.github/workflows/maven.yml`; no workflow file change is part of the current diff.
- Added JUnit 4 tests for DOM transferable round-trip behavior.
- Added JUnit 4 tests for `DefaultDrawingViewTransferHandler` copy, paste, selection, unsupported data, and undo/redo behavior.
- Added executable BDD-style JUnit scenarios for the Copy/Paste user story.
- Kept the Copy/Paste implementation narrow and did not rebuild it.
- Applied the main behavior-preserving refactor in `DefaultDrawingViewTransferHandler` by extracting repeated paste import/undo logic.
- Performed a small behavior-preserving refactor in `CompositeTransferable` by marking stable internal collection references as `final`.

## Verification Summary

Commands run successfully:

```bash
mvn clean install -DskipTests
mvn -pl jhotdraw-core test
mvn -pl jhotdraw-samples/jhotdraw-samples-misc test
mvn test --file pom.xml
```

The sample module test command currently runs 13 Copy/Paste-related tests with 0 failures.

GUI command attempted:

```bash
timeout 20s mvn exec:java "-Dexec.mainClass=org.jhotdraw.samples.svg.Main"
```

The GUI command was run from `jhotdraw-samples/jhotdraw-samples-misc`. It initialized the application and then was stopped by the 20 second timeout, so no manual GUI interaction is claimed.


# 01 Intro Lab

## Feature

Selected feature: Basic Editing - Copy/Paste in JHotDraw.

This portfolio analyses the existing Copy/Paste implementation. The feature was not rebuilt from scratch. The work traces the current actions, Swing transfer handling, clipboard abstraction, drawing formats, and figure model.

## Environment

- Date run: 2026-06-10
- Working directory: `/home/bob/Desktop/university/softwareMaintenance/JHotDraw`
- Java: OpenJDK 17.0.19
- Maven: Apache Maven 3.8.7
- Session: Wayland/X display variables were present (`DISPLAY=:0`, `WAYLAND_DISPLAY=wayland-0`)

## Build Command

Command:

```bash
mvn clean install -DskipTests
```

Result: succeeded.

The Maven reactor built all modules:

- `jhotdraw`
- `jhotdraw-api`
- `jhotdraw-utils`
- `jhotdraw-xml`
- `jhotdraw-datatransfer`
- `jhotdraw-actions`
- `jhotdraw-core`
- `jhotdraw-gui`
- `jhotdraw-app`
- `jhotdraw-samples`
- `jhotdraw-samples-misc`
- `jhotdraw-samples-mini`

Maven reported `BUILD SUCCESS` and installed the artifacts into the local Maven repository. Tests were intentionally skipped because the requested command used `-DskipTests`.

## GUI Run Command

Command run from `jhotdraw-samples/jhotdraw-samples-misc`:

```bash
timeout 20s mvn exec:java "-Dexec.mainClass=org.jhotdraw.samples.svg.Main"
```

Result: the command did not terminate within 20 seconds and `timeout` stopped it with exit code `124`.

Observed output showed the SVG application started far enough to initialize JHotDraw actions and menus. The output included many missing icon resource warnings, for example warnings for `edit.copy.icon` and `edit.paste.icon`. A GTK warning about `canberra-gtk-module` also appeared. Near shutdown/timeout, Java preferences emitted a `javax.xml.parsers.FactoryConfigurationError` related to `DocumentBuilderFactory`.

I did not manually interact with the GUI window in this environment, so I am not claiming an interactive GUI copy/paste test. The launch command was attempted and appeared to enter the GUI event loop, but it was stopped by the timeout.

## Notes

The important build result for the lab is that the Maven project builds successfully with Java 17. The GUI command was attempted honestly, but no manual GUI validation is claimed.

# 02 Change Request Lab

## Change Request

Analyse, document, test, and lightly refactor the existing JHotDraw Basic Editing - Copy/Paste feature.

This is not a request to create a new copy/paste implementation. It is a maintenance request for an existing feature that already exists in JHotDraw.

## User Story

As a user editing a drawing, I want to copy selected figures and paste them back into the drawing, so that I can duplicate existing work without recreating it manually.

## Acceptance Criteria

- Given a drawing view with one or more selected figures, when the user invokes Copy, then JHotDraw places a transferable representation of the selected figures on the clipboard.
- Given the clipboard contains a JHotDraw drawing transferable, when the user invokes Paste in an enabled drawing view, then the figures are imported into the current drawing.
- Given figures are pasted, then the pasted figures become the current selection.
- Given pasted figures are added to the drawing, then undo/redo can remove and re-add those pasted figures.
- Given there are no selected figures, when Copy is invoked, then no figure transferable is produced.
- Given a transferable uses an unsupported data flavor, when Paste is invoked, then the drawing should not import it as a JHotDraw figure.
- Given the clipboard contains supported non-JHotDraw content such as text, when the drawing has a matching `InputFormat`, then the paste can create suitable figures using that strategy.
- The existing architecture should be preserved: actions delegate to Swing `TransferHandler`, drawing formats remain strategies, and model concepts remain in `Drawing`/`Figure`.

## Scope

In scope:

- `CopyAction`
- `PasteAction`
- `ClipboardUtil`
- `DefaultDrawingViewTransferHandler`
- `DefaultDrawingView`
- `Drawing`
- `Figure`
- `InputFormat`
- `OutputFormat`
- `DOMStorableInputOutputFormat`
- `ImageOutputFormat`
- `TextInputFormat`
- sample setup in `org.jhotdraw.samples.draw` and `org.jhotdraw.samples.svg`

Out of scope:

- Rewriting Copy/Paste from scratch
- Replacing Swing clipboard infrastructure
- Adding a new GUI framework
- Large format or model refactors

# 03 Concept Location Lab

## Method Used

I used static analysis and Maven execution. I did not use an interactive debugger in this environment, so no debugger execution is claimed.

Static analysis commands included `rg`, `sed`, `nl`, and Maven test/build commands. The code path was traced through the existing implementation.

## Static Trace

1. `CopyAction.actionPerformed()` resolves the target component, falls back to the current focus owner, and calls `c.getTransferHandler().exportToClipboard(...)` with `TransferHandler.COPY`. See `jhotdraw-actions/src/main/java/org/jhotdraw/action/edit/CopyAction.java`, lines 58-70.
2. `DefaultDrawingView` installs `DefaultDrawingViewTransferHandler` in its constructor. See `jhotdraw-core/src/main/java/org/jhotdraw/draw/DefaultDrawingView.java`, line 305.
3. `DefaultDrawingViewTransferHandler.createTransferable(...)` collects selected figures from the drawing view, sorts them through the drawing, and asks each `OutputFormat` to create a `Transferable`. See `jhotdraw-core/src/main/java/org/jhotdraw/draw/DefaultDrawingViewTransferHandler.java`, lines 267-311.
4. `CompositeTransferable` combines multiple transfer flavors, such as native JHotDraw XML and image flavor. See `jhotdraw-datatransfer/src/main/java/org/jhotdraw/datatransfer/CompositeTransferable.java`, lines 19-82.
5. `DOMStorableInputOutputFormat.createTransferable(...)` writes selected figures into a `"Drawing-Clip"` DOM structure and returns an `InputStreamTransferable`. See `jhotdraw-core/src/main/java/org/jhotdraw/draw/io/DOMStorableInputOutputFormat.java`, lines 207-218.
6. `ClipboardUtil.getClipboard()` supplies the clipboard abstraction used by actions. See `jhotdraw-datatransfer/src/main/java/org/jhotdraw/datatransfer/ClipboardUtil.java`, lines 35-53.
7. `PasteAction.actionPerformed()` retrieves clipboard contents and calls `c.getTransferHandler().importData(...)`. See `jhotdraw-actions/src/main/java/org/jhotdraw/action/edit/PasteAction.java`, lines 59-72.
8. `DefaultDrawingViewTransferHandler.importData(...)` checks drawing `InputFormat`s against transferable data flavors and imports the first supported format. See `jhotdraw-core/src/main/java/org/jhotdraw/draw/DefaultDrawingViewTransferHandler.java`, lines 81-171.
9. Imported figures are detected by comparing drawing children before and after import, selected, optionally moved to the drop point, and wrapped in an undoable paste edit. See `DefaultDrawingViewTransferHandler.java`, lines 174-217.
10. `DOMStorableInputOutputFormat.read(Transferable, Drawing, boolean)` reads the `"Drawing-Clip"` and adds decoded figures to the drawing. See `DOMStorableInputOutputFormat.java`, lines 190-205.

## Domain Class Table

| Domain Class | Responsibility |
|---|---|
| `CopyAction` | UI action/controller for Copy. Locates target component and delegates clipboard export to Swing `TransferHandler`. |
| `PasteAction` | UI action/controller for Paste. Gets clipboard contents and delegates import to Swing `TransferHandler`. |
| `ClipboardUtil` | Central clipboard access helper. Tries system clipboard and falls back to JNLP/JVM-local clipboard if needed. |
| `DefaultDrawingView` | Swing component for a drawing. Owns selection state and installs the drawing transfer handler. |
| `DefaultDrawingViewTransferHandler` | Main copy/paste coordinator for drawing views. Creates transferables, imports supported formats, updates selection, handles drag/drop movement, and creates undo edits. |
| `CompositeTransferable` | Combines multiple `Transferable` instances into one clipboard payload with multiple data flavors. |
| `Drawing` | Model abstraction for a drawing. Owns figures and exposes input/output format strategy lists used by copy/paste. |
| `Figure` | Domain object being copied and pasted. Concrete figures are serialized, cloned, transformed, selected, and added to drawings. |
| `InputFormat` | Strategy interface for reading drawing content from files, streams, or transferables. |
| `OutputFormat` | Strategy interface for writing drawings or selected figures and creating transferables. |
| `DOMStorableInputOutputFormat` | JHotDraw native DOM/XML copy/paste format for serializing and deserializing selected figures. |
| `ImageOutputFormat` | Output strategy that creates image transfer data for copied figures. Useful for copying drawing content to other applications. |
| `TextInputFormat` | Input strategy that converts string clipboard content into text-holder figures. |
| `DrawView` | Sample draw application setup. Registers DOM, image, and text input/output formats on the drawing. |
| `SVGApplet`/SVG sample | SVG sample setup. Registers SVG/image formats for SVG drawings. |

## Debugger Plan

If running an interactive debugger, use these breakpoints:

- `CopyAction.actionPerformed()`
- `PasteAction.actionPerformed()`
- `DefaultDrawingViewTransferHandler.createTransferable()`
- `DefaultDrawingViewTransferHandler.importData()`
- `DOMStorableInputOutputFormat.createTransferable()`
- `DOMStorableInputOutputFormat.read(Transferable, Drawing, boolean)`
- `ClipboardUtil.getClipboard()`

Reproducible debugger scenario:

1. Start the SVG or Draw sample application.
2. Create a rectangle or another basic figure.
3. Select the figure.
4. Trigger Edit -> Copy or the copy keyboard shortcut.
5. Continue through `CopyAction`, `ClipboardUtil`, `DefaultDrawingViewTransferHandler.createTransferable`, and `DOMStorableInputOutputFormat.createTransferable`.
6. Trigger Edit -> Paste or the paste keyboard shortcut.
7. Continue through `PasteAction`, `ClipboardUtil`, `DefaultDrawingViewTransferHandler.importData`, and `DOMStorableInputOutputFormat.read`.
8. Inspect `drawing.getChildren()` before and after import and verify the pasted figure is selected.

# 04 Impact Analysis Lab

## Impact Summary

Copy/Paste crosses UI actions, Swing data transfer, drawing view selection, model storage, input/output strategies, and sample application setup. A change to one part can affect several formats and applications because the feature is deliberately generic.

## Package Impact Table

| Package name | # of classes visited | Comments |
|---|---:|---|
| `org.jhotdraw.action.edit` | 2 | Contains `CopyAction` and `PasteAction`. Affected by changes to command availability, focus handling, and clipboard delegation. |
| `org.jhotdraw.datatransfer` | 3 | Contains `ClipboardUtil`, `CompositeTransferable`, and `InputStreamTransferable`. Affected by clipboard fallback behavior, transferable flavor ordering, and data retrieval. |
| `org.jhotdraw.draw` | 6 | Contains `DefaultDrawingViewTransferHandler`, `DefaultDrawingView`, `Drawing`, `DefaultDrawing`, `QuadTreeDrawing`, and `AbstractDrawing`. Affected by selection, import/export coordination, undo events, drawing children, and registered formats. |
| `org.jhotdraw.draw.io` | 5 | Contains `InputFormat`, `OutputFormat`, `DOMStorableInputOutputFormat`, `ImageOutputFormat`, and `TextInputFormat`. Affected by supported data flavors, serialization/deserialization, image export, and text paste. |
| `org.jhotdraw.draw.figure` | 5 | Contains `Figure`, `CompositeFigure`, `AbstractCompositeFigure`, `RectangleFigure`, and `TextFigure`. Affected because figures are the copied/pasted domain objects. Serialization, cloning, bounds, transforms, and selection behavior matter. |
| `org.jhotdraw.samples.draw` | 2 | Contains `DrawView` and `DrawFigureFactory`. Affected because the sample registers DOM, image, and text formats used by copy/paste. |
| `org.jhotdraw.samples.svg` | 1 | `SVGApplet` was visited for sample format registration. SVG samples use different input/output strategies but rely on the same action and transfer handler structure. |

## Package Contributions

`org.jhotdraw.action.edit` contributes the user-level commands. These classes should stay thin: they identify the target component and delegate to Swing transfer support.

`org.jhotdraw.datatransfer` contributes infrastructure. `ClipboardUtil` hides clipboard setup and fallback behavior. `CompositeTransferable` allows one copy operation to expose several data flavors.

`org.jhotdraw.draw` contributes the central coordination logic. `DefaultDrawingView` owns selected figures and installs `DefaultDrawingViewTransferHandler`. The transfer handler creates transferables on copy, chooses an input strategy on paste, updates selection, and creates undoable edits.

`org.jhotdraw.draw.io` contributes strategy interfaces and implementations. This is the most important extension point: copy/paste does not need to know every file or clipboard format directly.

`org.jhotdraw.draw.figure` contributes the model objects being copied. Changes to figure serialization, clone behavior, bounds, or transforms can alter paste behavior.

`org.jhotdraw.samples.draw` and `org.jhotdraw.samples.svg` contribute application-specific format registration. The same core copy/paste path behaves differently depending on which formats a sample drawing registers.

## Risks When Changing Copy/Paste

- Changing data flavor preference can alter which format is pasted when multiple formats are available.
- Changing `DefaultDrawingViewTransferHandler.importData()` can affect paste, drag/drop, and file drop behavior.
- Changing `DOMStorableInputOutputFormat` can break native JHotDraw figure copy/paste and saved XML files.
- Changing `Figure` serialization can break old clipboard data and saved drawings.
- Changing selection logic can make pasted figures appear in the drawing but not become selected.
- Changing undo edit creation can cause paste to work visually but not be undoable.

# 05 CI Lab

## Workflow File

Workflow path:

```text
.github/workflows/maven.yml
```

## What Was Verified/Changed

A Maven workflow already exists in `.github/workflows/maven.yml`. No workflow file change is part of the current worktree diff.

The existing workflow:

- runs on `push` to `develop`
- runs on `pull_request` to `develop`
- uses `actions/checkout@v4`
- uses `actions/setup-java@v4`
- uses Temurin JDK 23
- caches Maven dependencies
- configures GitHub Packages credentials
- runs `mvn -B clean install --file pom.xml`
- runs `mvn test --file pom.xml`

## Reasoning

The lab asks to add or verify a GitHub Actions workflow. Since a Maven workflow already exists and contains both push and pull-request triggers plus Maven build/test commands, I verified and documented it instead of making an unrelated workflow change.

Local verification was still run with Java 17 using `mvn test --file pom.xml`, but that is separate from the current GitHub Actions workflow configuration.

## Expected CI Behavior

On push or pull request events targeting `develop`, the existing GitHub Actions workflow should:

1. Check out the repository.
2. Install JDK 23.
3. Restore/populate the Maven dependency cache.
4. Build/install all modules.
5. Run Maven tests.
6. Fail the workflow if compilation or tests fail.

## Local Verification

Command:

```bash
mvn test --file pom.xml
```

Result: succeeded. Maven reported `BUILD SUCCESS`.

# 06 Refactoring Lab

## Tooling

SonarLint was checked from the command line:

```bash
command -v sonarlint
```

No `sonarlint` executable was available in this environment. I also checked for obvious Sonar configuration files and did not find one. Therefore, this lab used manual inspection.

## Code Smells Found

### Duplicate Paste Import/Undo Logic

Affected class:

```text
jhotdraw-core/src/main/java/org/jhotdraw/draw/DefaultDrawingViewTransferHandler.java
```

The paste path had repeated logic for:

- taking a snapshot of existing drawing children
- reading figures through an `InputFormat`
- identifying newly imported figures
- clearing and updating selection
- moving imported figures to a drop point
- firing the paste undoable edit

### Mutable Collection Fields That Are Never Reassigned

Affected class:

```text
jhotdraw-datatransfer/src/main/java/org/jhotdraw/datatransfer/CompositeTransferable.java
```

The internal `transferables` and `flavors` collections are initialized once and mutated, but the collection references themselves are not reassigned. Leaving those references non-final makes the invariant less clear.

## Refactoring Applied

### Transfer Handler Extraction

`DefaultDrawingViewTransferHandler` now centralizes repeated paste logic in helper methods:

- `importTransferData(...)`
- `firePasteUndoableEdit(...)`

This reduces duplicated code inside the Mac and non-Mac data flavor search branches and reuses the same undo edit construction for pasted figures.

### Final Internal Collections

`CompositeTransferable` now declares its internal collection references as `final`:

```java
private final HashMap<DataFlavor, Transferable> transferables = new HashMap<>();
private final LinkedList<DataFlavor> flavors = new LinkedList<>();
```

This is behavior-preserving. The contents can still change, but the fields cannot be accidentally reassigned.

## Before/After Summary

Before:

- Paste import and undo construction logic was repeated across branches.
- `CompositeTransferable` collection fields did not communicate that their references are stable.

After:

- Paste import/undo behavior is centralized.
- `CompositeTransferable` has clearer field invariants.

## Reasoning

The refactoring is intentionally narrow. It does not alter the Copy/Paste architecture, data flavors, serialization format, or Swing clipboard integration.

The extraction improves maintainability because future changes to paste selection or undo behavior can be made in one helper instead of multiple repeated blocks.

The final-field change improves readability and reduces accidental mutation risk without changing runtime behavior.

## Verification

Commands run:

```bash
mvn -pl jhotdraw-samples/jhotdraw-samples-misc test
mvn test --file pom.xml
```

Both commands succeeded. The final reactor test verification reported `BUILD SUCCESS`.


# 07 Actualization Lab

## SOLID in JHotDraw Copy/Paste

### Single Responsibility Principle

`CopyAction` and `PasteAction` are controller/action layer classes. They do not serialize figures themselves. `CopyAction` locates the target component and delegates to `TransferHandler.exportToClipboard`. `PasteAction` reads clipboard contents and delegates to `TransferHandler.importData`.

`DefaultDrawingViewTransferHandler` coordinates transfer behavior for a drawing view. It knows about selected figures, transferables, formats, drop points, and undo edits, but it still delegates actual serialization to `OutputFormat` and deserialization to `InputFormat`.

`DOMStorableInputOutputFormat` is responsible for one concrete format: writing and reading DOM-storable drawing figures.

### Open/Closed Principle

Copy/Paste is open for new formats through `InputFormat` and `OutputFormat`. A drawing can register more formats without changing `CopyAction`, `PasteAction`, or the transfer handler.

Example from `DrawView`:

- `drawing.addInputFormat(ioFormat)`
- `drawing.addInputFormat(new ImageInputFormat(prototype))`
- `drawing.addInputFormat(new TextInputFormat(new TextFigure()))`
- `drawing.addOutputFormat(ioFormat)`
- `drawing.addOutputFormat(new ImageOutputFormat())`

The transfer handler loops over registered formats instead of hardcoding every supported paste type.

### Liskov Substitution Principle

The transfer handler works with the `Drawing` and `Figure` abstractions. A `DefaultDrawing`, `QuadTreeDrawing`, `RectangleFigure`, `TextFigure`, or sample-specific figure can participate as long as it honors the expected `Drawing`/`Figure` contracts.

### Interface Segregation Principle

`InputFormat` and `OutputFormat` are separate interfaces. A class can implement only input, only output, or both. `DOMStorableInputOutputFormat` implements both because it supports round-trip behavior. `ImageOutputFormat` implements output only because images are useful for copying out to other applications.

### Dependency Inversion Principle

High-level copy/paste coordination depends on abstractions:

- `TransferHandler`
- `Transferable`
- `InputFormat`
- `OutputFormat`
- `Drawing`
- `Figure`

The action classes do not depend on `DOMStorableInputOutputFormat` directly. The drawing's registered strategies supply concrete behavior.

## Clean Architecture View

JHotDraw is not written as a textbook Clean Architecture project, but the Copy/Paste feature shows useful separation of concerns.

### Controller/Action Layer

`CopyAction` and `PasteAction` are closest to user intent. They translate menu/keyboard actions into transfer operations.

### Coordination Layer

`DefaultDrawingViewTransferHandler` is the coordination layer. It connects Swing transfer APIs to JHotDraw's drawing model and format strategies.

### Strategy Layer

`InputFormat` and `OutputFormat` are strategy interfaces. Concrete strategies include:

- `DOMStorableInputOutputFormat`
- `ImageOutputFormat`
- `TextInputFormat`
- sample SVG formats

### Domain/Model Layer

`Drawing` and `Figure` are model concepts. `Drawing` owns figures and format lists. `Figure` represents the domain objects being copied and pasted.

### Infrastructure Layer

`ClipboardUtil`, `CompositeTransferable`, `InputStreamTransferable`, Swing `Clipboard`, Swing `TransferHandler`, and Java `DataFlavor` are infrastructure. They are kept away from figure geometry and drawing semantics.

## Concrete Example

When the user copies selected figures:

1. `CopyAction` delegates to Swing transfer export.
2. `DefaultDrawingViewTransferHandler` gets selected figures from `DefaultDrawingView`.
3. The drawing sorts figures in z-order.
4. Each registered `OutputFormat` creates a transferable.
5. `CompositeTransferable` combines the results.
6. `ClipboardUtil` supplies the clipboard.

When the user pastes:

1. `PasteAction` gets the clipboard transferable.
2. `DefaultDrawingViewTransferHandler` chooses a compatible `InputFormat`.
3. `DOMStorableInputOutputFormat` or another input strategy reads figures.
4. The drawing receives the imported figures.
5. The view selects the pasted figures.
6. The drawing receives an undoable paste edit.

This separation is why the same action classes can support native figure paste, image export, text paste, and sample-specific SVG formats.

# 08 Testing Lab

## Test Strategy

The goal was to test Copy/Paste-related logic without launching the full Swing GUI.

The tests now cover both the native transfer format and the drawing-view transfer handler:

- `DOMStorableInputOutputFormat` serializes selected figures into the native JHotDraw transferable format and reads them back.
- `DefaultDrawingViewTransferHandler` creates transferables from selected figures, imports supported data, updates selection, rejects unsupported data, and fires undoable paste edits.
- `CopyPasteBddTest` automates the user-facing Given-When-Then scenarios with JUnit test methods.

## Test Files Added

```text
jhotdraw-samples/jhotdraw-samples-misc/src/test/java/org/jhotdraw/samples/draw/DOMStorableInputOutputFormatTest.java
jhotdraw-samples/jhotdraw-samples-misc/src/test/java/org/jhotdraw/samples/draw/DefaultDrawingViewTransferHandlerTest.java
jhotdraw-samples/jhotdraw-samples-misc/src/test/java/org/jhotdraw/samples/draw/CopyPasteBddTest.java
```

This module was chosen because it already has JUnit 4.13.2 and the `DrawFigureFactory` needed to serialize common draw figures such as `RectangleFigure`.

## Tests Added

`createsTransferableThatRoundTripsSelectedFigure`

- Creates a `RectangleFigure`.
- Creates a DOM transferable from that selected figure.
- Reads the transferable into a new drawing.
- Verifies one figure was pasted.
- Verifies the pasted figure is a `RectangleFigure`.
- Verifies the pasted figure is not the same object instance as the original.
- Verifies bounds survive the round trip.

`readWithReplaceClearsExistingFigures`

- Creates a source drawing and a target drawing with an existing figure.
- Reads the transferable with `replace=true`.
- Verifies the target drawing contains only the copied figure.

`rejectsUnsupportedDataFlavor`

- Verifies `DOMStorableInputOutputFormat` does not claim support for `DataFlavor.stringFlavor`.

`copySelectedFigureCreatesTransferable`

- Verifies selected figures produce a drawing transferable through the transfer handler.

`copyWithoutSelectionCreatesNoTransferable`

- Verifies Copy with no selected figures produces no drawing transferable.

`pasteSupportedTransferableAddsAndSelectsImportedFigure`

- Verifies Paste imports a cloned figure, adds it to the drawing, and selects it.

`pasteUnsupportedTransferableLeavesDrawingUnchanged`

- Verifies unsupported data does not change the drawing.

`pasteFiresUndoableEditThatCanUndoAndRedoImportedFigure`

- Verifies the paste undoable edit removes and restores imported figures.

## Commands Run

```bash
mvn -pl jhotdraw-samples/jhotdraw-samples-misc test
```

Result: succeeded. JUnit reported:

```text
Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
```

Full verification:

```bash
mvn test --file pom.xml
```

Result: succeeded. The full Maven reactor reported `BUILD SUCCESS`.

## Remaining Test Opportunities

Useful future tests would include:

- Clipboard-level `CopyAction`/`PasteAction` tests using a JVM-local clipboard via `ClipboardUtil.setClipboard(...)`.
- Swing UI smoke tests for keyboard shortcuts and menu actions.

Those tests are possible, but they require more Swing setup or OS clipboard isolation than the focused JUnit tests added here.

# 09 BDD Lab

## Scenarios

### Scenario 1: Copy and paste one selected rectangle

Given a drawing with one selected rectangle  
When the user copies and pastes it  
Then the drawing contains two rectangles  
And the pasted rectangle is selected

### Scenario 2: Copy with no selection

Given a drawing view with no selected figures  
When the user invokes Copy  
Then no drawing figure transferable is produced  
And the drawing remains unchanged

### Scenario 3: Paste native JHotDraw figures

Given the clipboard contains a JHotDraw `"Drawing-Clip"` transferable  
And the current drawing supports `DOMStorableInputOutputFormat`  
When the user invokes Paste  
Then the figures from the transferable are added to the drawing  
And the pasted figures become selected

### Scenario 4: Paste unsupported data

Given the clipboard contains data with no supported data flavor  
When the user invokes Paste  
Then the drawing does not import the data  
And the drawing remains unchanged

### Scenario 5: Undo pasted figures

Given a user pasted one or more figures into a drawing  
When the user invokes Undo  
Then the pasted figures are removed from the drawing  
When the user invokes Redo  
Then the pasted figures are added back to the drawing

### Scenario 6: Paste text as a text figure

Given the clipboard contains plain text  
And the drawing registers `TextInputFormat`  
When the user invokes Paste  
Then the drawing adds one or more text-holder figures containing the pasted text

### Scenario 7: Copy exposes multiple data flavors

Given a drawing has selected figures  
And the drawing registers DOM and image output formats  
When the user invokes Copy  
Then the clipboard transferable exposes a native JHotDraw flavor  
And it exposes an image flavor for other applications

## Automation Status

The primary BDD scenarios are automated in:

```text
jhotdraw-samples/jhotdraw-samples-misc/src/test/java/org/jhotdraw/samples/draw/CopyPasteBddTest.java
```

No Cucumber, JBehave, or other BDD library was added. JUnit 4 was already available in the sample module, so the scenarios are implemented as executable Given-When-Then test method names.

Automated scenarios include:

- copy and paste one selected rectangle;
- copy with no selected figures;
- paste unsupported data;
- undo and redo pasted figures;
- paste plain text through `TextInputFormat`.

Verification:

```text
CopyPasteBddTest: Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```
