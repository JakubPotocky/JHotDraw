# Portfolio Work

This document collects the portfolio deliverables for the Software Maintenance course.
Each chapter corresponds to one lab.

---

# Chapter: ChangeReqLab — Change Request

> **System under maintenance:** JHotDraw 7.6 (Java structured-graphics editor framework).
> **Selected feature:** Automatic Selection — covering three sub-features in the Edit menu / toolbar:
> - **Select All** — select every figure on the canvas.
> - **Deselect All (Clear Selection)** — empty the current selection.
> - **Select Same** — extend the current selection with every figure whose class matches a class already in the selection.

## User stories

The feature is described from the point of view of the end user of the Draw sample application, using the standard *"As a … I want … so that …"* template. Each story also lists its acceptance criteria.

### US-1 — Select All

> **As** a user editing a drawing,
> **I want** to select every figure on the canvas with a single command,
> **so that** I can apply an operation (move, delete, change attributes, copy) to the whole drawing without clicking each figure individually.

**Trigger:** menu *Edit → Select All*, or the keyboard shortcut `Ctrl+A`.

**Acceptance criteria:**
1. After the command, every selectable figure on the active canvas is part of the current selection.
2. Figures whose `isSelectable()` returns `false` (e.g. locked figures) are not added.
3. The selection handles are visible on every newly selected figure.
4. Any UI element that listens to selection changes (attribute panels, toolbar buttons) is refreshed.

### US-2 — Deselect All (Clear Selection)

> **As** a user editing a drawing,
> **I want** to clear the current selection in one step,
> **so that** I can start a fresh selection without having to click on empty canvas or unselect each figure individually.

**Trigger:** menu *Edit → Clear Selection*.

**Acceptance criteria:**
1. After the command, the current selection is empty.
2. All selection handles disappear from the canvas.
3. Actions whose enablement depends on a non-empty selection (e.g. *Select Same*) become disabled.
4. The command is a no-op (and visibly disabled) when the selection is already empty.

### US-3 — Select Same

> **As** a user editing a drawing that contains many shapes of different kinds,
> **I want** to extend the current selection to every figure of the same kind as the figures I already have selected,
> **so that** I can apply the same change (e.g. recolour all rectangles) to a whole category of shapes without picking them one by one.

**Trigger:** *Select Same* button on the selection toolbar.

**Acceptance criteria:**
1. The button is disabled when the current selection is empty.
2. After the command, the selection contains every figure on the canvas whose runtime class matches at least one class already in the previous selection.
3. Figures of other classes remain unselected.
4. The command works additively — figures already selected stay selected.

---

## Change request statement (chosen for the impact-analysis lab)

*After Select All / Deselect All / Select Same, the Draw sample window must display a status-bar message of the form `"N figure(s) selected"` reflecting the new selection size. The status bar must also stay in sync with any later selection change (mouse click, drag-rectangle, keyboard shortcut, etc.), not only with the three actions above.*

## Why this change request

- It exercises **all three** sub-features of the Automatic Selection feature, so concept location and impact analysis cover one coherent slice of the system.
- It is realistic — a status bar with the current selection count is a feature found in most graphical editors.
- It is non-trivial enough to propagate beyond the three controller classes (it reaches the View layer of the sample), but small enough to be feasible inside one lab.

---

# Chapter: CLLab — Concept Location

> **Technique used:** IDE Debugger (dynamic program analysis) on the Draw sample (`org.jhotdraw.samples.draw.Main`).
> Breakpoints were placed at suspected controller entry points; execution was followed via Step Over / Step Into; the **Call Stack** and **Variables** panels in VS Code were used to record every class that participates at runtime.
> Full step-by-step procedure is in [alans.md](alans.md) (Parts 1–4).

## Initial set of classes — result of concept location

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

# Chapter: AnalysisLab1 — Packages Visited After Concept Location

> **Feature analysed:** Automatic Selection (Select All / Deselect All / Select Same) in JHotDraw 7.6.
> **Change request used for the impact analysis:** *After Select All / Deselect All / Select Same, show a "N figure(s) selected" message in a status bar at the bottom of the Draw window, and keep it in sync with every later selection change.*

---

