package com.keyeswest.trackme.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.keyeswest.trackme.models.Segment;

import java.util.List;
import java.util.UUID;


import timber.log.Timber;

import static com.keyeswest.trackme.data.TrackerBaseHelper.createLocationRecord;
import static com.keyeswest.trackme.data.TrackerBaseHelper.createSegmentRecord;


import static com.keyeswest.trackme.data.TrackerBaseHelper.updateSegmentRecordBoundsDistanceElapsed;
import static com.keyeswest.trackme.data.TrackerBaseHelper.updateSegmentRecordFavoriteStatus;
import static com.keyeswest.trackme.data.TracksContentProvider.CONTENT_URI_RELATIONSHIP_JOIN_SEGMENT_GET_LOCATIONS;

public class Queries {

    public static LocationCursor getLatestLocationBySegmentId(Context context, String segmentId){

        String[] selectionArgs = {segmentId};
        String selectionClause = LocationSchema.LocationTable.COLUMN_SEGMENT_ID + " = ?";
        String orderByClause = LocationSchema.LocationTable.COLUMN_TIME_STAMP + " DESC ";

        Uri queryUri = LocationSchema.LocationTable.CONTENT_URI;
        queryUri = queryUri.buildUpon().appendQueryParameter("limit","1").build();
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(
                queryUri,
                /* Columns; leaving this null returns every column in the table */
                null,
                /* Optional specification for columns in the "where" clause above */
                selectionClause,
                /* Values for "where" clause */
                selectionArgs,
                /* Sort order to return in Cursor */
                orderByClause);

        if (cursor != null){
            LocationCursor locationCursor = new LocationCursor(cursor);
            return locationCursor;
        }

        return null;

    }


    public static SegmentCursor getSegmentsForDateRange(Context context, long startTime,
                                                        long endTime){
        String[] selectionArgs = {Long.toString(startTime), Long.toString(endTime)};
        String selectionClause = SegmentSchema.SegmentTable.COLUMN_TIME_STAMP + " BETWEEN ? AND ? ";
        Uri queryUri = SegmentSchema.SegmentTable.CONTENT_URI;
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(
                queryUri,
                /* Columns; leaving this null returns every column in the table */
                null,
                /* Optional specification for columns in the "where" clause above */
                selectionClause,
                /* Values for "where" clause */
                selectionArgs,
                /* Sort order to return in Cursor */
                null);

        return new SegmentCursor(cursor);

    }

    public static Cursor getSegmentLocationFirstLastTimeStamps(Context context, String segmentId){
        String[] selectionArgs = {segmentId};
        String selectionClause = LocationSchema.LocationTable.COLUMN_SEGMENT_ID + " = ?";
        String column = LocationSchema.LocationTable.COLUMN_TIME_STAMP;
        String[] projection = {"MIN(" + column + ")", "MAX("+ column + ") "};

        Uri queryUri = LocationSchema.LocationTable.CONTENT_URI;
        ContentResolver resolver = context.getContentResolver();

        Cursor cursor = resolver.query(
                queryUri,
                /* Columns; leaving this null returns every column in the table */
                projection,
                /* Optional specification for columns in the "where" clause above */
                selectionClause,
                /* Values for "where" clause */
                selectionArgs,
                /* Sort order to return in Cursor */
                null);

        return cursor;

    }


    /***
     * Inserts a new segment.
     * @param context
     * @return segmentId of the new segment
     */
    public static Uri createNewSegment(Context context) {

        String segmentId = UUID.randomUUID().toString();
        // timestamp in seconds
        long timeStamp = System.currentTimeMillis() / 1000;

        ContentValues values = createSegmentRecord(segmentId, timeStamp);

        ContentResolver contentResolver = context.getContentResolver();

        try {
            Uri segment = contentResolver.insert(SegmentSchema.SegmentTable.CONTENT_URI, values);
            return segment;
        } catch (Exception ex) {
            Timber.e(ex, "Exception raised inserting segment to db.");
            return null;
        }

    }


    public static Segment getSegmentFromUri(Context context, Uri uri){

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(
                uri,
                /* Columns; leaving this null returns every column in the table */
                null,
                /* Optional specification for columns in the "where" clause above */
                null,
                /* Values for "where" clause */
                null,
                /* Sort order to return in Cursor */
                null);
        if (cursor != null){
            SegmentCursor segmentCursor = new SegmentCursor(cursor);
            segmentCursor.moveToNext();
            Segment segment = segmentCursor.getSegment();
            return segment;
        }

        return null;

    }


    public static Segment getSegmentFromSegmentId(Context context, String segmentId){

        String[] selectionArgs = {segmentId};
        String selectionClause = SegmentSchema.SegmentTable.COLUMN_ID + "= ?";
        ContentResolver resolver = context.getContentResolver();

        Cursor cursor = resolver.query(
                 SegmentSchema.SegmentTable.CONTENT_URI,
                /* Columns; leaving this null returns every column in the table */
                null,
                /* Optional specification for columns in the "where" clause above */
                selectionClause,
                /* Values for "where" clause */
                selectionArgs,
                /* Sort order to return in Cursor */
                null);

        if (cursor != null){
            SegmentCursor segmentCursor = new SegmentCursor(cursor);
            segmentCursor.moveToFirst();
            Segment segment = segmentCursor.getSegment();
            segmentCursor.close();
            return segment;
        }

        return null;

    }


