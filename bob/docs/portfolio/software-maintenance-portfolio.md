# Software Maintenance Portfolio - JHotDraw Copy/Paste

**Submission by:** Bob  
**Case study:** JHotDraw  
**Selected feature:** Basic Editing - Copy/Paste

This is the combined portfolio report. For the separated version, start with [`README.md`](README.md).

## 1. Case Study and Selected Feature

The selected feature is the existing Copy/Paste behavior in JHotDraw. It is implemented through Swing actions, a drawing-view transfer handler, clipboard utilities, input/output format strategies, and the drawing/figure model.

## 2. Change Request and User Story

> As a user editing a drawing, I want to copy selected figures and paste them back into the drawing, so that I can duplicate existing work without recreating it manually.

Acceptance criteria include copying selected figures, pasting supported transferable data, selecting pasted figures, supporting undo/redo, rejecting unsupported data flavors, and preserving the existing architecture.

## 3. Concept Location

Important classes:

- `CopyAction`
- `PasteAction`
- `ClipboardUtil`
- `DefaultDrawingView`
- `DefaultDrawingViewTransferHandler`
- `CompositeTransferable`
- `Drawing`
- `Figure`
- `InputFormat`
- `OutputFormat`
- `DOMStorableInputOutputFormat`
- `ImageOutputFormat`
- `TextInputFormat`

Copy delegates from `CopyAction` to Swing export. Paste delegates from `PasteAction` to Swing import. The transfer handler coordinates formats, selection, and undo edits.

## 4. Impact Analysis and CI

Affected packages include:

- `org.jhotdraw.action.edit`
- `org.jhotdraw.datatransfer`
- `org.jhotdraw.draw`
- `org.jhotdraw.draw.io`
- `org.jhotdraw.draw.figure`
- `org.jhotdraw.samples.draw`
- `org.jhotdraw.samples.svg`

CI is configured in `.github/workflows/maven.yml` with:

```bash
mvn -B clean install --file pom.xml
mvn test --file pom.xml
```

## 5. Refactoring

The transfer handler paste path was documented as the main refactoring area, with helper methods for import and undo edit creation. `CompositeTransferable` was also tightened by marking stable internal collection fields as `final`.

## 6. Clean Architecture and SOLID

The feature has a clear separation:

- actions as controllers;
- transfer handler as coordinator;
- formats as strategies;
- drawing/figure as model;
- clipboard/transferable classes as infrastructure.

This supports SRP, OCP, ISP, and DIP particularly well.

## 7. Testing

Added:

```text
jhotdraw-samples/jhotdraw-samples-misc/src/test/java/org/jhotdraw/samples/draw/DOMStorableInputOutputFormatTest.java
jhotdraw-samples/jhotdraw-samples-misc/src/test/java/org/jhotdraw/samples/draw/DefaultDrawingViewTransferHandlerTest.java
jhotdraw-samples/jhotdraw-samples-misc/src/test/java/org/jhotdraw/samples/draw/CopyPasteBddTest.java
```

The tests cover native DOM transferable round-trip behavior, replace behavior, selected copy transfer creation, paste import, selection after paste, unsupported data flavor rejection, paste undo/redo, and executable BDD-style user scenarios.

## 8. BDD

BDD scenarios were written for:

- copy/paste selected rectangle;
- copy with empty selection;
- paste unsupported data;
- undo/redo pasted figures;
- paste text as text-holder figures.

The scenarios are automated in `CopyPasteBddTest` with JUnit 4 Given-When-Then test method names. No separate BDD framework was added because JUnit was already available in the module and kept the maintenance change smaller.

## 9. Verification

Commands that succeeded:

```bash
mvn clean install -DskipTests
mvn -pl jhotdraw-samples/jhotdraw-samples-misc test
mvn test --file pom.xml
```

The sample module currently runs 13 Copy/Paste-related tests with 0 failures.

The SVG sample GUI command was attempted with a timeout, but no manual GUI interaction is claimed.
