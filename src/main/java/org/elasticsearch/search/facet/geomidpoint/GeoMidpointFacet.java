package org.elasticsearch.search.facet.geomidpoint;

import org.elasticsearch.search.facet.Facet;

/**
 * User: Joe Linn
 * Date: 8/21/13
 * Time: 1:00 PM
 */

public interface GeoMidpointFacet extends Facet{
    /**
     * The type of the filter facet.
     */
    public static final String TYPE = "geo_midpoint";

    /*public Object facet();

    public Object getFacet();*/

    double getLat();

    double getLon();

    long getCount();
}
