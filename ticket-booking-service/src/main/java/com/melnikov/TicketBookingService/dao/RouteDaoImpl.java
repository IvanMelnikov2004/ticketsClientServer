package com.melnikov.TicketBookingService.dao;

import com.melnikov.TicketBookingService.entity.Route;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.Optional;

@Repository
public class RouteDaoImpl implements RouteDao {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Route> routeRowMapper = (rs, rowNum) ->
            Route.builder()
                    .id(rs.getInt("id"))
                    .departureCity(rs.getString("departure_city"))
                    .arrivalCity(rs.getString("arrival_city"))
                    .build();

    public RouteDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

    }

    @Override
    public Optional<Integer> findIdByCities(String departureCity, String arrivalCity) {
        String sql = "SELECT id FROM routes WHERE departure_city = ? AND arrival_city = ?";
        try {
            return Optional.of(
                    jdbcTemplate.queryForObject(sql, Integer.class, departureCity, arrivalCity)
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByCities(String departureCity, String arrivalCity) {
        String sql = "SELECT EXISTS(SELECT 1 FROM routes WHERE departure_city = ? AND arrival_city = ?)";
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Boolean.class, departureCity, arrivalCity)
        );
    }

    @Override
    public Integer createRoute(String departureCity, String arrivalCity) {
        String sql = """
            INSERT INTO routes (departure_city, arrival_city)
            VALUES (?, ?)
            ON CONFLICT (departure_city, arrival_city) DO NOTHING
            RETURNING id
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, departureCity);
            ps.setString(2, arrivalCity);
            return ps;
        }, keyHolder);

        keyHolder.getKey();
        return keyHolder.getKey().intValue();
    }

    @Override
    public Optional<Route> findById(Integer id) {
        String sql = "SELECT * FROM routes WHERE id = ?";
        try {
            return Optional.of(
                    jdbcTemplate.queryForObject(sql, routeRowMapper, id)
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}