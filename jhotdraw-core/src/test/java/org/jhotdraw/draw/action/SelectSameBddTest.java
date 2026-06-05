/*
 * Behavior-Driven scenarios for user story US-3 ("Select Same").
 *
 * Each scenario maps a Given/When/Then sentence onto JGiven stages and uses
 * AssertJ for collection assertions. All Drawing-layer collaborators are
 * Mockito mocks so the scenarios exercise only SelectSameAction#selectSame().
 */
package org.jhotdraw.draw.action;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.As;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.junit.ScenarioTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.figure.Figure;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SelectSameBddTest
        extends ScenarioTest<SelectSameBddTest.GivenADrawing,
                             SelectSameBddTest.WhenTheUser,
                             SelectSameBddTest.ThenTheView> {

    /** Two distinct figure subtypes so equality of `getClass()` is deterministic. */
    interface FigureKindA extends Figure { }
    interface FigureKindB extends Figure { }

    @Test
    @As("US-3 best case: Select Same picks every figure that shares a class with the selection")
    public void us3_select_same_best_case() {
        given().a_drawing_with_$_figures_of_kind_A_and_$_of_kind_B(3, 1)
               .and().a_selection_containing_one_kind_A_figure();
        when().the_user_invokes_select_same();
        then().every_kind_A_figure_is_added_to_the_selection()
              .and().no_kind_B_figure_is_added();
    }

    @Test
    @As("US-3 boundary: empty drawing leaves the selection untouched")
    public void us3_select_same_on_empty_drawing() {
        given().an_empty_drawing()
               .and().a_selection_containing_one_kind_A_figure();
        when().the_user_invokes_select_same();
        then().the_view_receives_no_addToSelection_calls();
    }

    @Test
    @As("US-3 boundary: empty selection means the class-set is empty and nothing is added")
    public void us3_select_same_on_empty_selection() {
        given().a_drawing_with_$_figures_of_kind_A_and_$_of_kind_B(2, 2)
               .and().an_empty_selection();
        when().the_user_invokes_select_same();
        then().the_view_receives_no_addToSelection_calls();
    }

    @Test
    @As("US-3 boundary: mixed selection (A and B) selects every figure of either class")
    public void us3_select_same_with_mixed_selection() {
        given().a_drawing_with_$_figures_of_kind_A_and_$_of_kind_B(2, 2)
               .and().a_selection_containing_one_of_each_kind();
        when().the_user_invokes_select_same();
        then().every_kind_A_figure_is_added_to_the_selection()
              .and().every_kind_B_figure_is_added_to_the_selection();
    }

    // =================================================================
    //  Stages
    // =================================================================

    public static class GivenADrawing extends Stage<GivenADrawing> {

        @ProvidedScenarioState DrawingEditor editor;
        @ProvidedScenarioState DrawingView view;
        @ProvidedScenarioState Drawing drawing;
        @ProvidedScenarioState List<FigureKindA> figuresA = new ArrayList<>();
        @ProvidedScenarioState List<FigureKindB> figuresB = new ArrayList<>();

        public GivenADrawing a_drawing_with_$_figures_of_kind_A_and_$_of_kind_B(int aCount, int bCount) {
            initialise_mocks();
            for (int i = 0; i < aCount; i++) figuresA.add(mock(FigureKindA.class));
            for (int i = 0; i < bCount; i++) figuresB.add(mock(FigureKindB.class));
            List<Figure> all = new ArrayList<>();
            all.addAll(figuresA);
            all.addAll(figuresB);
            org.mockito.Mockito.when(drawing.getChildren()).thenReturn(all);
            return self();
        }

        public GivenADrawing an_empty_drawing() {
            initialise_mocks();
            org.mockito.Mockito.when(drawing.getChildren()).thenReturn(Collections.emptyList());
            return self();
        }

        public GivenADrawing a_selection_containing_one_kind_A_figure() {
            FigureKindA selected = mock(FigureKindA.class);
            org.mockito.Mockito.when(view.getSelectedFigures())
                    .thenReturn(new LinkedHashSet<>(Collections.singletonList(selected)));
            return self();
        }

        public GivenADrawing a_selection_containing_one_of_each_kind() {
            FigureKindA a = mock(FigureKindA.class);
            FigureKindB b = mock(FigureKindB.class);
            org.mockito.Mockito.when(view.getSelectedFigures())
                    .thenReturn(new LinkedHashSet<>(Arrays.asList(a, b)));
            return self();
        }

        public GivenADrawing an_empty_selection() {
            org.mockito.Mockito.when(view.getSelectedFigures()).thenReturn(Collections.emptySet());
            return self();
        }

        private void initialise_mocks() {
            if (editor != null) return;
            editor  = mock(DrawingEditor.class);
            view    = mock(DrawingView.class);
            drawing = mock(Drawing.class);
            lenient().when(editor.getActiveView()).thenReturn(view);
            lenient().when(view.getDrawing()).thenReturn(drawing);
            lenient().when(view.getSelectionCount()).thenReturn(0);
            lenient().when(view.isEnabled()).thenReturn(true);
        }
    }

    public static class WhenTheUser extends Stage<WhenTheUser> {

        @ExpectedScenarioState DrawingEditor editor;

        public WhenTheUser the_user_invokes_select_same() {
            new SelectSameAction(editor).selectSame();
            return self();
        }
    }

    public static class ThenTheView extends Stage<ThenTheView> {

        @ExpectedScenarioState DrawingView view;
        @ExpectedScenarioState List<FigureKindA> figuresA;
        @ExpectedScenarioState List<FigureKindB> figuresB;

        public ThenTheView every_kind_A_figure_is_added_to_the_selection() {
            assertThat(figuresA)
                    .as("there must be at least one kind-A figure to verify against")
                    .isNotEmpty();
            for (FigureKindA f : figuresA) {
                verify(view, times(1)).addToSelection(f);
            }
            return self();
        }

        public ThenTheView every_kind_B_figure_is_added_to_the_selection() {
            assertThat(figuresB).isNotEmpty();
            for (FigureKindB f : figuresB) {
                verify(view, times(1)).addToSelection(f);
            }
            return self();
        }

        public ThenTheView no_kind_B_figure_is_added() {
            for (FigureKindB f : figuresB) {
                verify(view, never()).addToSelection(f);
            }
            return self();
        }

        public ThenTheView the_view_receives_no_addToSelection_calls() {
            verify(view, never()).addToSelection(any(Figure.class));
            return self();
        }
    }
}
