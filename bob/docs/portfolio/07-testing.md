# 07 Testing

## Added Tests

Test files:

```text
jhotdraw-samples/jhotdraw-samples-misc/src/test/java/org/jhotdraw/samples/draw/DOMStorableInputOutputFormatTest.java
jhotdraw-samples/jhotdraw-samples-misc/src/test/java/org/jhotdraw/samples/draw/DefaultDrawingViewTransferHandlerTest.java
jhotdraw-samples/jhotdraw-samples-misc/src/test/java/org/jhotdraw/samples/draw/CopyPasteBddTest.java
```

## Coverage

The tests cover:

- creating a transferable from a selected `RectangleFigure`;
- reading that transferable into another drawing;
- verifying the pasted figure is a new object with the same bounds;
- verifying `replace=true` clears existing figures;
- verifying unsupported string flavor is rejected by the DOM format.
- verifying selected figures create a transferable through `DefaultDrawingViewTransferHandler`;
- verifying Copy with no selection creates no figure transferable;
- verifying Paste adds imported figures and selects them;
- verifying unsupported paste data leaves the drawing unchanged;
- verifying Paste fires an undoable edit that supports undo/redo.

## Verification

```bash
mvn -pl jhotdraw-samples/jhotdraw-samples-misc test
```

Result:

```text
Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
```

Full project:

```bash
mvn test --file pom.xml
```

Result: **BUILD SUCCESS**.
