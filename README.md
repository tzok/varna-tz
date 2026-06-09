# VARNA Tool

RNA secondary structure visualization tool with JSON input support.

## JSON Input Schema

This tool supports JSON input for custom RNA structure visualization.

### Root Object: `StructureData`

| Property           | Type   | Required | Description                          |
| ------------------ | ------ | -------- | ------------------------------------ |
| `nucleotides`      | Array  | Yes      | List of nucleotide objects           |
| `basePairs`        | Array  | Yes      | List of base pair objects            |
| `stackings`        | Array  | No       | List of stacking objects             |
| `drawingAlgorithm` | String | No       | Drawing algorithm: `NAVIEW`, `RADIATE`, `LINEAR`, `CIRCULAR`, or `VARNA_VIEW` |
| `stackingArrowPlacement` | String | No | Placement of stacking arrowheads: `centered`, `first-partner`, `second-partner`, or `both-partners` |
| `stackingArrowGap` | Number | No | Distance from each base circle edge to the stacking arrowhead visual center |

---

### Nucleotide Object

| Property       | Type    | Required | Description                                |
| -------------- | ------- | -------- | ------------------------------------------ |
| `id`           | Integer | Yes      | Unique identifier                          |
| `number`       | Integer or String | Yes      | Displayed residue identifier. Strings with insertion codes must start with an integer prefix, e.g. `190A` |
| `char`         | String  | Yes      | Nucleotide character (A, C, G, U, T)       |
| `outlineColor` | String  | No       | Color for outline (name or RGB "R,G,B")    |
| `innerColor`   | String  | No       | Color for inner fill (name or RGB "R,G,B") |
| `nameColor`    | String  | No       | Color for label (name or RGB "R,G,B")      |

---

### Insertion Codes

`number` may be provided either as a JSON number like `190` or as a string like `"190A"`.

- The full JSON value is rendered as the visible residue label.
- VARNA keeps its own internal unique numeric numbering for base lookup and pairing logic.
- SVG post-processing uses the leading integer prefix to detect numbering discontinuities and retain every 10th label.
- Residues sharing the same divisible-by-10 prefix, such as `10`, `10A`, and `10B`, keep only the first label in that prefix block.
- The first and last residue labels are always kept.

If a string `number` does not begin with an integer prefix, insertion-code-specific numbering rules are not applied to that residue.

---

### BasePair Object

| Property    | Type    | Required | Description                                 |
| ----------- | ------- | -------- | ------------------------------------------- |
| `id1`       | Integer | Yes      | First nucleotide ID                         |
| `id2`       | Integer | Yes      | Second nucleotide ID                        |
| `edge5`     | String  | Yes      | 5' edge type: `WC`, `SUGAR`, or `HOOGSTEEN` |
| `edge3`     | String  | Yes      | 3' edge type: `WC`, `SUGAR`, or `HOOGSTEEN` |
| `stericity` | String  | Yes      | Stericity: `CIS` or `TRANS`                 |
| `canonical` | Boolean | No       | Whether base pair is canonical              |
| `color`     | String  | No       | Color for the bond (name or RGB)            |
| `thickness` | Number  | No       | Line thickness                              |

---

### Stacking Object

| Property    | Type    | Required | Description          |
| ----------- | ------- | -------- | -------------------- |
| `id1`       | Integer | Yes      | First nucleotide ID  |
| `id2`       | Integer | Yes      | Second nucleotide ID |
| `color`     | String  | No       | Color for stacking   |
| `thickness` | Number  | No       | Line thickness       |

---

### Stacking Arrow Options

- `stackingArrowPlacement` controls where stacking arrowheads are drawn along the interaction.
- Accepted values are `centered`, `first-partner`, `second-partner`, and `both-partners`.
- Accepted aliases:
  - `centered`: `center`, `middle`, `midpoint`
  - `first-partner`: `first`, `near-first`, `near-first-partner`
  - `second-partner`: `second`, `near-second`, `near-second-partner`, `last`, `last-partner`
  - `both-partners`: `both`, `both-ends`, `ends`, `double`
- Invalid or missing `stackingArrowPlacement` values fall back to `centered`.
- `stackingArrowGap` is a positive number in drawing units. It controls the distance from the trimmed base-circle edge to the arrowhead visual center for `first-partner`, `second-partner`, and `both-partners`.
- Invalid or missing `stackingArrowGap` values fall back to the built-in default gap.

---

### Example

```json
{
  "drawingAlgorithm": "NAVIEW",
  "stackingArrowPlacement": "both-partners",
  "stackingArrowGap": 8.0,
  "nucleotides": [
    { "id": 1, "number": 1, "char": "C" },
    { "id": 2, "number": 2, "char": "A" }
  ],
  "basePairs": [
    {
      "id1": 1,
      "id2": 2,
      "edge5": "WC",
      "edge3": "WC",
      "stericity": "CIS",
      "canonical": true
    }
  ],
  "stackings": [{ "id1": 1, "id2": 2 }]
}
```

Sample files in the repository:

- `example.json` shows standard numeric residue numbering.
- `example-icode.json` shows mixed numeric and insertion-code residue labels such as `10A` and `10B`.

---

## CLI Usage

### Build the project

```bash
mvn package
```

### Run with Maven

```bash
mvn exec:java -Dexec.mainClass="pl.poznan.put.varna.AdvancedDrawer" -Dexec.args="path/to/structure.json"
```

### Run with JAR

```bash
java -jar target/varna-tz-1.5.3.jar path/to/structure.json
```

`mvn package` builds a runnable shaded JAR at `target/varna-tz-1.5.3.jar`.

The tool will read the JSON file and render the RNA structure.
