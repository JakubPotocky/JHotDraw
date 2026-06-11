#!/usr/bin/env python3
"""Generate the 10 figures for BobsWork/Final_Merged_Report_CopyPaste.md.

Design goals: readable, explicit, NOT overcomplicated. Every fact shown is
verified against the repository at commit b4eea564.
"""
import io
import os
import textwrap

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch
from PIL import Image, ImageDraw, ImageFont
from pygments import highlight
from pygments.lexers import JavaLexer
from pygments.formatters import ImageFormatter

OUT = "/home/bob/Desktop/university/softwareMaintenance/JHotDraw/BobsWork/diagrams"
os.makedirs(OUT, exist_ok=True)

FONT_DIR = os.path.join(matplotlib.get_data_path(), "fonts", "ttf")
F_BOLD = os.path.join(FONT_DIR, "DejaVuSans-Bold.ttf")
F_REG = os.path.join(FONT_DIR, "DejaVuSans.ttf")
F_MONO = os.path.join(FONT_DIR, "DejaVuSansMono.ttf")

# ---------- shared colours ----------
C_BOX = "#dce9f7"     # plain step
C_DEC = "#ffe6b3"     # decision
C_OK = "#d8f0d3"      # success / unchanged
C_CHG = "#f6c8c4"     # changed
C_DOC = "#fde3c0"     # docs-only change
C_EDGE = "#2c3e50"
C_TITLE = "#1f4e79"


# ---------- matplotlib helpers ----------
def new_ax(w, h):
    fig, ax = plt.subplots(figsize=(w, h), dpi=160)
    ax.set_xlim(0, 1)
    ax.set_ylim(0, 1)
    ax.axis("off")
    return fig, ax


def box(ax, cx, cy, w, h, text, fc=C_BOX, fs=11, bold_first=False, ec=C_EDGE, lw=1.4):
    ax.add_patch(FancyBboxPatch((cx - w / 2, cy - h / 2), w, h,
                                boxstyle="round,pad=0.006", fc=fc, ec=ec, lw=lw))
    if bold_first and "\n" in text:
        first, rest = text.split("\n", 1)
        ax.text(cx, cy + h * 0.22, first, ha="center", va="center",
                fontsize=fs + 1, fontweight="bold")
        ax.text(cx, cy - h * 0.16, rest, ha="center", va="center", fontsize=fs)
    else:
        ax.text(cx, cy, text, ha="center", va="center", fontsize=fs,
                fontweight="bold" if bold_first else "normal")


def arrow(ax, x1, y1, x2, y2, label=None, dashed=False, fs=10, lx=None, ly=None):
    ax.annotate("", xy=(x2, y2), xytext=(x1, y1),
                arrowprops=dict(arrowstyle="-|>", lw=1.5, color=C_EDGE,
                                linestyle="--" if dashed else "-",
                                shrinkA=2, shrinkB=2))
    if label:
        ax.text(lx if lx is not None else (x1 + x2) / 2,
                ly if ly is not None else (y1 + y2) / 2,
                label, ha="center", va="center", fontsize=fs, style="italic",
                bbox=dict(fc="white", ec="none", pad=1.2))


def title(ax, text, fs=15):
    ax.text(0.5, 1.015, text, ha="center", va="bottom", fontsize=fs,
            fontweight="bold", color=C_TITLE, transform=ax.transAxes)


def save(fig, name):
    fig.savefig(os.path.join(OUT, name), bbox_inches="tight",
                facecolor="white", pad_inches=0.18)
    plt.close(fig)
    print("wrote", name)


# ---------- PIL helpers (code / console figures) ----------
def add_title_bar(img, main, sub=None, bar=C_TITLE, bg="white"):
    tf = ImageFont.truetype(F_BOLD, 24)
    sf = ImageFont.truetype(F_REG, 17)
    bar_h = 46 + (26 if sub else 0)
    w = max(img.width, 40 + int(tf.getlength(main)),
            (40 + int(sf.getlength(sub))) if sub else 0)
    out = Image.new("RGB", (w, bar_h + img.height), bg)
    d = ImageDraw.Draw(out)
    d.rectangle([0, 0, w, bar_h], fill=bar)
    d.text((18, 10), main, font=tf, fill="white")
    if sub:
        d.text((18, 44), sub, font=sf, fill="#cfe2f3")
    out.paste(img, (0, bar_h))
    return out


