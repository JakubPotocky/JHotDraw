# Bob's Lab Exercise Map For Copy/Paste

## Selected Feature

Parent feature: Basic editing.

Sub-features from Bob's screenshots:

- Copy #13: As a user, I want to copy a selected object so that I can quickly duplicate existing work without recreating it manually.
- Paste #14: As a user, I want to paste a copied object so that I can quickly place a duplicate on the canvas.

## Course Goal For This Work

The course portfolio is based on maintaining a larger existing software project. In this case, the larger existing project is JHotDraw. Bob's work should therefore explain copy/paste as maintenance of an existing framework feature, not as greenfield implementation.

## Exercise Sequence

| Order | Lab / Exercise | Course Deliverable | Bob's Copy/Paste Deliverable | Status |
| ---: | --- | --- | --- | --- |
| 1 | IntroLab | Get JHotDraw running with Maven and GitHub workflow. | Confirm codebase, modules, Maven project, selected feature context, current branch, and GUI launch attempt. | Completed in `bob/docs/exercises/01-intro-lab.md` |
| 2 | ChangeReqLab | Select feature, write user story, create portfolio artifact. | Document Basic editing #12 with copy #13 and paste #14 from screenshots. | Completed in `02_ChangeReqLab_Copy_Paste.md` |
| 3 | CLLab | Use debugger/dynamic analysis to locate feature concepts and produce initial class table. | Locate copy/paste action, transfer handler, clipboard, drawing, input/output formats, and sample wiring. | Completed in `03_CLLab_Concept_Location_Copy_Paste.md` |
| 4 | AnalysisLab1 | Apply static/dynamic analysis to estimate impacted classes; list packages, class counts, comments. | Produce package table and mark likely changed/unchanged areas for copy/paste. | Completed in `04_AnalysisLab1_Impact_Analysis_Copy_Paste.md` |
| 5 | CILab | Understand CI and set up GitHub Actions/Maven test build. | Document existing `.github/workflows/maven.yml`, Maven modules, and how CI supports copy/paste maintenance. | Completed in `05_CILab_Copy_Paste.md` |
| 6 | RefactoringLab1 / Code Smells | Identify code smells and apply suitable refactoring patterns. | Inspect copy/paste code smells such as duplicated action code, duplicated import logic, dead method, typo comments, and large transfer-handler responsibility. | Completed in `06_RefactoringLab1_Code_Smells_Copy_Paste.md` |
| 7 | ActualizationLab | Explain implementation, incorporation, change propagation, Clean Code, SOLID, Clean Architecture. | Explain how copy/paste is incorporated through Swing `TransferHandler`, JHotDraw `DrawingView`, `InputFormat`, and `OutputFormat`. | Completed in `08_ActualizationLab_Copy_Paste.md` |
| 8 | TestLab1 | Write unit tests for important business/domain functionality; document verification. | Added JUnit tests for DOM round-trip behavior and `DefaultDrawingViewTransferHandler` copy, paste, selection, unsupported data, and undo/redo. | Completed in `09_TestLab1_Copy_Paste.md` and `bob/docs/exercises/08-testing.md` |
| 9 | BDDLab | Map user stories to Given-When-Then and automate scenarios. | Added executable JUnit BDD-style scenarios for copy/paste rectangle, empty copy, unsupported paste, undo/redo, and text paste. | Completed in `10_BDDLab_Copy_Paste.md` and `bob/docs/exercises/09-bdd-user-stories-to-scenarios.md` |
| 10 | Conclusion / Baselines | Explain commit, baseline, acceptance testing, release readiness. | Covered in the newer portfolio conclusion and technical-debt notes. | Completed in `bob/docs/portfolio/09-conclusion-and-technical-debt.md` |
| 11 | Technical Debt Reflection | Explain maintainability, debt, legacy, socio-technical issues. | Covered in the newer portfolio conclusion and technical-debt notes. | Completed in `bob/docs/portfolio/09-conclusion-and-technical-debt.md` |

## Working Rule

Complete one exercise at a time. Each exercise should update `Portfolio_Copy_Paste.md` and have its own detailed markdown file in `BobsWork`.

## Current Status

The requested Copy/Paste lab sequence is complete in the workspace. The current Git branch is `copy-paste-basic-editing-labs`, and the lab work is present as local modified/untracked files until it is committed.
