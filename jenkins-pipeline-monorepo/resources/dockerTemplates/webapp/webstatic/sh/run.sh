#!/bin/bash -e
#
# S2I run script for the 'java_service_builder' image.
# The run script executes the server that runs your application.
#
# For more information see the documentation:
#	https://github.com/openshift/source-to-image/blob/master/docs/builder_image.md
#

if [ -z "${NGINX_CONFIG_PATH}" ];then
        nginx_config_path=/webapp/nginx.conf
else
        nginx_config_path=${NGINX_CONFIG_PATH}
fi
nginx -g 'daemon off;' -c ${nginx_config_path}

#nginx -g 'daemon off;' -c /etc/nginx/nginx.conf
