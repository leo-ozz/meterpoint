package eu.meterpoint.producer.domain.session;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

final class SessionRowMapper implements RowMapper<Session> {

    @Override
    public Session mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Session(
                rs.getLong("id"),
                rs.getString("charge_point_id"),
                rs.getInt("transaction_id"),
                rs.getInt("connector_id"),
                rs.getString("start_id_tag"),
                rs.getString("stop_id_tag"),
                rs.getString("meter_start"),
                rs.getString("meter_stop"),
                rs.getString("stop_reason"),
                SessionState.valueOf(rs.getString("state")),
                rs.getObject("start_message_id", UUID.class),
                rs.getObject("stop_message_id", UUID.class),
                instant(rs, "started_occurred_at"),
                instant(rs, "started_received_at"),
                instant(rs, "stopped_occurred_at"),
                instant(rs, "stopped_received_at"),
                instant(rs, "created_at"),
                instant(rs, "updated_at")
        );
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts == null ? null : ts.toInstant();
    }
}