DB_URL=jdbc:postgresql://localhost:5432/nicolbase?user=root&password=postgres&sslmode=disable

.PHONY:commit_hook network postgres postgres_sh createdb dropdb db_docs db_schema test server migrateup redis

commit_hook:
	git config core.hooksPath githooks ;

network:
	docker network create nicol-network ;

postgres:
	docker run --rm -it --name postgres --network nicol-network -p 5432:5432 -e POSTGRES_USER=root -e POSTGRES_PASSWORD=postgres -d postgres:14-alpine ;

postgres_sh:
	docker run --rm -it --name postgres --network nicol-network -p 5432:5432 -e TZ=Asia/Shanghai -e PGTZ=Asia/Shanghai -e POSTGRES_USER=root -e POSTGRES_PASSWORD=postgres -d postgres:14-alpine ;

createdb:
	docker exec -it postgres createdb --username=root --owner=root nicolbase ;

dropdb:
	docker exec -it postgres dropdb nicolbase ;

db_docs:
	dbdocs build "src/main/resources/db/design/nicolbase_define.dbml";

db_schema:
	dbml2sql --postgres -o src/main/resources/db/design/nicolbase_schema.sql src/main/resources/db/design/nicolbase_define.dbml ;

migrateup:
	flyway -url="$(DB_URL)" -locations=filesystem:./src/main/resources/db/migration/ migrate

redis:
	docker run --rm -it --name redis -p 6379:6379 -d redis:7-alpine


