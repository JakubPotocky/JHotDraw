# 09 BDD: User Stories to Scenarios

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

### Scenario 3: Paste unsupported data

Given the clipboard contains data with no supported data flavor  
When the user invokes Paste  
Then the drawing does not import the data  
And the drawing remains unchanged

### Scenario 4: Undo pasted figures

Given a user pasted figures into a drawing  
When the user invokes Undo  
Then the pasted figures are removed  
When the user invokes Redo  
Then the pasted figures are added back

### Scenario 5: Paste text

Given the clipboard contains plain text  
And the drawing registers `TextInputFormat`  
When the user invokes Paste  
Then text-holder figures are added to the drawing

## Automation Status

The scenarios are automated with JUnit 4 in:

```text
jhotdraw-samples/jhotdraw-samples-misc/src/test/java/org/jhotdraw/samples/draw/CopyPasteBddTest.java
```

No separate Cucumber/JBehave/JGiven dependency was added. The project already has JUnit in the sample module, so the BDD automation uses executable test methods with Given-When-Then names.

| Scenario | Automated by |
|---|---|
| Copy and paste one selected rectangle | `givenDrawingWithOneSelectedRectangle_whenUserCopiesAndPastesIt_thenDrawingContainsTwoRectanglesAndPastedRectangleIsSelected` |
| Copy with no selection | `givenDrawingViewWithNoSelectedFigures_whenUserInvokesCopy_thenNoDrawingFigureTransferableIsProduced` |
| Paste unsupported data | `givenClipboardContainsUnsupportedData_whenUserInvokesPaste_thenDrawingRemainsUnchanged` |
| Undo pasted figures | `givenUserPastedFiguresIntoDrawing_whenUserInvokesUndoAndRedo_thenPastedFiguresAreRemovedAndAddedBack` |
| Paste text | `givenClipboardContainsPlainTextAndDrawingRegistersTextInputFormat_whenUserInvokesPaste_thenTextHolderFigureIsAdded` |

Verification:

```bash
mvn -pl jhotdraw-samples/jhotdraw-samples-misc test
```

Result: `CopyPasteBddTest` ran 5 tests with 0 failures.

## Detailed Combined Note

- [`../../Labs.md`](../../Labs.md)
