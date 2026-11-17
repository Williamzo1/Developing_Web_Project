#!/bin/sh
set -eu
cd "$(dirname "$0")"
export SPRING_PROFILES_ACTIVE=local
exec sh ./mvnw spring-boot:run