    public static int updateSegmentBoundsDistanceElapsedTime(Context context, String segmentId,
                                                  double minLat, double maxLat,
                                                  double minLon, double maxLon,
                                                  double distance, long elpasedTime){

        //update the segment
        String selectionClause = SegmentSchema.SegmentTable.COLUMN_ID + " = ?";
        String[] selectionArgs = {segmentId};
        ContentValues updateValues = updateSegmentRecordBoundsDistanceElapsed(minLat, maxLat,
                minLon, maxLon, distance, elpasedTime);
        ContentResolver resolver = context.getContentResolver();
        int rowsUpdated =resolver.update(SegmentSchema.SegmentTable.CONTENT_URI, updateValues,
                selectionClause, selectionArgs);

        return rowsUpdated;

    }

    public static int updateSegmentFavoriteStatus(Context context, UUID segmentId,
                                                  boolean favoriteStatus){
        //update the segment
        String selectionClause = SegmentSchema.SegmentTable.COLUMN_ID + " = ?";
        String[] selectionArgs = {segmentId.toString()};
        ContentValues updateValues = updateSegmentRecordFavoriteStatus(favoriteStatus);

        ContentResolver resolver = context.getContentResolver();
        int rowsUpdated =resolver.update(SegmentSchema.SegmentTable.CONTENT_URI, updateValues,
                selectionClause, selectionArgs);


        return rowsUpdated;
    }

    public static int updateSegmentDuration(Context context, String segmentId, long duration){
        String selectionClause = SegmentSchema.SegmentTable.COLUMN_ID + " = ?";
        String[] selectionArgs = {segmentId};
        ContentValues updateValues = TrackerBaseHelper.updateSegmentDuration(duration);

        ContentResolver resolver = context.getContentResolver();
        int rowsUpdated =resolver.update(SegmentSchema.SegmentTable.CONTENT_URI, updateValues,
                selectionClause, selectionArgs);


        return rowsUpdated;
    }


    public static Uri createNewLocationFromSample(Context context,
                                                  android.location.Location sample,
                                                  String segmentId){

        ContentResolver contentResolver = context.getContentResolver();

        long epochTimeSec = sample.getTime();
        ContentValues locationValues = createLocationRecord(epochTimeSec,
                sample.getLatitude(), sample.getLongitude(), segmentId);

        Uri newLocation =contentResolver.insert(LocationSchema.LocationTable.CONTENT_URI,
                locationValues);

        return newLocation;

    }

    public static LocationCursor getLocationsForSegment(Context context, Uri segmentUri){
        String segmentRowId = segmentUri.getLastPathSegment();
        Uri requestUri = CONTENT_URI_RELATIONSHIP_JOIN_SEGMENT_GET_LOCATIONS;
        requestUri = requestUri.buildUpon().appendPath(segmentRowId).build();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(requestUri, null, null,
                null, null);

        LocationCursor locCursor = new LocationCursor(cursor);
        return locCursor;
    }

    public static SegmentCursor getSegmentsFromUriList(Context context, List<Uri> segments){


        String selectionClause = SegmentSchema.SegmentTable._ID + " IN ( ";
        for (int i=0; i< segments.size(); i++){
            String segmentRowId = segments.get(i).getLastPathSegment();
            if (i== (segments.size() -1 )){
                selectionClause = selectionClause.concat(segmentRowId + " ");
            }else{
                selectionClause = selectionClause.concat(segmentRowId + ", ");
            }

        }
        selectionClause = selectionClause.concat(")");

        Uri queryUri = SegmentSchema.SegmentTable.CONTENT_URI;

        ContentResolver contentResolver = context.getContentResolver();

        Cursor cursor = contentResolver.query(queryUri, null, selectionClause,
                null, null);

        SegmentCursor segmentCursor = new SegmentCursor(cursor);

        return segmentCursor;

    }


    public static void deleteTrip(Context context, UUID segmentId){

        // delete locations first
        Uri locationQueryUri = LocationSchema.LocationTable.CONTENT_URI;
        String selectionClause = LocationSchema.LocationTable.COLUMN_SEGMENT_ID + " = ?";
        String[] selectionArgs = {segmentId.toString()};

        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(locationQueryUri, selectionClause, selectionArgs);

        //delete segment
        Uri segmentQueryUri =  SegmentSchema.SegmentTable.CONTENT_URI;
        selectionClause = SegmentSchema.SegmentTable.COLUMN_ID + " = ?";
        contentResolver.delete(segmentQueryUri, selectionClause, selectionArgs);


    }
}
