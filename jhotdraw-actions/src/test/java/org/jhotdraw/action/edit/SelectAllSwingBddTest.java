/*
 * BDD scenario driven through AssertJ-Swing for the "Select All" feature.
 *
 * This complements AutomaticSelectionBddTest by automating the same user story
 * through a real Swing window. AssertJ-Swing's Robot drives the application
 * exactly as a human user would (focus the field, press Ctrl+A's binding, ...).
 *
 * Requires a graphical display; the whole class is skipped when running in a
 * headless environment.
 */
package org.jhotdraw.action.edit;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.As;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import com.tngtech.jgiven.junit.ScenarioTest;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.JTextField;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SelectAllSwingBddTest
        extends ScenarioTest<SelectAllSwingBddTest.GivenAVisibleApp,
                             SelectAllSwingBddTest.WhenTheUser,
                             SelectAllSwingBddTest.ThenTheApp> {

    @BeforeClass
    public static void skipIfHeadless() {
        org.junit.Assume.assumeFalse("AssertJ-Swing scenarios require a display",
                GraphicsEnvironment.isHeadless());
    }

    @After
    public void tearDownFixture() {
        if (frame != null) frame.cleanUp();
    }

    /** Shared between stages and tearDown. */
    static FrameFixture frame;

    @Test
    @As("US-1 via AssertJ-Swing: invoking SelectAllAction on a real JFrame selects the visible text")
    public void us1_select_all_through_assertj_swing() {
        given().a_visible_window_with_a_text_field_containing_$("hello world");
        when().the_user_invokes_select_all_on_the_text_field();
        then().the_text_field_reports_full_selection_$("hello world");
    }

    // =================================================================
    //  Stages
    // =================================================================

    public static class GivenAVisibleApp extends Stage<GivenAVisibleApp> {

        @ProvidedScenarioState
        JTextField textField;
        @ProvidedScenarioState
        FrameFixture window;

        public GivenAVisibleApp a_visible_window_with_a_text_field_containing_$(@Quoted String text) {
            JFrame f = GuiActionRunner.execute(() -> {
                JFrame jf = new JFrame("BDD-test");
                JTextField tf = new JTextField(text, 20);
                tf.setName("input");
                jf.add(tf);
                jf.pack();
                return jf;
            });
            textField = (JTextField) f.getContentPane().getComponent(0);
            window = new FrameFixture(f);
            window.show();
            frame = window;
            return self();
        }
    }

    public static class WhenTheUser extends Stage<WhenTheUser> {

        @ExpectedScenarioState
        JTextField textField;
        @ExpectedScenarioState
        FrameFixture window;

        public WhenTheUser the_user_invokes_select_all_on_the_text_field() {
            // focus the text field, then trigger SelectAllAction directly so the
            // scenario does not depend on a particular keymap.
            window.textBox("input").focus();
            GuiActionRunner.execute(() ->
                    new SelectAllAction(textField).actionPerformed(
                            new java.awt.event.ActionEvent(textField,
                                    java.awt.event.ActionEvent.ACTION_PERFORMED,
                                    "selectAll")));
            return self();
        }
    }

    public static class ThenTheApp extends Stage<ThenTheApp> {

        @ExpectedScenarioState
        JTextField textField;
        @ExpectedScenarioState
        FrameFixture window;

        public ThenTheApp the_text_field_reports_full_selection_$(@Quoted String expected) {
            JTextComponentFixture tf = window.textBox("input");
            tf.requireText(expected);
            assertThat(textField.getSelectedText())
                    .as("AssertJ-Swing: full text must be selected")
                    .isEqualTo(expected);
            return self();
        }
    }
}
