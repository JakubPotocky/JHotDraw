# AnalysisLab1 - Impact Analysis For Copy/Paste

## Lab Goal

The Impact Analysis Lab asks students to apply static and dynamic analysis to find the estimated impacted set of classes for a selected change request. The portfolio deliverable is a table listing packages, the number of visited classes, and comments explaining what was learned about each package and how it contributes to the feature.

## Feature Under Analysis

Feature: Basic editing - copy and paste.

Copy user story:

As a user, I want to copy a selected object so that I can quickly duplicate existing work without recreating it manually.

Paste user story:

As a user, I want to paste a copied object so that I can quickly place a duplicate on the canvas.

## Important Scope Note

The copy/paste feature already exists in this JHotDraw codebase. Therefore the impact analysis has two layers:

1. Feature comprehension impact: which classes must be understood to explain copy/paste.
2. Refactoring impact: which classes would probably be changed in the later code-smell/refactoring exercise.

For the current portfolio, the most likely actual code change is not to implement copy/paste from scratch. The most likely actual change is preventive/refactoring work around copy/paste maintainability.

## Initial Impact Set

The initial impact set comes directly from concept location:

| Class | Reason It Starts In The Impact Set |
| --- | --- |
| `CopyAction` | User command entry point for copying selected content. |
| `PasteAction` | User command entry point for pasting clipboard content. |
| `DefaultDrawingViewTransferHandler` | Core class that exports selected figures and imports pasted figures. |
| `DefaultDrawingView` | Drawing component that owns selection and installs the transfer handler. |
| `Drawing` | Model object that supplies figures plus input/output formats. |
| `DOMStorableInputOutputFormat` | Main Draw sample format for transferring drawing figures. |
| `ClipboardUtil` | Provides the clipboard used by copy and paste actions. |

## Estimated Impact Set For Refactoring

If the later exercise refactors copy/paste code smells, the estimated changed set is:

| Class | Estimated Mark | Reason |
| --- | --- | --- |
| `DefaultDrawingViewTransferHandler` | Changed | Contains duplicated import loops, large mixed responsibility, dead private `getDrawing()` method, typo comments, and paste undo logic. |
| `CopyAction` | Possibly changed | Shares repeated "resolve focused component" logic with `CutAction` and `PasteAction`. |
| `CutAction` | Possibly changed | Same focus-target lookup and clipboard export pattern as `CopyAction`. |
| `PasteAction` | Possibly changed | Same focus-target lookup pattern; paste-specific import logic should remain. |
| `CompositeTransferable` | Possibly changed | Has comment typos and direct concrete collection fields; likely documentation/naming cleanup only. |
| `DefaultDrawingView` | Unchanged unless tests require setup hooks | Installs the transfer handler and owns selection, but the current feature behavior should not require changing it. |
| `Drawing` / `AbstractDrawing` | Unchanged | Input/output format registration already supports copy/paste. |
| `InputFormat` / `OutputFormat` implementations | Unchanged | They already provide strategy-like read/write behavior for transfer. |
| `DrawView` / `DrawingPanel` | Unchanged | They already wire formats and actions for the Draw sample. |

## Table 1 - Packages Visited During Impact Analysis

