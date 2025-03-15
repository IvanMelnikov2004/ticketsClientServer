package com.melnikov.TicketBookingService.dao;



import com.melnikov.TicketBookingService.entity.Deposit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class DepositDaoImpl implements DepositDao{
    private final JdbcTemplate jdbcTemplate;

    public DepositDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Маппер для преобразования ResultSet в объект Deposit
    private static final class DepositRowMapper implements RowMapper<Deposit> {
        @Override
        public Deposit mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Deposit.builder()
                    .id(rs.getLong("id"))
                    .userId(rs.getLong("user_id"))
                    .amount(rs.getInt("amount")) // Если DECIMAL, можно использовать rs.getBigDecimal()
                    .status(rs.getString("status"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .build();
        }
    }

    // Найти депозит по ID
    public Optional<Deposit> findById(Long id) {
        String sql = "SELECT * FROM deposits WHERE id = ?";
        List<Deposit> deposits = jdbcTemplate.query(sql, new DepositRowMapper(), id);
        return deposits.isEmpty() ? Optional.empty() : Optional.of(deposits.get(0));
    }

    // Найти последние 10 депозитов пользователя (по времени создания)
    public List<Deposit> findLast10ByUserId(Long userId) {
        String sql = "SELECT * FROM deposits WHERE user_id = ? ORDER BY created_at DESC LIMIT 10";
        return jdbcTemplate.query(sql, new DepositRowMapper(), userId);
    }

    // Сохранить новый депозит
    public Deposit save(Deposit deposit) {
        String sql = "INSERT INTO deposits (user_id, amount, status, created_at) " +
                "VALUES (?, ?, ?, ?) RETURNING id";
        Long id = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                deposit.getUserId(),
                deposit.getAmount(),
                deposit.getStatus(),
                deposit.getCreatedAt()
        );
        deposit.setId(id);
        return deposit;
    }

    // Обновить статус депозита
    public void updateStatus(Long id, String status) {
        String sql = "UPDATE deposits SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, id);
    }
}
