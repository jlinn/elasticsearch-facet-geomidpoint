package org.elasticsearch.search.facet.geomidpoint;

import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.fielddata.IndexGeoPointFieldData;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapper;
import org.elasticsearch.search.facet.FacetExecutor;
import org.elasticsearch.search.facet.FacetParser;
import org.elasticsearch.search.facet.FacetPhaseExecutionException;
import org.elasticsearch.search.internal.SearchContext;

import java.io.IOException;

/**
 * User: Joe Linn
 * Date: 8/21/13
 * Time: 12:57 PM
 */
public class GeoMidpointFacetParser extends AbstractComponent implements FacetParser{
    @Inject
    public GeoMidpointFacetParser(Settings settings){
        super(settings);
        InternalGeoMidpointFacet.registerStreams();
    }

    @Override
    public String[] types() {
        return new String[]{GeoMidpointFacet.TYPE};
    }

    @Override
    public FacetExecutor.Mode defaultMainMode() {
        return FacetExecutor.Mode.COLLECTOR;
    }

    @Override
    public FacetExecutor.Mode defaultGlobalMode() {
        return FacetExecutor.Mode.COLLECTOR;
    }

    @Override
    public FacetExecutor parse(String facetName, XContentParser parser, SearchContext context) throws IOException {
        String field = null;

        String currentFieldName = null;
        XContentParser.Token token;
        while((token = parser.nextToken()) != XContentParser.Token.END_OBJECT){
            if(token == XContentParser.Token.FIELD_NAME){
                currentFieldName = parser.currentName();
            }
            else if(token.isValue()){
                if("field".equals(currentFieldName)){
                    field = parser.text();
                }
            }
        }
        if(field == null){
            throw new FacetPhaseExecutionException(facetName, "geo_midpoint facet requires [field] to be set.");
        }
        FieldMapper fieldMapper = context.smartNameFieldMapper(field);
        if(fieldMapper == null){
            throw new FacetPhaseExecutionException(facetName, "No mapping found for field [" + field + "].");
        }
        if(!(fieldMapper instanceof GeoPointFieldMapper.GeoStringFieldMapper)){
            throw new FacetPhaseExecutionException(facetName, "field [" + field + "] is not a geo point field, but a " + fieldMapper.fieldDataType().getType());
        }
        IndexGeoPointFieldData indexFieldData = context.fieldData().getForField(fieldMapper);
        return new GeoMidpointFacetExecutor(indexFieldData, context);
    }
}