| Package name | # of classes visited | Comments |
| --- | ---: | --- |
| `org.jhotdraw.action.edit` | 5 | Visited `CopyAction`, `PasteAction`, `CutAction`, `DuplicateAction`, and `AbstractSelectionAction`. This package contains the user-command entry points for basic editing. `CopyAction` and `PasteAction` are thin controllers; they do not copy figures directly. They resolve the focused component and delegate to Swing transfer infrastructure. `CutAction` is relevant because it uses the same clipboard export mechanism with `TransferHandler.MOVE`. `DuplicateAction` is adjacent because it duplicates selected figures without using the clipboard. |
| `org.jhotdraw.api.gui` | 1 | Visited `EditableComponent`. It defines generic editing operations and selection-empty state. Copy/paste themselves use `TransferHandler`, but this interface is still important because the action hierarchy and nearby basic editing commands rely on it. |
| `org.jhotdraw.draw` | 6 | Visited `DefaultDrawingView`, `DrawingView`, `DefaultDrawingViewTransferHandler`, `Drawing`, `AbstractDrawing`, and `DefaultDrawingEditor`. This is the most important package for copy/paste. `DefaultDrawingView` installs the transfer handler; `DefaultDrawingViewTransferHandler` implements the real export/import behavior; `Drawing` and `AbstractDrawing` provide registered input/output formats; `DefaultDrawingEditor` maps Ctrl/Cmd+C and Ctrl/Cmd+V to copy/paste action IDs. |
| `org.jhotdraw.draw.figure` | 3 | Visited `Figure`, `ImageHolderFigure`, and `TextHolderFigure`. `Figure` is the copied/pasted domain object. `ImageHolderFigure` and `TextHolderFigure` matter because image and text clipboard imports create figure objects from clipboard data. |
| `org.jhotdraw.draw.io` | 7 | Visited `InputFormat`, `OutputFormat`, `DOMStorableInputOutputFormat`, `SerializationInputOutputFormat`, `ImageInputFormat`, `ImageOutputFormat`, and `TextInputFormat`. This package supplies the strategy-like formats used by the transfer handler. Copy asks output formats to create transferables. Paste asks input formats whether they support a data flavor and then reads clipboard data into the drawing. |
| `org.jhotdraw.datatransfer` | 7 | Visited `ClipboardUtil`, `CompositeTransferable`, `AbstractClipboard`, `AWTClipboard`, `OSXClipboard`, `InputStreamTransferable`, and `ImageTransferable`. This package hides clipboard differences and represents clipboard data. `CompositeTransferable` is especially important because copy can offer multiple data flavors for the same selection. |
| `org.jhotdraw.app` | 2 | Visited `DefaultApplicationModel` and `DefaultMenuBuilder`. This package registers copy/paste actions and places them in the application menu. It contributes UI command wiring rather than figure transfer logic. |
| `org.jhotdraw.gui.action` | 1 | Visited `ButtonFactory`. It creates drawing action collections that include Copy and Paste, so it contributes toolbar/popup exposure. |
| `org.jhotdraw.samples.draw` | 3 | Visited `DrawView`, `DrawingPanel`, and `DrawFigureFactory`. The sample registers input/output formats in `DrawView` and exposes copy/paste actions in `DrawingPanel`. This package is important for understanding how the generic framework feature appears in the Draw sample application. |

Total packages visited: 9.

Total classes visited: 35.

## Impact Analysis Findings

### Finding 1 - Copy/Paste Is A Composite Feature

The feature crosses application actions, Swing transfer infrastructure, drawing model classes, input/output format strategies, and clipboard proxy classes. This means copy/paste is composite functionality rather than local functionality.

### Finding 2 - `DefaultDrawingViewTransferHandler` Is The Hotspot For Refactoring

Most feature behavior passes through `DefaultDrawingViewTransferHandler`. It exports selected figures, imports transfer data, selects imported figures, moves dropped figures, creates undoable paste edits, handles file-list drops, and participates in drag-and-drop. This is the most likely changed class for the code-smell/refactoring exercise.

### Finding 3 - Most Other Classes Should Stay Unchanged

The action classes, formats, drawing, and sample wiring mostly already provide the needed extension points. Changing many of them would likely be unnecessary unless the refactoring explicitly targets duplicated action logic.

### Finding 4 - The Existing Design Uses Useful Variation Points

`InputFormat` and `OutputFormat` make paste/copy extensible. Adding or supporting another data type can often be done by registering another format rather than changing `CopyAction` or `PasteAction`.

## Candidate Code Smells For The Next Exercise

| Location | Smell | Why It Matters |
| --- | --- | --- |
| `DefaultDrawingViewTransferHandler.importData` | Duplicated code | The Mac and non-Mac branches contain near-identical import, select, move, and undo logic. |
| `DefaultDrawingViewTransferHandler` | Large class / multiple responsibilities | The class handles clipboard import/export, drag-and-drop, file import, selection updates, movement to drop point, and undo creation. |
| `DefaultDrawingViewTransferHandler.getDrawing()` | Dead code / speculative generality | Private method always throws `UnsupportedOperationException` and is not used. |
| `CopyAction`, `CutAction`, `PasteAction` | Duplicated focus lookup | Each action repeats logic for resolving the focused `JComponent`. |
| `CompositeTransferable` | Comment typo / small documentation smell | "ComoositeTransferable" and "wjether" reduce professionalism and readability. |
| `DefaultDrawingViewTransferHandler` comments | Typo and negative comment smell | "transferalbe" typo and "ugly code sequence" comment indicate code that wants cleanup or clearer intent. |

## Estimated Refactoring Direction

The safest first refactoring is small and behavior-preserving:

1. Remove the unused private `getDrawing()` method from `DefaultDrawingViewTransferHandler`.
2. Fix typo comments in transfer-related classes.
3. Optionally extract repeated import-success behavior from `DefaultDrawingViewTransferHandler.importData` into a helper method.

The bigger refactoring, extracting a reusable focus-target resolver for `CopyAction`, `CutAction`, and `PasteAction`, is also valid but touches more classes.

## AnalysisLab Conclusion

The estimated impact set for copy/paste comprehension is broad, but the estimated changed set for a safe portfolio refactoring is narrow. The central class is `DefaultDrawingViewTransferHandler`. Most other packages are inspected to understand the feature but should remain unchanged unless a specific refactoring requires them.

