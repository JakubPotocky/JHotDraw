/*
 * JUnit 4 unit tests for the "Select Same" feature.
 *
 * Strategy:
 *  - The domain logic under test is SelectSameAction#selectSame().
 *  - All collaborators (DrawingEditor, DrawingView, Drawing, Figure) are
 *    Mockito mocks; the test does not rely on any concrete JHotDraw figure
 *    nor on a real Swing window.
 *  - Each test exercises a single code path through selectSame():
 *      (1) best case: same-class figures are added to the selection
 *      (2) boundary: empty drawing → selectSame is a no-op
 *      (3) boundary: selection is empty → selectSame is a no-op
 *      (4) boundary: every figure has a different class than the
 *          selected one  → no addition happens
 */
package org.jhotdraw.draw.action;

import java.util.Arrays;
import java.util.Collections;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.figure.Figure;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SelectSameActionJUnit4Test {

    /** Two distinct Figure subtypes so we can compare classes deterministically. */
    private interface FigureKindA extends Figure { }
    private interface FigureKindB extends Figure { }

    private DrawingEditor editor;
    private DrawingView view;
    private Drawing drawing;

    @Before
    public void setUp() {
        editor = mock(DrawingEditor.class);
        view = mock(DrawingView.class);
        drawing = mock(Drawing.class);
        // The action's parent class subscribes to the editor on construction.
        when(editor.getActiveView()).thenReturn(view);
        // Default selection state for tests that override below.
        lenient().when(view.getDrawing()).thenReturn(drawing);
        lenient().when(view.getSelectionCount()).thenReturn(0);
        lenient().when(view.isEnabled()).thenReturn(true);

        // Assertion: invariant that the test fixture wires the editor → view → drawing
        // chain that selectSame() walks. If any link is null the action would NPE
        // and the failure mode would be confusing rather than informative.
        assert editor.getActiveView() != null : "fixture: active view must not be null";
        assert view.getDrawing() != null     : "fixture: drawing must not be null";
    }

    // ---------------------------------------------------------------------
    //  Best case
    // ---------------------------------------------------------------------

    @Test
    public void selectSame_bestCase_addsAllFiguresOfSameClassToSelection() {
        FigureKindA selectedA = mock(FigureKindA.class);
        FigureKindA otherA1   = mock(FigureKindA.class);
        FigureKindA otherA2   = mock(FigureKindA.class);
        FigureKindB unrelated = mock(FigureKindB.class);

        when(view.getSelectedFigures()).thenReturn(
                new java.util.LinkedHashSet<>(Collections.singletonList(selectedA)));
        when(drawing.getChildren()).thenReturn(
                Arrays.asList(selectedA, otherA1, otherA2, unrelated));

        SelectSameAction action = new SelectSameAction(editor);
        action.selectSame();

        // Same-class figures must be added (including the originally-selected one,
        // which is current behavior — it is selected onto an already-selected set,
        // an idempotent operation).
        verify(view, times(1)).addToSelection(selectedA);
        verify(view, times(1)).addToSelection(otherA1);
        verify(view, times(1)).addToSelection(otherA2);
        // The unrelated kind is never added.
        verify(view, never()).addToSelection(unrelated);
    }

    // ---------------------------------------------------------------------
    //  Boundary cases
    // ---------------------------------------------------------------------

    /** Boundary: drawing has zero children — selectSame must be a no-op. */
    @Test
    public void selectSame_boundary_emptyDrawing_addsNothing() {
        FigureKindA selected = mock(FigureKindA.class);
        when(view.getSelectedFigures()).thenReturn(
                new java.util.LinkedHashSet<>(Collections.singletonList(selected)));
        when(drawing.getChildren()).thenReturn(Collections.emptyList());

        new SelectSameAction(editor).selectSame();

        verify(view, never()).addToSelection(any(Figure.class));
    }

    /** Boundary: selection is empty — selectSame computes an empty class-set
     *  and therefore must not add anything, even though figures exist. */
    @Test
    public void selectSame_boundary_emptySelection_addsNothing() {
        FigureKindA fig1 = mock(FigureKindA.class);
        FigureKindA fig2 = mock(FigureKindA.class);
        when(view.getSelectedFigures()).thenReturn(Collections.emptySet());
        when(drawing.getChildren()).thenReturn(Arrays.asList(fig1, fig2));

        new SelectSameAction(editor).selectSame();

        verify(view, never()).addToSelection(any(Figure.class));
    }

    /** Boundary: no figure in the drawing matches the class of the selection. */
    @Test
    public void selectSame_boundary_noMatchingClasses_addsNothing() {
        FigureKindA selected = mock(FigureKindA.class);
        FigureKindB other1   = mock(FigureKindB.class);
        FigureKindB other2   = mock(FigureKindB.class);

        when(view.getSelectedFigures()).thenReturn(
                new java.util.LinkedHashSet<>(Collections.singletonList(selected)));
        when(drawing.getChildren()).thenReturn(Arrays.asList(other1, other2));

        new SelectSameAction(editor).selectSame();

        verify(view, never()).addToSelection(other1);
        verify(view, never()).addToSelection(other2);
    }

    /** Boundary: selection contains figures of two different classes — every
     *  figure of either class must be added. */
    @Test
    public void selectSame_boundary_mixedSelection_addsBothClasses() {
        FigureKindA selA = mock(FigureKindA.class);
        FigureKindB selB = mock(FigureKindB.class);
        FigureKindA siblingA = mock(FigureKindA.class);
        FigureKindB siblingB = mock(FigureKindB.class);

        when(view.getSelectedFigures()).thenReturn(
                new java.util.LinkedHashSet<>(Arrays.asList(selA, selB)));
        when(drawing.getChildren()).thenReturn(Arrays.asList(siblingA, siblingB));

        new SelectSameAction(editor).selectSame();

        verify(view, times(1)).addToSelection(siblingA);
        verify(view, times(1)).addToSelection(siblingB);
    }

    // ---------------------------------------------------------------------
    //  Smoke
    // ---------------------------------------------------------------------

    @Test
    public void constructor_doesNotThrow_withValidEditor() {
        SelectSameAction action = new SelectSameAction(editor);
        assertNotNull(action);
    }
}
