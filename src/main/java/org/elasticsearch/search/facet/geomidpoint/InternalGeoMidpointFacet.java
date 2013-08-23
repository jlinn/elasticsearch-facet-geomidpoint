package org.elasticsearch.search.facet.geomidpoint;

import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.bytes.HashedBytesArray;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentBuilderString;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.InternalFacet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Joe Linn
 * Date: 8/21/13
 * Time: 2:36 PM
 */
public class InternalGeoMidpointFacet extends InternalFacet implements GeoMidpointFacet{
    private static final BytesReference STREAM_TYPE = new HashedBytesArray(Strings.toUTF8Bytes("geoMidpoint"));

    private List<GeoPoint> points;
    private long count;

    private double lat = Double.NaN;
    private double lon = Double.NaN;

    static Stream STREAM = new Stream(){
        @Override
        public Facet readFacet(StreamInput streamInput) throws IOException {
            return readGeoMidpointFacet(streamInput);
        }
    };

    InternalGeoMidpointFacet(){
        
    }

    public InternalGeoMidpointFacet(String name, List<GeoPoint> points, long count){
        super(name);
        this.points = points;
        this.count = count;
    }

    public static InternalGeoMidpointFacet readGeoMidpointFacet(StreamInput in) throws IOException{
        InternalGeoMidpointFacet facet = new InternalGeoMidpointFacet();
        facet.readFrom(in);
        return facet;
    }

    public static void registerStreams(){
        Streams.registerStream(STREAM, STREAM_TYPE);
    }

    @Override
    public double getLat() {
        if(Double.isNaN(this.lat)){
            this.computeMidpoint();
        }
        return this.lat;
    }

    @Override
    public double getLon() {
        if(Double.isNaN(this.lon)){
            this.computeMidpoint();
        }
        return this.lon;
    }

    @Override
    public long getCount() {
        return this.count;
    }

    @Override
    public BytesReference streamType() {
        return STREAM_TYPE;
    }

    @Override
    public Facet reduce(ReduceContext reduceContext) {
        List<Facet> facets = reduceContext.facets();
        if(facets.size() == 1){
            return facets.get(0);
        }
        List<GeoPoint> points = new ArrayList<GeoPoint>();
        long count = 0;

        for(Facet facet : facets){
            InternalGeoMidpointFacet midpointFacet = (InternalGeoMidpointFacet) facet;
            points.add(new GeoPoint(midpointFacet.getLat(), midpointFacet.getLon()));
            count += midpointFacet.getCount();
        }
        return new InternalGeoMidpointFacet(getName(), points, count);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    static final class Fields{
        static final XContentBuilderString _TYPE = new XContentBuilderString("_type");
        static final XContentBuilderString LAT = new XContentBuilderString("lat");
        static final XContentBuilderString LON = new XContentBuilderString("lon");
        static final XContentBuilderString COUNT = new XContentBuilderString("count");
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(getName());
        builder.field(Fields._TYPE, GeoMidpointFacet.TYPE);
        builder.field(Fields.LAT, getLat());
        builder.field(Fields.LON, getLon());
        builder.field(Fields.COUNT, getCount());
        builder.endObject();
        return builder;
    }

    private void computeMidpoint(){
        double x = 0;
        double y = 0;
        double z = 0;

        double latRadians;
        double lonRadians;

        long count = 0;

        for(GeoPoint point : this.points){
            if(!Double.isNaN(point.lat()) && !Double.isNaN(point.lon())){
                latRadians = Math.toRadians(point.lat());
                lonRadians = Math.toRadians(point.lon());
                x += Math.cos(latRadians) * Math.cos(lonRadians);
                y += Math.cos(latRadians) * Math.sin(lonRadians);
                z += Math.sin(latRadians);
                count++;
            }
        }

        x /= count;
        y /= count;
        z /= count;

        this.lon = Math.toDegrees(Math.atan2(y, x));
        this.lat = Math.toDegrees(Math.atan2(z, Math.sqrt(x * x + y * y)));
    }
}
