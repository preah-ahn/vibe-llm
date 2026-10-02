---
name: Slate Indigo Enterprise Workspace
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#464555'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#777587'
  outline-variant: '#c7c4d8'
  surface-tint: '#4d44e3'
  primary: '#3525cd'
  on-primary: '#ffffff'
  primary-container: '#4f46e5'
  on-primary-container: '#dad7ff'
  inverse-primary: '#c3c0ff'
  secondary: '#565e74'
  on-secondary: '#ffffff'
  secondary-container: '#dae2fd'
  on-secondary-container: '#5c647a'
  tertiary: '#005338'
  on-tertiary: '#ffffff'
  tertiary-container: '#006e4b'
  on-tertiary-container: '#67f4b7'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e2dfff'
  primary-fixed-dim: '#c3c0ff'
  on-primary-fixed: '#0f0069'
  on-primary-fixed-variant: '#3323cc'
  secondary-fixed: '#dae2fd'
  secondary-fixed-dim: '#bec6e0'
  on-secondary-fixed: '#131b2e'
  on-secondary-fixed-variant: '#3f465c'
  tertiary-fixed: '#6ffbbe'
  tertiary-fixed-dim: '#4edea3'
  on-tertiary-fixed: '#002113'
  on-tertiary-fixed-variant: '#005236'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.025em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 26px
    fontWeight: '600'
    lineHeight: 34px
    letterSpacing: -0.015em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.015em
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.01em
  title-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: -0.01em
  title-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: -0.005em
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 26px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 22px
  body-sm:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.02em
  code-inline:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  gutter: 1.5rem
  gutter-mobile: 0.75rem
  margin: 2rem
  margin-mobile: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1rem
  space-xl: 1.5rem
---

## Brand & Style

This design system expresses a focused, ultra-refined, and dependable aesthetic tailored for enterprise-grade productivity platforms, collaborative documentation workspaces, and conversational AI tooling.

The aesthetic philosophy balances **Corporate Modernism** with **Engineered Minimalism**:
- **Clarity over ornament**: Chrome and decorative elements recede into the background, ensuring high-density enterprise data and authoring surfaces stay front and center.
- **Cognitive comfort**: Balanced contrast, deliberate whitespace, and subdued slate tints minimize eye strain during long working hours.
- **Architectural precision**: Crisp micro-borders, deliberate alignment lines, and consistent optical geometry establish deep trust, structural rhythm, and functional authority.
- **Intelligent vibrancy**: Deep, luminous indigo signals high agency and computational focus, balanced by sharp emerald accents that reassure users with clear success states.

## Colors

The palette leverages a cool slate scale grounded in rich indigo and operational emerald:

- **Primary (`#4F46E5` / `#4338CA` on hover)**: Applied to critical calls-to-action, active document selections, AI prompt triggers, and brand touchpoints.
- **Secondary (`#0F172A`)**: The deep slate anchor used for high-emphasis typography, prominent headers, solid dark icons, and structural sidebar canvas elements.
- **Tertiary (`#10B981` / `#059669`)**: Precise emerald indicator reserved for operational status badges, completed automation flows, live deployment indicators, and validated fields.
- **Neutral Stack**:
  - `Canvas Canvas`: `#F8FAFC` (Slate 50) for outer frame and viewport background.
  - `Surface Default`: `#FFFFFF` for primary cards, editor canvas, and sheet modals.
  - `Surface Muted`: `#F1F5F9` (Slate 100) for nested panels, sidebars, table headers, and toolbar ribbons.
  - `Border Subtle`: `#E2E8F0` (Slate 200) for low-contrast divider lines and control boundaries.
  - `Text Muted`: `#64748B` (Slate 500) for metadata, labels, and secondary context.
- **Semantic Accents**:
  - `Warning`: `#F59E0B` (Amber 500) paired with `#FFFBEB` (Amber 50) background.
  - `Danger`: `#EF4444` (Red 500) paired with `#FEF2F2` (Red 50) background.
  - `AI / Agentic Aura`: Soft `#EEF2FF` wash combined with primary indigo borders to signify synthetic insights.

## Typography

The typographic hierarchy coordinates **Plus Jakarta Sans** for structural headlines with **Inter** for sustained readability in data-dense workspaces:

- **Headlines & Document Titles**: Plus Jakarta Sans provides contemporary geometric clarity, crisp apexes, and open counters that remain sharp in high-DPI enterprise environments.
- **Body & Controls**: Inter delivers maximum legibility across complex multi-column layouts, data tables, and rapid-fire AI message streams. Tabular figures (`tnum`) should be enabled across tables, statistics, and counter metrics.
- **Code & Technical Tokens**: JetBrains Mono serves inline parameters, schema keys, syntax tags, and prompt-tuning panels.
- **Scale Continuity**: Fluid sizing scales smoothly downward for dense panels and mobile breakpoints using defined fallback tokens.

## Layout & Spacing

The workspace layout is constructed upon an adaptable, multi-pane productivity model:

- **Structural Grid System**:
  - Desktop (>= 1280px): A multi-column flexible grid system spanning a fixed navigation dock (`64px`–`240px`), flexible document canvas (`max-width: 860px` centered for documents, fluid for data tables), and an optional collapsible AI/Inspector side pane (`360px`–`420px`).
  - Tablet (768px – 1279px): Navigation collapses to an icon bar; side panels shift into slide-over overlays; margins clamp to `1.25rem`.
  - Mobile (< 768px): Single-column vertical flow with top-level tab switches between editor and AI chat surfaces; margins compress to `1rem`.
