# Master Prompt For Solving The Remaining Software Maintenance Exercises

Use this prompt when asking Codex or another assistant to continue Bob's Software Maintenance portfolio work. It is written to keep the work grounded in the course slides, the JHotDraw codebase, and Bob's selected copy/paste feature.

```text
You are working in the JHotDraw codebase for the Software Maintenance course.

Goal:
Finish Bob's portfolio exercises one lab at a time for the Basic Editing feature focused on copy and paste.

Important source facts:
- Parent issue: Basic editing #12.
- Sub-issue: copy #13.
  User story: As a user, I want to copy a selected object so that I can quickly duplicate existing work without recreating it manually.
- Sub-issue: paste #14.
  User story: As a user, I want to paste a copied object so that I can quickly place a duplicate on the canvas.
- All documentation must be written as markdown files inside BobsWork.
- Use AlansWork/alansPortfolioWork.md as the style reference, but write Bob's work for copy/paste, not Alan's automatic-selection feature.
- Use the actual codebase as evidence. Do not invent classes.

Required workflow:
1. Start by mapping the lab exercises.
2. Then solve one exercise at a time.
3. For each exercise, inspect the relevant code before documenting.
4. Keep a clear distinction between observed code facts and inferred portfolio explanation.
5. Update BobsWork/Portfolio_Copy_Paste.md as the combined portfolio document.
6. Create a separate detailed markdown file for each lab exercise.
7. When code smells/refactoring are reached, identify smells first, then propose or implement a small behavior-preserving refactoring, then document the smell, strategy, and reason.
8. Do not edit unrelated code.
9. If tests are needed, prefer focused tests around copy/paste behavior or document practical manual verification if automated GUI testing is too expensive.
10. After each exercise, state what file was created or updated and what the next exercise is.

Feature scope:
The feature is Basic Editing -> Copy and Paste in JHotDraw.
The main user-visible actions are:
- Copy selected object to clipboard.
- Paste copied object onto the canvas as a duplicate.

Likely code areas:
- jhotdraw-actions/src/main/java/org/jhotdraw/action/edit/CopyAction.java
- jhotdraw-actions/src/main/java/org/jhotdraw/action/edit/PasteAction.java
- jhotdraw-actions/src/main/java/org/jhotdraw/action/edit/CutAction.java
- jhotdraw-actions/src/main/java/org/jhotdraw/action/edit/AbstractSelectionAction.java
- jhotdraw-api/src/main/java/org/jhotdraw/api/gui/EditableComponent.java
- jhotdraw-core/src/main/java/org/jhotdraw/draw/DefaultDrawingView.java
- jhotdraw-core/src/main/java/org/jhotdraw/draw/DefaultDrawingViewTransferHandler.java
- jhotdraw-core/src/main/java/org/jhotdraw/draw/Drawing.java
- jhotdraw-core/src/main/java/org/jhotdraw/draw/AbstractDrawing.java
- jhotdraw-core/src/main/java/org/jhotdraw/draw/io/InputFormat.java
- jhotdraw-core/src/main/java/org/jhotdraw/draw/io/OutputFormat.java
- jhotdraw-core/src/main/java/org/jhotdraw/draw/io/DOMStorableInputOutputFormat.java
- jhotdraw-core/src/main/java/org/jhotdraw/draw/io/SerializationInputOutputFormat.java
- jhotdraw-core/src/main/java/org/jhotdraw/draw/io/ImageInputFormat.java
- jhotdraw-core/src/main/java/org/jhotdraw/draw/io/ImageOutputFormat.java
- jhotdraw-core/src/main/java/org/jhotdraw/draw/io/TextInputFormat.java
- jhotdraw-datatransfer/src/main/java/org/jhotdraw/datatransfer/ClipboardUtil.java
- jhotdraw-datatransfer/src/main/java/org/jhotdraw/datatransfer/CompositeTransferable.java
- jhotdraw-datatransfer/src/main/java/org/jhotdraw/datatransfer/AWTClipboard.java
- jhotdraw-datatransfer/src/main/java/org/jhotdraw/datatransfer/OSXClipboard.java
- jhotdraw-app/src/main/java/org/jhotdraw/app/DefaultApplicationModel.java
- jhotdraw-app/src/main/java/org/jhotdraw/app/DefaultMenuBuilder.java
- jhotdraw-gui/src/main/java/org/jhotdraw/gui/action/ButtonFactory.java
- jhotdraw-samples/jhotdraw-samples-misc/src/main/java/org/jhotdraw/samples/draw/DrawingPanel.java
- jhotdraw-samples/jhotdraw-samples-misc/src/main/java/org/jhotdraw/samples/draw/DrawView.java

Course lab sequence to follow:
1. IntroLab: confirm Maven/GitHub/JHotDraw startup context.
2. ChangeReqLab: document the user story/change request from the screenshots.
3. CLLab: concept location by tracing copy and paste runtime/code paths.
4. AnalysisLab1: impact analysis table with packages, class counts, comments, and changed/unchanged decisions.
5. CILab: explain GitHub flow, Maven, and GitHub Actions CI for this project.
6. RefactoringLab1: find code smells around copy/paste and document/refactor them.
7. ActualizationLab: explain actualization, SOLID, Clean Code, and Clean Architecture in context.
8. TestLab1: document or implement tests for copy/paste behavior.
9. BDDLab: map copy/paste user stories to Given-When-Then scenarios and explain JGiven/AssertJ strategy.
10. Conclusion: explain commit, baseline, release readiness, and technical-debt reflection.

Expected documentation style:
- Clear title.
- Lab goal.
- Course requirement.
- Evidence from code.
- Tables where the lab asks for tables.
- Short conclusion and next step.
- Keep all files inside BobsWork.
```

