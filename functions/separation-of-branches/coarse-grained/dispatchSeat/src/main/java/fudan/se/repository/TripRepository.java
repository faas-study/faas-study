package fudan.se.repository;

import fudan.se.entity.Trip;
import fudan.se.entity.TripId;


public interface TripRepository  {

    Trip findByTripId(TripId tripId);

}