- **Spacing Rhythm**: All margins, structural gutters, and component padding adhere strictly to an enterprise 4px/8px incremental base. Vertical document rhythm maintains 1.5x body line height for uncompromised scannability.

## Elevation & Depth

Visual depth is achieved through **low-contrast micro-outlines paired with ambient, slate-tinted shadows**, preserving visual clarity without heavy skeuomorphic effects:

- **Surface Level 0 (Base Canvas)**: `#F8FAFC`. Zero elevation, non-interactive foundation.
- **Surface Level 1 (Panels & Cards)**: `#FFFFFF` surrounded by a `1px` crisp border in `#E2E8F0` with a subtle contact drop: `0 1px 2px 0 rgba(15, 23, 42, 0.04)`.
- **Surface Level 2 (Flyouts & Dropdowns)**: `#FFFFFF` with border `#E2E8F0`, ambient shadow `0 4px 12px -2px rgba(15, 23, 42, 0.08), 0 2px 4px -1px rgba(15, 23, 42, 0.04)`.
- **Surface Level 3 (Dialogs, Command Palettes, Modals)**: `#FFFFFF` overlaying a semi-transparent slate wash (`rgba(15, 23, 42, 0.4)`), bounded by `1px solid rgba(226, 232, 240, 0.8)` and deep shadow `0 20px 25px -5px rgba(15, 23, 42, 0.12), 0 8px 10px -6px rgba(15, 23, 42, 0.06)`.
- **Interactive State Elevation**: Hovering on selectable workspace cards elevates the element slightly by adjusting the border to `#CBD5E1` and introducing an indigo-tinted ambient glow `0 4px 14px -1px rgba(79, 70, 229, 0.08)`.

## Shapes

The system standardizes on a **Soft (Level 1)** geometric curve profile. This keeps the environment grounded, modern, and disciplined:

- **Micro Controls & Badges**: `0.25rem` (4px) corner radius for precision in small spaces like status tags, inline code snippets, and table controls.
- **Buttons, Form Inputs & Menu Items**: `0.375rem` to `0.5rem` (6px–8px) corner radius for approachable ergonomics without looking overly toy-like.
- **Cards, Panels & Containers**: `0.5rem` (8px) for modular inner surfaces; `0.75rem` (12px) for larger external floating dialogs and canvas cards.
- **Pills**: Reserved exclusively for conversational user avatars, status pips, and AI suggestion chips.

## Components

### Buttons
- **Primary**: Solid Indigo (`#4F46E5`), text white, subtle top border highlight (`rgba(255,255,255,0.15)`), active state `#4338CA`. Height 36px (compact) or 40px (standard).
- **Secondary**: Surface `#FFFFFF`, border `1px solid #E2E8F0`, text `#0F172A`. Hover background `#F8FAFC`, border `#CBD5E1`.
- **Ghost/Tertiary**: Transparent surface, text `#64748B`. Hover text `#0F172A`, background `#F1F5F9`.
- **AI Action**: Indigo-tinted background `#EEF2FF`, text `#4F46E5`, border `1px solid #C7D2FE`. Hover background `#E0E7FF`.

### Form Controls & Inputs
- **Text Inputs & Selects**: Height 38px, background `#FFFFFF`, border `1px solid #E2E8F0`, radius `6px`, padding `0 12px`.
- **Focus State**: Border `#4F46E5` accompanied by an outer focus ring `0 0 0 3px rgba(79, 70, 229, 0.12)`.
- **Checkboxes & Radios**: 16x16px bounding box, border `1.5px solid #CBD5E1`. Selected state solid `#4F46E5` with white checkmark glyph.

### Status Badges
- **Success (Operational / Complete)**: Background `#ECFDF5`, text `#065F46`, dot `#10B981`, border `1px solid #A7F3D0`.
- **Info (Active / In Progress)**: Background `#EEF2FF`, text `#3730A3`, dot `#4F46E5`, border `1px solid #C7D2FE`.
- **Warning (Attention Needed)**: Background `#FFFBEB`, text `#92400E`, dot `#F59E0B`, border `1px solid #FDE68A`.
- **Neutral (Draft / Offline)**: Background `#F1F5F9`, text `#475569`, dot `#94A3B8`, border `1px solid #E2E8F0`.

### Cards & Workspace Panels
- Background `#FFFFFF`, border `1px solid #E2E8F0`, padding `1.25rem`. Separators within cards use `#F1F5F9`. Headers pair `title-sm` with optional right-aligned metadata chips.

### AI Chat & Prompt Interface
- **User Prompt Bubble**: Background `#F8FAFC`, border `1px solid #E2E8F0`, text `#0F172A`, aligned flush right or structured within an inline thread.
- **AI Response Bubble**: Background `#FFFFFF`, left accent indicator `2px solid #4F46E5`, subtle outer shadow. Includes dedicated micro-action bar (Copy, Retry, Cite, Insert into Doc) appearing on hover.
- **Omni-Prompt Input Box**: Floating dock at viewport bottom, rounded-xl (`12px`), background `#FFFFFF`, border `1px solid #CBD5E1`, ambient shadow `0 10px 15px -3px rgba(15, 23, 42, 0.08)`. Features integrated keyboard shortcut tooltips (`⌘K`, `Enter`).