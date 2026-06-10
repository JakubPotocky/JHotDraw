# CLLab - Concept Location For Copy/Paste

## Lab Goal

The Concept Location Lab asks students to use dynamic program analysis, debugger tracing, and code inspection to locate the classes involved in the selected feature. The required portfolio deliverable is an initial set of classes in table format:

| Domain Class | Responsibility |
| --- | --- |

For Bob's feature, the selected concepts are copy and paste in JHotDraw's Basic editing area.

## Feature Under Analysis

Copy user story:

As a user, I want to copy a selected object so that I can quickly duplicate existing work without recreating it manually.

Paste user story:

As a user, I want to paste a copied object so that I can quickly place a duplicate on the canvas.

## Concept Location Strategy

The codebase was searched for copy, paste, clipboard, transferable, input/output format, and drawing view transfer behavior.

Important observation:

`CopyAction` and `PasteAction` do not directly clone or insert figures. They are controller actions that delegate to Swing's `TransferHandler`. In the drawing canvas, the active transfer handler is `DefaultDrawingViewTransferHandler`.

The runtime concept chain is therefore:

```text
User command
  -> CopyAction or PasteAction
  -> focused JComponent
  -> JComponent.getTransferHandler()
  -> DefaultDrawingViewTransferHandler
  -> DrawingView / Drawing
  -> OutputFormat or InputFormat
  -> Clipboard / Transferable
```

## Copy Scenario

Expected runtime flow for copying selected figures:

```text
CopyAction.actionPerformed()
  -> resolve focused JComponent
  -> c.getTransferHandler().exportToClipboard(...)
  -> DefaultDrawingViewTransferHandler.createTransferable(...)
  -> Drawing.sort(selectedFigures)
  -> each OutputFormat creates a Transferable
  -> CompositeTransferable stores supported flavors
  -> ClipboardUtil.getClipboard()
  -> AWT/OSX/JNLP/JVM clipboard receives content
```

## Paste Scenario

Expected runtime flow for pasting copied figures:

```text
PasteAction.actionPerformed()
  -> resolve focused JComponent
  -> ClipboardUtil.getClipboard().getContents(c)
  -> c.getTransferHandler().importData(c, transferable)
  -> DefaultDrawingViewTransferHandler.importData(...)
  -> choose supported InputFormat based on DataFlavor
  -> InputFormat.read(...)
  -> figures are added to Drawing
  -> view.clearSelection()
  -> view.addToSelection(importedFigures)
  -> drawing.fireUndoableEditHappened(...)
```

## Initial Set Of Classes

