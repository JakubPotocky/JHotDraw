# RefactoringLab1 - Code Smells And Refactoring For Copy/Paste

## Feature Under Maintenance

The selected feature is Basic editing copy/paste:

- Copy #13: As a user, I want to copy a selected object so that I can quickly duplicate existing work without recreating it manually.
- Paste #14: As a user, I want to paste a copied object so that I can quickly place a duplicate on the canvas.

The relevant implementation is mainly located around Swing transfer handling in JHotDraw. The most important class is:

```text
jhotdraw-core/src/main/java/org/jhotdraw/draw/DefaultDrawingViewTransferHandler.java
```

The refactoring exercise focused on making this existing copy/paste implementation easier to understand and maintain without changing its behavior.

## Lab Goal

The purpose of this lab is to identify code smells in the copy/paste feature and apply behavior-preserving refactorings. The work should not redesign the feature from scratch. It should improve maintainability while keeping the public behavior the same:

- Copy still exports selected figures through the drawing view transfer handler.
- Paste still imports clipboard data through available input formats.
- Pasted figures still become selected.
- Paste still creates an undoable edit.
- Drag/drop paste still moves pasted figures to the drop location.
- Unsupported input formats still fail safely and allow the next format to be tried.

## Classes Inspected

| Class | Role In Copy/Paste | Refactoring Status |
| --- | --- | --- |
| `CopyAction` | Action entry point for copying selected content through Swing `TransferHandler`. | Inspected, not changed. |
| `PasteAction` | Action entry point for importing clipboard content through Swing `TransferHandler`. | Inspected, not changed. |
| `DefaultDrawingViewTransferHandler` | Main copy/paste transfer implementation for drawing views. | Changed. |
| `CompositeTransferable` | Combines several transferable formats for clipboard data. | Comment cleanup only. |
| `Drawing` / `AbstractDrawing` | Store the input and output formats used by copy/paste. | Inspected, not changed. |
| `InputFormat` / `OutputFormat` classes | Read and write transferable figure data. | Inspected, not changed. |

## Code Smell 1: Duplicated Paste Import Logic

### Location

```text
DefaultDrawingViewTransferHandler.importData(...)
```

### Smell Type

Duplicated Code.

### Problem

The paste path had two search branches:

- A Mac OS X branch, which loops over `InputFormat` objects first and data flavors second.
- A non-Mac branch, which loops over data flavors first and `InputFormat` objects second.

The loop order is intentionally different because the source code contains a workaround for Mac OS X data flavor ordering. However, once a supported format was found, both branches performed the same import sequence:

1. Remember the current figures in the drawing.
2. Read the transferable into the drawing using the matching `InputFormat`.
3. Compute which figures were newly imported.
4. Clear the current selection.
5. Select the newly imported figures.
6. Add imported figures to the transfer figure set.
7. Move imported figures to the drop point when paste happens through drag/drop.
8. Create an undoable paste edit.
9. Return success.
10. If reading failed, print the exception and try another format.

That behavior was repeated in both branches. This makes maintenance risky because a future fix could be applied to one branch but forgotten in the other.

### Refactoring Applied

Extract Method.

The repeated behavior was extracted into:

```java
private boolean importTransferData(
        final JComponent comp,
        Transferable t,
        final HashSet<Figure> transferFigures,
        final Point dropPoint,
        final DrawingView view,
        final Drawing drawing,
        InputFormat format) throws UnsupportedFlavorException
```

### Result

The Mac and non-Mac branches still keep their different search order, but they now delegate the shared import work to one method. The loops are easier to read because they now express only the search logic:

- Find a supported `InputFormat`.
- Ask `importTransferData(...)` to perform the actual paste.
- Stop searching when paste succeeds.

## Code Smell 2: Duplicated Undoable Paste Edit

### Location

```text
DefaultDrawingViewTransferHandler.importData(...)
```

### Smell Type

Duplicated Code / Long Method.

### Problem

Paste creates an `AbstractUndoableEdit` so the user can undo and redo the paste operation. The same undoable-edit construction appeared in more than one import path. This creates two maintenance problems:

- The presentation name lookup for `edit.paste.text` could drift between branches.
- The undo/redo behavior could accidentally become inconsistent for clipboard paste versus file-list import.

The underlying responsibility is conceptually the same in both places: after figures have been imported, register one undoable edit that removes them on undo and adds them again on redo.

### Refactoring Applied

Extract Method.

The undoable edit creation was extracted into:

```java
private void firePasteUndoableEdit(
        final Drawing drawing,
        final LinkedList<Figure> importedFigures)
```

### Result

All paste import paths can now reuse one operation for undo registration. The method name also documents the intention: this is not general drawing logic; it specifically fires the undoable edit for a paste action.

## Code Smell 3: Dead Private Method

### Location

```text
DefaultDrawingViewTransferHandler.getDrawing()
```

### Smell Type

Dead Code / Speculative Generality.

### Problem

The class contained a private method that threw:

```text
UnsupportedOperationException("Not yet implemented")
```

It was not used by the class. Since it was private and unimplemented, it did not represent a useful extension point. Leaving this kind of method in production code makes maintenance harder because a future maintainer may waste time trying to understand whether it is part of an unfinished feature.

