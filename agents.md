# Agents Commit Rules

## Commit Message Format

Every commit message must contain:
- Task number (e.g. `[T01]`)
- Short summary
- Prompt(s) that were involved
- Agent as coauthor
- Model as coauthor

### Example

```text
[T01] A summery

promts:
 - The first prompt
 - ...
 
Co-authored-by: Junie <junie@jetbrains.com>
Co-authored-by: Gemini 3.7 Flash Medium <Gemini3.7FlashMedium@google.com>
```

## Guidelines

- Commits should be small.
- Multiple commits with the same task number are welcome.
- A task can be modified and unchecked again in `README.md`.
