# 08 BDD

## Scenario: Copy and paste a rectangle

Given a drawing with one selected rectangle  
When the user copies and pastes it  
Then the drawing contains two rectangles  
And the pasted rectangle is selected

## Scenario: Empty selection

Given no figures are selected  
When the user invokes Copy  
Then no figure transferable is created  
And the drawing remains unchanged

## Scenario: Unsupported data

Given the clipboard contains unsupported data  
When the user invokes Paste  
Then the drawing does not import a figure

## Scenario: Undo paste

Given pasted figures were added to a drawing  
When Undo is invoked  
Then the pasted figures are removed  
When Redo is invoked  
Then the pasted figures are added again

## Scenario: Paste text

Given the clipboard contains plain text  
And the drawing registers `TextInputFormat`  
When the user invokes Paste  
Then a text-holder figure is added to the drawing

## Automation

The scenarios are automated in:

```text
jhotdraw-samples/jhotdraw-samples-misc/src/test/java/org/jhotdraw/samples/draw/CopyPasteBddTest.java
```

No BDD framework was added. JUnit 4 is already available in the sample module, so the BDD automation uses executable Given-When-Then test method names without adding a new dependency.

Verification:

```text
CopyPasteBddTest: Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```