## Table 1 — Packages, classes visited, and what each package contributes

| Package name | # of classes visited | Comments |
|---|---:|---|
| `org.jhotdraw.action.edit` (module `jhotdraw-actions`) | 3 | App-layer Edit-menu controllers. Hosts `SelectAllAction`, `ClearSelectionAction` and their abstract base `AbstractSelectionAction`. **What I learned:** these actions are deliberately component-agnostic — they don't know about figures, they just delegate to whatever focused `EditableComponent` exists. **Contribution to the feature:** they are the controller entry points for two of the three selection scenarios (Select All, Deselect All) and resolve the focused component before forwarding the call. |
| `org.jhotdraw.api.gui` (module `jhotdraw-api`) | 1 | Defines the `EditableComponent` contract (`selectAll`, `clearSelection`, `SELECTION_EMPTY_PROPERTY`). **What I learned:** this single interface is the seam that lets the same Edit-menu actions work for the drawing canvas *and* for plain Swing text components. **Contribution to the feature:** it is the contract through which `SelectAllAction` and `ClearSelectionAction` reach the drawing view without depending on the drawing module. |
| `org.jhotdraw.draw` (module `jhotdraw-core`) | 2 | Core drawing framework. Visited classes: `DrawingEditor` and `DrawingView`. **What I learned:** `DrawingEditor` is a Mediator that exposes the active `DrawingView`, and `DrawingView` is the single source of truth for the current selection (it owns `selectedFigures`, `getSelectionCount()` and fires `FigureSelectionEvent`). **Contribution to the feature:** every selection change — whether triggered by a menu action, a toolbar button, or the mouse — ends up modifying `DrawingView` state and being broadcast from here. |
| `org.jhotdraw.draw.action` (module `jhotdraw-core`) | 2 | Drawing-aware action layer. Visited: `SelectSameAction` and its abstract base `AbstractSelectedAction`. **What I learned:** unlike the app-layer actions, these actions know about `DrawingEditor`, `DrawingView` and `Figure`, and `AbstractSelectedAction` already listens for `FigureSelectionEvent` to keep its enabled state in sync. **Contribution to the feature:** `SelectSameAction` is the third controller entry point; it inspects the currently selected figures and adds every figure of the same class to the selection. |
| `org.jhotdraw.draw.figure` (module `jhotdraw-core`) | 1 | Domain entity package containing `Figure`. **What I learned:** `SelectSameAction` only uses `Figure.getClass()` and `Figure.isSelectable()` — i.e. structural information, not behaviour. **Contribution to the feature:** figures are the things being counted/selected, but the feature does not require any new behaviour from `Figure` itself. |
| `org.jhotdraw.app` (module `jhotdraw-app`) | 3 | Application shell. Visited: `DefaultApplicationModel`, `DefaultMenuBuilder`, `AbstractView`. **What I learned:** action wiring is centralised — the model registers actions by ID, the menu builder looks them up by ID, and concrete views extend `AbstractView`. **Contribution to the feature:** this is what plugs the Edit-menu actions into every JHotDraw application without each sample having to wire them manually. |
| `org.jhotdraw.gui.action` (module `jhotdraw-gui`) | 1 | Visited: `ButtonFactory`. **What I learned:** the SVG-style toolbar with the *Select Same* button is assembled here, separate from the actions themselves. **Contribution to the feature:** it is the reason the *Select Same* button shows up on the toolbar of several samples without their panel code knowing about it. |
| `org.jhotdraw.samples.draw` (module `jhotdraw-samples-misc`) | 2 | Visited: `DrawingPanel` and `DrawView`. **What I learned:** `DrawingPanel` is just a layout container for the tools/attributes side panel, while `DrawView` is the `AbstractView` subclass that actually owns the scroll pane and the `DrawingView`. **Contribution to the feature:** `DrawView` is the natural place to host any per-window UI that reacts to selection (e.g. the status bar in the change request) because it has direct access to the `DrawingView` and its `FigureSelectionListener` registration. |

**Total packages visited: 8**
**Total classes visited: 15** (3 seeded by concept location + 12 inspected during the marking algorithm).

---

## What the table tells me overall