def code_figure(code, name, main, sub=None, line_start=1, bar=C_TITLE):
    fmt = ImageFormatter(font_name="DejaVu Sans Mono", font_size=15,
                         line_numbers=True, line_number_start=line_start,
                         line_pad=4, image_pad=12, style="default")
    png = highlight(code, JavaLexer(), fmt)
    img = Image.open(io.BytesIO(png)).convert("RGB")
    add_title_bar(img, main, sub, bar).save(os.path.join(OUT, name))
    print("wrote", name)


def slice_dedent(path, lo, hi):
    lines = open(path).read().splitlines()[lo - 1:hi]
    return textwrap.dedent("\n".join(lines))


HANDLER = ("/home/bob/Desktop/university/softwareMaintenance/JHotDraw/"
           "jhotdraw-core/src/main/java/org/jhotdraw/draw/"
           "DefaultDrawingViewTransferHandler.java")


# =====================================================================
# Figure 1 — activity diagram of the concept location search process
# =====================================================================
def fig01_clean():
    fig, ax = new_ax(9, 10.5)
    title(ax, "Figure 1 — Concept Location: search process (activity diagram)")
    ax.add_patch(plt.Circle((0.5, 0.975), 0.011, fc=C_EDGE))
    box(ax, 0.5, 0.905, 0.66, 0.075,
        'Formulate search query from the change request\n(e.g. "paste", "clipboard", "Transferable")')
    box(ax, 0.5, 0.785, 0.54, 0.07,
        "Find a starting set of modules\n(grep search over the codebase)")
    box(ax, 0.5, 0.665, 0.48, 0.07,
        "Select one candidate module\nand read / debug it")
    box(ax, 0.5, 0.525, 0.42, 0.07, "Is the concept\nimplemented here?", fc=C_DEC)
    box(ax, 0.84, 0.525, 0.25, 0.07, "Mark as located;\nrecord in table", fc=C_OK)
    box(ax, 0.5, 0.375, 0.48, 0.07,
        "Does it delegate the concept\nto supplier modules?", fc=C_DEC)
    box(ax, 0.15, 0.375, 0.25, 0.085, "Follow dependencies:\nvisit supplier modules")
    box(ax, 0.5, 0.225, 0.60, 0.07,
        "Backtrack: return to previous module\nor pick another from the starting set")

    arrow(ax, 0.5, 0.964, 0.5, 0.944)
    arrow(ax, 0.5, 0.867, 0.5, 0.821)
    arrow(ax, 0.5, 0.750, 0.5, 0.701)
    arrow(ax, 0.5, 0.630, 0.5, 0.561)
    arrow(ax, 0.71, 0.525, 0.715, 0.525, label="yes", lx=0.713, ly=0.545)
    ax.add_patch(plt.Circle((0.84, 0.435), 0.011, fc=C_EDGE))
    ax.add_patch(plt.Circle((0.84, 0.435), 0.017, fill=False, ec=C_EDGE, lw=1.4))
    arrow(ax, 0.84, 0.49, 0.84, 0.455)
    arrow(ax, 0.5, 0.49, 0.5, 0.411, label="no", lx=0.52, ly=0.455)
    arrow(ax, 0.26, 0.375, 0.276, 0.375, label="yes", lx=0.268, ly=0.395)
    arrow(ax, 0.15, 0.418, 0.15, 0.665, label=None)
    arrow(ax, 0.15, 0.665, 0.259, 0.665)
    arrow(ax, 0.5, 0.34, 0.5, 0.261, label="no", lx=0.52, ly=0.30)
    arrow(ax, 0.80, 0.225, 0.92, 0.225)
    arrow(ax, 0.92, 0.225, 0.92, 0.665)
    arrow(ax, 0.92, 0.665, 0.741, 0.665)

    ax.text(0.5, 0.10,
            'This run:  grep "paste"  →  PasteAction (thin, delegates)  →  dependency search  →'
            '  TransferHandler\n→  DefaultDrawingViewTransferHandler.importData  ⇒  concept located;'
            ' confirmed with debugger breakpoints',
            ha="center", va="center", fontsize=10.5, style="italic",
            bbox=dict(fc="#f4f6f8", ec="#aaaaaa", pad=6))
    save(fig, "fig01-concept-location-activity-diagram.png")


