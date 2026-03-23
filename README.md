# Beacon with GraphQL API
## Goal
This repository offers an alternative to GA4GH's Beacon v2  by implementing a GraphQL-based API for genomic data querying. It provides a flexible and efficient way to access genomic datasets, individuals, biosamples through the GraphQL API.

## Deployment
Place the data to be added to the Beacon in the `data` directory. This data needs to be represented in  BFF (Beacon Friendly Format) as is also the case for GA4GH's Beacon v2. Use the beacon2-ri-tools generator: [DanielNaro/beacon2-ri-tools](https://github.com/DanielNaro/beacon2-ri-tools). This is a fork of  [EGA-archive/beacon2-ri-tools](https://github.com/EGA-archive/beacon2-ri-tools), which is modified to keep track of those entries where zygosity is equal to 0.

Then deploy the set of containers using Docker Compose:

```bash
docker compose -f docker-compose.yaml -p graphql_beacon up -d
```

The loading process might have to be restarted again using

```bash
docker start -ai graphql_beacon-beacon-loader-1
```

The loading process can be monitored using any PostgreSQL client to connect to the `beacon` database on port `5433` with username `postgres` and password `password_for_beacon_postgres`.

Once the loading process is complete, the required views need to be created: execute the scripts contained in `src/main/resources/create_views` with a PostgreSQL client.

Then access the GraphQL API at port `9080`.