- The **automatic selection** feature is spread across **two clean layers**: a generic app-layer (`jhotdraw-actions` + `jhotdraw-api`) for menu-driven actions, and a drawing-aware layer (`jhotdraw-core` + `jhotdraw-gui`) for figure-aware actions. The split is what allows the same Edit menu to work for both text components and drawing canvases.
- The drawing core is wired with the **Observer pattern** (`FigureSelectionEvent` / `FigureSelectionListener` on `DrawingView`). This is why most packages I visited ended up *UNCHANGED* during the impact analysis — the API needed to react to selection changes is already exposed.
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


---

# Chapter: ActualizationLab — SOLID and Clean Architecture

> **Reference:** Martin, *Clean Architecture* (2017); Martin, *Agile Software Development, Principles, Patterns, and Practices* (2002, SOLID).
> **Selected feature:** Automatic Selection — *Select All*, *Deselect All*, *Select Same*.

The classes I will refer to throughout this chapter are the same ones from the Concept-Location and Refactoring chapters:

- `AbstractSelectionAction`, `SelectAllAction`, `ClearSelectionAction` (module `jhotdraw-actions`)
- `AbstractSelectedAction`, `SelectSameAction` (module `jhotdraw-core`, package `org.jhotdraw.draw.action`)
- Interfaces `EditableComponent`, `Figure`, `DrawingView`, `DrawingEditor` (module `jhotdraw-core`)

---

## 1. SOLID — concrete examples in the selection feature

### S — Single Responsibility Principle
> *A class should have one and only one reason to change.*

After the *Form Template Method* refactoring (commit `b0aabaa5`):

- `AbstractSelectionAction` has **one** reason to change: how a generic Edit action *dispatches* on the focused component (text vs. editable).
- `SelectAllAction` has **one** reason to change: what "select all" means for each component kind (`selectAll()`).
- `ClearSelectionAction` has **one** reason to change: what "deselect all" means (`clearSelection()` / collapsing the text caret).

Before the refactoring SRP was violated: the dispatch *and* the per-component behavior were both inside each subclass' `actionPerformed`, so a change in dispatch policy forced edits in two places. Now dispatch lives in the base class and behavior lives in hooks — each class has exactly one axis of change.

### O — Open/Closed Principle
> *Software entities should be open for extension, closed for modification.*

`AbstractSelectionAction` is now a textbook OCP example:

```java
public final void actionPerformed(ActionEvent e) {  // closed for modification
    if (target instanceof EditableComponent || target instanceof JTextComponent) {
        // dispatch to abstract hooks
    }
}
protected abstract void actOnEditableComponent(EditableComponent c);
protected abstract void actOnTextComponent(JTextComponent c);
```

Adding a new "selection" semantics (e.g. *Invert Selection*) is done by **extending** `AbstractSelectionAction` and implementing the two hooks — no edit to the base class. `final` on `actionPerformed` enforces the "closed" half of the principle.

### L — Liskov Substitution Principle
> *Subtypes must be substitutable for their base type.*

`SelectAllAction` and `ClearSelectionAction` honor LSP w.r.t. `AbstractSelectionAction`:

- They never throw stronger exceptions than the base contract.
- They never narrow the precondition (they accept any `target` the base accepts).
- They preserve the post-condition expressed by the characterization tests in [SelectionActionsNGTest](jhotdraw-actions/src/test/java/org/jhotdraw/action/edit/SelectionActionsNGTest.java) — when the base says "dispatch on focused EditableComponent", both subclasses really do operate on that component.

`SelectSameAction extends AbstractSelectedAction` is also LSP-clean: it uses the inherited `getView()` / `getEditor()` exactly as a generic `AbstractSelectedAction` client would, and adds no surprises on top.

### I — Interface Segregation Principle
> *Clients should not depend on interfaces they do not use.*

JHotDraw's selection feature is built on small, focused interfaces rather than a fat "selectable thing" interface:

- [EditableComponent](jhotdraw-core/src/main/java/org/jhotdraw/gui/EditableComponent.java) exposes only the four methods Edit actions need: `selectAll()`, `clearSelection()`, `delete()`, `duplicate()`. Drawing views implement it; so do custom editors. Neither has to know about the other.
- `DrawingView` exposes the selection-set API (`getSelectedFigures()`, `addToSelection`, `clearSelection`) separately from the rendering API (`drawingChanged`, `getDrawing`, …). `SelectSameAction` depends only on the selection slice.
- `Figure` is itself decomposed into smaller mixins (`AttributeKeys`, change listeners, etc.) so that an action interested only in attribute *equality* (the core of "Select Same") never sees rendering or geometry methods it does not call.

ISP violation example we *avoided*: had `EditableComponent` been merged into `DrawingView`, `SelectAllAction` would suddenly depend on rendering methods it never invokes — a classic ISP smell.

### D — Dependency Inversion Principle
> *Depend on abstractions, not on concretions.*

Every collaborator the selection feature touches is an interface, not a class:

| Caller | Depends on (abstract) | Concrete impl. injected at runtime |
|---|---|---|
| `AbstractSelectionAction` | `EditableComponent`, `JTextComponent` | whatever has keyboard focus |
| `AbstractSelectedAction`  | `DrawingEditor`, `DrawingView`        | `DefaultDrawingEditor`, `DefaultDrawingView` |
| `SelectSameAction`        | `Drawing`, `Figure`                   | `QuadTreeDrawing`, concrete `AbstractFigure` subclasses |

The high-level *policy* ("when the user invokes Select All, select everything in the focused editable thing") does not import any concrete Swing component or any concrete `Figure` subclass. Concrete classes are wired in by the application bootstrap (`Main` / `DrawApplicationModel`), not by the action code — the dependency arrow points *inward* toward the abstractions, exactly as DIP prescribes.

---

## 2. Clean Architecture for the selection feature

Robert C. Martin's *Clean Architecture* organizes code into concentric rings; the **Dependency Rule** says source-code dependencies may only point **inward**. Mapping the selection feature onto the four rings:

```
-¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¬
-  Frameworks & Drivers   (Swing, AWT, KeyEvent, JTextField)    -   outer
-  -¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¬  -
-  -  Interface Adapters  (Actions, Views)                   -  -
-  -   SelectAllAction, ClearSelectionAction,                -  -
-  -   SelectSameAction, DefaultDrawingView                  -  -
-  -  -¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¬  -  -
-  -  -  Application / Use Cases                          -  -  -
-  -  -   AbstractSelectionAction (dispatch policy),      -  -  -
-  -  -   AbstractSelectedAction (editor/view binding)    -  -  -
-  -  -  -¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¬  -  -  -
-  -  -  -  Entities / Enterprise Business Rules       -  -  -  -
-  -  -  -   Drawing, Figure, attribute model,         -  -  -  -
-  -  -  -   EditableComponent contract                -  -  -  -
-  -  -  L¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦-  -  -  -
-  -  L¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦-  -  -
-  L¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦-  -   inner
L¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦-
```

### Ring 1 — Entities (innermost)
The most stable concepts: a **Drawing** is a collection of **Figures**; a Figure has attributes; an **EditableComponent** is anything that can `selectAll` / `clearSelection`. These do not depend on Swing, on actions, or on which menu item triggered them. They live in `jhotdraw-core` and `jhotdraw-api` and would survive a port to JavaFX.

### Ring 2 — Use Cases (application policy)
The *what should happen when the user asks to Select All* policy lives in `AbstractSelectionAction.actionPerformed` and in `SelectSameAction.selectSame()`. They orchestrate entities (call `selectAll()` on an `EditableComponent`, iterate `getDrawing().getChildren()`) but know nothing about Swing painting or key bindings. They depend only on the inner ring.

### Ring 3 — Interface Adapters
`SelectAllAction`, `ClearSelectionAction`, `SelectSameAction` are **adapters**: they translate a Swing `ActionEvent` (outer ring) into a use-case call (inner ring). `DefaultDrawingView` adapts the `DrawingView` use-case interface to a concrete `JComponent`. This is the ring where the *plug* is shaped to fit Swing on one side and the use case on the other.