| Domain Class | Responsibility |
| --- | --- |
| `CopyAction` | Controller entry point for Edit -> Copy and copy shortcuts. It resolves the currently focused `JComponent` and delegates copying to the component's `TransferHandler` using `TransferHandler.COPY`. |
| `PasteAction` | Controller entry point for Edit -> Paste and paste shortcuts. It resolves the currently focused `JComponent`, gets clipboard contents through `ClipboardUtil`, and delegates import to the component's `TransferHandler`. |
| `CutAction` | Closely related controller action using the same transfer mechanism as copy, but with `TransferHandler.MOVE`. It is relevant because copy/paste logic shares clipboard/export behavior with cut. |
| `AbstractSelectionAction` | Shared base class for selection-based edit actions. It stores an optional target component and updates enablement based on component enabled state or `EditableComponent.SELECTION_EMPTY_PROPERTY`. |
| `EditableComponent` | Interface for editable components. It defines selection-related editing operations such as delete, duplicate, select all, clear selection, and selection-empty state. It is referenced by action enablement and related basic editing actions. |
| `DefaultDrawingView` | The drawing canvas component. It installs `DefaultDrawingViewTransferHandler`, owns the current selection, exposes selected figures, and supports selection changes after paste. |
| `DrawingView` | Interface for the drawing view abstraction. It provides access to the drawing, selected figures, selection count, coordinate conversion, and component-level editing behavior. |
| `DefaultDrawingViewTransferHandler` | Core copy/paste transfer class for drawing views. It creates transferables from selected figures during copy, imports figures from transferables during paste, selects imported figures, moves dropped figures, and creates undoable edits. |
| `Drawing` | Domain model containing figures and registered input/output formats. Copy uses output formats; paste uses input formats. |
| `AbstractDrawing` | Base implementation of `Drawing` that stores `inputFormats` and `outputFormats` and exposes them to the transfer handler. |
| `Figure` | Domain entity being copied and pasted. Selected figures are exported; imported figures are added back to the drawing and selected. |
| `OutputFormat` | Strategy-like interface for exporting selected figures into a `Transferable`. Used by copy. |
| `InputFormat` | Strategy-like interface for importing figures from a `Transferable`. Used by paste. |
| `DOMStorableInputOutputFormat` | Draw sample's main input/output format for drawing clips. It can create a transferable for selected figures and read figures back from a transferable. |
| `SerializationInputOutputFormat` | Alternative input/output format that clones selected figures into a transferable drawing representation. Relevant to generic JHotDraw transfer support. |
| `ImageOutputFormat` | Exports selected figures as an image transferable. Relevant because copy can place multiple data flavors on the clipboard. |
| `ImageInputFormat` | Imports image clipboard data as an image figure. Relevant to paste when the clipboard contains image data. |
| `TextInputFormat` | Imports string clipboard data as text figures. Relevant to paste when clipboard data is plain text. |
| `CompositeTransferable` | Combines multiple `Transferable` objects so the clipboard can offer several data flavors for the same copy operation. |
| `ClipboardUtil` | Central utility for retrieving the clipboard. It tries system clipboard access and falls back to JNLP or a JVM-local clipboard. |
| `AWTClipboard` | Proxy wrapper around the Java AWT clipboard. |
| `OSXClipboard` | Mac-specific clipboard proxy that can combine AWT clipboard contents with native OS X transferable data. |
| `DefaultApplicationModel` | Registers `CopyAction`, `PasteAction`, `CutAction`, and related edit actions in the application action map. |
| `DefaultMenuBuilder` | Adds Copy, Paste, Cut, Duplicate, and Delete actions to the Edit menu. |
| `DefaultDrawingEditor` | Maps Ctrl/Cmd+C and Ctrl/Cmd+V keystrokes to copy and paste action IDs and installs editor-level copy/paste actions. |
| `ButtonFactory` | Creates drawing action collections that include Copy and Paste for toolbars/popup UI. |
| `DrawingPanel` | Sample UI panel that adds Copy and Paste actions to a popup action button in the Draw sample. |
| `DrawView` | Sample view that creates the drawing and registers the input/output formats used by copy/paste. |

Total initial concept-location classes: 28.

## Most Central Classes

The most central classes for explaining copy/paste are:

1. `CopyAction`.
2. `PasteAction`.
3. `DefaultDrawingView`.
4. `DefaultDrawingViewTransferHandler`.
5. `Drawing`.
6. `InputFormat`.
7. `OutputFormat`.
8. `DOMStorableInputOutputFormat`.
9. `CompositeTransferable`.
10. `ClipboardUtil`.

## What I Learned

Copy/paste in JHotDraw is intentionally layered:

- The action classes know about user commands and focused components.
- Swing `TransferHandler` performs the component-level transfer operation.
- `DefaultDrawingViewTransferHandler` knows how a drawing view exports and imports figures.
- `Drawing` supplies input/output formats.
- Input/output formats decide the concrete clipboard representation.
- Clipboard proxy classes hide system clipboard differences.

This means copy/paste is not implemented as one method called `copyFigure()` or `pasteFigure()`. It is a framework collaboration between actions, Swing transfer infrastructure, the drawing model, and registered formats.

## Next Exercise

The next exercise is AnalysisLab1. The impact-analysis table should classify which packages and classes are likely changed, unchanged, or only inspected for the copy/paste portfolio work.

