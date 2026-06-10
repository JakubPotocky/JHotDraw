# Bob's Portfolio Work - Copy/Paste Basic Editing

This document collects Bob's portfolio deliverables for the Software Maintenance course. Each chapter corresponds to one lab exercise. The selected feature is the Basic Editing copy/paste functionality from the JHotDraw case study.

---

# Chapter: ChangeReqLab - Change Request

## System Under Maintenance

JHotDraw 7.6, a Java structured-graphics editor framework used as the course case study.

## Selected Feature

Parent feature: Basic editing #12.

Sub-features:

- Copy #13.
- Paste #14.

## User Stories

### Copy

As a user, I want to copy a selected object so that I can quickly duplicate existing work without recreating it manually.

### Paste

As a user, I want to paste a copied object so that I can quickly place a duplicate on the canvas.

## Acceptance Criteria

Copy:

1. The user can select one or more figures on the canvas.
2. The user can trigger Copy through menu, shortcut, or toolbar/popup action.
3. The selected figures are exported to the clipboard.
4. The original figures remain unchanged in the drawing.
5. The copied data can later be pasted into a compatible drawing view.

Paste:

1. The user can trigger Paste through menu, shortcut, or toolbar/popup action.
2. The command reads the clipboard.
3. If the clipboard contains a supported format, figures are imported into the active drawing.
4. Imported figures become selected.
5. Paste creates an undoable edit.
6. Unsupported clipboard content does not corrupt the drawing.

---

# Chapter: CLLab - Concept Location

## Technique Used

Static search and code reading were used to map the likely runtime path. The same result can be confirmed dynamically by placing debugger breakpoints in:

- `CopyAction.actionPerformed`.
- `PasteAction.actionPerformed`.
- `DefaultDrawingViewTransferHandler.createTransferable`.
- `DefaultDrawingViewTransferHandler.importData`.
- `DOMStorableInputOutputFormat.createTransferable`.
- `DOMStorableInputOutputFormat.read`.
- `DefaultDrawingView.addToSelection`.

## Initial Set Of Classes

| Domain Class | Responsibility |
| --- | --- |
| `CopyAction` | Controller entry point for copy. Resolves focused component and delegates to `TransferHandler.exportToClipboard`. |
| `PasteAction` | Controller entry point for paste. Gets clipboard contents and delegates to `TransferHandler.importData`. |
| `CutAction` | Related clipboard action that shares export behavior with copy. |
| `AbstractSelectionAction` | Shared base for selection edit actions and enablement logic. |
| `EditableComponent` | Editable component contract used by related basic editing actions. |
| `DefaultDrawingView` | Drawing canvas component; installs the transfer handler and owns selected figures. |
| `DrawingView` | Drawing view abstraction used by the transfer handler. |
| `DefaultDrawingViewTransferHandler` | Core transfer class for exporting selected figures and importing pasted figures. |
| `Drawing` | Drawing model containing figures and input/output formats. |
| `AbstractDrawing` | Stores the registered input and output formats. |
| `Figure` | Domain object copied, pasted, selected, and inserted into drawings. |
| `OutputFormat` | Creates transferables from selected figures during copy. |
| `InputFormat` | Reads transferables and adds figures during paste. |
| `DOMStorableInputOutputFormat` | Main Draw sample format for drawing clips. |
| `SerializationInputOutputFormat` | Alternative transferable drawing representation. |
| `ImageOutputFormat` | Exports selected figures as image data. |
| `ImageInputFormat` | Imports image clipboard data as image figures. |
| `TextInputFormat` | Imports text clipboard data as text figures. |
| `CompositeTransferable` | Combines multiple transfer formats for clipboard content. |
| `ClipboardUtil` | Retrieves system or fallback clipboard. |
| `AWTClipboard` | Proxy for AWT clipboard. |
| `OSXClipboard` | Mac-specific clipboard proxy and native transferable combiner. |
| `DefaultApplicationModel` | Registers copy and paste actions in the app action map. |
| `DefaultMenuBuilder` | Places copy and paste in the Edit menu. |
| `DefaultDrawingEditor` | Maps keyboard shortcuts to copy and paste action IDs. |
| `ButtonFactory` | Adds copy and paste actions to drawing UI action collections. |
| `DrawingPanel` | Draw sample UI panel that exposes copy and paste in its action popup. |
| `DrawView` | Creates the Draw sample drawing and registers input/output formats. |

## Concept Location Conclusion