### Ring 4 — Frameworks & Drivers
Swing, AWT, the key-binding map, `JTextField`, the `EventHandler` glue — all the volatile I/O machinery. The selection feature touches Swing only through this outermost ring; the inner rings never `import javax.swing.*`. (After the SonarLint cleanup, even the imports in `SelectAllAction` are scoped to the precise Swing types it actually adapts.)

### Why the dependency direction matters here

- `AbstractSelectionAction` depends on `EditableComponent` (entity), not the other way around. If we replace Swing with JavaFX, the entity contract is untouched — only the outer two rings change.
- The unit tests in [SelectionActionsNGTest](jhotdraw-actions/src/test/java/org/jhotdraw/action/edit/SelectionActionsNGTest.java) work by feeding the action a **fake** `EditableComponent`. That is only possible *because* the action depends on the abstraction, not on `DefaultDrawingView` — the tests run with no `DrawingEditor` and no real drawing at all. This is the practical pay-off of obeying the Dependency Rule.
- The *Form Template Method* refactoring deliberately moved the dispatch policy **inward** (from each subclass into `AbstractSelectionAction`) and pushed the Swing-specific behavior **outward** (into the hooks). The refactoring therefore does not just remove duplication — it sharpens the ring boundary between application policy and Swing adapter.

### Where the architecture is *not* perfectly clean

Honesty matters in the portfolio:

- `AbstractSelectionAction` extends `javax.swing.AbstractAction` — that is a Ring-4 type leaking into Ring 2. A purer design would have the use case as a plain `SelectionUseCase` interface and a thin Swing adapter that *delegates* to it. JHotDraw chose pragmatic extension over strict separation, which is a common, conscious trade-off for desktop Swing apps.
- `SelectSameAction` reaches into `getView().getSelectedFigures()` synchronously; a strictly clean design would interpose a use-case interactor that emits a result the view subscribes to.

Calling these out shows I understand both the principle and the trade-offs the original authors made.

---

## 3. Take-aways for the assignment

1. The five SOLID principles each have a concrete witness in the selection feature, and the *Form Template Method* refactoring measurably improved SRP (one reason to change per class) and OCP (`final` template + abstract hooks).
2. Mapping the feature onto Clean Architecture's four rings shows that JHotDraw separates entities (`Drawing`, `Figure`, `EditableComponent`) from application policy (`Abstract*Action`), and that the SonarLint cleanups (transient fields, protected constructors, lambda) are small but real reinforcements of the inward-pointing dependency direction.
3. The same characterization tests that protected the refactoring also *prove* that the use case is decoupled from Swing — they run without any real Swing window because the use case depends on `EditableComponent`, not on `JComponent`.


---

# Chapter: TestingLab — JUnit 4 unit tests for the selected feature

> **Reference:** JUnit 4.13.2; Mockito 4.11.0; Java assertions ([JEP 8]/`java.lang.AssertionError`).
> **Selected feature:** Automatic Selection — *Select All*, *Deselect All*, *Select Same*.

## 1. Maven setup

JUnit 4 and Mockito were not yet on either of the two modules that own the feature. I added them as `<scope>test</scope>` dependencies:

- [jhotdraw-actions/pom.xml](jhotdraw-actions/pom.xml) — `junit:junit:4.13.2`, `org.mockito:mockito-core:4.11.0`.
- [jhotdraw-core/pom.xml](jhotdraw-core/pom.xml) — same two artifacts.

Mockito **4.11.0** is the last line that still supports Java 8, which is the build target for JHotDraw (`<maven.compiler.source>1.8</maven.compiler.source>`). Maven Surefire's default test pattern (`**/*Test.java`) automatically picks up the new files alongside the existing TestNG `*NGTest.java` ones; both providers run side-by-side. Surefire enables `-ea` by default, so my Java assertions are active during the test run.

## 2. What was tested and why

I focused on the **most important domain logic** for the feature — the part that survives a UI port and cannot be replaced by a Swing key binding:

