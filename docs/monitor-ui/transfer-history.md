# Transfer History

The Transfer History section provides a searchable, filterable log of all
transfer requests that have been processed by OpenECPDS. It covers both successful
and failed transfers across all destinations.


## Transfer History

The history page lists completed transfer requests with their destination, target filename, size, duration, status, and Data Mover. Use the filters at the top to narrow by date range, destination, or status. Click a row to see the full transfer detail including error messages for failures. The screenshot below shows transfers for the `hourly_aq` destination on 2026-07-24.


![Transfer History](img/transfer-history.png)

The cross-destination transfer list also offers **Delivered name** through
**Cols → Custom**, and accepts `delivered=` in the search box and query builder.
The transfer detail page shows it separately from the original **Target**, and
updates it when a delivery completes. It is recorded only for a module-reported
name that differs from Target; an empty value is not proof of delivery to Target.
See [Delivered filename](destinations.md#delivered-filename) for examples,
attempt semantics and the required database upgrade.
