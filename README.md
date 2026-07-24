
# platform-status-frontend

Frontend half of the platform status application, designed to run in the public zone and test various infrastructure elements.

## Internal Authentication

This application uses internal-auth for authentication by default. However, internal-auth may not be available in all environments (e.g., Integration).

### Feature Switch

The internal auth can be controlled via the `internalAuth.enabled` configuration property:

- **Default**: `true` (internal auth is enabled)
- **To disable**: Override in environment-specific config

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
