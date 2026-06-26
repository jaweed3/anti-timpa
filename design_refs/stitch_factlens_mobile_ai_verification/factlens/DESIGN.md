---
name: FactLens
colors:
  surface: '#f8f9ff'
  surface-dim: '#d8dae0'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f3fa'
  surface-container: '#eceef4'
  surface-container-high: '#e6e8ee'
  surface-container-highest: '#e1e2e8'
  on-surface: '#191c20'
  on-surface-variant: '#414750'
  inverse-surface: '#2e3135'
  inverse-on-surface: '#eff0f7'
  outline: '#717782'
  outline-variant: '#c1c7d2'
  surface-tint: '#0061a4'
  primary: '#00497d'
  on-primary: '#ffffff'
  primary-container: '#0061a4'
  on-primary-container: '#c0dbff'
  inverse-primary: '#9fcaff'
  secondary: '#006d44'
  on-secondary: '#ffffff'
  secondary-container: '#99f2be'
  on-secondary-container: '#0a7148'
  tertiary: '#713700'
  on-tertiary: '#ffffff'
  tertiary-container: '#944a00'
  on-tertiary-container: '#ffceaf'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d1e4ff'
  primary-fixed-dim: '#9fcaff'
  on-primary-fixed: '#001d36'
  on-primary-fixed-variant: '#00497d'
  secondary-fixed: '#9cf5c1'
  secondary-fixed-dim: '#80d8a6'
  on-secondary-fixed: '#002111'
  on-secondary-fixed-variant: '#005232'
  tertiary-fixed: '#ffdcc6'
  tertiary-fixed-dim: '#ffb784'
  on-tertiary-fixed: '#301400'
  on-tertiary-fixed-variant: '#713700'
  background: '#f8f9ff'
  on-background: '#191c20'
  surface-variant: '#e1e2e8'
  success-emerald: '#006D44'
  warning-amber: '#924C00'
  error-red: '#BA1A1A'
  background-almost-white: '#FDFBFF'
  surface-pure-white: '#FFFFFF'
  neutral-gray: '#74777F'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 57px
    fontWeight: '400'
    lineHeight: 64px
    letterSpacing: -0.25px
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '400'
    lineHeight: 40px
  headline-sm:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '400'
    lineHeight: 32px
  title-lg:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '500'
    lineHeight: 28px
  title-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 24px
    letterSpacing: 0.15px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.5px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.25px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.1px
  caption:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
    letterSpacing: 0.4px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  xs: 4px
  sm: 8px
  md: 12px
  lg: 16px
  xl: 24px
  2xl: 32px
  3xl: 48px
---

FactLens Design Context
Product Overview
FactLens is an Android-first AI application that helps users verify information directly from their screen without leaving the current application.
The application consists of two major experiences:

Main mobile application
Floating overlay verification
The floating overlay is the flagship feature.
The design should feel lightweight, trustworthy, and invisible until needed.
Design Philosophy
The product should reduce friction.
The user should never feel like they are switching applications.
Everything should require the fewest possible taps.
Every screen should answer one question:
"How can the user verify information faster?"
Design Keywords
Minimal
Modern
Fast
Professional
Trustworthy
Clean
AI-first
Material 3
Not playful
Not gamified
Inspiration
Google Lens
Google Translate Tap-to-Translate
Android System UI
Perplexity
ChatGPT Mobile
Pixel Design Language
Visual Identity
Primary Color
Blue
Accent Color
Emerald (success)
Amber (warning)
Red (false claim)
Background
Almost White
Cards
Pure White
Dark Mode
Supported
Color Usage
Blue
Primary actions
Success
Verified information
Amber
Needs attention
Red
Likely false
Gray
Neutral information
Never overuse colors.
The interface should remain calm.
Typography
Use one font only.
Inter
Hierarchy
Display
Headline
Title
Body
Caption
Avoid decorative fonts.
Border Radius
Small
8
Medium
16
Large
24
Overlay Card
24
Buttons
16
Spacing System
4
8
12
16
24
32
48
Never use arbitrary spacing.
Elevation
Soft shadows only.
No heavy shadows.
Cards should appear floating but subtle.
Components
Always reuse components.
Never redesign existing components.
Core Components
PrimaryButton
SecondaryButton
FactCard
EvidenceCard
SourceCard
HistoryCard
ConfidenceBadge
OverlayCard
BottomActionBar
LoadingCard
EmptyState
Buttons
Height
48
Primary button
Filled
Secondary
Outlined
Danger
Filled Red
Buttons always have rounded corners.
Cards
Rounded
Soft shadow
Padding 16
Gap 12
Cards should never feel crowded.
Icons
Rounded icons.
Consistent stroke width.
Do not mix icon packs.
Navigation
Bottom Navigation
Maximum
4 tabs
Recommended
Home
History
Saved
Settings
Overlay Design
This is the most important experience.
The overlay should never block the whole screen.
Maximum height
35%
Rounded top corners
Draggable
Dismissable
Expandable
One tap should dismiss it.
Home Screen
Contains
Quick Scan
Recent History
Saved Results
AI Status
No unnecessary widgets.
Scan Result Screen
Structure
Verdict
Confidence
Summary
Evidence
Sources
Actions
Primary action should always remain visible.
Verdict Colors
Supported
Green
Contradicted
Red
Misleading
Amber
Unknown
Gray
Never rely only on colors.
Always include text.
Evidence Card
Each evidence contains
Title
Source
Summary
Confidence
Open Source Button
History
Simple list.
Grouped by day.
Searchable.
No complicated filters.
Empty States
Friendly.
Minimal.
One illustration maximum.
One CTA.
Loading State
Skeleton loading preferred.
Avoid long spinners.
Animations
Fast.
Subtle.
200–300ms.
No excessive motion.
Accessibility
Support Dynamic Text.
Minimum touch target
48dp
High contrast.
Screen reader friendly.
UX Rules
Never require more than three taps for verification.
Never force account creation.
Never interrupt the user.
Always show evidence.
Never show AI confidence without explanation.
Every AI decision must provide supporting evidence.
AI Agent Rules
Do not invent new components.
Reuse existing widgets.
Follow spacing system.
Follow typography hierarchy.
Follow Material 3.
Prioritize readability over decoration.
Minimize visual noise.
The overlay experience is the highest priority.
Whenever unsure, choose the simpler design.
Screen Priority

Overlay Verification
Scan Result
Home
History
Saved
Settings
Design effort should follow this order.