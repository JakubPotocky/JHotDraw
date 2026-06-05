/*
 * Behavior-Driven scenarios for the "Automatic Selection" feature.
 *
 * Maps user stories US-1 (Select All) and US-2 (Deselect All) onto JGiven
 * Given/When/Then stages and uses AssertJ for domain assertions.
 *
 * The Swing-fixture variant lives in SelectAllSwingBddTest so that this class
 * can run in any environment (no display required).
 */
package org.jhotdraw.action.edit;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.As;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import com.tngtech.jgiven.junit.ScenarioTest;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import javax.swing.JTextField;
import org.jhotdraw.api.gui.EditableComponent;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

public class AutomaticSelectionBddTest
        extends ScenarioTest<AutomaticSelectionBddTest.GivenAComponent,
                             AutomaticSelectionBddTest.WhenTheUser,
                             AutomaticSelectionBddTest.ThenTheComponent> {

    // -----------------------------------------------------------------
    //  US-1 — Select All
    // -----------------------------------------------------------------

    @Test
    @As("US-1 best case: Select All on a focused EditableComponent calls selectAll() once")
    public void us1_select_all_on_editable_component() {
        given().a_focused_enabled_editable_component();
        when().the_user_invokes_select_all();
        then().select_all_is_invoked_$_times(1)
              .and().clear_selection_is_never_invoked();
    }

    @Test
    @As("US-1 best case: Select All on a focused JTextField selects the entire text")
    public void us1_select_all_on_text_field() {
        given().a_focused_text_field_containing_$("hello world");
        when().the_user_invokes_select_all();
        then().the_text_field_selection_range_is_$_to_$(0, "hello world".length());
    }

    @Test
    @As("US-1 boundary: a disabled component is left untouched")
    public void us1_select_all_on_disabled_component_is_a_no_op() {
        given().a_focused_disabled_editable_component();
        when().the_user_invokes_select_all();
        then().select_all_is_never_invoked()
              .and().clear_selection_is_never_invoked();
    }

    // -----------------------------------------------------------------
    //  US-2 — Deselect All
    // -----------------------------------------------------------------

    @Test
    @As("US-2 best case: Deselect All on a focused EditableComponent calls clearSelection() once")
    public void us2_deselect_all_on_editable_component() {
        given().a_focused_enabled_editable_component();
        when().the_user_invokes_deselect_all();
        then().clear_selection_is_invoked_$_times(1)
              .and().select_all_is_never_invoked();
    }

    @Test
    @As("US-2 best case: Deselect All on a focused JTextField collapses the caret")
    public void us2_deselect_all_on_text_field() {
        given().a_focused_text_field_containing_$("hello world");
        when().the_full_text_in_the_text_field_is_pre_selected()
              .and().the_user_invokes_deselect_all();
        then().the_text_field_selection_is_collapsed();
    }

    @Test
    @As("US-2 boundary: Deselect All on a disabled component is a no-op")
    public void us2_deselect_all_on_disabled_component_is_a_no_op() {
        given().a_focused_disabled_editable_component();
        when().the_user_invokes_deselect_all();
        then().clear_selection_is_never_invoked()
              .and().select_all_is_never_invoked();
    }

    // =================================================================
    //  Stages
    // =================================================================

    public static class GivenAComponent extends Stage<GivenAComponent> {

        @ProvidedScenarioState
        JComponent target;
        @ProvidedScenarioState
        EditableComponent editable;
        @ProvidedScenarioState
        JTextField textField;

        public GivenAComponent a_focused_enabled_editable_component() {
            target = mock(JComponent.class, withSettings().extraInterfaces(EditableComponent.class));
            editable = (EditableComponent) target;
            org.mockito.Mockito.when(target.isEnabled()).thenReturn(true);
            return self();
        }

        public GivenAComponent a_focused_disabled_editable_component() {
            target = mock(JComponent.class, withSettings().extraInterfaces(EditableComponent.class));
            editable = (EditableComponent) target;
            org.mockito.Mockito.when(target.isEnabled()).thenReturn(false);
            return self();
        }

        public GivenAComponent a_focused_text_field_containing_$(@Quoted String text) {
            textField = new JTextField(text);
            target = textField;
            return self();
        }
    }

    public static class WhenTheUser extends Stage<WhenTheUser> {

        @ExpectedScenarioState
        JComponent target;
        @ExpectedScenarioState
        EditableComponent editable;
        @ExpectedScenarioState
        JTextField textField;

        public WhenTheUser the_user_invokes_select_all() {
            new SelectAllAction(target).actionPerformed(
                    new ActionEvent(target, ActionEvent.ACTION_PERFORMED, "selectAll"));
            return self();
        }

        public WhenTheUser the_user_invokes_deselect_all() {
            new ClearSelectionAction(target).actionPerformed(
                    new ActionEvent(target, ActionEvent.ACTION_PERFORMED, "clearSelection"));
            return self();
        }

        public WhenTheUser the_full_text_in_the_text_field_is_pre_selected() {
            textField.setCaretPosition(0);
            textField.moveCaretPosition(textField.getText().length());
            return self();
        }
    }

    public static class ThenTheComponent extends Stage<ThenTheComponent> {

        @ExpectedScenarioState
        EditableComponent editable;
        @ExpectedScenarioState
        JTextField textField;

        public ThenTheComponent select_all_is_invoked_$_times(int n) {
            verify(editable, times(n)).selectAll();
            return self();
        }

        public ThenTheComponent select_all_is_never_invoked() {
            verify(editable, never()).selectAll();
            return self();
        }

        public ThenTheComponent clear_selection_is_invoked_$_times(int n) {
            verify(editable, times(n)).clearSelection();
            return self();
        }

        public ThenTheComponent clear_selection_is_never_invoked() {
            verify(editable, never()).clearSelection();
            return self();
        }

        public ThenTheComponent the_text_field_selection_range_is_$_to_$(int start, int end) {
            assertThat(textField.getSelectionStart()).isEqualTo(start);
            assertThat(textField.getSelectionEnd()).isEqualTo(end);
            return self();
        }

        public ThenTheComponent the_text_field_selection_is_collapsed() {
            assertThat(textField.getSelectionStart())
                    .as("after deselect, the caret must be collapsed")
                    .isEqualTo(textField.getSelectionEnd());
            return self();
        }
    }
}
