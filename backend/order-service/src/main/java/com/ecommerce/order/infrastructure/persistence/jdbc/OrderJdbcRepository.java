package com.ecommerce.order.infrastructure.persistence.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderJdbcRepository {

    private final NamedParameterJdbcTemplate namedJdbc;

    public int batchUpdateStatus(List<Long> ids, String status) {
        String sql = "UPDATE orders SET status = :status WHERE id IN (:ids)";
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("status", status)
            .addValue("ids", ids);
        return namedJdbc.update(sql, params);
    }
}
