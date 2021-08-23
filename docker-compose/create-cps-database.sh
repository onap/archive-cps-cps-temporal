#!/bin/bash

set -e
set -u

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" -d "$POSTGRES_DB" <<-EOSQL
	    CREATE USER ${CPS_CORE_DB_USERNAME} PASSWORD '$CPS_CORE_DB_PASSWORD';
      CREATE DATABASE $CPS_CORE_DB;
      GRANT ALL PRIVILEGES ON DATABASE $CPS_CORE_DB TO $CPS_CORE_DB_USERNAME;
EOSQL

echo "CPS database created"
