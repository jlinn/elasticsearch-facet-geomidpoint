package org.elasticsearch.test.integration.search.facet;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.facet.geomidpoint.GeoMidpointFacet;
import org.elasticsearch.test.integration.AbstractNodesTests;
import org.junit.Test;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.closeTo;

/**
 * User: Joe Linn
 * Date: 8/22/13
 * Time: 11:25 AM
 */
public class GeoMidpointFacetTest extends AbstractNodesTests{
    protected int numberOfShards(){
        return 2;
    }

    protected int numberOfNodes(){
        return 1;
    }

    protected int numberOfRuns(){
        return 5;
    }

    public void beforeClass() throws Exception{
        Settings settings = ImmutableSettings.settingsBuilder().put("index.bumber_of_shards", numberOfShards()).put("index.number_of_replicas", 0).build();
        for(int i = 0; i < numberOfNodes(); i++){
            startNode("node" + i, settings);
        }
    }

    public void afterClass(){
        closeAllNodes();
    }

    @Test
    public void testGeoMidpointFacets() throws Exception{
        try{
            client().admin().indices().prepareDelete("test").execute().actionGet();
        }
        catch(Exception e){
            // ignore this exception
        }
        String mapping = XContentFactory.jsonBuilder().startObject().startObject("type1").startObject("properties")
                .startObject("coordinates").field("type", "geo_point").endObject().endObject().endObject().endObject().string();
        client().admin().indices().prepareCreate("test").execute().actionGet();
        client().admin().indices().preparePutMapping("test").setType("type1").setSource(mapping).execute().actionGet();

        client().admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();

        client().prepareIndex("test", "type1").setSource(XContentFactory.jsonBuilder().startObject()
            .startObject("coordinates").field("lat", 32.828326).field("lon", -117.255854).endObject().endObject())
                .execute().actionGet();

        client().prepareIndex("test", "type1").setSource(XContentFactory.jsonBuilder().startObject()
                .startObject("coordinates").field("lat", 32.792095).field("lon", -117.232337).endObject().endObject())
                .execute().actionGet();

        client().prepareIndex("test", "type1").setSource(XContentFactory.jsonBuilder().startObject()
                .startObject("coordinates").field("lat", 32.749789).field("lon", -117.167650).endObject().endObject())
                .execute().actionGet();

        client().admin().indices().prepareFlush().setRefresh(true).execute().actionGet();

        client().admin().indices().prepareRefresh().execute().actionGet();

        for(int i = 0; i < numberOfRuns(); i++){
            SearchResponse searchResponse = client().prepareSearch()
                    .setIndices("test")
                    .setSearchType(SearchType.COUNT)
                    .setExtraSource(XContentFactory.jsonBuilder().startObject()
                        .startObject("facets")
                        .startObject("facet1")
                        .startObject("geo_midpoint")
                        .field("field", "coordinates")
                        .endObject()
                        .endObject()
                        .endObject()
                        .endObject())
                    .execute().actionGet();

            logger.trace(searchResponse.toString());

            //assert that we got the expected number of docs
            assertThat((int)searchResponse.getHits().getTotalHits(), equalTo(3));

            GeoMidpointFacet facet = searchResponse.getFacets().facet("facet1");

            //assert that the proper facet was returned
            assertThat(facet.getName(), equalTo("facet1"));
            assertThat(facet.getType(), equalTo("geo_midpoint"));

            //assert that the facet carried out the calculations correctly
            assertThat((int)facet.getCount(), equalTo(3));
            assertThat(facet.getLat(), closeTo(32.790076, 0.000009));
            assertThat(facet.getLon(), closeTo(-117.218601, 0.000009));
        }
    }
}