Copy/paste is a composite feature spread across the action layer, Swing transfer infrastructure, JHotDraw drawing model, clipboard utilities, and drawing input/output formats. The central implementation class for the feature is `DefaultDrawingViewTransferHandler`, while `CopyAction` and `PasteAction` are controller entry points.

---

# Chapter: AnalysisLab1 - Impact Analysis

## Feature Analysed

Basic editing copy/paste in JHotDraw.

## Initial Impact Set

| Class | Reason |
| --- | --- |
| `CopyAction` | User command entry point for copy. |
| `PasteAction` | User command entry point for paste. |
| `DefaultDrawingViewTransferHandler` | Core class for exporting selected figures and importing pasted figures. |
| `DefaultDrawingView` | Drawing canvas component that installs the transfer handler and owns selection state. |
| `Drawing` | Supplies figures and registered input/output formats. |
| `DOMStorableInputOutputFormat` | Main Draw sample format for drawing clipboard clips. |
| `ClipboardUtil` | Provides the clipboard used by copy and paste. |

## Packages Visited

| Package name | # of classes visited | Comments |
| --- | ---: | --- |
| `org.jhotdraw.action.edit` | 5 | Contains copy, paste, cut, duplicate, and shared selection-action behavior. The copy/paste actions are controller entry points and delegate to Swing transfer infrastructure. |
| `org.jhotdraw.api.gui` | 1 | Contains `EditableComponent`, the generic editing contract used by nearby basic editing actions and action enablement. |
| `org.jhotdraw.draw` | 6 | Contains the central drawing view, drawing abstraction, editor shortcut mapping, and transfer handler. This is the most important package for copy/paste behavior. |
| `org.jhotdraw.draw.figure` | 3 | Contains figure abstractions and figure types created by image/text paste formats. |
| `org.jhotdraw.draw.io` | 7 | Contains `InputFormat` and `OutputFormat` implementations used to read and write clipboard transfer data. |
| `org.jhotdraw.datatransfer` | 7 | Contains clipboard proxies and transferable data wrappers. |
| `org.jhotdraw.app` | 2 | Registers and places copy/paste actions in application menus. |
| `org.jhotdraw.gui.action` | 1 | Adds copy/paste actions to toolbar/popup action collections. |
| `org.jhotdraw.samples.draw` | 3 | Registers Draw sample input/output formats and exposes the actions in the sample UI. |

Total packages visited: 9.

Total classes visited: 35.

## Estimated Changed Set For Refactoring

| Class | Mark | Reason |
| --- | --- | --- |
| `DefaultDrawingViewTransferHandler` | Changed | Contains duplicated import logic, dead private method, and transfer-related comments that need cleanup. |
| `CopyAction` | Possibly changed | Shares duplicated focused-component lookup with cut and paste. |
| `CutAction` | Possibly changed | Shares duplicated focused-component lookup with copy and paste. |
| `PasteAction` | Possibly changed | Shares focused-component lookup but has paste-specific import behavior. |
| `CompositeTransferable` | Possibly changed | Contains typo-level documentation smells. |
| Input/output formats | Unchanged | Existing extension point already supports transfer behavior. |
| Sample UI classes | Unchanged | Existing wiring already exposes copy and paste. |

## Impact Analysis Conclusion

Copy/paste is composite functionality. Understanding it requires inspecting many packages, but a safe portfolio refactoring should keep the changed set narrow. The most important next target is `DefaultDrawingViewTransferHandler`.

---

# Chapter: CILab - Continuous Integration

## CI Workflow

The repository contains:

```text
.github/workflows/maven.yml
```

The workflow runs on pushes and pull requests to `develop`.

## Pipeline Summary

| Step | Purpose |
| --- | --- |
| Checkout | Gets the repository source code. |
| Set up JDK 23 | Creates a clean Java build environment. |
| Configure Maven credentials | Allows Maven package access if needed. |
| `mvn -B clean install --file pom.xml` | Builds the full multi-module project. |
| `mvn test --file pom.xml` | Runs tests. |

## Why CI Matters For Copy/Paste

Copy/paste spans several Maven modules:

- `jhotdraw-actions`
- `jhotdraw-datatransfer`
- `jhotdraw-core`
- `jhotdraw-app`
- `jhotdraw-gui`
- `jhotdraw-samples`

CI matters because a small refactoring in `DefaultDrawingViewTransferHandler`, `CopyAction`, `PasteAction`, or clipboard classes could break action wiring, transfer formats, undo behavior, or sample UI behavior. The CI build protects the shared baseline by compiling and testing the full project before changes are accepted.

