# Diagrams

---

## Figure 1 — fig01-concept-location-activity-diagram.png

**Figure title (text at the top):**

```
Figure 1 — Concept Location: search process (activity diagram)
```

**Boxes** (vertical flow, top to bottom):

1. Start — (UML start node).
2. Box:

```
Formulate search query from the change request
(e.g. "paste", "clipboard", "Transferable")
```

3. Box:

```
Find a starting set of modules
(grep search over the codebase)
```

4. Box:

```
Select one candidate module
and read / debug it
```

5. Box (decision):

```
Is the concept
implemented here?
```

6. Box (placed to the right of box 5):

```
Mark as located;
record in table
```

7. End — a small circle with a ring around it (UML end node), below box 6.
8. Box (decision, below box 5):

```
Does it delegate the concept
to supplier modules?
```

9. Box (placed to the left of box 8):

```
Follow dependencies:
visit supplier modules
```

10. Box (below box 8):

```
Backtrack: return to previous module
or pick another from the starting set
```

11. Note box at the very bottom (no arrows to it):

```
This run:  grep "paste"  →  PasteAction (thin, delegates)  →  dependency search  →  TransferHandler
→  DefaultDrawingViewTransferHandler.importData  ⇒  concept located; confirmed with debugger breakpoints
```

**Arrows:**

- Start → Box 2
- Box 2 → Box 3
- Box 3 → Box 4
- Box 4 → Box 5
- Box 5 → Box 6 — label: `yes`
- Box 6 → End
- Box 5 → Box 8 — label: `no`
- Box 8 → Box 9 — label: `yes`
- Box 9 → Box 4 (loop back up)
- Box 8 → Box 10 — label: `no`
- Box 10 → Box 4 (loop back up)

---

## Figure 2 — fig02-uml-class-diagram-workarea.png

**Figure title:**

```
Figure 2 — UML class diagram of the Copy/Paste work area (after commit b4eea564)
```

**Boxes** (UML class boxes — first line is the class name, make it bold):

1.

```
CopyAction
+actionPerformed(e)
```

2.

```
PasteAction
+actionPerformed(e)
```

3.

```
«abstract» TransferHandler
+exportToClipboard(...)
+importData(...)
```

4. (the central class):

```
DefaultDrawingViewTransferHandler
+importData(comp, t): boolean
#createTransferable(...): Transferable
−importTransferData(...): boolean   (new, line 174)
−firePasteUndoableEdit(...)   (new, line 195)
```

5.

```
DefaultDrawingView
+addToSelection(figs)
+clearSelection()
installs handler (line 305)
```

6.

```
«interface» InputFormat
+isDataFlavorSupported(f)
+read(t, drawing, replace)
```

7.

```
«interface» OutputFormat
+createTransferable(...)
```

8.

```
Drawing
+getChildren()
+getInput/OutputFormats()
+fireUndoableEditHappened(e)
```

9.

```
DOMStorableInputOutputFormat
```

**Arrows:**

- Box 1 (CopyAction) → Box 3 (TransferHandler) — label: `delegates export`
- Box 2 (PasteAction) → Box 3 (TransferHandler) — label: `delegates importData`
- Box 4 → Box 3 — label: `extends` (UML inheritance: solid line, hollow triangle head)
- Box 4 → Box 6 (InputFormat) — label: `reads via`
- Box 4 → Box 7 (OutputFormat) — label: `writes via`
- Box 4 → Box 8 (Drawing) — label: `modifies; fires undo edits`
- Box 4 → Box 5 (DefaultDrawingView) — label: `updates selection`
- Box 9 → Box 6 — dashed line, label: `implements` (UML realization: hollow triangle head)
- Box 9 → Box 7 — dashed line, hollow triangle head (no label)

---

## Figure 3 — fig03-dynamic-analysis-breakpoints.png

**Figure title:**

```
Figure 3 — Dynamic impact analysis: debugger breakpoints hit in order (Draw sample)
```

Two vertical columns of boxes, side by side.

**Left column boxes** (top to bottom):

1. Header box:

```
Edit → Copy
```

2.

```
1   CopyAction.actionPerformed
```

3.

```
2   DefaultDrawingViewTransferHandler
     .createTransferable
```

4.

```
3   DOMStorableInputOutputFormat
     .createTransferable
```

5. Result box:

```
Selection exported to the clipboard
as one CompositeTransferable
```

**Left column arrows:** 1 → 2 → 3 → 4 → 5 (straight down).

**Right column boxes** (top to bottom):

6. Header box:

```
Edit → Paste
```

7.

```
1   PasteAction.actionPerformed
```

8.

```
2   DefaultDrawingViewTransferHandler
     .importData
```

9.

```
3   DOMStorableInputOutputFormat.read
```

10.

```
4   DefaultDrawingView.addToSelection
```

11. Result box:

```
Figures added and selected;
undoable paste edit registered
```

**Right column arrows:** 6 → 7 → 8 → 9 → 10 → 11 (straight down).

