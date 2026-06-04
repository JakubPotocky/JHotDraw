# Portfolio Work

This document collects the portfolio deliverables for the Software Maintenance course.
Each chapter corresponds to one lab.

---

# Chapter: ChangeReqLab â€” Change Request

> **System under maintenance:** JHotDraw 7.6 (Java structured-graphics editor framework).
> **Selected feature:** Automatic Selection â€” covering three sub-features in the Edit menu / toolbar:
> - **Select All** â€” select every figure on the canvas.
> - **Deselect All (Clear Selection)** â€” empty the current selection.
> - **Select Same** â€” extend the current selection with every figure whose class matches a class already in the selection.

## User stories

The feature is described from the point of view of the end user of the Draw sample application, using the standard *"As a â€¦ I want â€¦ so that â€¦"* template. Each story also lists its acceptance criteria.

### US-1 â€” Select All

> **As** a user editing a drawing,
> **I want** to select every figure on the canvas with a single command,
> **so that** I can apply an operation (move, delete, change attributes, copy) to the whole drawing without clicking each figure individually.

**Trigger:** menu *Edit â†’ Select All*, or the keyboard shortcut `Ctrl+A`.

**Acceptance criteria:**
1. After the command, every selectable figure on the active canvas is part of the current selection.
2. Figures whose `isSelectable()` returns `false` (e.g. locked figures) are not added.
3. The selection handles are visible on every newly selected figure.
4. Any UI element that listens to selection changes (attribute panels, toolbar buttons) is refreshed.

### US-2 â€” Deselect All (Clear Selection)

> **As** a user editing a drawing,
> **I want** to clear the current selection in one step,
> **so that** I can start a fresh selection without having to click on empty canvas or unselect each figure individually.

**Trigger:** menu *Edit â†’ Clear Selection*.

**Acceptance criteria:**
1. After the command, the current selection is empty.
2. All selection handles disappear from the canvas.
3. Actions whose enablement depends on a non-empty selection (e.g. *Select Same*) become disabled.
4. The command is a no-op (and visibly disabled) when the selection is already empty.

### US-3 â€” Select Same

> **As** a user editing a drawing that contains many shapes of different kinds,
> **I want** to extend the current selection to every figure of the same kind as the figures I already have selected,
> **so that** I can apply the same change (e.g. recolour all rectangles) to a whole category of shapes without picking them one by one.

**Trigger:** *Select Same* button on the selection toolbar.

**Acceptance criteria:**
1. The button is disabled when the current selection is empty.
2. After the command, the selection contains every figure on the canvas whose runtime class matches at least one class already in the previous selection.
3. Figures of other classes remain unselected.
4. The command works additively â€” figures already selected stay selected.

---

## Change request statement (chosen for the impact-analysis lab)

*After Select All / Deselect All / Select Same, the Draw sample window must display a status-bar message of the form `"N figure(s) selected"` reflecting the new selection size. The status bar must also stay in sync with any later selection change (mouse click, drag-rectangle, keyboard shortcut, etc.), not only with the three actions above.*

## Why this change request

- It exercises **all three** sub-features of the Automatic Selection feature, so concept location and impact analysis cover one coherent slice of the system.
- It is realistic â€” a status bar with the current selection count is a feature found in most graphical editors.
- It is non-trivial enough to propagate beyond the three controller classes (it reaches the View layer of the sample), but small enough to be feasible inside one lab.

---

# Chapter: CLLab â€” Concept Location

> **Technique used:** IDE Debugger (dynamic program analysis) on the Draw sample (`org.jhotdraw.samples.draw.Main`).
> Breakpoints were placed at suspected controller entry points; execution was followed via Step Over / Step Into; the **Call Stack** and **Variables** panels in VS Code were used to record every class that participates at runtime.
> Full step-by-step procedure is in [alans.md](alans.md) (Parts 1â€“4).

## Initial set of classes â€” result of concept location

