/*
 * JUnit 4 unit tests for the "Automatic Selection" feature controllers.
 *
 * Scope: domain logic of SelectAllAction and ClearSelectionAction.
 * Strategy: each test exercises a SINGLE code path through actionPerformed()
 *           and uses Mockito to stub the collaborator (EditableComponent /
 *           JTextComponent) so the test never depends on real Swing focus
 *           management or on JHotDraw's drawing model.
 *
 * The test class is deliberately separate from SelectionActionsNGTest (TestNG)
 * which characterizes legacy behavior; this class is the Classwork JUnit 4
 * suite covering best-case and boundary scenarios.
 */
package org.jhotdraw.action.edit;

import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import org.jhotdraw.api.gui.EditableComponent;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class SelectionActionsJUnit4Test {

    /** Mock EditableComponent that is also a JComponent (the AbstractSelectionAction
     *  resolution path requires the target to extend JComponent). */
    private JComponent editableTarget;
    private EditableComponent editable;
    private JTextField textTarget;

    @Before
    public void setUp() {
        // Mock both interfaces in one object: a JComponent subclass that also
        // implements EditableComponent.
        editableTarget = mock(JComponent.class, withSettings().extraInterfaces(EditableComponent.class));
        editable = (EditableComponent) editableTarget;
        when(editableTarget.isEnabled()).thenReturn(true);

        textTarget = new JTextField("hello world");

        // Java assertion: the mocked target must satisfy the AbstractSelectionAction
        // contract (JComponent + EditableComponent). Should never be false here;
        // if it ever is, the test setup itself is broken.
        assert editableTarget instanceof EditableComponent
                : "Test fixture invariant violated: target is not an EditableComponent";
    }

    private static ActionEvent event(JComponent src) {
        return new ActionEvent(src, ActionEvent.ACTION_PERFORMED, "test");
    }

    // ---------------------------------------------------------------------
    //  SelectAllAction — best case
    // ---------------------------------------------------------------------

    @Test
    public void selectAll_bestCase_editableComponent_invokesSelectAllExactlyOnce() {
        SelectAllAction action = new SelectAllAction(editableTarget);

        action.actionPerformed(event(editableTarget));

        verify(editable, times(1)).selectAll();
        verify(editable, never()).clearSelection();
    }

    @Test
    public void selectAll_bestCase_jTextComponent_selectsFullText() {
        SelectAllAction action = new SelectAllAction(textTarget);

        action.actionPerformed(event(textTarget));

        // Swing's JTextComponent#selectAll selects the full text [0, length).
        assertEquals(0, textTarget.getSelectionStart());
        assertEquals("hello world".length(), textTarget.getSelectionEnd());
    }

    // ---------------------------------------------------------------------
    //  SelectAllAction — boundary cases
    // ---------------------------------------------------------------------

    /** Boundary: target is disabled — actionPerformed must be a no-op. */
    @Test
    public void selectAll_boundary_disabledTarget_isNoOp() {
        when(editableTarget.isEnabled()).thenReturn(false);
        SelectAllAction action = new SelectAllAction(editableTarget);

        action.actionPerformed(event(editableTarget));

        verify(editable, never()).selectAll();
        verify(editable, never()).clearSelection();
    }

    /** Boundary: text component is empty — selectAll over an empty document
     *  is well-defined and must leave the caret range at [0, 0]. */
    @Test
    public void selectAll_boundary_emptyTextDocument_selectionRangeIsZeroLength() {
        JTextComponent empty = new JTextField("");
        SelectAllAction action = new SelectAllAction((JComponent) empty);

        action.actionPerformed(event((JComponent) empty));

        assertEquals(0, empty.getSelectionStart());
        assertEquals(0, empty.getSelectionEnd());
    }

    /** Boundary: invoked multiple times in a row — must remain idempotent
     *  (each invocation calls selectAll() exactly once, never clearSelection). */
    @Test
    public void selectAll_boundary_repeatedInvocations_areIdempotentInDispatch() {
        SelectAllAction action = new SelectAllAction(editableTarget);

        action.actionPerformed(event(editableTarget));
        action.actionPerformed(event(editableTarget));
        action.actionPerformed(event(editableTarget));

        verify(editable, times(3)).selectAll();
        verify(editable, never()).clearSelection();
    }

    // ---------------------------------------------------------------------
    //  ClearSelectionAction — best case
    // ---------------------------------------------------------------------

    @Test
    public void clearSelection_bestCase_editableComponent_invokesClearSelectionExactlyOnce() {
        ClearSelectionAction action = new ClearSelectionAction(editableTarget);

        action.actionPerformed(event(editableTarget));

        verify(editable, times(1)).clearSelection();
        verify(editable, never()).selectAll();
    }

    @Test
    public void clearSelection_bestCase_jTextComponent_collapsesCaret() {
        textTarget.setCaretPosition(0);
        textTarget.moveCaretPosition("hello world".length()); // select full text
        // sanity check the test fixture itself before exercising the action
        assertTrue("fixture: text should be selected before action runs",
                textTarget.getSelectionEnd() > textTarget.getSelectionStart());

        ClearSelectionAction action = new ClearSelectionAction(textTarget);
        action.actionPerformed(event(textTarget));

        // After clearSelection the caret collapses to selectionStart.
        assertEquals(textTarget.getSelectionStart(), textTarget.getSelectionEnd());
    }

    // ---------------------------------------------------------------------
    //  ClearSelectionAction — boundary cases
    // ---------------------------------------------------------------------

    @Test
    public void clearSelection_boundary_disabledTarget_isNoOp() {
        when(editableTarget.isEnabled()).thenReturn(false);
        ClearSelectionAction action = new ClearSelectionAction(editableTarget);

        action.actionPerformed(event(editableTarget));

        verify(editable, never()).clearSelection();
        verify(editable, never()).selectAll();
    }

    @Test
    public void clearSelection_boundary_alreadyEmptyTextDocument_remainsCollapsed() {
        JTextComponent empty = new JTextField("");
        ClearSelectionAction action = new ClearSelectionAction((JComponent) empty);

        action.actionPerformed(event((JComponent) empty));

        assertEquals(0, empty.getSelectionStart());
        assertEquals(0, empty.getSelectionEnd());
    }

    // ---------------------------------------------------------------------
    //  Cross-cutting: action must be constructed successfully and expose its
    //  target (sanity unit test for the constructor branch that registers a
    //  weak property listener).
    // ---------------------------------------------------------------------

    @Test
    public void constructor_registersOnTarget_andActionIsNotNull() {
        SelectAllAction a = new SelectAllAction(editableTarget);
        ClearSelectionAction b = new ClearSelectionAction(editableTarget);
        assertNotNull(a);
        assertNotNull(b);
        // The target must have had at least one PropertyChangeListener registered
        // by AbstractSelectionAction's constructor (the WeakPropertyChangeListener).
        verify(editableTarget, times(2))
                .addPropertyChangeListener(org.mockito.ArgumentMatchers.any());
    }

    /** Branch coverage: if the target is neither EditableComponent nor JTextComponent,
     *  the action falls into the "beep" branch and must NOT touch any selection API.
     *  We simulate this by giving the action a plain JComponent and verifying that
     *  beep() is invoked on the toolkit and no selection method is called. */
    @Test
    public void selectAll_boundary_unknownComponentKind_beepsAndDoesNotSelect() {
        JComponent plain = mock(JComponent.class);
        java.awt.Toolkit toolkit = mock(java.awt.Toolkit.class);
        when(plain.isEnabled()).thenReturn(true);
        when(plain.getToolkit()).thenReturn(toolkit);

        SelectAllAction action = new SelectAllAction(plain);
        action.actionPerformed(event(plain));

        verify(toolkit, times(1)).beep();
        verify(editable, never()).selectAll();
    }
}
