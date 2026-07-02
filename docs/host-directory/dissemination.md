# Dissemination Directory

!!! info
    For **Dissemination** hosts the Directory field is a **target-path template** applied to every transfer. Variable tokens (`$host[…]`, `$dataFile[…]`, `$dataTransfer[…]`, etc.) are substituted at transfer time by `TransferManagement.getTargetName()`. The field may also use a **selector syntax** to choose different paths based on conditions, or be a **script** that returns the path dynamically.

## Path Template

Enter a path template. All substitution variables are replaced at transfer time. If the resolved path ends with `/` (or is empty), the transfer's own target filename is appended automatically.

```
/outgoing/$destination[name]/$dataFile[original]
```

```
/outgoing/$host[name]/
```

## Selector Syntax

Use `(condition) path` lines to select different target paths based on variable values. The line with the highest number of matching conditions wins. A bare line (no `(...)`) acts as the default.

```
($$destination[name]==ecpds) /high-priority/$dataFile[original]
($$destination[name]==archive) /archive/$dataFile[metaStream]/$dataFile[original]
/default/$dataFile[original]
```

| Operator | Meaning | Example |
|---|---|---|
| `==` | Equals (or regex if second part is `{pattern}`) | `$host[name]=={ecpds.*}` |
| `!=` | Not equals | `$destination[name]!=test` |
| `.=` | Starts with | `$dataFile[original].=/data/` |
| `=.` | Ends with | `$dataFile[original]=..grib2` |

## Script Mode

!!! note
    Select **JavaScript** or **Python** mode using the radio buttons above the editor — the wrapper is added automatically on save. The script is evaluated by `Format.choose()` via `ScriptManager`; the **return value / last expression** is used as the resolved target path. Variables are substituted before the script runs.

### JavaScript

```javascript
// Return the resolved target path as a string
var host = "$host[name]";
var stream = "$dataFile[metaStream]";
"/outgoing/" + host + "/" + stream + "/";
```

### Python

```python
host   = "$host[name]"
stream = "$dataFile[metaStream]"
print("/outgoing/" + host + "/" + stream + "/")
```

## Variables

All tokens below are substituted before the path is used or the script runs.

### Host

`$host[name]` `$host[comment]` `$host[host]` `$host[login]` `$host[passwd]` `$host[userMail]` `$host[networkCode]` `$host[networkName]` `$host[nickname]`

### Data File

`$dataFile[timeStep]` `$dataFile[arrivedTime]` `$dataFile[id]` `$dataFile[original]` `$dataFile[source]` `$dataFile[formatSize]` `$dataFile[size]` `$dataFile[timeBase]` `$dataFile[timeFile]` `$dataFile[metaTime]` `$dataFile[metaStream]` `$dataFile[checksum]`

### Data Transfer

`$dataTransfer[target]` `$dataTransfer[id]` `$dataTransfer[comment]` `$dataTransfer[identity]` `$dataTransfer[priority]` `$dataTransfer[scheduled]` `$dataTransfer[statusCode]` `$dataTransfer[name]` `$dataTransfer[path]` `$dataTransfer[parent]` `$dataTransfer[asap]`

### Destination

`$destination[name]` `$destination[comment]` `$destination[userMail]`

### Country

`$country[name]` `$country[iso]`

### Transfer Method, Module & Server

`$transferMethod[name]` `$transferMethod[comment]` `$ectransModule[name]` `$transferServer[name]` `$transferServer[host]` `$transferServer[port]` `$transferGroup[name]` `$moverName`

## Related

- [Host Directory Field](index.md)
- [Acquisition Directory](acquisition.md)
- [Replication, Source, Backup & Proxy Directory](replication.md)
- [Transfer Modules](../transfer-modules/index.md)