| Domain Class | Responsibility |
|---|---|
| `SelectAllAction` | Controller entry point for the "Select All" menu item; resolves the focused `EditableComponent` and invokes its `selectAll()` method. |
| `ClearSelectionAction` | Controller entry point for the "Clear Selection" menu item; resolves the focused `EditableComponent` and invokes its `clearSelection()` method. |
| `SelectSameAction` | Controller entry point for the "Select Same" toolbar action; collects the classes of currently selected figures and adds every figure of those classes to the selection. |
| `AbstractSelectionAction` | Abstract base class for `SelectAllAction` and `ClearSelectionAction`; manages action enablement based on the target component's selection state. |
| `AbstractSelectedAction` | Abstract base class for `SelectSameAction`; listens to `DrawingEditor` for the active view and enables the action only when at least one figure is selected. |
| `EditableComponent` | Contract interface declaring `selectAll()` and `clearSelection()`; implemented by `DefaultDrawingView` to make the drawing canvas behave as an editable component. |
| `DrawingView` | Contract interface declaring the full selection API: `addToSelection()`, `removeFromSelection()`, `selectAll()`, `clearSelection()`, `getSelectedFigures()`. |
| `DefaultDrawingView` | Core domain class that owns the `selectedFigures` set and implements `selectAll()`, `clearSelection()`, and `addToSelection()`; fires `FigureSelectionEvent` after every change. |
| `Drawing` | Domain model that holds all figures; provides `getChildren()` which is iterated by `selectAll()` and `selectSame()`. |
| `Figure` | Domain entity representing each shape; queried via `isSelectable()` (Select All) and `getClass()` (Select Same) to decide whether it joins the selection. |
| `DrawingEditor` | Mediator that exposes the active `DrawingView` to figure-aware actions such as `SelectSameAction`. |
| `FigureSelectionEvent` | Event object carrying the old and new selection sets; created by `DefaultDrawingView.fireSelectionChanged()`. |
| `FigureSelectionListener` | Observer interface; every registered listener receives `selectionChanged(FigureSelectionEvent)` whenever the selection changes. |
| `AbstractAttributeEditorHandler` | Observer that reacts to selection changes by refreshing attribute editor panels (color, stroke, font, etc.). |
| `SelectionComponentDisplayer` | Observer that shows or hides toolbar UI components depending on whether figures are selected. |
| `SelectionComponentRepainter` | Observer that repaints toolbar UI components to reflect the attributes of the new selection. |

**Total: 16 domain classes** identified by dynamic analysis.

---

# Chapter: AnalysisLab1 â€” Packages Visited After Concept Location

> **Feature analysed:** Automatic Selection (Select All / Deselect All / Select Same) in JHotDraw 7.6.
> **Change request used for the impact analysis:** *After Select All / Deselect All / Select Same, show a "N figure(s) selected" message in a status bar at the bottom of the Draw window, and keep it in sync with every later selection change.*

---

## Table 1 â€” Packages, classes visited, and what each package contributes

| Package name | # of classes visited | Comments |
|---|---:|---|
| `org.jhotdraw.action.edit` (module `jhotdraw-actions`) | 3 | App-layer Edit-menu controllers. Hosts `SelectAllAction`, `ClearSelectionAction` and their abstract base `AbstractSelectionAction`. **What I learned:** these actions are deliberately component-agnostic â€” they don't know about figures, they just delegate to whatever focused `EditableComponent` exists. **Contribution to the feature:** they are the controller entry points for two of the three selection scenarios (Select All, Deselect All) and resolve the focused component before forwarding the call. |
| `org.jhotdraw.api.gui` (module `jhotdraw-api`) | 1 | Defines the `EditableComponent` contract (`selectAll`, `clearSelection`, `SELECTION_EMPTY_PROPERTY`). **What I learned:** this single interface is the seam that lets the same Edit-menu actions work for the drawing canvas *and* for plain Swing text components. **Contribution to the feature:** it is the contract through which `SelectAllAction` and `ClearSelectionAction` reach the drawing view without depending on the drawing module. |
| `org.jhotdraw.draw` (module `jhotdraw-core`) | 2 | Core drawing framework. Visited classes: `DrawingEditor` and `DrawingView`. **What I learned:** `DrawingEditor` is a Mediator that exposes the active `DrawingView`, and `DrawingView` is the single source of truth for the current selection (it owns `selectedFigures`, `getSelectionCount()` and fires `FigureSelectionEvent`). **Contribution to the feature:** every selection change â€” whether triggered by a menu action, a toolbar button, or the mouse â€” ends up modifying `DrawingView` state and being broadcast from here. |
| `org.jhotdraw.draw.action` (module `jhotdraw-core`) | 2 | Drawing-aware action layer. Visited: `SelectSameAction` and its abstract base `AbstractSelectedAction`. **What I learned:** unlike the app-layer actions, these actions know about `DrawingEditor`, `DrawingView` and `Figure`, and `AbstractSelectedAction` already listens for `FigureSelectionEvent` to keep its enabled state in sync. **Contribution to the feature:** `SelectSameAction` is the third controller entry point; it inspects the currently selected figures and adds every figure of the same class to the selection. |
| `org.jhotdraw.draw.figure` (module `jhotdraw-core`) | 1 | Domain entity package containing `Figure`. **What I learned:** `SelectSameAction` only uses `Figure.getClass()` and `Figure.isSelectable()` â€” i.e. structural information, not behaviour. **Contribution to the feature:** figures are the things being counted/selected, but the feature does not require any new behaviour from `Figure` itself. |
| `org.jhotdraw.app` (module `jhotdraw-app`) | 3 | Application shell. Visited: `DefaultApplicationModel`, `DefaultMenuBuilder`, `AbstractView`. **What I learned:** action wiring is centralised â€” the model registers actions by ID, the menu builder looks them up by ID, and concrete views extend `AbstractView`. **Contribution to the feature:** this is what plugs the Edit-menu actions into every JHotDraw application without each sample having to wire them manually. |
| `org.jhotdraw.gui.action` (module `jhotdraw-gui`) | 1 | Visited: `ButtonFactory`. **What I learned:** the SVG-style toolbar with the *Select Same* button is assembled here, separate from the actions themselves. **Contribution to the feature:** it is the reason the *Select Same* button shows up on the toolbar of several samples without their panel code knowing about it. |
| `org.jhotdraw.samples.draw` (module `jhotdraw-samples-misc`) | 2 | Visited: `DrawingPanel` and `DrawView`. **What I learned:** `DrawingPanel` is just a layout container for the tools/attributes side panel, while `DrawView` is the `AbstractView` subclass that actually owns the scroll pane and the `DrawingView`. **Contribution to the feature:** `DrawView` is the natural place to host any per-window UI that reacts to selection (e.g. the status bar in the change request) because it has direct access to the `DrawingView` and its `FigureSelectionListener` registration. |

