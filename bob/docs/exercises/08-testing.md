# 08 Testing

## Test Target

The added automated tests cover both the native figure serialization path and the drawing-view transfer handler used by Copy/Paste.

Test files:

```text
jhotdraw-samples/jhotdraw-samples-misc/src/test/java/org/jhotdraw/samples/draw/DOMStorableInputOutputFormatTest.java
jhotdraw-samples/jhotdraw-samples-misc/src/test/java/org/jhotdraw/samples/draw/DefaultDrawingViewTransferHandlerTest.java
jhotdraw-samples/jhotdraw-samples-misc/src/test/java/org/jhotdraw/samples/draw/CopyPasteBddTest.java
```

## Tests

| Test | Purpose |
|---|---|
| `createsTransferableThatRoundTripsSelectedFigure` | Verifies a copied `RectangleFigure` can be serialized to a transferable and read into another drawing. |
| `readWithReplaceClearsExistingFigures` | Verifies `replace=true` removes existing drawing figures before import. |
| `rejectsUnsupportedDataFlavor` | Verifies the DOM format does not claim support for string flavor. |
| `copySelectedFigureCreatesTransferable` | Verifies selected figures produce a drawing transferable through `DefaultDrawingViewTransferHandler`. |
| `copyWithoutSelectionCreatesNoTransferable` | Verifies Copy with an empty selection produces no drawing figure transferable. |
| `pasteSupportedTransferableAddsAndSelectsImportedFigure` | Verifies a supported transferable is imported, cloned, added to the drawing, and selected. |
| `pasteUnsupportedTransferableLeavesDrawingUnchanged` | Verifies unsupported clipboard data is rejected without changing the drawing. |
| `pasteFiresUndoableEditThatCanUndoAndRedoImportedFigure` | Verifies Paste registers an undoable edit for imported figures. |

## Commands

```bash
mvn -pl jhotdraw-samples/jhotdraw-samples-misc test
```

Result: the sample module test command succeeded. The Copy/Paste-related JUnit classes ran 13 tests with 0 failures.

The root CI-style command was also run:

```bash
mvn test --file pom.xml
```

Result: **BUILD SUCCESS**.

## Detailed Combined Note

- [`../../Labs.md`](../../Labs.md)
