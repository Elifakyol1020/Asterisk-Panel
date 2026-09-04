#!/bin/sh
set -eu

python3 - /etc/asterisk/res_odbc.conf.template /etc/asterisk/res_odbc.conf /etc/odbc.ini <<'PY'
import os
import sys
from pathlib import Path

template, res_odbc, odbc_ini = map(Path, sys.argv[1:])
required = ("DB_HOST", "DB_PORT", "DB_NAME", "DB_USERNAME", "DB_PASSWORD")
missing = [name for name in required if not os.environ.get(name)]
if missing:
    raise SystemExit("Missing Asterisk database environment variables: " + ", ".join(missing))

values = {name: os.environ[name] for name in required}
text = template.read_text()
for name, value in values.items():
    if "\n" in value or "\r" in value:
        raise SystemExit(f"Invalid newline in {name}")
    text = text.replace("${" + name + "}", value)
res_odbc.write_text(text)
odbc_ini.write_text(f"""[asterisk-postgres]
Description=Asterisk PostgreSQL Realtime
Driver=PostgreSQL Unicode
Servername={values['DB_HOST']}
Port={values['DB_PORT']}
Database={values['DB_NAME']}
Username={values['DB_USERNAME']}
Password={values['DB_PASSWORD']}
""")
PY

AST_DB_MANAGE="/usr/src/asterisk-${ASTERISK_VERSION}/contrib/ast-db-manage"

if [ -d "$AST_DB_MANAGE/config" ]; then
  cd "$AST_DB_MANAGE"
  python3 - "$AST_DB_MANAGE/config.ini.sample" /tmp/asterisk-config.ini <<'PY'
import os
import sys
from pathlib import Path
from urllib.parse import quote

source, target = map(Path, sys.argv[1:3])
db_user = quote(os.environ["DB_USERNAME"], safe="")
db_password = quote(os.environ["DB_PASSWORD"], safe="")
db_host = os.environ.get("DB_HOST", "postgres")
db_port = os.environ.get("DB_PORT", "5432")
db_name = os.environ["DB_NAME"]
url = f"postgresql://{db_user}:{db_password}@{db_host}:{db_port}/{db_name}"

text = source.read_text()
lines = []
replaced = False
for line in text.splitlines():
    if line.startswith("sqlalchemy.url ="):
        lines.append(f"sqlalchemy.url = {url}")
        replaced = True
    else:
        lines.append(line)
if not replaced:
    lines.append(f"sqlalchemy.url = {url}")
target.write_text("\n".join(lines) + "\n")
PY
  /opt/asterisk-venv/bin/alembic -c /tmp/asterisk-config.ini upgrade head
fi

exec "$@"