**Total packages visited: 8**
**Total classes visited: 15** (3 seeded by concept location + 12 inspected during the marking algorithm).

---

## What the table tells me overall

- The **automatic selection** feature is spread across **two clean layers**: a generic app-layer (`jhotdraw-actions` + `jhotdraw-api`) for menu-driven actions, and a drawing-aware layer (`jhotdraw-core` + `jhotdraw-gui`) for figure-aware actions. The split is what allows the same Edit menu to work for both text components and drawing canvases.
- The drawing core is wired with the **Observer pattern** (`FigureSelectionEvent` / `FigureSelectionListener` on `DrawingView`). This is why most packages I visited ended up *UNCHANGED* during the impact analysis â€” the API needed to react to selection changes is already exposed.
- The **only place that actually needs new code** for the change request lives in the sample (`org.jhotdraw.samples.draw.DrawView`), confirming that JHotDraw's separation between framework and sample applications works as intended.


---

# Chapter: RefactoringLab — Refactoring to Patterns

> **Branch used:** `AlansBranch` (feature branch off `development`, GitHub flow).
> **Build/test command:** `mvn -s .maven-settings.xml --batch-mode verify`
> **Reference:** Kerievsky, *Refactoring to Patterns* — Chapter 4 (smells) and the refactoring catalogue.

---

## 1. Code smell that triggered the refactoring

While inspecting the three controller classes of the *Automatic Selection* feature
(`SelectAllAction`, `ClearSelectionAction`, `SelectSameAction`), I found a clear
example of **Duplicated Code** (Kerievsky, Ch. 4) between the two app-layer actions.

`SelectAllAction.actionPerformed` and `ClearSelectionAction.actionPerformed` were
near byte-identical: roughly 14 of 18 lines were copy-paste. Only the body of the
two `instanceof` branches differed — one calls `selectAll()`, the other calls
`clearSelection()` (and the JTextComponent variant).

**Before — `SelectAllAction.actionPerformed`:**

```java
JComponent c = target;
if (c == null && (KeyboardFocusManager...getPermanentFocusOwner() instanceof JComponent)) {
    c = (JComponent) KeyboardFocusManager...getPermanentFocusOwner();
}
if (c != null && c.isEnabled()) {
    if (c instanceof EditableComponent)      ((EditableComponent) c).selectAll();
    else if (c instanceof JTextComponent)    ((JTextComponent) c).selectAll();
    else                                     c.getToolkit().beep();
}
```

**Before — `ClearSelectionAction.actionPerformed`:**