# =====================================================================
# Figure 2 — UML class diagram of the work area
# =====================================================================
def fig02():
    fig, ax = new_ax(13, 8.5)
    title(ax, "Figure 2 — UML class diagram of the Copy/Paste work area (after commit b4eea564)")
    box(ax, 0.14, 0.93, 0.21, 0.075, "CopyAction\n+actionPerformed(e)", bold_first=True)
    box(ax, 0.14, 0.81, 0.21, 0.075, "PasteAction\n+actionPerformed(e)", bold_first=True)
    box(ax, 0.72, 0.875, 0.27, 0.105,
        "«abstract» TransferHandler\n+exportToClipboard(...)\n+importData(...)",
        bold_first=True)
    box(ax, 0.44, 0.565, 0.46, 0.20,
        "DefaultDrawingViewTransferHandler\n"
        "+importData(comp, t): boolean\n"
        "#createTransferable(...): Transferable\n"
        "−importTransferData(...): boolean   (new, line 174)\n"
        "−firePasteUndoableEdit(...)   (new, line 195)",
        bold_first=True, fs=10.5)
    box(ax, 0.855, 0.565, 0.25, 0.13,
        "DefaultDrawingView\n+addToSelection(figs)\n+clearSelection()\ninstalls handler (line 305)",
        bold_first=True, fs=10.5)
    box(ax, 0.14, 0.27, 0.23, 0.11,
        "«interface» InputFormat\n+isDataFlavorSupported(f)\n+read(t, drawing, replace)",
        bold_first=True, fs=10.5)
    box(ax, 0.44, 0.27, 0.22, 0.09,
        "«interface» OutputFormat\n+createTransferable(...)", bold_first=True, fs=10.5)
    box(ax, 0.75, 0.27, 0.26, 0.13,
        "Drawing\n+getChildren()\n+getInput/OutputFormats()\n+fireUndoableEditHappened(e)",
        bold_first=True, fs=10.5)
    box(ax, 0.29, 0.065, 0.32, 0.06, "DOMStorableInputOutputFormat", bold_first=True, fs=10.5)

    arrow(ax, 0.245, 0.93, 0.585, 0.90, label="delegates export", lx=0.41, ly=0.945)
    arrow(ax, 0.245, 0.81, 0.585, 0.85, label="delegates importData", lx=0.40, ly=0.80)
    arrow(ax, 0.56, 0.665, 0.66, 0.822, label="extends", lx=0.645, ly=0.74)
    arrow(ax, 0.30, 0.465, 0.18, 0.327, label="reads via", lx=0.205, ly=0.40)
    arrow(ax, 0.44, 0.465, 0.44, 0.317, label="writes via", lx=0.495, ly=0.39)
    arrow(ax, 0.58, 0.465, 0.72, 0.337, label="modifies; fires undo edits", lx=0.685, ly=0.41)
    arrow(ax, 0.67, 0.585, 0.728, 0.585, label="updates selection", lx=0.70, ly=0.625)
    arrow(ax, 0.22, 0.095, 0.165, 0.213, dashed=True, label="implements", lx=0.135, ly=0.14)
    arrow(ax, 0.37, 0.095, 0.425, 0.223, dashed=True)
    save(fig, "fig02-uml-class-diagram-workarea.png")


# =====================================================================
# Figure 3 — dynamic analysis: breakpoints hit in order
# =====================================================================
def fig03():
    fig, ax = new_ax(12, 7.5)
    title(ax, "Figure 3 — Dynamic impact analysis: debugger breakpoints hit in order (Draw sample)")
    for cx, head, steps, result in (
        (0.26, "Edit → Copy",
         ["1   CopyAction.actionPerformed",
          "2   DefaultDrawingViewTransferHandler\n     .createTransferable",
          "3   DOMStorableInputOutputFormat\n     .createTransferable"],
         "Selection exported to the clipboard\nas one CompositeTransferable"),
        (0.74, "Edit → Paste",
         ["1   PasteAction.actionPerformed",
          "2   DefaultDrawingViewTransferHandler\n     .importData",
          "3   DOMStorableInputOutputFormat.read",
          "4   DefaultDrawingView.addToSelection"],
         "Figures added and selected;\nundoable paste edit registered"),
    ):
        box(ax, cx, 0.91, 0.26, 0.065, head, fc=C_DEC, bold_first=True, fs=13)
        y = 0.80
        prev = 0.877
        for s in steps:
            h = 0.085 if "\n" in s else 0.06
            box(ax, cx, y, 0.40, h, s, fs=11)
            arrow(ax, cx, prev, cx, y + h / 2 + 0.004)
            prev = y - h / 2
            y -= (h + 0.055)
        box(ax, cx, y + 0.01, 0.40, 0.08, result, fc=C_OK, fs=11)
        arrow(ax, cx, prev, cx, y + 0.055)
    ax.text(0.5, 0.035,
            "Runtime change check: modify the offset tx.translate(5, 5) in DefaultDrawingView.duplicate()"
            " (line 1345),\nrecompile, run — the visibly different duplicate offset proves the modified"
            " code path is the one executed.",
            ha="center", va="center", fontsize=10.5, style="italic",
            bbox=dict(fc="#f4f6f8", ec="#aaaaaa", pad=6))
    save(fig, "fig03-dynamic-analysis-breakpoints.png")