## CI Limitation

The current pipeline does not prove the full GUI copy/paste workflow by itself. The portfolio should still document focused manual or automated verification for selecting a figure, copying it, pasting it, and checking that the pasted figure is added and selected.

---

# Chapter: RefactoringLab1 - Code Smells And Refactoring

## Refactoring Goal

The goal was to improve the maintainability of the copy/paste implementation without changing user-visible behavior. The selected refactoring target was `DefaultDrawingViewTransferHandler`, because concept location and impact analysis showed that it contains the central paste import workflow.

## Smells Found

| Smell | Location | Reason |
| --- | --- | --- |
| Duplicated paste import logic | `DefaultDrawingViewTransferHandler.importData(...)` | Mac and non-Mac search branches repeated the same successful import sequence. |
| Duplicated undo edit creation | `DefaultDrawingViewTransferHandler.importData(...)` | The paste undo/redo edit appeared in multiple import paths. |
| Dead private method | `DefaultDrawingViewTransferHandler` | An unused private `getDrawing()` method threw an unimplemented-operation exception. |
| Comment/documentation noise | `DefaultDrawingViewTransferHandler`, `CompositeTransferable` | Some comments contained typos or unclear wording. |

## Refactorings Applied

| Refactoring | File | Result |
| --- | --- | --- |
| Extract Method | `DefaultDrawingViewTransferHandler.java` | Shared transferable paste behavior moved into `importTransferData(...)`. |
| Extract Method | `DefaultDrawingViewTransferHandler.java` | Shared undoable paste edit creation moved into `firePasteUndoableEdit(...)`. |
| Remove Dead Code | `DefaultDrawingViewTransferHandler.java` | Removed unused unimplemented private method. |
| Improve Comments | `DefaultDrawingViewTransferHandler.java`, `CompositeTransferable.java` | Corrected misleading wording and typo-level documentation issues. |

## Behavior Preservation

The refactoring keeps the same copy/paste behavior. The handler still finds a compatible `InputFormat`, reads the transferable into the drawing, determines which figures are newly imported, selects those figures, moves them to the drop point when needed, and creates the same undoable paste edit.

## Verification

Command run:

```text
mvn -pl jhotdraw-core,jhotdraw-datatransfer -am test
```

Result:

```text
BUILD SUCCESS
```

## Detailed Artifact

See:

```text
BobsWork/06_RefactoringLab1_Code_Smells_Copy_Paste.md
```

---

# Chapter: ActualizationLab - Actualization

## Actualized Change

The copy/paste maintenance work was actualized as a small behavior-preserving refactoring, not as a new feature implementation. The main changed class was `DefaultDrawingViewTransferHandler`, because concept location and impact analysis showed that this is where Swing transfer behavior becomes concrete drawing behavior.

| File | Change | Why |
| --- | --- | --- |
| `DefaultDrawingViewTransferHandler.java` | Extracted `importTransferData(...)`. | Removes duplicated successful paste import logic from Mac and non-Mac search branches. |
| `DefaultDrawingViewTransferHandler.java` | Extracted `firePasteUndoableEdit(...)`. | Keeps paste undo/redo registration in one place. |
| `DefaultDrawingViewTransferHandler.java` | Removed unused private `getDrawing()` method. | Removes dead unimplemented code. |
| `DefaultDrawingViewTransferHandler.java` | Improved comments. | Makes transfer fallback and repaint behavior clearer. |
| `CompositeTransferable.java` | Corrected Javadoc typos. | Improves documentation quality without runtime behavior change. |

## Incorporation Into Existing Behavior

Copy and paste remain incorporated through Swing `TransferHandler`:

- `CopyAction` resolves the focused component and delegates to `TransferHandler.exportToClipboard(..., COPY)`.
- `PasteAction` gets clipboard contents through `ClipboardUtil` and delegates to `TransferHandler.importData(...)`.
- `DefaultDrawingView` already installs `DefaultDrawingViewTransferHandler`.
- `DefaultDrawingViewTransferHandler` exports selected figures through `OutputFormat` implementations and imports pasted figures through `InputFormat` implementations.

No menu, toolbar, shortcut, or action wiring needed to change.

## Responsibility Boundaries

| Boundary | Responsibility |
| --- | --- |
| Actions | Resolve target component and delegate to Swing transfer handling. |
| Transfer handler | Export selected figures, import supported clipboard data, update selection, move dropped figures, and fire paste undo edits. |
| Drawing model | Own figures and registered input/output formats. |
| Input/output formats | Read and write specific clipboard/file data formats. |
| Clipboard utilities | Provide platform-aware clipboard access. |