```java
JComponent c = target;
if (c == null && (KeyboardFocusManager...getPermanentFocusOwner() instanceof JComponent)) {
    c = (JComponent) KeyboardFocusManager...getPermanentFocusOwner();
}
if (c != null && c.isEnabled()) {
    if (c instanceof EditableComponent)      ((EditableComponent) c).clearSelection();
    else if (c instanceof JTextComponent) {
        JTextComponent tc = (JTextComponent) c;
        tc.select(tc.getSelectionStart(), tc.getSelectionStart());
    } else                                   c.getToolkit().beep();
}
```

This duplication is exactly the precondition Kerievsky lists for *Form Template Method*:
identical algorithm skeleton, varying leaf operations.

**Other smells noted (but not addressed here):**
- *Conditional Complexity* in the `instanceof` chain — would require widening
  `EditableComponent` or wrapping `JTextComponent`; cross-module change beyond scope.
- *Long Method* in `SelectSameAction.selectSame()` — borderline at 10 LoC; logged for
  future *Compose Method*.
- Cross-hierarchy duplication between the app-layer and draw-layer actions — the
  app/draw split is intentional architecture, so collapsing the hierarchies is
  explicitly out of scope.

---

## 2. What I planned to change

Pull the duplicated dispatch logic up into the existing common superclass
`AbstractSelectionAction` and expose the variation points as abstract hooks:

- `AbstractSelectionAction#actionPerformed(ActionEvent)` becomes a `final` template method.
- Two new `protected abstract` hooks — `actOnEditableComponent(EditableComponent)`
  and `actOnTextComponent(JTextComponent)` — capture the parts that vary.
- Each subclass loses its own `actionPerformed` and only implements the two hooks.

The public method signatures of both subclasses are preserved, so no caller has to change.

---

## 3. Strategy of the refactoring

Small, behaviour-preserving steps with a green build between each commit:

| Step | Action | Verification | Commit |
|---|---|---|---|
| 0 | Confirm I am on `AlansBranch`, baseline build is green. | `mvn ... -pl jhotdraw-actions -am verify` › BUILD SUCCESS | — |
| 1 | Add a TestNG dependency to `jhotdraw-actions` (no test infra existed). Write 6 *characterization tests* that pin the exact current behaviour: dispatch on `EditableComponent`, dispatch on `JTextComponent`, no-op on disabled target, for *both* actions. | All 6 tests green. | `d87e99b6` `test(actions): add characterization tests for SelectAllAction and ClearSelectionAction` |
| 2 | Apply *Form Template Method*: promote `actionPerformed` to `AbstractSelectionAction` as `final`; introduce two `protected abstract` hooks; replace the two subclasses' `actionPerformed` with hook implementations. | All 6 tests still green; full reactor (`mvn -DskipTests verify`, 12 modules) BUILD SUCCESS. | `b0aabaa5` `refactor(actions): Form Template Method on AbstractSelectionAction` |

### Why this order

- **Tests before refactor** — without them the refactoring is not really
  "behaviour-preserving"; it just *looks* preserving. The tests are the safety net.
- **Parent-class change before child-class change** — adding the abstract hooks and
  the `final actionPerformed` to the parent in the same commit as removing the
  subclasses' overrides keeps the project compiling at every commit boundary.
- **One logical refactoring per commit** — so the diff is reviewable and easy to
  revert if a regression is later discovered.

---

## 4. Refactoring(s) applied from [Ker05]

**Form Template Method** — Kerievsky, *Refactoring to Patterns*, p. 345.

**Reasoning.** The two methods exhibited the canonical input pattern for this
refactoring: same algorithm skeleton (resolve focused JComponent › guard on
enabled › dispatch on runtime type), differing only in the leaf operations
(*what* to do for each component kind). Kerievsky's book pairs this exact smell
(Duplicated Code with vertical variation) with *Form Template Method* as the
mechanical fix. *Extract Superclass* was not needed because
`AbstractSelectionAction` already existed.

**Refactorings I deliberately rejected.**

- *Replace Conditional with Polymorphism* on the `instanceof` chain over
  `EditableComponent` / `JTextComponent` / else. Rejected because `JTextComponent`
  is a Swing class I cannot retrofit, and adding wrapper classes for it would
  cross the architectural boundary the change request explicitly told me to
  respect.
- *Extract Method* on `SelectSameAction.selectSame()` — out of scope; would belong
  in a separate commit on the draw-layer hierarchy.

---

## 5. Result on `AlansBranch`

