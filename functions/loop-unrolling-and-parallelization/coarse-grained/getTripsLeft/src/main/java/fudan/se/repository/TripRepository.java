package fudan.se.repository;

import com.amazonaws.services.lambda.runtime.Context;

import fudan.se.entity.Trip;
import fudan.se.entity.TripId;

import java.util.ArrayList;


public interface TripRepository {
    ArrayList<Trip> findAll(Context context);
}