**Note box at the bottom** (no arrows, spans both columns):

```
Runtime change check: modify the offset tx.translate(5, 5) in DefaultDrawingView.duplicate() (line 1345),
recompile, run — the visibly different duplicate offset proves the modified code path is the one executed.
```

---

## Figure 4 — fig04-impact-visited-vs-changed.png

**Figure title:**

```
Figure 4 — Impact analysis: visited (9 packages / 35 classes) vs. changed (commit b4eea564)
```

**Boxes:**

1–9. Nine small boxes in a row (or two rows) at the top, one package each:

```
action.edit (5)
```

```
api.gui (1)
```

```
app (2)
```

```
draw.figure (3)
```

```
draw.io (7)
```

```
gui.action (1)
```

```
draw (6)
```

```
samples.draw (3)
```

```
datatransfer (7)
```

10. Box (middle of the figure, first line bold):

```
CHANGED
DefaultDrawingViewTransferHandler
Extract Method ×2  ·  dead getDrawing() removed
comments rewritten  ·  625 → 576 lines
```

11. Box (next to box 10, first line bold):

```
CHANGED (docs only)
CompositeTransferable
JavaDoc typos fixed  ·  fields made final
```

12. Wide box below:

```
UNCHANGED:  actions · view · formats · UI wiring — the duplication was local;
propagation deliberately stopped (action-layer duplication → backlog)
```

13. Wide box at the bottom:

```
ADDED (test scope, in samples.draw):  DefaultDrawingViewTransferHandlerTest · CopyPasteBddTest ·
DOMStorableInputOutputFormatTest   (13 tests in total)
```

**Arrows** (only two):

- Package box `draw (6)` → Box 10 (CHANGED) — label: `narrowed to`
- Package box `datatransfer (7)` → Box 11 (CHANGED, docs only) — label: `narrowed to`

Boxes 12 and 13 have no arrows.

---

## Figure 5 — fig05-before-duplicated-block.png

This is a code screenshot, not a box-and-arrow diagram: one title box on top of
one big monospace text box. No arrows.

**Title box (two lines):**

```
Figure 5 — BEFORE: the duplicated import block (appeared 3× inside importData)
DefaultDrawingViewTransferHandler.java @ parent of b4eea564 — one of three copies (macOS branch, lines 101–137; indentation reduced)
```

