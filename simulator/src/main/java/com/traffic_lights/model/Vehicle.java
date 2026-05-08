package com.traffic_lights.model;

/**
 * Represents a single vehicle entity within the traffic simulation.
 * <p>
 * This record acts as a data carrier for a vehicle's unique identity and its
 * intended path through the intersection. As a {@code record}, it is immutable
 * and provides built-in implementations for accessors, {@code equals()},
 * {@code hashCode()}, and {@code toString()}.
 * </p>
 *
 * @param id The unique alphanumeric identifier for the vehicle.
 * @param startRoad The geographical {@link Direction} from which the vehicle enters the intersection.
 * @param endRoad The geographical {@link Direction} toward which the vehicle is traveling.
 */
public record Vehicle(

    String id,
    Direction startRoad,
    Direction endRoad

)  {
}
