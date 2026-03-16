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
| `drawingAlgorithm` | String | No       | Drawing algorithm (e.g., `"NAVIEW"`) |

---

### Nucleotide Object

| Property       | Type    | Required | Description                                |
| -------------- | ------- | -------- | ------------------------------------------ |
| `id`           | Integer | Yes      | Unique identifier                          |
| `number`       | Integer | Yes      | Position number (can be negative)          |
| `char`         | String  | Yes      | Nucleotide character (A, C, G, U, T)       |
| `outlineColor` | String  | No       | Color for outline (name or RGB "R,G,B")    |
| `innerColor`   | String  | No       | Color for inner fill (name or RGB "R,G,B") |
| `nameColor`    | String  | No       | Color for label (name or RGB "R,G,B")      |

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

### Example

```json
{
  "drawingAlgorithm": "NAVIEW",
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
