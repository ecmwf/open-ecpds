#!/bin/sh
#
# File entrypoint.sh
#
# Purpose: Hawtio startup script (OpenECPDS)
#
# See docs/monitoring/jmx-export.md for why this launches io.hawt.embedded.Main directly
# (via HawtioLauncher) instead of using the official project's JBang-based "hawtio" CLI.
#

exec java $JAVA_OPTS -cp "/opt/hawtio/classes:/opt/hawtio/lib/*" HawtioLauncher