The refactoring keeps copy/paste as a composite feature while improving the local structure of the central handler.

## Change Propagation

Change propagation was deliberately limited. `CopyAction`, `PasteAction`, `CutAction`, `DefaultDrawingView`, input/output format implementations, and UI wiring were left unchanged because their existing responsibilities were already correct. The duplication was inside `DefaultDrawingViewTransferHandler`, so the code change stayed there.

## Clean Code And SOLID

The change supports Clean Code by reducing duplication, extracting named methods, removing dead code, and improving comments. It supports SOLID mainly by preserving existing abstractions: `Drawing`, `DrawingView`, `InputFormat`, and `OutputFormat` remain the contracts used by the transfer handler. The design is still not perfect, because `DefaultDrawingViewTransferHandler` remains a large integration class, but the most repeated paste behavior now has clearer internal boundaries.

## Risks And Test Needs

The focused Maven test run already completed successfully:

```text
mvn -pl jhotdraw-core,jhotdraw-datatransfer -am test
```

Result:

```text
BUILD SUCCESS
```

The remaining risk is that the automated tests do not fully prove the GUI workflow through menus, keyboard focus, and the operating-system clipboard. TestLab1 now verifies copying selected figures, pasting supported transferable content, selection after paste, unsupported clipboard data, and paste undo/redo through component-level JUnit tests.

## Detailed Artifact

See:

```text
BobsWork/08_ActualizationLab_Copy_Paste.md
```

---

# Chapter: TestLab1 - Automated Tests

## Added Test Files

| File | Purpose |
| --- | --- |
| `DOMStorableInputOutputFormatTest.java` | Tests the native JHotDraw DOM transferable round-trip path used by Copy/Paste. |
| `DefaultDrawingViewTransferHandlerTest.java` | Tests the real drawing-view transfer handler for Copy/Paste behavior. |

## Behavior Covered

The automated tests verify that:

- selected figures produce a transferable;
- copying with no selected figures produces no drawing transferable;
- a supported transferable is pasted into a drawing;
- pasted figures become selected;
- unsupported clipboard data leaves the drawing unchanged;
- paste creates an undoable edit that supports undo and redo;
- native DOM transferable data round-trips to a new figure with the same bounds;
- `replace=true` clears existing figures before import.

## Verification

Command run:

```text
mvn -pl jhotdraw-samples/jhotdraw-samples-misc test
```

Result:

```text
Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Detailed Artifact

See:

```text
BobsWork/09_TestLab1_Copy_Paste.md
```

---

# Chapter: BDDLab - Given-When-Then Scenarios

## Scenario Coverage

The Copy/Paste user story was mapped to five executable scenarios:

| Scenario | Automated test |
| --- | --- |
| Copy and paste one selected rectangle | `givenDrawingWithOneSelectedRectangle_whenUserCopiesAndPastesIt_thenDrawingContainsTwoRectanglesAndPastedRectangleIsSelected` |
| Copy with no selection | `givenDrawingViewWithNoSelectedFigures_whenUserInvokesCopy_thenNoDrawingFigureTransferableIsProduced` |
| Paste unsupported data | `givenClipboardContainsUnsupportedData_whenUserInvokesPaste_thenDrawingRemainsUnchanged` |
| Undo and redo pasted figures | `givenUserPastedFiguresIntoDrawing_whenUserInvokesUndoAndRedo_thenPastedFiguresAreRemovedAndAddedBack` |
| Paste plain text through `TextInputFormat` | `givenClipboardContainsPlainTextAndDrawingRegistersTextInputFormat_whenUserInvokesPaste_thenTextHolderFigureIsAdded` |

The automation lives in:

```text
jhotdraw-samples/jhotdraw-samples-misc/src/test/java/org/jhotdraw/samples/draw/CopyPasteBddTest.java
```

## Verification

The BDD-style test class ran as part of the sample module test command:

```text
Running org.jhotdraw.samples.draw.CopyPasteBddTest
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```

## Detailed Artifact

See:

```text
BobsWork/10_BDDLab_Copy_Paste.md
```

---

# Current Status

The Copy/Paste lab sequence is complete in the workspace. The local feature branch is `copy-paste-basic-editing-labs`; the work still needs to be committed/pushed before it becomes part of Git history or remote CI.
