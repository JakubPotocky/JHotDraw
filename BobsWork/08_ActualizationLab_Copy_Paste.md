# ActualizationLab - Actualization For Copy/Paste

## Feature Under Maintenance

The selected Basic Editing feature is copy/paste:

- Copy #13: As a user, I want to copy a selected object so that I can quickly duplicate existing work without recreating it manually.
- Paste #14: As a user, I want to paste a copied object so that I can quickly place a duplicate on the canvas.

This exercise does not cover the rest of Basic Editing, such as moving, resizing, or appearance changes. Those belong to the parent issue context, but Bob's implementation and portfolio scope is copy and paste only.

## Lab Goal

The goal of ActualizationLab is to explain how the implemented refactoring is incorporated into the existing JHotDraw system. The work is maintenance of an existing feature, not a new copy/paste design from scratch.

The actual code changes were:

| File | Actual change | Purpose |
| --- | --- | --- |
| `jhotdraw-core/src/main/java/org/jhotdraw/draw/DefaultDrawingViewTransferHandler.java` | Extracted `importTransferData(...)`. | Centralize the repeated successful transferable-paste sequence. |
| `jhotdraw-core/src/main/java/org/jhotdraw/draw/DefaultDrawingViewTransferHandler.java` | Extracted `firePasteUndoableEdit(...)`. | Centralize paste undo/redo registration. |
| `jhotdraw-core/src/main/java/org/jhotdraw/draw/DefaultDrawingViewTransferHandler.java` | Removed unused private `getDrawing()` method. | Remove dead, misleading, unimplemented code. |
| `jhotdraw-core/src/main/java/org/jhotdraw/draw/DefaultDrawingViewTransferHandler.java` | Improved comments around repainting and failed transferable reads. | Make the existing behavior easier to understand. |
| `jhotdraw-datatransfer/src/main/java/org/jhotdraw/datatransfer/CompositeTransferable.java` | Corrected typo-level Javadoc issues. | Improve documentation quality without changing runtime behavior. |

## Existing Copy/Paste Flow

Copy and paste are incorporated through Swing's existing `TransferHandler` mechanism.

### Copy Flow

```text
CopyAction.actionPerformed(...)
  -> focused JComponent
  -> component.getTransferHandler().exportToClipboard(..., TransferHandler.COPY)
  -> DefaultDrawingViewTransferHandler.createTransferable(...)
  -> Drawing.getOutputFormats()
  -> OutputFormat.createTransferable(...)
  -> CompositeTransferable
  -> clipboard
```

The important code evidence is that `CopyAction` does not know about figures, drawings, or file formats directly. It resolves the focused component and delegates to `TransferHandler.exportToClipboard(...)`. For a drawing view, `DefaultDrawingView` installs `DefaultDrawingViewTransferHandler`, and that handler creates a transferable from the selected figures.

### Paste Flow

```text
PasteAction.actionPerformed(...)
  -> ClipboardUtil.getClipboard().getContents(...)
  -> component.getTransferHandler().importData(...)
  -> DefaultDrawingViewTransferHandler.importData(...)
  -> Drawing.getInputFormats()
  -> InputFormat.read(...)
  -> imported figures added to Drawing
  -> view selection updated
  -> paste undoable edit fired
```

The important code evidence is that `PasteAction` only retrieves clipboard contents and delegates import to the focused component's transfer handler. `DefaultDrawingViewTransferHandler` then uses the drawing's registered `InputFormat` objects to read supported clipboard data.

## Why `DefaultDrawingViewTransferHandler` Was The Correct Change Point

`DefaultDrawingViewTransferHandler` was the correct local change point because it is the class where the copy/paste feature becomes concrete drawing behavior.

| Responsibility | Why it belongs in `DefaultDrawingViewTransferHandler` |
| --- | --- |
| Export selected figures | The handler receives the `DrawingView` component and calls `view.getSelectedFigures()`. |
| Build transferable clipboard content | The handler asks the drawing's `OutputFormat` strategies to create transferables. |
| Import clipboard content | The handler searches the drawing's `InputFormat` strategies and calls `format.read(...)`. |
| Update selection after paste | The handler can call `view.clearSelection()` and `view.addToSelection(...)`. |
| Move imported figures for drag/drop | The handler has access to the drop point and drawing-view coordinate conversion. |
| Register paste undo behavior | The handler knows exactly which figures were imported by the paste. |

Changing `CopyAction` or `PasteAction` would have been the wrong level for this refactoring. Those classes are generic action entry points. They should stay focused on resolving the target component and delegating to Swing. They should not learn how drawings, figures, `InputFormat`, or `OutputFormat` work.