# =====================================================================
# Figure 4 — impact analysis: visited vs. changed
# =====================================================================
def fig04():
    fig, ax = new_ax(12.5, 8)
    title(ax, "Figure 4 — Impact analysis: visited (9 packages / 35 classes) vs. changed (commit b4eea564)")
    row1 = [("action.edit (5)", 0.10), ("api.gui (1)", 0.30), ("app (2)", 0.50),
            ("draw.figure (3)", 0.70), ("draw.io (7)", 0.90)]
    row2 = [("gui.action (1)", 0.20), ("draw (6)", 0.40), ("samples.draw (3)", 0.60),
            ("datatransfer (7)", 0.80)]
    for t, cx in row1:
        box(ax, cx, 0.885, 0.175, 0.055, t, fc="#eef1f4", fs=10.5)
    for t, cx in row2:
        box(ax, cx, 0.795, 0.175, 0.055, t, fc="#eef1f4", fs=10.5)

    box(ax, 0.32, 0.545, 0.50, 0.15,
        "CHANGED\nDefaultDrawingViewTransferHandler\nExtract Method ×2  ·  dead getDrawing() removed\n"
        "comments rewritten  ·  625 → 576 lines", fc=C_CHG, bold_first=True, fs=11.5)
    box(ax, 0.79, 0.545, 0.34, 0.13,
        "CHANGED (docs only)\nCompositeTransferable\nJavaDoc typos fixed  ·  fields made final",
        fc=C_DOC, bold_first=True, fs=11.5)
    arrow(ax, 0.40, 0.768, 0.345, 0.625, label="narrowed to", lx=0.32, ly=0.70)
    arrow(ax, 0.80, 0.768, 0.79, 0.615, label="narrowed to", lx=0.855, ly=0.70)

    box(ax, 0.5, 0.30, 0.93, 0.10,
        "UNCHANGED:  actions · view · formats · UI wiring — the duplication was local;\n"
        "propagation deliberately stopped (action-layer duplication → backlog)", fc=C_OK, fs=12)
    box(ax, 0.5, 0.135, 0.93, 0.095,
        "ADDED (test scope, in samples.draw):  DefaultDrawingViewTransferHandlerTest ·"
        " CopyPasteBddTest ·\nDOMStorableInputOutputFormatTest   (13 tests in total)",
        fc="#e8e3f5", fs=11)
    save(fig, "fig04-impact-visited-vs-changed.png")


# =====================================================================
# Figure 5/6/7 — code figures (verbatim from the repository)
# =====================================================================
def fig05():
    before = "/tmp/handler_before.java"
    code = slice_dedent(before, 101, 137) + "\n..."
    code_figure(code, "fig05-before-duplicated-block.png",
                "Figure 5 — BEFORE: the duplicated import block (appeared 3× inside importData)",
                "DefaultDrawingViewTransferHandler.java @ parent of b4eea564 — one of three copies"
                " (macOS branch, lines 101–137; indentation reduced)",
                line_start=101, bar="#8c2f2f")


