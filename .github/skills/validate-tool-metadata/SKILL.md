# Validate Tool Metadata

## Purpose

Validate and fix `lighttool.toml` metadata files for Light Phone tools. Ensures required fields are present, permissions are declared correctly, and TOML syntax is valid.

## When to Use

- Creating a new tool and want to validate the metadata file
- Modifying tool metadata (name, version, permissions, server package)
- Catching metadata errors before build
- Verifying declared permissions match code usage

## Workflow

1. **Locate** `lighttool.toml` in the tool module root
2. **Validate** against the required schema:
   - `[tool]` section: `id`, `name`, `version`, `author`
   - `[server]` section: `package`
   - `[permissions]` sections: `required` (array), `optional` (array)
3. **Check** that:
   - Tool ID matches package name pattern: `com.thelightphone.*`
   - Permissions are from the allowed list: `INTERNET`, `CAMERA`, `RECORD_AUDIO`, `LOCATION`, `WAKE_LOCK`, `VIBRATE`, `POST_NOTIFICATIONS`
   - Server package aligns with tool structure
4. **Report** any issues with suggested fixes
5. **Verify** the KSP plugin can validate it at build time

## Example Valid lighttool.toml

```toml
[tool]
id = "com.thelightphone.my-tool"
name = "My Tool"
version = "1.0.0"
author = "Your Name"

[server]
package = "com.thelightphone.my_tool.server"

[permissions]
required = ["INTERNET"]
optional = ["CAMERA"]
```

## Common Issues

| Issue | Fix |
|-------|-----|
| Missing `id` or `name` | Add required field in `[tool]` section |
| Invalid tool ID format | Use `com.thelightphone.your-tool-name` pattern |
| Undeclared permissions in code | Add to `required` or `optional` array |
| TOML syntax error | Validate TOML format (arrays with `[]`, strings with quotes) |
| Server package mismatch | Ensure matches source directory structure |

---

**Related**: [Tool Metadata Reference](docs/tool_metadata/README.md)
