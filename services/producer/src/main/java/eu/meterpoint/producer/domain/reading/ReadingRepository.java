package eu.meterpoint.producer.domain.reading;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ReadingRepository {

    private final JdbcClient jdbcClient;

    public ReadingRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public int insertIgnoringConflict(Reading reading) {
        // TODO
        return 0;
    }
}