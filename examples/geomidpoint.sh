#!/bin/sh

# delete any pre-existing index using our desired index name
curl -XDELETE http://localhost:9200/geomidpoint-test

# create an index with a mapping which includes a geo_point field
echo
curl -XPUT http://localhost:9200/geomidpoint-test -d '{
    "settings": {
        "index": {
            "number_of_shards": 1,
            "number_of_replicas": 0
        }
    },
    "mappings": {
        "test1": {
            "properties": {
                "city": {"type": "string"},
                "state": {"type": "string"},
                "coordinates": {"type": "geo_point"}
            }
        }
    }
}'

# index some example documents
echo
curl -XPUT http://localhost:9200/geomidpoint-test/test1/1 -d '{
    "city": "san diego",
    "state": "ca",
    "coordinates": {
        "lat": 32.792095,
        "lon": -117.232337
    }
}'

echo
curl -XPUT http://localhost:9200/geomidpoint-test/test1/2 -d '{
    "city": "la jolla",
    "state": "ca",
    "coordinates": {
        "lat": 32.828326,
        "lon": -117.255854
    }
}'

echo
curl -XPUT http://localhost:9200/geomidpoint-test/test1/3 -d '{
    "city": "san diego",
    "state": "ca",
    "coordinates": {
        "lat": 32.749789,
        "lon": -117.167650
    }
}'

# refresh the index
echo
curl -XPOST http://localhost:9200/geomidpoint-test/_refresh

# perform a faceted search
echo
curl -XGET 'http://localhost:9200/geomidpoint-test/test1/_search?pretty=true' -d '{
    "facets": {
        "facet1": {
            "geo_midpoint": {
                "field": "coordinates"
            }
        }
    }
}'

# clean up the test index
curl -XDELETE http://localhost:9200/geomidpoint-test