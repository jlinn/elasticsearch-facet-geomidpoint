GeoMidpoint Facet Plugin for Elasticsearch
==========================================

An [Elasticsearch](http://www.elasticsearch.org/) plugin which calculates the geographic midpoint of the given geo_point field values in all matching documents.

## Compatibility

|GeoMidpoint Facet Plugin|Elasticsearch|
|------------------------|-------------|
|0.0.1|0.90.3|

## Usage

```
    # create an index with a mapping which includes a geo_point field
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
                    "coordinates": {"type": "geo_point"}
                }
            }
        }
    }'

    # index some documents
    curl -XPUT http://localhost:9200/geomidpoint-test/test1/1 -d '{
        "coordinates": {
            "lat": 32.792095,
            "lon": -117.232337
        }
    }'

    curl -XPUT http://localhost:9200/geomidpoint-test/test1/2 -d '{
        "coordinates": {
            "lat": 32.828326,
            "lon": -117.255854
        }
    }'

    # perform a faceted search
    curl -XGET 'http://localhost:9200/geomidpoint-test/test1/_search?pretty=true' -d '{
        "facets": {
            "facet1": {
                "geo_midpoint": {
                    "field": "coordinates"
                }
            }
        }
    }'
```