```
b0aabaa5 refactor(actions): Form Template Method on AbstractSelectionAction
d87e99b6 test(actions): add characterization tests for SelectAllAction and ClearSelectionAction
ba1a91b0 (origin/AlansBranch)  ‹ upstream baseline
```

- `AbstractSelectionAction` — gained `final actionPerformed(ActionEvent)` plus
  two `protected abstract` hooks.
- `SelectAllAction` — `actionPerformed` deleted; replaced by 4-line
  `actOnEditableComponent` and 4-line `actOnTextComponent`.
- `ClearSelectionAction` — same shape.
- 6 TestNG characterization tests added (all green, before *and* after the refactor).
- Full reactor build green (12 modules).
- Branch ready for a pull request `AlansBranch` › `develop`.

### Purpose of the refactoring

Beyond removing duplication, the *Form Template Method* refactoring **prepares**
the actions for the upcoming change request (status-bar selection counter):
once the dispatch logic lives in a single place, any cross-cutting addition —
logging, telemetry, status updates — can be applied to *both* `SelectAll` and
`ClearSelection` by editing one method instead of two.


---

## 6. SonarLint findings (Option A)

After installing SonarLint v5.3.0 and opening the five target files, SonarLint reported the rules below in the **Problems** panel. Each row maps the rule ID to the matching [Ker05] Chapter 4 smell and to the action I took.

| File | Rule | [Ker05] smell | Fix |
|---|---|---|---|
| `AbstractSelectionAction` | `java:S1604` Anonymous inner class can be a lambda | Long Method / scaffolding noise | Replaced the 11-line anonymous `PropertyChangeListener` with a 6-line lambda. |
| `AbstractSelectionAction` | `java:S5993` Abstract class has a public constructor | Inappropriate intimacy / API hygiene | Changed `public AbstractSelectionAction(...)` to `protected`. |
| `AbstractSelectionAction` | `java:S1948` Non-`Serializable` field in a `Serializable` class | (no [Ker05] match — pure correctness) | Marked `target` and `propertyHandler` as `transient`. |
| `AbstractSelectionAction` | `java:S1871` Two branches in a conditional have the same implementation | Duplicated Code | Collapsed `if ... else if ...` into a single `if (... \|\| ...)` inside the lambda. |
| `AbstractSelectedAction` | `java:S1124` Modifier order does not follow JLS | Code style / readability | `transient private DrawingView` › `private transient DrawingView`. |
| `AbstractSelectedAction` | `java:S1116` Empty statement | Speculative Generality | Removed stray `;` after the inner `EventHandler` class definition. |
| `AbstractSelectedAction` | `java:S125`  Commented-out code | Comments-as-Deodorant (Ch. 4) | Removed `//updateEnabledState();` from the constructor. |
| `AbstractSelectedAction` | `java:S5993` Abstract class has a public constructor | API hygiene | Changed `public AbstractSelectedAction(DrawingEditor)` to `protected`. |
| `AbstractSelectedAction` | `java:S1948` Non-`Serializable` field in a `Serializable` class | correctness | Made `editor` `transient` (in addition to `activeView` which was already transient). |
| `SelectAllAction` / `ClearSelectionAction` | `java:S1128` Unused / wildcard imports | Comments-as-Deodorant (dead noise) | Replaced `import java.awt.event.*; import javax.swing.*; import javax.swing.text.*; import org.jhotdraw.util.*;` with explicit single-type imports. |

### Why these findings are consistent with my main refactoring

The biggest finding (`java:S1871` Duplicated branches) is exactly the same family of smell as the **Duplicated Code** I addressed with *Form Template Method* in section 4 above — SonarLint independently confirmed the same direction. The remaining rules are smaller hygiene fixes (modifier order, dead code, unused imports, transient markers) that I addressed in a separate cleanup commit.

### Result on `AlansBranch`

```
af27751b refactor(actions): apply SonarLint cleanups around selection actions
b0aabaa5 refactor(actions): Form Template Method on AbstractSelectionAction
d87e99b6 test(actions): add characterization tests for SelectAllAction and ClearSelectionAction
ba1a91b0 (origin/AlansBranch)  ‹ upstream baseline
```

- All 6 TestNG characterization tests still green after the cleanup commit.
- Full reactor build (`mvn -s .maven-settings.xml --batch-mode -DskipTests verify`, 12 modules) › BUILD SUCCESS.
- After re-opening the five files, SonarLint reports zero findings on them.
