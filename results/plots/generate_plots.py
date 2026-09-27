import csv
from pathlib import Path

import matplotlib.pyplot as plt


ROOT = Path(__file__).resolve().parents[2]
TABLES = ROOT / "results" / "tables"
PLOTS = ROOT / "results" / "plots"
SIZES = [100, 1000, 10000, 100000]
COLORS = {
    "Dynamic Array": "#1f77b4",
    "Linked List": "#ff7f0e",
    "insert beginning": "#1f77b4",
    "remove beginning": "#ff7f0e",
    "insert middle": "#2ca02c",
    "remove middle": "#d62728",
    "insert": "#9467bd",
    "extractMin": "#8c564b",
}


def read_rows(filename):
    with open(TABLES / filename, newline="", encoding="utf-8") as file:
        return list(csv.DictReader(file))


def values_for(rows, field, **filters):
    selected = []

    for row in rows:
        if all(row[key] == value for key, value in filters.items()):
            selected.append((int(row["n"]), float(row[field])))

    selected.sort()
    return [item[1] for item in selected]


def prepare_axis(axis, title, y_label, use_symlog=False):
    axis.set_title(title)
    axis.set_xlabel("Initial size n")
    axis.set_ylabel(y_label)
    axis.set_xscale("log")
    if use_symlog:
        axis.set_yscale("symlog", linthresh=1)
    else:
        axis.set_yscale("log")
    axis.set_xticks(SIZES, ["100", "1,000", "10,000", "100,000"])
    axis.grid(True, which="both", alpha=0.25)


def plot_structure_lines(axis, rows, field):
    for structure in ["Dynamic Array", "Linked List"]:
        axis.plot(
            SIZES,
            values_for(rows, field, structure=structure),
            marker="o",
            linewidth=2,
            label=structure,
            color=COLORS[structure],
        )
    axis.legend()


def plot_update_lines(axis, rows, structure, field):
    for operation in ["insert", "remove"]:
        for position in ["beginning", "middle"]:
            key = operation + " " + position
            label = operation.title() + " at " + position
            marker = "o" if position == "beginning" else "s"
            axis.plot(
                SIZES,
                values_for(
                    rows,
                    field,
                    structure=structure,
                    operation=operation,
                    position=position,
                ),
                marker=marker,
                linewidth=2,
                label=label,
                color=COLORS[key],
            )
    axis.legend(fontsize=9)


def plot_heap_lines(axis, rows, field):
    for operation in ["insert", "extractMin"]:
        axis.plot(
            SIZES,
            values_for(rows, field, operation=operation),
            marker="o",
            linewidth=2,
            label=operation,
            color=COLORS[operation],
        )
    axis.legend()


def create_time_plot(random_access, search, updates, priority):
    figure, axes = plt.subplots(3, 2, figsize=(14, 16))
    figure.suptitle("Execution time vs. input size", fontsize=18)

    prepare_axis(axes[0, 0], "Random access", "Average time (ns)")
    plot_structure_lines(axes[0, 0], random_access, "average_time_ns")

    prepare_axis(axes[0, 1], "Search", "Average time (ns)")
    plot_structure_lines(axes[0, 1], search, "average_time_ns")

    prepare_axis(axes[1, 0], "Dynamic Array updates", "Average time (ns)")
    plot_update_lines(axes[1, 0], updates, "Dynamic Array", "average_time_ns")

    prepare_axis(axes[1, 1], "Linked List updates", "Average time (ns)")
    plot_update_lines(axes[1, 1], updates, "Linked List", "average_time_ns")

    prepare_axis(axes[2, 0], "Min-Heap priority processing", "Average time (ns)")
    plot_heap_lines(axes[2, 0], priority, "average_time_ns")
    axes[2, 1].remove()

    figure.tight_layout(rect=[0, 0, 1, 0.97])
    figure.savefig(PLOTS / "execution_time_vs_n.png", dpi=160, bbox_inches="tight")
    plt.close(figure)


def create_operation_plot(random_access, search, updates, priority):
    figure, axes = plt.subplots(3, 2, figsize=(14, 16))
    figure.suptitle("Operations, comparisons, and accesses vs. input size", fontsize=18)

    prepare_axis(axes[0, 0], "Random access", "Element accesses")
    plot_structure_lines(axes[0, 0], random_access, "element_accesses")

    prepare_axis(axes[0, 1], "Search", "Element comparisons")
    plot_structure_lines(axes[0, 1], search, "element_comparisons")

    prepare_axis(axes[1, 0], "Dynamic Array updates", "Element movements")
    plot_update_lines(axes[1, 0], updates, "Dynamic Array", "metric_count")

    prepare_axis(axes[1, 1], "Linked List updates", "Node accesses", use_symlog=True)
    plot_update_lines(axes[1, 1], updates, "Linked List", "metric_count")

    prepare_axis(axes[2, 0], "Min-Heap priority processing", "Element comparisons")
    plot_heap_lines(axes[2, 0], priority, "element_comparisons")
    axes[2, 1].remove()

    figure.tight_layout(rect=[0, 0, 1, 0.97])
    figure.savefig(PLOTS / "operation_counts_vs_n.png", dpi=160, bbox_inches="tight")
    plt.close(figure)


def main():
    PLOTS.mkdir(parents=True, exist_ok=True)
    random_access = read_rows("random_access.csv")
    search = read_rows("search.csv")
    updates = read_rows("insertion_removal.csv")
    priority = read_rows("priority_processing.csv")
    create_time_plot(random_access, search, updates, priority)
    create_operation_plot(random_access, search, updates, priority)
    print("Plots saved in results/plots")


if __name__ == "__main__":
    main()
