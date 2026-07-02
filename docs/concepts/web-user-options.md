# Web User Options

!!! info
    These options customise the behaviour of the monitoring portal for individual **Web Users**.
    They are set in the user's **Properties** field in the monitoring interface
    (`/do/user/user/<uid>`) using the `monitor.` prefix.

## Portal Appearance

| Option | Type | Default | Description |
|---|---|---|---|
| `monitor.shareFeedback` | Boolean | `true` | Control the visibility of the **Share Feedback** button in the portal header for this Web User. When set to `false`, the feedback button is hidden. The change takes effect on the user's next login |

### Quick-start example

```properties
# Hide the feedback button for service accounts / automated users
monitor.shareFeedback = "no"
```

## Related

- [OpenECPDS Entities](entities.md) — Web Users, Categories, Resources
- [Data User Options](../use-cases/data-portal-user-options.md) — `portal.*` options for Data Portal users
