package com.melnikov.TicketBookingService.dao;

import com.melnikov.TicketBookingService.entity.Route;

import java.util.Optional;

public interface RouteDao {
    Optional<Integer> findIdByCities(String departureCity, String arrivalCity);
    boolean existsByCities(String departureCity, String arrivalCity);
    Integer createRoute(String departureCity, String arrivalCity);
    Optional<Route> findById(Integer id);
}