### Refactoring Applied

Remove Dead Code.

The unused private method was removed.

### Result

The class now has less misleading code. Maintainers can focus on the real transfer-handler behavior.

## Code Smell 4: Misleading Or Low-Quality Comments

### Locations

```text
DefaultDrawingViewTransferHandler.java
CompositeTransferable.java
```

### Smell Type

Comment Smell / Documentation Noise.

### Problem

Some comments contained typos or wording that made the code look less professional:

- A comment used wording like "ugly code sequence" to describe fallback behavior.
- Documentation in `CompositeTransferable` contained misspellings of the class name and words such as "whether".

This does not normally affect runtime behavior, but comments are part of maintainability. In a maintenance course portfolio, unclear comments matter because they can slow down program comprehension.

### Refactoring Applied

Comment cleanup.

The comments were changed to describe behavior more directly:

- Failed transferable reads now say that the handler will try the next `InputFormat`.
- `CompositeTransferable` documentation now spells the class name correctly.
- Typo-level documentation errors were corrected.

### Result

The code is a little easier to read, and the comments no longer distract from the actual copy/paste behavior.

## Code Smells Considered But Not Changed

Not every possible smell should be fixed in one lab. Some possible future refactorings were intentionally left alone because they would increase the impact set.

| Possible Smell | Location | Why It Was Not Changed Now |
| --- | --- | --- |
| Repeated focused-component lookup | `CopyAction`, `CutAction`, `PasteAction` | Fixing this cleanly would touch several action classes and might require a shared helper or base-class change. The current lab kept the changed set narrow. |
| Large transfer-handler responsibility | `DefaultDrawingViewTransferHandler` | The class handles copy, paste, drag/drop, file-list import, selection, movement, and undo. Splitting it may be useful later, but it would be a larger design refactoring. |
| Broad exception handling | `DefaultDrawingViewTransferHandler.importData(...)` | The method catches `Throwable`, which is broad. Changing this could affect error behavior and was not necessary for the selected duplicate-code cleanup. |
| GUI-level behavior without direct automated test | Copy/paste workflow | More test work belongs in TestLab1 or BDDLab. RefactoringLab1 focused on behavior-preserving structure. |

## Before And After Summary

| Area | Before | After |
| --- | --- | --- |
| Transfer import logic | Repeated in Mac and non-Mac search branches. | Shared in `importTransferData(...)`. |
| Paste undo edit | Repeated anonymous `AbstractUndoableEdit` creation. | Shared in `firePasteUndoableEdit(...)`. |
| Dead method | Private unimplemented `getDrawing()` existed. | Removed. |
| Comments | Some comments had typos or low-quality wording. | Comments are clearer and typo corrections were applied. |
| Behavior | Existing copy/paste behavior. | Intended to remain unchanged. |

## Why The Refactoring Is Behavior-Preserving

The refactoring is behavior-preserving because it moves existing logic into named helper methods without changing the order or meaning of the important operations.

For transferable paste, the same behavior remains:

1. Store the existing figures before reading.
2. Call `format.read(t, drawing, false)`.
3. Compare the drawing after reading with the previous figure list.
4. Treat the difference as imported figures.
5. Clear the current selection.
6. Select the imported figures.
7. Add imported figures to the transfer figure set.
8. Move figures to the drop point if needed.
9. Fire the same undoable paste edit.
10. Return `true` on success.
11. Return `false` after an `IOException` so another input format can be tried.

The Mac OS X workaround also remains intact. The code still uses one loop ordering on Mac OS X and another loop ordering elsewhere. Only the repeated body of the successful import has been extracted.

## Refactoring Pattern Mapping

| Smell | Refactoring Pattern | Reason |
| --- | --- | --- |
| Duplicated code in paste import branches | Extract Method | The repeated sequence had one clear responsibility: import transferable data using a chosen input format. |
| Repeated undoable edit construction | Extract Method | The undo/redo registration is a named concept in the paste workflow. |
| Unused unimplemented private method | Remove Dead Code | Private dead code cannot help clients and distracts maintainers. |
| Typo-level comment problems | Improve Comments | Program comprehension depends on trustworthy source text. |

## Verification

The affected Maven modules were tested with:

```text
mvn -pl jhotdraw-core,jhotdraw-datatransfer -am test
```

Result:

```text
BUILD SUCCESS
```

Tests executed in the affected reactor build:

| Test Class | Tests | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| `org.jhotdraw.geom.BezierPathNGTest` | 4 | 0 | 0 | 0 |
| `org.jhotdraw.draw.figure.AbstractFigureNGTest` | 2 | 0 | 0 | 0 |

The automated test set does not fully simulate GUI copy/paste. That limitation should be handled in later TestLab1 and BDDLab documentation by adding or describing scenario-level verification.

## Portfolio Conclusion

The copy/paste refactoring improved maintainability by removing duplicate import behavior, centralizing paste undo behavior, removing dead code, and cleaning confusing comments. The change set stayed intentionally small and focused on the classes already identified during concept location and impact analysis.

The next exercise should continue with actualization: explain how the change is incorporated into the existing system, where responsibilities are local versus composite, and how change propagation is controlled.
