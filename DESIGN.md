## Typography and fonts

### 1. General text — Montserrat

**Montserrat** for general interface text, including:
- Body text and descriptions.
- Ingredient names and quantities.
- Form labels and input text.
- Button labels.
- Navigation labels.
- Recipe instructions.
- Supporting text and metadata.

Montserrat Regular as the default. Medium or Semibold for emphasis where appropriate.

### 2. Headings — Montserrat Alternates

**Montserrat Alternates** for headings and prominent titles, including:
- Main screen titles.
- Section headings.
- Recipe titles.
- Pantry headings.
- Settings headings.
- Empty-state headings.
- Other prominent text that benefits from a distinctive appearance.

Appropriate weights to establish hierarchy without making the interface feel heavy or overly decorative.

### 3. Logo — Bookmania-inspired typography

Keep the **Canned Hope** wordmark inspired by Bookmania, using the reference:
https://www.marksimonson.com/notebook/view/the-bookmania-cookbook/

The logo must remain text-only, with no additional symbol or illustration. (will design and implement a logo)

Bookmania is reserved for the brand identity; Montserrat Alternates handles headings, and Montserrat handles general interface text.

Verify the appropriate licensing and embedding permissions before distributing the application.

### 4. Typography rules

- Maintain a consistent typographic hierarchy across all five screens.
- Use Montserrat Alternates for headings and Montserrat for general text.
- Use font weight, size, and spacing to establish hierarchy.
- Avoid using too many different weights on a single screen.
- Keep body text readable on smaller Android screens.
- Ensure sufficient line spacing for recipe instructions.
- Allow long recipe names to wrap without clipping.
- Maintain readable text contrast in both light and dark modes.
- Do not use decorative typography where it compromises usability.

### 5. Android implementation

The application is intended to be developed in Java using native Android layouts.

Use font resources and Android theme/text appearance styles to apply the correct fonts consistently.

Define reusable typography styles for:
- Brand wordmark.
- Screen headings.
- Section headings.
- Recipe titles.
- Body text.
- Supporting text.
- Buttons.
- Form labels.

Avoid manually configuring fonts independently on every screen when a reusable style can provide consistency.
