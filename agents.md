# Agents Commit Rules

## Commit Message Format

Every commit message must contain:
- Task number (e.g. `[T01]`)
- Short summary
- Prompt(s) that were involved
- Agent as coauthor
- Model as coauthor

The commit **author** must be Niels Falk <niels@nielsfalk.de>. The agent and model are recorded as coauthors only.

### Example

```text
[T01] A summery

promts:
 - The first prompt
 - ...
 
Co-authored-by: Junie <junie@jetbrains.com>
Co-authored-by: GLM-5.2 <glm-5.2@z.ai>
```

## Guidelines

- Commits should be small.
- Multiple commits with the same task number are welcome.
- A task can be modified and unchecked again in `README.md`.
- The **model** coauthor must be the model actually powering the agent for that commit (e.g. `Co-authored-by: GLM-5.2 <glm-5.2@z.ai>`). Do not copy a model name from the example or another commit — use the real model.
- The commit author must be Niels Falk <niels@nielsfalk.de>. Existing commits have been rewritten to use this author; new commits must keep it.
