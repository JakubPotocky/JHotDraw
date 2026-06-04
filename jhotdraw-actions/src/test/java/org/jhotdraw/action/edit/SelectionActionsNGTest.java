/*
 * Characterization tests for SelectAllAction and ClearSelectionAction.
 * Pin the current behavior of the two app-layer actions before refactoring.
 */
package org.jhotdraw.action.edit;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JTextField;
import org.jhotdraw.api.gui.EditableComponent;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

public class SelectionActionsNGTest {

    /** Minimal JComponent that also implements EditableComponent for testing. */
    @SuppressWarnings("serial")
    private static class FakeEditableComponent extends JComponent implements EditableComponent {
        int selectAllCalls = 0;
        int clearSelectionCalls = 0;
        int deleteCalls = 0;
        int duplicateCalls = 0;
        boolean selectionEmpty = true;

        @Override public void delete() { deleteCalls++; }
        @Override public void duplicate() { duplicateCalls++; }
        @Override public void selectAll() { selectAllCalls++; selectionEmpty = false; }
        @Override public void clearSelection() { clearSelectionCalls++; selectionEmpty = true; }
        @Override public boolean isSelectionEmpty() { return selectionEmpty; }
        @Override public void addPropertyChangeListener(String p, PropertyChangeListener l) { }
        @Override public void removePropertyChangeListener(String p, PropertyChangeListener l) { }
    }

    private static ActionEvent dummyEvent(JComponent src) {
        return new ActionEvent(src, ActionEvent.ACTION_PERFORMED, "test");
    }

    // ---------- SelectAllAction ----------

    @Test
    public void selectAll_onEditableComponent_callsSelectAll() {
        FakeEditableComponent c = new FakeEditableComponent();
        SelectAllAction a = new SelectAllAction(c);
        a.actionPerformed(dummyEvent(c));
        assertEquals(c.selectAllCalls, 1);
        assertEquals(c.clearSelectionCalls, 0);
    }

    @Test
    public void selectAll_onJTextComponent_callsSwingSelectAll() {
        JTextField tf = new JTextField("hello world");
        SelectAllAction a = new SelectAllAction(tf);
        a.actionPerformed(dummyEvent(tf));
        // Swing's JTextComponent#selectAll selects the full text
        assertEquals(tf.getSelectionStart(), 0);
        assertEquals(tf.getSelectionEnd(), "hello world".length());
    }

    @Test
    public void selectAll_onDisabledTarget_isNoOp() {
        FakeEditableComponent c = new FakeEditableComponent();
        c.setEnabled(false);
        SelectAllAction a = new SelectAllAction(c);
        a.actionPerformed(dummyEvent(c));
        assertEquals(c.selectAllCalls, 0);
    }

    // ---------- ClearSelectionAction ----------

    @Test
    public void clearSelection_onEditableComponent_callsClearSelection() {
        FakeEditableComponent c = new FakeEditableComponent();
        c.selectionEmpty = false;
        ClearSelectionAction a = new ClearSelectionAction(c);
        a.actionPerformed(dummyEvent(c));
        assertEquals(c.clearSelectionCalls, 1);
        assertEquals(c.selectAllCalls, 0);
        assertTrue(c.isSelectionEmpty());
    }

    @Test
    public void clearSelection_onJTextComponent_collapsesSelectionToCaret() {
        JTextField tf = new JTextField("hello world");
        tf.selectAll();
        int caret = tf.getSelectionStart();
        ClearSelectionAction a = new ClearSelectionAction(tf);
        a.actionPerformed(dummyEvent(tf));
        assertEquals(tf.getSelectionStart(), caret);
        assertEquals(tf.getSelectionEnd(), caret);
    }

    @Test
    public void clearSelection_onDisabledTarget_isNoOp() {
        FakeEditableComponent c = new FakeEditableComponent();
        c.selectionEmpty = false;
        c.setEnabled(false);
        ClearSelectionAction a = new ClearSelectionAction(c);
        a.actionPerformed(dummyEvent(c));
        assertEquals(c.clearSelectionCalls, 0);
        assertFalse(c.isSelectionEmpty());
    }
}
