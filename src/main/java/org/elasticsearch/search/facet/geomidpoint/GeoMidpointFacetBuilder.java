package org.elasticsearch.search.facet.geomidpoint;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilderException;
import org.elasticsearch.search.facet.FacetBuilder;

import java.io.IOException;

/**
 * User: Joe Linn
 * Date: 8/21/13
 * Time: 2:23 PM
 */
public class GeoMidpointFacetBuilder extends FacetBuilder{
    private String fieldName;

    /**
     * Constructs a new geo midpoint facet with the provided name
     * @param name the name of the facet
     */
    public GeoMidpointFacetBuilder(String name){
        super(name);
    }

    /**
     * @param fieldName the name of the geo point field to be used
     * @return the current GeoMidpointFacetBuilder object
     */
    public GeoMidpointFacetBuilder field(String fieldName){
        this.fieldName = fieldName;
        return this;
    }

    /**
     * @param global if true, this facet will run in the global scope, and will not be bounded by a query
     * @return the current GeoMidpointFacetBuilder object
     */
    public GeoMidpointFacetBuilder global(boolean global){
        super.global(global);
        return this;
    }

    /**
     * @param filter the filter to be applied to this facet
     * @return the current GeoMidpointFacetBuilder object
     */
    public GeoMidpointFacetBuilder facetFilter(FilterBuilder filter){
        this.facetFilter = filter;
        return this;
    }

    /**
     * @param nested the nested path on which the facet will execute
     * @return the current GeoMidpointFacetBuilder object
     */
    public GeoMidpointFacetBuilder nested(String nested){
        this.nested = nested;
        return this;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        if(fieldName == null){
            throw new SearchSourceBuilderException("field must be set on geo_midpoint facet for facet[" + name + "]");
        }

        builder.startObject(name);

        builder.startObject(GeoMidpointFacet.TYPE);

        builder.field(fieldName);

        builder.endObject();

        addFilterFacetAndGlobal(builder, params);

        builder.endObject();
        return builder;
    }
}