| Method under test | File | Why it is "important domain logic" |
|---|---|---|
| `AbstractSelectionAction#actionPerformed(ActionEvent)` (template method) | [AbstractSelectionAction.java](jhotdraw-actions/src/main/java/org/jhotdraw/action/edit/AbstractSelectionAction.java) | Dispatches to `EditableComponent` vs. `JTextComponent` vs. "beep" — the *policy* that all three scenarios depend on. |
| `SelectAllAction#actOnEditableComponent` / `actOnTextComponent` | [SelectAllAction.java](jhotdraw-actions/src/main/java/org/jhotdraw/action/edit/SelectAllAction.java) | Per-component meaning of "select all". |
| `ClearSelectionAction#actOnEditableComponent` / `actOnTextComponent` | [ClearSelectionAction.java](jhotdraw-actions/src/main/java/org/jhotdraw/action/edit/ClearSelectionAction.java) | Per-component meaning of "deselect". |
| `SelectSameAction#selectSame()` | [SelectSameAction.java](jhotdraw-core/src/main/java/org/jhotdraw/draw/action/SelectSameAction.java) | Pure domain logic — collect classes from the current selection, then add every figure of those classes to the selection. |

## 3. Test files created

- [jhotdraw-actions/src/test/java/org/jhotdraw/action/edit/SelectionActionsJUnit4Test.java](jhotdraw-actions/src/test/java/org/jhotdraw/action/edit/SelectionActionsJUnit4Test.java) — 11 tests covering `SelectAllAction` and `ClearSelectionAction`.
- [jhotdraw-core/src/test/java/org/jhotdraw/draw/action/SelectSameActionJUnit4Test.java](jhotdraw-core/src/test/java/org/jhotdraw/draw/action/SelectSameActionJUnit4Test.java) — 6 tests covering `SelectSameAction#selectSame()`.

## 4. Strategy: one path per test, mocks at every boundary

Each test follows the rule from the assignment:

> *A unit test should test a single code-path through a single method. When the execution of a method passes outside of that method, you have a dependency and should apply mocks/stubs.*

Concrete realisations:

- For `AbstractSelectionAction` I mock a **JComponent that also implements EditableComponent** in one Mockito instance using `withSettings().extraInterfaces(EditableComponent.class)`. The action therefore sees the right runtime type without me having to instantiate any real Swing widget.
- For `JTextComponent` I use a real `JTextField` because it is a value object — no external state, deterministic, no I/O. Replacing it with a mock would not isolate any dependency (it would mock the standard library), so a real instance is the simpler choice.
- For `SelectSameAction` every collaborator (`DrawingEditor`, `DrawingView`, `Drawing`, `Figure`) is a Mockito mock. Two private nested interfaces, `FigureKindA` and `FigureKindB`, give me deterministic distinct `Class<?>` keys without needing concrete `AbstractFigure` subclasses (which would drag in the rendering stack).

## 5. Best-case tests

| # | Test | Verifies |
|---|---|---|
| 1 | `selectAll_bestCase_editableComponent_invokesSelectAllExactlyOnce` | Best path: target is an `EditableComponent` → `selectAll()` is called exactly once and `clearSelection()` is never called. |
| 2 | `selectAll_bestCase_jTextComponent_selectsFullText` | Best path on a `JTextField` containing `"hello world"` → selection range is `[0, 11)`. |
| 3 | `clearSelection_bestCase_editableComponent_invokesClearSelectionExactlyOnce` | Mirror of #1 for `clearSelection`. |
| 4 | `clearSelection_bestCase_jTextComponent_collapsesCaret` | After clearing a previously selected `"hello world"`, `selectionStart == selectionEnd`. |
| 5 | `selectSame_bestCase_addsAllFiguresOfSameClassToSelection` | One `FigureKindA` is selected; a drawing of `[A, A, A, B]` results in the three `A`s added via `view.addToSelection(...)` and the `B` never added. |

## 6. Boundary-case tests