Changing concrete input/output format classes would also have been too low-level. The duplication was not inside a specific format such as DOM, serialization, text, or image import. The duplication was in the handler sequence after a matching format was found.

## Incorporation Into Swing `TransferHandler`

The refactoring keeps the public Swing integration points the same:

| Swing method or concept | Existing role | Effect of the refactoring |
| --- | --- | --- |
| `TransferHandler.exportToClipboard(...)` | Used by `CopyAction` to start copy. | Unchanged. |
| `TransferHandler.createTransferable(...)` | Overridden by the drawing handler to export selected figures. | Unchanged behavior. |
| `TransferHandler.importData(...)` | Used by `PasteAction` and drag/drop to import data. | Same entry point, cleaner internal structure. |
| `TransferSupport.getDropLocation()` | Supplies drag/drop paste location. | Unchanged. `moveToDropPoint(...)` is still called after import. |
| `ClipboardUtil.getClipboard()` | Supplies the clipboard used by copy and paste actions. | Unchanged. |

This matters because copy/paste is not wired manually through a custom command chain. It is incorporated through Swing's standard transfer API, and the refactoring preserves that contract.

## Use Of JHotDraw Extension Points

The implemented code relies on JHotDraw extension points instead of hard-coding clipboard formats.

| Extension point | How copy/paste uses it |
| --- | --- |
| `Drawing` | Provides `getInputFormats()` and `getOutputFormats()` for paste and copy. |
| `AbstractDrawing` | Stores the registered input and output format lists. |
| `OutputFormat` | Creates transferable data from selected figures during copy. |
| `InputFormat` | Reads transferable data and adds figures during paste. |
| `DrawingView` | Provides the selected figures for copy and receives selection updates after paste. |
| `Figure` | Represents the copied and pasted domain objects. |
| `CompositeTransferable` | Combines several transferable formats into one clipboard object. |

This design supports Open/Closed Principle in a practical way. A drawing can support additional clipboard formats by registering different `InputFormat` and `OutputFormat` implementations. The transfer handler does not need to know the concrete format classes.

## Local And Composite Responsibilities

Copy/paste is a composite feature, but the refactoring kept local responsibilities local.

| Responsibility type | Examples |
| --- | --- |
| Local to `DefaultDrawingViewTransferHandler` | Finding a compatible input format, importing the transferable, calculating newly imported figures, updating selection, moving dropped figures, firing paste undo. |
| Local to actions | `CopyAction` and `PasteAction` resolve the target component and delegate to its transfer handler. |
| Local to the drawing model | `Drawing` owns figures and the input/output format lists. |
| Local to input/output strategies | `InputFormat` and `OutputFormat` know how to read or write a specific data format. |
| Local to clipboard utilities | `ClipboardUtil`, `AWTClipboard`, and `OSXClipboard` provide clipboard access and platform handling. |
| Composite feature behavior | The user-visible copy/paste workflow emerges from actions, Swing transfer handling, drawing formats, selection state, clipboard utilities, and undo events working together. |

The refactoring does not pretend that copy/paste belongs to one class. Instead, it makes the central handler easier to maintain while preserving the existing collaboration.

## Change Propagation

The change propagation was intentionally limited.

| Area | Changed? | Reason |
| --- | --- | --- |
| `DefaultDrawingViewTransferHandler` | Yes | It contained the duplicated paste import and undo logic. |
| `CompositeTransferable` | Comment cleanup only | It had typo-level documentation smells. |
| `CopyAction` | No | Its delegation to `TransferHandler.exportToClipboard(...)` was already correct. |
| `PasteAction` | No | Its delegation to `TransferHandler.importData(...)` was already correct. |
| `CutAction` | No | Related clipboard action, but not needed for this copy/paste refactoring. |
| `DefaultDrawingView` | No | It already installs the correct transfer handler in its constructor. |
| `InputFormat` implementations | No | The import behavior was not format-specific. |
| `OutputFormat` implementations | No | Copy/export behavior was not the duplicated area. |
| Menu, toolbar, and shortcut wiring | No | The user entry points already reached the existing actions. |

The main maintenance decision was to avoid turning a small refactoring into a cross-module redesign. The impact analysis already showed that copy/paste spans several packages. Actualization keeps the changed set close to the actual smell.

## Clean Code And Maintainability

The implemented refactoring supports Clean Code in these ways:

