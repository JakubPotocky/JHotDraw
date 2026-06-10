# BDDLab - Copy/Paste User Stories To Scenarios

## Lab Goal

The BDD lab maps the Copy/Paste user stories to Given-When-Then scenarios and automates those scenarios. The automation is implemented with JUnit 4 test methods named in BDD style instead of adding a new Cucumber/JGiven dependency.

Automation file:

```text
jhotdraw-samples/jhotdraw-samples-misc/src/test/java/org/jhotdraw/samples/draw/CopyPasteBddTest.java
```

## User Story

As a user editing a drawing, I want to copy selected figures and paste them back into the drawing, so that I can duplicate existing work without recreating it manually.

## Automated Scenarios

### Scenario 1 - Copy and paste one selected rectangle

Given a drawing with one selected rectangle  
When the user copies and pastes it  
Then the drawing contains two rectangles  
And the pasted rectangle is selected

Automated by:

```text
givenDrawingWithOneSelectedRectangle_whenUserCopiesAndPastesIt_thenDrawingContainsTwoRectanglesAndPastedRectangleIsSelected
```

### Scenario 2 - Copy with no selection

Given a drawing view with no selected figures  
When the user invokes Copy  
Then no drawing figure transferable is produced  
And the drawing remains unchanged

Automated by:

```text
givenDrawingViewWithNoSelectedFigures_whenUserInvokesCopy_thenNoDrawingFigureTransferableIsProduced
```

### Scenario 3 - Paste unsupported data

Given the clipboard contains data with no supported data flavor  
When the user invokes Paste  
Then the drawing does not import the data  
And the drawing remains unchanged

Automated by:

```text
givenClipboardContainsUnsupportedData_whenUserInvokesPaste_thenDrawingRemainsUnchanged
```

### Scenario 4 - Undo pasted figures

Given a user pasted figures into a drawing  
When the user invokes Undo  
Then the pasted figures are removed  
When the user invokes Redo  
Then the pasted figures are added back

Automated by:

```text
givenUserPastedFiguresIntoDrawing_whenUserInvokesUndoAndRedo_thenPastedFiguresAreRemovedAndAddedBack
```

### Scenario 5 - Paste text

Given the clipboard contains plain text  
And the drawing registers `TextInputFormat`  
When the user invokes Paste  
Then a text-holder figure is added to the drawing

Automated by:

```text
givenClipboardContainsPlainTextAndDrawingRegistersTextInputFormat_whenUserInvokesPaste_thenTextHolderFigureIsAdded
```

## Why JUnit Is Enough Here

The lab goal is executable scenarios. The project already uses Maven and has JUnit available in the sample module. Adding a new BDD framework would increase dependency and configuration scope without improving the Copy/Paste maintenance behavior being checked. The test method names preserve the Given-When-Then structure while keeping the build simple.

## Verification

Command:

```text
mvn -pl jhotdraw-samples/jhotdraw-samples-misc test
```

Result:

```text
Running org.jhotdraw.samples.draw.CopyPasteBddTest
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```

## Conclusion

BDDLab is complete. The user story has five Given-When-Then scenarios, and all five are automated in `CopyPasteBddTest`.