| # | Test | Boundary explored |
|---|---|---|
| 6 | `selectAll_boundary_disabledTarget_isNoOp` | `target.isEnabled() == false` → action must do nothing. |
| 7 | `selectAll_boundary_emptyTextDocument_selectionRangeIsZeroLength` | Empty `JTextField` → selection range is `[0, 0)`. |
| 8 | `selectAll_boundary_repeatedInvocations_areIdempotentInDispatch` | Calling `actionPerformed` three times yields exactly three `selectAll()` calls and zero `clearSelection()` calls — idempotent in *dispatch*, never crosses into the wrong branch. |
| 9 | `selectAll_boundary_unknownComponentKind_beepsAndDoesNotSelect` | Target is a plain `JComponent` (neither `EditableComponent` nor `JTextComponent`) → `toolkit.beep()` is called once and no selection method is ever invoked. |
| 10 | `clearSelection_boundary_disabledTarget_isNoOp` | Mirror of #6 for `clearSelection`. |
| 11 | `clearSelection_boundary_alreadyEmptyTextDocument_remainsCollapsed` | Already-empty `JTextField` stays collapsed at `[0, 0]`. |
| 12 | `selectSame_boundary_emptyDrawing_addsNothing` | Drawing has zero children → `view.addToSelection(...)` is never invoked. |
| 13 | `selectSame_boundary_emptySelection_addsNothing` | Selection is empty → empty class-set → no figures are added even though the drawing is non-empty. |
| 14 | `selectSame_boundary_noMatchingClasses_addsNothing` | Selected `FigureKindA` but the drawing only contains `FigureKindB` instances → nothing is added. |
| 15 | `selectSame_boundary_mixedSelection_addsBothClasses` | Selection contains an `A` and a `B`; siblings of both kinds in the drawing → both are added. |
| 16 | `constructor_registersOnTarget_andActionIsNotNull` | Both action constructors register a `PropertyChangeListener` on the target (the `WeakPropertyChangeListener` indirection from the parent class). |
| 17 | `constructor_doesNotThrow_withValidEditor` | Smoke for `SelectSameAction(editor)`. |

## 7. Java assertions for invariants

Java's `assert` keyword is used for things that **must never be false** — fixture invariants. If they ever fire, the test infrastructure itself is broken (not the production code), and we want execution stopped immediately rather than letting a misleading "expected X but got Y" message bubble up.

```java
// SelectionActionsJUnit4Test#setUp
assert editableTarget instanceof EditableComponent
        : "Test fixture invariant violated: target is not an EditableComponent";

// SelectSameActionJUnit4Test#setUp
assert editor.getActiveView() != null : "fixture: active view must not be null";
assert view.getDrawing() != null     : "fixture: drawing must not be null";

// ClearSelectionAction best-case test, before exercising the action
assertTrue("fixture: text should be selected before action runs",
        textTarget.getSelectionEnd() > textTarget.getSelectionStart());
```

This matches the rule from the assignment:

- `assert` halts the JVM with `AssertionError` — used for *should never happen* fixture invariants.
- `IllegalArgumentException` / `NullPointerException` would let the program continue — not used here, because a broken fixture is not a recoverable condition.

Surefire turns assertions on automatically (`-ea`), so all `assert` statements above run during `mvn test`.

## 8. How I verified the feature

```powershell
mvn -s .maven-settings.xml --batch-mode -pl jhotdraw-actions test `
    '-Dtest=SelectionActionsJUnit4Test' `
    '-Dsurefire.failIfNoSpecifiedTests=false'
# → Tests run: 11, Failures: 0, Errors: 0, Skipped: 0  — BUILD SUCCESS

mvn -s .maven-settings.xml --batch-mode -pl jhotdraw-core test `
    '-Dtest=SelectSameActionJUnit4Test' `
    '-Dsurefire.failIfNoSpecifiedTests=false'
# → Tests run: 6, Failures: 0, Errors: 0, Skipped: 0  — BUILD SUCCESS

mvn -s .maven-settings.xml --batch-mode -DskipTests verify
# → 12/12 modules — BUILD SUCCESS
```

Combined evidence:

- **17 JUnit 4 tests** (best case + boundaries + smoke) on the three controllers of the feature, plus the 6 TestNG characterization tests added during the *Refactoring* lab → **23 automated checks** guarding the feature.
- Every collaborator that crosses a class boundary (`EditableComponent`, `DrawingEditor`, `DrawingView`, `Drawing`, `Figure`, `Toolkit`) is mocked, so each test exercises exactly one method and one path.
- Java assertions guard the test fixtures themselves so that a broken fixture cannot masquerade as a feature regression.
- The full reactor still builds (`mvn verify` → 12 modules green), so the new dependencies do not poison any other module.