| Principle | Application in this change |
| --- | --- |
| Meaningful names | `importTransferData(...)` and `firePasteUndoableEdit(...)` describe the intent of extracted behavior. |
| Avoid duplication | The successful paste sequence is no longer repeated in both Mac and non-Mac search branches. |
| Small focused methods | The large `importData(...)` method still exists, but important sub-responsibilities are now named and separated. |
| Remove dead code | The unused unimplemented private `getDrawing()` method was removed. |
| Trustworthy comments | Comments now explain fallback behavior without typo noise or distracting wording. |

## SOLID Discussion

| SOLID principle | Relation to Bob's copy/paste change |
| --- | --- |
| Single Responsibility Principle | The transfer handler still has broad responsibilities, but extracted methods give paste import and paste undo registration clearer internal responsibilities. |
| Open/Closed Principle | Clipboard format support remains open through `InputFormat` and `OutputFormat` strategies, not through modifying action classes. |
| Liskov Substitution Principle | No public interface contracts were changed, so existing `InputFormat`, `OutputFormat`, `Drawing`, and `DrawingView` implementations remain substitutable. |
| Interface Segregation Principle | The change does not add methods to broad interfaces. It reuses existing focused contracts. |
| Dependency Inversion Principle | The handler works through abstractions such as `Drawing`, `DrawingView`, `InputFormat`, and `OutputFormat`, rather than depending on one concrete clipboard format implementation. |

The SOLID conclusion is not that the class is perfect. `DefaultDrawingViewTransferHandler` is still a large integration class. The improvement is that the most duplicated paste behavior now has clearer internal boundaries.

## Clean Architecture Discussion

JHotDraw is not organized as textbook Clean Architecture, but the copy/paste feature still has useful boundary separation:

| Layer or boundary | Example in this feature |
| --- | --- |
| User action boundary | `CopyAction` and `PasteAction` represent user commands. |
| Swing integration boundary | `TransferHandler` and `Transferable` connect the application to platform clipboard behavior. |
| Drawing view boundary | `DrawingView` exposes selection and coordinate behavior. |
| Domain/model boundary | `Drawing` owns figures and undoable edit notifications. |
| Format strategy boundary | `InputFormat` and `OutputFormat` isolate data-format details. |

The actualization keeps these boundaries intact. No UI action was made responsible for parsing formats. No format class was made responsible for selection or undo. No drawing model class was made responsible for clipboard access.

## Remaining Risks

| Risk | Explanation |
| --- | --- |
| GUI workflow is not fully automated | The Maven test run does not prove a full user copy/paste scenario in the Draw sample UI. |
| Platform clipboard behavior can vary | The handler still contains a Mac OS X data flavor ordering workaround, which is hard to validate without platform-specific testing. |
| `importData(...)` remains broad | The method still handles transferable import, file-list import, drag/drop details, and broad exception handling. |
| File-list import is asynchronous | The `SwingWorker` path has different timing from ordinary clipboard paste. |
| Full GUI workflow is not automated | TestLab1 and BDDLab verify undo/redo at component level, but menu/keyboard focus behavior still needs GUI or manual validation. |

## Verification Status

The code refactoring and added tests were verified with:

```text
mvn -pl jhotdraw-samples/jhotdraw-samples-misc test
```

Result:

```text
Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The root CI-style test command was also run:

```text
mvn test --file pom.xml
```

Result: `BUILD SUCCESS`.

## What Was Tested Next

TestLab1 and BDDLab now cover the main behavior that was previously listed as future work:

| Test target | Covered by |
| --- | --- |
| Copy selected figures | `DefaultDrawingViewTransferHandlerTest.copySelectedFigureCreatesTransferable` |
| Copy with no selected figures | `DefaultDrawingViewTransferHandlerTest.copyWithoutSelectionCreatesNoTransferable` |
| Paste supported transferable | `DefaultDrawingViewTransferHandlerTest.pasteSupportedTransferableAddsAndSelectsImportedFigure` |
| Selection after paste | `DefaultDrawingViewTransferHandlerTest` and `CopyPasteBddTest` |
| Unsupported clipboard data | `DefaultDrawingViewTransferHandlerTest.pasteUnsupportedTransferableLeavesDrawingUnchanged` and BDD scenario 3 |
| Paste undo/redo | `DefaultDrawingViewTransferHandlerTest.pasteFiresUndoableEditThatCanUndoAndRedoImportedFigure` and BDD scenario 4 |
| Paste text | BDD scenario 5 using `TextInputFormat` |

## Portfolio Conclusion

The copy/paste refactoring was actualized by making a small internal improvement at the correct integration point: `DefaultDrawingViewTransferHandler`. The change preserves Swing `TransferHandler` behavior, keeps JHotDraw's format strategy extension points intact, avoids unnecessary propagation into actions and UI wiring, and improves maintainability by removing duplication and dead code.

The next exercise is TestLab1.
