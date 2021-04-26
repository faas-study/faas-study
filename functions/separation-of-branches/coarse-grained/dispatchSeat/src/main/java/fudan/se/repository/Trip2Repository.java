package fudan.se.repository;

import fudan.se.entity.Trip;
import fudan.se.entity.TripId;


public interface Trip2Repository {

    Trip findByTripId(TripId tripId);

}