def fig06():
    code = (
        "// macOS branch (line 101) and default branch (line 113):\n"
        "// each duplicated ~35-line block is now ONE guarded call\n"
        "if (format.isDataFlavorSupported(flavor)) {\n"
        "    if (importTransferData(comp, t, transferFigures, dropPoint,\n"
        "                           view, drawing, format)) {\n"
        "        retValue = true;\n"
        "        break SearchLoop;\n"
        "    }\n"
        "}\n"
        "\n"
        "// file-drop path (line 153): reuses the same shared undo construction\n"
        "firePasteUndoableEdit(drawing, importedFigures);\n"
    )
    code_figure(code, "fig06-after-call-sites.png",
                "Figure 6 — AFTER: the three call sites (lines 101, 113, 153)",
                "DefaultDrawingViewTransferHandler.java @ b4eea564 — excerpt, reformatted"
                " for readability (logic verbatim)",
                line_start=1, bar="#1e6b34")


def fig07():
    code = slice_dedent(HANDLER, 174, 217)
    code_figure(code, "fig07-after-extracted-methods.png",
                "Figure 7 — AFTER: the two extracted methods (Extract Method ×2)",
                "importTransferData(...) line 174 · firePasteUndoableEdit(...) line 195 —"
                " verbatim from b4eea564",
                line_start=174, bar="#1e6b34")


# =====================================================================
# Figure 8 — structure before vs. after
# =====================================================================
def fig08():
    fig, ax = new_ax(13, 8)
    title(ax, "Figure 8 — Postfactoring: structure of the import path before vs. after commit b4eea564")
    # panel frames
    for x0, head, color in ((0.03, "BEFORE — 625 lines", "#8c2f2f"),
                            (0.53, "AFTER — 576 lines (−49)", "#1e6b34")):
        ax.add_patch(FancyBboxPatch((x0, 0.04), 0.44, 0.84, boxstyle="round,pad=0.004",
                                    fc="#fbfbfb", ec="#bbbbbb", lw=1))
        ax.text(x0 + 0.22, 0.915, head, ha="center", fontsize=14, fontweight="bold", color=color)

    # left: one big method with 3 red copies
    box(ax, 0.25, 0.565, 0.40, 0.50, "", fc="#f3f3f3")
    ax.text(0.25, 0.775, "importData(...)  ≈ 185 lines", ha="center",
            fontsize=12.5, fontweight="bold")
    box(ax, 0.25, 0.675, 0.34, 0.075,
        "copy 1 — macOS branch:\nimport sequence + anonymous undo edit", fc=C_CHG, fs=10.5)
    box(ax, 0.25, 0.565, 0.34, 0.075,
        "copy 2 — default branch:\nimport sequence + anonymous undo edit", fc=C_CHG, fs=10.5)
    box(ax, 0.25, 0.455, 0.34, 0.075,
        "copy 3 — file-drop path:\nanonymous undo edit (again)", fc=C_CHG, fs=10.5)
    box(ax, 0.25, 0.21, 0.36, 0.075,
        "private getDrawing() — dead code\n(throw new UnsupportedOperationException)",
        fc="#e2e2e2", fs=10.5)
    ax.text(0.25, 0.105, "any fix to the paste logic had to be applied 3×",
            ha="center", fontsize=10.5, style="italic", color="#8c2f2f")

    # right: thin call sites -> two shared methods
    box(ax, 0.75, 0.66, 0.40, 0.26, "", fc="#f3f3f3")
    ax.text(0.75, 0.745, "importData(...) — format negotiation only", ha="center",
            fontsize=12.5, fontweight="bold")
    box(ax, 0.655, 0.665, 0.16, 0.045, "call — line 101", fc="white", fs=10)
    box(ax, 0.845, 0.665, 0.16, 0.045, "call — line 113", fc="white", fs=10)
    box(ax, 0.75, 0.595, 0.22, 0.045, "undo call — line 153", fc="white", fs=10)
    box(ax, 0.75, 0.43, 0.38, 0.085,
        "importTransferData(...) — line 174\nONE shared import sequence", fc=C_OK,
        bold_first=False, fs=11.5)
    box(ax, 0.75, 0.27, 0.38, 0.085,
        "firePasteUndoableEdit(...) — line 195\nONE shared undo/redo edit", fc=C_OK, fs=11.5)
    arrow(ax, 0.655, 0.642, 0.70, 0.477)
    arrow(ax, 0.845, 0.642, 0.80, 0.477)
    ax.plot([0.862, 0.952, 0.952], [0.595, 0.595, 0.27], color=C_EDGE, lw=1.5)
    arrow(ax, 0.952, 0.27, 0.943, 0.27)
    arrow(ax, 0.75, 0.387, 0.75, 0.317, label="calls (line 186)", lx=0.655, ly=0.352)
    ax.text(0.75, 0.115, "getDrawing() deleted ✓      a future change is a one-place change",
            ha="center", fontsize=10.5, style="italic", color="#1e6b34")
    save(fig, "fig08-structure-before-after.png")


