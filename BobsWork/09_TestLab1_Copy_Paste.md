# TestLab1 - Automated Tests For Copy/Paste

## Lab Goal

TestLab1 asks for automated tests around the important behavior of the selected maintenance feature. For Bob's feature, the relevant behavior is Basic Editing Copy/Paste in JHotDraw.

## Test Scope

The tests cover two levels:

| Test level | Test file | What it verifies |
| --- | --- | --- |
| Native transfer format | `jhotdraw-samples/jhotdraw-samples-misc/src/test/java/org/jhotdraw/samples/draw/DOMStorableInputOutputFormatTest.java` | Selected figures can be serialized into the native JHotDraw transferable format and read back into another drawing. |
| Drawing transfer handler | `jhotdraw-samples/jhotdraw-samples-misc/src/test/java/org/jhotdraw/samples/draw/DefaultDrawingViewTransferHandlerTest.java` | The real drawing-view transfer handler creates transferables, imports supported data, updates selection, rejects unsupported data, and fires paste undo/redo edits. |

The tests are in the samples module because `DrawFigureFactory` and the sample Draw figures are the concrete setup used by the native drawing copy/paste format.

## Test Cases

| Test | Purpose |
| --- | --- |
| `createsTransferableThatRoundTripsSelectedFigure` | Verifies a copied `RectangleFigure` can round-trip through `DOMStorableInputOutputFormat`. |
| `readWithReplaceClearsExistingFigures` | Verifies `replace=true` clears existing drawing figures before import. |
| `rejectsUnsupportedDataFlavor` | Verifies the native DOM format does not claim support for plain string data. |
| `copySelectedFigureCreatesTransferable` | Verifies selected figures produce a drawing transferable through `DefaultDrawingViewTransferHandler`. |
| `copyWithoutSelectionCreatesNoTransferable` | Verifies Copy with no selected figures produces no drawing figure transferable. |
| `pasteSupportedTransferableAddsAndSelectsImportedFigure` | Verifies paste adds a cloned imported figure and selects it. |
| `pasteUnsupportedTransferableLeavesDrawingUnchanged` | Verifies unsupported clipboard data is rejected without changing the drawing. |
| `pasteFiresUndoableEditThatCanUndoAndRedoImportedFigure` | Verifies paste creates an undoable edit that removes and re-adds imported figures. |

## Verification

Command:

```text
mvn -pl jhotdraw-samples/jhotdraw-samples-misc test
```

Result:

```text
Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Remaining Test Limits

These tests exercise the drawing transfer pipeline directly, but they are not manual GUI tests. They do not prove that a real user can interact with every menu item, toolbar button, operating-system clipboard, or platform-specific Mac OS X data flavor ordering path. Those remain higher-level GUI/manual risks.

## Conclusion

TestLab1 is complete for the Copy/Paste maintenance scope. The most important behavior is now covered by automated JUnit tests: copy selection handling, paste import, selection after paste, unsupported data rejection, native transferable round-trip, and paste undo/redo.