**Code box** (monospace font; line numbers start at **101**; note the original
code's typo "transferalbe" on the last comment line — keep it, it is verbatim):

```java
LinkedList<Figure> existingFigures = new LinkedList<>(drawing.getChildren());
try {
    format.read(t, drawing, false);
    final LinkedList<Figure> importedFigures = new LinkedList<>(drawing.
            getChildren());
    importedFigures.removeAll(existingFigures);
    view.clearSelection();
    view.addToSelection(importedFigures);
    transferFigures.addAll(importedFigures);
    moveToDropPoint(comp, transferFigures, dropPoint);
    drawing.fireUndoableEditHappened(new AbstractUndoableEdit() {
        private static final long serialVersionUID = 1L;

        @Override
        public String getPresentationName() {
            ResourceBundleUtil labels = ResourceBundleUtil.getBundle(
                    "org.jhotdraw.draw.Labels");
            return labels.getString("edit.paste.text");
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            drawing.removeAll(importedFigures);
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            drawing.addAll(importedFigures);
        }
    });
    retValue = true;
    break SearchLoop;
} catch (IOException e) {
    e.printStackTrace();
    // failed to read transferalbe, try with next InputFormat
...
```

---

## Figure 6 — fig06-after-call-sites.png

Same shape as Figure 5: a title box on top of one monospace code box. No arrows.

**Title box (two lines):**

```
Figure 6 — AFTER: the three call sites (lines 101, 113, 153)
DefaultDrawingViewTransferHandler.java @ b4eea564 — excerpt, reformatted for readability (logic verbatim)
```

**Code box** (monospace; line numbers start at 1):

```java
// macOS branch (line 101) and default branch (line 113):
// each duplicated ~35-line block is now ONE guarded call
if (format.isDataFlavorSupported(flavor)) {
    if (importTransferData(comp, t, transferFigures, dropPoint,
                           view, drawing, format)) {
        retValue = true;
        break SearchLoop;
    }
}

// file-drop path (line 153): reuses the same shared undo construction
firePasteUndoableEdit(drawing, importedFigures);
```

---

## Figure 7 — fig07-after-extracted-methods.png

Same shape again: title box on top of one monospace code box. No arrows.

**Title box (two lines):**

```
Figure 7 — AFTER: the two extracted methods (Extract Method ×2)
importTransferData(...) line 174 · firePasteUndoableEdit(...) line 195 — verbatim from b4eea564
```

**Code box** (monospace; line numbers start at **174**):

```java
private boolean importTransferData(final JComponent comp, Transferable t, final HashSet<Figure> transferFigures,
                                   final Point dropPoint, final DrawingView view, final Drawing drawing,
                                   InputFormat format) throws UnsupportedFlavorException {
    LinkedList<Figure> existingFigures = new LinkedList<>(drawing.getChildren());
    try {
        format.read(t, drawing, false);
        final LinkedList<Figure> importedFigures = new LinkedList<>(drawing.getChildren());
        importedFigures.removeAll(existingFigures);
        view.clearSelection();
        view.addToSelection(importedFigures);
        transferFigures.addAll(importedFigures);
        moveToDropPoint(comp, transferFigures, dropPoint);
        firePasteUndoableEdit(drawing, importedFigures);
        return true;
    } catch (IOException e) {
        e.printStackTrace();
        // Failed to read transferable; try with the next InputFormat.
        return false;
    }
}

private void firePasteUndoableEdit(final Drawing drawing, final LinkedList<Figure> importedFigures) {
    drawing.fireUndoableEditHappened(new AbstractUndoableEdit() {
        private static final long serialVersionUID = 1L;

        @Override
        public String getPresentationName() {
            ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
            return labels.getString("edit.paste.text");
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            drawing.removeAll(importedFigures);
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            drawing.addAll(importedFigures);
        }
    });
}
```

---

## Figure 8 — fig08-structure-before-after.png

**Figure title:**

```
Figure 8 — Postfactoring: structure of the import path before vs. after commit b4eea564
```

Two side-by-side panels (draw each panel as one large outline rectangle with a
heading, and put the listed boxes inside it).

### Left panel

Panel heading:

```
BEFORE — 625 lines
```

Boxes inside, top to bottom:

1. A large container box with this heading text at its top:

```
importData(...)  ≈ 185 lines
```

2. Inside the container, three stacked boxes:

```
copy 1 — macOS branch:
import sequence + anonymous undo edit
```

```
copy 2 — default branch:
import sequence + anonymous undo edit
```

```
copy 3 — file-drop path:
anonymous undo edit (again)
```

3. Below the container, a separate box:

```
private getDrawing() — dead code
(throw new UnsupportedOperationException)
```

4. Plain text line at the bottom of the panel (no box):

```
any fix to the paste logic had to be applied 3×
```

No arrows in the left panel.

### Right panel

Panel heading:

```
AFTER — 576 lines (−49)
```

Boxes inside, top to bottom:

5. A container box with this heading text at its top:

```
importData(...) — format negotiation only
```

6. Inside the container, three small boxes:

```
call — line 101
```

```
call — line 113
```

```
undo call — line 153
```

7. Below the container:

```
importTransferData(...) — line 174
ONE shared import sequence
```

8. Below that:

```
firePasteUndoableEdit(...) — line 195
ONE shared undo/redo edit
```

9. Plain text line at the bottom of the panel (no box):

```
getDrawing() deleted ✓      a future change is a one-place change
```

**Arrows (right panel only):**

- Box `call — line 101` → Box 7 (importTransferData)
- Box `call — line 113` → Box 7 (importTransferData)
- Box `undo call — line 153` → Box 8 (firePasteUndoableEdit) — route it around
  the side so it does NOT pass through box 7. **Important:** line 153 calls
  firePasteUndoableEdit directly, not importTransferData.
- Box 7 → Box 8 — label: `calls (line 186)`

---

## Figure 9 — fig09-test-run-results.png

A console screenshot: one title box on top of one monospace text box. No arrows.

**Title box (two lines):**

```
Figure 9 — Verification: executed test run (5 + 5 + 3 = 13 tests, all green)
branch copy-paste-basic-editing-labs @ b4eea564 — run 11 June 2026, headless
```

**Console box** (monospace, exact text):

```
$ mvn -pl jhotdraw-samples/jhotdraw-samples-misc -am test
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.jhotdraw.samples.draw.DefaultDrawingViewTransferHandlerTest
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.282 sec
Running org.jhotdraw.samples.draw.CopyPasteBddTest
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.186 sec
Running org.jhotdraw.samples.draw.DOMStorableInputOutputFormatTest
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 sec

Results :

Tests run: 13, Failures: 0, Errors: 0, Skipped: 0

(exit code 0 — BUILD SUCCESS)
```

---

## Figure 10 — fig10-ci-pipeline.png

**Figure title:**

```
Figure 10 — GitHub flow + CI pipeline (.github/workflows/maven.yml)
```

Six boxes in a single horizontal row, left to right:

1.

```
Feature branch
copy-paste-
basic-editing-labs
```

2.

```
Pull request
to develop
```

3.

```
GitHub Actions
checkout · JDK 23
Temurin · Maven cache
```

4.

```
mvn -B clean install
all Maven modules
```

5.

```
mvn test
incl. the 13
copy/paste tests
```

6.

```
Green build =
new verified
baseline
```

**Arrows:** 1 → 2 → 3 → 4 → 5 → 6 (a simple left-to-right chain).

**Note box underneath** (no arrows):

```
Triggers: push & pull_request on develop — the suite is headless (no OS clipboard), so it runs unchanged on the build server
```