# =====================================================================
# Figure 9 — executed test run (console)
# =====================================================================
def fig09():
    lines = [
        ("$ mvn -pl jhotdraw-samples/jhotdraw-samples-misc -am test", "#6bd0f5"),
        ("-------------------------------------------------------", "#888888"),
        (" T E S T S", "#d4d4d4"),
        ("-------------------------------------------------------", "#888888"),
        ("Running org.jhotdraw.samples.draw.DefaultDrawingViewTransferHandlerTest", "#dcdcaa"),
        ("Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.282 sec", "#d4d4d4"),
        ("Running org.jhotdraw.samples.draw.CopyPasteBddTest", "#dcdcaa"),
        ("Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.186 sec", "#d4d4d4"),
        ("Running org.jhotdraw.samples.draw.DOMStorableInputOutputFormatTest", "#dcdcaa"),
        ("Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 sec", "#d4d4d4"),
        ("", "#d4d4d4"),
        ("Results :", "#d4d4d4"),
        ("", "#d4d4d4"),
        ("Tests run: 13, Failures: 0, Errors: 0, Skipped: 0", "#4ec94e"),
        ("", "#d4d4d4"),
        ("(exit code 0 — BUILD SUCCESS)", "#4ec94e"),
    ]
    mono = ImageFont.truetype(F_MONO, 19)
    pad, lh = 24, 30
    width = pad * 2 + max(int(mono.getlength(t)) for t, _ in lines)
    img = Image.new("RGB", (width, pad * 2 + lh * len(lines)), "#1e1e1e")
    d = ImageDraw.Draw(img)
    for i, (t, col) in enumerate(lines):
        d.text((pad, pad + i * lh), t, font=mono, fill=col)
    out = add_title_bar(
        img,
        "Figure 9 — Verification: executed test run (5 + 5 + 3 = 13 tests, all green)",
        "branch copy-paste-basic-editing-labs @ b4eea564 — run 11 June 2026, headless",
        bar="#1e6b34", bg="#1e1e1e")
    out.save(os.path.join(OUT, "fig09-test-run-results.png"))
    print("wrote fig09-test-run-results.png")


# =====================================================================
# Figure 10 — GitHub flow + CI pipeline
# =====================================================================
def fig10():
    fig, ax = new_ax(14, 4.6)
    title(ax, "Figure 10 — GitHub flow + CI pipeline (.github/workflows/maven.yml)", fs=14)
    steps = [
        ("Feature branch\ncopy-paste-\nbasic-editing-labs", "#fff2cc"),
        ("Pull request\nto develop", "#fff2cc"),
        ("GitHub Actions\ncheckout · JDK 23\nTemurin · Maven cache", C_BOX),
        ("mvn -B clean install\nall Maven modules", C_BOX),
        ("mvn test\nincl. the 13\ncopy/paste tests", C_BOX),
        ("Green build =\nnew verified\nbaseline", C_OK),
    ]
    xs = [0.085, 0.25, 0.415, 0.585, 0.75, 0.915]
    for (t, fc), cx in zip(steps, xs):
        box(ax, cx, 0.56, 0.138, 0.40, t, fc=fc, fs=10)
    for a, b in zip(xs[:-1], xs[1:]):
        arrow(ax, a + 0.071, 0.56, b - 0.071, 0.56)
    ax.text(0.5, 0.13,
            "Triggers: push & pull_request on develop — the suite is headless (no OS clipboard),"
            " so it runs unchanged on the build server",
            ha="center", fontsize=10.5, style="italic",
            bbox=dict(fc="#f4f6f8", ec="#aaaaaa", pad=5))
    save(fig, "fig10-ci-pipeline.png")


fig01_clean()
fig02()
fig03()
fig04()
fig05()
fig06()
fig07()
fig08()
fig09()
fig10()
# remove the discarded draft if it was written
tmp = os.path.join(OUT, "_tmp_discard.png")
if os.path.exists(tmp):
    os.remove(tmp)
print("done")
