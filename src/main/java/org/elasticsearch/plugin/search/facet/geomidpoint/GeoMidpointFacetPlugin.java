package org.elasticsearch.plugin.search.facet.geomidpoint;

import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.search.facet.FacetModule;
import org.elasticsearch.search.facet.geomidpoint.GeoMidpointFacetParser;

/**
 * User: Joe Linn
 * Date: 8/21/13
 * Time: 12:40 PM
 */
public class GeoMidpointFacetPlugin extends AbstractPlugin{
    @Override
    public String name() {
        return "facet-geomidpoint";
    }

    @Override
    public String description() {
        return "Geographic midpoint facet support.";
    }

    public void onModule(FacetModule facetModule){
        facetModule.addFacetProcessor(GeoMidpointFacetParser.class);
    }
}
