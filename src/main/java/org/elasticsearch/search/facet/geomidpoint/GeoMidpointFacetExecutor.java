package org.elasticsearch.search.facet.geomidpoint;

import org.apache.lucene.index.AtomicReaderContext;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.fielddata.GeoPointValues;
import org.elasticsearch.index.fielddata.IndexGeoPointFieldData;
import org.elasticsearch.search.facet.FacetExecutor;
import org.elasticsearch.search.facet.InternalFacet;
import org.elasticsearch.index.fielddata.GeoPointValues.Iter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Joe Linn
 * Date: 8/21/13
 * Time: 2:52 PM
 */
public class GeoMidpointFacetExecutor extends FacetExecutor{
    final IndexGeoPointFieldData indexFieldData;

    List<GeoPoint> points;
    long count;

    public GeoMidpointFacetExecutor(IndexGeoPointFieldData indexFieldData){
        this.indexFieldData = indexFieldData;
    }

    @Override
    public InternalFacet buildFacet(String facetName) {
        return new InternalGeoMidpointFacet(facetName, points, count);
    }

    @Override
    public Collector collector() {
        return new Collector();
    }

    class Collector extends FacetExecutor.Collector{
        protected final Aggregator aggregator = new Aggregator();
        protected GeoPointValues values;

        @Override
        public void postCollection() {
            GeoMidpointFacetExecutor.this.points = aggregator.points;
            GeoMidpointFacetExecutor.this.count = aggregator.count;
        }

        @Override
        public void collect(int i) throws IOException {
            aggregator.onDoc(i, values);
        }

        @Override
        public void setNextReader(AtomicReaderContext atomicReaderContext) throws IOException {
            values = indexFieldData.load(atomicReaderContext).getGeoPointValues();
        }
    }

    public static class Aggregator{
        List<GeoPoint> points = new ArrayList<GeoPoint>();
        long count = 0;

        public void onDoc(int docId, GeoPointValues values){
            if(values.hasValue(docId)){
                final Iter iter = values.getIter(docId);
                while(iter.hasNext()){
                    final GeoPoint next = iter.next();
                    if(!Double.isNaN(next.lat()) && !Double.isNaN(next.lon())){
                        points.add(new GeoPoint(next.lat(), next.lon()));
                        count++;
                    }
                }
            }
        }
    }
}
