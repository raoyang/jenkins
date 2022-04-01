#!/bin/bash -e
#
# S2I run script for the 'java_service_builder' image.
# The run script executes the server that runs your application.
#
# For more information see the documentation:
#	https://github.com/openshift/source-to-image/blob/master/docs/builder_image.md
#

cd /tmp/src

CONSOLE_DIR=$( ls -al | grep "^d" | grep Console |awk '{print $NF}' )

if [ "$TARGET_BIN" == "" ];then
	TARGET_BIN="$CONSOLE_DIR/bin/Release/netcoreapp3.0/$CONSOLE_DIR.dll"
fi

EXEC_CMD="dotnet $DOTNET_OPTIONS $TARGET_BIN $DOTNET_ARGS"
echo "$EXEC_CMD"
$EXEC_CMD
